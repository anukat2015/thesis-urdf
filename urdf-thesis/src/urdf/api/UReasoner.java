/**
 * 
 */
package urdf.api;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import urdf.reasoner.MAXSAT;
import urdf.reasoner.Sampling;
import urdf.reasoner.URDF;
import urdf.tools.RuleParser;
import flex.messaging.FlexContext;
import flex.messaging.FlexSession;

/**
 * 
 * This class is the key class for the URDF-API. Here, all the communication between the user interface (at the browser-side) and the application (at the
 * server-side) takes place.
 * 
 * @author Timm Meiser and Martin Theobald
 * @since 07.02.2010
 * @version 1.0
 * */
public class UReasoner extends UObject {

  /** The list of default soft rules to use for the reasoning engine. */
  private List<USoftRule> defaultSoftRules;

  /** The list of combined default and user hard rules to use for the reasoning engine. */
  private List<UHardRule> defaultHardRules;

  /**
   * The URuleStore object that stores the lists of default rules in a special structure to transmit to the Flash client.
   */
  private URuleStore ruleStore;

  /** The input stream for the database parameters. */
  private InputStream dbParamaetersStream;

  private URDF urdf;

  private boolean dataLoading = false;

  public static int numOfInstances = 0;

  private FlexSession session;

  private String sessionID = "";

  public static HashMap<String, UReasoner> reasonerHash = new HashMap<String, UReasoner>();

  /**
   * The constructor of the reasoner object which is called once for every user (session).
   * */
  public UReasoner() {

    super();

    session = FlexContext.getFlexSession();
    System.out.println("SESSION: " + session);
    session.setTimeoutPeriod(20000);
    session.addSessionDestroyedListener(new UVizSessionListener());

    // add the own session id
    this.sessionID = session.getId();

    // if (!reasonerHash.containsKey(session.getId()))
    // reasonerHash.put(session.getId(), this);
    if (!reasonerHash.containsKey(this.sessionID))
      reasonerHash.put(this.sessionID, this);

    // numOfInstances++;
    /*
     * System.out.println(" --------------------------------------- "); System.out.println("I created the reasoner object !!!!!!!!");
     * System.out.println("Number of instances is : " + numOfInstances); System.out.println("Number of instances in reasoner hash is : " + reasonerHash.size());
     * System.out.println(" --------------------------------------- ");
     */
    try {

      defaultSoftRules = new ArrayList<USoftRule>();
      defaultHardRules = new ArrayList<UHardRule>();
      dbParamaetersStream = this.getClass().getClassLoader().getResource("yago.ini").openStream();

      // -----------------------------------------------------------
      // set the rule store values
      RuleParser.parseAll(this.getClass().getClassLoader().getResource("rules.txt").openStream(), defaultSoftRules, defaultHardRules, new ArrayList<UQuery>());
      ruleStore = new URuleStore(defaultSoftRules, defaultHardRules);
      // -----------------------------------------------------------

      urdf = new URDF(dbParamaetersStream, defaultSoftRules, defaultHardRules);

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }

  }

  /**
   * Kills this session and reasoner object.
   * 
   * @throws Throwable
   * */
  public boolean resetDBConnection() throws Throwable {
    // clear URDF
    boolean succesful = resetReasoner();
    // System.gc();
    // UReasoner.killReasoner(this.sessionID);
    return succesful;
  }

  public boolean resetReasoner() {
    // now, clear the reasoner and close the database connection
    // this.urdf.clear();
    // Connection connection = this.urdf.getConnection();
    // roll-back the current connection made changes to DB
    try {
      // connection.rollback();
      this.urdf.clear();
      // this.urdf.abortConnections();
      this.urdf.close();
      System.gc();
      this.urdf = new URDF(dbParamaetersStream, defaultSoftRules, defaultHardRules);
    } catch (Exception e) {
      System.out.println("Error is " + e);
      e.printStackTrace();
      return false;
    }
    /*
     * this.urdf = null; this.dbParamaeters = null; this.defaultHardRules.clear(); this.defaultHardRules = null; this.defaultSoftRules.clear();
     * this.defaultSoftRules = null; this.ruleStore.clear(); this.ruleStore = null;
     */
    // this.session.invalidate();
    // this.session = null;
    // System.out.println(" I cleared the UReasoner instance !");
    return true;
  }

