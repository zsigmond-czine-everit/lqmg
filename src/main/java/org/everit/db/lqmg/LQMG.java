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

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import liquibase.Liquibase;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.h2.Driver;

import com.mysema.query.sql.codegen.MetaDataExporter;

/**
 * This class responsible for generate QueryDSL JAVA classes.
 */
public class LQMG {

    /**
     * The {@link Logger} instance for logging.
     */
    private static final Logger LOGGER = Logger.getLogger(LQMG.class.getName());

    /**
     * Generate the JAVA classes to QueryDSL from LiquiBase XML.
     * 
     * @param parameters
     *            the parameters for the generation. See more {@link GenerationProperties}.
     */
    public static void generate(final GenerationProperties parameters, final ResourceAccessor resourceAccessor) {
        LOGGER.log(Level.INFO, "Load driver.");
        Driver h2Driver = Driver.load();
        LOGGER.log(Level.INFO, "Loaded driver.");

        Connection connection = null;
        try {
            LOGGER.log(Level.INFO, "Creating connection.");
            connection = h2Driver.connect("jdbc:h2:mem:", new Properties());
            LOGGER.log(Level.INFO, "Created connection.");

            LOGGER.log(Level.INFO, "Get database.");
            AbstractJdbcDatabase database =
                    (AbstractJdbcDatabase) DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                            new JdbcConnection(connection));
            LOGGER.log(Level.INFO, "Start LiguiBase and update.");
            Liquibase liquibase = new Liquibase(parameters.getChangeLogFile(), resourceAccessor, database);
            liquibase.update(null);
            LOGGER.log(Level.INFO, "Finish LiguiBase and update.");

            LOGGER.log(Level.INFO, "Start meta data export.");
            MetaDataExporter metaDataExporter = new MetaDataExporter();
            metaDataExporter.setNamingStrategy(new CustomNamingStrategy());
            metaDataExporter.setPackageName(parameters.getPackageName());
            metaDataExporter.setSchemaPattern(parameters.getSchemaPattern());

            metaDataExporter.setSchemaToPackage(parameters.isSchemaToPackage());
            metaDataExporter.setTargetFolder(new File(parameters.getTargetFolder()));
            metaDataExporter.export(connection.getMetaData());
            LOGGER.log(Level.INFO, "Finish meta data export.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new LiquiBaseQueryDSLModellGeneratorException("Exception message: " + e.getMessage(), e);
        } catch (DatabaseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new LiquiBaseQueryDSLModellGeneratorException("Exception message: " + e.getMessage(), e);
        } catch (LiquibaseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new LiquiBaseQueryDSLModellGeneratorException("Exception message: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    LOGGER.log(Level.INFO, "Connection closed.");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new LiquiBaseQueryDSLModellGeneratorException("Exception message: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * The command line processor.
     * 
     * @param args
     *            the arguments.
     */
    public static void main(final String[] args) {
        if ((args.length == 0) || ((args.length == 1) && "--help".equals(args[0]))) {
            LOGGER.log(Level.INFO, args.length != 1 ? "No arguments." : "Called the command help.");
            LQMG.printHelp();
            return;
        }

        String changeLogFile = null;
        String packageName = "";
        String targetFolder = null;
        String schemaPattern = null;
        Boolean schemaToPackage = true;

        LOGGER.log(Level.INFO, "Processing arguments.");
        for (int i = 0, n = args.length; i < n; i++) {
            if (args[i].startsWith("--changeLogFile=")) {
                changeLogFile = args[i].substring("--changeLogFile=".length());
                LOGGER.log(Level.INFO, "The changeLogFile argument: " + changeLogFile);
            } else if (args[i].startsWith("--packageName=")) {
                packageName = args[i].substring("--packageName=".length());
                LOGGER.log(Level.INFO, "The packageName argument: " + packageName);
            } else if (args[i].startsWith("--targetFolder=")) {
                targetFolder = args[i].substring("--targetFolder=".length());
                LOGGER.log(Level.INFO, "The targetFolder argument: " + targetFolder);
            } else if (args[i].startsWith("--schemaPattern=")) {
                schemaPattern = args[i].substring("--schemaPattern=".length());
                LOGGER.log(Level.INFO, "The schemaPattern argument: " + schemaPattern);
            } else if (args[i].startsWith("--schemaToPackage=")) {
                schemaToPackage = Boolean.valueOf(args[i].substring("--schemaToPackage=".length()));
                LOGGER.log(Level.INFO, "The schemaToPackage argument: " + schemaToPackage);
            } else {
                LOGGER.log(Level.INFO, "Unknow parameter: " + args[i]
                        + "'. Run <with --help to get information about the possible parameters");
            }
        }
        LOGGER.log(Level.INFO, "Processed arguments.");

        if ((changeLogFile == null) || (targetFolder == null)) {
            LOGGER.log(Level.SEVERE, "Missing required argument(s): " + (changeLogFile == null ? "'changeLogFile'"
                    : "") + ", " + (targetFolder == null ? "'targetFolder'" : ""));
            return;
        }

        LOGGER.log(Level.INFO, "Set the changeLogFile and targetFolder paramters.");
        GenerationProperties params = new GenerationProperties(changeLogFile, targetFolder);
        if (schemaToPackage != null) {
            LOGGER.log(Level.INFO, "Set the schemaToPackage paramters.");
            params.setSchemaToPackage(schemaToPackage);
        }

        if (schemaPattern != null) {
            LOGGER.log(Level.INFO, "Set the schemaPattern paramters.");
            params.setSchemaPattern(schemaPattern);
        }

        if (packageName != null) {
            LOGGER.log(Level.INFO, "Set the packageName paramters.");
            params.setPackageName(packageName);
        }

        LOGGER.log(Level.INFO, "Starting generate.");
        LQMG.generate(params, new FileSystemResourceAccessor());
        LOGGER.log(Level.INFO, "Ended generate.");
    }

    /**
     * The printing the help note.
     */
    public static void printHelp() {
        LOGGER.log(Level.INFO, "Print help note.");
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
        System.out.println("  --schemaToPackage: the schema to package or not; (default: true)");
        System.out.println("  --help: This help\n\n");
    }

    /**
     * Simple constructor.
     */
    private LQMG() {
        // private constructor for util class.
    }
}
