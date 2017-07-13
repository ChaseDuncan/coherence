package edu.illinois.cs.cogcomp.coherence.wiki;

import org.mapdb.DBMaker;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.BTreeMap;

import java.io.FileWriter;
import java.io.IOException;

import java.lang.NullPointerException;

public class TitleToCoordMap{
  public TitleToCoordMap(String dbPath){
    db = DBMaker
      .fileDB(dbPath)
      .closeOnJvmShutdown()
      .make();
    map = db.treeMap("globe-coordinates")
      .keySerializer(Serializer.STRING)
      .valueSerializer(Serializer.DOUBLE_ARRAY)
      .open();
    try{
      fw = new FileWriter("cannotfind");
    } catch (IOException e){
      e.printStackTrace();
    }
  }

  public double[] getCoord(String title){
    return map.get(title);
  }

  DB db = null;
  BTreeMap<String, double[]> map = null;
  FileWriter fw = null;
}