  /**
   * Processes the given query, using the specified soft rules and hard rules.
   * 
   * @param query
   *          the query that should be executed.
   * @return the query result for the given input query.
   * */
  public UQueryResult processQuery(UQuery query, URuleStore rules, boolean doProcessRules, boolean useAllResults, int numOfReturnedFacts) throws Exception {
    // the array of result fact sets
    // ArrayList<UFactSet> tempResultFacts = new ArrayList<UFactSet>();

    UFactSet newDependencyGraph;// = new UFactSet();

    // check the number of result facts
    if (numOfReturnedFacts < 0 || numOfReturnedFacts > 300)
      numOfReturnedFacts = 100;

    boolean moreFactsAvailable = false;

    // the needed complete execution time
    long executionTime = System.currentTimeMillis();

    UQueryResult queryResult = new UQueryResult();
    UStatistics statistics = new UStatistics();
    queryResult.init("QueryResult");

    long time;

    try {
      // set the rules for the reasoner
      // urdf.setHardRules(rules.getHardRules()); // just overwrite the hard rule list
      // urdf.setSoftRules(rules.getSoftRules()); // just overwrite the soft rule list
      urdf.setRules(rules);
      // System.out.println("hard rules are : ");
      // System.out.println(rules.getHardRules().toString());
      // System.out.println("soft rules are : ");
      // System.out.println(rules.getSoftRules().toString());
      // System.out.println("Process Rules ? - " + doProcessRules);
      // System.out.println("query is : ");
      // System.out.println(query.toString());

      List<UFactSet> resultFacts = new ArrayList<UFactSet>();
      List<UBindingSet> resultBindings = new ArrayList<UBindingSet>();
      ArrayList<ULineageAnd> resultLineage = new ArrayList<ULineageAnd>();

      time = System.currentTimeMillis();
      urdf.sortBySelectivity(query, new UBindingSet());
      statistics.setSortInMS((int) (System.currentTimeMillis() - time));

      time = System.currentTimeMillis();
      urdf.ground(query, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, new HashSet<UArgument>(), new UFactSet(), null, 0, false,
          doProcessRules, new ULineageAnd(null), resultLineage);
      statistics.setGroundInMS((int) (System.currentTimeMillis() - time));
      // System.out.println("result facts are : ");
      // System.out.println(resultLineage.toString());

      if (doProcessRules) {

        time = System.currentTimeMillis();
        urdf.invertRules(urdf.globalDependencyGraph);
        statistics.setInvRulInMS((int) (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).processMAXSAT();
        statistics.setMaxSatInMS((int) (System.currentTimeMillis() - time));

      }

      // This is a simple PW-based confidence sampling
      // time = System.currentTimeMillis();
      Sampling.getConfAll(resultLineage);
      Sampling.getConfAll(urdf.globalDependencyGraph);

      /*
       * time = System.currentTimeMillis(); // urdf.ground(query, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, new HashSet<UArgument>(),
       * new UFactSet(), null, 0, false); urdf.ground(query, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, new HashSet<UArgument>(), new
       * UFactSet(), null, 0, false, doProcessRules, new ULineageAnd(null), resultLineage); statistics.setGroundInMS((int) (System.currentTimeMillis() - time));
       * 
       * time = System.currentTimeMillis(); urdf.invertRules(urdf.globalConstants, urdf.globalDependencyGraph); statistics.setInvRulInMS((int)
       * (System.currentTimeMillis() - time));
       * 
       * time = System.currentTimeMillis(); new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).processMAXSAT();
       * statistics.setMaxSatInMS((int) (System.currentTimeMillis() - time));
       * 
       * // This is a simple PW-based confidence sampling // time = System.currentTimeMillis(); Sampling.getConfAll(resultLineage);
       * Sampling.getConfAll(urdf.globalDependencyGraph);
       */

      /*
       * j = 0; for (UFactSet result : resultFacts) { // add the current result fact set tempResultFacts.add(result); }
       */

      newDependencyGraph = new UFactSet(urdf.globalDependencyGraph);
      int size = newDependencyGraph.size();

      // statistics.setNumOfDistinctFacts(tempResultFacts.size());
      statistics.setNumOfDistinctFacts(size); // todo : check if this is the correct value here
      statistics.setNumOfDistinctHardRules(rules.getHardRules().size());
      statistics.setNumOfDistinctSoftRules(rules.getSoftRules().size());
      statistics.setNumOfGroundedHardRules(urdf.hardRuleGroundings.size());
      statistics.setNumOfGroundedSoftRules(urdf.softRuleGroundings.size());

      // set the total number of facts
      queryResult.setTotalNumberOfFacts(size);

      // ok, we have to reduce the number of facts, in case this is the first call
      if (newDependencyGraph.size() > numOfReturnedFacts)
        moreFactsAvailable = true;

      // we submitted the query again, this time every result fact will be serialized and sent to the client
      // queryResult.init(newDependencyGraph, tempResultFacts, statistics, moreFactsAvailable, size);
      queryResult.init(newDependencyGraph, resultLineage, statistics, moreFactsAvailable, size);

      // System.out.println("Result is computed !!!!!!!!");

      // we have to restrict the number of result fact -> adjust the dependency graph and the result list
      if (!useAllResults && moreFactsAvailable) {

        // if we use data loading -> we handle this case separately later on
        if (!dataLoading) {
          produceResultsAndDependencyGraph(queryResult, numOfReturnedFacts);
          // queryResult.setMoreFactsAvailable(true);
        }
      } else
        queryResult.setMoreFactsAvailable(false);

      resultFacts.clear();
      resultBindings.clear();
      // do not clear this structure because we take this structure for the result to
      // serialize! -> by clearing this -> no result at the client
      // resultLineage.clear();
      // urdf.softRuleGroundings.clear();
      // urdf.hardRuleGroundings.clear();
      // clear URDF
      urdf.clear();
      // urdf.clearRuleForced();

      System.gc();

      // urdf.close();

      // set the data loading flag again
      dataLoading = false;

      int exTime = (int) (System.currentTimeMillis() - executionTime);
      queryResult.getStatistics().setExecTime(exTime);

      return queryResult;

    } catch (Exception e) {
      urdf.softRuleGroundings.clear();
      urdf.hardRuleGroundings.clear();
      // clear URDF
      urdf.clear();
      System.gc();
      e.printStackTrace();
    }// System.exit(1);}

    return queryResult;

  }

  /**
   * Processes the given query, using the specified soft rules and hard rules.
   * 
   * @param query
   *          the query that should be executed.
   * @return the query result for the given input query.
   * */
  public UQueryResult loadData(UQuery queryOne, UQuery queryTwo, URuleStore rules, boolean doProcessRules, boolean useAllResults, int numOfReturnedFacts)
      throws Exception {

    UQueryResult resultFinal = new UQueryResult();
    resultFinal.init("QueryResult");
    UQueryResult result = new UQueryResult();
    result.init("QueryResult");

    // update the session timeout
    // session.setTimeoutPeriod(100);

    try {

      // set the data loading flag again
      dataLoading = true;

      resultFinal = processQuery(queryOne, ruleStore, doProcessRules, useAllResults, numOfReturnedFacts);

      // set the data loading flag again
      dataLoading = true;
      // set the flags for changed rules to false, because
      // there can not be a rule change between both query execution actions
      rules.setHardRulesChanged(false);
      rules.setSoftRulesChanged(false);

      result = processQuery(queryTwo, ruleStore, doProcessRules, useAllResults, numOfReturnedFacts);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // merge the dependency graphs from both results
    UFactSet dependencyGraph = resultFinal.getGlobalDependencyGraph();
    dependencyGraph.addAll(result.getGlobalDependencyGraph());

    // merge the query results from both results
    // resultFinal.getQueryResults().addAll(result.getQueryResults());
    resultFinal.getResultLineage().addAll(result.getResultLineage());

    // set the total number of facts
    resultFinal.setTotalNumberOfFacts(resultFinal.getGlobalDependencyGraph().size());
    // merge both statistics, by averaging the values
    resultFinal.getStatistics().mergeStatistics(result.getStatistics());

    // ok, we have to reduce the number of facts, in case this is the first call
    if (resultFinal.getTotalNumberOfFacts() > numOfReturnedFacts)
      resultFinal.setMoreFactsAvailable(true);

    // we submitted the query again, this time every result fact will be serialized and sent to the client
    if (!useAllResults && resultFinal.getMoreFactsAvailable())
      produceResultsAndDependencyGraph(resultFinal, numOfReturnedFacts);
    else
      resultFinal.setMoreFactsAvailable(false);

    // statistics are left out as they aren`t important for this operation anyway

    // set the data loading flag again
    dataLoading = false;

    return resultFinal;

  }

  /**
   * Delivers the internally stored default soft and hard rules as an array of lists.
   * 
   * @return the array of the default soft and hard rule lists.
   * */
  public URuleStore requestRules() {
    // System.out.println("Soft Rules are : " + ruleStore.getSoftRules().toString());
    // System.out.println("Hard Rules are : " + ruleStore.getHardRules().toString());
    return this.ruleStore;
  }

  /**
   * Delivers the internally stored default soft rules.
   * 
   * @return the list of default soft rules.
   * */
  public List<USoftRule> requestSoftRules() {
    return defaultSoftRules;
  }

  /**
   * Delivers the internally stored default hard rules.
   * 
   * @return the list of default hard rules.
   * */
  public List<UHardRule> requestHardRules() {
    return defaultHardRules;
  }

  private void produceResultsAndDependencyGraph(UQueryResult queryResult, int numOfReturnedFacts) {

    HashSet<UFact> factHash = new HashSet<UFact>();
    UFactSet newDependencyGraph = new UFactSet();
    ArrayList<ULineageAnd> newLineageResults = new ArrayList<ULineageAnd>();

    int i = 0, j = 0;//
    int size = queryResult.getResultLineage().size();

    // UFactSet factSet;
    ULineageAnd lineageResult;
    HashSet<ULineageAbstract> children;
    ULineageOr lineageFact;
    UFact fact;

    for (i = 0; i < size; i++) {
      // we allow only 200 distinct facts
      if (j >= numOfReturnedFacts)
        break;

      lineageResult = queryResult.getResultLineage().get(i);
      children = lineageResult.children;

      for (ULineageAbstract lineageOr : children) {
        lineageFact = (ULineageOr) lineageOr;
        fact = lineageFact.getFact();

        // fact is already in hash -> continue
        if (factHash.contains(fact))
          // System.out.println("fact is already in hash : " + fact.toString());
          continue;

        // add the fact to the hash
        factHash.add(fact);
        j++;
      }
    }

    // System.out.println("i is : " + i);
    // System.out.println("j is : " + j);

    // we can return every fact
    if (i >= size) {
      // dependency graph is much larger than the result -> fill the remaining facts with facts from the dependency graph
      if (factHash.size() < numOfReturnedFacts) {
        for (UFact f : queryResult.getGlobalDependencyGraph()) {
          // we reached the limit
          if (factHash.size() >= numOfReturnedFacts)
            break;
          factHash.add(f);
        }
      }
    }

    // copy only the used query results into the new query result list
    for (int l = 0; l < i; l++)
      newLineageResults.add(queryResult.getResultLineage().get(l));

    for (UFact hashFact : factHash)
      newDependencyGraph.add(hashFact);

    // overwrite the old dependency graph with the new one
    queryResult.setGlobalDependencyGraph(newDependencyGraph);
    // overwrite the old query results with the new ones
    queryResult.setResultLineage(newLineageResults);
  }

  /**
   * Check whether the the two given facts are the same or not.
   * 
   * @param fact1
   *          the first fact to check.
   * @param fact2
   *          the second fact to check.
   * @return true, if both facts are the same, false otherwise.
   * */
  protected static boolean equals(UFactSet fact1, UFactSet fact2) {
    return fact1.size() == fact2.size() && fact1.containsAll(fact2) && fact2.containsAll(fact1);
  }

  /**
   * Checks whether the given list of fact sets contains the specified fact set or not.
   * 
   * @param factSetList
   *          the list of fact sets to check.
   * @param factSet
   *          the fact set to check.
   * @return true, if the list of fact sets contains the given fact set, false otherwise.
   * */
  protected static boolean contains(List<UFactSet> factSetList, UFactSet factSet) {
    for (UFactSet f : factSetList)
      if (equals(f, factSet))
        return true;
    return false;
  }

  public static String printLineage(UFactSet facts) {
    StringBuffer s = new StringBuffer();
    for (UFact fact : facts)
      s.append(fact.getLineage().toString(0));
    return s.toString();
  }

  public static String getSp(int level) {
    String s = String.valueOf(level) + "\t";
    for (int i = 0; i < level; i++)
      s += "|";
    return s;
  }

  public void clear() throws Exception {
    // now, clear the reasoner and close the database connection
    // this.urdf.clear();
    this.urdf.close();
    this.urdf = null;
    this.defaultHardRules.clear();
    this.defaultHardRules = null;
    this.defaultSoftRules.clear();
    this.defaultSoftRules = null;
    this.ruleStore.clear();
    this.ruleStore = null;
    this.session.invalidate();
    this.session = null;
    // System.out.println(" I cleared the UReasoner instance !");
  }

  protected void finalize() throws Throwable {

    // now, clear the reasoner and close the database connection
    // this.urdf.clear();
    // this.urdf.close();
    // System.gc();

    // session.invalidate();
    // session = null;

    // do finalization here
    super.finalize(); // not necessary if extending Object.
  }

  /**
   * The destructor for the UReasoner object.
   * */
  public static void killReasoner(String sessionID) throws Throwable {
    numOfInstances--;

    if (!reasonerHash.containsKey(sessionID))
      return;

    UReasoner reasoner = reasonerHash.remove(sessionID);
    if (reasonerHash.isEmpty())
      reasonerHash.clear();

    /*
     * System.out.println(" --------------------------------------- "); System.out.println("I destroyed the reasoner object !!!!!!!!");
     * System.out.println("Number of instances is : " + numOfInstances); System.out.println("Number of instances in reasoner hash is : " + reasonerHash.size());
     * System.out.println(" --------------------------------------- ");
     */

    // do finalization here
    // now, clear the reasoner and close the database connection
    reasoner.clear();
    // System.gc();

    // reasoner.finalize(); //not necessary if extending Object.
    reasoner = null;

    // System.out.println(" System is clean now !");
    System.gc();
  }

  static void dump(UQuery query, boolean processRules) throws Exception {
    BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Masterthesis\\Projects\\UViz\\QueryLog" + query.getName() + ".txt")); // should be "UTF-8"
    writer.write("Literals are : \n");
    for (ULiteral literal : query.getLiterals()) {
      // writer.write(id + "\t" + (long) fact.getId() + "\t" + fact.getRelationName() + "\t" + fact.getFirstArgumentName() + "\t" + fact.getSecondArgumentName()
      // + "\t" + fact.getConfidence() + "\n");
      writer.write(literal.toString() + "\n");
    }
    writer.write("Variables are : " + query.getVariables() + "\n");
    writer.write("Constants are : " + query.getConstants() + "\n");
    writer.write("Process Rules : " + processRules + "\n");

    writer.flush();
    writer.close();
  }

  static void dump(UQuery query, UQueryResult queryResult, URuleStore rules, boolean processRules, boolean useAllResults, int numOfReturnedFacts)
      throws Exception {
    BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Masterthesis\\Projects\\UViz\\QueryResultLog" + query.getName() + ".txt")); // should be
    // "UTF-8"
    // for (UFactSet factSet : queryResult.getQueryResults()) {
    for (ULineageAbstract lineageResults : queryResult.getResultLineage()) {
      // writer.write(id + "\t" + (long) fact.getId() + "\t" + fact.getRelationName() + "\t" + fact.getFirstArgumentName() + "\t" + fact.getSecondArgumentName()
      // + "\t" + fact.getConfidence() + "\n");
      // writer.write(factSet.toString() + "\n");
      writer.write(lineageResults.toString() + "\n");
    }
    writer.write("Used Soft Rules are: " + rules.getSoftRules().toString() + "\n");
    writer.write("Used Hard Rules are: " + rules.getHardRules().toString() + "\n");
    writer.write("Process Rules : " + processRules + "\n");
    writer.write("Use all Results : " + useAllResults + "\n");
    writer.write("Number of allowed Facts : " + numOfReturnedFacts + "\n");
    writer.flush();
    writer.close();
  }
}
