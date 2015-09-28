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
package org.everit.osgi.dev.lqmg.internal;

import java.util.Map;

import javax.annotation.Generated;

import org.osgi.framework.Bundle;

/**
 * DTO for holding {@link Bundle} and its attributes together.
 */
public class BundleWithAttributes {

  public final Map<String, Object> attributes;

  public final Bundle bundle;

  public BundleWithAttributes(final Bundle bundle, final Map<String, Object> attributes) {
    this.bundle = bundle;
    this.attributes = attributes;
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

  @Generated("Generated with Eclipse")
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((attributes == null) ? 0 : attributes.hashCode());
    result = (prime * result) + ((bundle == null) ? 0 : (int) bundle.getBundleId());
    return result;
  }

}
