package urdf.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import urdf.api.UArgument;
import urdf.api.UBindingSet;
import urdf.api.UEntity;
import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.UGroundedHardRule;
import urdf.api.UGroundedSoftRule;
import urdf.api.UHardRule;
import urdf.api.ULineageAnd;
import urdf.api.ULiteral;
import urdf.api.UQuery;
import urdf.api.URelation;
import urdf.api.URule;
import urdf.api.USoftRule;
import urdf.reasoner.LinearMAXSAT;
import urdf.reasoner.MAXSAT;
import urdf.reasoner.URDF;
import urdf.tools.Gaussian;
import urdf.tools.RuleParser;

public class URDF_main2 {

  private static int constId = 0;

  private static Random random = new Random(System.currentTimeMillis());

  public static int EXPANSION_MODE = 0;

  public static int NOISE_LEVEL_RULES = 0, NOISE_LEVEL_FACTS = 0, DUMMY_POOL = 1000, REPEATS = 2, STEPS = 10;

  static {
    URDF.RECURSIVE_RULES = true;// false;
    URDF.MAX_LEVEL = 9; // Integer.MAX_VALUE;
  }

  public static void main3(String[] args) throws Exception {

    Random r = new Random(); //System.currentTimeMillis());

    int m = 100, s = 100, h = 100, p = 1000;

    int step_m = 0, step_s = 100, step_h = 0, step_p = 0;

    for (int k = 0; k < 20; k++) {

      int n = 0;
      if (m > p - 1) {
        System.err.println("CLAUSE SIZE EXCEEDS #VARS!");
        return;
      }

      long time = System.currentTimeMillis();

      HashSet<UGroundedSoftRule> softRules = new HashSet<UGroundedSoftRule>();
      HashSet<UGroundedHardRule> hardRules = new HashSet<UGroundedHardRule>();

      UFactSet allFacts = new UFactSet();
      for (int i = 0; i < s; i++) {
        UFact head = new UFact(String.valueOf(Math.abs(r.nextInt()) % p), r.nextDouble());
        allFacts.add(head);
        n++;
        UFactSet body = new UFactSet(m);
        for (int j = 0; j < m; j++) {
          UFact fact;
          do {
            fact = new UFact(String.valueOf(Math.abs(r.nextInt()) % p), r.nextDouble());
          } while (head.equals(fact) || body.contains(fact));
          body.add(fact);
          allFacts.add(fact);
          n++;
        }
        UGroundedSoftRule clause = new UGroundedSoftRule(body, head, r.nextDouble());
        softRules.add(clause);
      }

      int i = 0, size = allFacts.size();
      UFactSet facts = new UFactSet();
      for (UFact fact : allFacts) {
        facts.add(fact);
        i++;
        if (i % Math.max((size / h), 2) == 0 && facts.size() > 0) {
          hardRules.add(new UGroundedHardRule(facts));
          // System.out.println((size / h) + "\t" + i + "  " + new UGroundedHardRule(facts));
          facts = new UFactSet();
        }
      }
      if (facts.size() > 0) {
        hardRules.add(new UGroundedHardRule(facts));
        //System.out.println((n / h) + "\t" + i + "  " + new UGroundedHardRule(facts));
      }
      n += i;

      //LinearMAXSAT solver = new LinearMAXSAT(softRules, hardRules, URDF.invertRules(softRules, hardRules));
      MAXSAT solver = new MAXSAT(softRules, hardRules, URDF.invertRules(softRules, hardRules));
      solver.processMAXSAT();

      time = System.currentTimeMillis() - time;

      System.out.println("\nK:" + k + "\tMAXSAT-STEPS:" + LinearMAXSAT.steps + "\tS+H=" + n + "\tSTEP-RATIO:" + (LinearMAXSAT.steps / (double) n) + "\tTIME:"
          + time + "\tTIME-RATIO:" + (time / (double) n));

      double opt = 0;
      for (UGroundedSoftRule C : softRules)
        if (C.isSatisfied() == UFact.TRUE)
          opt += C.getWeight();

      System.out.println(" > FACTS:" + allFacts.size() + "\tTRUE:" + LinearMAXSAT.TRUE_FACTS + "\tFALSE:" + LinearMAXSAT.FALSE_FACTS + "\tOPT:" + opt);

      time = System.currentTimeMillis();

      //LinearMAXSAT solver = new LinearMAXSAT(softRules, hardRules, URDF.invertRules(softRules, hardRules));
      LinearMAXSAT solver2 = new LinearMAXSAT(softRules, hardRules, URDF.invertRules(softRules, hardRules));
      solver2.processMAXSAT();

      time = System.currentTimeMillis() - time;

      System.out.println("K:" + k + "\tMAXSAT-STEPS:" + LinearMAXSAT.steps + "\tS+H=" + n + "\tSTEP-RATIO:" + (LinearMAXSAT.steps / (double) n) + "\tTIME:"
          + time + "\tTIME-RATIO:" + (time / (double) n));

      opt = 0;
      for (UGroundedSoftRule C : softRules)
        if (C.isSatisfied() == UFact.TRUE)
          opt += C.getWeight();

      System.out.println(" > FACTS:" + allFacts.size() + "\tTRUE:" + LinearMAXSAT.TRUE_FACTS + "\tFALSE:" + LinearMAXSAT.FALSE_FACTS + "\tOPT:" + opt);

      m += step_m;
      s += step_s;
      h += step_h;
      p += step_p;
    }
  }

