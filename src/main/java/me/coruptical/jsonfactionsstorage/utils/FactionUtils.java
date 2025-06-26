package me.coruptical.jsonfactionsstorage.utils;

import me.coruptical.jsonfactionsstorage.JsonFactionsStorage;
import me.coruptical.jsonfactionsstorage.data_stuff.Faction;
import me.coruptical.jsonfactionsstorage.data_stuff.FastChunk;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

public class FactionUtils {
    JsonFactionsStorage plugin = JsonFactionsStorage.getInstance();

    public HashMap<String, FastChunk> sumAllChunks(){
        int totalChunks = 0;
        long startTime = System.nanoTime();
        HashMap<String, FastChunk> map = new HashMap<>();
        if(plugin.factions.size() > 0){
            for(String facName: plugin.factions.keySet()){
                Faction faction = plugin.factions.get(facName);
                totalChunks += faction.getChunks().size();
                for (FastChunk c : faction.getChunks()) {
                    String id = c.getChunkID();
                    map.put(id, c);
                }
            }
            long elapsedTimed = System.nanoTime() - startTime;
            plugin.getLogger().log(Level.WARNING, "Time taken to sum " + totalChunks + " chunks: " + (double) elapsedTimed/1000000 + "ms");
        }
        return map;
    }

    public void radiusClaim(Player p, String factionName, int startX, int startZ, int radius){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            long startTime = System.nanoTime();
            Faction faction = plugin.factions.get(factionName);
            int factionId = faction.getId();
            int toClaim = 0;
            int claimed = 0;
            for(int x = -radius; x <= radius; x++){
                for(int z = -radius; z <= radius; z++){
                    FastChunk chunk = new FastChunk(startX+x, startZ+z, factionId);
                    if(!plugin.allFactionChunks.containsKey(chunk.getChunkID())){
                        faction.getChunks().add(chunk);
                        plugin.allFactionChunks.put(chunk.getChunkID(), chunk);
                        claimed++;
                    }
                    toClaim++;
                }
            }
            long elapsedTimed = System.nanoTime() - startTime;
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully claimed " + claimed + "/" + toClaim + " chunks for " + factionName));
            plugin.getLogger().info("Time taken for async radius-claim with radius " + radius + ": " + (double) elapsedTimed/1000000 + "ms");
        });
    }

    public Integer findValidID(){
        long startTime = System.nanoTime();
        int i = 1;
        while(true){
            if(!plugin.allFactionIDS.contains(i)){
                plugin.allFactionIDS.add(i);
                long elapsedTime = System.nanoTime() - startTime;
                plugin.getLogger().log(Level.INFO, "Valid id found: " + i);
                plugin.getLogger().log(Level.INFO, "Time taken for finding a valid id: " + (double) elapsedTime/1000000 + "ms");
                return i;
            }
            i++;
        }
    }

    public void deleteFactionID(Faction faction){
        int id = faction.getId();
        plugin.allFactionIDS.remove(id);
    }

    public void initFactionIDS(){
        for(String key : plugin.factions.keySet()){
            Faction fac = plugin.factions.get(key);
            int id = fac.getId();
            plugin.factionIDS.put(id, fac.getFactionName());
            plugin.allFactionIDS.add(id);
        }
    }

    public String returnFactionFromID(int id){
        return plugin.factionIDS.getOrDefault(id, null);
    }
}
