package edu.illinois.cs.cogcomp.coherence;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.xlwikifier.datastructures.QueryDocument;
public class CoherenceTest {
  @Test
  public void testCoherenceCtor(){
    System.out.println("Testing Coherence constructor.");
    QueryDocument testDoc = new QueryDocument("test");
    Coherence coherenceSolver = new Coherence(testDoc);
  }
  @Test
  public void testInitializeProblem(){
    System.out.println("Testing problem initialization.");
    List<QueryDocument> docs = TACExamples.getTACExamples();
    Coherence coherenceSolver = new Coherence(docs.get(0));
    coherenceSolver.initializeProblem();
  }
}
