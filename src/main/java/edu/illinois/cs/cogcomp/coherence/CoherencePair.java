package edu.illinois.cs.cogcomp.coherence;

import edu.illinois.cs.cogcomp.xlwikifier.datastructures.WikiCand;
import edu.illinois.cs.cogcomp.xlwikifier.datastructures.ELMention;

class CoherencePair{
  public CoherencePair(WikiCand cand1,
                       WikiCand cand2,
                       ELMention men1,
                       ELMention men2)
  {
    this.cand1 = cand1;
    this.cand2 = cand2;
    this.men1  = men1;
    this.men2  = men2;
  }

  WikiCand getCand1(){ return this.cand1; }
  WikiCand getCand2(){ return this.cand2; }
  ELMention getMen1(){ return this.men1; }
  ELMention getMen2(){ return this.men2; }

  void setCand2(WikiCand cand2){
    this.cand2 = cand2;
  }
  void setCand1(WikiCand cand1){
    this.cand1 = cand1;
  }
  void setMen2(ELMention men2){
    this.men2 = men2;
  }
  void setMen1(ELMention men1){
    this.men1 = men1;
  }
  void setCands(WikiCand cand1, WikiCand cand2){
    this.cand1 = cand1;
    this.cand2 = cand2;
  }
  private WikiCand cand1;
  private WikiCand cand2;
  private ELMention men1;
  private ELMention men2;
}
