package com.candydeathnotes.gui;

import com.candydeathnotes.utils.ConfigManager;
import com.candydeathnotes.utils.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class DeathNotesGUI {

    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;

    public DeathNotesGUI(PlayerDataManager playerDataManager, ConfigManager configManager) {
        this.playerDataManager = playerDataManager;
        this.configManager = configManager;
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Ölüm Notları Mağazası");

        FileConfiguration messagesConfig = configManager.getMessagesConfig();
        Set<String> ownedMessages = playerDataManager.getOwnedMessages(player);
        String activeMessage = playerDataManager.getActiveMessage(player);

        int slot = 9;

        // Satın alınmış ve aktif olarak kullanılmayan mesajları GUI'ye ekle
        for (String messageId : ownedMessages) {
            String text = messagesConfig.getString("messages." + messageId + ".text");

            // Eğer bu mesaj aktif olarak kullanılıyorsa, farklı şekilde gösterecek
            Material material = messageId.equals(activeMessage) ? Material.DOUBLE_PLANT : Material.FEATHER;
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + text);

            List<String> lore = new ArrayList<>();
            if (messageId.equals(activeMessage)) {
                lore.add(ChatColor.GREEN + "Durum: Kullanılıyor");  // Aktif kullanılan öğe
            } else {
                lore.add(ChatColor.RED + "Durum: Kullanılmıyor");  // Sahip olup kullanılmayan öğe
                lore.add(ChatColor.YELLOW + "Kullanmak için tıklayın");  // Kullanmak için tıklayın mesajı
            }
            meta.setLore(lore);
            item.setItemMeta(meta);

            gui.setItem(slot, item);
            slot++;
            if (slot == 45) break;
        }

        // Satın alınmamış öğeleri (kitap) GUI'ye ekle
        for (String messageId : messagesConfig.getConfigurationSection("messages").getKeys(false)) {
            if (!ownedMessages.contains(messageId)) {
                String text = messagesConfig.getString("messages." + messageId + ".text");
                int price = messagesConfig.getInt("messages." + messageId + ".price");

                ItemStack item = new ItemStack(Material.BOOK);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.AQUA + text);

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Fiyat: " + ChatColor.GOLD + price);
                lore.add(ChatColor.RED + "Durum: Kilitli");  // Satın alınmamış öğe için durum
                lore.add(ChatColor.YELLOW + "Satın almak için tıklayın");
                meta.setLore(lore);
                item.setItemMeta(meta);

                gui.setItem(slot, item);
                slot++;
                if (slot == 45) break;
            }
        }

        // Oyuncunun kafasını GUI'ye ekle
        ItemStack headItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta headMeta = (SkullMeta) headItem.getItemMeta();
        assert headMeta != null;

        headMeta.setOwner(player.getName());
        headMeta.setDisplayName(ChatColor.GREEN + player.getName());

        List<String> lore = new ArrayList<>();
        if (activeMessage == null || activeMessage.equals("none")) {
            lore.add(ChatColor.RED + "Şu an aktif mesajınız yok!");
            lore.add(ChatColor.YELLOW + "Lütfen Aşağıdan Mesaj Seçiniz");
        } else {
            String activeMessageText = messagesConfig.getString("messages." + activeMessage + ".text");
            lore.add(ChatColor.YELLOW + activeMessageText);
            lore.add(ChatColor.DARK_GRAY + "» " + ChatColor.AQUA + "Şu an kullandığın Ölüm Mesajı" + ChatColor.DARK_GRAY + " «");
            lore.add(ChatColor.DARK_GRAY + "» " + ChatColor.LIGHT_PURPLE + ChatColor.UNDERLINE + "Mesajsız girmek için tıkla" + ChatColor.DARK_GRAY + " «");
        }

        headMeta.setLore(lore);
        headItem.setItemMeta(headMeta);

        gui.setItem(4, headItem);
        player.openInventory(gui);
    }

    /**
     * Oyuncunun tıkladığı mesajın ID'sini döndürür.
     *
     * @param event InventoryClickEvent olayı
     * @return Mesaj ID'si (eğer bulunursa), yoksa null döner.
     */
    public String getClickedMessageId(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return null;

        String messageText = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        FileConfiguration messagesConfig = configManager.getMessagesConfig();

        for (String key : messagesConfig.getConfigurationSection("messages").getKeys(false)) {
            if (ChatColor.stripColor(messagesConfig.getString("messages." + key + ".text")).equalsIgnoreCase(messageText)) {
                return key;
            }
        }
        return null;
    }
}
