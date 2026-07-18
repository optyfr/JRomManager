package jrm.server.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jrm.aui.progress.ProgressHandler;
import jrm.misc.OffsetProvider;
import jrm.misc.ProfileSettingsEnum;
import jrm.profile.Profile;
import jrm.server.shared.datasources.XMLRequest;
import jrm.server.shared.datasources.XMLResponse;

/**
 * Shared test fixtures for loading real profile datasets and constructing XML datasource requests.
 */
public final class TestDataSets {

    /** Path to a small Logiqx datafile in {@code jrmcore} test resources. */
    private static final String A5200_DAT = "dats/MAME 0.288 Software List ROMs (merged)/a5200.xml";

    private TestDataSets() {
    }

    /**
     * Loads a real profile from the {@code a5200.xml} datafile shipped with {@code jrmcore} test resources.
     *
     * @param session the web session used for path resolution and caching
     * @return the loaded profile
     */
    public static Profile loadA5200Profile(final WebSession session) {
        final File file = resolveResource(A5200_DAT);
        final Profile profile = Profile.load(session, file, new NoopProgressHandler());
        assertThat(profile).isNotNull();
        return profile;
    }

    /**
     * Loads the {@code a5200.xml} profile and attaches the test {@code catver.ini} and {@code nplayers.ini} files.
     *
     * @param session the web session
     * @param workPath the workspace used to hold the copied INI files
     * @return the loaded profile with CatVer and NPlayers populated
     * @throws IOException if copying INI files fails
     */
    public static Profile loadProfileWithFilters(final WebSession session, final Path workPath) throws IOException {
        final Profile profile = loadA5200Profile(session);
        Files.createDirectories(workPath);
        final Path catver = workPath.resolve("catver.ini");
        final Path nplayers = workPath.resolve("nplayers.ini");
        Files.copy(resolveResource("ini/catver.ini").toPath(), catver, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(resolveResource("ini/nplayers.ini").toPath(), nplayers, StandardCopyOption.REPLACE_EXISTING);
        profile.setProperty(ProfileSettingsEnum.filter_catver_ini, catver.toAbsolutePath().toString());
        profile.setProperty(ProfileSettingsEnum.filter_nplayers_ini, nplayers.toAbsolutePath().toString());
        profile.loadCatVer(null);
        profile.loadNPlayers(null);
        return profile;
    }

    /**
     * Builds an {@link XMLRequest} for the given session from a raw XML string.
     *
     * @param session the web session
     * @param xml the request XML
     * @return the parsed request
     * @throws Exception if parsing fails
     */
    public static XMLRequest xmlRequest(final WebSession session, final String xml) throws Exception {
        return new XMLRequest(session, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), xml.length());
    }

    /**
     * Processes an {@link XMLResponse} and returns its XML output as a UTF-8 string.
     *
     * @param response the response to process
     * @return the response XML
     * @throws Exception if processing fails
     */
    public static String processResponse(final XMLResponse response) throws Exception {
        try (response) {
            try (final var tfis = response.processRequest()) {
                return new String(tfis.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * Resolves a resource path relative to {@code jrmcore/src/test/resources}.
     *
     * @param resource the relative resource path
     * @return the resolved file
     */
    public static File resolveResource(final String resource) {
        final Path base = Path.of(System.getProperty("JRomManager.rootPath", System.getProperty("user.dir")), "jrmcore/src/test/resources").toAbsolutePath().normalize();
        return base.resolve(resource).toFile();
    }

    /**
     * No-op {@link ProgressHandler} suitable for profile loading in unit tests.
     */
    public static final class NoopProgressHandler implements ProgressHandler {
        @Override
        public void setOptions(final Option first, final Option... rest) {
            // no-op
        }

        @Override
        public void setInfos(final int threadCnt, final Boolean multipleSubInfos) {
            // no-op
        }

        @Override
        public void clearInfos() {
            // no-op
        }

        @Override
        public void setProgress(final String msg, final Integer val, final Integer max, final String submsg) {
            // no-op
        }

        @Override
        public void setProgress2(final String msg, final Integer val, final Integer max) {
            // no-op
        }

        @Override
        public void setProgress3(final String msg, final Integer val, final Integer max) {
            // no-op
        }

        @Override
        public int getCurrent() {
            return 0;
        }

        @Override
        public int getCurrent2() {
            return 0;
        }

        @Override
        public int getCurrent3() {
            return 0;
        }

        @Override
        public boolean isCancel() {
            return false;
        }

        @Override
        public void doCancel() {
            // no-op
        }

        @Override
        public void canCancel(final boolean canCancel) {
            // no-op
        }

        @Override
        public boolean canCancel() {
            return false;
        }

        @Override
        public InputStream getInputStream(final InputStream in, final Integer len) {
            return in;
        }

        @Override
        public void close() {
            // no-op
        }

        @Override
        public void addError(final String error) {
            // no-op
        }

        @Override
        public void setOffsetProvider(final OffsetProvider offsetProvider) {
            // no-op
        }
    }
}