  public static void main2(String[] args) throws Exception {

    HashSet<UGroundedSoftRule> softRules = new HashSet<UGroundedSoftRule>();
    HashSet<UGroundedHardRule> hardRules = new HashSet<UGroundedHardRule>();

    UFact A = new UFact("A", 0.6);
    UFact B = new UFact("B", 0.9);
    UFact C = new UFact("C", 0.1);
    UFact D = new UFact("D", 1.0);

    UGroundedSoftRule C_1 = new UGroundedSoftRule(new UFactSet(A, C), B, 0.9);
    UGroundedSoftRule C_2 = new UGroundedSoftRule(new UFactSet(C, D), A, 0.7);

    UGroundedHardRule S_1 = new UGroundedHardRule(new UFactSet(A, B));
    UGroundedHardRule S_2 = new UGroundedHardRule(new UFactSet(C, D));

    softRules.add(C_1);
    softRules.add(C_2);

    hardRules.add(S_1);
    hardRules.add(S_2);

    LinearMAXSAT solver = new LinearMAXSAT(softRules, hardRules, URDF.invertRules(softRules, hardRules));
    solver.processMAXSAT();
  }

  public static void main(String[] args) throws Exception {

    PrintStream Sysout = new PrintStream(System.out, true, "UTF-8"); // for UTF-8 console output

    ArrayList<USoftRule> softRules = new ArrayList<USoftRule>();
    ArrayList<UHardRule> hardRules = new ArrayList<UHardRule>();
    ArrayList<UQuery> queries = new ArrayList<UQuery>();

    RuleParser.parseAll(new URDF_main().getClass().getClassLoader().getResource("rules.txt").openStream(), softRules, hardRules, queries);
    // RuleParser.scanDir(new File("C:\\yago-softrules2\\onlySpeciality"), softRules, hardRules, queries);
    System.out.println("PROCESSING " + queries.size() + " QUERIES, " + softRules.size() + " SOFTRULES, " + hardRules.size() + " HARDRULES.");

    int i, j, k, r;
    long time;

    URDF urdf = new URDF(args[0], softRules, hardRules);

    // DEMO MODE: Ground all atoms and rules, display lineage, warm up cache
    long totalTime = System.currentTimeMillis();

    for (k = 0; k < STEPS; k++) {

      EXPANSION_MODE = 0;
      NOISE_LEVEL_RULES = k * 10;
      NOISE_LEVEL_FACTS = k * 10;

      long NUM_FACTS = 0, NUM_SOFTRULES = 0, NUM_HARDRULES = 0, SIZE_SOFTRULES = 0, SIZE_HARDRULES = 0, GROUNDING_TIME = 0, INVERTING_TIME = 0, MAXSAT_TIME = 0;

      for (i = 0; i < 10; i++) {
        // for (i = 3; i < 4; i++) {
        UQuery query = queries.get(i);

        // Ground the query top-down
        Sysout.println("\tQUERY " + i + "\t\t\t" + query);

        for (r = 0; r < REPEATS; r++) {

          List<UFactSet> resultFacts = new ArrayList<UFactSet>();
          List<UBindingSet> resultBindings = new ArrayList<UBindingSet>();
          ArrayList<ULineageAnd> resultLineage = new ArrayList<ULineageAnd>();

          time = System.currentTimeMillis();
          urdf.sortBySelectivity(query, new UBindingSet());
          // Sysout.println("SORT [" + (System.currentTimeMillis() - time) + "ms]\t\t" + query.size() + " ATOMS");

          time = System.currentTimeMillis();
          urdf.ground(query, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, new HashSet<UArgument>(), new UFactSet(), null, 0, true, true,
                  new ULineageAnd(null), resultLineage);


          // Sysout.println("\t" + urdf.softRuleGroundings.size() + " GROUNDED SOFT RULES, " + urdf.hardRuleGroundings.size() + " GROUNDED HARD RULES, "
          // + urdf.globalDependencyGraph.size() + " FACTS");

          // Synthetic expansions
          syntheticExpansion(urdf);

          // Sysout.println("\t" + urdf.softRuleGroundings.size() + " GROUNDED SOFT RULES, " + urdf.hardRuleGroundings.size() + " GROUNDED HARD RULES, "
          // + urdf.globalDependencyGraph.size() + " FACTS");

          // Sysout.println("GROUND [" + (System.currentTimeMillis() - time) + "ms]\t\t" + resultFacts.size() + " DISTINCT RESULT SETS");
          if (r > 0)
            GROUNDING_TIME += System.currentTimeMillis() - time;

          time = System.currentTimeMillis();
          urdf.invertRules(urdf.globalDependencyGraph);
          // Sysout.println("INV-RULES [" + (System.currentTimeMillis() - time) + "ms]\t\t" + urdf.softRuleGroundings.size() + " GROUNDED SOFT RULES, "
          // + urdf.hardRuleGroundings.size() + " GROUNDED HARD RULES, " + urdf.globalDependencyGraph.size() + " FACTS");
          if (r > 0)
            INVERTING_TIME += System.currentTimeMillis() - time;

          time = System.currentTimeMillis();
          new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).processMAXSAT();
          // Sysout.println("MAX-SAT [" + (System.currentTimeMillis() - time) + "ms]");
          if (r > 0)
            MAXSAT_TIME += System.currentTimeMillis() - time;

          // This is a simple PW-based sampling algorithm
          //time = System.currentTimeMillis();
          // Sampling.getConfAll(resultLineage);
          // Sysout.println("PW-CONF [" + (System.currentTimeMillis() - time) + "ms]");
          // Sysout.println("SLD STEPS: " + URDF.SLD_steps);

          j = 0;
          // for (ULineageAnd result : resultLineage) {
          // Sysout.println("\nRESULT [" + i + "|" + (j + 1) + "/" + resultLineage.size() + "]\t\t");
          // Sysout.print(result.toString(-1));
          // result.writeGraphVizFile(i); // write a graph viz file including the hard rule lineage
          // j++;
          // }

          // Sysout.println();

          j = 0;
          int hSize = 0;
          for (UFactSet hardRule : urdf.hardRuleGroundings) {
            // if (hardRule.size() > 1)
            Sysout.println("HARDRULE[" + i + "|" + (j + 1) + "/" + urdf.hardRuleGroundings.size() + "]\t" + hardRule);
            hSize += hardRule.size();
            j++;
          }

          j = 0;
          int sSize = 0;
          for (UGroundedSoftRule softRule : urdf.softRuleGroundings) {
            // if (softRule.size() > 0)
            // Sysout.println("SOFTRULE[" + i + "|" + (j + 1) + "/" + urdf.softRuleGroundings.size() + "]\t" + softRule);
            sSize += softRule.size() + (softRule.getHead() != null ? 1 : 0);
            j++;
          }

          // Sysout.println(k + "\tFACTS=" + urdf.globalDependencyGraph.size() + "\tSOFTRULES_SIZE=" + sSize + "\tHARDRULES_SIZE=" + hSize + "\tRATIO="
          // + (MAXSAT_TIME / (hSize + sSize + 1.0)));

          if (r == 0) {
            NUM_FACTS += urdf.globalDependencyGraph.size();
            NUM_SOFTRULES += urdf.softRuleGroundings.size();
            NUM_HARDRULES += urdf.hardRuleGroundings.size();
            SIZE_SOFTRULES += sSize;
            SIZE_HARDRULES += hSize;
          }

          // Use this to run comparisons with Alchemy (needs to be installed on your machine, see MLN.java)
          // System.out
          // .println(MLN_Interface.run("urdf_new_" + i, query, urdf.globalDependencyGraph, softRules, urdf.softRuleGroundings, urdf.hardRuleGroundings));
          // System.out.println(new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).evalMaxSat(urdf.globalDependencyGraph));
          // System.out.println(new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).evalMaxSat(MLN.getTruthAssignmentsFromFile(file,
          // urdf.globalDependencyGraph)));

          resultFacts.clear();
          resultBindings.clear();
          resultLineage.clear();
          urdf.clearRuleForced();// urdf.clear();

        }
        // urdf.clearRuleForced();
        System.gc();
      }

      Sysout.println(k + "\tNUM_FACTS=" + NUM_FACTS + "\tNUM_SOFTRULES=" + NUM_SOFTRULES + "\tNUM_HARDRULES=" + NUM_HARDRULES + "\tSIZE_SOFTRULES="
          + SIZE_SOFTRULES + "\tSIZE_HARDRULES=" + SIZE_HARDRULES + "\t|S+H|=" + (SIZE_HARDRULES + SIZE_SOFTRULES) + "\tGROUNDING_TIME="
          + (GROUNDING_TIME / (REPEATS - 1.0)) + "\tINVERTING_TIME=" + (INVERTING_TIME / (REPEATS - 1.0)) + "\tMAXSAT_TIME=" + (MAXSAT_TIME + (REPEATS - 1.0))
          + "\tRATIO=" + (MAXSAT_TIME / (SIZE_HARDRULES + SIZE_SOFTRULES + 1.0)));

    }
    totalTime = System.currentTimeMillis() - totalTime;
    Sysout.println("overall time: " + totalTime + " ms");
    Sysout.flush();
    Sysout.close();
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

