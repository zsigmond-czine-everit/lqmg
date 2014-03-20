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

import com.mysema.query.sql.codegen.DefaultNamingStrategy;

/**
 * This class defines custom conversion strategy from table to class and column to property names.
 */
public class LQMGNamingStrategy extends DefaultNamingStrategy {

    private final ConfigurationContainer configContainer;

    public LQMGNamingStrategy(ConfigurationContainer configContainer) {
        this.configContainer = configContainer;
    }

    @Override
    public String appendSchema(final String packageName, final String schemaName) {
        String result = super.appendSchema(packageName, schemaName);
        if (result.startsWith(".")) {
            result = result.substring(1);
        }
        return result;
    }

    @Override
    public String getClassName(String tableName) {
        // TODO check configuration and convert
        return super.getClassName(tableName);
    }
}
