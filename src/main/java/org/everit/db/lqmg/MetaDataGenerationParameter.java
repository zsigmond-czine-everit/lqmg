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
public class MetaDataGenerationParameter {

    /**
     * Path to the liquibase changelog file.
     */
    private String changeLogFile;

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
    private String schemaPattern;

    /**
     * The changeLogFile path type. {@link ChangeLogPathType#FILESYSTEM} or {@link ChangeLogPathType#RESOURCE}.
     */
    private ChangeLogPathType changlogPathType = ChangeLogPathType.FILESYSTEM;

    /**
     * The simple constructor.
     * 
     * @param changeLogFile
     *            path to the liquibase changelog file.
     * @param targetFolder
     *            the folder where source will be generated to.
     */
    public MetaDataGenerationParameter(final String changeLogFile, final String targetFolder) {
        this.changeLogFile = changeLogFile;
        this.targetFolder = targetFolder;
    }

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public final ChangeLogPathType getChanglogPathType() {
        return changlogPathType;
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

    public void setChangeLogFile(final String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    public final void setChanglogPathType(final ChangeLogPathType changlogPathType) {
        this.changlogPathType = changlogPathType;
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

}
