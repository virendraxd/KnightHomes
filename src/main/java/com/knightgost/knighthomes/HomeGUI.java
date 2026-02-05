package com.knightgost.knighthomes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
//boolean slotUnlocked
public class HomeGUI implements Listener {
    public static Map<UUID, Integer> deletingHomeSlot = new HashMap<>();
    public static final Map<UUID, BukkitTask> activeDeleteTasks = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> activeDeleteSlots = new ConcurrentHashMap<>();

    private final HomeManager homeManager;
    private final JavaPlugin plugin;

    public HomeGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        this.homeManager = new HomeManager(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static int getMaxHomesAllowed(Player player) {
        for (int i = 5; i >= 1; i--) {
            if (player.hasPermission("knighthomes.home.limit." + i)) {
                return i;
            }
        }

        int defaultLimit = KnightHomes.getInstance()
                .getConfig()
                .getInt("default-home-limit", 1);

        return Math.max(1, Math.min(defaultLimit, 5));
    }

    public void openHomeGui(Player player) {

        Inventory gui = Bukkit.createInventory(player, 45, Component.text("§6§lKnightHomes"));

        // ─── Player Head (slot 4) ───
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(player);
        headMeta.displayName(
            MessageUtils.getColoredIconMessage(plugin,"player", "&e" + player.getName())
        );

        int ownedHomes = 0;
        int maxHomes = getMaxHomesAllowed(player);

        for (int i = 0; i < maxHomes; i++) {
            if (HomeManager.loadHome(player.getUniqueId(), i) != null) ownedHomes++;
        }

        headMeta.lore(List.of(
            MessageUtils.getColoredIconMessage(plugin,"homes_owned", "§aʜᴏᴍᴇꜱ ᴏᴡɴᴇᴅ: §b" + ownedHomes + "/" + maxHomes)
        ));
        head.setItemMeta(headMeta);
        gui.setItem(4, head);

        // ─── Personal Homes ─── 
        int[] homeSlots = {20, 21, 22, 23, 24};
        int deleteSlot = 29;

        for (int i = 0; i < 5; i++) {
            Location home = HomeManager.loadHome(player.getUniqueId(), i);

            boolean slotUnlocked = i < maxHomes;

            // Bed Item to represent home
            ItemStack bed = new ItemStack(home != null ? Material.LIME_BED :
                                        slotUnlocked ? Material.GRAY_BED : Material.RED_BED);
            ItemMeta bedMeta = bed.getItemMeta();
            bedMeta.displayName(
                home != null
                    ? MessageUtils.getColoredIconMessage(plugin, "home_slot", "§aʜᴏᴍᴇ " + (i + 1))
                    : slotUnlocked
                    ? MessageUtils.getColoredIconMessage(plugin, "empty_slot", "§7ᴇᴍᴘᴛʏ ꜱʟᴏᴛ " + (i + 1))
                    : MessageUtils.getColoredIconMessage(plugin, "locked_slot", "§cʟᴏᴄᴋᴇᴅ ꜱʟᴏᴛ")
            );
            bedMeta.lore(List.of(
                Component.text(
                    home != null ? "§7Left-click to §ateleport" :
                    slotUnlocked ? "§7No home set." : "§cMax homes reached."),
                Component.text("§8Slot: §f#" + (i + 1))
            ));
            bed.setItemMeta(bedMeta);
            gui.setItem(homeSlots[i], bed);

            // Dye Items or redstone for delete
            ItemStack dye = new ItemStack(home != null ? Material.RED_DYE :
                                        slotUnlocked ? Material.GRAY_DYE : Material.REDSTONE);
            ItemMeta dyeMeta = dye.getItemMeta();
            dyeMeta.displayName(
                home != null
                    ? MessageUtils.getColoredIconMessage(plugin, "delete_home", "§cᴅᴇʟᴇᴛᴇ ʜᴏᴍᴇ " + (i + 1))
                    : slotUnlocked
                    ? MessageUtils.getColoredIconMessage(plugin, "set_home", "§7ꜱᴇᴛ ʜᴏᴍᴇ " + (i + 1))
                    : MessageUtils.getColoredIconMessage(plugin, "locked_slot", "§cꜱʟᴏᴛ ʟᴏᴄᴋᴇᴅ")
            );
            dyeMeta.lore(List.of(
                Component.text(
                    home != null ? "§7Right-click to §cdelete home" :
                    slotUnlocked ? "§7Right-click to §aset home here" : "§cYou can’t set more homes.")
            ));
            dye.setItemMeta(dyeMeta);
            gui.setItem(deleteSlot++, dye);
        }
        player.openInventory(gui);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player player)) return;

        String rawTitle = Component.text("§6§lKnightHomes").toString();
        if (!event.getView().title().toString().equals(rawTitle)) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        ClickType click = event.getClick();
        UUID uuid = player.getUniqueId();

