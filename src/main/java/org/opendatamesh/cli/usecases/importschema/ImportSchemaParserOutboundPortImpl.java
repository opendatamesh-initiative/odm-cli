package org.opendatamesh.cli.usecases.importschema;

import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.dpds.location.DescriptorLocation;
import org.opendatamesh.dpds.location.UriLocation;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;
import org.opendatamesh.dpds.parser.DPDSParser;
import org.opendatamesh.dpds.parser.DPDSSerializer;
import org.opendatamesh.dpds.parser.ParseOptions;
import org.opendatamesh.dpds.parser.ParseResult;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class ImportSchemaParserOutboundPortImpl implements ImportSchemaParserOutboundPort {

    private final Path descriptorPath;
    private final OdmCliConfiguration odmCliConfiguration;

    ImportSchemaParserOutboundPortImpl(Path descriptorPath, OdmCliConfiguration odmCliConfiguration) {
        this.descriptorPath = descriptorPath;
        this.odmCliConfiguration = odmCliConfiguration;
    }

    @Override
    public DataProductVersionDPDS getDataProductVersion() {
        try {
            DPDSParser descriptorParser = new DPDSParser(
                    "https://raw.githubusercontent.com/opendatamesh-initiative/odm-specification-dpdescriptor/main/schemas/",
                    "1.0.0",
                    null);

            DescriptorLocation descriptorLocation = new UriLocation(Files.readString(descriptorPath));

            ParseOptions options = new ParseOptions();
            options.setValidate(false);
            ParseResult results = descriptorParser.parse(descriptorLocation, options);
            return results.getDescriptorDocument();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveDescriptor(DataProductVersionDPDS descriptor) {
        try (FileWriter writer = new FileWriter(descriptorPath.toFile())) {
            //TODO
            String serializedContent = DPDSSerializer.DEFAULT_JSON_SERIALIZER.serialize(descriptor, odmCliConfiguration.getCliConfiguration().getSaveFormat());
            writer.write(serializedContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
