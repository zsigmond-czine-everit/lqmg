package org.everit.db.lqmg;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

/**
 * The LiquiBase XML to QueryDSL metamodel generator exception.
 */
public class LiquiBaseQueryDSLModellGeneratorException extends RuntimeException {

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
    public LiquiBaseQueryDSLModellGeneratorException(final String msg, final Throwable exception) {
        super(msg, exception);
    }

}