        // Personal home slots (20–24) 
        if (slot >= 20 && slot <= 24) {
            int homeSlot = slot - 20;
            Location home = HomeManager.loadHome(uuid, homeSlot);

            if (click.isLeftClick() && home != null) {
                player.closeInventory();
                boolean combatBlockEnabled = plugin.getConfig().getBoolean("combat-teleport-block");
                boolean inCombat = CombatManager.isInCombat(player.getUniqueId());
                        
                // Only block teleport and show message if combat-teleport-block is true
                if (combatBlockEnabled && inCombat) {
                    player.sendMessage(
                        MessageUtils.getColoredIconMessage(plugin, "combat_teleport_blocked", "§cYou cannot teleport while in combat!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_HURT, 1f, 1f);
                    return;
                }
            
                // If combat-teleport-block is false, just teleport normally even if in combat
                Component component = MessageUtils.getColoredIconMessage(plugin, "home_teleport_success", "§aTeleported to your Home " + (homeSlot + 1) + ".");
                String message = LegacyComponentSerializer.legacySection().serialize(component);
                TeleportUtils.teleportWithCountdown(player, home, message, plugin);
            }
            
        }

        if (slot >= 29 && slot <= 33 && click.isRightClick()) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.hasItemMeta()) {
                ItemMeta meta = clicked.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    List<Component> lore = meta.lore();
                    if (lore != null && !lore.isEmpty() &&
                        lore.get(0).equals(Component.text("§7Click again to §6§lCONFIRM"))) {

                        // Confirm delete
                        int homeSlot = slot - 29;
                        HomeManager.deleteHome(uuid, homeSlot);
                        player.sendMessage(
                            MessageUtils.getColoredIconMessage(plugin, "home_deleted", "§cDeleted Home " + (homeSlot + 1) + "."));
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                        player.closeInventory();

                        activeDeleteTasks.remove(uuid);
                        activeDeleteSlots.remove(uuid);
                        return;
                    }
                }
            }
        }
    
        // Handle delete (red dye) or set (gray dye)
        if (slot >= 29 && slot <= 33 && click.isRightClick()) {
            final int homeSlot = slot - 29;
            Location home = HomeManager.loadHome(uuid, homeSlot);

            if (home != null) {
                // Cancel any previous delete countdown
                if (activeDeleteTasks.containsKey(uuid)) {
                    activeDeleteTasks.get(uuid).cancel();

                    // Reset previous button (if same inventory still open)
                    Integer prevSlot = activeDeleteSlots.get(uuid);
                    if (prevSlot != null && player.getOpenInventory().getTopInventory().equals(event.getInventory())) {
                        Location prevHome = HomeManager.loadHome(uuid, prevSlot - 29);
                        if (prevHome != null) {
                            ItemStack resetDye = new ItemStack(Material.RED_DYE);
                            ItemMeta resetMeta = resetDye.getItemMeta();
                            resetMeta.displayName(MessageUtils.getColoredIconMessage(plugin, "delete_home", "§cᴅᴇʟᴇᴛᴇ ʜᴏᴍᴇ " + (prevSlot - 28)));
                            resetMeta.lore(List.of(Component.text("§7Right-click to §cdelete home")));
                            resetDye.setItemMeta(resetMeta);
                            event.getInventory().setItem(prevSlot, resetDye);
                        }
                    }
                }

                // Start new countdown
                BukkitTask task = new BukkitRunnable() {
                    int secondsLeft = 5;

                    @Override
                    public void run() {
                        if (!player.isOnline() || !player.getOpenInventory().getTopInventory().equals(event.getInventory())) {
                            cancel();
                            return;
                        }

                        Inventory topInv = player.getOpenInventory().getTopInventory();

                        ItemStack dye = new ItemStack(Material.RED_DYE);
                        ItemMeta meta = dye.getItemMeta();

                        if (secondsLeft == 0) {
                            meta.displayName(MessageUtils.getColoredIconMessage(plugin, "delete_home", "§cᴅᴇʟᴇᴛᴇ ʜᴏᴍᴇ " + (homeSlot + 1)));
                            meta.lore(List.of(Component.text("§7Click again to §6§lCONFIRM")));
                            dye.setItemMeta(meta);
                            topInv.setItem(slot, dye);
                            cancel();
                            return;
                        }

                        meta.displayName(MessageUtils.getColoredIconMessage(plugin, "delete_home", "§cᴅᴇʟᴇᴛᴇ ʜᴏᴍᴇ " + (homeSlot + 1)));
                        meta.lore(List.of(Component.text("§7Confirming in §f" + secondsLeft + "s")));
                        dye.setItemMeta(meta);
                        topInv.setItem(slot, dye);
                        secondsLeft--;
                        player.playSound(player.getLocation(), Sound.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON, 1.0f, 1.0f);
                    }
                }.runTaskTimer(JavaPlugin.getProvidingPlugin(getClass()), 0L, 20L);

                activeDeleteTasks.put(uuid, task);
                activeDeleteSlots.put(uuid, slot);
                return;
            }else {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() != Material.GRAY_DYE) {
                    player.sendMessage("§cYou can’t set a home in a locked slot.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    return;
                }

                int maxHomes = HomeGUI.getMaxHomesAllowed(player);
                // homeSlot is 0-based (0–4), maxHomes is 1–5
                if (homeSlot  >= maxHomes) {
                    player.sendMessage("§cYou have reached the maximum number of homes.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    return;
                }

                homeManager.saveHome(uuid, homeSlot, player.getLocation());
                player.sendMessage(
                    MessageUtils.getColoredIconMessage(plugin, "set_home_confirmed", "§aSet Home at Slot " + (homeSlot + 1) + "."));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        if (!(human instanceof Player player)) return;

        String rawTitle = Component.text("Homes (Max 5)").toString();
        if (!event.getView().title().toString().equals(rawTitle)) return;

        HomeCommand.deletingHomeSlot.remove(player.getUniqueId());
    }

    public ItemStack buildHomeItem(int slot, UUID uuid) {
        Location home = HomeManager.loadHome(uuid, slot);
        if (home == null) return null;

        ItemStack item = new ItemStack(Material.GREEN_BED); 
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§aHome " + (slot + 1)));
            meta.lore(List.of(
                Component.text("§7Click to teleport"),
                Component.text("§eRight-click to delete")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
}
