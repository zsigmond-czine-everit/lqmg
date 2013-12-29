package org.everit.db.lqmg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import liquibase.resource.ResourceAccessor;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

/**
 * A simple resource accessor based on OSGi bundles. It is possible to switch the actual bundle from time to time which
 * the ResourceAccessor should work with.
 */
public class OSGiResourceAccessor implements ResourceAccessor {

    private Bundle bundle;

    public OSGiResourceAccessor(Bundle bundle) {
        if (bundle == null) {
            throw new IllegalArgumentException("The parameter bundle cannot be null.");
        }
        this.bundle = bundle;
    }

    @Override
    public InputStream getResourceAsStream(String file) throws IOException {
        URL resource = bundle.getResource(file);
        if (resource == null) {
            return null;
        }
        return resource.openStream();
    }

    @Override
    public Enumeration<URL> getResources(String packageName) throws IOException {
        return bundle.findEntries(packageName, "*", false);
    }

    @Override
    public ClassLoader toClassLoader() {
        return bundle.adapt(BundleWiring.class).getClassLoader();
    }

    /**
     * Switching the current bundle where resources are searched in.
     * 
     * @param bundle
     *            The new bundle.
     */
    public void switchBundle(Bundle bundle) {
        if (bundle == null) {
            throw new IllegalArgumentException("The parameter bundle cannot be null");
        }
        this.bundle = bundle;
    }
}
