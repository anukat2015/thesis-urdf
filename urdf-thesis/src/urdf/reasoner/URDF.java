package urdf.reasoner;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import urdf.api.UArgument;
import urdf.api.UBinding;
import urdf.api.UBindingSet;
import urdf.api.UEntity;
import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.UGroundedHardRule;
import urdf.api.UGroundedSoftRule;
import urdf.api.UHardRule;
import urdf.api.ULineageAbstract;
import urdf.api.ULineageAnd;
import urdf.api.ULineageOr;
import urdf.api.ULiteral;
import urdf.api.URelation;
import urdf.api.URule;
import urdf.api.URuleStore;
import urdf.api.USoftRule;
import urdf.db.DBConfig;
import urdf.db.DBConfig.DatabaseParameters;
import urdf.reasoner.arithmetic.ArithmeticPredicate;
import urdf.reasoner.function.GazetteerFunctionCall;

public class URDF {

  private Connection connection;

  private PreparedStatement groundFactStmt1, groundFactStmt2, groundFactStmt3, groundFactStmt4, groundFactStmt5, groundFactStmt6, groundFactStmt7;

  // Internal caches for DB lookups & SLD resolution
  public Cache dbCache, ruleCache;

  public LineageCache lineageCache;

  public HardRuleCache hardRuleCache;

  public HashMap<UFact, ULineageAnd> dbLineageCache;

  public SoftRuleIndex softRules;

  public List<UHardRule> hardRules;

  public HashSet<UGroundedSoftRule> softRuleGroundings;

  public HashSet<UGroundedHardRule> hardRuleGroundings;

  public UFactSet globalDependencyGraph;

  public HashSet<UArgument> globalConstants;

  public Map<String, double[]> relationStatistics;

  public Map<UFact, HashSet<UGroundedSoftRule>> invSoftRules = null;

  public URDF(String iniFile, List<USoftRule> softRules, List<UHardRule> hardRules) throws Exception {
    this(new FileInputStream(iniFile), softRules, hardRules);
  }

  public URDF(InputStream iniStream, List<USoftRule> softRules, List<UHardRule> hardRules) throws Exception {
    this(DBConfig.databaseParameters(iniStream), softRules, hardRules);
  }

  public URDF(DatabaseParameters p, List<USoftRule> softRules, List<UHardRule> hardRules) throws Exception {

    if (p.system.toLowerCase().indexOf("postgres") >= 0) {

      DriverManager.registerDriver((Driver) Class.forName("org.postgresql.Driver").newInstance());

      this.connection = DriverManager.getConnection("jdbc:postgresql://" + p.host + ":" + p.port + (p.databaseName == null ? "" : "/" + p.databaseName),
          p.user, p.password);

      this.groundFactStmt1 = connection.prepareStatement("SELECT arg1, relation, arg2, confidence FROM facts WHERE relation = ? and arg1 = ?");
      this.groundFactStmt2 = connection.prepareStatement("SELECT arg1, relation, arg2, confidence FROM facts WHERE relation = ? and arg2 = ?");
      this.groundFactStmt3 = connection.prepareStatement("SELECT arg1, relation, arg2, confidence FROM facts WHERE relation = ?");
      this.groundFactStmt4 = connection.prepareStatement("SELECT arg1, relation, arg2, confidence FROM facts WHERE arg1 = ?");
      this.groundFactStmt5 = connection.prepareStatement("SELECT arg1, relation, arg2, confidence FROM facts WHERE arg2 = ?");
      this.groundFactStmt6 = connection.prepareStatement("SELECT arg1, relation, arg2, confidence FROM facts WHERE arg1 = ? and arg2 = ?");
      this.groundFactStmt7 = connection.prepareStatement("SELECT arg1, relation, arg2, confidence FROM facts WHERE relation = ? and arg1 = ? and arg2 = ?");

    } else if (p.system.toLowerCase().indexOf("oracle") >= 0) {

      DriverManager.registerDriver((Driver) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance());

      // localhost:1000 setting
      // this.connection = DriverManager.getConnection(
      // "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=localhost)(PORT=2000)))(CONNECT_DATA=(SERVICE_NAME=" + p.inst +
      // ")(server = dedicated)))", p.user, p.password);

      this.connection = DriverManager.getConnection("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=" + p.host
          + ")(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=" + p.inst + ")(server = dedicated)))", p.user, p.password);

      // Use the following Oracle indexes for best performance:
      // create index facts_a1_rel_a2_c_idx on facts(arg1,relation,arg2,confidence);
      // create index facts_a2_rel_a1_c_idx on facts(arg2,relation,arg1,confidence);
      // create index facts_rel_a1_a2_c_idx on facts(relation,arg1,arg2,confidence);
      // create index facts_a1_a2_rel_c_idx on facts(arg1,arg2,relation,confidence);
      // create table rel_stats as (
      // select f1.relation, f1.n, f2.mult1, f3.mult2 from
      // (select relation, count(id) as n from facts group by relation) f1,
      // (select relation, avg(n1) as mult1 from (select relation, arg1, count(id) as n1 from facts group by relation, arg1) group by relation) f2,
      // (select relation, avg(n2) as mult2 from (select relation, arg2, count(id) as n2 from facts group by relation, arg2) group by relation) f3
      // where f1.relation = f2.relation and f1.relation = f3.relation
      // );
      // create index rel_stats_idx on rel_stats(relation);

      this.groundFactStmt1 = connection
          .prepareStatement("SELECT /*+ INDEX(facts facts_a1_rel_a2_c_idx)*/ arg1, relation, arg2, confidence FROM facts WHERE relation = ? and arg1 = ?");
      this.groundFactStmt2 = connection
          .prepareStatement("SELECT /*+ INDEX(facts facts_a2_rel_a1_c_idx)*/ arg1, relation, arg2, confidence FROM facts WHERE relation = ? and arg2 = ?");
      this.groundFactStmt3 = connection
          .prepareStatement("SELECT /*+ INDEX(facts facts_rel_a1_a2_c_idx)*/ arg1, relation, arg2, confidence FROM facts WHERE relation = ?");
      this.groundFactStmt4 = connection
          .prepareStatement("SELECT /*+ INDEX(facts facts_a1_rel_a2_c_idx)*/ arg1, relation, arg2, confidence FROM facts WHERE arg1 = ?");
      this.groundFactStmt5 = connection
          .prepareStatement("SELECT /*+ INDEX(facts facts_a2_rel_a1_c_idx)*/ arg1, relation, arg2, confidence FROM facts WHERE arg2 = ?");
      this.groundFactStmt6 = connection
          .prepareStatement("SELECT /*+ INDEX(facts facts_a1_a2_rel_c_idx)*/ arg1, relation, arg2, confidence FROM facts WHERE arg1 = ? and arg2 = ?");
      this.groundFactStmt7 = connection
          .prepareStatement("SELECT /*+ INDEX(facts facts_a1_rel_a2_c_idx)*/ arg1, relation, arg2, confidence FROM facts WHERE relation = ? and arg1 = ? and arg2 = ?");

    } else
      throw new Exception("UNKNOWN DB DRIVER!");

    // Register Gazetteer function calls with this connection
    URelation.valueOf("isClose").setFunctionCall(GazetteerFunctionCall.getInstance(connection));
    URelation.valueOf("gaz_isClose").setFunctionCall(GazetteerFunctionCall.getInstance(connection));

    // Initialize the selectivity estimator
    this.relationStatistics = new HashMap<String, double[]>(200);
    Statement stmt = connection.createStatement();
    ResultSet rset = stmt.executeQuery("SELECT relation, n, mult1, mult2 FROM rel_stats");
    while (rset.next()) {
      double[] stats = new double[3];
      stats[0] = rset.getDouble(2);
      stats[1] = rset.getDouble(3);
      stats[2] = rset.getDouble(4);
      relationStatistics.put(rset.getString(1), stats);
    }
    rset.close();
    stmt.close();

    // Assume function calls are always expensive!
    for (URelation relation : URelation.instances())
      if (relation.isFunctionCall())
        relationStatistics.put(relation.getName(), new double[] { Double.MAX_VALUE, 100, 100 });

    this.dbCache = new Cache();
    this.ruleCache = new Cache();
    this.lineageCache = new LineageCache();
    this.hardRuleCache = new HardRuleCache();
    this.dbLineageCache = new HashMap<UFact, ULineageAnd>();

    this.softRules = new SoftRuleIndex(softRules);
    for (URule r : softRules)
      this.sortBySelectivity(r, new UBindingSet());

    this.hardRules = hardRules;
    for (URule r : hardRules)
      this.sortBySelectivity(r, new UBindingSet());
  }

