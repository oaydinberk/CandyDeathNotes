package com.candydeathnotes.listeners;

import com.candydeathnotes.utils.ConfigManager;
import com.candydeathnotes.utils.PlayerDataManager;
import com.candydeathnotes.Main;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathNoteListener implements Listener {

    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final Main plugin;

    public DeathNoteListener(PlayerDataManager playerDataManager, ConfigManager configManager, Main plugin) {
        this.playerDataManager = playerDataManager;
        this.configManager = configManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String activeMessageId = playerDataManager.getActiveMessage(player);

        if (activeMessageId == null || activeMessageId.equals("none")) {
            return;
        }

        String deathMessage = configManager.getMessagesConfig().getString("messages." + activeMessageId + ".text");
        if (deathMessage == null) {
            return;
        }

        Location deathLocation = player.getLocation().add(0, 1, 0);
        Hologram hologram = HologramsAPI.createHologram(plugin, deathLocation);
        hologram.appendTextLine("§c" + deathMessage); // Hologram metni kırmızı olacak

        // Efektler
        deathLocation.getWorld().spawnParticle(Particle.SMOKE_NORMAL, deathLocation, 15, 0.3, 0.3, 0.3, 0.01);
        deathLocation.getWorld().spawnParticle(Particle.SPELL_WITCH, deathLocation, 10, 0.3, 0.7, 0.3, 0.01);

        // Öldüren oyuncuyu bul
        Player killer = player.getKiller();
        if (killer != null) {
            // Öldüren oyuncuya ses efekti çal
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                deathLocation.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, deathLocation, 8, 0.3, 0.3, 0.3, 0.05);
                hologram.delete();
            }
        }.runTaskLater(plugin, 100L); // 5 saniye sonra hologramı sil
    }
}
