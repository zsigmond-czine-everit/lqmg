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
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

import org.h2.Driver;

import com.mysema.query.sql.codegen.MetaDataExporter;

public class Main {

    public static void generate(final String changeLogFile, final String packageName, final String targetFolder,
            final String excludeChangeLogFile) {
        if ((changeLogFile == null) || (packageName == null) || (targetFolder == null)) {
            System.out
                    .println("Missing reguired parameters. Reguired parameters: changeLogFile, packageName, targetFolder");
            return;
        }

        Driver h2Driver = Driver.load();
        Connection connection = null;
        try {
            connection = h2Driver.connect("jdbc:h2:mem:", new Properties());
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                    new JdbcConnection(connection));
            Liquibase liquibase =
                    new Liquibase(changeLogFile, new FileSystemResourceAccessor(), database);
            liquibase.update(null);

            MetaDataExporter metaDataExporter = new MetaDataExporter();
            metaDataExporter.setPackageName(packageName);
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
        String packageName = null;
        String targetFolder = null;
        String excludeChangeLogFile = null;

        for (int i = 0, n = args.length; i < n; i++) {
            if (args[i].startsWith("--changeLogFile=")) {
                changeLogFile = args[i].substring("--changeLogFile=".length());
            } else if (args[i].startsWith("--packageName=")) {
                packageName = args[i].substring("--packageName=".length());
            } else if (args[i].startsWith("--targetFolder=")) {
                targetFolder = args[i].substring("--targetFolder=".length());
            } else if (args[i].startsWith("--excludeChangeLogFile=")) {
                excludeChangeLogFile = args[i].substring("--excludeChangeLogFile=".length());
            } else {
                System.out
                        .println("Unknown parameter '" + args[i]
                                + "'. Run <with --help to get information about the possible parameters");
            }
        }
        Main.generate(changeLogFile, packageName, targetFolder, excludeChangeLogFile);
    }

    public static void printHelp() {
        System.out.println("Example usage: lqmg.sh --changelogFile=/tmp/changelog.xml --packageName=foo"
                + " --targetFolder=/tmp/generated\n");
        System.out.println("Arguments: \n");
        System.out.println("  --changeLogFile: Path to the liquibase changelog file");
        System.out.println("  --packageName: The java package of the generated QueryDSL metamodel classes");
        System.out.println("  --targetFolder: The folder where source will be generated to");
        System.out.println("  --excludeChangeLogFile: Path to the liquibase changelog file which want to ignore.");
        System.out.println("  --help: This help\n\n");
    }
}
