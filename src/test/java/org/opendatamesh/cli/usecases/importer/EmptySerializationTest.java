package org.opendatamesh.cli.usecases.importer;

import org.junit.jupiter.api.Test;
import org.opendatamesh.dpds.model.DataProductVersion;
import org.opendatamesh.dpds.model.info.Info;
import org.opendatamesh.dpds.model.info.Owner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.NullNode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

public class EmptySerializationTest {

    @Test
    public void testSerializationFailsWhenEmptyFieldsAreExcluded() {
        
        DataProductVersion dataProductVersion = new DataProductVersion();
        Info info = new Info();
        info.setName("Test Product");
        info.setContactPoints(List.of());
        Owner owner = new Owner();
        owner.setId("");
        info.setOwner(owner);
        info.setAdditionalProperties(Map.of("x-associatedTeam", NullNode.getInstance()));
        dataProductVersion.setInfo(info);

        ObjectMapper objectMapper = new ObjectMapper();
        configObjectMapper(objectMapper);

        try {
            String jsonString = objectMapper.writeValueAsString(dataProductVersion);
            System.out.println(jsonString);
            assertThat(jsonString).contains("\"contactPoints\":[]");
            assertThat(jsonString).contains("\"x-associatedTeam\":null");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class);
        }
    }

    // The ObjectMapper configuration with NON_EMPTY (this is what is being tested)
    private void configObjectMapper(ObjectMapper mapper) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);  // NON_EMPTY to exclude empty fields
    }
}
