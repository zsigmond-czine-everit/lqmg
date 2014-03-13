package org.everit.osgi.dev.lqmg.internal;

import org.everit.osgi.dev.lqmg.schema.LQMGAbstractEntityType;
import org.osgi.framework.Bundle;

public class ConfigValue<T extends LQMGAbstractEntityType> {

    private final Bundle bundle;

    private final String configurationXMLPath;

    private final T entityConfiguration;

    public ConfigValue(T entityConfiguration, Bundle bundle, String configurationXMLPath) {
        this.entityConfiguration = entityConfiguration;
        this.bundle = bundle;
        this.configurationXMLPath = configurationXMLPath;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public String getConfigurationXMLPath() {
        return configurationXMLPath;
    }

    public T getEntityConfiguration() {
        return entityConfiguration;
    }

}
