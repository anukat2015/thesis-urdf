package urdf.reasoner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.UGroundedHardRule;
import urdf.api.ULineageAbstract;
import urdf.api.ULineageAnd;
import urdf.api.ULineageOr;
import urdf.api.USoftRule;

// A collection of static sampling methods for PW-confidences
public class Sampling {

  public static double TOTAL_CONF;

  public static double NUM_SAMPLES = 1 << 14;

  private Sampling() {
  }

  public static void getConfAll(ArrayList<ULineageAnd> resultLineage) {
    calcConf(new ArrayList<ULineageAbstract>(resultLineage));
  }

  public static void getConfAll(UFactSet facts) {
    ArrayList<ULineageAbstract> lineage = new ArrayList<ULineageAbstract>();
    for (UFact f : facts)
      lineage.add(f.getLineage());
    calcConf(lineage);
  }
  
  private static void calcConf(ArrayList<ULineageAbstract> lineage) {
    //System.out.println("BASE:" + lineage.size());
    
    for (ULineageAbstract l : lineage) {

      HashSet<ULineageAnd> changeableBaseLineage = new HashSet<ULineageAnd>();
      HashSet<ULineageAnd> constant = new HashSet<ULineageAnd>();
      HashSet<USoftRule> softRules = new HashSet<USoftRule>();
      HashSet<UFactSet> hardRules = new HashSet<UFactSet>();

      getBaseLineage(l, changeableBaseLineage, softRules, constant, new HashSet<ULineageAbstract>());
      //System.out.println("BASE: " + changeableBaseLineage);
      
      int PWs = 1;
      for (ULineageAnd l2 : changeableBaseLineage) {
        //System.out.println("BASE: " + l2.getGroundedRule().getHead());
        UGroundedHardRule h = l2.getGroundedRule().getHead().getGroundedHardRule();
        if (h != null)
          hardRules.add(l2.getGroundedRule().getHead().getGroundedHardRule());
      }
      //System.out.println(hardRules);
      
      for (UFactSet hardRule : hardRules) {
        int i = 1;
        for (UFact f_i : hardRule)
          for (ULineageAbstract l3 : f_i.getLineage().getChildren())
            if (changeableBaseLineage.contains(l3))
              i++;
        PWs *= i;
      }
      //System.out.println("PWS: " + (PWs * (1 << softRules.size())));

      TOTAL_CONF = 0.0;
      if (PWs * 1 << softRules.size() <= NUM_SAMPLES)
        sampleAllConf(l, new ArrayList<ULineageAnd>(changeableBaseLineage), new ArrayList<USoftRule>(softRules), 0);
      else
        sampleRandomConf(l, new ArrayList<ULineageAnd>(changeableBaseLineage), new ArrayList<USoftRule>(softRules));
      normalize(l, new HashSet<ULineageAbstract>());

      for (ULineageAnd c : constant)
        c.tmp = 0;
    }
  }

  // might create many impossible worlds
  // might sample the same world twice
  private static void sampleRandomConf(ULineageAbstract root, ArrayList<ULineageAnd> changeableBaseLineage, ArrayList<USoftRule> softRules) {
    Random r = new Random(System.currentTimeMillis());
    for (int k = 0; k < NUM_SAMPLES; k++) {
      // create next possible world
      for (ULineageAnd l : changeableBaseLineage)
        l.tmp = (!hasTrueFactinCompetitorSet(l, changeableBaseLineage) && r.nextBoolean()) ? 1 : -1;
      for (USoftRule s : softRules)
        s.tmp = r.nextBoolean() ? 1 : -1;
      double conf = evalConf(changeableBaseLineage, softRules);
      if (conf > 0.0 && eval(root)) {
        addConf(root, conf);
        TOTAL_CONF += conf;
      } else {
        // set all tmp = 0;
        addConf(root, 0.0);
      }
    }
  }

