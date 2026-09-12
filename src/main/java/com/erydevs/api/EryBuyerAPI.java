package com.erydevs.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EryBuyerAPI {

    private static BuyerAPI instance;

    private EryBuyerAPI() {
    }

    @NotNull
    public static BuyerAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("EryBuyer API ещё не зарегистрировано");
        }
        return instance;
    }

    public static void register(@NotNull BuyerAPI api) {
        instance = api;
    }

    public static void unregister() {
        instance = null;
    }

    @Nullable
    public static BuyerAPI getInstanceOrNull() {
        return instance;
    }
}
