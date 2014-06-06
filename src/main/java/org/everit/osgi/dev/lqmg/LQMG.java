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
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.everit.osgi.dev.lqmg.internal.ConfigPath;
import org.everit.osgi.dev.lqmg.internal.ConfigurationContainer;
import org.everit.osgi.dev.lqmg.internal.LQMGChangeExecListener;
import org.everit.osgi.dev.lqmg.internal.LQMGMetadataExporter;
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

/**
 * This class responsible for generate QueryDSL JAVA classes.
 */
public class LQMG {

    public static final String CAPABILITY_ATTR_SCHEMA_NAME = "name";
    public static final String CAPABILITY_ATTR_SCHEMA_RESOURCE = "resource";
    public static final String CAPABILITY_LIQUIBASE_SCHEMA = "liquibase.schema";
    public static final String CAPABILITY_LQMG_CONFIG_RESOURCE = "lqmg.config.resource";

    private static HackUtil frameworkUtil = new EquinoxHackUtilImpl();

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

    private static void exportMetaData(final GenerationProperties parameters,
            final Connection connection, ConfigurationContainer configContainer) throws SQLException {
        LOGGER.log(Level.INFO, "Start meta data export.");
        LQMGMetadataExporter metaDataExporter = new LQMGMetadataExporter(configContainer, parameters.getPackages());

        metaDataExporter.setTargetFolder(new File(parameters.getTargetFolder()));
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
            osgiContainer = LQMG.startOSGiContainer(parameters.getBundleLocations(), tempDirectory.getAbsolutePath());

            Map<Bundle, List<BundleCapability>> matchingBundles = LiquibaseOSGiUtil
                    .findBundlesBySchemaExpression(parameters.getSchema(),
                            osgiContainer.getBundleContext(), Bundle.RESOLVED);

            if (matchingBundles.size() == 0) {
                if (parameters.isHackWires()) {
                    LOGGER.info("No matching bundle found. Trying to find unresolved bundles and hack their wires.");
                    frameworkUtil.hackBundles(osgiContainer, tempDirectory);
                    FrameworkWiring frameworkWiring = osgiContainer.adapt(FrameworkWiring.class);
                    frameworkWiring.resolveBundles(null);

                    matchingBundles = LiquibaseOSGiUtil
                            .findBundlesBySchemaExpression(parameters.getSchema(),
                                    osgiContainer.getBundleContext(), Bundle.RESOLVED);
                } else {
                    LOGGER.severe("No matching bundle found. Probably setting hackWires to true would help");
                }
                if (matchingBundles.size() == 0) {
                    LQMG.throwCapabilityNotFound(parameters, osgiContainer);
                }
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

    private static void throwCapabilityNotFound(final GenerationProperties parameters, final Framework osgiContainer) {

        logUnresolvedBundles(osgiContainer);

        throw new LQMGException(
                "Could not find matching capability in any of the bundles for schema expression: "
                        + parameters.getSchema(), null);
    }

    private static void logUnresolvedBundles(final Framework osgiContainer) {
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
    }

    private static Framework startOSGiContainer(final String[] bundleLocations, final String tempDirPath)
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
        for (String bundleLocation : bundleLocations) {
            try {
                systemBundleContext.installBundle(bundleLocation);
            } catch (BundleException e) {
                LOGGER.log(Level.WARNING, "Could not start bundle " + bundleLocation, e);
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
            
            generateLQMGTables(database);

            LOGGER.log(Level.INFO, "Start LiquiBase and update.");
            Map<String, Object> attributes = bundleCapability.getAttributes();
            
            ResourceAccessor resourceAccessor = new OSGiResourceAccessor(bundle, attributes);

            String schemaResource = (String) attributes.get(LiquibaseOSGiUtil.ATTR_SCHEMA_RESOURCE);
            Liquibase liquibase = new Liquibase(schemaResource, resourceAccessor, database);

            ConfigurationContainer configContainer = new ConfigurationContainer();

            if (parameters.getConfigurationPath() != null) {
                configContainer.addConfiguration(new ConfigPath(null, parameters.getConfigurationPath()));
            }

            LQMGChangeExecListener lqmgChangeExecListener = new LQMGChangeExecListener(configContainer);
            liquibase.setChangeExecListener(lqmgChangeExecListener);

            liquibase.update((String) null);
            LOGGER.log(Level.INFO, "Finish LiquiBase and update.");

            LQMG.exportMetaData(parameters, connection, configContainer);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            // error to create connection.
            // error connection.getMetaData
            // error when export database.
            throw new LQMGException(
                    "Error during try to connection the database.", e);
        } catch (LiquibaseException e) {
            // liquibase.update(null);
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new LQMGException(
                    "Error during processing XML file; "
                            + parameters.getSchema(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    LOGGER.log(Level.INFO, "Connection closed.");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new LQMGException(
                            "Closing the connection was unsuccessful.", e);
                }
            }
        }
    }

    private static void generateLQMGTables(AbstractJdbcDatabase database) throws LiquibaseException {
        Liquibase liquibase = new Liquibase("META-INF/lqmg.changelog.xml", new ClassLoaderResourceAccessor(
                LQMG.class.getClassLoader()), database);
        
        liquibase.update((String) null); 
    }

    /**
     * Simple constructor.
     */
    private LQMG() {
        // private constructor for util class.
    }
}
