package com.knightgost.knighthomes;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class KnightHomes extends JavaPlugin {

    private static KnightHomes instance;

    @Override
    public void onEnable() {
        getLogger().info("KnightHomes is now enabled!");

        reloadConfig();
        saveDefaultConfig();  // Copies config.yml from resources to plugin folder if not exists
        addMissingDefaults(); // Add missing defaults without overwriting existing values

        getLogger().info("KnightHomes loaded with configuration.");
        
        getCommand("home").setExecutor(new HomeCommand(this));
        
        instance = this;
        getServer().getPluginManager().registerEvents(new CombatListener(), this);
        getServer().getPluginManager().registerEvents(new TeleportUtils(), this);
    }

    public static KnightHomes getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        getLogger().info("KnightHomes is now disabled.");
    }

    private void addMissingDefaults() {
        FileConfiguration config = getConfig();

        // ICONS
        if (!config.isSet("icons.player")) config.set("icons.player", "⭐");
        if (!config.isSet("icons.homes_owned")) config.set("icons.homes_owned", "🏠");
        if (!config.isSet("icons.home_slot")) config.set("icons.home_slot", "▣");
        if (!config.isSet("icons.empty_slot")) config.set("icons.empty_slot", "🫙");
        if (!config.isSet("icons.locked_slot")) config.set("icons.locked_slot", "🔒");
        if (!config.isSet("icons.delete_home")) config.set("icons.delete_home", "❌");
        if (!config.isSet("icons.set_home")) config.set("icons.set_home", "➕");
        if (!config.isSet("icons.set_home_confirmed")) config.set("icons.set_home_confirmed", "✔");
        if (!config.isSet("icons.teleport_cancelled")) config.set("icons.teleport_cancelled", "✖");
        if (!config.isSet("icons.teleporting")) config.set("icons.teleporting", "✈");
        if (!config.isSet("icons.combat_teleport_blocked")) config.set("icons.combat_teleport_blocked", "⛔");
        if (!config.isSet("icons.home_teleport_success")) config.set("icons.home_teleport_success", "🌀");
        if (!config.isSet("icons.home_deleted")) config.set("icons.home_deleted", "🗑");
        if (!config.isSet("icons.out_of_combat")) config.set("icons.out_of_combat", "🛡");
        if (!config.isSet("icons.in_combat")) config.set("icons.in_combat", "⚔");
        if (!config.isSet("icons.teleport_cooldown")) config.set("icons.teleport_cooldown", "⛔");

        // COLORS
        if (!config.isSet("colors.player")) config.set("colors.player", "&6");
        if (!config.isSet("colors.homes_owned")) config.set("colors.homes_owned", "&f");
        if (!config.isSet("colors.home_slot")) config.set("colors.home_slot", "&a");
        if (!config.isSet("colors.empty_slot")) config.set("colors.empty_slot", "&7");
        if (!config.isSet("colors.locked_slot")) config.set("colors.locked_slot", "&4");
        if (!config.isSet("colors.delete_home")) config.set("colors.delete_home", "&4");
        if (!config.isSet("colors.set_home")) config.set("colors.set_home", "&e");
        if (!config.isSet("colors.set_home_confirmed")) config.set("colors.set_home_confirmed", "&e");
        if (!config.isSet("colors.teleport_cancelled")) config.set("colors.teleport_cancelled", "&4");
        if (!config.isSet("colors.teleporting")) config.set("colors.teleporting", "&f");
        if (!config.isSet("colors.combat_teleport_blocked")) config.set("colors.combat_teleport_blocked", "&4");
        if (!config.isSet("colors.home_teleport_success")) config.set("colors.home_teleport_success", "&2");
        if (!config.isSet("colors.home_deleted")) config.set("colors.home_deleted", "&4");
        if (!config.isSet("colors.out_of_combat")) config.set("colors.out_of_combat", "&2");
        if (!config.isSet("colors.in_combat")) config.set("colors.in_combat", "&4");
        if (!config.isSet("colors.teleport_cooldown")) config.set("colors.teleport_cooldown", "&4");

        // MISC
        if (!config.isSet("cooldown.enabled")) config.set("cooldown.enabled", true);
        if (!config.isSet("cooldown.time")) config.set("cooldown.time", 1);
        if (!config.isSet("cooldown.bypass-permission")) config.set("cooldown.bypass-permission", "knighthomes.cooldown.bypass");
        if (!config.isSet("combat-teleport-block")) config.set("combat-teleport-block", true);
        if (!config.isSet("default-home-limit")) config.set("default-home-limit", 1);

        saveConfig();
    }
}
