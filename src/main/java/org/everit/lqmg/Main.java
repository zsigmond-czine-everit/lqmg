package org.everit.lqmg;

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

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import liquibase.Liquibase;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

import org.h2.Driver;

import com.mysema.query.sql.codegen.MetaDataExporter;

public class Main {

    public static void generate(final String changeLogFile, final String targetFolder, final String packageName,
            final boolean schemaToPackage, final String schemaPattern) {

        Driver h2Driver = Driver.load();
        Connection connection = null;
        try {
            connection = h2Driver.connect("jdbc:h2:mem:", new Properties());
            AbstractJdbcDatabase database =
                    (AbstractJdbcDatabase) DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                            new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(changeLogFile, new FileSystemResourceAccessor(), database);
            liquibase.update(null);

            MetaDataExporter metaDataExporter = new MetaDataExporter();
            metaDataExporter.setNamingStrategy(new CustomNamingStrategy());
            metaDataExporter.setPackageName(packageName);
            metaDataExporter.setSchemaPattern(schemaPattern);

            metaDataExporter.setSchemaToPackage(true);
            metaDataExporter.setTargetFolder(new File(targetFolder));
            metaDataExporter.export(connection.getMetaData());

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DatabaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LiquibaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(final String[] args) {
        if ((args.length == 0) || ((args.length == 1) && "--help".equals(args[0]))) {
            Main.printHelp();
            return;
        }

        String changeLogFile = null;
        String packageName = "";
        String targetFolder = null;
        String schemaPattern = null;
        boolean schemaToPackage = true;

        for (int i = 0, n = args.length; i < n; i++) {
            if (args[i].startsWith("--changeLogFile=")) {
                changeLogFile = args[i].substring("--changeLogFile=".length());
            } else if (args[i].startsWith("--packageName=")) {
                packageName = args[i].substring("--packageName=".length());
            } else if (args[i].startsWith("--targetFolder=")) {
                targetFolder = args[i].substring("--targetFolder=".length());
            } else if (args[i].startsWith("--schemaPattern=")) {
                schemaPattern = args[i].substring("--excludeChangeLogFile=".length());
            } else if (args[i].startsWith("--schemaToPackage=")) {
                schemaToPackage = Boolean.valueOf(args[i].substring("--schemaToPackage=".length()));
            } else {
                System.out.println("Unknown parameter '" + args[i]
                        + "'. Run <with --help to get information about the possible parameters");
            }
        }
        if ((changeLogFile == null) || (targetFolder == null)) {
            System.out
                    .println("Missing reguired parameters. Reguired parameters: changeLogFile, packageName, targetFolder");
            return;
        }
        Main.generate(changeLogFile, targetFolder, packageName, schemaToPackage, schemaPattern);
    }

    public static void printHelp() {
        System.out.println("Example usage: lqmg.sh --changeLogFile=/tmp/changelog.xml --packageName=foo"
                + " --targetFolder=/tmp/generated\n");
        System.out.println("Arguments: \n");
        System.out.println("  --changeLogFile: Path to the liquibase changelog file");
        System.out.println("  --packageName: The java package of the generated QueryDSL metamodel classes"
                + " (default: empty, that means that the package will be either empty or derived from the schema)");
        System.out.println("  --targetFolder: The folder where source will be generated to");
        System.out.println("  --schemaPattern: a schema name pattern; must match the schema name as it is stored in"
                + " the database; \"\" retrieves those without a schema; null means that the schema name should not"
                + " be used to narrow the search (default: null)");
        System.out.println("  --help: This help\n\n");
    }
}
