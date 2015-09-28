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
package org.everit.osgi.dev.lqmg.internal;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.everit.osgi.dev.lqmg.internal.schema.xml.AbstractNamingRuleType;
import org.everit.osgi.dev.lqmg.internal.schema.xml.ClassNameRuleType;
import org.everit.osgi.dev.lqmg.internal.schema.xml.PropertyMappingType;
import org.everit.osgi.dev.lqmg.internal.schema.xml.PropertyMappingsType;

import com.mysema.query.codegen.EntityType;
import com.mysema.query.sql.codegen.DefaultNamingStrategy;

public class LQMGNamingStrategy extends DefaultNamingStrategy {

  protected enum DBAttributeType {

    COLUMN("column"), FOREIGN_KEY("foreignKey"), PRIMARY_KEY("primaryKey");

    private String typeName;

    private DBAttributeType(final String typeName) {
      this.typeName = typeName;
    }

    public String getTypeName() {
      return typeName;
    }
  }

  protected final ConfigurationContainer configurationContainer;

  public LQMGNamingStrategy(final ConfigurationContainer configurationContainer) {
    this.configurationContainer = configurationContainer;
  }

  @Override
  public String getDefaultVariableName(final EntityType entityType) {
    String tableName = entityType.getData().get("table").toString();
    String schema = (String) entityType.getData().get("schema");
    String propertyName = configurationContainer.resolveClassName(schema, tableName, this);
    ConfigValue<? extends AbstractNamingRuleType> config =
        configurationContainer.findConfigForEntity(schema,
            tableName);

    AbstractNamingRuleType namingRule = config.getNamingRule();
    String prefix = namingRule.getPrefix();
    String suffix = namingRule.getSuffix();

    if (prefix != null) {
      propertyName = propertyName.substring(prefix.length());
    }

    if (suffix != null) {
      propertyName = propertyName.substring(0, propertyName.length() - suffix.length());
    }

    propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
    return escape(entityType, propertyName);
  }

  protected List<JAXBElement<PropertyMappingType>> getForeignKeyAndPrimaryKeyAndColumnForEntity(
      final EntityType entityType) {

    String tableName = entityType.getData().get("table").toString();
    String schema = (String) entityType.getData().get("schema");
    ConfigValue<? extends AbstractNamingRuleType> configValue =
        configurationContainer.findConfigForEntity(
            schema, tableName);

    if (configValue == null) {
      return null;
    }

    AbstractNamingRuleType namingRule = configValue.getNamingRule();
    if (!(namingRule instanceof ClassNameRuleType)) {
      return null;
    }

    PropertyMappingsType propertyMappings = ((ClassNameRuleType) namingRule).getPropertyMappings();
    if (propertyMappings == null) {
      return null;
    }

    return propertyMappings.getPrimaryKeyAndForeignKeyAndColumn();
  }

  @Override
  public String getPropertyName(final String columnName, final EntityType entityType) {
    String propertyName =
        getPropertyNameFromMapping(columnName, entityType, DBAttributeType.COLUMN);
    if (propertyName != null) {
      return propertyName;
    } else {
      return super.getPropertyName(columnName, entityType);
    }
  }

  @Override
  public String getPropertyNameForForeignKey(String fkName, final EntityType entityType) {
    String propertyName =
        getPropertyNameFromMapping(fkName, entityType, DBAttributeType.FOREIGN_KEY);
    if (propertyName != null) {
      return propertyName;
    } else {
      if (fkName.toLowerCase().startsWith("fk_")) {
        fkName = fkName.substring(3) + "_" + fkName.substring(0, 2);
      }
      return getSuperPropertyName(fkName, entityType);
    }
  }

  @Override
  public String getPropertyNameForPrimaryKey(String pkName, final EntityType entityType) {
    String propertyName =
        getPropertyNameFromMapping(pkName, entityType, DBAttributeType.PRIMARY_KEY);
    if (propertyName != null) {
      return propertyName;
    } else {
      if (pkName.toLowerCase().startsWith("pk_")) {
        pkName = pkName.substring(3) + "_" + pkName.substring(0, 2);
      }
      return getSuperPropertyName(pkName, entityType);
    }
  }

  protected String getPropertyNameFromMapping(final String originalName,
      final EntityType entityType,
      final DBAttributeType dbAttributeType) {

    List<JAXBElement<PropertyMappingType>> foreignKeyAndPrimaryKeyAndColumn =
        getForeignKeyAndPrimaryKeyAndColumnForEntity(entityType);

    if (foreignKeyAndPrimaryKeyAndColumn == null) {
      return null;
    }

    PropertyMappingType selectedMapping = null;
    Iterator<JAXBElement<PropertyMappingType>> iterator =
        foreignKeyAndPrimaryKeyAndColumn.iterator();
    while (iterator.hasNext() && (selectedMapping == null)) {
      JAXBElement<?> jaxbElement = iterator.next();
      if (dbAttributeType.getTypeName().equals(jaxbElement.getName().getLocalPart())) {
        PropertyMappingType propertyMapping = (PropertyMappingType) jaxbElement.getValue();
        if (propertyMapping.getName().equals(originalName)) {
          selectedMapping = propertyMapping;
        }
      }
    }

    if (selectedMapping != null) {
      return selectedMapping.getProperty();
    } else {
      return null;
    }
  }

  protected String getSuperPropertyName(final String columnName, final EntityType entityType) {
    return super.getPropertyName(columnName, entityType);
  }
}
