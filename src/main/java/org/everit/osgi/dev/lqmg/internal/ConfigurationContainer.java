/**
 * This file is part of Everit - Liquibase-QueryDSL Model Generator.
 *
 * Everit - Liquibase-QueryDSL Model Generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Liquibase-QueryDSL Model Generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Liquibase-QueryDSL Model Generator.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.dev.lqmg.internal;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.everit.osgi.dev.lqmg.LQMGException;
import org.everit.osgi.dev.lqmg.schema.AbstractNamingRuleType;
import org.everit.osgi.dev.lqmg.schema.ClassNameRuleType;
import org.everit.osgi.dev.lqmg.schema.LQMGType;
import org.everit.osgi.dev.lqmg.schema.NamingRulesType;
import org.everit.osgi.dev.lqmg.schema.RegexRuleType;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

public class ConfigurationContainer {

    private static class NullConfigValue extends ConfigValue<AbstractNamingRuleType> {

        public NullConfigValue() {
            super(null, null, null);
        }

    }

    private final Map<ConfigKey, ConfigValue<? extends AbstractNamingRuleType>> cache =
            new HashMap<ConfigKey, ConfigValue<? extends AbstractNamingRuleType>>();

    private final Map<ConfigKey, ConfigValue<ClassNameRuleType>> entityConfigs =
            new HashMap<ConfigKey, ConfigValue<ClassNameRuleType>>();

    private final JAXBContext jaxbContext;

    private final Map<ConfigKey, ConfigValue<ClassNameRuleType>> mainEntityConfigs =
            new HashMap<ConfigKey, ConfigValue<ClassNameRuleType>>();

    private final Map<ConfigKey, ConfigValue<RegexRuleType>> mainNamingRuleConfigs =
            new HashMap<ConfigKey, ConfigValue<RegexRuleType>>();

    private final Map<ConfigKey, ConfigValue<RegexRuleType>> namingRuleConfigs =
            new HashMap<ConfigKey, ConfigValue<RegexRuleType>>();

    private final Map<String, Pattern> patternsByRegex = new HashMap<String, Pattern>();

    private final Set<ConfigPath> processedConfigs = new HashSet<ConfigPath>();

    public ConfigurationContainer() {
        try {
            this.jaxbContext = JAXBContext.newInstance(LQMGType.class.getPackage().getName(), this.getClass()
                    .getClassLoader());
        } catch (JAXBException e) {
            throw new LQMGException("Could not create JAXBContext for configuration", e);
        }
    }

    public void addConfiguration(ConfigPath configPath) {
        if (!processedConfigs.add(configPath)) {
            // If the config file is already processed, just return
            return;
        }
        Bundle bundle = configPath.getBundle();
        String resource = configPath.getResource();
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        ClassLoader classLoader = bundleWiring.getClassLoader();
        URL configurationURL = classLoader.getResource(resource);
        if (configurationURL == null) {
            throw new LQMGException("Configuration file not found on the specified path '" + resource + "' in bundle "
                    + bundle.toString(), null);
        }
        LQMGType lqmgType = unmarshalConfiguration(configurationURL);
        processLQMGType(lqmgType, resource, bundle);
    }

    private <T extends AbstractNamingRuleType> void addValueToConfigMap(ConfigKey configKey,
            ConfigValue<T> configValue,
            Map<ConfigKey, ConfigValue<T>> configMap) {

        ConfigValue<? extends AbstractNamingRuleType> cachedValue = cache.get(configKey);
        if (cachedValue != null) {
            cache.remove(configKey);
        }

        ConfigValue<T> existingValue = configMap.get(configKey);
        if (existingValue != null) {
            StringBuilder sb = new StringBuilder("Configuration is defined more than once: ").append(
                    configKey.toString()).append("\n");

            Bundle bundle = configValue.getBundle();
            if (bundle != null) {
                sb.append("  Bundle: ").append(bundle.toString()).append("; ");
            }
            sb.append("Path: ").append(configValue.getConfigurationXMLPath()).append("\n");

            Bundle existingValueBundle = existingValue.getBundle();
            if (existingValueBundle != null) {
                sb.append("  Bundle: ").append(existingValueBundle.toString()).append("; ");
            }
            sb.append("Path: ").append(existingValue.getConfigurationXMLPath());

            throw new LQMGException(sb.toString(), null);
        }
        configMap.put(configKey, configValue);
    }

    public AbstractNamingRuleType findConfigForKey(ConfigKey key) {
        ConfigValue<? extends AbstractNamingRuleType> configValue = cache.get(key);
        if (configValue != null) {
            if (configValue instanceof NullConfigValue) {
                return null;
            } else {
                return configValue.getNamingRule();
            }
        }

        configValue = findEntityConfigInMap(key, mainEntityConfigs);
        if (configValue != null) {
            return configValue.getNamingRule();
        }

        return null;
    }

    private ConfigValue<ClassNameRuleType> findEntityConfigInMap(final ConfigKey key,
            final Map<ConfigKey, ConfigValue<ClassNameRuleType>> map) {

        ConfigValue<ClassNameRuleType> configValue = map.get(key);
        if (configValue != null) {
            return configValue;
        }
        if (key.getSchemaName() != null) {
            ConfigKey nullSchemaConfigKey = new ConfigKey(null, key.getEntity());
            return map.get(nullSchemaConfigKey);
        }
        return null;
    }

    private ConfigValue<RegexRuleType> findNamingRuleInMap(final ConfigKey key,
            final Map<ConfigKey, ConfigValue<RegexRuleType>> map) {

        String entityName = key.getEntity();

        if (key.getSchemaName() != null) {
            String schemaName = key.getSchemaName();
            for (Entry<ConfigKey, ConfigValue<RegexRuleType>> entry : map.entrySet()) {
                ConfigKey entryKey = entry.getKey();
                if (schemaName.equals(entryKey.getSchemaName())) {

                }
            }
        }

        return null;
    }

    private boolean matches(String regex, String value) {
        Pattern pattern = patternsByRegex.get(regex);
        if (pattern == null) {
            pattern = Pattern.compile(regex);
            patternsByRegex.put(regex, pattern);
        }
        Matcher matcher = pattern.matcher(value);

        // TODO
        return matcher.matches();
    }

    private void processLQMGType(LQMGType lqmgType, String xmlConfigurationPath, Bundle bundle) {
        String defaultPackageName = lqmgType.getDefaultPackage();
        String defaultSchemaName = lqmgType.getDefaultSchema();

        NamingRulesType entities = lqmgType.getNamingRules();
        if (entities != null) {
            List<AbstractNamingRuleType> entityAndEntitySet = entities.getClassNameRuleAndRegexRule();
            for (AbstractNamingRuleType lqmgAbstractEntity : entityAndEntitySet) {
                if (lqmgAbstractEntity.getPackage() == null) {
                    lqmgAbstractEntity.setPackage(defaultPackageName);
                }

                if (lqmgAbstractEntity.getSchema() == null) {
                    lqmgAbstractEntity.setSchema(defaultSchemaName);
                }

                if (lqmgAbstractEntity.isUseSchema() == null) {
                    lqmgAbstractEntity.setUseSchema(lqmgType.isDefaultUseSchema());
                }

                if (lqmgAbstractEntity instanceof ClassNameRuleType) {
                    ClassNameRuleType lqmgEntity = (ClassNameRuleType) lqmgAbstractEntity;
                    ConfigKey configKey = new ConfigKey(lqmgEntity.getSchema(),
                            lqmgEntity.getEntity());
                    ConfigValue<ClassNameRuleType> configValue = new ConfigValue<ClassNameRuleType>(lqmgEntity,
                            bundle, xmlConfigurationPath);

                    if (bundle == null) {
                        addValueToConfigMap(configKey, configValue, mainEntityConfigs);
                    } else {
                        addValueToConfigMap(configKey, configValue, entityConfigs);
                    }
                } else {
                    RegexRuleType lqmgEntitySet = (RegexRuleType) lqmgAbstractEntity;
                    ConfigKey configKey = new ConfigKey(lqmgEntitySet.getSchema(),
                            lqmgEntitySet.getRegex());
                    ConfigValue<RegexRuleType> configValue = new ConfigValue<RegexRuleType>(lqmgEntitySet,
                            bundle, xmlConfigurationPath);

                    if (bundle == null) {
                        addValueToConfigMap(configKey, configValue, mainNamingRuleConfigs);
                    } else {
                        addValueToConfigMap(configKey, configValue, namingRuleConfigs);
                    }
                }
            }
        }
    }

    private LQMGType unmarshalConfiguration(URL configurationURL) {
        Unmarshaller unmarshaller;
        try {
            unmarshaller = jaxbContext.createUnmarshaller();

            @SuppressWarnings("unchecked")
            JAXBElement<LQMGType> rootElement = (JAXBElement<LQMGType>) unmarshaller.unmarshal(configurationURL);
            return rootElement.getValue();
        } catch (JAXBException e) {
            throw new LQMGException("Could not unmarshal LQMG configuration: " + configurationURL.toExternalForm(), e);
        }
    }
}
