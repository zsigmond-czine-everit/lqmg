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

import org.everit.osgi.dev.lqmg.schema.AbstractNamingRuleType;
import org.osgi.framework.Bundle;

public class ConfigValue<T extends AbstractNamingRuleType> {

    private final Bundle bundle;

    private final String configurationXMLPath;

    private final T namingRule;

    public ConfigValue(T namingRule, Bundle bundle, String configurationXMLPath) {
        this.namingRule = namingRule;
        this.bundle = bundle;
        this.configurationXMLPath = configurationXMLPath;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public String getConfigurationXMLPath() {
        return configurationXMLPath;
    }

    public T getNamingRule() {
        return namingRule;
    }

}
