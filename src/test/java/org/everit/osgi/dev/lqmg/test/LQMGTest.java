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
package org.everit.osgi.dev.lqmg.test;

import java.io.File;
import java.net.URL;
import java.util.UUID;

import org.everit.osgi.dev.lqmg.GenerationProperties;
import org.everit.osgi.dev.lqmg.LQMG;
import org.everit.osgi.dev.lqmg.LQMGException;
import org.junit.Assert;
import org.junit.Test;

public class LQMGTest {

    private static void deleteFolder(final File folder) {
        File[] files = folder.listFiles();
        if (files != null) { // some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    LQMGTest.deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    /**
     * Testing the normal usage of the LQMG module.
     */
    @Test
    public void testLQMG() {
        String tmpDirProperty = "java.io.tmpdir";
        String tmpDir = System.getProperty(tmpDirProperty);
        if (tmpDir == null) {
            Assert.fail("User temp directory could not be retrieved");
        }

        ClassLoader classLoader = LQMGTest.class.getClassLoader();
        URL bundle1URL = classLoader.getResource("META-INF/testBundles/bundle1/");
        URL bundle2URL = classLoader.getResource("META-INF/testBundles/bundle2/");

        UUID uuid = UUID.randomUUID();
        File tmpDirFile = new File(tmpDir);
        File testDirFile = new File(tmpDirFile, "lqmgtest-" + uuid.toString());
        String tempFolderName = testDirFile.getAbsolutePath();

        GenerationProperties props = new GenerationProperties("myApp", new String[] {
                "reference:" + bundle2URL.toExternalForm(),
                "reference:" + bundle1URL.toExternalForm() }, tempFolderName);
        // props.setConfigurationPath("/tmp/xxxx");
        try {
            LQMG.generate(props);
        } finally {
            LQMGTest.deleteFolder(testDirFile);
        }
    }

    /**
     * Test three cases: - when there are no matching capability for the given schema /n - when there are multiple
     * capabilities in a bundle matching the given schema /n - when found multiple bundles containing matching
     * capabilities for the given schema
     */
    @Test
    public void testLQMGNotNormalCalls() {
        String tmpDirProperty = "java.io.tmpdir";
        String tmpDir = System.getProperty(tmpDirProperty);
        if (tmpDir == null) {
            Assert.fail("User temp directory could not be retrieved");
        }

        ClassLoader classLoader = LQMGTest.class.getClassLoader();
        URL bundle1URL = classLoader.getResource("META-INF/testBundles/bundle1/");
        URL bundle2URL = classLoader.getResource("META-INF/testBundles/bundle2/");

        UUID uuid = UUID.randomUUID();
        File tmpDirFile = new File(tmpDir);
        File testDirFile = new File(tmpDirFile, "lqmgtest-" + uuid.toString());
        String tempFolderName = testDirFile.getAbsolutePath();

        GenerationProperties props = new GenerationProperties("notExistingmyApp", new String[] {
                "reference:" + bundle2URL.toExternalForm(),
                "reference:" + bundle1URL.toExternalForm() }, tempFolderName);

        GenerationProperties props2 = new GenerationProperties("doubledCap", new String[] {
                "reference:" + bundle2URL.toExternalForm(),
                "reference:" + bundle1URL.toExternalForm() }, tempFolderName);
        // props2.setSchema("doubledCap");
        props2.setTargetFolder(tempFolderName);

        try {
            LQMG.generate(props2);

            LQMG.generate(props);
            Assert.assertTrue(false);
        } catch (LQMGException e) {
            Assert.assertTrue(true);
        } finally {
            LQMGTest.deleteFolder(testDirFile);
        }
    }

    @Test
    public void testLQMGwithWrongSQL() {
        String tmpDirProperty = "java.io.tmpdir";
        String tmpDir = System.getProperty(tmpDirProperty);
        if (tmpDir == null) {
            Assert.fail("User temp directory could not be retrieved");
        }

        ClassLoader classLoader = LQMGTest.class.getClassLoader();
        URL bundle1URL = classLoader.getResource("META-INF/testBundles/bundle1/");
        URL bundle2URL = classLoader.getResource("META-INF/testBundles/bundle2/");

        UUID uuid = UUID.randomUUID();
        File tmpDirFile = new File(tmpDir);
        File testDirFile = new File(tmpDirFile, "lqmgtest-" + uuid.toString());
        String tempFolderName = testDirFile.getAbsolutePath();

        GenerationProperties props = new GenerationProperties("wrongSQL", new String[] {
                "reference:" + bundle2URL.toExternalForm(),
                "reference:" + bundle1URL.toExternalForm() }, tempFolderName);
        try {
            LQMG.generate(props);
            Assert.assertTrue(false);
        } catch (LQMGException e) {
            Assert.assertTrue(true);
        } finally {
            LQMGTest.deleteFolder(testDirFile);
        }
    }

    // /**
    // * Testing the command line processor with various attributes.
    // */
    // @Test
    // public void testMainArguments() {
    // String tmpDirProperty = "java.io.tmpdir";
    // String tmpDir = System.getProperty(tmpDirProperty);
    // if (tmpDir == null) {
    // Assert.fail("User temp directory could not be retrieved");
    // }
    //
    // UUID uuid = UUID.randomUUID();
    // File tmpDirFile = new File(tmpDir);
    // File testDirFile = new File(tmpDirFile, "lqmgtest-" + uuid.toString());
    //
    // ClassLoader classLoader = LQMGTest.class.getClassLoader();
    // URL bundle1URL = classLoader.getResource("META-INF/testBundles/bundle1/");
    // URL bundle2URL = classLoader.getResource("META-INF/testBundles/bundle2/");
    //
    // String externalForm = bundle1URL.toExternalForm();
    // String externalForm2 = bundle2URL.toExternalForm();
    //
    // try {
    // String[] strings = {};
    // LQMG.main(strings);
    // strings = new String[] { "--help" };
    // LQMG.main(strings);
    // strings = new String[] { "--nincsilyen=valami" };
    // LQMG.main(strings);
    // strings = new String[] { "--schema=myApp", "--targetFolder=/tmp/generated" };
    // LQMG.main(strings);
    // strings = new String[] { "--schema=myApp", "--bundles=" + externalForm };
    // LQMG.main(strings);
    // strings = new String[] { "--schema=myApp", "--bundles=" + externalForm + ';' + externalForm2,
    // "--targetFolder=/tmp/generated" };
    // LQMG.main(strings);
    // strings = new String[] { "--schema=myApp", "--bundles=" + externalForm + ';' + externalForm2,
    // "--packageName=foo", "--targetFolder=/tmp/generated", "--schemaToPackage=false",
    // "--schemaPattern=liquibase.schema" };
    // LQMG.main(strings);
    // } finally {
    // LQMGTest.deleteFolder(testDirFile);
    // }
    // }
}
