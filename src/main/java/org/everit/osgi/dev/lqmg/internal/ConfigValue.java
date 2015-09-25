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

import org.everit.osgi.dev.lqmg.internal.schema.xml.AbstractNamingRuleType;
import org.osgi.framework.Bundle;

/**
 * DTO for configuration value.
 *
 * @param <T>
 *          the type of the used naming rule.
 */
public class ConfigValue<T extends AbstractNamingRuleType> {

  public final Bundle bundle;

  public final String configurationXMLPath;

  public final T namingRule;

  /**
   * Constructor.
   */
  public ConfigValue(final T namingRule, final Bundle bundle, final String configurationXMLPath) {
    this.namingRule = namingRule;
    this.bundle = bundle;
    this.configurationXMLPath = configurationXMLPath;
  }

  // TODO remove
  public T getNamingRule() {
    return namingRule;
  }

}
