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

import org.everit.osgi.dev.lqmg.internal.schema.xml.AbstractNamingRuleType;

import com.mysema.query.codegen.EntityType;
import com.mysema.query.sql.codegen.DefaultNamingStrategy;

public class LQMGNamingStrategy extends DefaultNamingStrategy {

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

    @Override
    public String getPropertyNameForForeignKey(final String fkName, final EntityType entityType) {

        // TODO get it from lqmg table
        return getPropertyName(fkName, entityType);
    }
}
