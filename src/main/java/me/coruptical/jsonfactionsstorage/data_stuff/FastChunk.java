package me.coruptical.jsonfactionsstorage.data_stuff;

import java.io.Serializable;

public class FastChunk implements Serializable {
    private int x;
    private int z;
    private String chunkID;
    private int chunkOwnerID;

    public FastChunk(int coordX, int coordZ, int chunkOwnerID){
        this.x = coordX;
        this.z = coordZ;
        this.chunkOwnerID = chunkOwnerID;
        this.chunkID = coordX + ";" + coordZ;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getChunkID() {
        return chunkID;
    }

    public void setChunkID(String chunkID) {
        this.chunkID = chunkID;
    }

    public int getChunkOwnerID() {
        return chunkOwnerID;
    }

    public void setChunkOwnerID(int chunkOwnerID) {
        this.chunkOwnerID = chunkOwnerID;
    }

    @Override
    public String toString() {
        return "FastChunk{" +
                "x=" + x +
                ", z=" + z +
                ", chunkID='" + chunkID + '\'' +
                ", chunkOwner='" + chunkOwnerID + '\'' +
                '}';
    }
}
