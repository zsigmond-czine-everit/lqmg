/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.dev.lqmg.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.xml.bind.UnmarshalException;

import org.apache.commons.io.FileUtils;
import org.everit.persistence.lqmg.GenerationProperties;
import org.everit.persistence.lqmg.LQMG;
import org.everit.persistence.lqmg.LQMGException;
import org.everit.persistence.lqmg.LQMGMain;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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

  // @Test
  // public void _00_generateQDSLForLQMG() {
  // String tmpDirProperty = "java.io.tmpdir";
  // String tmpDir = System.getProperty(tmpDirProperty);
  // if (tmpDir == null) {
  // Assert.fail("User temp directory could not be retrieved");
  // }
  //
  // ClassLoader classLoader = LQMGTest.class.getClassLoader();
  // URL bundle1URL = classLoader.getResource("META-INF/testBundles/bundle1/");
  // URL bundle2URL = classLoader.getResource("META-INF/testBundles/bundle2/");
  //
  // UUID uuid = UUID.randomUUID();
  // File tmpDirFile = new File(tmpDir);
  // File testDirFile = new File(tmpDirFile, "lqmgtest-" + uuid.toString());
  //
  // GenerationProperties props = new GenerationProperties("myApp", new String[] {
  // "reference:" + bundle2URL.toExternalForm(),
  // "reference:" + bundle1URL.toExternalForm() },
  // "C:/Users/balazs_zsoldos/git/osgi-lqmg/src/main/generated/java");
  //
  // props.setPackages(new String[] { "org.everit.osgi.dev.lqmg.internal.schema.qdsl" });
  //
  // try {
  // File configFile = new File(testDirFile, "config.xml");
  // URL globalConfigURL = this.getClass().getResource("/META-INF/lqmg.lqmg.xml");
  // FileUtils.copyURLToFile(globalConfigURL, configFile);
  // props.setConfigurationPath(configFile.getAbsolutePath());
  // LQMG.generate(props);
  // // TODO check if generated classes are ok.
  // } catch (IOException e) {
  // throw new RuntimeException(e);
  // } finally {
  // }
  // }

  /**
   * Testing the normal usage of the LQMG module.
   */
  @Test
  public void _01_testLQMGNoConfiguration() {
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
    try {
      LQMG.generate(props);
      // TODO Test if there are no generated classes
    } finally {
      LQMGTest.deleteFolder(testDirFile);
    }
  }

  /**
   * Testing the normal usage of the LQMG module.
   */
  @Test
  public void _02_testLQMGWrongConfigurationURL() {
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
    props.setConfigurationPath("/tmp/xxxx");
    try {
      LQMG.generate(props);
      Assert.fail("An LQMG exception should have been thrown.");
    } catch (LQMGException e) {
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof UnmarshalException);
      Throwable fileNotFoundCause = cause.getCause();
      Assert.assertTrue(fileNotFoundCause instanceof FileNotFoundException);
    } finally {
      LQMGTest.deleteFolder(testDirFile);
    }
  }

  /**
   * Testing the normal usage of the LQMG module.
   */
  @Test
  public void _03_testLQMGGlobalConfiguration() {
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

    props.setPackages(new String[] { "org.everit.osgi.dev.lqmg.test.q2" });

    try {
      File configFile = new File(testDirFile, "config.xml");
      URL globalConfigURL = this.getClass().getResource("/META-INF/global.1.lqmg.xml");
      FileUtils.copyURLToFile(globalConfigURL, configFile);
      props.setConfigurationPath(configFile.getAbsolutePath());
      LQMG.generate(props);
      // TODO check if generated classes are ok.
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      LQMGTest.deleteFolder(testDirFile);
    }
  }

  /**
   * Testing the normal usage of the LQMG module.
   */
  @Test
  public void _04_testConfigInBundles() {
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

    GenerationProperties props = new GenerationProperties("simpleConfig", new String[] {
        "reference:" + bundle2URL.toExternalForm(),
        "reference:" + bundle1URL.toExternalForm() }, tempFolderName);
    props.setDefaultSchema("org.test");

    props.setPackages(new String[] { "org.everit.osgi.dev.lqmg.test.q2" });

    try {
      File configFile = new File(testDirFile, "config.xml");
      URL globalConfigURL = this.getClass().getResource("/META-INF/global.2.lqmg.xml");
      FileUtils.copyURLToFile(globalConfigURL, configFile);
      props.setConfigurationPath(configFile.getAbsolutePath());
      LQMG.generate(props);
      // TODO check if generated classes are ok.
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      LQMGTest.deleteFolder(testDirFile);
    }
  }

  /**
   * Testing the normal usage of the LQMG module.
   */
  @Test
  public void _05_testCommandLine() {
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

    String bundleLocations = "reference:" + bundle2URL.toExternalForm() + ";" + "reference:"
        + bundle1URL.toExternalForm();

    try {
      File configFile = new File(testDirFile, "config.xml");
      URL globalConfigURL = this.getClass().getResource("/META-INF/global.2.lqmg.xml");
      FileUtils.copyURLToFile(globalConfigURL, configFile);
      LQMGMain.main(new String[] { "-cp", "simpleConfig", "-b", bundleLocations, "-c",
          configFile.getAbsolutePath(), "-p", "org.everit.osgi.dev.lqmg.test.q2", "-o",
          tempFolderName });
      // TODO check if generated classes are ok.
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      LQMGTest.deleteFolder(testDirFile);
    }
  }

  /**
   * Test three cases: - when there are no matching capability for the given schema /n - when there
   * are multiple capabilities in a bundle matching the given schema /n - when found multiple
   * bundles containing matching capabilities for the given schema
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

}
