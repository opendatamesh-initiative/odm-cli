package org.opendatamesh.cli.usecases.devops.starttask;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests of the descriptor reader. Part of BDMD-5443, see the prompt in {@code spdd/prompt/}. */
class StartTaskDescriptorOutboundPortImplTest {

    private final StartTaskDescriptorOutboundPortImpl port = new StartTaskDescriptorOutboundPortImpl();

    @Test
    void whenReadingThenStageKeyOrderIsKept() throws Exception {
        DescriptorLifecycle lifecycle = port.readLifecycle(new ObjectMapper().readTree("""
                {"dataProductDescriptor":"1.0.0","internalComponents":{"lifecycleInfo":{"zeta":[],"alpha":[],"mid":[]}}}
                """));

        assertThat(lifecycle.descriptorVersion()).isEqualTo("1.0.0");
        assertThat(lifecycle.stages().keySet()).containsExactly("zeta", "alpha", "mid");
    }

    @Test
    void whenDollarHrefIsMissingThenHrefIsUsed() throws Exception {
        DescriptorLifecycle lifecycle = port.readLifecycle(new ObjectMapper().readTree("""
                {"dataProductDescriptor":"1.0.0","internalComponents":{"lifecycleInfo":{"dev":[
                  {"order":2,"service":{"href":"azure"},"template":{"name":"t","description":"d"}},
                  {"name":"n","service":{"$href":"gh","href":"ignored"}}]}}}
                """));

        DescriptorTask first = lifecycle.stages().get("dev").get(0);
        assertThat(first.executorRef()).isEqualTo("azure");
        assertThat(first.order()).isEqualTo(2);
        assertThat(first.templateName()).isEqualTo("t");
        assertThat(first.templateDescription()).isEqualTo("d");
        assertThat(first.name()).isNull();
        DescriptorTask second = lifecycle.stages().get("dev").get(1);
        assertThat(second.executorRef()).isEqualTo("gh");
        assertThat(second.order()).isNull();
    }

    @Test
    void whenReadingParamsThenValuesAreStringified() throws Exception {
        DescriptorLifecycle lifecycle = port.readLifecycle(new ObjectMapper().readTree("""
                {"internalComponents":{"lifecycleInfo":{"dev":[{"configurations":{"params":
                  {"text":"a","number":2,"flag":true,"object":{"k":1},"array":[1,"b"],"nothing":null}}}]}}}
                """));

        assertThat(lifecycle.stages().get("dev").get(0).params())
                .containsEntry("text", "a")
                .containsEntry("number", "2")
                .containsEntry("flag", "true")
                .containsEntry("object", "{\"k\":1}")
                .containsEntry("array", "[1,\"b\"]")
                .doesNotContainKey("nothing");
    }

    @Test
    void whenLifecycleInfoIsMissingThenStagesAreEmpty() throws Exception {
        DescriptorLifecycle lifecycle = port.readLifecycle(new ObjectMapper().readTree("{}"));

        assertThat(lifecycle.descriptorVersion()).isNull();
        assertThat(lifecycle.stages()).isEmpty();
    }
}
