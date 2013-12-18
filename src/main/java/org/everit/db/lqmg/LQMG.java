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

    private static final String ARG_SCHEMA_TO_PACKAGE = "schemaToPackage";
    private static final String ARG_SCHEMA_PATTERN = "schemaPattern";
    private static final String ARG_TARGET_FOLDER = "targetFolder";
    private static final String ARG_PACKAGE_NAME = "packageName";
    private static final String ARG_BUNDLES = "bundles";
    private static final String ARG_LOGICAL_FILE_PATH = "logicalFilePath";
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
    public static void generate(final GenerationProperties parameters) {
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
            Liquibase liquibase = new Liquibase(parameters.getLogicalFilePath(), resourceAccessor, database);
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
            // error to create connection.
            // error connection.getMetaData
            // error when export database.
            throw new LiquiBaseQueryDSLModellGeneratorException("Error during try to connection the database.", e);
        } catch (DatabaseException e) {
            // fincorrectDataBaseImplementation
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new LiquiBaseQueryDSLModellGeneratorException("Unable to find the correct database implementation", e);
        } catch (LiquibaseException e) {
            // liquibase.update(null);
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new LiquiBaseQueryDSLModellGeneratorException("Error during processing XML file; "
                    + parameters.getLogicalFilePath(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    LOGGER.log(Level.INFO, "Connection closed.");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new LiquiBaseQueryDSLModellGeneratorException("Closing the connection was unsuccessful.", e);
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

        String logicalFilePath = null;
        String packageName = "";
        String targetFolder = null;
        String schemaPattern = null;
        String bundlesParam = null;
        Boolean schemaToPackage = true;

        LOGGER.log(Level.INFO, "Processing arguments.");
        for (int i = 0, n = args.length; i < n; i++) {
            if (args[i].startsWith("--" + ARG_LOGICAL_FILE_PATH)) {
                logicalFilePath = evaluateArgValue(args[i], ARG_LOGICAL_FILE_PATH);
            } else if (args[i].startsWith("--" + ARG_PACKAGE_NAME + "=")) {
                packageName = evaluateArgValue(args[i], ARG_PACKAGE_NAME);
            } else if (args[i].startsWith("--" + ARG_BUNDLES + "=")) {
                bundlesParam = evaluateArgValue(args[i], ARG_BUNDLES);
            } else if (args[i].startsWith("--" + ARG_TARGET_FOLDER + "=")) {
                targetFolder = evaluateArgValue(args[i], ARG_TARGET_FOLDER);
            } else if (args[i].startsWith("--" + ARG_SCHEMA_PATTERN + "=")) {
                schemaPattern = evaluateArgValue(args[i], ARG_SCHEMA_PATTERN);
            } else if (args[i].startsWith("--" + ARG_SCHEMA_TO_PACKAGE + "=")) {
                schemaToPackage = Boolean.valueOf(evaluateArgValue(args[i], ARG_SCHEMA_TO_PACKAGE));
            } else {
                LOGGER.log(Level.INFO, "Unknow parameter: " + args[i]
                        + "'. Run <with --help to get information about the possible parameters");
            }
        }
        LOGGER.log(Level.INFO, "Processed arguments.");

        if (logicalFilePath == null) {
            LOGGER.log(Level.SEVERE, "Missing required argument: " + ARG_LOGICAL_FILE_PATH);
        }
        if (targetFolder == null) {
            LOGGER.log(Level.SEVERE, "Missing required argument: " + ARG_TARGET_FOLDER);
        }
        if (bundlesParam == null) {
            LOGGER.log(Level.SEVERE, "Missing required argument: " + ARG_BUNDLES);
        }
        if (logicalFilePath == null || targetFolder == null || bundlesParam == null) {
            return;
        }

        GenerationProperties params = new GenerationProperties(logicalFilePath, bundlesParam.split(";"), targetFolder);
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
        LQMG.generate(params);
        LOGGER.log(Level.INFO, "Ended generate.");
    }

    private static String evaluateArgValue(String fullArg, String argName) {
        String result = fullArg.substring(("--" + argName + "=").length());
        LOGGER.log(Level.INFO, "The " + argName + " argument: " + result);
        return result;
    }

    /**
     * The printing the help note.
     */
    public static void printHelp() {
        LOGGER.log(Level.INFO, "Print help note.");
        System.out.println("Example usage: lqmg.sh --changeLogFile=/tmp/changelog.xml --packageName=foo"
                + " --targetFolder=/tmp/generated\n");
        System.out.println("Arguments: \n");
        System.out.println("  --" + ARG_LOGICAL_FILE_PATH + ": Logical file path of the schema");
        System.out.println("  --" + ARG_BUNDLES + ": The path to the persistent bundles separated by semicolon");
        System.out.println("  --" + ARG_PACKAGE_NAME + ": The java package of the generated QueryDSL metamodel classes"
                + " (default: empty, that means that the package will be either empty or derived from the schema)");
        System.out.println("  --" + ARG_TARGET_FOLDER + ": The folder where source will be generated to");
        System.out.println("  --" + ARG_SCHEMA_PATTERN
                + ": a schema name pattern; must match the schema name as it is stored in"
                + " the database; \"\" retrieves those without a schema; null means that the schema name should not"
                + " be used to narrow the search (default: null)");
        System.out.println("  --" + ARG_SCHEMA_TO_PACKAGE + ": the schema to package or not; (default: true)");
        System.out.println("  --help: This help\n\n");
    }

    /**
     * Simple constructor.
     */
    private LQMG() {
        // private constructor for util class.
    }
}
