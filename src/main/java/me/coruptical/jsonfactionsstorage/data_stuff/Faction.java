package me.coruptical.jsonfactionsstorage.data_stuff;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class Faction implements Serializable {

    private int id;
    private Set<FastChunk> chunks;
    private List<String> members;
    private String ownerUUID;
    private String factionName;

    public Faction(int id, String factionName, Set<FastChunk> chunks, List<String> members, String ownerUUID){
        this.id = id;
        this.factionName = factionName;
        this.chunks = chunks;
        this.members = members;
        this.ownerUUID = ownerUUID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<FastChunk> getChunks() {
        return chunks;
    }

    public void setChunks(Set<FastChunk> chunks) {
        this.chunks = chunks;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(String ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public String getFactionName() {
        return factionName;
    }

    public void setFactionName(String factionName) {
        this.factionName = factionName;
    }

    @Override
    public String toString() {
        return "Faction{" +
                "chunks=" + chunks.size() +
                ", members=" + members +
                ", ownerUUID='" + ownerUUID + '\'' +
                ", factionName='" + factionName + '\'' +
                '}';
    }
}
