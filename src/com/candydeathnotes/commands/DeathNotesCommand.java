package com.candydeathnotes.commands;

import com.candydeathnotes.gui.DeathNotesGUI;
import com.candydeathnotes.utils.ConfigManager;
import com.candydeathnotes.utils.PlayerDataManager;
import com.candydeathnotes.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeathNotesCommand implements CommandExecutor {

    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final DeathNotesGUI deathNotesGUI;
    private final Main plugin;

    public DeathNotesCommand(PlayerDataManager playerDataManager, ConfigManager configManager, DeathNotesGUI deathNotesGUI, Main plugin) {
        this.playerDataManager = playerDataManager;
        this.configManager = configManager;
        this.deathNotesGUI = deathNotesGUI;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komut yalnızca oyuncular tarafından kullanılabilir.");
            return true;
        }

        Player player = (Player) sender;

        // /deathnotes komutunun sadece args[0] ile kullanılacağı yerler
        if (args.length == 0) {
            return onCommand(sender, command, label, new String[] { "menu" });
        }

        if (args[0].equalsIgnoreCase("menu")) {
            if (!player.hasPermission("deathnotes.use")) {
                player.sendMessage(configManager.getPrefix() + "§cBu komutu kullanmak için yeterli izniniz yok.");
                return true;
            }
            deathNotesGUI.openGUI(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            player.sendMessage(configManager.getPrefix() + "§bCandyDeathNotes Plugin v1.0");
            player.sendMessage("§7</> §aDev By RianMC, Berk AYDIN");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("deathnotes.admin")) {
                player.sendMessage(configManager.getPrefix() + "§cBu komutu kullanmak için yeterli izniniz yok.");
                return true;
            }

            configManager.setupFiles();
            player.sendMessage(configManager.getPrefix() + "§aYapılandırma ve mesaj dosyaları başarıyla yeniden yüklendi.");
            return true;
        }

        return false;
    }

    // Tab kompleter işlemi burada yapılacak.
    public List<String> onTabComplete(String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            if ("menu".startsWith(args[0].toLowerCase())) suggestions.add("menu");
            if ("info".startsWith(args[0].toLowerCase())) suggestions.add("info");
            if ("reload".startsWith(args[0].toLowerCase())) suggestions.add("reload");
        }

        return suggestions;
    }
}
