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
 * QForeignkeyMapping is a Querydsl query type for QForeignkeyMapping
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QForeignkeyMapping extends com.mysema.query.sql.RelationalPathBase<QForeignkeyMapping> {

    private static final long serialVersionUID = -1504334033;

    public static final QForeignkeyMapping foreignkeyMapping = new QForeignkeyMapping("querydsl_foreignkey_mapping");

    public final StringPath foreignkeyName = createString("foreignkeyName");

    public final StringPath propertyName = createString("propertyName");

    public final StringPath schemaName = createString("schemaName");

    public final StringPath tableName = createString("tableName");

    public final com.mysema.query.sql.PrimaryKey<QForeignkeyMapping> constraint6 = createPrimaryKey(foreignkeyName, schemaName, tableName);

    public QForeignkeyMapping(String variable) {
        super(QForeignkeyMapping.class, forVariable(variable), null, "querydsl_foreignkey_mapping");
        addMetadata();
    }

    public QForeignkeyMapping(String variable, String schema, String table) {
        super(QForeignkeyMapping.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QForeignkeyMapping(Path<? extends QForeignkeyMapping> path) {
        super(path.getType(), path.getMetadata(), null, "querydsl_foreignkey_mapping");
        addMetadata();
    }

    public QForeignkeyMapping(PathMetadata<?> metadata) {
        super(QForeignkeyMapping.class, metadata, null, "querydsl_foreignkey_mapping");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(foreignkeyName, ColumnMetadata.named("foreignkey_name").ofType(12).withSize(255).notNull());
        addMetadata(propertyName, ColumnMetadata.named("property_name").ofType(12).withSize(255).notNull());
        addMetadata(schemaName, ColumnMetadata.named("schema_name").ofType(12).withSize(255).notNull());
        addMetadata(tableName, ColumnMetadata.named("table_name").ofType(12).withSize(255).notNull());
    }

}