  public void sortBySelectivity(URule rule, UBindingSet bindings) throws Exception {
    for (ULiteral literal : rule.getLiterals())
      literal.setSelectivity(getSelectivity(literal, bindings));
    Collections.sort(rule.getLiterals());
  }

  private double getSelectivity(ULiteral literal, UBindingSet bindings) {
    double selectivity = Double.MAX_VALUE; // ground arithmetic predicates with unbound variables or unknown predicates last
    UArgument arg = literal.getRelation();
    double[] stats;
    if (literal.getRelation().isArithmetic()) {
      if ((!literal.getFirstArgument().isVariable() || bindings.getBinding(literal.getFirstArgument()) != null)
          && ((!literal.getSecondArgument().isVariable() || bindings.getBinding(literal.getSecondArgument()) != null)))
        selectivity = 1;
    } else if ((!arg.isVariable() || (arg = bindings.getBinding(arg)) != null) && (stats = relationStatistics.get(arg.getName())) != null) {
      if ((!literal.getFirstArgument().isVariable() || bindings.getBinding(literal.getFirstArgument()) != null)
          && ((!literal.getSecondArgument().isVariable() || bindings.getBinding(literal.getSecondArgument()) != null)))
        selectivity = 1;
      else if (!literal.getFirstArgument().isVariable() || bindings.getBinding(literal.getFirstArgument()) != null)
        selectivity = stats[1];
      else if (!literal.getSecondArgument().isVariable() || bindings.getBinding(literal.getSecondArgument()) != null)
        selectivity = stats[2];
      else
        selectivity = stats[0];
    } else {
      arg = literal.getFirstArgument();
      if (!arg.isVariable() || (arg = bindings.getBinding(arg)) != null)
        selectivity /= 200;
      arg = literal.getSecondArgument();
      if (!arg.isVariable() || (arg = bindings.getBinding(arg)) != null)
        selectivity /= 100;
      selectivity *= 0.99; // still better than an arithmetic predicate with unbound variables!
    }
    return selectivity;
  }

  public void setHardRules(List<UHardRule> hardRules) {
    // this.clearRuleForced();
    this.hardRules = hardRules;
  }

  public void setSoftRules(List<USoftRule> softRules) {
    // this.clearRuleForced();
    this.softRules = new SoftRuleIndex(softRules);
  }

  public void setRules(List<USoftRule> softRules, List<UHardRule> hardRules) {
    this.clearRuleForced();
    this.hardRules = hardRules;
    this.softRules = new SoftRuleIndex(softRules);
  }

  public void setRules(URuleStore ruleStore) {
    this.hardRules = ruleStore.getHardRules();
    this.softRules = new SoftRuleIndex(ruleStore.getSoftRules());
    if (ruleStore.isHardRulesChanged() || ruleStore.isSoftRulesChanged())
      this.clearRuleForced();
  }

  /**
   * Top-level function for query processing (incl. grounding via DB lookups & SLD resolution) and MAX-SAT for consistency reasoning
   */
  public void ground(URule rule, List<UFactSet> resultFacts, List<UBindingSet> resultBindings, ArrayList<ULineageAnd> resultLineage) throws Exception {
    sortBySelectivity(rule, new UBindingSet());
    ground(rule, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, new HashSet<UArgument>(), new UFactSet(), null, 0, false, true,
        new ULineageAnd(null), resultLineage);
    invertRules(globalDependencyGraph);
    new MAXSAT(softRuleGroundings, hardRuleGroundings, invSoftRules).processMAXSAT();
  }

  /**
   * Top-level function for query processing (grounding via DB lookups only), SLD resolution & MAX-SAT are configurable by the flag 'doProcessRules')
   */
  public void ground(URule rule, List<UFactSet> resultFacts, List<UBindingSet> resultBindings, ArrayList<ULineageAnd> resultLineage, boolean doProcessRules)
      throws Exception {
    sortBySelectivity(rule, new UBindingSet());
    ground(rule, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, new HashSet<UArgument>(), new UFactSet(), null, 0, false, doProcessRules,
        new ULineageAnd(null), resultLineage);
    if (doProcessRules) {
      invertRules(globalDependencyGraph);
      new MAXSAT(softRuleGroundings, hardRuleGroundings, invSoftRules).processMAXSAT();
    }
  }

