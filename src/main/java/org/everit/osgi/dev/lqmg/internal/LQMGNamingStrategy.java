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

        COLUMN("column"), PRIMARY_KEY("primaryKey"), FOREIGN_KEY("foreignKey");

        private DBAttributeType(String typeName) {
            this.typeName = typeName;
        }

        private String typeName;

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
        ConfigValue<? extends AbstractNamingRuleType> config = configurationContainer.findConfigForEntity(schema,
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
        ConfigValue<? extends AbstractNamingRuleType> configValue = configurationContainer.findConfigForEntity(
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

    protected String getPropertyNameFromMapping(final String originalName, final EntityType entityType,
            DBAttributeType dbAttributeType) {

        List<JAXBElement<PropertyMappingType>> foreignKeyAndPrimaryKeyAndColumn =
                getForeignKeyAndPrimaryKeyAndColumnForEntity(entityType);

        if (foreignKeyAndPrimaryKeyAndColumn == null) {
            return null;
        }

        PropertyMappingType selectedMapping = null;
        Iterator<JAXBElement<PropertyMappingType>> iterator = foreignKeyAndPrimaryKeyAndColumn.iterator();
        while (iterator.hasNext() && selectedMapping == null) {
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

    protected String getSuperPropertyName(String columnName, EntityType entityType) {
        return super.getPropertyName(columnName, entityType);
    }

    @Override
    public String getPropertyName(String columnName, EntityType entityType) {
        String propertyName = getPropertyNameFromMapping(columnName, entityType, DBAttributeType.COLUMN);
        if (propertyName != null) {
            return propertyName;
        } else {
            return super.getPropertyName(columnName, entityType);
        }
    }

    @Override
    public String getPropertyNameForForeignKey(String fkName, final EntityType entityType) {
        String propertyName = getPropertyNameFromMapping(fkName, entityType, DBAttributeType.FOREIGN_KEY);
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
    public String getPropertyNameForPrimaryKey(String pkName, EntityType entityType) {
        String propertyName = getPropertyNameFromMapping(pkName, entityType, DBAttributeType.PRIMARY_KEY);
        if (propertyName != null) {
            return propertyName;
        } else {
            if (pkName.toLowerCase().startsWith("pk_")) {
                pkName = pkName.substring(3) + "_" + pkName.substring(0, 2);
            }
            return getSuperPropertyName(pkName, entityType);
        }
    }
}
