package com.candydeathnotes.utils;

import com.candydeathnotes.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {

    private final Main plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;
    private File playerDataFile;
    private FileConfiguration playerDataConfig;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        setupFiles();
    }

    public void setupFiles() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // config.yml oluştur
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                config = YamlConfiguration.loadConfiguration(configFile);
                config.set("economy-system", "Vault");
                config.set("prefix", "&6[CC-DeathNotes] ");
                saveConfig();
                addCommentsToConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }

        // messages.yml oluştur
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try {
                messagesFile.createNewFile();
                FileWriter writer = new FileWriter(messagesFile);
                writer.write("# =====================================================================================\n");
                writer.write("#                             CandyDeathNotes - Mesaj Yapılandırma Dosyası                             \n");
                writer.write("# =====================================================================================\n");
                writer.write("# Bu dosya, sunucunuzda ölüm notları için mesajlar ve fiyatları yönetmenizi sağlar.              \n");
                writer.write("# Burada yer alan her bir mesaj, oyunculara sunulacak ve belirli bir fiyat karşılığında satın      \n");
                writer.write("# alınabilir.                                                                                   \n");
                writer.write("#                                                                                              \n");
                writer.write("# Her mesajın benzersiz bir ID'si vardır. Bu ID'leri kullanarak yeni mesajlar ekleyebilir ve      \n");
                writer.write("# var olanları düzenleyebilirsiniz.                                                              \n");
                writer.write("#                                                                                              \n");
                writer.write("# ID formatı şu şekildedir: 'msg_1', 'msg_2' vb. (örneğin: msg_1, msg_2, msg_3, ...).              \n");
                writer.write("# Her mesajın metni (text) ve fiyatı (price) aşağıda belirtildiği gibi ayarlanabilir.            \n");
                writer.write("#                                                                                              \n");
                writer.write("# Lütfen her bir mesajın 'text' ve 'price' alanlarını dikkatlice düzenleyin.                   \n");
                writer.write("# NOT: Renk kodları text değerleri için devre dışı bırakılmıştır.                               \n");
                writer.write("# =====================================================================================\n\n");
                writer.close();

                messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
                messagesConfig.set("messages.msg_1.text", "Hello World!");
                messagesConfig.set("messages.msg_1.price", 100);
                saveMessagesConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        }

        // playerdatas.yml oluştur
        playerDataFile = new File(plugin.getDataFolder(), "playerdatas.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
                playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
                savePlayerData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
        }
    }

    public void addNewMessage(String message, int price) {
        String messageID = "msg_" + (messagesConfig.getConfigurationSection("messages").getKeys(false).size() + 1);
        messagesConfig.set("messages." + messageID + ".text", message);
        messagesConfig.set("messages." + messageID + ".price", price);
        saveMessagesConfig();
    }

    public void reloadConfigs() {
        setupFiles(); // Dosyaları yeniden yükle
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
            addCommentsToConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public void saveMessagesConfig() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getPlayerDataConfig() {
        return playerDataConfig;
    }

    public void savePlayerData() {
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addCommentsToConfig() {
        try {
            FileWriter writer = new FileWriter(configFile);
            writer.write(
                    "# =====================================================================================\n" +
                    "#                             CandyDeathNotes Yapılandırma Dosyası\n" +
                    "# =====================================================================================\n" +
                    "# Bu dosya, CandyDeathNotes eklentisinin yapılandırmalarını içerir.\n" +
                    "# Sunucunuzda oyuncuların ölüm notları ve diğer mesajları yönetmek için buradaki ayarları\n" +
                    "# kullanabilirsiniz. Lütfen dosya içeriğini dikkatlice düzenleyiniz.\n" +
                    "# =====================================================================================\n\n" +
                    "# =====================================================================================\n" +
                    "# Economy Sistemi Ayarı\n" +
                    "# Bu seçenek, sunucunuzda oyuncuların ödeme yapabileceği sistemi belirler.\n" +
                    "# İki farklı sistem seçeneği mevcuttur: 'Vault' veya 'PlayerPoints'.\n" +
                    "# 'Vault' seçeneği, oyuncu bakiyelerini Vault üzerinden yönetir.\n" +
                    "# 'PlayerPoints' seçeneği, oyuncu bakiyelerini PlayerPoints sistemi ile yönetir.\n" +
                    "# Lütfen uygun olanı seçiniz.\n" +
                    "# =====================================================================================\n" +
                    "economy-system: \"Vault\"\n\n" +
                    "# =====================================================================================\n" +
                    "# Prefix Ayarı\n" +
                    "# Buraya yazacağınız önek, tüm mesajlarda başlık olarak kullanılacaktır.\n" +
                    "# =====================================================================================\n" +
                    "prefix: \"&6[CC-DeathNotes] \"\n"
            );
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPrefix() {
        String prefix = config.getString("prefix", "§6[CC-DeathNotes] §f");
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }
}