  /**
   * Top-level function for query processing (grounding via DB lookups only), SLD resolution is configurable by the flag 'doProcessRules')
   */
  public void ground(URule rule, UFactSet facts, UBindingSet bindings, List<UFactSet> resultFacts, List<UBindingSet> resultBindings, int idx,
      HashSet<UArgument> constants, UFactSet dependencyGraph, LineageStack stack, int level, boolean doExpandLineage, boolean doProcessRules,
      ULineageAnd localLineage, ArrayList<ULineageAnd> resultLineage) throws Exception {

    URule localRule = new URule(rule.getName(), rule.getLiterals());

    boolean init = false;
    if (idx == 0 && level == 0) {
      globalDependencyGraph = new UFactSet();
      globalConstants = new HashSet<UArgument>();
      hardRuleGroundings = new HashSet<UGroundedHardRule>();
      softRuleGroundings = new HashSet<UGroundedSoftRule>();
      doExpandLineage = true;
      SLD_steps = 0;
      init = true;
    }

    // Select the next literal to be grounded
    ULiteral literal = localRule.getLiterals().get(idx);

    // Swap resolve-order if selectivity has changed because of new variable bindings
    if ((literal.getFirstArgument().isVariable() && bindings.getBinding(literal.getFirstArgument()) == null)
        || (literal.getSecondArgument().isVariable() && bindings.getBinding(literal.getSecondArgument()) == null)
        || (literal.getRelation().isVariable() && bindings.getBinding(literal.getRelation()) == null)) {

      ULiteral minLiteral = literal;
      int minIdx = idx;
      double minSelectivity = Double.MAX_VALUE;
      for (int i = idx; i < localRule.size(); i++) {
        ULiteral literal2 = localRule.getLiteral(i);
        literal2.setSelectivity(getSelectivity(literal2, bindings));
        if (literal2.getSelectivity() < minSelectivity) {
          minLiteral = literal2;
          minIdx = i;
          minSelectivity = literal2.getSelectivity();
        }
        // System.out.println(URDF.getSp(level) + " > " + literal2 + ": " + bindings + " " + literal2.getSelectivity());
      }
      if (literal != minLiteral) {
        localRule.swap(idx, minIdx);
        literal = minLiteral;
      }
    }

    //System.out.println(URDF.getSp(level) + "GROUND LITERAL " + idx + ": " + literal + " " + bindings + " " + literal.getSelectivity());
    HashMap<UFact, ULineageOr> lineageForGroundings = new HashMap<UFact, ULineageOr>();
    UFactSet groundedFacts = groundLiteral(literal, bindings, constants, dependencyGraph, stack, level, doExpandLineage, doProcessRules, lineageForGroundings);
    //System.out.println(URDF.getSp(level) + "GROUNDED LITERAL: " + literal + " " + bindings + " " + lineageForGroundings);

    for (UFact fact : groundedFacts) {

      // Containers for all facts and bindings resolved so far
      UFactSet localFacts = new UFactSet(facts);
      UBindingSet localBindings = new UBindingSet(bindings);

      UArgument argument = literal.getFirstArgument(), binding;
      if (argument.isVariable()) {
        binding = localBindings.getBinding(argument);
        if (binding == null)
          localBindings.add(new UBinding(argument, fact.getFirstArgument()));
        else if (!fact.getFirstArgument().equals(binding))
          continue;
      } else if (argument.isVariable() && argument.equals(fact.getFirstArgument()))
        continue;

      argument = literal.getSecondArgument();
      if (argument.isVariable()) {
        binding = localBindings.getBinding(argument);
        if (binding == null)
          localBindings.add(new UBinding(argument, fact.getSecondArgument()));
        else if (!fact.getSecondArgument().equals(binding))
          continue;
      } else if (argument.isVariable() && argument.equals(fact.getSecondArgument()))
        continue;

      argument = literal.getRelation();
      if (argument.isVariable()) {
        binding = localBindings.getBinding(argument);
        if (binding == null)
          localBindings.add(new UBinding(argument, fact.getRelation()));
        else if (!fact.getRelation().equals(binding))
          continue;
      } else if (argument.isVariable() && !argument.equals(fact.getRelation()))
        continue;

      // We have a grounding that matches the local bindings
      localFacts.add(fact);
      ULineageAnd newLocalLineage = localLineage.copyNonRecursive();
      newLocalLineage.addChild(lineageForGroundings.get(fact));

      // If all literals are processed, we have a conjunctive match to the entire query/rule
      if (idx == localRule.size() - 1 && localBindings.size() == localRule.getVariables().size()) {

        // Create top-level result facts
        resultFacts.add(localFacts);
        resultBindings.add(localBindings);
        resultLineage.add(newLocalLineage);

      } // Otherwise continue grounding the next literal, nested loop!
      else if (idx < localRule.size() - 1)
        ground(localRule, localFacts, localBindings, resultFacts, resultBindings, idx + 1, constants, dependencyGraph, stack, level, doExpandLineage,
            doProcessRules, newLocalLineage, resultLineage);
    }

    if (init) {

      // ground hard rules
      groundHardRules(constants, dependencyGraph);

      // obtain dependency graph
      this.softRuleGroundings.clear();
      this.hardRuleGroundings.clear();

      UFactSet newDependencyGraph = new UFactSet();
      ArrayList<ULineageAnd> newResultLineage = new ArrayList<ULineageAnd>();
      for (ULineageAnd l : resultLineage) {
        ULineageAnd result = (ULineageAnd) l.oneFactPerPath(newDependencyGraph, this.softRuleGroundings, this.hardRuleGroundings);
        if (result != null)
          newResultLineage.add(result);
      }
      resultLineage.clear();
      resultLineage.addAll(newResultLineage);

      // connect lineage to facts
      UFactSet increasedDG = new UFactSet(newDependencyGraph);
      do {
        newDependencyGraph.addAll(increasedDG);
        for (UFact f : newDependencyGraph) {
          ULineageOr l;
          if (f.getLineage() == null) {
            HashMap<UFact, ULineageOr> lineageOfFact = new HashMap<UFact, ULineageOr>();
            groundLiteral(new ULiteral(f.getRelation(), f.getFirstArgument(), f.getSecondArgument()), new UBindingSet(), constants, dependencyGraph, null, 0,
                true, true, lineageOfFact);
            groundHardRules(constants, dependencyGraph);
            l = lineageOfFact.get(f);
          } else
            l = f.getLineage();
          l = (ULineageOr) l.oneFactPerPath(increasedDG, this.softRuleGroundings, this.hardRuleGroundings);
          f.setLineage(l);
        }
      } while (increasedDG.size() != newDependencyGraph.size());

      this.globalDependencyGraph.clear();
      this.globalDependencyGraph.addAll(newDependencyGraph);

      // add unary hard rules:
      for (UFact f : this.globalDependencyGraph) {
        if (f.getGroundedHardRule() == null) {
          f.setGroundedHardRule(new UGroundedHardRule());
          f.getGroundedHardRule().add(f);
          f.getGroundedHardRule().setHardRule(null); // this is a unary set, no first-order hard rule produced this grounding
          hardRuleGroundings.add(f.getGroundedHardRule());
        }
      }

      // produce result facts from lineage (some of them might have been pruned!)
      resultFacts.clear();
      for (ULineageAnd l : resultLineage) {
        UFactSet result = new UFactSet();
        for (ULineageAbstract child : l.getChildren()) {
          ULineageOr or = (ULineageOr) child;
          if (or.getFact() != null)
            result.add(or.getFact());
        }
        resultFacts.add(result);
      }

      // result bindings are empty
      resultBindings.clear();
    }
  }

