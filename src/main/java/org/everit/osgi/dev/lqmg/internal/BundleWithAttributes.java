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

import java.util.Map;

import org.osgi.framework.Bundle;

public class BundleWithAttributes {

    private final Bundle bundle;

    private final Map<String, Object> attributes;

    public BundleWithAttributes(final Bundle bundle, final Map<String, Object> attributes) {
        this.bundle = bundle;
        this.attributes = attributes;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BundleWithAttributes other = (BundleWithAttributes) obj;
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (bundle == null) {
            if (other.bundle != null) {
                return false;
            }
        } else if (other.bundle == null) {
            return false;
        } else if (bundle.getBundleId() != other.bundle.getBundleId()) {
            return false;
        }
        return true;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Bundle getBundle() {
        return bundle;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((attributes == null) ? 0 : attributes.hashCode());
        result = (prime * result) + ((bundle == null) ? 0 : (int) bundle.getBundleId());
        return result;
    }

}
