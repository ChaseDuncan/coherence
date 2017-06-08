package edu.illinois.cs.cogcomp.locationcoherence;

import org.junit.Test;

import edu.illinois.cs.cogcomp.xlwikifier.datastructures.QueryDocument;
public class LocationCoherenceTest {
  @Test
  public void testLocationCoherence(){
    System.out.println("great comfort.");
    QueryDocument testDoc = new QueryDocument("test");
    LocationCoherence lc = new LocationCoherence(testDoc);
  }
}