  private UFactSet groundLiteral(ULiteral literal, UBindingSet bindings, HashSet<UArgument> constants, UFactSet dependencyGraph, LineageStack stack, int level,
      boolean doExpandLineage, boolean doProcessRules, HashMap<UFact, ULineageOr> rLineage) throws Exception {

    UFact fact;
    UFactSet groundedFacts = new UFactSet(), cachedFacts;
    ResultSet rset = null;

    // Include partial bindings for variables in previously grounded facts
    UArgument binding1 = literal.getFirstArgument().isVariable() ? bindings.getBinding(literal.getFirstArgument()) : literal.getFirstArgument();
    UArgument binding2 = literal.getSecondArgument().isVariable() ? bindings.getBinding(literal.getSecondArgument()) : literal.getSecondArgument();
    UArgument binding3 = literal.getRelation().isVariable() ? bindings.getBinding(literal.getRelation()) : literal.getRelation();

    // ----------------------- ARITHMETIC PREDICATES, false ones are never grounded -----------------------

    if (!literal.getRelation().isVariable() && literal.getRelation().isArithmetic()) {
      groundedFacts = evaluateArithmeticPredicate(binding1, binding2, (URelation) binding3, literal.getCompareValue());
      addLineageToFactsFromDB(groundedFacts, rLineage);
    }

    // ----------------------- FUNCTION CALLS, need explicit selectivity estimates for optimization -------

    else if (!literal.getRelation().isVariable() && literal.getRelation().isFunctionCall()) {
      groundedFacts = evaluateFunctionCall(binding1, binding2, (URelation) binding3, literal.getCompareValue());
      addLineageToFactsFromDB(groundedFacts, rLineage);
    }

    // --- OTHER (EXTENSIONAL OR INTENSIONAL) PREDICATES, REQUIRE DB LOOKUPS AND/OR RECURSIVE RESOLUTION ---

    else if (binding1 != null && binding2 == null && binding3 != null) {

      if ((cachedFacts = dbCache.get(binding1, binding2, binding3)) == null) {
        groundFactStmt1.setString(1, binding3.getName());
        groundFactStmt1.setString(2, binding1.getName());
        rset = groundFactStmt1.executeQuery();
        while (rset.next()) {
          if ((cachedFacts = dbCache.get(rset.getString(1) + "$" + rset.getString(3) + "$" + URelation.valueOf(rset.getString(2)).getName())) == null) {
            fact = new UFact(URelation.valueOf(rset.getString(2)), new UEntity(rset.getString(1)), new UEntity(rset.getString(3)), rset.getDouble(4));
            dbCache.put(fact.getFirstArgumentName() + "$" + fact.getSecondArgumentName() + "$" + fact.getRelationName(), new UFactSet(fact));
          } else
            fact = cachedFacts.iterator().next();
          groundedFacts.add(fact);
        }
        dbCache.put(binding1, binding2, binding3, new UFactSet(groundedFacts));
      } else
        groundedFacts.addAll(cachedFacts);
      addLineageToFactsFromDB(groundedFacts, rLineage);

      if (doProcessRules) {
        HashMap<UFact, ULineageOr> lineageFromRules = new HashMap<UFact, ULineageOr>();
        cachedFacts = groundLiteralTopDown(literal, bindings, constants, dependencyGraph, stack, level, doExpandLineage, lineageFromRules);
        addLineageToFactsFromRules(cachedFacts, rLineage, lineageFromRules);
        // System.out.println(getSp(level) + " ---> " + rLineage);
        groundedFacts.addAll(cachedFacts);
      }

    } else if (binding1 == null && binding2 != null && binding3 != null) {

      if ((cachedFacts = dbCache.get(binding1, binding2, binding3)) == null) {
        groundFactStmt2.setString(1, binding3.getName());
        groundFactStmt2.setString(2, binding2.getName());
        rset = groundFactStmt2.executeQuery();
        while (rset.next()) {
          if ((cachedFacts = dbCache.get(rset.getString(1) + "$" + rset.getString(3) + "$" + URelation.valueOf(rset.getString(2)).getName())) == null) {
            fact = new UFact(URelation.valueOf(rset.getString(2)), new UEntity(rset.getString(1)), new UEntity(rset.getString(3)), rset.getDouble(4));
            dbCache.put(fact);
          } else
            fact = cachedFacts.iterator().next();
          groundedFacts.add(fact);
        }
        dbCache.put(binding1, binding2, binding3, new UFactSet(groundedFacts));
      } else
        groundedFacts.addAll(cachedFacts);
      addLineageToFactsFromDB(groundedFacts, rLineage);

      if (doProcessRules) {
        HashMap<UFact, ULineageOr> lineageFromRules = new HashMap<UFact, ULineageOr>();
        cachedFacts = groundLiteralTopDown(literal, bindings, constants, dependencyGraph, stack, level, doExpandLineage, lineageFromRules);
        addLineageToFactsFromRules(cachedFacts, rLineage, lineageFromRules);
        groundedFacts.addAll(cachedFacts);
      }

    } else if (binding1 == null && binding2 == null && binding3 != null) {

      if ((cachedFacts = dbCache.get(binding1, binding2, binding3)) == null) {
        groundFactStmt3.setString(1, binding3.getName());
        rset = groundFactStmt3.executeQuery();
        while (rset.next()) {
          if ((cachedFacts = dbCache.get(rset.getString(1) + "$" + rset.getString(3) + "$" + URelation.valueOf(rset.getString(2)).getName())) == null) {
            fact = new UFact(URelation.valueOf(rset.getString(2)), new UEntity(rset.getString(1)), new UEntity(rset.getString(3)), rset.getDouble(4));
            dbCache.put(fact);
          } else
            fact = cachedFacts.iterator().next();
          groundedFacts.add(fact);
        }
        dbCache.put(binding1, binding2, binding3, new UFactSet(groundedFacts));
      } else
        groundedFacts.addAll(cachedFacts);
      addLineageToFactsFromDB(groundedFacts, rLineage);

      if (doProcessRules) {
        HashMap<UFact, ULineageOr> lineageFromRules = new HashMap<UFact, ULineageOr>();
        cachedFacts = groundLiteralTopDown(literal, bindings, constants, dependencyGraph, stack, level, doExpandLineage, lineageFromRules);
        addLineageToFactsFromRules(cachedFacts, rLineage, lineageFromRules);
        groundedFacts.addAll(cachedFacts);
      }

    } else if (binding1 != null && binding2 == null && binding3 == null) {

      if ((cachedFacts = dbCache.get(binding1, binding2, binding3)) == null) {
        groundFactStmt4.setString(1, binding1.getName());
        rset = groundFactStmt4.executeQuery();
        while (rset.next()) {
          if ((cachedFacts = dbCache.get(rset.getString(1) + "$" + rset.getString(3) + "$" + URelation.valueOf(rset.getString(2)).getName())) == null) {
            fact = new UFact(URelation.valueOf(rset.getString(2)), new UEntity(rset.getString(1)), new UEntity(rset.getString(3)), rset.getDouble(4));
            dbCache.put(fact);
          } else
            fact = cachedFacts.iterator().next();
          groundedFacts.add(fact);
        }
        dbCache.put(binding1, binding2, binding3, new UFactSet(groundedFacts));
      } else
        groundedFacts.addAll(cachedFacts);
      addLineageToFactsFromDB(groundedFacts, rLineage);

      if (doProcessRules) {
        HashMap<UFact, ULineageOr> lineageFromRules = new HashMap<UFact, ULineageOr>();
        cachedFacts = groundLiteralTopDown(literal, bindings, constants, dependencyGraph, stack, level, doExpandLineage, lineageFromRules);
        addLineageToFactsFromRules(cachedFacts, rLineage, lineageFromRules);
        groundedFacts.addAll(cachedFacts);
      }

    } else if (binding1 == null && binding2 != null && binding3 == null) {

      if ((cachedFacts = dbCache.get(binding1, binding2, binding3)) == null) {
        groundFactStmt5.setString(1, binding2.getName());
        rset = groundFactStmt5.executeQuery();
        while (rset.next()) {
          if ((cachedFacts = dbCache.get(rset.getString(1) + "$" + rset.getString(3) + "$" + URelation.valueOf(rset.getString(2)).getName())) == null) {
            fact = new UFact(URelation.valueOf(rset.getString(2)), new UEntity(rset.getString(1)), new UEntity(rset.getString(3)), rset.getDouble(4));
            dbCache.put(fact);
          } else
            fact = cachedFacts.iterator().next();
          groundedFacts.add(fact);
        }
        dbCache.put(binding1, binding2, binding3, new UFactSet(groundedFacts));
      } else
        groundedFacts.addAll(cachedFacts);
      addLineageToFactsFromDB(groundedFacts, rLineage);

      if (doProcessRules) {
        HashMap<UFact, ULineageOr> lineageFromRules = new HashMap<UFact, ULineageOr>();
        cachedFacts = groundLiteralTopDown(literal, bindings, constants, dependencyGraph, stack, level, doExpandLineage, lineageFromRules);
        addLineageToFactsFromRules(cachedFacts, rLineage, lineageFromRules);
        groundedFacts.addAll(cachedFacts);
      }

    } else if (binding1 != null && binding2 != null && binding3 == null) {

      if ((cachedFacts = dbCache.get(binding1, binding2, binding3)) == null) {
        groundFactStmt6.setString(1, binding1.getName());
        groundFactStmt6.setString(2, binding2.getName());
        rset = groundFactStmt6.executeQuery();
        while (rset.next()) {
          if ((cachedFacts = dbCache.get(rset.getString(1) + "$" + rset.getString(3) + "$" + URelation.valueOf(rset.getString(2)).getName())) == null) {
            fact = new UFact(URelation.valueOf(rset.getString(2)), new UEntity(rset.getString(1)), new UEntity(rset.getString(3)), rset.getDouble(4));
            dbCache.put(fact);
          } else
            fact = cachedFacts.iterator().next();
          groundedFacts.add(fact);
        }
        dbCache.put(binding1, binding2, binding3, new UFactSet(groundedFacts));
      } else
        groundedFacts.addAll(cachedFacts);
      addLineageToFactsFromDB(groundedFacts, rLineage);

      if (doProcessRules) {
        HashMap<UFact, ULineageOr> lineageFromRules = new HashMap<UFact, ULineageOr>();
        cachedFacts = groundLiteralTopDown(literal, bindings, constants, dependencyGraph, stack, level, doExpandLineage, lineageFromRules);
        addLineageToFactsFromRules(cachedFacts, rLineage, lineageFromRules);
        groundedFacts.addAll(cachedFacts);
      }

    } else if (binding1 != null && binding2 != null && binding3 != null) {

      if ((cachedFacts = dbCache.get(binding1, binding2, binding3)) == null) {
        groundFactStmt7.setString(1, binding3.getName());
        groundFactStmt7.setString(2, binding1.getName());
        groundFactStmt7.setString(3, binding2.getName());
        rset = groundFactStmt7.executeQuery();
        while (rset.next()) {
          if ((cachedFacts = dbCache.get(rset.getString(1) + "$" + rset.getString(3) + "$" + URelation.valueOf(rset.getString(2)).getName())) == null) {
            fact = new UFact(URelation.valueOf(rset.getString(2)), new UEntity(rset.getString(1)), new UEntity(rset.getString(3)), rset.getDouble(4));
            dbCache.put(fact);
          } else
            fact = cachedFacts.iterator().next();
          groundedFacts.add(fact);
        }
      } else
        groundedFacts.addAll(cachedFacts);
      addLineageToFactsFromDB(groundedFacts, rLineage);

      // skip redundant resolution steps for single facts
      if (doProcessRules && groundedFacts.size() == 0) {
        HashMap<UFact, ULineageOr> lineageFromRules = new HashMap<UFact, ULineageOr>();
        cachedFacts = groundLiteralTopDown(literal, bindings, constants, dependencyGraph, stack, level, doExpandLineage, lineageFromRules);
        addLineageToFactsFromRules(cachedFacts, rLineage, lineageFromRules);
        // System.out.println(getSp(level) + " ---> " + rLineage);
        groundedFacts.addAll(cachedFacts);
      }

    } else
      System.err.println("FULL FACT SCAN NOT ALLOWED!"); // empty bindings case

    if (rset != null)
      rset.close();

    determineHardRules(groundedFacts);

    return groundedFacts;
  }

