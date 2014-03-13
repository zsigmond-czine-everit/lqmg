package org.everit.osgi.dev.lqmg.internal;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.everit.osgi.dev.lqmg.LQMGException;
import org.everit.osgi.dev.lqmg.schema.LQMGAbstractEntityType;
import org.everit.osgi.dev.lqmg.schema.LQMGEntitiesType;
import org.everit.osgi.dev.lqmg.schema.LQMGEntitySetType;
import org.everit.osgi.dev.lqmg.schema.LQMGEntityType;
import org.everit.osgi.dev.lqmg.schema.LQMGType;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

public class ConfigurationContainer {

    private static <T extends LQMGAbstractEntityType> void addValueToConfigMap(ConfigKey configKey,
            ConfigValue<T> configValue,
            Map<ConfigKey, ConfigValue<T>> configMap) {

        ConfigValue<T> existingValue = configMap.get(configKey);
        if (existingValue != null) {
            StringBuilder sb = new StringBuilder("Configuration is defined more than once: ").append(
                    configKey.toString()).append("\n");

            Bundle bundle = configValue.getBundle();
            if (bundle != null) {
                sb.append("Bundle: ").append(bundle.toString()).append("; ");
            }
            sb.append("Path: ").append(configValue.getConfigurationXMLPath());

            throw new LQMGException(sb.toString(), null);
        }
    }

    private final Map<ConfigKey, ConfigValue<LQMGEntityType>> entityConfigs =
            new HashMap<ConfigKey, ConfigValue<LQMGEntityType>>();

    private final Map<ConfigKey, ConfigValue<LQMGEntitySetType>> entitySetConfigs =
            new HashMap<ConfigKey, ConfigValue<LQMGEntitySetType>>();

    private final JAXBContext jaxbContext;

    private final Map<ConfigKey, ConfigValue<LQMGEntityType>> mainEntityConfigs =
            new HashMap<ConfigKey, ConfigValue<LQMGEntityType>>();

    private final Map<ConfigKey, ConfigValue<LQMGEntitySetType>> mainEntitySetConfigs =
            new HashMap<ConfigKey, ConfigValue<LQMGEntitySetType>>();

    public ConfigurationContainer() {
        try {
            this.jaxbContext = JAXBContext.newInstance("", this.getClass().getClassLoader());
        } catch (JAXBException e) {
            throw new LQMGException("Could not create JAXBContext for configuration", e);
        }
    }

    public void addConfiguration(Bundle bundle, String path) {
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        ClassLoader classLoader = bundleWiring.getClassLoader();
        URL configurationURL = classLoader.getResource(path);
        LQMGType lqmgType = unmarshalConfiguration(configurationURL);
        processLQMGType(lqmgType, path, bundle);
    }

    private void processLQMGType(LQMGType lqmgType, String xmlConfigurationPath, Bundle bundle) {
        String defaultPackageName = lqmgType.getPackageName();
        String defaultSchemaName = lqmgType.getSchemaName();

        LQMGEntitiesType entities = lqmgType.getEntities();
        if (entities != null) {
            List<LQMGAbstractEntityType> entityAndEntitySet = entities.getEntityAndEntitySet();
            for (LQMGAbstractEntityType lqmgAbstractEntity : entityAndEntitySet) {
                if (lqmgAbstractEntity.getPackageName() == null) {
                    lqmgAbstractEntity.setPackageName(defaultPackageName);
                }

                if (lqmgAbstractEntity.getSchemaName() == null) {
                    lqmgAbstractEntity.setSchemaName(defaultSchemaName);
                }

                if (lqmgAbstractEntity.isPutSchemaIntoMetadata() == null) {
                    lqmgAbstractEntity.setPutSchemaIntoMetadata(lqmgType.isPutSchemaIntoClass());
                }

                if (lqmgAbstractEntity instanceof LQMGEntityType) {
                    LQMGEntityType lqmgEntity = (LQMGEntityType) lqmgAbstractEntity;
                    ConfigKey configKey = new ConfigKey(lqmgEntity.getSchemaName(),
                            lqmgEntity.getEntityName());
                    ConfigValue<LQMGEntityType> configValue = new ConfigValue<LQMGEntityType>(lqmgEntity,
                            bundle, xmlConfigurationPath);

                    if (bundle == null) {

                    }
                }
            }
        }
    }

    private LQMGType unmarshalConfiguration(URL configurationURL) {
        Unmarshaller unmarshaller;
        try {
            unmarshaller = jaxbContext.createUnmarshaller();
            return (LQMGType) unmarshaller.unmarshal(configurationURL);
        } catch (JAXBException e) {
            throw new LQMGException("Could not unmarshal LQMG configuration: " + configurationURL.toExternalForm(), e);
        }
    }
}
