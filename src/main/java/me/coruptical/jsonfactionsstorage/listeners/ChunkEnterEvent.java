package me.coruptical.jsonfactionsstorage.listeners;

import com.google.gson.Gson;
import me.coruptical.jsonfactionsstorage.JsonFactionsStorage;
import me.coruptical.jsonfactionsstorage.data_stuff.FastChunk;
import me.coruptical.jsonfactionsstorage.utils.FactionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

public class ChunkEnterEvent implements Listener {
    JsonFactionsStorage plugin = JsonFactionsStorage.getInstance();
    int timesRan = 0;
    long totalTime = 0;
    private FactionUtils utils = new FactionUtils();

    @EventHandler
    public void onChunkEnter(PlayerMoveEvent e){
        long startTime = System.nanoTime();
        Player p = e.getPlayer();
        Location loc = p.getLocation();
        int chunkX = loc.getChunk().getX();
        int chunkZ = loc.getChunk().getZ();
        String currentChunkID = chunkX+";"+chunkZ;
        if(plugin.playerLastChunk.containsKey(p)){
            FastChunk lastChunk = plugin.playerLastChunk.get(p);
            int lastChunkOwnerID = lastChunk.getChunkOwnerID();
            String lastFac = utils.returnFactionFromID(lastChunkOwnerID);
            if(!lastChunk.getChunkID().equalsIgnoreCase(currentChunkID)){
                if(plugin.allFactionChunks.containsKey(currentChunkID)){
                    FastChunk currentFactionChunk = plugin.allFactionChunks.get(currentChunkID);
                    if(!(currentFactionChunk.getChunkOwnerID() == lastChunkOwnerID)) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Entering " + utils.returnFactionFromID(currentFactionChunk.getChunkOwnerID()) + "'s territory."));
                        plugin.playerLastChunk.replace(p, currentFactionChunk);
                    }
                }else{
                    if(lastFac != null){
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Entering wilderness."));
                    }else{
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "chunk coords: " + chunkX + " " + chunkZ));
                    }
                    plugin.playerLastChunk.replace(p, new FastChunk(chunkX, chunkZ, 0));
                }
            }
        }else{
            if(plugin.allFactionChunks.containsKey(currentChunkID)){
                FastChunk chunk = plugin.allFactionChunks.get(currentChunkID);
                plugin.playerLastChunk.put(p, chunk);
            }else{
                plugin.playerLastChunk.put(p, new FastChunk(chunkX, chunkZ, 0));
            }
        }
        long elapsedTime = System.nanoTime() - startTime;
        totalTime += elapsedTime;
        timesRan++;
        if(timesRan % 100 == 0){
            plugin.getLogger().info("Time taken to run " + timesRan + " chunkEnter methods: " + (double) totalTime/1000000 + "ms");
            plugin.getLogger().info("Average time: " + (double) (totalTime/timesRan)/1000000 + "ms");
        }
    }

}
