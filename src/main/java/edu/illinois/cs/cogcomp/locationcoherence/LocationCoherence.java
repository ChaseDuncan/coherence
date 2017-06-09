package edu.illinois.cs.cogcomp.locationcoherence;

import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.xlwikifier.ConfigParameters;
import edu.illinois.cs.cogcomp.xlwikifier.CrossLingualWikifier;
import edu.illinois.cs.cogcomp.xlwikifier.CrossLingualWikifierManager;
import edu.illinois.cs.cogcomp.xlwikifier.datastructures.QueryDocument;
import edu.illinois.cs.cogcomp.xlwikifier.datastructures.ELMention;

import edu.illinois.cs.cogcomp.infer.ilp.GurobiHook;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;// not sure I need this
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.xlwikifier.datastructures.WikiCand;
import edu.illinois.cs.cogcomp.xlwikifier.evaluation.TACDataReader;
import edu.stanford.nlp.trees.GrammaticalRelation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LocationCoherence {

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
          Pair<Integer, Integer> plainoff = doc.getXmlhandler().getPlainOffsets(m.getStartOffset(), m.getEndOffset());
          if(plainoff != null){
              m.setStartOffset(plainoff.getFirst());
              m.setEndOffset(plainoff.getSecond());
              doc.mentions.add(m);
          }
        }
      }
    }

    CrossLingualWikifier xlwikifier = CrossLingualWikifierManager.buildWikifierAnnotator(Language.English, config);

    for(QueryDocument doc: docs){

      System.out.println(doc.getDocID());

      // wikification
      xlwikifier.annotate(doc);

      // for each mention, print its candidates
      for(ELMention m: doc.mentions){
        System.out.println(m.getSurface()+" "+m.getStartOffset()+" "+m.getEndOffset()+" "+m.gold_mid);
        for(WikiCand cand: m.getCandidates()){
          System.out.println("\t"+cand.orig_title+" "+cand.getTitle()+" "+cand.score);
        }
      }
    }
    return ret;
  }

  public LocationCoherence(QueryDocument qDoc){
    this.doc= qDoc; 

    // initialize title->location map

    // int param determines verbosity of output
//    GurobiHook gurobiHook = new GurobiHook(2);
  }

  public void evalLocCoherence(){
    for(ELMention m : this.doc.mentions){

    }
  }

  public QueryDocument getEvaluation(){
    return doc;
  }

  public static void main(String[] args){
      generateTACExamples();
  }

  QueryDocument doc;
}
