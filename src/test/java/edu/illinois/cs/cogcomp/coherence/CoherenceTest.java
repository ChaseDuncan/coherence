package edu.illinois.cs.cogcomp.coherence;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import edu.illinois.cs.cogcomp.coherence.wiki.TitleToCoordMap;

import edu.illinois.cs.cogcomp.xlwikifier.datastructures.QueryDocument;
public class CoherenceTest {
  @Test
  public void testCoherenceCtor(){
    System.out.println("Testing Coherence constructor.");
    QueryDocument testDoc = new QueryDocument("test");
    // Coherence coherenceSolver = new Coherence(testDoc);
  }
  @Test
  public void testInitializeProblem(){
    System.out.println("Testing problem initialization.");
    List<QueryDocument> docs = TACExamples.getTACExamples();
    TitleToCoordMap ttcm = 
      new TitleToCoordMap("/shared/corpora/cddunca2/wikidata/wikidata.db");

    for (QueryDocument d : docs){
      Coherence coherenceSolver = new Coherence(d,ttcm);
      coherenceSolver.initializeProblem();
      coherenceSolver.solve();
    }

  }
}
