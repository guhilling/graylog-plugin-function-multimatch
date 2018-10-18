package de.hilling.graylog.plugins.multimatch;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class MultiMatchFunctionMetaData implements PluginMetaData {
    private static final String PLUGIN_PROPERTIES = "de.hilling.graylog.plugins.multimatch/graylog-plugin.properties";

    @Override
    public String getUniqueId() {
        return MultiMatchFunctionPlugin.class.getSimpleName();
    }

    @Override
    public String getName() {
        return "Multiple conditions message matcher";
    }

    @Override
    public String getAuthor() {
        return "Gunnar Hilling <gunnar@hilling.de>";
    }

    @Override
    public URI getURL() {
        return URI.create("https://github.com/guhilling/graylog-plugin-function-multimatch");
    }

    @Override
    public Version getVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "version", Version.from(1, 0, 0, "unknown"));
    }

    @Override
    public String getDescription() {
        return "Pipeline function that matches multiple fields of the message to multiple conditions";
    }

    @Override
    public Version getRequiredVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "graylog.version", Version.from(2, 4, 0));
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
