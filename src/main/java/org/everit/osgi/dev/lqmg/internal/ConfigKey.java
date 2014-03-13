package org.everit.osgi.dev.lqmg.internal;

public class ConfigKey {

    private final String entity;

    private final String schemaName;

    public ConfigKey(String schemaName, String entity) {
        this.schemaName = schemaName;
        this.entity = entity;
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
        ConfigKey other = (ConfigKey) obj;
        if (entity == null) {
            if (other.entity != null) {
                return false;
            }
        } else if (!entity.equals(other.entity)) {
            return false;
        }
        if (schemaName == null) {
            if (other.schemaName != null) {
                return false;
            }
        } else if (!schemaName.equals(other.schemaName)) {
            return false;
        }
        return true;
    }

    public String getEntity() {
        return entity;
    }

    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entity == null) ? 0 : entity.hashCode());
        result = prime * result + ((schemaName == null) ? 0 : schemaName.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ConfigKey [schemaName=" + schemaName + ", entity=" + entity + "]";
    }

}
