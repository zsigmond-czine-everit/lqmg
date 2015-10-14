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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.everit.persistence.lqmg.LQMGException;
import org.everit.persistence.lqmg.internal.schema.xml.AbstractNamingRuleType;

import com.mysema.codegen.model.SimpleType;
import com.mysema.codegen.model.Type;
import com.mysema.query.sql.codegen.DefaultNamingStrategy;
import com.mysema.query.sql.codegen.NamingStrategy;
import com.mysema.query.sql.support.ForeignKeyData;
import com.mysema.query.sql.support.InverseForeignKeyData;
import com.mysema.query.sql.support.PrimaryKeyData;

public class LQMGKeyDataFactory {

  private static final int FK_FOREIGN_COLUMN_NAME = 8;

  private static final int FK_FOREIGN_SCHEMA_NAME = 6;

  private static final int FK_FOREIGN_TABLE_NAME = 7;

  private static final int FK_NAME = 12;

  private static final int FK_PARENT_COLUMN_NAME = 4;

  private static final int FK_PARENT_SCHEMA_NAME = 2;

  private static final int FK_PARENT_TABLE_NAME = 3;

  private static final int PK_COLUMN_NAME = 4;

  private static final int PK_NAME = 6;

  private final ConfigurationContainer configContainer;

  private NamingStrategy namingStrategy = new DefaultNamingStrategy();

  public LQMGKeyDataFactory(final ConfigurationContainer configContainer) {
    this.configContainer = configContainer;
  }

  protected Type createType(@Nullable final String schemaName, final String table) {
    String simpleName = configContainer.resolveClassName(schemaName, table, namingStrategy);
    if (simpleName == null) {
      throw new LQMGException(
          "Cannot resolve class name for '" + table + "' table in '" + schemaName
              + "' schema.",
          null);
    }

    ConfigValue<? extends AbstractNamingRuleType> configValue =
        configContainer.findConfigForEntity(schemaName,
            table);
    AbstractNamingRuleType namingRule = configValue.getNamingRule();
    String packageName = namingRule.getPackage();

    return new SimpleType(packageName + "." + simpleName, packageName, simpleName);
  }

  public Map<String, InverseForeignKeyData> getExportedKeys(final DatabaseMetaData md,
      final String catalog, final String schema, final String tableName) throws SQLException {
    ResultSet foreignKeys = md.getExportedKeys(catalog, schema, tableName);
    Map<String, InverseForeignKeyData> inverseForeignKeyData =
        new HashMap<String, InverseForeignKeyData>();
    try {
      while (foreignKeys.next()) {
        String name = foreignKeys.getString(FK_NAME);
        String parentColumnName = foreignKeys.getString(FK_PARENT_COLUMN_NAME);
        String foreignSchemaName = foreignKeys.getString(FK_FOREIGN_SCHEMA_NAME);
        String foreignTableName = foreignKeys.getString(FK_FOREIGN_TABLE_NAME);
        String foreignColumn = foreignKeys.getString(FK_FOREIGN_COLUMN_NAME);
        if ((name == null) || name.isEmpty()) {
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

  public Map<String, ForeignKeyData> getImportedKeys(final DatabaseMetaData md,
      final String catalog, final String schema, final String tableName) throws SQLException {
    ResultSet foreignKeys = md.getImportedKeys(catalog, schema, tableName);
    Map<String, ForeignKeyData> foreignKeyData = new HashMap<String, ForeignKeyData>();
    try {
      while (foreignKeys.next()) {
        String name = foreignKeys.getString(FK_NAME);
        String parentSchemaName = foreignKeys.getString(FK_PARENT_SCHEMA_NAME);
        String parentTableName = foreignKeys.getString(FK_PARENT_TABLE_NAME);
        String parentColumnName = foreignKeys.getString(FK_PARENT_COLUMN_NAME);
        String foreignColumn = foreignKeys.getString(FK_FOREIGN_COLUMN_NAME);
        if ((name == null) || name.isEmpty()) {
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

  public Map<String, PrimaryKeyData> getPrimaryKeys(final DatabaseMetaData md,
      final String catalog, final String schema, final String tableName) throws SQLException {
    ResultSet primaryKeys = md.getPrimaryKeys(catalog, schema, tableName);
    Map<String, PrimaryKeyData> primaryKeyData = new HashMap<String, PrimaryKeyData>();
    try {
      while (primaryKeys.next()) {
        String name = primaryKeys.getString(PK_NAME);
        String columnName = primaryKeys.getString(PK_COLUMN_NAME);
        if ((name == null) || name.isEmpty()) {
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
}
