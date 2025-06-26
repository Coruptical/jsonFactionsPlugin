package me.coruptical.jsonfactionsstorage.dynmap;

import me.coruptical.jsonfactionsstorage.JsonFactionsStorage;
import me.coruptical.jsonfactionsstorage.data_stuff.Faction;
import me.coruptical.jsonfactionsstorage.data_stuff.FastChunk;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.*;
import java.util.logging.Level;

public class DynmapEngine {

    private static final DynmapEngine i = new DynmapEngine() {
    };

    JsonFactionsStorage plugin = JsonFactionsStorage.getInstance();
    public DynmapAPI dynmapApi;
    public MarkerAPI markerApi;
    public MarkerSet markerset;

    private boolean stop = false;

    // private Map<String, TempAreaMarker> tempAreas;

    private DynmapEngine() {
    }

    public static DynmapEngine getInstance() {
        return i;
    }

    public void setStop(boolean stop){
        this.stop = stop;
    }




    public final static int BLOCKS_PER_CHUNK = 16;

    public final static String DYNMAP_INTEGRATION = "\u00A7dDynmap Integration: \u00A7e";

    public final static String FACTIONS = "factions";
    public final static String FACTIONS_ = FACTIONS + "_";

    public final static String FACTIONS_MARKERSET = FACTIONS_ + "markerset";


    private long startTime = 0;
    private long facsRan = 0;
    private long facsToRun = 0;
    public List<String> ranIds = new ArrayList<>();

    //
    //
    public boolean running = false;
    //
    //
    Map<String, TempAreaMarker> tempAreas = new HashMap<>();