  private void addLineageToFactsFromDB(UFactSet facts, HashMap<UFact, ULineageOr> rLineage) {
    for (UFact f : facts) {
      if (rLineage.get(f) == null)
        rLineage.put(f, new ULineageOr(f));
      if (this.dbLineageCache.get(f) == null)
        this.dbLineageCache.put(f, new ULineageAnd(null));// empty body -> from DB / arith
      rLineage.get(f).addChild(this.dbLineageCache.get(f));
    }
  }

  private void addLineageToFactsFromRules(UFactSet facts, HashMap<UFact, ULineageOr> rLineage, HashMap<UFact, ULineageOr> lineageFromRules) {
    for (UFact f : facts) {
      if (rLineage.containsKey(f))
        rLineage.get(f).merge(lineageFromRules.get(f));
      else
        rLineage.put(f, lineageFromRules.get(f));
    }
  }

  public static int SLD_steps = 0;

  public static boolean RECURSIVE_RULES = false;

  public static int MAX_LEVEL = 100;

  private UFactSet groundLiteralTopDown(ULiteral literal, UBindingSet bindings, HashSet<UArgument> constants, UFactSet dependencyGraph, LineageStack stack,
      int level, boolean doExpandLineage, HashMap<UFact, ULineageOr> rLineage) throws Exception {

    UFactSet headGroundings = new UFactSet();
    if (level >= MAX_LEVEL)
      return headGroundings;

    // System.out.println("\nGROUND LITERAL: "+ literal + " " + bindings + " RULES: " + softRules.get(literal, bindings));

    for (USoftRule softRule : softRules.get(literal, bindings)) {
      if (softRule.getHead() == null)
        continue;

      // TOP-DOWN SLD RESOLUTION FOR HORN-CLAUSES: 1) PROPAGATE VARIABLE BINDINGS FROM HEAD LITERAL TO BODY OF CLAUSE
      UBindingSet localBindings = new UBindingSet();
      int freeVariables = 0;

      // BINDING FOR arg1
      if (softRule.getHead().getFirstArgument().isVariable() && !literal.getFirstArgument().isVariable()) {
        // grounding is further restricted to matching domains
        if (softRule.getHead().getRelation().getArgDomain().isSubDomainOf(literal.getRelation().getArgDomain())) {
          localBindings.add(new UBinding(softRule.getHead().getFirstArgument(), literal.getFirstArgument()));
        } else
          continue;
      } else if (softRule.getHead().getFirstArgument().isVariable() && literal.getFirstArgument().isVariable()
          && bindings.getBinding(literal.getFirstArgument()) != null) {
        if (softRule.getHead().getRelation().getArgDomain().isSubDomainOf(literal.getRelation().getArgDomain())) {
          localBindings.add(new UBinding(softRule.getHead().getFirstArgument(), bindings.getBinding(literal.getFirstArgument())));
        } else
          continue;
      } else if (!softRule.getHead().getFirstArgument().isVariable()) {
        if (!softRule.getHead().getFirstArgument().equals(literal.getFirstArgument()))
          continue;
      } else if (softRule.getHead().getFirstArgument().isVariable())
        freeVariables++;

      // BINDING FOR arg2
      if (softRule.getHead().getSecondArgument().isVariable() && !literal.getSecondArgument().isVariable()) {
        // grounding is further restricted to matching domains
        if (softRule.getHead().getRelation().getArgRange().isSubDomainOf(literal.getRelation().getArgRange())) {
          localBindings.add(new UBinding(softRule.getHead().getSecondArgument(), literal.getSecondArgument()));
        } else
          continue;
      } else if (softRule.getHead().getSecondArgument().isVariable() && literal.getSecondArgument().isVariable()
          && bindings.getBinding(literal.getSecondArgument()) != null) {
        if (softRule.getHead().getRelation().getArgRange().isSubDomainOf(literal.getRelation().getArgRange())) {
          localBindings.add(new UBinding(softRule.getHead().getSecondArgument(), bindings.getBinding(literal.getSecondArgument())));
        } else
          continue;
      } else if (!softRule.getHead().getSecondArgument().isVariable()) {
        if (!softRule.getHead().getSecondArgument().equals(literal.getSecondArgument()))
          continue;
      } else if (softRule.getHead().getSecondArgument().isVariable())
        freeVariables++;

      // BINDING FOR relation
      if (softRule.getHead().getRelation().isVariable() && !literal.getRelation().isVariable()) {
        // grounding is further restricted to matching domains
        if (softRule.getHead().getRelation().getDomain().isSubDomainOf(literal.getRelation().getDomain())) {
          localBindings.add(new UBinding(softRule.getHead().getRelation(), literal.getRelation()));
        } else
          continue;
      } else if (softRule.getHead().getRelation().isVariable() && literal.getRelation().isVariable() && bindings.getBinding(literal.getRelation()) != null) {
        if (softRule.getHead().getRelation().getDomain().isSubDomainOf(literal.getRelation().getDomain())) {
          localBindings.add(new UBinding(softRule.getHead().getRelation(), bindings.getBinding(literal.getRelation())));
        } else
          continue;
      } else if (!softRule.getHead().getRelation().isVariable()) {
        if (!softRule.getHead().getRelation().equals(literal.getRelation()))
          continue;
      } else if (softRule.getHead().getRelation().isVariable())
        freeVariables++;

      // CHECK IF THIS EXACT SUBGOAL HAS BEEN RESOLVED BEFORE
      if (stack == null || level == 0)
        stack = new LineageStack();

      // BREAK POTENTIAL TOP-DOWN RECURSION LOOPS
      else {
        // (this is a brute-force break for cycles in SLD resolution!)
        if (stack.contains(softRule, localBindings) || (!RECURSIVE_RULES && stack.containsRule(softRule))) {
          // System.out.println(URDF.getSp(2 * level) + level + " CYCLE: " + literal + " " + localBindings + " " + softRule);
          continue;
        }
      }

      // PUT BINDINGS FOR THIS RESOLUTION STEP INTO THE HASH
      stack.add(softRule, new UBindingSet(localBindings));

      //System.out.println(getSp(level) + "GROUND HEAD-SLD: " + literal + " " + localBindings + " " + softRule);

      // SLD RESOLUTION: 2) RECURSIVELY TRY TO GROUND THE BODY OF THE HORN-CLAUSE
      ArrayList<UFactSet> resultFactSets = new ArrayList<UFactSet>();
      ArrayList<UBindingSet> resultBindings = new ArrayList<UBindingSet>();
      ArrayList<ULineageAnd> localLineage = null;

      if (literal.getRelation().isArithmetic() && freeVariables == 0) {

        // skip recursion if all variables are bound
        resultBindings.add(localBindings);
        resultFactSets.add(new UFactSet(new UFact(literal, localBindings, -1.0)));

      } else {

        HashMap<UFact, ULineageOr> cachedLineage = this.lineageCache.get(stack.getStack());

        if (cachedLineage == null) {

          // otherwise enter next recursion level for grounding
          localLineage = new ArrayList<ULineageAnd>();
          ground(softRule, new UFactSet(), localBindings, resultFactSets, resultBindings, 0, constants, dependencyGraph, stack, level + 1, doExpandLineage,
              true, new ULineageAnd(softRule), localLineage);

        } else {

          // REMOVE THIS RESOLUTION STEP FROM THE GLOBAL STACK
          stack.remove(softRule, localBindings);

          for (UFact f : cachedLineage.keySet()) {
            headGroundings.add(f);
            if (rLineage.containsKey(f)) {
              rLineage.get(f).merge(cachedLineage.get(f));
            } else {
              rLineage.put(f, cachedLineage.get(f));
            }
          }
          continue; // loop over soft rules

        }
      }

      SLD_steps++;

      HashMap<UFact, ULineageOr> currentLineage = new HashMap<UFact, ULineageOr>();
      for (int i = 0; i < resultFactSets.size(); i++) {

        if (softRule.getVariables().size() != resultBindings.get(i).size()) {
          System.err.println("\nINVALID GROUNDING FOR SOFTRULE: " + softRule + " -> " + resultBindings.get(i));
          continue;
        } else if (literal.getRelation().isArithmetic() // this is for arithmetic head literals only
            && (groundLiteral(literal, resultBindings.get(i), constants, dependencyGraph, stack, level + 1, true, true, rLineage)).size() == 0) {// TODO???
          System.err.println(URDF.getSp(level) + "INVALID GROUNDING FOR ARITHMETIC PREDICATE: " + literal + " " + resultBindings.get(i) + " - " + softRule);
          continue;
        }

        // RESOLVE THE NEWLY INFERRED HEAD FACT WITH THE CURRENT RESULT BINDINGS
        UFact headFact = new UFact(softRule.getHead(), resultBindings.get(i), -1.0); // zero confidence for derived facts at this point of inference!
        if (headFact.getFirstArgument() == null || headFact.getSecondArgument() == null || headFact.getRelation() == null) {
          System.err.println("\nINVALID BINDING FOR HEAD PREDICATE: " + headFact + " -> " + resultBindings.get(i));
          continue;
        }

        UFactSet cachedFacts;
        if ((cachedFacts = dbCache.get(headFact.getFirstArgument(), headFact.getSecondArgument(), headFact.getRelation())) != null) {
          headFact = cachedFacts.iterator().next(); // this derived fact is also a base fact
        } else if ((cachedFacts = ruleCache.get(headFact.getFirstArgument(), headFact.getSecondArgument(), headFact.getRelation())) != null) {
          headFact = cachedFacts.iterator().next(); // get reference to cached fact and continue resolution
        } else {
          ruleCache.put(headFact); // else cache this new derived fact in the rule cache
        }

        // ADD LINEAGE INFORMATION TO THE INFERRED FACT
        if (localLineage != null) {// arithmetic head?
          if (rLineage.get(headFact) == null)
            rLineage.put(headFact, new ULineageOr(headFact));
          rLineage.get(headFact).addChild(localLineage.get(i));
          if (currentLineage.get(headFact) == null)
            currentLineage.put(headFact, new ULineageOr(headFact));
          currentLineage.get(headFact).addChild(localLineage.get(i));
        }

        // ADD HEAD TO RESULT SET
        headGroundings.add(headFact);
      }
      this.lineageCache.put(stack.getStack(), currentLineage);

      // REMOVE THIS RESOLUTION STEP FROM THE GLOBAL STACK
      stack.remove(softRule, localBindings);
    }

    return headGroundings;
  }

