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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.everit.osgi.dev.lqmg.LQMGException;
import org.everit.osgi.dev.lqmg.schema.AbstractNamingRuleType;

import com.mysema.codegen.model.SimpleType;
import com.mysema.codegen.model.Type;
import com.mysema.query.sql.codegen.DefaultNamingStrategy;
import com.mysema.query.sql.codegen.NamingStrategy;
import com.mysema.query.sql.support.ForeignKeyData;
import com.mysema.query.sql.support.InverseForeignKeyData;
import com.mysema.query.sql.support.PrimaryKeyData;

public class LQMGKeyDataFactory {

    private static final int FK_FOREIGN_COLUMN_NAME = 8;

    private static final int FK_FOREIGN_TABLE_NAME = 7;

    private static final int FK_FOREIGN_SCHEMA_NAME = 6;

    private static final int FK_NAME = 12;

    private static final int FK_PARENT_COLUMN_NAME = 4;

    private static final int FK_PARENT_TABLE_NAME = 3;

    private static final int FK_PARENT_SCHEMA_NAME = 2;

    private static final int PK_COLUMN_NAME = 4;

    private static final int PK_NAME = 6;

    private NamingStrategy namingStrategy = new DefaultNamingStrategy();

    private final ConfigurationContainer configContainer;

    public LQMGKeyDataFactory(ConfigurationContainer configContainer) {
        this.configContainer = configContainer;
    }

    public Map<String, InverseForeignKeyData> getExportedKeys(DatabaseMetaData md,
            String catalog, String schema, String tableName) throws SQLException {
        ResultSet foreignKeys = md.getExportedKeys(catalog, schema, tableName);
        Map<String, InverseForeignKeyData> inverseForeignKeyData = new HashMap<String, InverseForeignKeyData>();
        try {
            while (foreignKeys.next()) {
                String name = foreignKeys.getString(FK_NAME);
                String parentColumnName = foreignKeys.getString(FK_PARENT_COLUMN_NAME);
                String foreignSchemaName = foreignKeys.getString(FK_FOREIGN_SCHEMA_NAME);
                String foreignTableName = foreignKeys.getString(FK_FOREIGN_TABLE_NAME);
                String foreignColumn = foreignKeys.getString(FK_FOREIGN_COLUMN_NAME);
                if (name == null || name.isEmpty()) {
                    name = tableName + "_" + foreignTableName + "_IFK";
                }

                InverseForeignKeyData data = inverseForeignKeyData.get(name);
                if (data == null) {
                    data = new InverseForeignKeyData(name, foreignSchemaName,
                            foreignTableName, createType(foreignSchemaName, foreignTableName));
                    inverseForeignKeyData.put(name, data);
                }
                data.add(parentColumnName, foreignColumn);
            }
            return inverseForeignKeyData;
        } finally {
            foreignKeys.close();
        }
    }

    public Map<String, ForeignKeyData> getImportedKeys(DatabaseMetaData md,
            String catalog, String schema, String tableName) throws SQLException {
        ResultSet foreignKeys = md.getImportedKeys(catalog, schema, tableName);
        Map<String, ForeignKeyData> foreignKeyData = new HashMap<String, ForeignKeyData>();
        try {
            while (foreignKeys.next()) {
                String name = foreignKeys.getString(FK_NAME);
                String parentSchemaName = foreignKeys.getString(FK_PARENT_SCHEMA_NAME);
                String parentTableName = foreignKeys.getString(FK_PARENT_TABLE_NAME);
                String parentColumnName = foreignKeys.getString(FK_PARENT_COLUMN_NAME);
                String foreignColumn = foreignKeys.getString(FK_FOREIGN_COLUMN_NAME);
                if (name == null || name.isEmpty()) {
                    name = tableName + "_" + parentTableName + "_FK";
                }

                ForeignKeyData data = foreignKeyData.get(name);
                if (data == null) {
                    data = new ForeignKeyData(name, parentSchemaName, parentTableName,
                            createType(parentSchemaName, parentTableName));
                    foreignKeyData.put(name, data);
                }
                data.add(foreignColumn, parentColumnName);
            }
            return foreignKeyData;
        } finally {
            foreignKeys.close();
        }
    }

    public Map<String, PrimaryKeyData> getPrimaryKeys(DatabaseMetaData md,
            String catalog, String schema, String tableName) throws SQLException {
        ResultSet primaryKeys = md.getPrimaryKeys(catalog, schema, tableName);
        Map<String, PrimaryKeyData> primaryKeyData = new HashMap<String, PrimaryKeyData>();
        try {
            while (primaryKeys.next()) {
                String name = primaryKeys.getString(PK_NAME);
                String columnName = primaryKeys.getString(PK_COLUMN_NAME);
                if (name == null || name.isEmpty()) {
                    name = tableName + "_PK";
                }

                PrimaryKeyData data = primaryKeyData.get(name);
                if (data == null) {
                    data = new PrimaryKeyData(name);
                    primaryKeyData.put(name, data);
                }
                data.add(columnName);
            }
            return primaryKeyData;
        } finally {
            primaryKeys.close();
        }
    }

    protected Type createType(@Nullable String schemaName, String table) {
        String simpleName = configContainer.resolveClassName(schemaName, table, namingStrategy);
        if (simpleName == null) {
            throw new LQMGException("Cannot resolve class name for '" + table + "' table in '" + schemaName
                    + "' schema.", null);
        }

        ConfigValue<? extends AbstractNamingRuleType> configValue = configContainer.findConfigForEntity(schemaName,
                table);
        AbstractNamingRuleType namingRule = configValue.getNamingRule();
        String packageName = namingRule.getPackage();

        return new SimpleType(packageName + "." + simpleName, packageName, simpleName);
    }
}
