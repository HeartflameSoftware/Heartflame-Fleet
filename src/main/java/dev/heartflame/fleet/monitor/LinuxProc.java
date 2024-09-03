package dev.heartflame.fleet.monitor;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public enum LinuxProc {

    /**
     * Returns information about the system network usage.
     */
    NETWORK("/proc/net/dev"),
    
    MEMORY("/proc/meminfo");

    private final Path path;

    LinuxProc(String path) {
        this.path = resolvePath(path);
    }

    private static @Nullable Path resolvePath(String path) {
        try {
            Path p = Paths.get(path);
            if (Files.isReadable(p)) {
                return p;
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

        return null;
    }

    public @NonNull List<String> readFile() {
        if (this.path != null) {
            try {
                return Files.readAllLines(this.path, StandardCharsets.UTF_8);
            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        return Collections.emptyList();
    }

}
