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
    private final String[] bundlePaths;

    /**
     * A schema name with optional filter expression that points to a bundle schema capability. E.g.:
     * userMgmt;filter:="(version=2)"
     */
    private String capability;

    /**
     * Optional path of a configuration XML. In case this configuration exists, the rules in it are stronger than the
     * rules specified at the capability attributes in the bundles.
     */
    private String configurationPath;

    /**
     * Comma separated list of java packages that should be generated. Null means that all packages should be generated.
     */
    private String[] packages = new String[0];

    /**
     * The folder where source will be generated to.
     */
    private String targetFolder;

    /**
     * The simple constructor.
     * 
     * @param topSchema
     *            A schema name with optional capability filter where liquibase should start searching for changesets.
     * @param targetFolder
     *            the folder where source will be generated to.
     */
    public GenerationProperties(final String topSchema, final String[] bundlePaths, final String targetFolder) {
        this.targetFolder = targetFolder;
        this.bundlePaths = bundlePaths;
    }

    public String[] getBundlePaths() {
        return bundlePaths;
    }

    public String getCapability() {
        return capability;
    }

    public String getConfigurationPath() {
        return configurationPath;
    }

    public String[] getPackages() {
        return packages;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public void setCapability(String schema) {
        this.capability = schema;
    }

    public void setConfigurationPath(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    public void setPackages(String[] packages) {
        this.packages = packages;
    }

    public void setTargetFolder(final String targetFolder) {
        this.targetFolder = targetFolder;
    }

}
