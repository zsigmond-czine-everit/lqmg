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

import org.osgi.framework.Bundle;

/**
 * DTO for configuration path.
 */
public class ConfigPath {

  public final Bundle bundle;

  public final String resource;

  public ConfigPath(final Bundle bundle, final String resource) {
    this.bundle = bundle;
    this.resource = resource;
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

  @Generated("Generated with Eclipse")
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((bundle == null) ? 0 : bundle.hashCode());
    result = (prime * result) + ((resource == null) ? 0 : resource.hashCode());
    return result;
  }

}
