/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.persistence.lqmg.internal;

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

import org.everit.persistence.lqmg.LQMGException;
import org.everit.persistence.lqmg.internal.schema.xml.AbstractNamingRuleType;
import org.everit.persistence.lqmg.internal.schema.xml.ClassNameRuleType;
import org.everit.persistence.lqmg.internal.schema.xml.LQMGType;
import org.everit.persistence.lqmg.internal.schema.xml.NamingRulesType;
import org.everit.persistence.lqmg.internal.schema.xml.RegexRuleType;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import com.querydsl.sql.SchemaAndTable;

/**
 * The configuration container used to code generation.
 */
public class ConfigurationContainer {

  /**
   * {@link ConfigValue} with <code>null</code>.
   */
  private static class NullConfigValue extends ConfigValue<AbstractNamingRuleType> {

    public NullConfigValue() {
      super(null, null, null);
    }

  }

  private final Map<SchemaAndTable, ConfigValue<? extends AbstractNamingRuleType>> cache =
      new HashMap<>();

  private final Map<SchemaAndTable, ConfigValue<ClassNameRuleType>> classNameRuleMap =
      new HashMap<>();

  private final JAXBContext jaxbContext;

  private final Map<SchemaAndTable, ConfigValue<ClassNameRuleType>> mainClassNameRuleMap =
      new HashMap<>();

  private final Map<SchemaAndTable, ConfigValue<RegexRuleType>> mainRegexRuleMap =
      new HashMap<>();

  private final Map<String, Pattern> patternsByRegex = new HashMap<>();

  private final Set<ConfigPath> processedConfigs = new HashSet<>();

  private final Map<SchemaAndTable, ConfigValue<RegexRuleType>> regexRuleMap =
      new HashMap<>();

  /**
   * Constructor.
   */
  public ConfigurationContainer() {
    try {
      jaxbContext = JAXBContext.newInstance(LQMGType.class.getPackage().getName(), this.getClass()
          .getClassLoader());
    } catch (JAXBException e) {
      throw new LQMGException("Could not create JAXBContext for configuration", e);
    }
  }

  /**
   * Adds a configuration.
   */
  public void addConfiguration(final ConfigPath configPath) {
    if (!processedConfigs.add(configPath)) {
      // If the config file is already processed, just return
      return;
    }
    Bundle bundle = configPath.bundle;
    String resource = configPath.resource;
    URL configurationURL;
    if (bundle == null) {
      try {
        configurationURL = new File(configPath.resource).toURI().toURL();
      } catch (MalformedURLException e) {
        throw new LQMGException(
            "Could not read configuration from path " + configPath.resource, e);
      }
    } else {
      BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
      ClassLoader classLoader = bundleWiring.getClassLoader();
      configurationURL = classLoader.getResource(resource);
    }

    if (configurationURL == null) {
      throw new LQMGException(
          "Configuration file not found on the specified path '" + resource + "' in bundle "
              + bundle.toString(),
          null);
    }
    LQMGType lqmgType = unmarshalConfiguration(configurationURL);
    processLQMGType(lqmgType, resource, bundle);
  }

  private <T extends AbstractNamingRuleType> void addValueToConfigMap(
      final SchemaAndTable schemaAndTable,
      final ConfigValue<T> configValue,
      final Map<SchemaAndTable, ConfigValue<T>> configMap) {

    ConfigValue<? extends AbstractNamingRuleType> cachedValue = cache.get(schemaAndTable);
    if (cachedValue != null) {
      cache.remove(schemaAndTable);
    }

    ConfigValue<T> existingValue = configMap.get(schemaAndTable);
    if (existingValue != null) {
      StringBuilder sb = new StringBuilder("Configuration is defined more than once: ").append(
          schemaAndTable.toString()).append("\n");

      Bundle bundle = configValue.bundle;
      if (bundle != null) {
        sb.append("  Bundle: ").append(bundle.toString()).append("; ");
      }
      sb.append("Path: ").append(configValue.configurationXMLPath).append("\n");

      Bundle existingValueBundle = existingValue.bundle;
      if (existingValueBundle != null) {
        sb.append("  Bundle: ").append(existingValueBundle.toString()).append("; ");
      }
      sb.append("Path: ").append(existingValue.configurationXMLPath);

      throw new LQMGException(sb.toString(), null);
    }
    configMap.put(schemaAndTable, configValue);
  }

