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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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

import com.mysema.query.sql.codegen.NamingStrategy;

public class ConfigurationContainer {

    private static class NullConfigValue extends ConfigValue<AbstractNamingRuleType> {

        public NullConfigValue() {
            super(null, null, null);
        }

    }

    private final Map<ConfigKey, ConfigValue<? extends AbstractNamingRuleType>> cache =
            new HashMap<ConfigKey, ConfigValue<? extends AbstractNamingRuleType>>();

    private final Map<ConfigKey, ConfigValue<ClassNameRuleType>> classNameRuleMap =
            new HashMap<ConfigKey, ConfigValue<ClassNameRuleType>>();

    private final JAXBContext jaxbContext;

    private final Map<ConfigKey, ConfigValue<ClassNameRuleType>> mainClassNameRuleMap =
            new HashMap<ConfigKey, ConfigValue<ClassNameRuleType>>();

    private final Map<ConfigKey, ConfigValue<RegexRuleType>> mainRegexRuleMap =
            new HashMap<ConfigKey, ConfigValue<RegexRuleType>>();

    private final Map<ConfigKey, ConfigValue<RegexRuleType>> regexRuleMap =
            new HashMap<ConfigKey, ConfigValue<RegexRuleType>>();

    private final Map<String, Pattern> patternsByRegex = new HashMap<String, Pattern>();

    private final Set<ConfigPath> processedConfigs = new HashSet<ConfigPath>();

    private final Map<ConfigKey, String> classNameCache = new HashMap<ConfigKey, String>();

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
        URL configurationURL;
        if (bundle == null) {
            try {
                configurationURL = new File(configPath.getResource()).toURI().toURL();
            } catch (MalformedURLException e) {
                throw new LQMGException("Could not read configuration from path " + configPath.getResource(), e);
            }
        } else {
            BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
            ClassLoader classLoader = bundleWiring.getClassLoader();
            configurationURL = classLoader.getResource(resource);
        }

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

    public ConfigValue<? extends AbstractNamingRuleType> findConfigForEntity(String schemaName, String entityName) {
        ConfigKey configKey = new ConfigKey(schemaName, entityName);
        ConfigValue<? extends AbstractNamingRuleType> configValue = cache.get(configKey);
        if (configValue != null) {
            if (configValue instanceof NullConfigValue) {
                return null;
            } else {
                return configValue;
            }
        }

        configValue = findEntityConfigInMap(schemaName, entityName, mainClassNameRuleMap);
        if (configValue != null) {
            return configValue;
        }
        configValue = findRegexRuleInMap(schemaName, entityName, mainRegexRuleMap);
        if (configValue != null) {
            return configValue;
        }
        configValue = findEntityConfigInMap(schemaName, entityName, classNameRuleMap);
        if (configValue != null) {
            return configValue;
        }

        return findRegexRuleInMap(schemaName, entityName, regexRuleMap);
    }

    public String resolveClassName(String schemaName, String entityName, NamingStrategy namingStrategy) {
        ConfigKey key = new ConfigKey(schemaName, entityName);
        String className = classNameCache.get(key);
        if (className != null) {
            return className;
        }
        ConfigValue<? extends AbstractNamingRuleType> configValue = findConfigForEntity(schemaName, entityName);
        if (configValue == null) {
            return null;
        }

        AbstractNamingRuleType namingRule = configValue.getNamingRule();
        if (namingRule instanceof RegexRuleType) {
            RegexRuleType regexRule = (RegexRuleType) namingRule;
            String regex = regexRule.getRegex();
            String replacement = regexRule.getReplacement();
            Pattern pattern = getPatternByRegex(regex);
            Matcher matcher = pattern.matcher(entityName);
            String replacedEntityName = matcher.replaceAll(replacement);
            className = namingStrategy.getClassName(replacedEntityName);
        } else if (namingRule instanceof ClassNameRuleType) {
            className = ((ClassNameRuleType) namingRule).getClazz();
        }
        classNameCache.put(key, className);
        return className;
    }

    private ConfigValue<ClassNameRuleType> findEntityConfigInMap(final String schemaName, final String entityName,
            final Map<ConfigKey, ConfigValue<ClassNameRuleType>> map) {

        ConfigKey configKey = new ConfigKey(schemaName, entityName);
        ConfigValue<ClassNameRuleType> configValue = map.get(configKey);
        if (configValue != null) {
            return configValue;
        }
        if (schemaName != null) {
            ConfigKey nullSchemaConfigKey = new ConfigKey(null, entityName);
            return map.get(nullSchemaConfigKey);
        }
        return null;
    }