  private static void sampleAllConf(ULineageAbstract root, ArrayList<ULineageAnd> changeableBaseLineage, ArrayList<USoftRule> softRules, int i) {
    // creates all possible worlds recursively
    if (i < changeableBaseLineage.size()) {
      ULineageAnd l_i = changeableBaseLineage.get(i);
      if (!hasTrueFactinCompetitorSet(l_i, changeableBaseLineage)) {
        l_i.tmp = 1;
        sampleAllConf(root, changeableBaseLineage, softRules, i + 1);
      }
      l_i.tmp = -1;
      sampleAllConf(root, changeableBaseLineage, softRules, i + 1);
      l_i.tmp = 0;
    } else if (i - changeableBaseLineage.size() < softRules.size()) {
      USoftRule r = softRules.get(i - changeableBaseLineage.size());
      r.tmp = 1;
      sampleAllConf(root, changeableBaseLineage, softRules, i + 1);
      r.tmp = -1;
      sampleAllConf(root, changeableBaseLineage, softRules, i + 1);
      r.tmp = 0;
    } else {
      //System.out.println("PW: " + changeableBaseLineage + " " + evalConf(changeableBaseLineage, softRules));
      double conf = evalConf(changeableBaseLineage, softRules);
      boolean b = eval(root);
      //System.out.println("possible world: "+b+" "+conf);
      //for (USoftRule r : softRules){
      //	System.out.println(r.tmp+" "+r);
      //}
      //System.out.println(root.toString(0));// tmp is set
      if (conf > 0.0 && b) {
        addConf(root, conf);
        TOTAL_CONF += conf;
      } else {
        // set all tmp = 0;
        addConf(root, 0.0);
      }
      //System.out.println(root.toString(0));// conf is updated
    }
  }

  private static boolean hasTrueFactinCompetitorSet(ULineageAnd l_i, ArrayList<ULineageAnd> changeableBaseLineage) {
    UGroundedHardRule h = l_i.getGroundedRule().getHead().getGroundedHardRule();
    if (h != null)
      for (UFact f_l : h)
        for (ULineageAbstract l_j : f_l.getLineage().getChildren())
          if (l_j != l_i && changeableBaseLineage.contains(l_j) && l_j.tmp == 1)
            return true;
    return false;
  }

  private static void normalize(ULineageAbstract root, HashSet<ULineageAbstract> seen) {
    if (seen.contains(root))
      return;
    seen.add(root);
    if (TOTAL_CONF>0.0){
    	root.setConf(root.getConf() / TOTAL_CONF);
    }else{
    	root.setConf(0.0);
    }
    for (ULineageAbstract l : root.getChildren())
      normalize(l, seen);
    if (root instanceof ULineageOr) {
      ULineageOr or = (ULineageOr) root;
      if (or.getFact() != null && or.getFact().getGroundedHardRule() != null) {
        for (UFact f : or.getFact().getGroundedHardRule()) {
          if (f != or.getFact()) {
            normalize(f.getLineage(), seen);
          }
        }
      }
    }
  }

  private static void getBaseLineage(ULineageAbstract root, HashSet<ULineageAnd> baseLineage, HashSet<USoftRule> softRules, HashSet<ULineageAnd> constant,
      HashSet<ULineageAbstract> seen) {
    if (seen.contains(root))
      return;

    seen.add(root);
    root.setConf(0.0);

    // leafs
    if (root instanceof ULineageAnd) {
      if (root.getChildren().isEmpty()) {
        // it is from DB or Ar
        if (((ULineageAnd) root).getGroundedRule().getHead().getBaseConfidence() == 1.0) {
          root.tmp = 1;
          constant.add((ULineageAnd) root);
        } else if (((ULineageAnd) root).getGroundedRule().getHead().getBaseConfidence() == 0.0) {
          root.tmp = -1;
          constant.add((ULineageAnd) root);
        } else
          baseLineage.add((ULineageAnd) root);
      }
      if (((ULineageAnd) root).getRule() != null) {
        softRules.add(((ULineageAnd) root).getRule());
      }
    }

    // children
    for (ULineageAbstract child : root.getChildren()) {
      getBaseLineage(child, baseLineage, softRules, constant, seen);
    }

    // hard rules
    if (root instanceof ULineageOr) {
      ULineageOr or = (ULineageOr) root;
      if (or.getFact() != null && or.getFact().getGroundedHardRule() != null) {
        for (UFact f : or.getFact().getGroundedHardRule()) {
          if (f != or.getFact()) {
            getBaseLineage(f.getLineage(), baseLineage, softRules, constant, seen);
          }
        }
      }
    }
  }

