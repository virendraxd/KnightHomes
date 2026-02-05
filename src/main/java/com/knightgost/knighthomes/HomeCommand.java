package com.knightgost.knighthomes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HomeCommand implements CommandExecutor {
    public static Map<UUID, Integer> deletingHomeSlot = new HashMap<>();
    private final HomeGUI gui;
    private final JavaPlugin plugin;

    public HomeCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gui = new HomeGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /home - must be player
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // /home reload
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("home.reload")) {
                sender.sendMessage("§cYou don't have permission to reload the config.");
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage("§aKnightHomes configuration reloaded successfully.");
            return true;
        }


        // Open home GUI
        gui.openHomeGui(player);
        return true;
    }
}