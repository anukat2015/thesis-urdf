package urdf.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import urdf.api.UArgument;
import urdf.api.UBindingSet;
import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.UGroundedHardRule;
import urdf.api.UGroundedSoftRule;
import urdf.api.UHardRule;
import urdf.api.ULineageAnd;
import urdf.api.ULiteral;
import urdf.api.UQuery;
import urdf.api.URule;
import urdf.api.USoftRule;
import urdf.reasoner.MAXSAT;
import urdf.reasoner.Sampling;
import urdf.reasoner.URDF;
import urdf.tools.RuleParser;

public class URDF_main {

  public static int EXPANSION_MODE = 0;

  public static int NOISE_LEVEL_RULES = 0, NOISE_LEVEL_FACTS = 0, DUMMY_POOL = 1000;

  public static void main(String[] args) throws Exception {

    PrintStream Sysout = new PrintStream(System.out, true, "UTF-8"); // for UTF-8 console output

    ArrayList<USoftRule> softRules = new ArrayList<USoftRule>();
    ArrayList<UHardRule> hardRules = new ArrayList<UHardRule>();
    ArrayList<UQuery> queries = new ArrayList<UQuery>();

    RuleParser.parseAll(new URDF_main().getClass().getClassLoader().getResource("rules.txt").openStream(), softRules, hardRules, queries);

    int i, j;
    long time;

    List<UFactSet> resultFacts = new ArrayList<UFactSet>();
    List<UBindingSet> resultBindings = new ArrayList<UBindingSet>();
    ArrayList<ULineageAnd> resultLineage = new ArrayList<ULineageAnd>();

    URDF urdf = new URDF(args[0], softRules, hardRules);

    // DEMO MODE: Ground all atoms and rules, display lineage, warm up cache
    long totalTime = System.currentTimeMillis();

    for (i = 1; i < queries.size(); i++) {
      UQuery query = queries.get(i);

      // Ground the query top-down
      Sysout.println("\nQUERY " + i + "\t\t\t" + query);

      time = System.currentTimeMillis();
      urdf.sortBySelectivity(query, new UBindingSet());
      Sysout.println("SORT [" + (System.currentTimeMillis() - time) + "ms]\t\t" + query.size() + " ATOMS");

      time = System.currentTimeMillis();
      urdf.ground(query, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, new HashSet<UArgument>(), new UFactSet(), null, 0, true, true,
          new ULineageAnd(null), resultLineage);
      Sysout.println("GROUND [" + (System.currentTimeMillis() - time) + "ms]\t\t" + resultFacts.size() + " DISTINCT RESULT SETS");

      time = System.currentTimeMillis();
      urdf.invertRules(urdf.globalDependencyGraph);
      Sysout.println("INV-RULES [" + (System.currentTimeMillis() - time) + "ms]\t\t" + urdf.softRuleGroundings.size() + " GROUNDED SOFT RULES, "
          + urdf.hardRuleGroundings.size() + " GROUNDED HARD RULES, " + urdf.globalDependencyGraph.size() + " FACTS");

      time = System.currentTimeMillis();
      new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).processMAXSAT();
      Sysout.println("MAX-SAT [" + (System.currentTimeMillis() - time) + "ms]");

      // This is a simple PW-based sampling algorithm
      time = System.currentTimeMillis();

      Sampling.getConfAll(urdf.globalDependencyGraph);
      Sampling.getConfAll(resultLineage);

      Sysout.println("PW-CONF [" + (System.currentTimeMillis() - time) + "ms]");
      Sysout.println("SLD STEPS: " + URDF.SLD_steps);

      j = 0;
      for (ULineageAnd result : resultLineage) {
        Sysout.println("\nRESULT [" + i + "|" + (j + 1) + "/" + resultLineage.size() + "]\t\t");
        Sysout.print(result.toString(-1));
        // result.writeGraphVizFile(i); // write a graph viz file including the hard rule lineage
        j++;
      }

      Sysout.println();

      j = 0;
      for (UFactSet hardRule : urdf.hardRuleGroundings) {
        // if (hardRule.size() > 1)
        Sysout.println("HARDRULE[" + i + "|" + (j + 1) + "/" + urdf.hardRuleGroundings.size() + "]\t" + hardRule);
        j++;
      }

      j = 0;
      for (UGroundedSoftRule softRule : urdf.softRuleGroundings) {
        // if (softRule.size() > 0)
        Sysout.println("SOFTRULE[" + i + "|" + (j + 1) + "/" + urdf.softRuleGroundings.size() + "]\t" + softRule);
        j++;
      }

      // Use this to run comparisons with Alchemy (needs to be installed on your machine, see MLN.java)
      // String file = "urdf_" + i;
      // System.out.println(MLN_Interface.run(file, query, urdf.globalDependencyGraph, softRules, urdf.softRuleGroundings, urdf.hardRuleGroundings));
      // System.out.println(new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).evalMaxSat(urdf.globalDependencyGraph));
      // System.out.println(new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).evalMaxSat(MLN.getTruthAssignmentsFromFile(file,
      // urdf.globalDependencyGraph)));

      resultFacts.clear();
      resultBindings.clear();
      resultLineage.clear();
      urdf.softRuleGroundings.clear();
      urdf.hardRuleGroundings.clear();
      urdf.clear();

      System.gc();
    }
    totalTime = System.currentTimeMillis() - totalTime;
    Sysout.println("overall time: " + totalTime + " ms");
    urdf.close();
  }

  static void dump(String id, UFactSet dependencyGraph) throws Exception {
    BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\graph_" + id + ".txt")); // should be "UTF-8"
    for (UFact fact : dependencyGraph) {
      writer.write(id + "\t" + (long) fact.hashCode() + "\t" + fact.getRelationName() + "\t" + fact.getFirstArgumentName() + "\t"
          + fact.getSecondArgumentName() + "\t" + fact.getBaseConfidence() + "\n");
    }
    writer.flush();
    writer.close();
  }

  public static double getRuleWeightClosedWorld(URDF urdf, USoftRule rule) throws Exception {
    if (rule.getHead() == null)
      return 1;

    URule r = new URule(rule.getName(), new ArrayList<ULiteral>(rule.getLiterals()));
    double d1 = 1, d2 = 1;

    List<UFactSet> resultFacts = new ArrayList<UFactSet>();
    List<UBindingSet> resultBindings = new ArrayList<UBindingSet>();

    HashSet<UArgument> constants = new HashSet<UArgument>();
    UFactSet dependencyGraph = new UFactSet();

    urdf.hardRuleGroundings = new HashSet<UGroundedHardRule>();
    urdf.softRuleGroundings = new HashSet<UGroundedSoftRule>();
    urdf.sortBySelectivity(r, new UBindingSet());
    urdf.ground(r, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, constants, dependencyGraph, null, 0, false, false, new ULineageAnd(null),
        new ArrayList<ULineageAnd>());
    d1 = resultFacts.size();

    r.addLiteral(rule.getHead());

    resultFacts.clear();
    resultBindings.clear();
    constants.clear();
    dependencyGraph.clear();

    urdf.hardRuleGroundings = new HashSet<UGroundedHardRule>();
    urdf.softRuleGroundings = new HashSet<UGroundedSoftRule>();
    urdf.sortBySelectivity(r, new UBindingSet());
    urdf.ground(r, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, constants, dependencyGraph, null, 0, false, false, new ULineageAnd(null),
        new ArrayList<ULineageAnd>());
    d2 = resultFacts.size();

    // System.out.println(" => " + d2 + " / " + d1 + " = " + (d2 / d1));

    return d2 / d1;
  }

  static void writeMaxSatProblem(int x, HashSet<UGroundedHardRule> hardRules, HashSet<UGroundedSoftRule> softRules, UFactSet dependencyGraph) throws Exception {
    boolean[][] inSoftHead = new boolean[softRules.size()][dependencyGraph.size()];
    boolean[][] inSoftBody = new boolean[softRules.size()][dependencyGraph.size()];
    boolean[][] inComp = new boolean[hardRules.size()][dependencyGraph.size()];
    double[] weights = new double[softRules.size()];

    HashMap<UFact, Integer> f2id = new HashMap<UFact, Integer>();
    for (UFact f : dependencyGraph) {
      f2id.put(f, f2id.size());
    }

    int s = 0;
    for (UGroundedSoftRule r : softRules) {
      if (r.size() > 0)
        weights[s] = r.getWeight();
      else
        weights[s] = r.getHead().getBaseConfidence();
      inSoftHead[s][f2id.get(r.getHead())] = true;
      for (UFact f : r) {
        inSoftBody[s][f2id.get(f)] = true;
      }
      s++;
    }
    int h = 0;
    for (UGroundedHardRule r : hardRules) {
      for (UFact f : r)
        inComp[h][f2id.get(f)] = true;
      h++;
    }

    BufferedWriter writer = new BufferedWriter(new FileWriter("query" + x));
    writer.write("#VARS: " + f2id.size() + "\n");
    writer.write("#CLAUSES: " + s + "\n");
    writer.write("#COMP: " + h + "\n");
    writer.write("\nCLAUSES x VARS positive:\n");
    for (int i = 0; i < s; i++) {
      for (int j = 0; j < f2id.size(); j++) {
        if (inSoftHead[i][j])
          writer.write("1 ");
        else
          writer.write("0 ");
      }
      writer.write("\n");
    }
    writer.write("\nCLAUSES x VARS negative:\n");
    for (int i = 0; i < s; i++) {
      for (int j = 0; j < f2id.size(); j++) {
        if (inSoftBody[i][j])
          writer.write("1 ");
        else
          writer.write("0 ");
      }
      writer.write("\n");
    }
    writer.write("\nCOMP x VARS:\n");
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < f2id.size(); j++) {
        if (inComp[i][j])
          writer.write("1 ");
        else
          writer.write("0 ");
      }
      writer.write("\n");
    }
    writer.write("\nCLAUSE WEIGHTS:\n");
    for (int i = 0; i < s; i++) {
      writer.write(weights[i] + "\n");
    }
    writer.flush();
    writer.close();
  }

  public static String getSp(int level) {
    String s = String.valueOf(level) + "\t";
    for (int i = 0; i < level; i++)
      s += "|";
    return s;
  }
}
