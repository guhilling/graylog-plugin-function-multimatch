package de.hilling.graylog.plugins.multimatch;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

import java.util.Collection;
import java.util.Collections;

public class MultiMatchFunctionPlugin implements Plugin {
    @Override
    public PluginMetaData metadata() {
        return new MultiMatchFunctionMetaData();
    }

    @Override
    public Collection<PluginModule> modules () {
        return Collections.singletonList(new MultiMatchFunctionModule());
    }
}
