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
 * This class store all parameters which is required to the JAVA classes generation.
 */
public class GenerationProperties {

    /**
     * A schema name with optional filter expression that points to a bundle schema capability.
     */
    private String topSchema;

    /**
     * The folder where source will be generated to.
     */
    private String targetFolder;

    /**
     * The java package of the generated QueryDSL metamodel classes.
     */
    private String packageName = "";

    /**
     * The schema to package.
     */
    private boolean schemaToPackage = true;

    /**
     * A schema name pattern; must match the schema name as it is stored in the database.
     */
    private String schemaPattern = null;

    /**
     * The paths to the bundles (directory path of jar file path).
     */
    private String[] bundlePaths;

    /**
     * The simple constructor.
     * 
     * @param topSchema
     *            A schema name with optional capability filter where liquibase should start searching for changesets.
     * @param targetFolder
     *            the folder where source will be generated to.
     */
    public GenerationProperties(final String topSchema, final String[] bundlePaths, final String targetFolder) {
        this.topSchema = topSchema;
        this.targetFolder = targetFolder;
        this.bundlePaths = bundlePaths;
    }

    public String getTopSchema() {
        return topSchema;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSchemaPattern() {
        return schemaPattern;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public boolean isSchemaToPackage() {
        return schemaToPackage;
    }

    public void setTopSchema(final String topSchema) {
        this.topSchema = topSchema;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public void setSchemaPattern(final String schemaPattern) {
        this.schemaPattern = schemaPattern;
    }

    public void setSchemaToPackage(final boolean schemaToPackage) {
        this.schemaToPackage = schemaToPackage;
    }

    public void setTargetFolder(final String targetFolder) {
        this.targetFolder = targetFolder;
    }

    public String[] getBundlePaths() {
        return bundlePaths;
    }
}
