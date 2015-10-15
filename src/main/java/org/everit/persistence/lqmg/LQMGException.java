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
package org.everit.persistence.lqmg;

/**
 * The LiquiBase XML to QueryDSL metamodel generator exception.
 */
public class LQMGException extends RuntimeException {

  /**
   * Generated serial version UID.
   */
  private static final long serialVersionUID = 4553681195568297655L;

  /**
   * The simple constructor.
   *
   * @param msg
   *          the error message.
   * @param exception
   *          the exception
   */
  public LQMGException(final String msg, final Throwable exception) {
    super(msg, exception);
  }

}