  /**
   * Finds a {@link ConfigValue} based on schemaName and entityName.
   */
  public ConfigValue<? extends AbstractNamingRuleType> findConfigForEntity(
      final SchemaAndTable schemaAndTable) {

    ConfigValue<? extends AbstractNamingRuleType> configValue = cache.get(schemaAndTable);

    if (configValue != null) {
      if (configValue instanceof NullConfigValue) {
        return null;
      } else {
        return configValue;
      }
    }

    configValue = findEntityConfigInMap(schemaAndTable, mainClassNameRuleMap);
    if (configValue != null) {
      return configValue;
    }
    configValue = findRegexRuleInMap(schemaAndTable, mainRegexRuleMap);
    if (configValue != null) {
      return configValue;
    }
    configValue = findEntityConfigInMap(schemaAndTable, classNameRuleMap);
    if (configValue != null) {
      return configValue;
    }

    return findRegexRuleInMap(schemaAndTable, regexRuleMap);
  }

  private ConfigValue<ClassNameRuleType> findEntityConfigInMap(final SchemaAndTable schemaAndTable,
      final Map<SchemaAndTable, ConfigValue<ClassNameRuleType>> map) {

    ConfigValue<ClassNameRuleType> configValue = map.get(schemaAndTable);

    // No schema matched record, trying to search on records where schema is not defined
    if (configValue == null) {
      SchemaAndTable noSchemaAndTable = new SchemaAndTable(null, schemaAndTable.getTable());
      configValue = map.get(noSchemaAndTable);
    }
    return configValue;
  }

  private ConfigValue<RegexRuleType> findRegexRuleInMap(final SchemaAndTable schemaAndTable,
      final Map<SchemaAndTable, ConfigValue<RegexRuleType>> map) {

    List<ConfigValue<RegexRuleType>> result = new ArrayList<ConfigValue<RegexRuleType>>();

    String schemaName = schemaAndTable.getSchema();
    String tableName = schemaAndTable.getTable();

    if (schemaName != null) {
      for (Entry<SchemaAndTable, ConfigValue<RegexRuleType>> entry : map.entrySet()) {
        SchemaAndTable entryKey = entry.getKey();
        ConfigValue<RegexRuleType> configValue = entry.getValue();
        String regex = configValue.namingRule.getRegex();
        if (schemaName.equals(entryKey.getSchema()) && matches(regex, tableName)) {
          result.add(configValue);
        }
      }
    }
    validateConfigValueResultSize(schemaAndTable, result);
    if (result.size() == 1) {
      return result.get(0);
    }

    // No schema matched record, trying to search on records where schema is not defined
    for (Entry<SchemaAndTable, ConfigValue<RegexRuleType>> entry : map.entrySet()) {
      ConfigValue<RegexRuleType> configValue = entry.getValue();
      RegexRuleType regexRule = configValue.namingRule;
      if ((regexRule.getSchema() == null) && matches(regexRule.getRegex(), tableName)) {
        result.add(configValue);
      }
    }

    validateConfigValueResultSize(schemaAndTable, result);
    if (result.size() == 1) {
      return result.get(0);
    }
    return null;
  }

  /**
   * Returns the cached and compiled {@link Pattern} by the given regex.
   */
  public Pattern getPatternByRegex(final String regex) {
    Pattern pattern = patternsByRegex.get(regex);
    if (pattern == null) {
      pattern = Pattern.compile(regex);
      patternsByRegex.put(regex, pattern);
    }
    return pattern;
  }

  private boolean matches(final String regex, final String value) {
    Pattern pattern = getPatternByRegex(regex);
    Matcher matcher = pattern.matcher(value);

    return matcher.matches();
  }

  private void processClassNameRuleType(final String xmlConfigurationPath, final Bundle bundle,
      final AbstractNamingRuleType lqmgAbstractEntity) {
    ClassNameRuleType lqmgEntity = (ClassNameRuleType) lqmgAbstractEntity;
    SchemaAndTable schemaAndTable = new SchemaAndTable(
        lqmgEntity.getSchema(), lqmgEntity.getEntity());
    ConfigValue<ClassNameRuleType> configValue =
        new ConfigValue<ClassNameRuleType>(lqmgEntity,
            bundle, xmlConfigurationPath);

    validatePackage(configValue);
    if (bundle == null) {
      addValueToConfigMap(schemaAndTable, configValue, mainClassNameRuleMap);
    } else {
      addValueToConfigMap(schemaAndTable, configValue, classNameRuleMap);
    }
  }