  static void syntheticExpansion(URDF urdf) throws Exception {

    // -------------------- THE FOLLOWING IS FOR SYNTHETIC FACT AND RULE EXPANSIONS ONLY ----------------

    constId = 0;
    if (NOISE_LEVEL_RULES > 0 && NOISE_LEVEL_FACTS > 0) {

      // Add dummy facts and expand softrules
      if (EXPANSION_MODE == 0) {

        HashSet<UGroundedSoftRule> dummyRules = new HashSet<UGroundedSoftRule>();
        for (UGroundedSoftRule softRuleGrounding : urdf.softRuleGroundings) {
          // System.out.println("\t\t" + softRuleGrounding.hashCode()); // .size() + "\t" + softRuleGrounding);
          if (softRuleGrounding.size() < 1)
            continue;
          for (int i = 0; i < NOISE_LEVEL_RULES; i++) {
            UGroundedSoftRule dummyRule = new UGroundedSoftRule(softRuleGrounding.getHead());
            dummyRule.addAll(softRuleGrounding);
            dummyRule.setWeight(softRuleGrounding.getWeight());

            UFact maxFact = null;
            for (UFact f : softRuleGrounding) {
              if (maxFact == null || f.getBaseConfidence() > maxFact.getBaseConfidence())
                maxFact = f;
            }

            for (UFact dummyFact : injectInconsistenciesUniform(maxFact.getBaseConfidence(), random, false)) {
              dummyRule.add(dummyFact);
              urdf.globalDependencyGraph.add(dummyFact);
            }

            dummyRules.add(dummyRule);
          }
        }
        urdf.softRuleGroundings = dummyRules;

        // Add facts; expand softrules & hardrules; do not reuse dummy facts multiple times
      } else if (EXPANSION_MODE == 1) {

        HashSet<UGroundedSoftRule> dummyRules = new HashSet<UGroundedSoftRule>();
        for (UGroundedSoftRule softRuleGrounding : urdf.softRuleGroundings) {
          if (softRuleGrounding.size() < 1)
            continue;
          for (int i = 0; i < NOISE_LEVEL_RULES; i++) {
            UGroundedSoftRule dummyRule = new UGroundedSoftRule(softRuleGrounding.getHead());
            dummyRule.addAll(softRuleGrounding);
            dummyRule.setWeight(softRuleGrounding.getWeight());

            for (UFact f : softRuleGrounding) {
              if (f.getGroundedHardRule() == null && NOISE_LEVEL_FACTS > 0) {
                f.setGroundedHardRule(new UGroundedHardRule());
                f.getGroundedHardRule().add(f);
                urdf.hardRuleGroundings.add(f.getGroundedHardRule());
              }
              for (UFact dummyFact : injectInconsistenciesGaussian(f.getBaseConfidence(), random, false)) {
                f.getGroundedHardRule().add(dummyFact);
                dummyFact.setGroundedHardRule(f.getGroundedHardRule());
                dummyRule.add(dummyFact);
                urdf.globalDependencyGraph.add(dummyFact);
              }
            }
            dummyRules.add(dummyRule);
          }
        }
        urdf.softRuleGroundings = dummyRules;

        // Add facts; expand softrules & hardrules; reuse dummy facts multiple times
      } else if (EXPANSION_MODE == 2) {

        HashSet<UGroundedSoftRule> dummyRules = new HashSet<UGroundedSoftRule>();
        for (UGroundedSoftRule softRuleGrounding : urdf.softRuleGroundings) {
          if (softRuleGrounding.size() < 1)
            continue;
          for (int i = 0; i < NOISE_LEVEL_RULES; i++) {
            UGroundedSoftRule dummyRule = new UGroundedSoftRule(softRuleGrounding.getHead());
            dummyRule.addAll(softRuleGrounding);
            dummyRule.setWeight(softRuleGrounding.getWeight());

            for (UFact f : softRuleGrounding) {
              if (f.getGroundedHardRule() == null && NOISE_LEVEL_FACTS > 0) {
                f.setGroundedHardRule(new UGroundedHardRule());
                f.getGroundedHardRule().add(f);
                urdf.hardRuleGroundings.add(f.getGroundedHardRule());
              }
              for (UFact dummyFact : injectInconsistenciesGaussian(f.getBaseConfidence(), random, false)) {
                UFactSet cachedFacts;
                if ((cachedFacts = urdf.ruleCache.get(dummyFact.getFirstArgumentName() + "$" + dummyFact.getSecondArgumentName() + "$"
                    + dummyFact.getRelationName())) == null
                    && (cachedFacts = urdf.dbCache.get(dummyFact.getFirstArgumentName() + "$" + dummyFact.getSecondArgumentName() + "$"
                        + dummyFact.getRelationName())) == null)
                  urdf.ruleCache.put(dummyFact);
                else
                  dummyFact = cachedFacts.iterator().next();
                if (dummyFact.getGroundedHardRule() == null) {
                  f.getGroundedHardRule().add(dummyFact);
                  dummyFact.setGroundedHardRule(f.getGroundedHardRule());
                }
                dummyRule.add(dummyFact);
                urdf.globalDependencyGraph.add(dummyFact);
              }
            }
            dummyRules.add(dummyRule);
          }
        }
        urdf.softRuleGroundings = dummyRules;
      }
    }
  }

