package org.opendatamesh.cli.usecases.devops.starttask;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads the descriptor content as a Jackson tree, which keeps the key order of lifecycleInfo. */
class StartTaskDescriptorOutboundPortImpl implements StartTaskDescriptorOutboundPort {

    @Override
    public DescriptorLifecycle readLifecycle(JsonNode content) {
        String descriptorVersion = text(content.get("dataProductDescriptor"));
        LinkedHashMap<String, List<DescriptorTask>> stages = new LinkedHashMap<>();
        JsonNode lifecycleInfo = content.path("internalComponents").path("lifecycleInfo");
        Iterator<Map.Entry<String, JsonNode>> fields = lifecycleInfo.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> stage = fields.next();
            stages.put(stage.getKey(), readTasks(stage.getValue()));
        }
        return new DescriptorLifecycle(descriptorVersion, stages);
    }

    private List<DescriptorTask> readTasks(JsonNode stage) {
        List<DescriptorTask> tasks = new ArrayList<>();
        stage.forEach(task -> tasks.add(readTask(task)));
        return tasks;
    }

    private DescriptorTask readTask(JsonNode task) {
        JsonNode template = task.path("template");
        JsonNode order = task.get("order");
        return new DescriptorTask(
                text(task.get("name")),
                text(template.get("name")),
                text(template.get("description")),
                executorRef(task.path("service")),
                order != null && order.isNumber() ? order.asInt() : null,
                params(task.path("configurations").path("params"))
        );
    }

    private String executorRef(JsonNode service) {
        String dollarHref = text(service.get("$href"));
        return dollarHref != null ? dollarHref : text(service.get("href"));
    }

    private Map<String, String> params(JsonNode params) {
        Map<String, String> result = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = params.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> param = fields.next();
            JsonNode value = param.getValue();
            if (value.isNull()) {
                continue;
            }
            result.put(param.getKey(), value.isContainerNode() ? value.toString() : value.asText());
        }
        return result;
    }

    private String text(JsonNode node) {
        return node != null && node.isValueNode() && !node.isNull() ? node.asText() : null;
    }
}
