package org.everit.osgi.dev.lqmg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.framework.internal.core.AbstractBundle;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

public class EquinoxHackUtilImpl implements HackUtil {

    private static final Logger LOGGER = Logger.getLogger(EquinoxHackUtilImpl.class.getName());

    @Override
    public void hackBundles(Framework osgiContainer, File tempDirectory) {
        BundleContext systemBundleContext = osgiContainer.getBundleContext();

        ServiceReference<PlatformAdmin> platformServiceSR = systemBundleContext
                .getServiceReference(PlatformAdmin.class);

        PlatformAdmin platformAdmin = systemBundleContext.getService(platformServiceSR);
        State state = platformAdmin.getState();

        Bundle[] bundles = systemBundleContext.getBundles();
        List<BundleCapability> availableCapabilities = getAllCapabilities(bundles, state);
        for (Bundle bundle : bundles) {
            if (bundle.getState() == Bundle.INSTALLED) {
                BundleDescription bundleDescription = state.getBundle(bundle.getBundleId());
                hackBundle(bundle, bundleDescription, availableCapabilities);
            }
        }
    }

    private <V> String convertClauseFieldsToString(Map<String, V> map, boolean directives) {
        if (map.size() == 0)
            return "";
        String assignment = directives ? ":=" : "=";
        Set<Entry<String, V>> set = map.entrySet();
        StringBuilder sb = new StringBuilder();
        for (Entry<String, V> entry : set) {
            sb.append(";");
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) value;
                if (list.size() == 0) {
                    continue;
                }
                sb.append(key).append(assignment).append('"');
                for (Object object : list)
                    sb.append(escapeClauseValue(object)).append(',');
                sb.setLength(sb.length() - 1);
                sb.append('"');
            } else {
                sb.append(key).append(assignment).append('"').append(escapeClauseValue(value)).append('"');
            }
        }
        return sb.toString();
    }

    private String escapeClauseValue(Object object) {
        String stringValue = String.valueOf(object);
        return stringValue.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void hackBundle(Bundle bundle, BundleDescription bundleDescription,
            List<BundleCapability> availableCapabilities) {
        Manifest manifest = createHackedManifest(bundle, bundleDescription, availableCapabilities);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            JarOutputStream jarOut = new JarOutputStream(bout, manifest);
            AbstractBundle abstractBundle = (AbstractBundle) bundle;
            BaseData bundleData = (BaseData) abstractBundle.getBundleData();
            BundleFile bundleFile = bundleData.getBundleFile();
            BaseAdaptor adaptor = (BaseAdaptor) abstractBundle.getFramework().getAdaptor();

            List<String> entries = adaptor.listEntryPaths(Arrays.asList(new BundleFile[] { bundleFile }), "/", null,
                    BundleWiring.FINDENTRIES_RECURSE);

            for (String entry : entries) {
                if (!entry.equals("META-INF/MANIFEST.MF") && !entry.endsWith("/")) {
                    copyBundleEntryIntoJar(bundle, entry, jarOut);
                }
            }
            jarOut.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            bundle.update(new ByteArrayInputStream(bout.toByteArray()));
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }
    }

    private String createClauseString(String namespace, Map<String, Object> attributeMap,
            Map<String, String> directiveMap) {
        String attributesPart = convertClauseFieldsToString(attributeMap, false);
        String directivesPart = convertClauseFieldsToString(directiveMap, true);
        StringBuilder sb = new StringBuilder(namespace);
        if (!"".equals(attributesPart)) {
            sb.append(attributesPart);
        }
        if (!"".equals(directivesPart)) {
            sb.append(directivesPart);
        }
        return sb.toString();
    }

    private Manifest readOriginalManifest(Bundle bundle) {
        URL manifestURL = bundle.getResource("/META-INF/MANIFEST.MF");
        InputStream manifestStream = null;
        try {
            manifestStream = manifestURL.openStream();
            return new Manifest(manifestStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (manifestStream != null) {
                try {
                    manifestStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void copyBundleEntryIntoJar(Bundle bundle, String entry, JarOutputStream jarOut) throws IOException {
        jarOut.putNextEntry(new ZipEntry(entry));
        URL resource = bundle.getResource(entry);
        InputStream in = resource.openStream();
        try {
            byte[] buf = new byte[1024];
            int r = in.read(buf);
            while (r > -1) {
                jarOut.write(buf, 0, r);
                r = in.read(buf);
            }
        } finally {
            in.close();
        }
    }

    private Manifest createHackedManifest(Bundle bundle, BundleDescription bundleDescription,
            List<BundleCapability> availableCapabilities) {
        List<BundleRequirement> declaredRequirements = bundleDescription.getDeclaredRequirements(null);

        Manifest manifest = readOriginalManifest(bundle);
        Attributes mainAttributes = manifest.getMainAttributes();

        hackImportPackageManifestHeader(bundleDescription, availableCapabilities, mainAttributes);
        hackRequireBundleManifestHeader(bundleDescription, availableCapabilities, mainAttributes);

        StringBuilder sb = new StringBuilder();
        for (BundleRequirement declaredRequirement : declaredRequirements) {
            String namespace = declaredRequirement.getNamespace();
            if (namespace.equals(BundleRevision.PACKAGE_NAMESPACE) || namespace.equals(BundleRevision.HOST_NAMESPACE)
                    || namespace.equals(BundleRevision.BUNDLE_NAMESPACE)) {
                continue;
            }
            Map<String, String> directives = declaredRequirement.getDirectives();
            Map<String, Object> attributes = declaredRequirement.getAttributes();

            boolean optional = Constants.RESOLUTION_OPTIONAL.equals(declaredRequirement
                    .getDirectives().get(Constants.RESOLUTION_DIRECTIVE));
            if (!optional && !requirementSatisfiable(declaredRequirement, availableCapabilities)) {
                LOGGER.info("[HACK]: Making Require-Capability optional in bundle " + bundleDescription.toString()
                        + ": " + declaredRequirement.toString());
                directives = new HashMap<String, String>(directives);
                directives.put(Constants.RESOLUTION_DIRECTIVE, Constants.RESOLUTION_OPTIONAL);
            }
            String clauseString = createClauseString(namespace, attributes, directives);
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(clauseString);
        }
        if (sb.length() > 0) {
            mainAttributes.putValue(Constants.REQUIRE_CAPABILITY, sb.toString());
        }
        return manifest;
    }

    private void hackImportPackageManifestHeader(BundleDescription bundleDescription,
            List<BundleCapability> availableCapabilities, Attributes mainAttributes) {

        StringBuilder hackedImportPackageSB = new StringBuilder();

        ImportPackageSpecification[] allImports = bundleDescription.getImportPackages();

        for (ImportPackageSpecification importPackage : allImports) {
            if (hackedImportPackageSB.length() > 0) {
                hackedImportPackageSB.append(",");
            }
            hackedImportPackageSB.append(importPackage.getName()).append(";version=\"")
                    .append(importPackage.getVersionRange()).append("\"");

            boolean optional = Constants.RESOLUTION_OPTIONAL.equals(importPackage
                    .getDirective(Constants.RESOLUTION_DIRECTIVE));
            if (optional || !requirementSatisfiable(importPackage.getRequirement(), availableCapabilities)) {
                if (!optional) {
                    LOGGER.info("[HACK]: Making Import-Package optional in bundle " + bundleDescription.toString()
                            + ": " + hackedImportPackageSB.toString());
                }
                hackedImportPackageSB.append(";").append(Constants.RESOLUTION_DIRECTIVE).append(":=").append("\"")
                        .append(Constants.RESOLUTION_OPTIONAL).append("\"");
            }

        }

        if (hackedImportPackageSB.length() > 0) {
            mainAttributes.putValue(Constants.IMPORT_PACKAGE, hackedImportPackageSB.toString());
        }
    }

    private void hackRequireBundleManifestHeader(BundleDescription bundleDescription,
            List<BundleCapability> availableCapabilities, Attributes mainAttributes) {

        StringBuilder hackedRequireBundleSB = new StringBuilder();

        BundleSpecification[] requiredBundles = bundleDescription.getRequiredBundles();

        for (BundleSpecification requiredBundle : requiredBundles) {
            if (hackedRequireBundleSB.length() > 0) {
                hackedRequireBundleSB.append(",");
            }
            hackedRequireBundleSB.append(requiredBundle.getName() + ";bundle-version=\""
                    + requiredBundle.getVersionRange() + "\"");

            if (requiredBundle.isOptional()
                    || !requirementSatisfiable(requiredBundle.getRequirement(), availableCapabilities)) {
                if (!requiredBundle.isOptional()) {
                    LOGGER.info("[HACK]: Making Require-Bundle optional in bundle " + bundleDescription.toString()
                            + ": " + hackedRequireBundleSB.toString());
                }
                hackedRequireBundleSB.append(";").append(Constants.RESOLUTION_DIRECTIVE).append(":=").append("\"")
                        .append(Constants.RESOLUTION_OPTIONAL).append("\"");
            }
        }

        if (hackedRequireBundleSB.length() > 0) {
            mainAttributes.putValue(Constants.REQUIRE_BUNDLE, hackedRequireBundleSB.toString());
        }
    }

    private boolean requirementSatisfiable(BundleRequirement requirement, List<BundleCapability> availableCapabilities) {
        for (BundleCapability bundleCapability : availableCapabilities) {
            try {
                if (requirement.matches(bundleCapability)) {
                    return true;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private List<BundleCapability> getAllCapabilities(Bundle[] bundles, State state) {
        List<BundleCapability> availableCapabilities = new ArrayList<BundleCapability>();
        for (Bundle bundle : bundles) {
            BundleDescription bundleDescription = state.getBundle(bundle.getBundleId());
            List<BundleCapability> declaredCapabilities = bundleDescription.getDeclaredCapabilities(null);
            availableCapabilities.addAll(declaredCapabilities);
        }
        return availableCapabilities;
    }
}