    public void init() {
        Bukkit.getScheduler().runTaskLater(plugin, () ->{
            plugin.getLogger().warning("Initalising DynmapEngine...");
            Plugin dynmap = Bukkit.getServer().getPluginManager().getPlugin("dynmap");

            if (dynmap == null || !dynmap.isEnabled()) {
                plugin.getLogger().log(Level.SEVERE, "Dynmap is not enabled.");
                return;
            }

            // Should we even use dynmap?
            if (this.markerset != null) {
                plugin.getLogger().info("deleting marker set...");
                this.markerset.deleteMarkerSet();
                this.markerset = null;
            }

            // Shedule non thread safe sync at the end!
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () ->
            {
                if(!running){
                    if(!stop){
                        startTime = System.nanoTime();
                        facsToRun = 0;
                        facsRan = 0;
                        tempAreas.clear();

                        getAreas();

                        if (!updateCore()) {
                            plugin.getLogger().log(Level.WARNING, "doing updatecore");
                            return;
                        }

                        // createLayer() is thread safe but it makes use of fields set in updateCore() so we must have it after.
                        if (!updateLayer(createLayer())) {
                            plugin.getLogger().log(Level.WARNING, "doing update layer create layer");
                            return;
                        }

                        plugin.getLogger().info("Running update method for Dynmap Engine....");
                        //removeDuplicatesFromAreas();
                        // if(this.tempAreas != null) updateAreas(this.tempAreas);

                        Bukkit.getScheduler().runTaskLater(plugin, () -> updateAreas(tempAreas), 10L);
                    }else{
                        stop = false;
                    }
                }
            }, 100L, JsonFactionsStorage.getRefreshTimeInSeconds());

            plugin.getLogger().info("DynmapEngine initialisation completed.");
        }, 60L);
    }


    public boolean updateCore() {
        // Get DynmapAPI
        this.dynmapApi = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
        if (this.dynmapApi == null) {
            plugin.getLogger().severe("Could not retrieve the DynmapAPI.");
            return false;
        }

        // Get MarkerAPI
        this.markerApi = this.dynmapApi.getMarkerAPI();
        if (this.markerApi == null) {
            plugin.getLogger().severe("Could not retrieve the MarkerAPI.");
            return false;
        }

        return true;
    }

    public boolean updateLayer(TempMarkerSet temp) {
        this.markerset = this.markerApi.getMarkerSet(FACTIONS_MARKERSET);
        if (this.markerset == null) {
            this.markerset = temp.create(this.markerApi, FACTIONS_MARKERSET);
            if (this.markerset == null) {
                plugin.getLogger().severe("Could not create the Faction Markerset/Layer");
                return false;
            }
        } else {
            temp.update(this.markerset);
        }
        return true;
    }

    public TempMarkerSet createLayer() {
        TempMarkerSet ret = new TempMarkerSet();
        ret.label = JsonFactionsStorage.getLayerName();
        ret.minimumZoom = plugin.getConfig().getInt("MinimumZoom");
        ret.priority = plugin.getConfig().getInt("LayerPriority");
        ret.hideByDefault = plugin.getConfig().getBoolean("HideByDefault");
        return ret;
    }

    public void updateAreas(Map<String, TempAreaMarker> areas) {
        if(!stop){
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                long startTime = System.nanoTime();


                for(AreaMarker marker : this.markerset.getAreaMarkers()){
                    marker.deleteMarker();
                }
                for(Map.Entry<String, TempAreaMarker> entry: areas.entrySet()){
                    String markerID = entry.getKey();
                    TempAreaMarker temp = entry.getValue();

                    temp.create(this.markerset, markerID);
                }


                long elapsedTime = System.nanoTime() - startTime;
                plugin.getLogger().info("Time taken to update areas: " + (double) elapsedTime/1000000 + "ms");
            }, 1);
        }else{
            stop = false;
        }
    }

    public void getAreas(){
        this.running = true;
        computeAreas();
    }


    public void computeAreas() {
        if(!stop){
            long thisStart = System.nanoTime();
            String world = JsonFactionsStorage.getDynmapWorld();
            if(plugin.factions.size() > 0){
                for(String key : plugin.factions.keySet()){
                    Faction faction = plugin.factions.get(key);
                    Set<FastChunk> factionChunks = faction.getChunks();
                    plugin.getLogger().info("Computed: " + faction.getFactionName() + ": " + factionChunks.size() + "C");
                    createAreas(world, faction, factionChunks);
                }
            }else{
                plugin.getLogger().info("No factions exist to render dynmap on.");
            }
            long elapsedThis = System.nanoTime() - thisStart;
            plugin.getLogger().info("Time taken to compute areas: " + (double) elapsedThis/1000000 + "ms");
        }else{
            stop = false;
        }
    }

    public void createAreas(String world, Faction faction, Set<FastChunk> chunks) {
        if(!stop){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
                long thisStart = System.nanoTime();
                Map<String, TempAreaMarker> ret = new HashMap<>();
                // ... and has any chunks ...
                if (chunks.isEmpty()) {
                    //plugin.getLogger().info(faction.getFactionName() + " does not have any chunks to render.");
                    return;
                }

                // Index of polygon for given faction
                int markerIndex = 0;

                // Loop through chunks: set flags on chunk map
                TileFlags allChunkFlags = new TileFlags();
                LinkedList<FastChunk> allChunks = new LinkedList<>();
                for (FastChunk chunk : chunks) {
                    allChunkFlags.setFlag(chunk.getX(), chunk.getZ(), true); // Set flag for chunk
                    allChunks.addLast(chunk);
                }
                facsRan++;

                // Loop through until we don't find more areas
                while (allChunks != null) {
                    TileFlags ourChunkFlags = null;
                    LinkedList<FastChunk> ourChunks = null;
                    LinkedList<FastChunk> newChunks = null;

                    int minimumX = Integer.MAX_VALUE;
                    int minimumZ = Integer.MAX_VALUE;
                    for (FastChunk chunk : allChunks) {
                        int chunkX = chunk.getX();
                        int chunkZ = chunk.getZ();

                        // If we need to start shape, and this block is not part of one yet
                        if (ourChunkFlags == null && allChunkFlags.getFlag(chunkX, chunkZ)) {
                            ourChunkFlags = new TileFlags(); // Create map for shape
                            ourChunks = new LinkedList<>();
                            floodFillTarget(allChunkFlags, ourChunkFlags, chunkX, chunkZ); // Copy shape
                            ourChunks.add(chunk); // Add it to our chunk list
                            minimumX = chunkX;
                            minimumZ = chunkZ;
                        }
                        // If shape found, and we're in it, add to our node list
                        else if (ourChunkFlags != null && ourChunkFlags.getFlag(chunkX, chunkZ)) {
                            ourChunks.add(chunk);
                            if (chunkX < minimumX) {
                                minimumX = chunkX;
                                minimumZ = chunkZ;
                            } else if (chunkX == minimumX && chunkZ < minimumZ) {
                                minimumZ = chunkZ;
                            }
                        }
                        // Else, keep it in the list for the next polygon
                        else {
                            if (newChunks == null) {
                                newChunks = new LinkedList<>();
                            }
                            newChunks.add(chunk);
                        }
                    }

                    // Replace list (null if no more to process)
                    allChunks = newChunks;

                    if (ourChunkFlags == null) {
                        continue;
                    }

                    // Trace outline of blocks - start from minx, minz going to x+
                    int initialX = minimumX;
                    int initialZ = minimumZ;
                    int currentX = minimumX;
                    int currentZ = minimumZ;
                    Direction direction = Direction.XPLUS;
                    ArrayList<int[]> linelist = new ArrayList<>();
                    linelist.add(new int[]{initialX, initialZ}); // Add start point
                    while ((currentX != initialX) || (currentZ != initialZ) || (direction != Direction.ZMINUS)) {
                        switch (direction) {
                            case XPLUS: // Segment in X+ direction
                                if (!ourChunkFlags.getFlag(currentX + 1, currentZ)) { // Right turn?
                                    linelist.add(new int[]{currentX + 1, currentZ}); // Finish line
                                    direction = Direction.ZPLUS; // Change direction
                                } else if (!ourChunkFlags.getFlag(currentX + 1, currentZ - 1)) { // Straight?
                                    currentX++;
                                } else { // Left turn
                                    linelist.add(new int[]{currentX + 1, currentZ}); // Finish line
                                    direction = Direction.ZMINUS;
                                    currentX++;
                                    currentZ--;
                                }
                                break;
                            case ZPLUS: // Segment in Z+ direction
                                if (!ourChunkFlags.getFlag(currentX, currentZ + 1)) { // Right turn?
                                    linelist.add(new int[]{currentX + 1, currentZ + 1}); // Finish line
                                    direction = Direction.XMINUS; // Change direction
                                } else if (!ourChunkFlags.getFlag(currentX + 1, currentZ + 1)) { // Straight?
                                    currentZ++;
                                } else { // Left turn
                                    linelist.add(new int[]{currentX + 1, currentZ + 1}); // Finish line
                                    direction = Direction.XPLUS;
                                    currentX++;
                                    currentZ++;
                                }
                                break;
                            case XMINUS: // Segment in X- direction
                                if (!ourChunkFlags.getFlag(currentX - 1, currentZ)) { // Right turn?
                                    linelist.add(new int[]{currentX, currentZ + 1}); // Finish line
                                    direction = Direction.ZMINUS; // Change direction
                                } else if (!ourChunkFlags.getFlag(currentX - 1, currentZ + 1)) { // Straight?
                                    currentX--;
                                } else { // Left turn
                                    linelist.add(new int[]{currentX, currentZ + 1}); // Finish line
                                    direction = Direction.ZPLUS;
                                    currentX--;
                                    currentZ++;
                                }
                                break;
                            case ZMINUS: // Segment in Z- direction
                                if (!ourChunkFlags.getFlag(currentX, currentZ - 1)) { // Right turn?
                                    linelist.add(new int[]{currentX, currentZ}); // Finish line
                                    direction = Direction.XPLUS; // Change direction
                                } else if (!ourChunkFlags.getFlag(currentX - 1, currentZ - 1)) { // Straight?
                                    currentZ--;
                                } else { // Left turn
                                    linelist.add(new int[]{currentX, currentZ}); // Finish line
                                    direction = Direction.XMINUS;
                                    currentX--;
                                    currentZ--;
                                }
                                break;
                        }
                    }

                    int sz = linelist.size();
                    double[] x = new double[sz];
                    double[] z = new double[sz];
                    for (int i = 0; i < sz; i++) {
                        int[] line = linelist.get(i);
                        x[i] = (double) line[0] * (double) BLOCKS_PER_CHUNK;
                        z[i] = (double) line[1] * (double) BLOCKS_PER_CHUNK;
                    }

                    // Build information for specific area
                    String markerId = FACTIONS_ + world + "__" + faction.getFactionName() + "__" + markerIndex;

                    TempAreaMarker temp = new TempAreaMarker();
                    temp.label = faction.getFactionName();
                    temp.world = world;
                    temp.x = x;
                    temp.z = z;

                    temp.fillColor = Integer.parseInt(JsonFactionsStorage.getMarkerFillColor().replace("#", ""), 16);
                    temp.lineColor = Integer.parseInt(JsonFactionsStorage.getMarkerLineColor().replace("#", ""), 16);
                    temp.lineOpacity = JsonFactionsStorage.getMarkerLineOpacity();
                    temp.lineWeight = JsonFactionsStorage.getMarkerLineWeight();
                    temp.fillOpacity = JsonFactionsStorage.getMarkerFillOpacity();


                    temp.boost = false;
                    tempAreas.put(markerId, temp);
                    plugin.getLogger().info("tempAreas: " + tempAreas);
                    plugin.getLogger().info("X: " + Arrays.toString(temp.x));
                    plugin.getLogger().info("Z: " + Arrays.toString(temp.z));

                    markerIndex++;
                }

                long elapsedThis = System.nanoTime() - thisStart;
                plugin.getLogger().info("Time taken to create area for " + faction.getFactionName() + ": " + (double) elapsedThis/1000000 + "ms");
                if(facsRan == facsToRun) {
                    if(facsToRun > 0){
                        long elapsedTime = System.nanoTime() - startTime;
                        // updateAreas(this.tempAreas);
                        plugin.getLogger().warning("Time taken to process " + facsRan + " areas: " + (double) elapsedTime / 1000000 + "ms");
                        facsRan = 0;
                        startTime = 0;
                        facsToRun = 0;
                    }

                }
                this.running = false;
            });
        }else{
            stop = false;
        }
    }

    // Find all contiguous blocks, set in target and clear in source
    private void floodFillTarget(TileFlags source, TileFlags destination, int x, int y) {
        int cnt = 0;
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{x, y});

        while (!stack.isEmpty()) {
            int[] nxt = stack.pop();
            x = nxt[0];
            y = nxt[1];
            if (source.getFlag(x, y)) { // Set in src
                source.setFlag(x, y, false); // Clear source
                destination.setFlag(x, y, true); // Set in destination
                cnt++;
                if (source.getFlag(x + 1, y)) {
                    stack.push(new int[]{x + 1, y});
                }
                if (source.getFlag(x - 1, y)) {
                    stack.push(new int[]{x - 1, y});
                }
                if (source.getFlag(x, y + 1)) {
                    stack.push(new int[]{x, y + 1});
                }
                if (source.getFlag(x, y - 1)) {
                    stack.push(new int[]{x, y - 1});
                }
            }
        }
    }

    enum Direction {
        XPLUS, ZPLUS, XMINUS, ZMINUS
    }

}
