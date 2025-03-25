package com.candydeathnotes;

import com.candydeathnotes.commands.DeathNotesCommand;
import com.candydeathnotes.listeners.GUIListener;
import com.candydeathnotes.listeners.DeathNoteListener;
import com.candydeathnotes.gui.DeathNotesGUI;
import com.candydeathnotes.utils.ConfigManager;
import com.candydeathnotes.utils.PlayerDataManager;
import com.candydeathnotes.utils.EconomyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private PlayerDataManager playerDataManager;
    private ConfigManager configManager;
    private Economy economy;
    private EconomyManager economyManager;
    private DeathNotesGUI deathNotesGUI;

    @Override
    public void onEnable() {
        instance = this;

        // Vault (Ekonomi Sistemi) Kontrolü
        if (!setupEconomy()) {
            getLogger().severe("[CandyDeathNotes] Vault bulunamadı! Plugin devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Holographic Displays Kontrolü
        if (!isPluginEnabled("HolographicDisplays")) {
            getLogger().severe("[CandyDeathNotes] Holographic Displays eklentisi bulunamadı! Plugin devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Yapılandırma dosyalarını oluştur
        configManager = new ConfigManager(this);
        configManager.setupFiles();

        // Ekonomi yöneticisini başlat
        economyManager = new EconomyManager(this);

        // Oyuncu verilerini yöneten sınıfı başlat
        playerDataManager = new PlayerDataManager(configManager, economyManager);

        // DeathNotesGUI'yi başlat
        deathNotesGUI = new DeathNotesGUI(playerDataManager, configManager);


        // Listener'ları kaydet
        registerListeners();

        // Komutları kaydet
        DeathNotesCommand deathNotesCommand = new DeathNotesCommand(playerDataManager, configManager, deathNotesGUI, this);
        getCommand("deathnotes").setExecutor(deathNotesCommand);

        // Tab tamamlamayı bu şekilde yapıyoruz (artık "new" komutunu içermiyor).
        getCommand("deathnotes").setTabCompleter((sender, command, alias, args) -> {
            return deathNotesCommand.onTabComplete(args);
        });

        getLogger().info("[CandyDeathNotes] Plugin başarıyla etkinleştirildi!");
    }

    @Override
    public void onDisable() {
        getLogger().info("[CandyDeathNotes] Plugin devre dışı bırakıldı.");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    private boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    private void registerListeners() {
        // GUIListener'ı uygun şekilde başlatıyoruz
        getServer().getPluginManager().registerEvents(new GUIListener(playerDataManager, configManager, deathNotesGUI, economyManager), this);
        // DeathNoteListener'ı kaydediyoruz
        getServer().getPluginManager().registerEvents(new DeathNoteListener(playerDataManager, configManager, this), this);
    }

    public static Main getInstance() {
        return instance;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public DeathNotesGUI getDeathNotesGUI() {
        return deathNotesGUI;
    }
}
