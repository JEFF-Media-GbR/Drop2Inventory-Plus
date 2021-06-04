package de.jeff_media.drop2inventory.config;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

// TODO: Switch to my new Config Updater
public class ConfigUpdater {

    final Main main;

    public ConfigUpdater(Main main) {
        this.main = main;
    }

    public void updateConfig() {

        try {
            Files.deleteIfExists(new File(main.getDataFolder().getAbsolutePath() + File.separator + "config.old.yml").toPath());
        } catch (IOException e) {
            main.getLogger().severe("Could not delete config.old.yml");
        }

        Utils.renameFileInPluginDir(main, "config.yml", "config.old.yml");

        main.saveDefaultConfig();

        File oldConfigFile = new File(main.getDataFolder().getAbsolutePath() + File.separator + "config.old.yml");
        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldConfigFile);

        if(oldConfig.getBoolean(Config.DEBUG)) {
            main.setDebug(true);


        }

        try {
            oldConfig.load(oldConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        Map<String, Object> oldValues = oldConfig.getValues(false);

        // Read default config to keep comments
        ArrayList<String> linesInDefaultConfig = new ArrayList<>();
        try {

            Scanner scanner = new Scanner(
                    new File(main.getDataFolder().getAbsolutePath() + File.separator + "config.yml"),"UTF-8");
            while (scanner.hasNextLine()) {
                linesInDefaultConfig.add(scanner.nextLine() + "");
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ArrayList<String> newLines = new ArrayList<>();
        for (String line : linesInDefaultConfig) {
            String newline = line;
            if (line.startsWith("config-version:")) {

            }
            else if (line.startsWith("disabled-blocks:")) {
                newline = null;
                newLines.add(main.blocksIsWhitelist ? "enabled-blocks:" : "disabled-blocks:");
                if (main.disabledBlocks != null) {
                    for (Material mat : main.disabledBlocks) {
                        newLines.add("- " + mat.name());
                    }
                }
            }
            else if (line.startsWith("disabled-mobs:")) {
                newline = null;
                newLines.add(main.isMobsIsWhitelist() ? Config.ENABLED_MOBS : "disabled-mobs:");
                if (main.disabledMobs != null) {
                    for (String mob : main.disabledMobs) {
                        newLines.add("- " + mob);
                    }
                }
            }
            else if (line.startsWith("disabled-worlds:")) {
                newline = null;
                newLines.add("disabled-worlds:");
                if (main.disabledWorlds != null) {
                    for (String world : main.disabledWorlds) {
                        newLines.add("- " + world);
                    }
                }
            }
            else {
                for (String node : oldValues.keySet()) {
                    if (line.startsWith(node + ":")) {

                        String quotes = "";

                        if (node.startsWith("message-")) // needs double quotes
                            quotes = "\"";



                        newline = node + ": " + quotes + oldValues.get(node).toString() + quotes;

                        break;
                    }
                }
            }
            if (newline != null)
                newLines.add(newline);
        }

        BufferedWriter fw;
        String[] linesArray = newLines.toArray(new String[linesInDefaultConfig.size()]);
        try {
            fw = Files.newBufferedWriter(new File(main.getDataFolder().getAbsolutePath() + File.separator + "config.yml").toPath(), StandardCharsets.UTF_8);
            for (String s : linesArray) {
                fw.write(s + "\n");
            }
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Utils.renameFileInPluginDir(plugin, "config.yml.default", "config.yml");

    }

}