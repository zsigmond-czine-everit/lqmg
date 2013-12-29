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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.utils.manifest.Attribute;
import org.apache.felix.utils.manifest.Clause;
import org.apache.felix.utils.manifest.Directive;
import org.apache.felix.utils.manifest.Parser;
import org.h2.Driver;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.resource.Capability;

import com.mysema.query.sql.codegen.MetaDataExporter;

/**
 * This class responsible for generate QueryDSL JAVA classes.
 */
public class LQMG {

    public static final String CAPABILITY_LIQUIBASE_SCHEMA = "liquibase.schema";
    public static final String CAPABILITY_ATTR_SCHEMA_NAME = "schemaName";
    public static final String CAPABILITY_ATTR_SCHEMA_RESOURCE = "schemaResource";

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

        Framework osgiContainer = null;
        try {
            osgiContainer = startOSGiContainer(parameters.getBundlePaths());

            BundleCapability[] capabilities = findMatchingCapabilities(osgiContainer, parameters.getTopSchema());
            if (capabilities.length == 0) {
                LOGGER.log(Level.WARNING, "Did not find matching schema for generation");
                return;
            }
            for (BundleCapability bundleCapability : capabilities) {
                tryCodeGeneration(parameters, bundleCapability);
            }
        } finally {
            if (osgiContainer != null) {
                try {
                    osgiContainer.stop();
                    osgiContainer.waitForStop(0);
                } catch (BundleException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

    }

    private static void tryCodeGeneration(final GenerationProperties parameters, BundleCapability bundleCapability) {

        Map<String, Object> attributes = bundleCapability.getAttributes();
        Object schemaResourceAttribute = attributes.get(CAPABILITY_ATTR_SCHEMA_RESOURCE);
        if (schemaResourceAttribute == null) {
            // TODO throw a runtime exception
        }
        

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
            Liquibase liquibase = new Liquibase(parameters.getTopSchema(), resourceAccessor, database);
            liquibase.update(null);
            LOGGER.log(Level.INFO, "Finish LiguiBase and update.");

            exportMetaData(parameters, connection);
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
                    + parameters.getTopSchema(), e);
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

    private static void exportMetaData(final GenerationProperties parameters, final Connection connection)
            throws SQLException {
        LOGGER.log(Level.INFO, "Start meta data export.");
        MetaDataExporter metaDataExporter = new MetaDataExporter();
        metaDataExporter.setNamingStrategy(new CustomNamingStrategy());
        metaDataExporter.setPackageName(parameters.getPackageName());
        metaDataExporter.setSchemaPattern(parameters.getSchemaPattern());

        metaDataExporter.setSchemaToPackage(parameters.isSchemaToPackage());
        metaDataExporter.setTargetFolder(new File(parameters.getTargetFolder()));
        metaDataExporter.export(connection.getMetaData());
        LOGGER.log(Level.INFO, "Finish meta data export.");
    }

    private static BundleCapability[] findMatchingCapabilities(Framework framework, String topSchema) {
        Clause[] clauses = Parser.parseHeader(topSchema);
        if (clauses.length != 1) {
            // TODO throw exception
        }
        Clause clause = clauses[0];
        String schemaName = clause.getName();
        Attribute[] attributes = clause.getAttributes();
        if (attributes.length > 0) {
            // TODO throw excetpion that no attributes are supported
        }
        Directive[] directives = clause.getDirectives();
        if (directives.length > 1) {
            // TODO throw exception that onl
        }
        Filter filter = null;
        if (directives.length == 1) {
            if (!Constants.FILTER_DIRECTIVE.equals(directives[0].getName())) {
                // TODO throw exception as only filter is supported
            }
            String filterString = directives[0].getValue();
            try {
                filter = FrameworkUtil.createFilter(filterString);
            } catch (InvalidSyntaxException e) {
                // TODO throw runtime exception
                e.printStackTrace();
            }
        }

        return findMatchingCapabilities(framework, schemaName, filter);
    }

    private static BundleCapability[] findMatchingCapabilities(Framework framework, String schemaName, Filter filter) {
        List<BundleCapability> capabilities = new ArrayList<BundleCapability>();

        Bundle[] bundles = framework.getBundleContext().getBundles();
        for (Bundle bundle : bundles) {
            BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
            List<BundleCapability> potentialCapabilities = bundleWiring.getCapabilities(CAPABILITY_LIQUIBASE_SCHEMA);
            for (BundleCapability potentialCapability : potentialCapabilities) {
                Map<String, Object> attributes = potentialCapability.getAttributes();
                Object schemaNameAttribute = attributes.get(CAPABILITY_ATTR_SCHEMA_NAME);
                if (schemaName.equals(schemaNameAttribute) && filter.matches(attributes)) {
                    capabilities.add(potentialCapability);
                }
            }
        }

        return capabilities.toArray(new BundleCapability[0]);
    }

    private static Framework startOSGiContainer(String[] bundlePaths) {
        HashMap<Object, Object> felixConfig = new HashMap<Object, Object>();
        // We do not want to start any bundles just to resolve them
        felixConfig.put(FelixConstants.BUNDLE_DEFAULT_STARTLEVEL, 4);
        felixConfig.put(FelixConstants.FRAMEWORK_DEFAULT_STARTLEVEL, 1);
        Felix felix = new Felix(new HashMap<Object, Object>());

        try {
            felix.init();
            felix.start();
        } catch (BundleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        BundleContext systemBundleContext = felix.getBundleContext();
        for (String bundlePath : bundlePaths) {
            try {
                systemBundleContext.installBundle(bundlePath);
            } catch (BundleException e) {
                // TODO Log error
                e.printStackTrace();
            }
        }
        return felix;
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
