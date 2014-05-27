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
package org.everit.osgi.dev.lqmg.internal.schema.qdsl;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;




/**
 * QColumnMapping is a Querydsl query type for QColumnMapping
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QColumnMapping extends com.mysema.query.sql.RelationalPathBase<QColumnMapping> {

    private static final long serialVersionUID = -1368091516;

    public static final QColumnMapping columnMapping = new QColumnMapping("querydsl_column_mapping");

    public final StringPath columnName = createString("columnName");

    public final StringPath propertyName = createString("propertyName");

    public final StringPath schemaName = createString("schemaName");

    public final StringPath tableName = createString("tableName");

    public final com.mysema.query.sql.PrimaryKey<QColumnMapping> constraint9 = createPrimaryKey(columnName, schemaName, tableName);

    public QColumnMapping(String variable) {
        super(QColumnMapping.class, forVariable(variable), null, "querydsl_column_mapping");
        addMetadata();
    }

    public QColumnMapping(String variable, String schema, String table) {
        super(QColumnMapping.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QColumnMapping(Path<? extends QColumnMapping> path) {
        super(path.getType(), path.getMetadata(), null, "querydsl_column_mapping");
        addMetadata();
    }

    public QColumnMapping(PathMetadata<?> metadata) {
        super(QColumnMapping.class, metadata, null, "querydsl_column_mapping");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(columnName, ColumnMetadata.named("column_name").ofType(12).withSize(255).notNull());
        addMetadata(propertyName, ColumnMetadata.named("property_name").ofType(12).withSize(255).notNull());
        addMetadata(schemaName, ColumnMetadata.named("schema_name").ofType(12).withSize(255).notNull());
        addMetadata(tableName, ColumnMetadata.named("table_name").ofType(12).withSize(255).notNull());
    }

}

