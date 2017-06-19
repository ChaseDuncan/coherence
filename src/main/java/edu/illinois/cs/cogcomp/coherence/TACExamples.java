package edu.illinois.cs.cogcomp.coherence;

import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.xlwikifier.ConfigParameters;
import edu.illinois.cs.cogcomp.xlwikifier.CrossLingualWikifier;
import edu.illinois.cs.cogcomp.xlwikifier.CrossLingualWikifierManager;
import edu.illinois.cs.cogcomp.xlwikifier.datastructures.QueryDocument;
import edu.illinois.cs.cogcomp.xlwikifier.datastructures.ELMention;
import edu.illinois.cs.cogcomp.xlwikifier.datastructures.WikiCand;

import edu.illinois.cs.cogcomp.xlwikifier.evaluation.TACDataReader;
import edu.stanford.nlp.trees.GrammaticalRelation;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TACExamples{
  private final String SEPARATOR = "\t";
  private final String END_OF_LINE = "\n";

  private final static String QDOC_DATA_FILEPATH = "/home/cddunca2/EDL/coherence/data/qdocdata.csv";

  public static List<QueryDocument> getTACExamples(){
    File qDocDataFile = new File(QDOC_DATA_FILEPATH);
    List<QueryDocument> docs = null; 
    if(!qDocDataFile.exists())
      new TACExamples().createQDocDataFile();
    else
      System.out.println("TAC Examples exist. Reading from file.");
    
    return readTACExamplesFromFile();
  }

  private static List<QueryDocument> readTACExamplesFromFile(){
    HashMap<String,QueryDocument> docMap = new HashMap<String,QueryDocument>();
    try{
      File qDocDataFile = new File(QDOC_DATA_FILEPATH);
      FileReader fileReader = new FileReader(qDocDataFile);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      String line;
      while((line = bufferedReader.readLine()) != null){
        List<String> lineSplit = Arrays.asList(line.split("\\s*\t\\s*"));
        String docId = lineSplit.get(0);
        QueryDocument qd = docMap.get(docId);
        // add QueryDocument to list if it isn't there already
        if(null == qd){
          qd = new QueryDocument(docId);
          docMap.put(docId,qd);
        }
        ListIterator<String> it = lineSplit.listIterator(1);
        String mentionSurface = it.next();
        String mentionId = it.next();
        ELMention m = new ELMention(mentionId,mentionSurface,docId);
        List<WikiCand> candidates = new ArrayList<WikiCand>();
        while(it.hasNext()){
          candidates.add(new WikiCand(it.next(), 
                            Double.parseDouble(it.next())));
        }
        m.setCandidates(candidates);
        qd.mentions.add(m);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    List<QueryDocument> docs = new ArrayList<QueryDocument>();
    Iterator it = docMap.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry kv = (Map.Entry)it.next();
      docs.add((QueryDocument)kv.getValue());
    }
    return docs;
  }

  public static List<QueryDocument> generateTACExamples(){

    String config = "config/xlwikifier-tac.config";

    try {
      ConfigParameters.setPropValues(config);
    } catch (IOException e) {
      e.printStackTrace();
    }

    List<QueryDocument> ret = new ArrayList<>();
    TACDataReader reader = new TACDataReader(false);

    List<QueryDocument> docs = null;
    try {
      docs = reader.readDocs("examples", "en");
    } catch (IOException e) {
      e.printStackTrace();
    }

    List<ELMention> mentions = reader.read2016EnglishEvalGoldNAM();
    for(QueryDocument doc: docs){
      for(ELMention m: mentions){
        if(m.getDocID().equals(doc.getDocID())){
          Pair<Integer, Integer> plainoff = 
            doc.getXmlhandler().getPlainOffsets(m.getStartOffset(), m.getEndOffset());
          if(plainoff != null){
            m.setStartOffset(plainoff.getFirst());
            m.setEndOffset(plainoff.getSecond());
            doc.mentions.add(m);
          }
        }
      }
    }

    CrossLingualWikifier xlwikifier = 
      CrossLingualWikifierManager.buildWikifierAnnotator(Language.English, config);

    for(QueryDocument doc: docs){
      System.out.println(doc.getDocID());
      // wikification
      xlwikifier.annotate(doc);
      // for each mention, print its candidates
    }
    // return ret; why did CT do this?
    return docs;
  }

  public static void printMentions(List<QueryDocument> docs){
    for(QueryDocument doc: docs){
      for(ELMention m: doc.mentions){
        System.out.println(m.getSurface()+" "+
            m.getStartOffset()+" "+
            m.getEndOffset()+" "+
            m.gold_mid);
        for(WikiCand cand: m.getCandidates()){
          System.out.println("\t"+cand.orig_title+" "+
              cand.getTitle()+" "+cand.score);
        }
      }
    }
  }

  private void createQDocDataFile(){
    List<QueryDocument> examples = generateTACExamples();
    StringBuilder sb = new StringBuilder();
    for (QueryDocument ex : examples){
      String id = ex.getDocID(); 
      // clear buffer
      sb.setLength(0);
      for(ELMention m : ex.mentions){
        sb.append(id);
        sb.append(SEPARATOR);
        sb.append(m.getSurface());
        sb.append(SEPARATOR);
        sb.append(m.id);
        sb.append(SEPARATOR);
          for(WikiCand c : m.getCandidates()){
            sb.append(c.getTitle());
            sb.append(SEPARATOR);
            sb.append(c.getScore());
            sb.append(SEPARATOR);
          }
        sb.append(END_OF_LINE);
      }
    }
    try{
      File qDocDataFile = new File(QDOC_DATA_FILEPATH);
      FileWriter fw = new FileWriter(QDOC_DATA_FILEPATH);
      fw.write(sb.toString());
      fw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
