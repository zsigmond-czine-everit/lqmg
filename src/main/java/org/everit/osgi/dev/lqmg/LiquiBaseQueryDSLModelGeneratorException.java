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
package org.everit.osgi.dev.lqmg;

/**
 * The LiquiBase XML to QueryDSL metamodel generator exception.
 */
public class LiquiBaseQueryDSLModelGeneratorException extends RuntimeException {

    /**
     * Generated seriar version UID.
     */
    private static final long serialVersionUID = 4553681195568297655L;

    /**
     * The simple constructor.
     * 
     * @param msg
     *            the error message.
     * @param exception
     *            the exception
     */
    public LiquiBaseQueryDSLModelGeneratorException(final String msg, final Throwable exception) {
        super(msg, exception);
    }

}
