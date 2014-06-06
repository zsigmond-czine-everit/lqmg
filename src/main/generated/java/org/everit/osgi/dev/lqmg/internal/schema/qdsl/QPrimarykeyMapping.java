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
 * QPrimarykeyMapping is a Querydsl query type for QPrimarykeyMapping
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QPrimarykeyMapping extends com.mysema.query.sql.RelationalPathBase<QPrimarykeyMapping> {

    private static final long serialVersionUID = -1639769379;

    public static final QPrimarykeyMapping primarykeyMapping = new QPrimarykeyMapping("querydsl_primarykey_mapping");

    public final StringPath primarykeyName = createString("primarykeyName");

    public final StringPath propertyName = createString("propertyName");

    public final StringPath schemaName = createString("schemaName");

    public final StringPath tableName = createString("tableName");

    public final com.mysema.query.sql.PrimaryKey<QPrimarykeyMapping> constraint66 = createPrimaryKey(primarykeyName, schemaName, tableName);

    public QPrimarykeyMapping(String variable) {
        super(QPrimarykeyMapping.class, forVariable(variable), null, "querydsl_primarykey_mapping");
        addMetadata();
    }

    public QPrimarykeyMapping(String variable, String schema, String table) {
        super(QPrimarykeyMapping.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPrimarykeyMapping(Path<? extends QPrimarykeyMapping> path) {
        super(path.getType(), path.getMetadata(), null, "querydsl_primarykey_mapping");
        addMetadata();
    }

    public QPrimarykeyMapping(PathMetadata<?> metadata) {
        super(QPrimarykeyMapping.class, metadata, null, "querydsl_primarykey_mapping");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(primarykeyName, ColumnMetadata.named("primarykey_name").ofType(12).withSize(255).notNull());
        addMetadata(propertyName, ColumnMetadata.named("property_name").ofType(12).withSize(255).notNull());
        addMetadata(schemaName, ColumnMetadata.named("schema_name").ofType(12).withSize(255).notNull());
        addMetadata(tableName, ColumnMetadata.named("table_name").ofType(12).withSize(255).notNull());
    }

}

