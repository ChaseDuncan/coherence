package edu.illinois.cs.cogcomp.locationcoherence;

import edu.illinois.cs.cogcomp.xlwikifier.datastructures.QueryDocument;
import edu.illinois.cs.cogcomp.xlwikifier.datastructures.ELMention;

import edu.illinois.cs.cogcomp.infer.ilp.GurobiHook;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;// not sure I need this
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;


public class LocationCoherence {

  public LocationCoherence(QueryDocument qDoc){
    this.doc= qDoc; 

    // initialize title->location map

    // int param determines verbosity of output
    GurobiHook gurobiHook = new GurobiHook(2);
  }

  public void evalLocCoherence(){
    for(ELMention m : this.doc.mentions){

    }
  }

  public QueryDocument getEvaluation(){
    return doc;
  }

  public static void main(String[] args){
    System.out.println("Hello world.");
  }

  QueryDocument doc;
}
