package com.candydeathnotes.utils;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyManager {

    private final JavaPlugin plugin;
    private Economy vaultEconomy;
    private PlayerPointsAPI playerPointsAPI;
    private String economySystem;

    public EconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadEconomySystem();
    }

    private void loadEconomySystem() {
        FileConfiguration config = plugin.getConfig();
        this.economySystem = config.getString("economy-system", "Vault").toLowerCase();

        if (economySystem.equals("vault")) {
            if (!setupVault()) {
                plugin.getLogger().severe("[DeathNotes] Vault ekonomi sistemi yüklenemedi! PlayerPoints kullanılacak.");
                economySystem = "playerpoints";
                setupPlayerPoints();
            }
        } else if (economySystem.equals("playerpoints")) {
            if (!setupPlayerPoints()) {
                plugin.getLogger().severe("[DeathNotes] PlayerPoints API bulunamadı! Vault kullanılacak.");
                economySystem = "vault";
                setupVault();
            }
        } else {
            plugin.getLogger().severe("[DeathNotes] Geçersiz ekonomi sistemi! Varsayılan olarak Vault kullanılacak.");
            economySystem = "vault";
            setupVault();
        }
    }

    private boolean setupVault() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            vaultEconomy = rsp.getProvider();
            return true;
        }
        return false;
    }

    private boolean setupPlayerPoints() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlayerPoints")) {
            playerPointsAPI = PlayerPoints.getInstance().getAPI();
            return true;
        }
        return false;
    }

    public boolean hasEnoughMoney(Player player, int amount) {
        if (economySystem.equals("vault")) {
            return vaultEconomy != null && vaultEconomy.getBalance(player) >= amount;
        } else if (economySystem.equals("playerpoints")) {
            return playerPointsAPI != null && playerPointsAPI.look(player.getUniqueId()) >= amount;
        }
        return false;
    }

    public boolean withdrawMoney(Player player, int amount) {
        if (economySystem.equals("vault")) {
            if (vaultEconomy != null) {
                vaultEconomy.withdrawPlayer(player, amount);
                return true;
            }
        } else if (economySystem.equals("playerpoints")) {
            if (playerPointsAPI != null) {
                playerPointsAPI.take(player.getUniqueId(), amount);
                
                // Veriyi kaydetme işlemine de burada yer verebiliriz.
                // Bu işlem PlayerPoints ile ilgili bir değişiklik yaptıktan sonra veri kaydetmelidir.
                return true;
            }
        }
        return false;
    }
}
