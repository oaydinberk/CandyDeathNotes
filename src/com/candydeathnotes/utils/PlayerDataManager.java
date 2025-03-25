package com.candydeathnotes.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerDataManager {

    private final ConfigManager configManager;
    private final EconomyManager economyManager;

    public PlayerDataManager(ConfigManager configManager, EconomyManager economyManager) {
        this.configManager = configManager;
        this.economyManager = economyManager;
    }

    // Oyuncunun sahip olduğu mesajları döndürür
    public Set<String> getOwnedMessages(Player player) {
        FileConfiguration playerData = configManager.getPlayerDataConfig();
        List<String> list = playerData.getStringList("players." + player.getName() + ".ownedMessages");
        return new HashSet<>(list);
    }

    // Oyuncunun belirli bir mesajı satın alıp almadığını kontrol eder
    public boolean hasPurchasedMessage(Player player, String messageId) {
        return getOwnedMessages(player).contains(messageId);
    }

    // Oyuncu mesaj satın alırsa işlemi gerçekleştirir
    public void purchaseMessage(Player player, String messageId, int price) {
        if (hasPurchasedMessage(player, messageId)) {
            player.sendMessage("§cBu mesajı zaten satın aldınız!");
            return;
        }

        if (economyManager.hasEnoughMoney(player, price)) {
            boolean success = economyManager.withdrawMoney(player, price);
            if (!success) {
                player.sendMessage("§cÖdeme başarısız oldu!");
                return;
            }

            FileConfiguration playerData = configManager.getPlayerDataConfig();
            Set<String> ownedMessages = getOwnedMessages(player);
            ownedMessages.add(messageId); // Mesajı ekle

            // Oyuncunun sahip olduğu mesajları kaydet
            playerData.set("players." + player.getName() + ".ownedMessages", new ArrayList<>(ownedMessages));

            // Yeni satın alınan mesajı varsayılan olarak aktif yap
            setActiveMessage(player, messageId);

            // **ÖNEMLİ: VERİLERİ KAYDETMEK İÇİN ÇAĞRI EKLENDİ**
            configManager.savePlayerData();
        }
    }

    // Oyuncunun aktif mesajını döndürür
    public String getActiveMessage(Player player) {
        FileConfiguration playerData = configManager.getPlayerDataConfig();
        return playerData.getString("players." + player.getName() + ".use", "none");
    }

    // Oyuncunun aktif mesajını değiştirir
    public void setActiveMessage(Player player, String messageId) {
        FileConfiguration playerData = configManager.getPlayerDataConfig();
        if (messageId == null) {
            playerData.set("players." + player.getName() + ".use", null);
            player.sendMessage("§eÖlüm mesajınız devre dışı bırakıldı.");
        } else {
            playerData.set("players." + player.getName() + ".use", messageId);
        }
        configManager.savePlayerData();
    }

    // Oyuncunun sahip olduğu mesajlara yeni bir mesaj ekler
    public void addOwnedMessage(Player player, String messageId) {
        FileConfiguration playerData = configManager.getPlayerDataConfig();
        Set<String> ownedMessages = getOwnedMessages(player);
        ownedMessages.add(messageId);

        playerData.set("players." + player.getName() + ".ownedMessages", new ArrayList<>(ownedMessages));
        configManager.savePlayerData();
    }

    // Oyuncunun aktif ölüm mesajını getirir
    public String getActiveDeathMessage(String playerName) {
        FileConfiguration playerData = configManager.getPlayerDataConfig();
        String activeMessageID = playerData.getString("players." + playerName + ".use", null);

        if (activeMessageID == null || activeMessageID.equals("none")) {
            return null; // Oyuncunun aktif bir mesajı yoksa null döndür
        }

        FileConfiguration messagesConfig = configManager.getMessagesConfig();
        return messagesConfig.getString("messages." + activeMessageID + ".text", null);
    }

    // Oyuncunun satın aldığı mesajı ekler
    public void addPurchasedMessage(Player player, String messageId) {
        FileConfiguration playerData = configManager.getPlayerDataConfig();
        Set<String> ownedMessages = getOwnedMessages(player);
        
        if (!ownedMessages.contains(messageId)) {
            ownedMessages.add(messageId);  // Mesajı oyuncuya ekle
            playerData.set("players." + player.getName() + ".ownedMessages", new ArrayList<>(ownedMessages));
            configManager.savePlayerData();  // Verileri kaydet
        }
    }
}