  // checks all nodes where visited == true, sets visited to false
  private static void addConf(ULineageAbstract root, double currentWorld) {
    if (!root.visited)
      return;
    root.visited = false;
    if (root.tmp == 1)
      root.setConf(currentWorld + root.getConf());
    if (!(root instanceof ULineageAnd && root.getChildren().isEmpty()))
      root.tmp = 0;
    for (ULineageAbstract child : root.getChildren()) {
      addConf(child, currentWorld);
    }
    if (root instanceof ULineageOr && ((ULineageOr) root).getFact() != null && ((ULineageOr) root).getFact().getGroundedHardRule() != null) {
      for (UFact f : ((ULineageOr) root).getFact().getGroundedHardRule()) {
        if (f != ((ULineageOr) root).getFact()) {
          addConf(f.getLineage(), currentWorld);
        }
      }
    }
  }

  // checks all nodes where visited == false, sets visited to true
  // returns false <=> impossible world
  // sets root.tmp==1 <=> lineage node is true in world
  // sets root.tmp==-1 <=> lineage node is false in world
  private static boolean eval(ULineageAbstract root) {
    if (root.visited) { // Lineage can be a DAG
      return true;
    }
    root.visited = true;

    boolean sat = false;
    boolean possibleWorld = true;
    ULineageOr or = null;

    if (root instanceof ULineageOr) {
      sat = false;
      or = (ULineageOr) root;
      for (ULineageAbstract child : or.getChildren()) {
      	possibleWorld = eval(child) & possibleWorld;
        sat = child.tmp == 1 || sat;
      }
    } else if (root instanceof ULineageAnd) {
      ULineageAnd and = (ULineageAnd) root;
      if (and.getChildren().isEmpty()) {
        sat = and.tmp == 1;
      } else {
        sat = true;
        if (and.getRule() != null)
          sat = sat && and.getRule().tmp == 1;
        for (ULineageAbstract child : and.getChildren()) {
        	possibleWorld = eval(child) & possibleWorld;
          sat = child.tmp == 1 && sat;
        }
      }
    } else
      System.err.println("new lineage class??");

    if (sat) {
      root.tmp = 1;
    } else
      root.tmp = -1;

    // check hard rule
    if (root instanceof ULineageOr && or.getFact() != null && or.getFact().getGroundedHardRule() != null) {
      int numTrue = 0;
      if (sat)
        numTrue++;
      for (UFact f : or.getFact().getGroundedHardRule()) {
        if (f != or.getFact()) {
          if (!eval(f.getLineage()))
            return false;
          if (f.getLineage().tmp == 1) {
            numTrue++;
            if (numTrue > 1)
              return false;
          }
        }
      }
    }

    return possibleWorld;
  }

  private static double evalConf(ArrayList<ULineageAnd> lineage, ArrayList<USoftRule> softRules) {
    double w = 1.0;
    for (ULineageAnd l : lineage) {
      //System.out.println("\tF " + l + " " + l.getGroundedRule().getHead().getBaseConfidence());
      if (l.tmp == 0) {
        System.err.println("TRUTH ASSIGNMENT IS 'UNKNOWN': " + l);
        return 0;
      }
      if (l.tmp == 1)
        w *= l.getGroundedRule().getHead().getBaseConfidence();
      else
        w *= (1 - l.getGroundedRule().getHead().getBaseConfidence());
    }
    for (USoftRule s : softRules) {
      //System.out.println("\tS " + s + " " + s.getWeight());
      if (s.tmp == 0) {
        System.err.println("TRUTH ASSIGNMENT IS 'UNKNOWN': " + s);
        return 0;
      }
      if (s.tmp == 1)
        w *= s.getWeight();
      else
        w *= (1 - s.getWeight());
    }
    return w;
  }
}
