package com.erydevs.addon.loader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

public class JarLoader extends URLClassLoader {

    public JarLoader(@NotNull URL[] urls, @NotNull ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    @Nullable
    public URL getResource(@NotNull String name) {
        URL own = findResource(name);
        return own != null ? own : super.getResource(name);
    }

    @Override
    @Nullable
    public InputStream getResourceAsStream(@NotNull String name) {
        URL own = findResource(name);
        if (own != null) {
            try {
                return own.openStream();
            } catch (IOException ignored) {
            }
        }
        return super.getResourceAsStream(name);
    }
}
