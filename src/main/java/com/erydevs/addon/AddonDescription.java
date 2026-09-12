package com.erydevs.addon;

import org.jetbrains.annotations.NotNull;

public class AddonDescription {

    private final String name;
    private final String main;
    private final String version;

    public AddonDescription(@NotNull String name, @NotNull String main, @NotNull String version) {
        this.name = name;
        this.main = main;
        this.version = version;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getMain() {
        return main;
    }

    @NotNull
    public String getVersion() {
        return version;
    }
}