  private void processLQMGType(final LQMGType lqmgType, final String xmlConfigurationPath,
      final Bundle bundle) {
    String defaultPackageName = lqmgType.getDefaultPackage();
    String defaultSchemaName = lqmgType.getDefaultSchema();

    NamingRulesType entities = lqmgType.getNamingRules();
    if (entities == null) {
      return;
    }

    List<AbstractNamingRuleType> entityAndEntitySet = entities.getClassNameRuleAndRegexRule();
    for (AbstractNamingRuleType lqmgAbstractEntity : entityAndEntitySet) {
      if (lqmgAbstractEntity.getPackage() == null) {
        lqmgAbstractEntity.setPackage(defaultPackageName);
      }

      if (lqmgAbstractEntity.getSchema() == null) {
        lqmgAbstractEntity.setSchema(defaultSchemaName);
      }

      if (lqmgAbstractEntity.getPrefix() == null) {
        lqmgAbstractEntity.setPrefix(lqmgType.getDefaultPrefix());
      }

      if (lqmgAbstractEntity.getSuffix() == null) {
        lqmgAbstractEntity.setSuffix(lqmgType.getDefaultSuffix());
      }

      if (lqmgAbstractEntity instanceof ClassNameRuleType) {
        processClassNameRuleType(xmlConfigurationPath, bundle, lqmgAbstractEntity);
      } else if (lqmgAbstractEntity instanceof RegexRuleType) {
        processRegexRuleType(xmlConfigurationPath, bundle, lqmgAbstractEntity);
      } else {
        throw new IllegalArgumentException(
            "Unsupported naming rule type [" + lqmgAbstractEntity + "]");
      }
    }
  }

  private void processRegexRuleType(final String xmlConfigurationPath, final Bundle bundle,
      final AbstractNamingRuleType lqmgAbstractEntity) {
    RegexRuleType lqmgEntitySet = (RegexRuleType) lqmgAbstractEntity;
    SchemaAndTable configKey = new SchemaAndTable(
        lqmgEntitySet.getSchema(), lqmgEntitySet.getRegex());
    ConfigValue<RegexRuleType> configValue = new ConfigValue<RegexRuleType>(lqmgEntitySet,
        bundle, xmlConfigurationPath);

    validatePackage(configValue);
    if (bundle == null) {
      addValueToConfigMap(configKey, configValue, mainRegexRuleMap);
    } else {
      addValueToConfigMap(configKey, configValue, regexRuleMap);
    }
  }

  private void throwMultipleMatchingRegexException(final SchemaAndTable schemaAndTable,
      final List<ConfigValue<RegexRuleType>> matchingConfigs) {

    StringBuilder sb = new StringBuilder("Cannot decide which configuration should be applied to '")
        .append(schemaAndTable.getTable()).append("' table of '").append(schemaAndTable.getSchema())
        .append("' schema. Found matchings:\n");

    for (ConfigValue<RegexRuleType> configValue : matchingConfigs) {
      RegexRuleType namingRule = configValue.namingRule;
      sb.append("  Bundle: ").append(configValue.bundle).append("; XMLPath: ")
          .append(configValue.configurationXMLPath).append("; Schema: ")
          .append(namingRule.getSchema())
          .append("; Regex: ").append(namingRule.getRegex());
    }
    throw new LQMGException(sb.toString(), null);
  }

  private LQMGType unmarshalConfiguration(final URL configurationURL) {
    Unmarshaller unmarshaller;
    try {
      unmarshaller = jaxbContext.createUnmarshaller();

      @SuppressWarnings("unchecked")
      JAXBElement<LQMGType> rootElement =
          (JAXBElement<LQMGType>) unmarshaller.unmarshal(configurationURL);
      return rootElement.getValue();
    } catch (JAXBException e) {
      throw new LQMGException(
          "Could not unmarshal LQMG configuration: " + configurationURL.toExternalForm(), e);
    }
  }

  private void validateConfigValueResultSize(final SchemaAndTable schemaAndTable,
      final List<ConfigValue<RegexRuleType>> result) {
    if (result.size() > 1) {
      throwMultipleMatchingRegexException(schemaAndTable, result);
    }
  }

  private void validatePackage(final ConfigValue<? extends AbstractNamingRuleType> configValue) {
    AbstractNamingRuleType namingRule = configValue.namingRule;
    if ((namingRule.getPackage() != null) && !"".equals(namingRule.getPackage().trim())) {
      return;
    }
    StringBuilder sb = new StringBuilder("Missing java package: ConfigValue [bundle=")
        .append(configValue.bundle).append(", configurationXMLPath=")
        .append(configValue.configurationXMLPath).append(", namingRule.schema=")
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