    private ConfigValue<RegexRuleType> findRegexRuleInMap(final String schemaName, final String entityName,
            final Map<ConfigKey, ConfigValue<RegexRuleType>> map) {

        List<ConfigValue<RegexRuleType>> result = new ArrayList<ConfigValue<RegexRuleType>>();
        if (schemaName != null) {
            for (Entry<ConfigKey, ConfigValue<RegexRuleType>> entry : map.entrySet()) {
                ConfigKey entryKey = entry.getKey();
                ConfigValue<RegexRuleType> configValue = entry.getValue();
                String regex = configValue.getNamingRule().getRegex();
                if (schemaName.equals(entryKey.getSchemaName()) && matches(regex, entityName)) {
                    result.add(configValue);
                }
            }
        }
        if (result.size() > 1) {
            throwMultipleMatchingRegexException(schemaName, entityName, result);
        } else if (result.size() == 1) {
            return result.get(0);
        }

        // No schema matched record, trying to search on records where schema is not defined
        for (Entry<ConfigKey, ConfigValue<RegexRuleType>> entry : map.entrySet()) {
            ConfigValue<RegexRuleType> configValue = entry.getValue();
            RegexRuleType regexRule = configValue.getNamingRule();
            if (regexRule.getSchema() == null && matches(regexRule.getRegex(), entityName)) {
                result.add(configValue);
            }
        }

        if (result.size() > 1) {
            throwMultipleMatchingRegexException(schemaName, entityName, result);
        } else if (result.size() == 1) {
            return result.get(0);
        }
        return null;
    }

    private void throwMultipleMatchingRegexException(String schemaName, String entityName,
            List<ConfigValue<RegexRuleType>> matchingConfigs) {

        StringBuilder sb = new StringBuilder("Cannot decide which configuration should be applied to '")
                .append(entityName).append("' entity of '").append(schemaName).append("' schema. Found matchings:\n");

        for (ConfigValue<RegexRuleType> configValue : matchingConfigs) {
            RegexRuleType namingRule = configValue.getNamingRule();
            sb.append("  Bundle: ").append(configValue.getBundle()).append("; XMLPath: ")
                    .append(configValue.getConfigurationXMLPath()).append("; Schema: ").append(namingRule.getSchema())
                    .append("; Regex: ").append(namingRule.getRegex());
        }
        throw new LQMGException(sb.toString(), null);
    }

    private boolean matches(String regex, String value) {
        Pattern pattern = getPatternByRegex(regex);
        Matcher matcher = pattern.matcher(value);

        return matcher.matches();
    }

    private Pattern getPatternByRegex(String regex) {
        Pattern pattern = patternsByRegex.get(regex);
        if (pattern == null) {
            pattern = Pattern.compile(regex);
            patternsByRegex.put(regex, pattern);
        }
        return pattern;
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

                    validatePackage(configValue);
                    if (bundle == null) {
                        addValueToConfigMap(configKey, configValue, mainClassNameRuleMap);
                    } else {
                        addValueToConfigMap(configKey, configValue, classNameRuleMap);
                    }
                } else {
                    RegexRuleType lqmgEntitySet = (RegexRuleType) lqmgAbstractEntity;
                    ConfigKey configKey = new ConfigKey(lqmgEntitySet.getSchema(),
                            lqmgEntitySet.getRegex());
                    ConfigValue<RegexRuleType> configValue = new ConfigValue<RegexRuleType>(lqmgEntitySet,
                            bundle, xmlConfigurationPath);

                    validatePackage(configValue);
                    if (bundle == null) {
                        addValueToConfigMap(configKey, configValue, mainRegexRuleMap);
                    } else {
                        addValueToConfigMap(configKey, configValue, regexRuleMap);
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

    private void validatePackage(ConfigValue<? extends AbstractNamingRuleType> configValue) {
        AbstractNamingRuleType namingRule = configValue.getNamingRule();
        if (namingRule.getPackage() != null && !namingRule.getPackage().trim().equals("")) {
            return;
        }
        StringBuilder sb = new StringBuilder("Missing java package: ConfigValue [bundle=")
                .append(configValue.getBundle()).append(", configurationXMLPath=")
                .append(configValue.getConfigurationXMLPath()).append(", namingRule.schema=")
                .append(namingRule.getSchema()).append(", ");

        if (namingRule instanceof RegexRuleType) {
            sb.append("namingRule.regex=").append(((RegexRuleType) namingRule).getRegex());
        } else if (namingRule instanceof ClassNameRuleType) {
            sb.append("namingRule.class=").append(((ClassNameRuleType) namingRule).getClazz());
        }
        sb.append("].");
        throw new LQMGException(sb.toString(), null);
    }
}
