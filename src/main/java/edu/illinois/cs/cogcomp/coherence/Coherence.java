package edu.illinois.cs.cogcomp.coherence;

import edu.illinois.cs.cogcomp.xlwikifier.datastructures.QueryDocument;
import edu.illinois.cs.cogcomp.xlwikifier.datastructures.ELMention;

import edu.illinois.cs.cogcomp.infer.ilp.GurobiHook;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;// not sure I need this
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.xlwikifier.datastructures.WikiCand;
import edu.illinois.cs.cogcomp.xlwikifier.datastructures.ELMention;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;

public class Coherence {
  public Coherence(QueryDocument qDoc){
    this.doc= qDoc; 
    // initialize title->location map
    // int param determines verbosity of output
    this.gurobiHook = new GurobiHook(2);
  }
    
  public void initializeProblem(){
    initializeCandidateVariables();
    initializeCoherenceVariables();
  }

  // add each candidate for each mention in the doc
  // as a boolean variable with weight of 1. this 
  // part of the ILP is solely to enforce the constraint
  // that only 1 candidate can be on for a given mention.
  private void initializeCandidateVariables(){
      for(ELMention m : doc.mentions){
      varIdxs.clear();
      for(WikiCand c : m.getCandidates()){
        int varIdx = gurobiHook.addBooleanVariable(1);// every variable has weight of 1
        varToMention.put(new Integer(varIdx),new ImmutablePair<WikiCand,ELMention>(c, m));
        varIdxs.add(varIdx);
      }
      // all constraint vars coeffs are 1
      double[] coeffs = new double[varIdxs.size()];
      Arrays.fill(coeffs,1.0);
      // some jiggerypokery here in order to pass an int[]
      // made from ArrayList<Integer>
      gurobiHook.addEqualityConstraint(varIdxs.stream().mapToInt(i->i).toArray(),
                                       coeffs,
                                       1.0);
    }   
  }

  private void initializeCoherenceVariables(){
    for(ELMention m : doc.mentions){

    }
  }

  public QueryDocument getEvaluation(){
    return doc;
  }

  public static void main(String[] args){
    List<QueryDocument> qDocs = TACExamples.generateTACExamples();
    TACExamples.printMentions(qDocs);
  }

  QueryDocument doc;
  GurobiHook gurobiHook = null;
  private ArrayList<Integer> varIdxs = new ArrayList<Integer>();
  // maps gurobi variable idx to the ELMention
  // and the candidate for which it is a referent
  LinkedHashMap<Integer, Pair<WikiCand,ELMention>> varToMention = 
    new LinkedHashMap<Integer, Pair<WikiCand,ELMention>>();

}
