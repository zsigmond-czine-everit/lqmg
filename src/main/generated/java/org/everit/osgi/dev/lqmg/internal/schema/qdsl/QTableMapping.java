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
 * QTableMapping is a Querydsl query type for QTableMapping
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QTableMapping extends com.mysema.query.sql.RelationalPathBase<QTableMapping> {

    private static final long serialVersionUID = 1510670260;

    public static final QTableMapping tableMapping = new QTableMapping("querydsl_table_mapping");

    public final StringPath className = createString("className");

    public final StringPath packageName = createString("packageName");

    public final StringPath schemaName = createString("schemaName");

    public final StringPath tableName = createString("tableName");

    public final com.mysema.query.sql.PrimaryKey<QTableMapping> constraintD = createPrimaryKey(schemaName, tableName);

    public QTableMapping(String variable) {
        super(QTableMapping.class, forVariable(variable), null, "querydsl_table_mapping");
        addMetadata();
    }

    public QTableMapping(String variable, String schema, String table) {
        super(QTableMapping.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTableMapping(Path<? extends QTableMapping> path) {
        super(path.getType(), path.getMetadata(), null, "querydsl_table_mapping");
        addMetadata();
    }

    public QTableMapping(PathMetadata<?> metadata) {
        super(QTableMapping.class, metadata, null, "querydsl_table_mapping");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(className, ColumnMetadata.named("class_name").ofType(12).withSize(255));
        addMetadata(packageName, ColumnMetadata.named("package_name").ofType(12).withSize(255));
        addMetadata(schemaName, ColumnMetadata.named("schema_name").ofType(12).withSize(255).notNull());
        addMetadata(tableName, ColumnMetadata.named("table_name").ofType(12).withSize(255).notNull());
    }

}

