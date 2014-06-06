package org.everit.osgi.dev.lqmg;

import java.io.File;

import org.osgi.framework.launch.Framework;

public interface HackUtil {

    void hackBundles(Framework osgiContainer, File tempDirectory);

}
