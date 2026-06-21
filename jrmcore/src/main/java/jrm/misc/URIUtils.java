package jrm.misc;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

/**
 * Utility class providing helper methods to resolve and manipulate {@link URI} instances and file paths, checking existence, and
 * reading file contents.
 * 
 * @author optyfr
 */
public @UtilityClass class URIUtils {
    /**
     * Checks if the resource corresponding to the specified path string exists. If resolving the path as a URI fails, it falls back
     * to standard file system checks.
     * 
     * @param path the path string to check
     * 
     * @return {@code true} if the resource exists, {@code false} otherwise
     */
    public static boolean URIExists(String path) // NOSONAR
    {
        try {
            Path p = getPath(path);
            return Files.exists(p);
        } catch (Exception _) {
            return Files.exists(Paths.get(path));
        }
    }

    /**
     * Checks if the resource corresponding to the specified {@link URI} exists on the file system.
     * 
     * @param uri the URI of the resource to check
     * 
     * @return {@code true} if the resource exists, {@code false} otherwise
     */
    public static boolean URIExists(URI uri) // NOSONAR
    {
        try {
            return Files.exists(getPath(uri));
        } catch (Exception _) {
            return false;
        }
    }

    /**
     * Resolves a path string into a {@link Path} object. If the string can be parsed as a URI, resolves it as a URI path;
     * otherwise, falls back to standard path resolution.
     * 
     * @param path the path string to resolve
     * 
     * @return the resolved {@link Path} instance
     */
    public static Path getPath(String path) {
        try {
            var uri = URI.create(path);
            return Path.of(uri);
        } catch (Exception _) {
            return Paths.get(path);
        }

    }

    /**
     * Resolves the specified {@link URI} into a {@link Path} object.
     * 
     * @param uri the URI to resolve
     * 
     * @return the resolved {@link Path} instance
     */
    public static Path getPath(URI uri) {
        return Path.of(uri);
    }

    /**
     * Reads the entire content of a file specified by a path string into a single string using UTF-8 encoding.
     * 
     * @param path the path string to read from
     * 
     * @return the string content of the resource
     * 
     * @throws IOException if any I/O error occurs while reading the resource
     */
    public static String readString(String path) throws IOException {
        try (final var reader = Files.newBufferedReader(getPath(path), StandardCharsets.UTF_8)) {
            return reader.lines().collect(Collectors.joining());
        }
    }

}