  private void groundHardRules(HashSet<UArgument> constants, UFactSet dependencyGraph) throws Exception {

    while (this.hardRuleCache.hasNext()) {

      LineageStackEntry entry = this.hardRuleCache.next();
      UHardRule hardRule = (UHardRule) entry.getRule();
      UBindingSet bindings = entry.getBindings();

      UGroundedHardRule thisHardRuleGrounding = new UGroundedHardRule();
      thisHardRuleGrounding.setHardRule(hardRule);
      HashMap<UFact, ULineageOr> allLineage = new HashMap<UFact, ULineageOr>();

      for (ULiteral literal : hardRule.getLiterals()) {
        HashMap<UFact, ULineageOr> lineage = new HashMap<UFact, ULineageOr>();
        UFactSet groundedHardRule = groundLiteral(literal, bindings, constants, dependencyGraph, null, 0, true, true, lineage);

        for (UFact hardRuleFact : groundedHardRule) {
          thisHardRuleGrounding.add(hardRuleFact);
          hardRuleFact.setGroundedHardRule(thisHardRuleGrounding);
          if (allLineage.containsKey(hardRuleFact)) {
            allLineage.get(hardRuleFact).merge(lineage.get(hardRuleFact));
          } else {
            allLineage.put(hardRuleFact, lineage.get(hardRuleFact));
          }
        }
      }
    }
  }

