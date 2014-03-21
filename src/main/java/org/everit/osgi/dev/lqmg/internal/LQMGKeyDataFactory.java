package org.everit.osgi.dev.lqmg.internal;

import javax.annotation.Nullable;

import org.everit.osgi.dev.lqmg.schema.AbstractNamingRuleType;
import org.everit.osgi.dev.lqmg.schema.RegexRuleType;

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

    protected Type createType(@Nullable String schemaName, String table) {
        ConfigValue<? extends AbstractNamingRuleType> configValue = configContainer.findConfigForEntity(schemaName,
                table);
        AbstractNamingRuleType namingRule = configValue.getNamingRule();
        String packageName = namingRule.getPackage();
        String prefix = namingRule.getPrefix();
        if (prefix == null) {
            prefix = "";
        }
        String suffix = namingRule.getSuffix();
        if (suffix == null) {
            suffix = "";
        }
        if (namingRule instanceof RegexRuleType) {

        }

        String simpleName = prefix + namingStrategy.getClassName(table) + suffix;
        return new SimpleType(packageName + "." + simpleName, packageName, simpleName);
    }
}
