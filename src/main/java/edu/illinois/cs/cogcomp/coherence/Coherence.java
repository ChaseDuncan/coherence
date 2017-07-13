package edu.illinois.cs.cogcomp.coherence;

import edu.illinois.cs.cogcomp.coherence.wiki.TitleToCoordMap;
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
import java.util.Collection;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ListIterator;

import java.lang.Math;
import java.lang.Double;
import java.lang.Exception;

public class Coherence {
  public Coherence(QueryDocument qDoc,
                   TitleToCoordMap titleToCoordMap){
    this.doc= qDoc; 
    // initialize title->location map
    // int param determines verbosity of output
    gurobiHook = new GurobiHook(0);
    gurobiHook.setMaximize(false);
    
    // TODO: get this out of here; don't want to
    // construct this for each qdoc.
    this.titleToCoordMap = titleToCoordMap;
  }

  public double calculateWeight(CoherencePair pair){
    String t1 = pair.getCand1().getOrigTitle();
    String t2 = pair.getCand2().getOrigTitle();
    double[] t1coords = titleToCoordMap.getCoord(t1);
    double[] t2coords = titleToCoordMap.getCoord(t2);

    if( t1coords == null || t2coords == null)
      //return Double.NEGATIVE_INFINITY;
      return Double.POSITIVE_INFINITY;
    else{
      //System.out.println("title: " + t1 + "\tlat: " + t1coords[0] + "\tlong: " + t1coords[1]);
      //System.out.println("title: " + t2 + "\tlat: " + t2coords[0] + "\tlong: " + t2coords[1]);

      return distance(t1coords[0],t2coords[0],t1coords[1],t2coords[1]);
    }
  }

  /*
  * stolen from 
  * https://stackoverflow.com/questions/28510115/java-math-toradiansangle-vs-hard-calculated?lq=1
  */
  private double distance(double lat1, double lat2, double lon1, double lon2){

    final int R = 6371; // Radius of the earth

    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
      + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
      * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = R * c * 1000; // convert to meters

    return Math.sqrt(distance);
  }

  public void initializeProblem(){
    initializeCandidateVariables();
    initializeCoherenceVariables();
  }

  public void solve(){
    try {
      gurobiHook.solve();
      updateCandidates(); 
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  private void updateCandidates(){
    System.out.println(doc.text);
    System.out.println("\n\n\n");
    for(Map.Entry<Integer, ImmutablePair<WikiCand,ELMention>> e : varToMention.entrySet()){
      Integer varIdx = (Integer)e.getKey();
      if(gurobiHook.getBooleanValue(varIdx)){
        ImmutablePair<WikiCand,ELMention> p =
          (ImmutablePair<WikiCand,ELMention>)e.getValue();
        updateMention(p.right, p.left);
        //System.out.println(p.right.id + " " + varIdx + " " + p.left.getOrigTitle());
      }
    }
  }

  private void updateMention(ELMention mention, WikiCand cand){
    for(ELMention m : doc.mentions){
      if(m.id == mention.id){
        System.out.println(m.getSurface() + "\t" +
                           m.getCandidates().get(0).getOrigTitle() + "\t" +
                           cand.getOrigTitle());
        // m.setEnWikiTitle(cand.getOrigTitle());
      }
    }
  }

  // add each candidate for each mention in the doc
  // as a boolean variable with weight of 1. this 
  // part of the ILP is solely to enforce the constraint
  // that only 1 candidate can be on for a given mention.
  private void initializeCandidateVariables(){
    for(ELMention m : doc.mentions){
      String type = m.getType();
      if(!type.equals("LOC") && !type.equals("GPE")){
        continue;
      }
      varIdxs.clear();
      for(WikiCand c : m.getCandidates()){
        // every variable has weight of 1
        int varIdx = gurobiHook.addBooleanVariable(1);
        Integer IvarIdx = new Integer(varIdx);
        ImmutablePair<WikiCand,ELMention> candPair =
          new ImmutablePair<WikiCand,ELMention>(c, m);
        varToMention.put(IvarIdx,candPair);
        // mentionToVar.put(candPair,IvarIdx);
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

  // comment
  private void initializeCoherenceVariables(){
    computeWeights();
    addCoherenceVariables();
  }

  private static final double[] 
    RELATION_CONSTRAINT_COEFFCIENTS = new double[] { 2, -1, -1 };

  private void computeWeights(){
    int numCands = 0;
    System.out.println("Computing weights.");
    Set entries =
      varToMention.entrySet();
    List entryList =
      new ArrayList(entries);
    ListIterator it =
      entryList.listIterator();
    while(it.hasNext()){
      Map.Entry entry1 = (Map.Entry)it.next();
      Integer varIdx1 = (Integer)entry1.getKey();
      ImmutablePair<WikiCand,ELMention> candPair1 = 
        (ImmutablePair<WikiCand,ELMention>)entry1.getValue();
      int nextIndex = it.nextIndex();
      ListIterator innerIt = entryList.listIterator(nextIndex);
      numCands+=candPair1.getRight().getCandidates().size();
      while(innerIt.hasNext()){
        Map.Entry entry2 = (Map.Entry)innerIt.next();
        Integer varIdx2 = (Integer)entry2.getKey();
        ImmutablePair<WikiCand,ELMention> candPair2 = 
          (ImmutablePair<WikiCand,ELMention>)entry2.getValue();
        if(!candPair1.getRight().id.equals(candPair2.getRight().id)){
          CoherencePair cp = new CoherencePair(candPair1.getLeft(),
              candPair2.getLeft(),
              candPair1.getRight(),
              candPair2.getRight());
          double weight = calculateWeight(cp);
          // if(weight != Double.NEGATIVE_INFINITY){
          if(true){
            System.out.println(candPair1.getLeft().getOrigTitle() + "\t" +
              candPair2.getLeft().getOrigTitle() + "\t" +
              candPair1.getRight().getSurface() + "\t" +
              candPair2.getRight().getSurface() + "\t" + weight);

            int varIdx = gurobiHook.addBooleanVariable(weight);
            gurobiHook.addLessThanConstraint(new int[]{varIdx,varIdx1,varIdx2}, 
                RELATION_CONSTRAINT_COEFFCIENTS, 0);
          }
        }
      }
    }
  }

  private void addCoherenceVariables(){

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
  LinkedHashMap<Integer, ImmutablePair<WikiCand,ELMention>> varToMention = 
    new LinkedHashMap<Integer, ImmutablePair<WikiCand,ELMention>>();
  private TitleToCoordMap titleToCoordMap = null;
}
