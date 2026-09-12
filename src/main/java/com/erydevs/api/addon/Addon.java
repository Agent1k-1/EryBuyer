package com.erydevs.api.addon;

import org.jetbrains.annotations.NotNull;

public interface Addon {

    @NotNull
    String getName();

    @NotNull
    String getVersion();
}
