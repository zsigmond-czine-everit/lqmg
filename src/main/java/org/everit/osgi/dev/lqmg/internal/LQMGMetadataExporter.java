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
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.everit.osgi.dev.lqmg.internal.schema.xml.AbstractNamingRuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.mysema.codegen.CodeWriter;
import com.mysema.codegen.JavaWriter;
import com.mysema.codegen.model.ClassType;
import com.mysema.codegen.model.SimpleType;
import com.mysema.codegen.model.Type;
import com.mysema.codegen.model.TypeCategory;
import com.mysema.query.codegen.EntityType;
import com.mysema.query.codegen.JavaTypeMappings;
import com.mysema.query.codegen.Property;
import com.mysema.query.codegen.Serializer;
import com.mysema.query.codegen.SimpleSerializerConfig;
import com.mysema.query.codegen.TypeMappings;
import com.mysema.query.sql.ColumnImpl;
import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.codegen.DefaultNamingStrategy;
import com.mysema.query.sql.codegen.MetaDataSerializer;
import com.mysema.query.sql.codegen.NamingStrategy;
import com.mysema.query.sql.support.ForeignKeyData;
import com.mysema.query.sql.support.InverseForeignKeyData;
import com.mysema.query.sql.support.NotNullImpl;
import com.mysema.query.sql.support.PrimaryKeyData;
import com.mysema.query.sql.support.SizeImpl;

public class LQMGMetadataExporter {

    private static Logger LOGGER = LoggerFactory.getLogger(LQMGMetadataExporter.class);

    private final ConfigurationContainer configurationContainer;

    private final Set<String> packages;

    private TypeMappings typeMappings;

    private Serializer serializer;

    private boolean columnAnnotations = false;

    private Configuration configuration;

    private boolean validationAnnotations = false;

    private final Set<String> classes = new HashSet<String>();

    private File targetFolder;

    private String sourceEncoding = "UTF-8";

    protected NamingStrategy namingStrategy;

    private final LQMGKeyDataFactory keyDataFactory;

    public LQMGMetadataExporter(ConfigurationContainer configurationContainer, String[] packages) {
        this.configurationContainer = configurationContainer;
        this.namingStrategy = new LQMGNamingStrategy(configurationContainer);
        this.packages = new HashSet<String>(Arrays.asList(packages));
        this.keyDataFactory = new LQMGKeyDataFactory(configurationContainer);
    }

    protected EntityType createEntityType(String schemaName, String tableName, String className) {

        ConfigValue<? extends AbstractNamingRuleType> configValue = configurationContainer.findConfigForEntity(
                schemaName, tableName);
        AbstractNamingRuleType namingRule = configValue.getNamingRule();

        EntityType classModel;
        String packageName = namingRule.getPackage();
        Type classTypeModel = new SimpleType(TypeCategory.ENTITY,
                packageName + "." + className, packageName, className, false, false);
        classModel = new EntityType(classTypeModel);
        typeMappings.register(classModel, classModel);

        if (namingRule != null && namingRule.isUseSchema()) {
            classModel.getData().put("schema", schemaName);
        }
        classModel.getData().put("table", tableName);
        return classModel;
    }

    /**
     * Export the tables based on the given database metadata
     *
     * @param md
     * @throws SQLException
     */
    public void export(DatabaseMetaData md) throws SQLException {
        typeMappings = new JavaTypeMappings();
        serializer = new MetaDataSerializer(typeMappings, namingStrategy, false, Collections.<String> emptySet());
        configuration = Configuration.DEFAULT;

        List<String> types = new ArrayList<String>(2);
        types.add("TABLE");
        types.add("VIEW");

        ResultSet tables = md.getTables(null, null, null,
                types.toArray(new String[types.size()]));
        try {
            while (tables.next()) {
                handleTable(md, tables);
            }
        } finally {
            tables.close();
        }

    }

