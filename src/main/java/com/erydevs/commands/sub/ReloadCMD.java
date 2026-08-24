package com.erydevs.commands.sub;

import com.erydevs.EryBuyer;
import com.erydevs.commands.AdbuyerCommand;
import com.erydevs.commands.subcommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReloadCMD implements subcommand {

    private final EryBuyer plugin;
    private final AdbuyerCommand root;

    public ReloadCMD(@NotNull EryBuyer plugin, @NotNull AdbuyerCommand root) {
        this.plugin = plugin;
        this.root = root;
    }

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public @NotNull String getPermission() {
        return "erybuyer.adbuyer";
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String[] args) {
        plugin.getConfigManager().reloadConfig();
        plugin.getMessagesConfig().reloadMessages();
        plugin.getBoosterManager().reload();
        plugin.getBestManager().reload();
        plugin.getBuyerGUI().reloadMenus();
        plugin.getAutoBuyerManager().reload();

        root.send(player, plugin.getMessagesConfig().getMessageConfigReload());
    }
}
