/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

/**
 * This class encapsulates all the necessary information, that are produced by the reasoner, to send back to the client.
 * 
 * @author Timm Meiser
 * @since 05.01.10
 * @version 1.0
 * 
 */
public class UQueryResult extends UObject {

  /** The global dependency graph, which contains every produced fact (no duplicates). */
  private UFactSet globalDependencyGraph;

  /** The result lineage structures. */
  private ArrayList<ULineageAnd> resultLineage;

  /** The statistics regarding the produced query results. */
  private UStatistics statistics;

  /** The flag to indicate if more facts are available for this query result. */
  private boolean moreFactsAvailable = false;

  /** The total number of distinct result facts -> usually the dependency graph size. */
  private int totalNumberOfFacts = 0;

  /** The empty default constructor for the UQueryResult object. */
  public UQueryResult() {
    super();
    globalDependencyGraph = new UFactSet();
    resultLineage = new ArrayList<ULineageAnd>();
    statistics = new UStatistics();
  }

  /**
   * Initializes this UQueryResult object.
   * 
   * @param globalDG
   *          the set of all distinct facts produced during reasoning.
   * @param resultL
   *          the list of result lineage structures for the latest query.
   * @param stats
   *          the produced statistics for the latest query.
   * */
  public void init(UFactSet globalDG, ArrayList<ULineageAnd> resultL, UStatistics stats, boolean moreFacts, int totalFacts) {
    this.globalDependencyGraph = globalDG;
    this.resultLineage = resultL;
    this.statistics = stats;
    this.moreFactsAvailable = moreFacts;
    this.totalNumberOfFacts = totalFacts;
  }

  /**
   * Sets the global dependency graph (set of all produced facts).
   * 
   * @param globalDependencyGraph
   *          the set of all distinct facts produced during reasoning.
   */
  public void setGlobalDependencyGraph(UFactSet globalDependencyGraph) {
    this.globalDependencyGraph = globalDependencyGraph;
  }

  /**
   * Delivers the global dependency graph (set of all produced facts).
   * 
   * @param globalDependencyGraph
   *          the set of all distinct facts produced during reasoning.
   */
  public UFactSet getGlobalDependencyGraph() {
    return globalDependencyGraph;// = globalDependencyGraph;
  }

  /**
   * @param resultLineage
   *          the resultLineage to set
   */
  public void setResultLineage(ArrayList<ULineageAnd> resultLineage) {
    this.resultLineage = resultLineage;
  }

  /**
   * @return the resultLineage
   */
  public ArrayList<ULineageAnd> getResultLineage() {
    return resultLineage;
  }

  /**
   * Delivers the statistics, regarding the produced query results.
   * 
   * @return the statistics.
   */
  public UStatistics getStatistics() {
    return statistics;
  }

  /**
   * Sets the statistics, regarding the produced query results.
   * 
   * @param statistics
   *          the statistics for the latest query execution.
   */
  public void setStatistics(UStatistics statistics) {
    this.statistics = statistics;
  }

  /**
   * Sets the flag to indicate if more than the sent facts are available.
   * 
   * @param moreFactsAvailable
   *          flag for the fact number indication.
   */
  public void setMoreFactsAvailable(boolean moreFactsAvailable) {
    this.moreFactsAvailable = moreFactsAvailable;
  }

  /**
   * Delivers the flag to indicate if more than the sent facts are available.
   * 
   * @return the flag for the fact number indication.
   */
  public boolean getMoreFactsAvailable() {
    return this.moreFactsAvailable;
  }

  /**
   * Sets the total number of distinct facts available for this query result.
   * 
   * @param totalNumberOfFacts
   *          the total number of available facts for this result.
   */
  public void setTotalNumberOfFacts(int totalNumberOfFacts) {
    this.totalNumberOfFacts = totalNumberOfFacts;
  }

  /**
   * Delivers the total number of distinct facts available for this query result.
   * 
   */
  public int getTotalNumberOfFacts() {
    return this.totalNumberOfFacts;
  }

  /**
   * Adds the given query result to the internally managed list of query results.
   * 
   * @param queryResult
   *          the query result (fact-set) to add.
   * */
  /*
   * public void addQueryResult(UFactSet queryResult) { this.queryResults.add(queryResult); }
   */

  /**
   * Adds the given query result to the internally managed list of query results.
   * 
   * @param queryResult
   *          the query result (fact-set) to add.
   * */
  public void addLineageResult(ULineageAnd lineageResult) {
    this.resultLineage.add(lineageResult);
  }

  /**
   * Reads in the serialized data from the mapped UQueryResult ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
  }

  /**
   * Writes out the data to serialize to the mapped UQueryResult ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    //output.writeObject(globalDependencyGraph);
    // output.writeObject(queryResults.toArray());
    output.writeObject(globalDependencyGraph);
    output.writeObject(resultLineage.toArray());
    output.writeObject(statistics);
    output.writeBoolean(moreFactsAvailable);
    output.writeInt(totalNumberOfFacts);
    // output.writeObject(groundedHardRules.toArray());

    //System.out.println("result facts are : ");
    //System.out.println(resultLineage.toString());
  }

}
