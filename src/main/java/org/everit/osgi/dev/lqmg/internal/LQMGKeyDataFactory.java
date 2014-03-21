package org.everit.osgi.dev.lqmg.internal;

import javax.annotation.Nullable;

import com.mysema.codegen.model.SimpleType;
import com.mysema.codegen.model.Type;
import com.mysema.query.sql.codegen.DefaultNamingStrategy;
import com.mysema.query.sql.codegen.KeyDataFactory;
import com.mysema.query.sql.codegen.NamingStrategy;

public class LQMGKeyDataFactory extends KeyDataFactory {

    private NamingStrategy namingStrategy = new DefaultNamingStrategy();

    private final ConfigurationContainer configContainer;

    public LQMGKeyDataFactory(ConfigurationContainer configContainer) {
        super(null, null, null, null, false);
        this.configContainer = configContainer;
    }

    private Type createType(@Nullable String schemaName, String table) {
        String packageName = this.packageName;
        if (schemaToPackage && schemaName != null) {
            packageName = namingStrategy.appendSchema(packageName, schemaName);
        }
        String simpleName = prefix + namingStrategy.getClassName(table) + suffix;
        return new SimpleType(packageName + "." + simpleName, packageName, simpleName);
    }
}
