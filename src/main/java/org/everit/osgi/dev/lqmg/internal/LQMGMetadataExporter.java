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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.everit.osgi.dev.lqmg.LQMGException;
import org.everit.osgi.dev.lqmg.schema.AbstractNamingRuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.codegen.EntityType;
import com.mysema.query.sql.codegen.DefaultNamingStrategy;
import com.mysema.query.sql.codegen.KeyDataFactory;
import com.mysema.query.sql.codegen.MetaDataExporter;
import com.mysema.query.sql.codegen.NamingStrategy;
import com.mysema.query.sql.support.ForeignKeyData;
import com.mysema.query.sql.support.InverseForeignKeyData;
import com.mysema.query.sql.support.PrimaryKeyData;

public class LQMGMetadataExporter extends MetaDataExporter {

    private static Logger LOGGER = LoggerFactory.getLogger(LQMGMetadataExporter.class);

    private final ConfigurationContainer configurationContainer;

    private final Set<String> packages;

    protected NamingStrategy namingStrategy = new DefaultNamingStrategy();
    
    private final KeyDataFactory keyDataFactory;

    public LQMGMetadataExporter(ConfigurationContainer configurationContainer, String[] packages) {
        this.configurationContainer = configurationContainer;
        this.packages = new HashSet<String>(Arrays.asList(packages));
        this.keyDataFactory = new LQMGKeyDataFactory(configurationContainer);
    }

    @Override
    protected EntityType createEntityType(String schemaName, String tableName, String className) {
        EntityType entityType = super.createEntityType(schemaName, tableName, className);

        ConfigKey configKey = new ConfigKey(schemaName, tableName);
        ConfigValue<? extends AbstractNamingRuleType> configValue = configurationContainer.findConfigForKey(configKey);
        AbstractNamingRuleType namingRule = configValue.getNamingRule();
        if (namingRule != null && !namingRule.isUseSchema()) {
            entityType.getData().remove("schema");
        }
        return entityType;
    }

    protected void handleTable(DatabaseMetaData md, ResultSet tables) throws SQLException {
        String schema = tables.getString("TABLE_SCHEM");
        String entity = tables.getString("TABLE_NAME");
        ConfigKey configKey = new ConfigKey(schema, entity);
        ConfigValue<? extends AbstractNamingRuleType> configValue = configurationContainer.findConfigForKey(configKey);
        if (configValue == null) {
            LOGGER.info("No configuration for table '" + entity + "' in schema '" + schema
                    + "'. Ignoring from metadata class generation");
            return;
        }
        AbstractNamingRuleType namingRule = configValue.getNamingRule();

        String javaPackage = namingRule.getPackage();
        if (packages.size() > 0 || !packages.contains(javaPackage)) {
            LOGGER.info("Java package '" + javaPackage + "' is not included, ignoring entity '" + entity
                    + "' from schema '" + schema + "'.");
            return;
        }

        try {
            callSuper("handleTable", new Class[] { DatabaseMetaData.class, ResultSet.class },
                    new Object[] { md, tables });
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof SQLException) {
                throw (SQLException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
        }
    }

    private void callSuper(String methodName, Class<?>[] argumentTypes, Object[] arguments)
            throws InvocationTargetException {
        try {
            Method superMethod = super.getClass().getMethod(methodName, argumentTypes);
            superMethod.setAccessible(true);
            superMethod.invoke(this, arguments);
        } catch (NoSuchMethodException e) {
            throw new LQMGException("Error in reflection call", e);
        } catch (SecurityException e) {
            throw new LQMGException("Error in reflection call", e);
        } catch (IllegalAccessException e) {
            throw new LQMGException("Error in reflection call", e);
        } catch (IllegalArgumentException e) {
            throw new LQMGException("Error in reflection call", e);
        }
    }

    protected void handleTableOriginal(DatabaseMetaData md, ResultSet tables) throws SQLException {
        String catalog = tables.getString("TABLE_CAT");
        String schema = tables.getString("TABLE_SCHEM");
        String schemaName = tables.getString("TABLE_SCHEM");
        String tableName = tables.getString("TABLE_NAME");
        String normalizedTableName = namingStrategy.normalizeTableName(tableName);
        String className = namingStrategy.getClassName(normalizedTableName);
        EntityType classModel = createEntityType(schemaName, normalizedTableName, className);

        // collect primary keys
        Map<String, PrimaryKeyData> primaryKeyData = keyDataFactory
                .getPrimaryKeys(md, catalog, schema, tableName);
        if (!primaryKeyData.isEmpty()) {
            classModel.getData().put(PrimaryKeyData.class, primaryKeyData.values());
        }

        // collect foreign keys
        Map<String, ForeignKeyData> foreignKeyData = keyDataFactory
                .getImportedKeys(md, catalog, schema, tableName);
        if (!foreignKeyData.isEmpty()) {
            classModel.getData().put(ForeignKeyData.class, foreignKeyData.values());
        }

        // collect inverse foreign keys
        Map<String, InverseForeignKeyData> inverseForeignKeyData = keyDataFactory
                .getExportedKeys(md, catalog, schema, tableName);
        if (!inverseForeignKeyData.isEmpty()) {
            classModel.getData().put(InverseForeignKeyData.class, inverseForeignKeyData.values());
        }

        // collect columns
        ResultSet columns = md.getColumns(catalog, schema, tableName.replace("/", "//"), null);
        try {
            while (columns.next()) {
                handleColumn(classModel, tableName, columns);
            }
        } finally {
            columns.close();
        }

        // serialize model
        serialize(classModel);

        LOGGER.info("Exported " + tableName + " successfully");
    }
    
    private void serialize(EntityType type) {
        try {
            callSuper("serialize", new Class[] {EntityType.class}, new Object[] {type});
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
        }
    }

    private void handleColumn(EntityType classModel, String tableName, ResultSet columns) throws SQLException {
        try {
            callSuper("handleColumn", new Class[] { EntityType.class, String.class, ResultSet.class }, new Object[] {
                    classModel, tableName, columns });
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof SQLException) {
                throw (SQLException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
        }
    }
}
