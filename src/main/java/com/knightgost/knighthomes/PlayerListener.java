package com.knightgost.knighthomes;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class PlayerListener implements Listener {

    private final UpdateChecker updateChecker;

    public PlayerListener(UpdateChecker updateChecker) {
        this.updateChecker = updateChecker;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (player.isOp()) {
            updateChecker.notifyPlayer(player);
        }
    }
}
