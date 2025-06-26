package me.coruptical.jsonfactionsstorage.utils;

import com.google.gson.Gson;
import me.coruptical.jsonfactionsstorage.JsonFactionsStorage;
import me.coruptical.jsonfactionsstorage.data_stuff.Faction;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class JsonUtils {

    private static JsonFactionsStorage plugin = JsonFactionsStorage.getInstance();

    public void saveData() throws IOException {
        long startTime = System.nanoTime();
        Gson gson = new Gson();
        File file = new File(plugin.getDataFolder().getAbsolutePath() + "/data.json");
        file.getParentFile().mkdir();
        file.createNewFile();
        Writer writer = new FileWriter(file, false);

        ArrayList<Faction> factionList = new ArrayList<>();
        for(String key : plugin.factions.keySet()){
            factionList.add(plugin.factions.get(key));
        }

        gson.toJson(factionList, writer);
        writer.flush();
        writer.close();
        long elapsedTime = System.nanoTime() - startTime;
        plugin.getLogger().warning("Data saved: " + (double) elapsedTime/1000000 + "ms");
    }

    public void loadData() throws IOException {
        long startTime = System.nanoTime();
        Gson gson = new Gson();
        File file = new File(plugin.getDataFolder().getAbsolutePath() + "/data.json");
        if(file.exists()){
            Reader reader = new FileReader(file);
            Faction[] facs = gson.fromJson(reader, Faction[].class);
            for(Faction faction : facs){
                String name = faction.getFactionName();
                plugin.factions.put(name, faction);
            }
            long elapsedTime = System.nanoTime() - startTime;
            plugin.getLogger().warning("Data loaded.\nTime taken to load all data: " + (double) elapsedTime/1000000 + "ms");
        }else{
            plugin.getLogger().warning("There is no data to be loaded. None was loaded.");
        }
    }
}