  public static UFactSet injectInconsistenciesGaussian(double mu, Random r, boolean DISJOINT) throws Exception {
    UFactSet facts = new UFactSet();

    double sigma = 1.0;
    for (int i = 0; i <= NOISE_LEVEL_FACTS; i++) {
      double x = mu + ((i - (NOISE_LEVEL_FACTS / 2)) * sigma); // symmetric around mean, stepsize sigma
      if (x == mu)
        continue;

      UFact f;
      do {
        // System.out.println(mu + "  " + x + " " + Gaussian.phi(x, mu, sigma));
        int id1 = r != null ? r.nextInt(DUMMY_POOL) : ++constId;
        f = new UFact(URelation.DUMMY, new UEntity("D" + String.valueOf(id1)), new UEntity("D" + String.valueOf(id1)), Gaussian.phi(x, mu, sigma));
      } while (DISJOINT && facts.contains(f));
      facts.add(f);
    }

    return facts;
  }

  private static UFactSet injectInconsistenciesUniform(double mu, Random r, boolean DISJOINT) throws Exception {
    UFactSet facts = new UFactSet();

    double sigma = 1.0;
    double uni = (mu - sigma / 2 + Math.ceil(r.nextDouble() * sigma * 10) / (sigma * 10));

    for (int i = 0; i < NOISE_LEVEL_FACTS; i++) {
      UFact f;
      do {
        int id1 = r != null ? r.nextInt(DUMMY_POOL) : ++constId;

        // System.out.println(mu + "  " + x + " " + uni;
        f = new UFact(URelation.DUMMY, new UEntity("D" + String.valueOf(id1)), new UEntity("D" + String.valueOf(id1)), uni);
      } while (DISJOINT && facts.contains(f));
      facts.add(f);
    }

    return facts;
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