  private void determineHardRules(UFactSet factSet) throws Exception {
    for (UFact fact : factSet)
      for (UHardRule hardRule : hardRules) {
        for (ULiteral literal : hardRule.getLiterals()) {
          // Special grounding pattern for hard rules: do not bind variables with ?? prefix (isGroundable=false)
          UBindingSet localBindings = new UBindingSet();
          if (literal.getFirstArgument().isVariable() && literal.getRelation().getArgDomain().isSubDomainOf(fact.getRelation().getArgDomain())) {
            if (literal.getFirstArgument().isGroundable())
              localBindings.add(new UBinding(literal.getFirstArgument(), fact.getFirstArgument()));
          } else if (!literal.getFirstArgument().equals(fact.getFirstArgument()))
            continue;

          if (literal.getSecondArgument().isVariable() && literal.getRelation().getArgRange().isSubDomainOf(fact.getRelation().getArgRange())) {
            if (literal.getSecondArgument().isGroundable())
              localBindings.add(new UBinding(literal.getSecondArgument(), fact.getSecondArgument()));
          } else if (!literal.getSecondArgument().equals(fact.getSecondArgument()))
            continue;

          if (literal.getRelation().isVariable() && literal.getRelation().getDomain().isSubDomainOf(fact.getRelation().getDomain())) {
            if (literal.getRelation().isGroundable())
              localBindings.add(new UBinding(literal.getRelation(), fact.getRelation()));
          } else if (!literal.getRelation().equals(fact.getRelation()))
            continue;

          // we found a matching hard-rule
          this.hardRuleCache.put(hardRule, localBindings);
          return;
          // this might not work, if there is a hard rule with more than one first-order literal
        }
      }
  }

