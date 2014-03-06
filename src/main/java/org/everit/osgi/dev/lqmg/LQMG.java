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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import liquibase.Liquibase;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;

import org.everit.osgi.dev.lqmg.internal.CustomNamingStrategy;
import org.everit.osgi.liquibase.bundle.LiquibaseOSGiUtil;
import org.everit.osgi.liquibase.bundle.OSGiResourceAccessor;
import org.h2.Driver;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.FrameworkWiring;

import com.mysema.query.sql.codegen.MetaDataExporter;

/**
 * This class responsible for generate QueryDSL JAVA classes.
 */
public class LQMG {

    public static final String CAPABILITY_LIQUIBASE_SCHEMA = "liquibase.schema";
    public static final String CAPABILITY_ATTR_SCHEMA_NAME = "name";
    public static final String CAPABILITY_ATTR_SCHEMA_RESOURCE = "resource";

    private static final String ARG_SCHEMA_TO_PACKAGE = "schemaToPackage";
    private static final String ARG_SCHEMA_PATTERN = "schemaPattern";
    private static final String ARG_TARGET_FOLDER = "targetFolder";
    private static final String ARG_PACKAGE_NAME = "packageName";
    private static final String ARG_BUNDLES = "bundles";
    private static final String ARG_SCHEMA = "schema";
    /**
     * The {@link Logger} instance for logging.
     */
    private static final Logger LOGGER = Logger.getLogger(LQMG.class.getName());

    private static File createTempDirectory() throws IOException {
        final File temp = File.createTempFile("lqmg-",
                Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: "
                    + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: "
                    + temp.getAbsolutePath());
        }

