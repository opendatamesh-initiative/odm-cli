package org.opendatamesh.cli.plugin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class PluginLoader {

    @Value("${cli.config.pluginFolder:./plugins}")
    private String pluginPath;

    @PostConstruct
    public void initialize() {
        System.setProperty("loader.path", pluginPath);
    }


    public ImportPlugin getImportPlugin(String from, String to) {

        List<ImportPlugin> plugins = SpringFactoriesLoader.loadFactories(
                ImportPlugin.class,
                this.getClass().getClassLoader()
        );

        return plugins.stream().filter(plugin -> plugin.supports(from, to)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No ImportPlugin found supporting from '%s' to '%s'", from, to)
                ));
    }
}
