package me.coruptical.jsonfactionsstorage.commands;

import me.coruptical.jsonfactionsstorage.JsonFactionsStorage;
import me.coruptical.jsonfactionsstorage.data_stuff.Faction;
import me.coruptical.jsonfactionsstorage.data_stuff.FastChunk;
import me.coruptical.jsonfactionsstorage.utils.FactionUtils;
import me.coruptical.jsonfactionsstorage.utils.SQLUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Factions implements CommandExecutor, TabCompleter {
    JsonFactionsStorage plugin = JsonFactionsStorage.getInstance();
    FactionUtils utils = new FactionUtils();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player p = (Player) sender;
            if(args.length > 0){
                if(args[0].equalsIgnoreCase("create")){
                    if(args.length > 1){
                        String facName = args[1].toLowerCase();
                        if(!plugin.factions.containsKey(facName)){
                            Faction newFac = new Faction(utils.findValidID(), facName, new HashSet<>(), new ArrayList<>(), null);
                            plugin.factions.put(facName, newFac);
                            plugin.factionIDS.put(newFac.getId(), facName);
                            SQLUtils.getInstance().insertFaction(newFac, p);
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully created new faction called &f" + facName));
                        }else{
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This faction already exists."));
                        }
                    }else{
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Please enter a name for this faction."));
                    }
                }else if(args[0].equalsIgnoreCase("delete")){
                    if(args.length > 1){
                        String facName = args[1].toLowerCase();
                        if(plugin.factions.containsKey(facName)){
                            Faction faction = plugin.factions.get(facName);
                            for(FastChunk chunk: plugin.factions.get(facName).getChunks()){
                                plugin.allFactionChunks.remove(chunk.getChunkID());
                            }
                            utils.deleteFactionID(faction);
                            plugin.factions.remove(facName);
                            SQLUtils.getInstance().deleteFaction(faction, p);
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully deleted &f" + facName));
                        }else{
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This faction does not exist."));
                        }
                    }else{
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Please enter the faction to delete."));
                    }
                }else if(args[0].equalsIgnoreCase("claim")) {
                    if (args.length > 1) {
                        String facName = args[1].toLowerCase();
                        if (plugin.factions.containsKey(facName)) {
                            Faction faction = plugin.factions.get(facName);
                            Location loc = p.getLocation();
                            int x = loc.getChunk().getX();
                            int z = loc.getChunk().getZ();
                            if (plugin.allFactionChunks.get(x + ";" + z) == null) {
                                FastChunk chunk = new FastChunk(x, z, faction.getId());
                                plugin.factions.get(facName).getChunks().add(chunk);
                                plugin.allFactionChunks.put(chunk.getChunkID(), chunk);
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully claimed" + x + " " + z + " for " + facName + "'s faction."));
                            } else {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This chunk is already claimed."));
                            }
                        } else {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This faction does not exist."));
                        }
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Please enter the faction to claim this chunk for."));
                    }
                }else if(args[0].equalsIgnoreCase("un-claim")){
                    if(args.length > 1) {
                        String facName = args[1].toLowerCase();
                        if(plugin.factions.containsKey(facName)){
                            Faction faction = plugin.factions.get(facName);
                            Location loc = p.getLocation();
                            int x = loc.getChunk().getX();
                            int z = loc.getChunk().getZ();
                            String id = x+";"+z;
                            if(plugin.allFactionChunks.containsKey(id)){
                                FastChunk chunk = plugin.allFactionChunks.get(id);
                                if(chunk.getChunkOwnerID() == faction.getId()){
                                    plugin.allFactionChunks.remove(id);
                                    plugin.factions.get(facName).getChunks().remove(chunk);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully removed chunk at " + x + " " + z + " for " + facName));
                                }else{
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This chunk is not owned by " + facName));
                                }
                            }else{
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This chunk is not owned by a faction."));
                            }
                        }else{
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This faction does not exist."));
                        }
                    }else{
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Please enter the faction to un-claim this chunk for."));
                    }
                }else if(args[0].equalsIgnoreCase("radius-claim")){
                    if(args.length > 1){
                        String facName = args[1].toLowerCase();
                        if(args.length > 2){
                            try{
                                int radius = Integer.parseInt(args[2]);
                                if(radius <= 128){
                                    Location loc = p.getLocation();
                                    utils.radiusClaim(p, facName, loc.getChunk().getX(), loc.getChunk().getZ(), radius);
                                }else{
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This radius is larger than 128."));
                                }
                            }catch(NumberFormatException e){
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This is not a valid radius."));
                            }
                        }else{
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Please enter a radius 0-128."));
                        }
                    }else{
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Please enter the faction to radius-claim for."));
                    }
                }else if(args[0].equalsIgnoreCase("list")){
                    for(String name : plugin.factions.keySet()){
                        Faction fac = plugin.factions.get(name);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b" + name + ": \n &7" + fac.toString()));
                    }
                }else if(args[0].equalsIgnoreCase("list-chunks")){
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&fallFactionChunks hashmap: "));
                    if(!plugin.allFactionChunks.isEmpty()){
                        for(String coordID : plugin.allFactionChunks.keySet()){
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f" + coordID));
                        }
                    }else{
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7The allFactionChunks hashmap is empty."));
                    }
                }
            }else{
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Please enter a valid argument. &8<claim/create/delete/un-claim/radius-claim/list>"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();
        if(args.length == 0) return list;
        if(args.length == 1 && args[0].length() == 0){
            list.add("list-chunks");
            list.add("create");
            list.add("claim");
            list.add("radius-claim");
            list.add("delete");
            list.add("list");
            list.add("un-claim");
        }
        if(args.length == 1 && args[0].length() > 0){
            if("claim".startsWith(args[0])) list.add("claim");
            if("radius-claim".startsWith(args[0])) list.add("radius-claim");
            if("delete".startsWith(args[0])) list.add("delete");
            if("list".startsWith(args[0])) list.add("list");
            if("un-claim".startsWith(args[0])) list.add("un-claim");
            if("create".startsWith(args[0])) list.add("create");
            if("list-chunks".startsWith(args[0])) list.add("list-chunks");
        }
        return list;
    }
}
