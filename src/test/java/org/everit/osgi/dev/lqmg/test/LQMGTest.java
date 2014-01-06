package org.everit.osgi.dev.lqmg.test;

import java.io.File;
import java.net.URL;
import java.util.UUID;

import org.everit.db.lqmg.GenerationProperties;
import org.everit.db.lqmg.LQMG;
import org.junit.Assert;
import org.junit.Test;

public class LQMGTest {

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
        try {
        LQMG.generate(props);
        } finally {
            deleteFolder(testDirFile);
        }
    }
    
    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
