package org.everit.osgi.dev.lqmg.internal;

import org.osgi.framework.Bundle;

public class ConfigPath {

    private final Bundle bundle;

    private final String resource;

    public ConfigPath(Bundle bundle, String resource) {
        this.bundle = bundle;
        this.resource = resource;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConfigPath other = (ConfigPath) obj;
        if (bundle == null) {
            if (other.bundle != null) {
                return false;
            }
        } else if (!bundle.equals(other.bundle)) {
            return false;
        }
        if (resource == null) {
            if (other.resource != null) {
                return false;
            }
        } else if (!resource.equals(other.resource)) {
            return false;
        }
        return true;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public String getResource() {
        return resource;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bundle == null) ? 0 : bundle.hashCode());
        result = prime * result + ((resource == null) ? 0 : resource.hashCode());
        return result;
    }

}
