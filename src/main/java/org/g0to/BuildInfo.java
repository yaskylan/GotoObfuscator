package org.g0to;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

public class BuildInfo {
    private static final BuildInfo INSTANCE = new BuildInfo();

    private final String version;
    private final String instant;

    private BuildInfo() {
        final InputStream is = BuildInfo.class.getResourceAsStream("/buildinfo");

        if (is == null) {
            this.version = "dev";
            this.instant = Instant.now().toString();
        } else {
            String version;
            String instant;

            try {
                final List<String> list = IOUtils.readLines(is, StandardCharsets.UTF_8);

                version = list.get(0);
                instant = list.get(1);
            } catch (UncheckedIOException e) {
                new Exception("Unable to read buildinfo", e).printStackTrace();

                version = "error";
                instant = Instant.now().toString();
            }

            this.version = version;
            this.instant = instant;
        }
    }

    @Override
    public String toString() {
        return "BuildInfo version: " + this.version + ", buildTime: " + this.instant;
    }

    public static BuildInfo getBuildInfo() {
        return INSTANCE;
    }
}
