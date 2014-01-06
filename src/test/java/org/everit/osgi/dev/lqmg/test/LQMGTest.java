package org.everit.osgi.dev.lqmg.test;

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
