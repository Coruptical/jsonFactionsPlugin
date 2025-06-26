package me.coruptical.jsonfactionsstorage.utils;

import me.coruptical.jsonfactionsstorage.JsonFactionsStorage;
import me.coruptical.jsonfactionsstorage.data_stuff.Faction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.material.Lever;

import javax.xml.transform.Result;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;

public class SQLUtils {
    private static JsonFactionsStorage plugin = JsonFactionsStorage.getInstance();
    private static final String dbName = "database";
    Connection conn = null;


    private static final SQLUtils i = new SQLUtils();

    private SQLUtils(){

    }

    public static SQLUtils getInstance() {
        return i;
    }

    public void init(){
        File dataFolder = new File(plugin.getDataFolder().getAbsolutePath(), dbName+".db");
        if(!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
                dataFolder.mkdirs();
                plugin.getLogger().log(Level.WARNING, "File for database created.");
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getLogger().log(Level.SEVERE, "File write error: "+dbName+".db");
            }
        }

        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            plugin.getLogger().log(Level.WARNING, "Opened database connection successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
        }

    }

    public void openConnection(){
        File dataFolder = new File(plugin.getDataFolder().getAbsolutePath(), dbName+".db");
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            plugin.getLogger().log(Level.WARNING, "Opened database connection from some class/method.");
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
        }
    }
    public boolean closeConnection(){
        try {
            conn.close();
            conn = null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        plugin.getLogger().log(Level.WARNING, "Closed database connection from some class/method.");
        return true;
    }

    public void createTable(Connection conn) throws SQLException{
        String createTableSQL = "CREATE TABLE IF NOT EXISTS factions " +
                "( " +
                "id int, " +
                "factionName varchar(255), " +
                "factionData blob " +
                ");";
        Statement stmt = conn.createStatement();
        stmt.execute(createTableSQL);
        plugin.getLogger().log(Level.WARNING, "Successfully created factions table for database.");
    }

    public void loadAllFactions(){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
            long startTime = System.nanoTime();
            long insertedFactions = 0;
            long insertedChunks = 0;
            try {
                createTable(conn);
                SHandler sHandler = new SHandler();
                String getSQL = "SELECT * from factions";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(getSQL);

                while(rs.next()){
                    String factionName = rs.getString("factionName");
                    byte[] factionBytes = rs.getBytes("factionData");
                    Faction faction = (Faction) sHandler.readBytes(factionBytes);
                    plugin.factions.put(factionName, faction);
                    insertedFactions++;
                    insertedChunks += faction.getChunks().size();
                    plugin.getLogger().log(Level.INFO, "Loaded " + factionName + " with " + faction.getChunks().size() + " chunks into factions hashmap.");
                }
                long elapsedTime = System.nanoTime() - startTime;
                plugin.getLogger().log(Level.INFO, "Loaded " + insertedFactions + " factions with a total of " + insertedChunks + " chunks.");
                plugin.getLogger().log(Level.WARNING, "Time taken to load all async factions: " + (double) elapsedTime/1000000 + "ms");
                FactionUtils factionUtils = new FactionUtils();
                factionUtils.initFactionIDS();
                plugin.allFactionChunks = factionUtils.sumAllChunks();
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
            }
            closeConnection();
        });
    }
    public void insertFaction(Faction faction, Player player){
        long startTime = System.nanoTime();
        openConnection();
        SHandler sHandler = new SHandler();
        byte[] factionBytes = sHandler.toBytes(faction);
        String insertSQL = "INSERT INTO factions(id, factionName, factionData) VALUES(?,?,?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);
            pstmt.setInt(1, faction.getId());
            pstmt.setString(2, faction.getFactionName());
            pstmt.setBytes(3, factionBytes);
            pstmt.executeUpdate();
            pstmt.close();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Added " + faction.getFactionName()) + " to database.");
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
        }
        closeConnection();
        long elapsedTime = System.nanoTime() - startTime;
        plugin.getLogger().log(Level.INFO, "Time taken to create new faction: " + (double) elapsedTime/1000000 + "ms");
    }

    public void deleteFaction(Faction faction, Player player){
        long startTime = System.nanoTime();
        openConnection();
        String deleteSQL = "DELETE FROM factions WHERE id = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, faction.getId());
            pstmt.executeUpdate();
            pstmt.close();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Deleted " + faction.getFactionName() + " from database."));
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
        }
        closeConnection();
        long elapsedTime = System.nanoTime() - startTime;
        plugin.getLogger().log(Level.INFO, "Time taken to delete faction: " + (double) elapsedTime/1000000 + "ms");
    }

    public void insertAllFactions(){
        long startTime = System.nanoTime();
        long insertedFactions = 0;
        long insertedChunks = 0;
        openConnection();
        try {
            SHandler sHandler = new SHandler();
            for(String key: plugin.factions.keySet()){
                Faction faction = plugin.factions.get(key);
                byte[] factionBytes = sHandler.toBytes(faction);
                String insertSQL = "UPDATE factions SET factionData = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setBytes(1, factionBytes);
                pstmt.setInt(2, faction.getId());
                pstmt.executeUpdate();
                pstmt.close();
                insertedFactions++;
                insertedChunks += faction.getChunks().size();
            }
            long elapsedTime = System.nanoTime() - startTime;
            plugin.getLogger().log(Level.INFO, "Inserted " + insertedFactions + " factions with a total of " + insertedChunks + " chunks.");
            plugin.getLogger().log(Level.WARNING, "Time taken for insertFactions: " + (double) elapsedTime/1000000 + "ms");
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.SEVERE, "Error while inserting all factions: ");
            plugin.getLogger().log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
        }
        boolean close = closeConnection();
        if(!close){
            plugin.getLogger().log(Level.SEVERE, "Failed to close connection to database.");
        }
    }

    public void deleteTable(Connection conn) throws SQLException{
        String deleteTableSQL = "DROP TABLE factions";
        Statement stmt = conn.createStatement();
        stmt.execute(deleteTableSQL);
    }
}
