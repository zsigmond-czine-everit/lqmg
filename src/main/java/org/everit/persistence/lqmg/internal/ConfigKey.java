/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.persistence.lqmg.internal;

import javax.annotation.Generated;

/**
 * DTO for configuration keys.
 */
public class ConfigKey {

  public final String entity;

  public final String schemaName;

  public ConfigKey(final String schemaName, final String entity) {
    this.schemaName = schemaName;
    this.entity = entity;
  }

  @Generated("Generated with Eclipse")
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

  @Generated("Generated with Eclipse")
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((entity == null) ? 0 : entity.hashCode());
    result = (prime * result) + ((schemaName == null) ? 0 : schemaName.hashCode());
    return result;
  }

  @Generated("Generated with Eclipse")
  @Override
  public String toString() {
    return "ConfigKey [schemaName=" + schemaName + ", entity=" + entity + "]";
  }

}
