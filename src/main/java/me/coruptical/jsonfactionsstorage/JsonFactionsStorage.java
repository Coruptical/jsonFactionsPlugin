package me.coruptical.jsonfactionsstorage;

import com.google.gson.Gson;
import me.coruptical.jsonfactionsstorage.commands.Factions;
import me.coruptical.jsonfactionsstorage.data_stuff.Faction;
import me.coruptical.jsonfactionsstorage.data_stuff.FastChunk;
import me.coruptical.jsonfactionsstorage.dynmap.DynmapEngine;
import me.coruptical.jsonfactionsstorage.listeners.ChunkEnterEvent;
import me.coruptical.jsonfactionsstorage.utils.SQLUtils;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public final class JsonFactionsStorage extends JavaPlugin {

    private static JsonFactionsStorage instance;

    public static JsonFactionsStorage getInstance(){
        return instance;
    }

    public HashMap<String, Faction> factions = new HashMap<>();
    public HashMap<String, FastChunk> allFactionChunks = new HashMap<>();
    public HashMap<Player, FastChunk> playerLastChunk = new HashMap<>();
    public Set<Integer> allFactionIDS = new HashSet<>();
    public HashMap<Integer, String> factionIDS = new HashMap<>();


    @Override
    public void onEnable() {
        instance = this;

        SQLUtils.getInstance().init();
        SQLUtils.getInstance().loadAllFactions();

        // Setup config
        saveDefaultConfig();

        // final FactionUtils factionUtils = new FactionUtils();

        getServer().getPluginManager().registerEvents(new ChunkEnterEvent(), this);

        getCommand("factions").setExecutor(new Factions());
        getCommand("factions").setTabCompleter(new Factions());

        DynmapEngine.getInstance().init();

    }

    @Override
    public void onDisable() {
        saveDefaultConfig();
        SQLUtils.getInstance().insertAllFactions();
    }

    public static String getDynmapWorld() {
        return getInstance().getConfig().getString("DynmapWorld");
    }

    public static String getLayerName(){
        return getInstance().getConfig().getString("LayerName");
    }

    public static int getRefreshTimeInMinutes() {
        return getInstance().getConfig().getInt("RefreshInterval") * 20 * 60;
    }

    public static int getRefreshTimeInSeconds(){
        return getInstance().getConfig().getInt("RefreshInterval") * 20;
    }

    public static int getMarkerLineWeight() {
        return getInstance().getConfig().getInt("MarkerLineWeight");
    }

    public static double getMarkerLineOpacity() {
        return getInstance().getConfig().getDouble("MarkerLineOpacity");
    }

    public static double getMarkerFillOpacity() {
        return getInstance().getConfig().getDouble("MarkerFillOpacity");
    }

    public static String getAge() {
        return getInstance().getConfig().getString("CurrentAge");
    }

    public static String getMarkerLineColor(){
        return getInstance().getConfig().getString("MarkerLineColor");
    }

    public static String getMarkerFillColor(){
        return getInstance().getConfig().getString("MarkerFillColor");
    }
}
