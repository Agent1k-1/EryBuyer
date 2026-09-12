package com.erydevs.addon;

import com.erydevs.EryBuyer;
import org.jetbrains.annotations.NotNull;

public interface Addon extends com.erydevs.api.addon.Addon {

    void onEnable(@NotNull EryBuyer plugin);

    void onDisable();
}
