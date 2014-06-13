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

/**
 * This class store all parameters which is required to the JAVA classes generation.
 */
public class GenerationProperties {

    /**
     * The paths to the bundles (directory path of jar file path).
     */
    private final String[] bundleLocations;

    /**
     * A schema name with optional filter expression that points to a bundle schema capability. E.g.:
     * userMgmt;filter:="(version=2)"
     */
    private String capability;
    
    /**
     * Default schema on SQL Connection to be used during generating tables.
     */
    private String defaultSchema;

    /**
     * Optional path of a configuration XML. In case this configuration exists, the rules in it are stronger than the
     * rules specified at the capability attributes in the bundles.
     */
    private String configurationPath;

    /**
     * Comma separated list of java packages that should be generated. Null means that all packages should be generated.
     * Classes for tables coming from Liquibase (changelog and lock tables) are never generated.
     */
    private String[] packages = new String[0];

    /**
     * The folder where source will be generated to.
     */
    private String targetFolder;

    /**
     * If true, LQMG will update the unresolved bundles in the way that all of their unsatisfied requirements will be
     * modified to be optional.
     */
    private boolean hackWires = true;
    
    /**
     * If true, inner classes will be generated for foreign and primary keys.
     */
    private boolean innerClassesForKeys = true;

    /**
     * The simple constructor.
     *
     * @param capability
     *            A schema name with optional capability filter where liquibase should start searching for changesets.
     * @param targetFolder
     *            the folder where source will be generated to.
     */
    public GenerationProperties(final String capability, final String[] bundleLocations, final String targetFolder) {
        this.targetFolder = targetFolder;
        this.bundleLocations = bundleLocations;
        this.capability = capability;
    }

    public String[] getBundleLocations() {
        return bundleLocations;
    }

    public String getConfigurationPath() {
        return configurationPath;
    }

    public String[] getPackages() {
        return packages;
    }

    public String getCapability() {
        return capability;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public boolean isHackWires() {
        return hackWires;
    }

    public void setConfigurationPath(final String configurationPath) {
        this.configurationPath = configurationPath;
    }

    public void setHackWires(final boolean strict) {
        hackWires = strict;
    }

    public void setPackages(final String[] packages) {
        if (packages == null) {
            throw new IllegalArgumentException("Packages cannot be null. In case all packages should be included, a"
                    + " zero length array should be used.");
        }
        this.packages = packages;
    }

    public void setCapability(final String schema) {
        this.capability = schema;
    }

    public void setTargetFolder(final String targetFolder) {
        this.targetFolder = targetFolder;
    }
    
    public void setInnerClassesForKeys(boolean innerClassesForKeys) {
        this.innerClassesForKeys = innerClassesForKeys;
    }
    
    public boolean isInnerClassesForKeys() {
        return innerClassesForKeys;
    }
    
    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }
    
    public String getDefaultSchema() {
        return defaultSchema;
    }
}
