package org.opendatamesh.cli.commands.local.importschema;

import org.opendatamesh.cli.commands.PicoCliCommandExecutor;
import org.opendatamesh.cli.usecases.importschema.ImportSchemaFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportCommandExecutor implements PicoCliCommandExecutor {

    private final ImportSchemaFactory importSchemaFactory;

    private String descriptorFilePath;
    private String from;
    private String to;
    private List<String> inParams;
    private List<String> outParams;

    public ImportCommandExecutor(ImportSchemaFactory importSchemaFactory) {
        this.importSchemaFactory = importSchemaFactory;
    }

    @Override
    public Integer call() {
        importSchemaFactory.getImportSchemaUseCase(
                descriptorFilePath,
                from,
                to,
                parseParams(inParams),
                parseParams(outParams)
        ).execute();
        return 0;
    }

    public void setDescriptorFilePath(String descriptorFilePath) {
        this.descriptorFilePath = descriptorFilePath;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setInParams(List<String> inParams) {
        this.inParams = inParams;
    }

    public void setOutParams(List<String> outParams) {
        this.outParams = outParams;
    }

    //TODO move in an utility class
    private Map<String, String> parseParams(List<String> params) {
        Map<String, String> paramsMap = new HashMap<>();
        if (params == null) {
            return paramsMap;
        }
        for (String p : params) {
            String[] parts = p.split("=", 2);
            if (parts.length == 2) {
                paramsMap.put(parts[0], parts[1]);
            } else {
                throw new IllegalArgumentException("Invalid build argument format [" + p + "]");
            }
        }
        return paramsMap;
    }
}
