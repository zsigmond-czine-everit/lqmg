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

import org.everit.osgi.dev.lqmg.schema.LQMGAbstractEntityType;
import org.osgi.framework.Bundle;

public class ConfigValue<T extends LQMGAbstractEntityType> {

    private final Bundle bundle;

    private final String configurationXMLPath;

    private final T entityConfiguration;

    public ConfigValue(T entityConfiguration, Bundle bundle, String configurationXMLPath) {
        this.entityConfiguration = entityConfiguration;
        this.bundle = bundle;
        this.configurationXMLPath = configurationXMLPath;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public String getConfigurationXMLPath() {
        return configurationXMLPath;
    }

    public T getEntityConfiguration() {
        return entityConfiguration;
    }

}
