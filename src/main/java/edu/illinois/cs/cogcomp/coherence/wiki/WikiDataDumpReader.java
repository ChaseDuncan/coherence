package edu.illinois.cs.cogcomp.coherence.wiki;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream; 
import java.io.BufferedReader;
import java.io.File; 
import java.io.FileInputStream; 
import java.io.InputStream;
import java.io.StringReader;
import java.io.IOException;
import java.lang.NullPointerException;
import java.io.InputStreamReader;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonString;

import java.nio.charset.StandardCharsets;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import org.mapdb.DBMaker;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.BTreeMap;

/*
   Parses WikiData dump and produces a MapDB
   database that maps normalized titles to
   their corresponding globe coordinates in
   WikiData.
 */
public class WikiDataDumpReader {
  private void parseWikiData(String wikidata, String dbFile){
    try { 
      InputStream is = new BufferedInputStream(new FileInputStream(wikidata));
      BZip2CompressorInputStream bz = new BZip2CompressorInputStream(is); 
      BufferedReader br = 
        new BufferedReader(new InputStreamReader(bz, StandardCharsets.UTF_8));

      // first line of file is simply "["
      String line = br.readLine();

      DB db = DBMaker
        .fileDB(dbFile)
        .transactionEnable()
        .make();
      BTreeMap<String, double[]> map = db.treeMap("globe-coordinates")
        .keySerializer(Serializer.STRING)
        .valueSerializer(Serializer.DOUBLE_ARRAY)
        .createOrOpen();

      while((line = br.readLine()) != null && !line.equals("]")){

        JsonReader jsonReader = 
          Json.createReader(new StringReader(line));
        Map topLevel = jsonReader.readObject();
        jsonReader.close();

        // sitelinks->enwiki->title
        // claims->P625->mainsnak->datavalue->{"latitude":x, "longitude":y, ...}
        String title;
        try{
          title = ((JsonString)((Map)((Map)topLevel.get("sitelinks"))
                .get("enwiki"))
              .get("title"))
            .getString();
        } catch (NullPointerException e){
          continue;
        }

        JsonArray P625 = (JsonArray)((Map)topLevel.get("claims"))
          .get("P625");
        JsonObject jo = null;
        Map mainSnak = null;
        if(P625 != null && (jo = P625.getJsonObject(0)) != null){
          mainSnak = (Map)jo.get("mainsnak");

          Map dataValue;
          try{
            dataValue = (Map)mainSnak.get("datavalue");        
          } catch (NullPointerException e){
            logger.debug("### mainsnak is null in {}.\n{}", title, line);
            continue;
          }

          Map valueMap;
          try{
            valueMap = (Map)dataValue.get("value");
          } catch (NullPointerException e){
            logger.debug("### datavalue is null in {}.\n{}", title, line);
            continue;
          }

          double lat;
          double lon; 
          try{
            lat = Double.parseDouble(((JsonValue)valueMap.get("latitude")).toString());
            lon = Double.parseDouble(((JsonValue)valueMap.get("longitude")).toString());
            String normedTitle = normTitle(title);
            map.put(normedTitle, new double[]{lat,lon});
            System.out.println("Processed title " + normedTitle);
          } catch (NullPointerException e){
            logger.debug("### valueMap is null in {}.\n{}", title, line);
          }
        }
      }

      db.commit();
      db.close(); 

    } catch (IOException e) { 
      e.printStackTrace();
    }
  }

  /*
     @param  args  0 WikiData .bz2 file.
     1 path to .db file 
   */
  public static void main(String[] args){
    String wikidata = args[0];
    String dbFile   = args[1];
    WikiDataDumpReader reader = new WikiDataDumpReader();
    reader.parseWikiData(wikidata, dbFile);
  }

  /*
     Normalizes titles by replacing " " with "_"
     and making entire string lowercase, e.g.
     "Barack Obama"=>"barack_obama".
     @param  title the string to normalize
     @return       the normalized string
   */
  private static String normTitle(String title){
    return title.replaceAll(" ", "_").toLowerCase();
  }

  private static Logger logger = LoggerFactory.getLogger(WikiDataDumpReader.class);
}

/*
   For reference, this is field in the WikiData JSON
   string that holds the information about global coordinates.
   Global coordinates are propery 625 (P625).

   "P625":
   [{"mainsnak":
   {"snaktype":"value",
   "property":"P625",
   "datavalue":
   {"value":
   {
   "latitude":57,
   "longitude":-5,
   "altitude":null,
   "precision":1.0e-5,
   "globe":"http:\/\/www.wikidata.org\/entity\/Q2"
   },
   "type":"globecoordinate"
   },
   "datatype":"globe-coordinate"
   },
   "type":"statement",
   "id":"q22$327258B4-7023-4189-984B-E3A4A5E74B92",
   "rank":"normal",
   "references":
   [{"hash":"732ec1c90a6f0694c7db9a71bf09fe7f2b674172",
   "snaks":
   {"P143":
   [{"snaktype":"value",
   "property":"P143",
   "datavalue":
   {"value":
   {"entity-type":"item",
   "numeric-id":10000,"id":"Q10000"},
   "type":"wikibase-entityid"},
   "datatype":"wikibase-item"}
   ]},
   "snaks-order":["P143"]
   }]
   }
   ]
 */