    protected void handleTable(DatabaseMetaData md, ResultSet tables) throws SQLException {
        String schema = tables.getString("TABLE_SCHEM");
        String entity = tables.getString("TABLE_NAME");
        ConfigValue<? extends AbstractNamingRuleType> configValue = configurationContainer
                .findConfigForEntity(schema, entity);
        if (configValue == null) {
            LOGGER.info("No configuration for table '" + entity + "' in schema '" + schema
                    + "'. Ignoring from metadata class generation");
            return;
        }
        AbstractNamingRuleType namingRule = configValue.getNamingRule();

        String javaPackage = namingRule.getPackage();
        if (packages.size() > 0 && !packages.contains(javaPackage)) {
            LOGGER.info("Java package '" + javaPackage + "' is not included, ignoring entity '" + entity
                    + "' from schema '" + schema + "'.");
            return;
        }

        String catalog = tables.getString("TABLE_CAT");
        String tableName = tables.getString("TABLE_NAME");
        String normalizedTableName = namingStrategy.normalizeTableName(tableName);
        String className = configurationContainer.resolveClassName(schema, tableName, namingStrategy);
        EntityType classModel = createEntityType(schema, normalizedTableName, className);

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
            String fileSuffix = ".java";

            String packageName = type.getPackageName();
            String path = packageName.replace('.', '/') + "/" + type.getSimpleName() + fileSuffix;
            write(serializer, path, type);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void write(Serializer serializer, String path, EntityType type) throws IOException {
        File targetFile = new File(targetFolder, path);
        classes.add(targetFile.getPath());
        StringWriter w = new StringWriter();
        CodeWriter writer = new JavaWriter(w);
        serializer.serialize(type, SimpleSerializerConfig.DEFAULT, writer);

        // conditional creation
        boolean generate = true;
        byte[] bytes = w.toString().getBytes(sourceEncoding);
        if (targetFile.exists() && targetFile.length() == bytes.length) {
            String str = Files.toString(targetFile, Charset.forName(sourceEncoding));
            if (str.equals(w.toString())) {
                generate = false;
            }
        } else {
            targetFile.getParentFile().mkdirs();
        }

        if (generate) {
            Files.write(bytes, targetFile);
        }
    }

    protected Property createProperty(EntityType classModel, String normalizedColumnName,
            String propertyName, Type typeModel) {
        return new Property(
                classModel,
                propertyName,
                propertyName,
                typeModel,
                Collections.<String> emptyList(),
                false);
    }

    private void handleColumn(EntityType classModel, String tableName, ResultSet columns) throws SQLException {
        String columnName = columns.getString("COLUMN_NAME");
        String normalizedColumnName = namingStrategy.normalizeColumnName(columnName);
        int columnType = columns.getInt("DATA_TYPE");
        Number columnSize = (Number) columns.getObject("COLUMN_SIZE");
        Number columnDigits = (Number) columns.getObject("DECIMAL_DIGITS");
        int nullable = columns.getInt("NULLABLE");

        String propertyName = namingStrategy.getPropertyName(normalizedColumnName, classModel);
        Class<?> clazz = configuration.getJavaType(columnType,
                columnSize != null ? columnSize.intValue() : 0,
                columnDigits != null ? columnDigits.intValue() : 0,
                tableName, columnName);
        if (clazz == null) {
            throw new IllegalStateException("Found no mapping for " + columnType + " (" + tableName + "." + columnName
                    + ")");
        }
        TypeCategory fieldType = TypeCategory.get(clazz.getName());
        if (Number.class.isAssignableFrom(clazz)) {
            fieldType = TypeCategory.NUMERIC;
        } else if (Enum.class.isAssignableFrom(clazz)) {
            fieldType = TypeCategory.ENUM;
        }
        Type typeModel = new ClassType(fieldType, clazz);
        Property property = createProperty(classModel, normalizedColumnName, propertyName, typeModel);
        ColumnMetadata column = ColumnMetadata.named(normalizedColumnName).ofType(columnType);
        if (nullable == DatabaseMetaData.columnNoNulls) {
            column = column.notNull();
        }
        if (columnSize != null) {
            column = column.withSize(columnSize.intValue());
        }
        if (columnDigits != null) {
            column = column.withDigits(columnDigits.intValue());
        }
        property.getData().put("COLUMN", column);

        if (columnAnnotations) {
            property.addAnnotation(new ColumnImpl(normalizedColumnName));
        }
        if (validationAnnotations) {
            if (nullable == DatabaseMetaData.columnNoNulls) {
                property.addAnnotation(new NotNullImpl());
            }
            int size = columns.getInt("COLUMN_SIZE");
            if (size > 0 && clazz.equals(String.class)) {
                property.addAnnotation(new SizeImpl(0, size));
            }
        }
        classModel.addProperty(property);
    }

    /**
     * Set the target folder
     *
     * @param targetFolder
     *            target source folder to create the sources into (e.g. target/generated-sources/java)
     */
    public void setTargetFolder(File targetFolder) {
        this.targetFolder = targetFolder;
    }

    /**
     * @param columnAnnotations
     */
    public void setColumnAnnotations(boolean columnAnnotations) {
        this.columnAnnotations = columnAnnotations;
    }

    /**
     * @param validationAnnotations
     */
    public void setValidationAnnotations(boolean validationAnnotations) {
        this.validationAnnotations = validationAnnotations;
    }
}