  // Pre-processing step for MAX-SAT, create inverted index for rules
  public UFactSet invertRules(UFactSet dependencyGraph) {

    // Inverted index with facts pointing to soft rules
    this.invSoftRules = new HashMap<UFact, HashSet<UGroundedSoftRule>>(dependencyGraph.size());

    for (UFact fact : dependencyGraph) {

      fact.setTruthValue(UFact.UNKNOWN);
      if (fact.getBaseConfidence() > 0)
        fact.w_i = fact.getBaseConfidence();
      else
        fact.w_i = -0.1; // Double.MIN_NORMAL; // this is a heuristic: set derived facts to false unless there is also positive evidence from the rules!
      fact.p_i = 0;

      // Inverted lists of soft rules
      HashSet<UGroundedSoftRule> clauses = new HashSet<UGroundedSoftRule>();
      invSoftRules.put(fact, clauses);

      // Add unary competitor sets, if not yet initialized
      if (fact.getGroundedHardRule() == null) {
        fact.setGroundedHardRule(new UGroundedHardRule());
        fact.getGroundedHardRule().add(fact);
        fact.getGroundedHardRule().setHardRule(null); // this is a unary set, no first-order hard rule produced this grounding
        hardRuleGroundings.add(fact.getGroundedHardRule());
      }

      for (UGroundedSoftRule softRuleGrounding : softRuleGroundings) {
        if (softRuleGrounding.getHead().equals(fact) || softRuleGrounding.contains(fact))
          clauses.add(softRuleGrounding);
        // This is for unit clauses with one negated fact only (does not actually occur in our current setting)
        if (softRuleGrounding.size() == 1 && softRuleGrounding.getHead() == null && softRuleGrounding.contains(fact))
          fact.w_i += softRuleGrounding.getWeight();
      }

      // Add unary soft rule
      UGroundedSoftRule unaryClause = new UGroundedSoftRule(fact, fact.w_i); // this is a unary clause, no first-order soft rule produced this grounding
      clauses.add(unaryClause);
      softRuleGroundings.add(unaryClause);
    }

    return dependencyGraph;
  }

  // Pre-processing step for MAX-SAT, create inverted index for rules
  public static Map<UFact, HashSet<UGroundedSoftRule>> invertRules(HashSet<UGroundedSoftRule> softRules, HashSet<UGroundedHardRule> hardRules) {

    // Inverted index with facts pointing to soft rules
    Map<UFact, HashSet<UGroundedSoftRule>> invSoftRules = new HashMap<UFact, HashSet<UGroundedSoftRule>>();

    for (UGroundedHardRule hardRule : hardRules) {
      for (UFact fact : hardRule) {

        fact.setGroundedHardRule(hardRule);
        fact.setTruthValue(UFact.UNKNOWN);
        if (fact.getBaseConfidence() > 0)
          fact.w_i = fact.getBaseConfidence();
        else
          fact.w_i = -0.1; // Double.MIN_NORMAL; // this is a heuristic: set derived facts to false unless there is also positive evidence from the rules!
        fact.p_i = 0;

        // Inverted lists of soft rules
        HashSet<UGroundedSoftRule> clauses = new HashSet<UGroundedSoftRule>();
        invSoftRules.put(fact, clauses);

        for (UGroundedSoftRule softRule : softRules) {
          if (softRule.getHead().equals(fact) || softRule.contains(fact))
            clauses.add(softRule);
          // This is for unit clauses with one negated fact only (does not actually occur in our current setting)
          if (softRule.size() == 1 && softRule.getHead() == null && softRule.contains(fact))
            fact.w_i += softRule.getWeight();
        }

        // Add unary soft rule
        UGroundedSoftRule unaryClause = new UGroundedSoftRule(fact, fact.w_i); // this is a unary clause, no first-order soft rule produced this grounding
        clauses.add(unaryClause);
        softRules.add(unaryClause);
      }
    }

    return invSoftRules;
  }

  private UFactSet evaluateArithmeticPredicate(UArgument argument1, UArgument argument2, URelation relation, int compareValue) throws Exception {
    UFactSet groundedFacts, cachedFacts;
    if ((cachedFacts = dbCache.get(argument1, argument2, relation)) == null) {
      if (argument1 != null && argument2 != null) {
        groundedFacts = new UFactSet();
        try {
          double similarity;
          if ((similarity = ArithmeticPredicate.compareBindings(argument1, argument2, relation, compareValue)) >= 0.0) {
            UFact fact = new UFact(relation, argument1, argument2, similarity);
            groundedFacts.add(fact);
            dbCache.put(fact);
          }
        } catch (NumberFormatException e) {
          throw new NumberFormatException("Not a valid pair of arguments to compare: " + argument1.getName() + " <> " + argument2.getName());
        }
      } else
        throw new Exception("Insufficient binding for arithmetic predicate!");
    } else
      groundedFacts = cachedFacts;
    return groundedFacts;
  }

  private UFactSet evaluateFunctionCall(UArgument argument1, UArgument argument2, URelation relation, int compareValue) throws Exception {
    UFactSet groundedFacts;
    if (relation.getFunctionCall() == null)
      throw new Exception("Unknown function call!");
    if ((groundedFacts = dbCache.get(argument1, argument2, relation)) == null) {
      groundedFacts = relation.getFunctionCall().call(argument1, argument2, relation, compareValue);
      dbCache.put(argument1, argument2, relation, groundedFacts);
      for (UFact fact : groundedFacts)
        dbCache.put(fact);
    }
    return groundedFacts;
  }

  protected static String getSp(int level) {
    String s = level + "\t";
    for (int i = 0; i < level; i++)
      s += "  ";
    return s;
  }

  public void clear() {
    this.softRuleGroundings.clear();
    this.hardRuleGroundings.clear();
    this.globalConstants.clear();
    this.globalDependencyGraph.clear();
    if (invSoftRules != null)
      this.invSoftRules.clear();
    if (this.lineageCache.size() > 5000) {
      this.lineageCache.clear();
      this.ruleCache.clear();
      this.dbCache.clear();
      this.hardRuleCache.clear();
      this.dbLineageCache.clear();
    }
  }

  public void clearRuleForced() {
    this.softRuleGroundings.clear();
    this.hardRuleGroundings.clear();
    this.globalConstants.clear();
    this.globalDependencyGraph.clear();
    if (invSoftRules != null)
      this.invSoftRules.clear();
    this.lineageCache.clear();
    this.ruleCache.clear();
    this.dbCache.clear();
    this.hardRuleCache.clear();
    this.dbLineageCache.clear();
  }

  public void close() throws Exception {
    for (URelation relation : URelation.instances())
      if (relation.isFunctionCall())
        relation.getFunctionCall().close();
    this.groundFactStmt1.close();
    this.groundFactStmt2.close();
    this.groundFactStmt3.close();
    this.groundFactStmt4.close();
    this.groundFactStmt5.close();
    this.groundFactStmt6.close();
    this.groundFactStmt7.close();
    this.connection.close();
  }
}