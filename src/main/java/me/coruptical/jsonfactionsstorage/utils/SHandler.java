package me.coruptical.jsonfactionsstorage.utils;

import me.coruptical.jsonfactionsstorage.JsonFactionsStorage;

import java.io.*;

public class SHandler {
    private static JsonFactionsStorage plugin = JsonFactionsStorage.getInstance();

    public byte[] toBytes(Object modelData){
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(modelData);
            return baos.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public Object readBytes(byte[] data){
        try{
            ByteArrayInputStream baip = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(baip);
            return ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }
}