        return temp;
    }

    private static void deleteFolder(final File folder) {
        File[] files = folder.listFiles();
        if (files != null) { // some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    LQMG.deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private static String evaluateArgValue(final String fullArg, final String argName) {
        String result = fullArg.substring(("--" + argName + "=").length());
        LOGGER.log(Level.INFO, "The " + argName + " argument: " + result);
        return result;
    }

    private static void exportMetaData(final GenerationProperties parameters,
            final Connection connection) throws SQLException {
        LOGGER.log(Level.INFO, "Start meta data export.");
        MetaDataExporter metaDataExporter = new MetaDataExporter();
        metaDataExporter.setNamingStrategy(new CustomNamingStrategy());
        metaDataExporter.setPackageName(parameters.getPackageName());
        metaDataExporter.setSchemaPattern(parameters.getSchemaPattern());

        metaDataExporter.setSchemaToPackage(parameters.isSchemaToPackage());
        metaDataExporter
                .setTargetFolder(new File(parameters.getTargetFolder()));
        metaDataExporter.export(connection.getMetaData());
        LOGGER.log(Level.INFO, "Finish meta data export.");
    }

    /**
     * Generate the JAVA classes to QueryDSL from LiquiBase XML.
     * 
     * @param parameters
     *            the parameters for the generation. See more {@link GenerationProperties}.
     */
    public static void generate(final GenerationProperties parameters) {

        Framework osgiContainer = null;
        File tempDirectory = null;
        try {
            tempDirectory = LQMG.createTempDirectory();
            osgiContainer = LQMG.startOSGiContainer(parameters.getBundlePaths(), tempDirectory.getAbsolutePath());

            Map<Bundle, List<BundleCapability>> matchingBundles = LiquibaseOSGiUtil
                    .findBundlesBySchemaExpression(parameters.getSchema(),
                            osgiContainer.getBundleContext(), Bundle.RESOLVED);

            if (matchingBundles.size() == 0) {
                LQMG.handleCapabilityNotFound(parameters, osgiContainer);
            }
            if (matchingBundles.size() > 1) {
                LOGGER.log(Level.WARNING,
                        "Found multiple bundles containing matching capabilities for schema"
                                + " expression: '" + parameters.getSchema()
                                + "'. Using the first one from list: "
                                + matchingBundles.keySet().toString());
            }
            Entry<Bundle, List<BundleCapability>> matchingSchema = matchingBundles
                    .entrySet().iterator().next();
            Bundle bundle = matchingSchema.getKey();
            List<BundleCapability> matchingCapabilities = matchingSchema
                    .getValue();
            if (matchingCapabilities.size() > 1) {
                LOGGER.warning("There are multiple capabilities in bundle "
                        + bundle.toString()
                        + ". Using the first one from the list: "
                        + matchingCapabilities.toString());
            }
            BundleCapability matchingCapability = matchingCapabilities.get(0);
            LQMG.tryCodeGeneration(parameters, bundle, matchingCapability);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not create temp directory", e);
            return;
        } catch (BundleException e) {
            LOGGER.log(Level.SEVERE, "Could not start embedded OSGi framework", e);
        } finally {
            if (osgiContainer != null) {
                try {
                    osgiContainer.stop();
                    osgiContainer.waitForStop(0);
                } catch (BundleException e) {
                    LOGGER.log(Level.SEVERE, "Could not stop embedded OSGi container during code generation", e);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Stopping of embedded OSGi container was interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }
            if (tempDirectory != null) {
                LQMG.deleteFolder(tempDirectory);
            }
        }

    }

    private static void handleCapabilityNotFound(final GenerationProperties parameters, final Framework osgiContainer) {
        BundleContext systemBundleContext = osgiContainer.getBundleContext();
        Bundle[] bundles = systemBundleContext.getBundles();

        for (Bundle bundle : bundles) {
            if (bundle.getState() == Bundle.INSTALLED) {
                try {
                    bundle.start();
                } catch (BundleException e) {
                    LOGGER.log(Level.WARNING, "The bundle " + bundle.toString() + " could not be resolved", e);
                }
            }
        }

        throw new LiquiBaseQueryDSLModelGeneratorException(
                "Could not find matching capability in any of the bundles for schema expression: "
                        + parameters.getSchema(), null);
    }

    /**
     * The command line processor.
     * 
     * @param args
     *            the arguments.
     */
    public static void main(final String[] args) {
        if ((args.length == 0)
                || ((args.length == 1) && "--help".equals(args[0]))) {
            LOGGER.log(Level.INFO, args.length != 1 ? "No arguments."
                    : "Called the command help.");
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
            if (args[i].startsWith("--" + ARG_SCHEMA + "=")) {
                logicalFilePath = LQMG.evaluateArgValue(args[i],
                        ARG_SCHEMA);
            } else if (args[i].startsWith("--" + ARG_PACKAGE_NAME + "=")) {
                packageName = LQMG.evaluateArgValue(args[i], ARG_PACKAGE_NAME);
            } else if (args[i].startsWith("--" + ARG_BUNDLES + "=")) {
                bundlesParam = LQMG.evaluateArgValue(args[i], ARG_BUNDLES);
            } else if (args[i].startsWith("--" + ARG_TARGET_FOLDER + "=")) {
                targetFolder = LQMG.evaluateArgValue(args[i], ARG_TARGET_FOLDER);
            } else if (args[i].startsWith("--" + ARG_SCHEMA_PATTERN + "=")) {
                schemaPattern = LQMG.evaluateArgValue(args[i], ARG_SCHEMA_PATTERN);
            } else if (args[i].startsWith("--" + ARG_SCHEMA_TO_PACKAGE + "=")) {
                schemaToPackage = Boolean.valueOf(LQMG.evaluateArgValue(args[i],
                        ARG_SCHEMA_TO_PACKAGE));
            } else {
                LOGGER.log(
                        Level.INFO,
                        "Unknow parameter: "
                                + args[i]
                                + "'. Run <with --help to get information about the possible parameters");
            }
        }
        LOGGER.log(Level.INFO, "Processed arguments.");

        if (logicalFilePath == null) {
            LOGGER.log(Level.SEVERE, "Missing required argument: "
                    + ARG_SCHEMA);
        }
        if (targetFolder == null) {
            LOGGER.log(Level.SEVERE, "Missing required argument: "
                    + ARG_TARGET_FOLDER);
        }
        if (bundlesParam == null) {
            LOGGER.log(Level.SEVERE, "Missing required argument: "
                    + ARG_BUNDLES);
        }
        if ((logicalFilePath == null) || (targetFolder == null)
                || (bundlesParam == null)) {
            return;
        }

        GenerationProperties params = new GenerationProperties(logicalFilePath,
                bundlesParam.split(";"), targetFolder);

        LOGGER.log(Level.INFO, "Set the schemaToPackage paramters.");
        params.setSchemaToPackage(schemaToPackage);

        if (schemaPattern != null) {
            LOGGER.log(Level.INFO, "Set the schemaPattern paramters.");
            params.setSchemaPattern(schemaPattern);
        }

        LOGGER.log(Level.INFO, "Set the packageName paramters.");
        params.setPackageName(packageName);

        LOGGER.log(Level.INFO, "Starting generate.");
        LQMG.generate(params);
        LOGGER.log(Level.INFO, "Ended generate.");
    }

    /**
     * The printing the help note.
     */
    public static void printHelp() {
        LOGGER.log(Level.INFO, "Print help note.");
        System.out
                .println("Example usage: lqmg.sh --schema=myApp --packageName=foo"
                        + " --targetFolder=/tmp/generated\n");
        System.out.println("Arguments: \n");
        System.out
                .println("  --"
                        + ARG_SCHEMA
                        + ": Name of the schema that is listed in the Provide-Capability of a bundle. "
                        + "It is possible to define filter on the Capability as well.");
        System.out
                .println("  --"
                        + ARG_BUNDLES
                        + ": The path to the persistent bundles separated by semicolon");
        System.out
                .println("  --"
                        + ARG_PACKAGE_NAME
                        + ": The java package of the generated QueryDSL metamodel classes"
                        + " (default: empty, that means that the package will be either empty or derived from the schema)");
        System.out.println("  --" + ARG_TARGET_FOLDER
                + ": The folder where source will be generated to");
        System.out
                .println("  --"
                        + ARG_SCHEMA_PATTERN
                        + ": a schema name pattern; must match the schema name as it is stored in"
                        + " the database; \"\" retrieves those without a schema; null means that the schema name should not"
                        + " be used to narrow the search (default: null)");
        System.out.println("  --" + ARG_SCHEMA_TO_PACKAGE
                + ": the schema to package or not; (default: true)");
        System.out.println("  --help: This help\n\n");
    }

    private static Framework startOSGiContainer(final String[] bundlePaths, final String tempDirPath)
            throws BundleException {
        FrameworkFactory frameworkFactory = ServiceLoader
                .load(FrameworkFactory.class).iterator().next();

        Map<String, String> config = new HashMap<String, String>();
        config.put("osgi.configuration.area", tempDirPath);
        config.put("osgi.baseConfiguration.area", tempDirPath);
        config.put("osgi.sharedConfiguration.area", tempDirPath);
        config.put("osgi.instance.area", tempDirPath);
        config.put("osgi.user.area", tempDirPath);

        Framework framework = frameworkFactory.newFramework(config);
        framework.start();

        BundleContext systemBundleContext = framework.getBundleContext();
        for (String bundlePath : bundlePaths) {
            try {
                systemBundleContext.installBundle(bundlePath);
            } catch (BundleException e) {
                LOGGER.log(Level.WARNING, "Could not start bundle " + bundlePath, e);
            }
        }
        FrameworkWiring frameworkWiring = framework
                .adapt(FrameworkWiring.class);
        frameworkWiring.resolveBundles(null);

        return framework;
    }

    private static void tryCodeGeneration(
            final GenerationProperties parameters, final Bundle bundle,
            final BundleCapability bundleCapability) {

        LOGGER.log(Level.INFO, "Load driver.");
        Driver h2Driver = Driver.load();
        LOGGER.log(Level.INFO, "Loaded driver.");
        Connection connection = null;
        try {
            LOGGER.log(Level.INFO, "Creating connection.");
            connection = h2Driver.connect("jdbc:h2:mem:", new Properties());
            LOGGER.log(Level.INFO, "Created connection.");

            LOGGER.log(Level.INFO, "Get database.");
            AbstractJdbcDatabase database = new H2Database();
            database.setConnection(new JdbcConnection(connection));

            LOGGER.log(Level.INFO, "Start LiquiBase and update.");
            ResourceAccessor resourceAccessor = new OSGiResourceAccessor(bundle);
            String schemaResource = (String) bundleCapability.getAttributes()
                    .get(LiquibaseOSGiUtil.ATTR_SCHEMA_RESOURCE);
            Liquibase liquibase = new Liquibase(schemaResource,
                    resourceAccessor, database);
            liquibase.update((String) null);
            LOGGER.log(Level.INFO, "Finish LiquiBase and update.");

            LQMG.exportMetaData(parameters, connection);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            // error to create connection.
            // error connection.getMetaData
            // error when export database.
            throw new LiquiBaseQueryDSLModelGeneratorException(
                    "Error during try to connection the database.", e);
        } catch (LiquibaseException e) {
            // liquibase.update(null);
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new LiquiBaseQueryDSLModelGeneratorException(
                    "Error during processing XML file; "
                            + parameters.getSchema(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    LOGGER.log(Level.INFO, "Connection closed.");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new LiquiBaseQueryDSLModelGeneratorException(
                            "Closing the connection was unsuccessful.", e);
                }
            }
        }
    }

    /**
     * Simple constructor.
     */
    private LQMG() {
        // private constructor for util class.
    }
}
