package com.candydeathnotes.listeners;

import com.candydeathnotes.gui.DeathNotesGUI;
import com.candydeathnotes.utils.ConfigManager;
import com.candydeathnotes.utils.PlayerDataManager;
import com.candydeathnotes.utils.EconomyManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Sound;

public class GUIListener implements Listener {

    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final DeathNotesGUI deathNotesGUI;
    private final EconomyManager economyManager;

    public GUIListener(PlayerDataManager playerDataManager, ConfigManager configManager, DeathNotesGUI deathNotesGUI, EconomyManager economyManager) {
        this.playerDataManager = playerDataManager;
        this.configManager = configManager;
        this.deathNotesGUI = deathNotesGUI;
        this.economyManager = economyManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String prefix = configManager.getPrefix();

        if (!event.getView().getTitle().equals("§8Ölüm Notları Mağazası")) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        if (clickedItem.getItemMeta() instanceof SkullMeta) {
            clearActiveMessage(player, prefix);
            return;
        }

        String messageId = deathNotesGUI.getClickedMessageId(event);
        if (messageId == null) {
            invalidMessage(player, prefix);
            return;
        }

        FileConfiguration messagesConfig = configManager.getMessagesConfig();
        int price = messagesConfig.getInt("messages." + messageId + ".price");

        if (playerDataManager.hasPurchasedMessage(player, messageId)) {
            handleAlreadyPurchased(player, messageId, prefix, messagesConfig);
            return;
        }

        if (economyManager.hasEnoughMoney(player, price)) {
            processPurchase(player, messageId, prefix, messagesConfig, price);
        } else {
            insufficientFunds(player, prefix);
        }
    }

    private void clearActiveMessage(Player player, String prefix) {
        playerDataManager.setActiveMessage(player, "none");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HAT, 1.0f, 1.0f);
        player.sendMessage(prefix + "§aArtık ölüm notunuz yok!");
        deathNotesGUI.openGUI(player);
    }

    private void invalidMessage(Player player, String prefix) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        player.sendMessage(prefix + "§cBilinmeyen not!");
    }

    private void handleAlreadyPurchased(Player player, String messageId, String prefix, FileConfiguration messagesConfig) {
        String currentActiveMessage = playerDataManager.getActiveMessage(player);
        if (messageId.equals(currentActiveMessage)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADING, 1.0f, 1.0f);
            player.sendMessage(prefix + "§cBu not zaten aktif!");
            return;
        }

        playerDataManager.setActiveMessage(player, messageId);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(prefix + "§aYeni Ölüm Notunuz §e" + messagesConfig.getString("messages." + messageId + ".text") + " §aolarak ayarlandı");
        deathNotesGUI.openGUI(player);
    }

    private void processPurchase(Player player, String messageId, String prefix, FileConfiguration messagesConfig, int price) {
        economyManager.withdrawMoney(player, price);
        playerDataManager.addPurchasedMessage(player, messageId);
        playerDataManager.setActiveMessage(player, messageId); // Satın alır almaz aktif yap
        
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(prefix + "§aBaşarıyla satın alındı ve aktif edildi: §e" + messagesConfig.getString("messages." + messageId + ".text"));
        
        deathNotesGUI.openGUI(player);
    }

    private void insufficientFunds(Player player, String prefix) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        player.sendMessage(prefix + "§cYetersiz bakiye! Bu notu almak için yeterli paraya sahip değilsiniz.");
    }
}