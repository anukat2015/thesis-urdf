/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents the collection of statistical parameters to fill after a run of the URDF-Engine. These statistics will be used by the user interface to
 * give the user statistical information about the latest run of the engine.
 * 
 * @author Timm Meiser
 * @since 18.12.09
 * @version 1.0
 */
public class UStatistics extends UObject {

  /**
   * The time needed to sort the input atoms by their selectivity. The time is specified in milliseconds.
   */
  private int sortInMS = 0;

  /**
   * The time needed to ground the input atoms. The time is specified in milliseconds.
   */
  private int groundInMS = 0;

  /**
   * The time needed to invert the grounded soft rules produced by the reasoner. The time is specified in milliseconds.
   */
  private int invRulInMS = 0;

  /**
   * The time needed to process the MAxSat algorithm on top of the results produced by the reasoning engine. the input atoms by their selectivity. The time is
   * specified in milliseconds.
   */
  private int maxSatInMS = 0;

  /** The complete execution time needed on the server-side. */
  private int execInMS = 0;

  /**
   * The number of distinct results produced by the URDF-Engine, regarding the given input query.
   */
  private int numOfResults = 0;

  /**
   * The number of distinct lineage levels produced by the URDF-Engine, regarding the given input query.
   */
  private int numOfLineageLevels = 0;

  /**
   * The number of distinct soft rules used by the URDF-Engine during reasoning, regarding the given input query.
   */
  private int numOfDistinctSoftRules = 0;

  /**
   * The number of distinct hard rules used by the URDF-Engine during reasoning, regarding the given input query.
   */
  private int numOfDistinctHardRules = 0;

  /**
   * The number of distinct grounded soft rules produced by the URDF-Engine during reasoning, regarding the given input query.
   */
  private int numOfGroundedSoftRules = 0;

  /**
   * The number of distinct grounded grounded rules produced by the URDF-Engine during reasoning, regarding the given input query.
   */
  private int numOfGroundedHardRules = 0;

  /**
   * The number of distinct facts produced by the URDF-Engine during reasoning, regarding the given input query.
   */
  private int numOfDistinctFacts = 0;

  /**
   * The number of distinct relations used by the URDF-Engine during reasoning, regarding the given input query.
   */
  private int numOfDistinctRelations = 0;

  /**
   * The number of distinct entities used by the URDF-Engine during reasoning, regarding the given input query.
   */
  private int numOfDistinctEntities = 0;

  /** The empty default constructor for the UStatistics object. */
  public UStatistics() {
  }

  public void mergeStatistics(UStatistics statistics) {

    execInMS = ((execInMS + statistics.getExecInMS()) / 2);
    sortInMS = ((sortInMS + statistics.getSortInMS()) / 2);
    groundInMS = ((groundInMS + statistics.getGroundInMS()) / 2);
    invRulInMS = ((invRulInMS + statistics.getInvRulInMS()) / 2);
    maxSatInMS = ((maxSatInMS + statistics.getMaxSatInMS()) / 2);
    execInMS = ((execInMS + statistics.getExecInMS()) / 2);
    numOfResults = ((numOfResults + statistics.getNumOfResults()) / 2);
    numOfLineageLevels = ((numOfLineageLevels + statistics.getNumOfLineageLevels()) / 2);
    numOfDistinctSoftRules = ((numOfDistinctSoftRules + statistics.getNumOfDistinctSoftRules()) / 2);
    numOfDistinctHardRules = ((numOfDistinctHardRules + statistics.getNumOfDistinctHardRules()) / 2);
    numOfGroundedSoftRules = ((numOfGroundedSoftRules + statistics.getNumOfGroundedSoftRules()) / 2);
    numOfGroundedHardRules = ((numOfGroundedHardRules + statistics.getNumOfGroundedHardRules()) / 2);
    numOfDistinctFacts = ((numOfDistinctFacts + statistics.getNumOfDistinctFacts()) / 2);
    numOfDistinctRelations = ((numOfDistinctRelations + statistics.getNumOfDistinctRelations()) / 2);
    numOfDistinctEntities = ((numOfDistinctEntities + statistics.getNumOfDistinctEntities()) / 2);
  }

  /**
   * The constructor that initializes the UObject object.
   * 
   * @param name
   *          the name for the UObject object.
   * */
  public UStatistics(String name) {
    super.init(name);
  }

  /**
   * @return the sortInMS
   */
  public int getSortInMS() {
    return sortInMS;
  }

  /**
   * @return the groundInMS
   */
  public int getGroundInMS() {
    return groundInMS;
  }

  /**
   * @return the invRulInMS
   */
  public int getInvRulInMS() {
    return invRulInMS;
  }

  /**
   * @return the maxSatInMS
   */
  public int getMaxSatInMS() {
    return maxSatInMS;
  }

  /**
   * @return the execInMS
   */
  public int getExecInMS() {
    return execInMS;
  }

  /**
   * @return the numOfResults
   */
  public int getNumOfResults() {
    return numOfResults;
  }

  /**
   * @return the numOfLineageLevels
   */
  public int getNumOfLineageLevels() {
    return numOfLineageLevels;
  }

  /**
   * @return the numOfDistinctSoftRules
   */
  public int getNumOfDistinctSoftRules() {
    return numOfDistinctSoftRules;
  }

  /**
   * @return the numOfDistinctHardRules
   */
  public int getNumOfDistinctHardRules() {
    return numOfDistinctHardRules;
  }

  /**
   * @return the numOfGroundedSoftRules
   */
  public int getNumOfGroundedSoftRules() {
    return numOfGroundedSoftRules;
  }

  /**
   * @return the numOfGroundedHardRules
   */
  public int getNumOfGroundedHardRules() {
    return numOfGroundedHardRules;
  }

  /**
   * @return the numOfDistinctFacts
   */
  public int getNumOfDistinctFacts() {
    return numOfDistinctFacts;
  }

  /**
   * @return the numOfDistinctRelations
   */
  public int getNumOfDistinctRelations() {
    return numOfDistinctRelations;
  }

  /**
   * @return the numOfDistinctEntities
   */
  public int getNumOfDistinctEntities() {
    return numOfDistinctEntities;
  }

  /**
   * Sets the time needed to sort the input query atoms by selectivity.
   * <p>
   * The output time is presented in milliseconds.
   * 
   * @param sortInMS
   *          the needed time in milliseconds.
   */
  public void setSortInMS(int sortInMS) {
    this.sortInMS = sortInMS;
  }

  /**
   * Sets the time needed to ground the input query.
   * <p>
   * The grounding time is presented in milliseconds.
   * 
   * @param groundInMS
   *          the needed time in milliseconds.
   */
  public void setGroundInMS(int groundInMS) {
    this.groundInMS = groundInMS;
  }

  /**
   * Sets the time needed to invert the grounded soft rules produced by the reasoner.
   * <p>
   * The inverting time is presented in milliseconds.
   * 
   * @param invRulInMS
   *          the needed time in milliseconds.
   */
  public void setInvRulInMS(int invRulInMS) {
    this.invRulInMS = invRulInMS;
  }

  /**
   * Sets the time needed to process the MaxSat algorithm on the produced reasoner results.
   * <p>
   * The MaxSat time is presented in milliseconds.
   * 
   * @param maxSatInMS
   *          the needed time in milliseconds.
   */
  public void setMaxSatInMS(int maxSatInMS) {
    this.maxSatInMS = maxSatInMS;
  }

  /**
   * Sets the total time needed execute the query on the server.
   * <p>
   * The execution time is presented in milliseconds.
   * 
   * @param execInMS
   *          the total needed execution time on the server in milliseconds.
   */
  public void setExecTime(int execInMS) {
    this.execInMS = execInMS;
  }

  /**
   * Sets the total time needed execute the query on the server (including standard output in the console).
   * <p>
   * The execution time is presented in milliseconds.
   * 
   * @param execStdOutInMS
   *          the total needed execution time on the server in milliseconds (with standard output).
   */
  /*
   * public void setExecTimeStdOut(int execStdOutInMS) { this.execStdOutInMS = execStdOutInMS; }
   */

  /**
   * Sets the number of distinct results to the input query.
   * 
   * @param numOfResults
   *          the number of distinct results to the input query.
   */
  public void setNumOfResults(int numOfResults) {
    this.numOfResults = numOfResults;
  }

  /**
   * Sets the number of distinct lineage levels produced during reasoning.
   * 
   * @param numOfLineageLevels
   *          the number of distinct lineage levels.
   */
  public void setNumOfLineageLevels(int numOfLineageLevels) {
    this.numOfLineageLevels = numOfLineageLevels;
  }

  /**
   * Sets the number of distinct soft rules used during reasoning.
   * 
   * @param numOfDistinctSoftRules
   *          the number of distinct soft rules.
   */
  public void setNumOfDistinctSoftRules(int numOfDistinctSoftRules) {
    this.numOfDistinctSoftRules = numOfDistinctSoftRules;
  }

  /**
   * Sets the number of distinct hard rules used during reasoning.
   * 
   * @param numOfDistinctHardRules
   *          the number of distinct hard rules.
   */
  public void setNumOfDistinctHardRules(int numOfDistinctHardRules) {
    this.numOfDistinctHardRules = numOfDistinctHardRules;
  }

  /**
   * Sets the number of distinct grounded soft rules produced during reasoning.
   * 
   * @param numOfGroundedSoftRules
   *          the number of distinct grounded soft rules used.
   */
  public void setNumOfGroundedSoftRules(int numOfGroundedSoftRules) {
    this.numOfGroundedSoftRules = numOfGroundedSoftRules;
  }

  /**
   * Delivers the number of distinct grounded hard rules produced during reasoning.
   * 
   * @param numOfGroundedHardRules
   *          the number of distinct grounded hard rules produced.
   */
  public void setNumOfGroundedHardRules(int numOfGroundedHardRules) {
    this.numOfGroundedHardRules = numOfGroundedHardRules;
  }

  /**
   * Sets the number of distinct facts produced during reasoning.
   * 
   * @param numOfDistinctFacts
   *          the number of distinct facts produced.
   */
  public void setNumOfDistinctFacts(int numOfDistinctFacts) {
    this.numOfDistinctFacts = numOfDistinctFacts;
  }

  /**
   * Delivers the number of distinct relations used during reasoning.
   * 
   * @param numOfDistinctRelations
   *          the number of distinct facts used.
   */
  public void setNumOfDistinctRelations(int numOfDistinctRelations) {
    this.numOfDistinctRelations = numOfDistinctRelations;
  }

  /**
   * Delivers the number of distinct relations produced during reasoning.
   * 
   * @param numOfDistinctEntities
   *          the number of distinct facts produced.
   */
  public void setNumOfDistinctEntities(int numOfDistinctEntities) {
    this.numOfDistinctEntities = numOfDistinctEntities;
  }

  /**
   * Reads in the serialized data from the mapped URelation ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    /*
     * super.readExternal(input); sortInMS = input.readInt(); groundInMS = input.readInt(); invRulInMS = input.readInt(); maxSatInMS = input.readInt();
     * numOfResults = input.readInt(); numOfLineageLevels = input.readInt(); numOfDistinctSoftRules = input.readInt(); numOfDistinctHardRules = input.readInt();
     * numOfGroundedSoftRules = input.readInt(); numOfGroundedHardRules = input.readInt(); numOfDistinctFacts = input.readInt(); numOfDistinctRelations =
     * input.readInt(); numOfDistinctEntities = input.readInt();
     */
  }

  /**
   * Writes out the data to serialize to the mapped URelation ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeInt(sortInMS);
    output.writeInt(groundInMS);
    output.writeInt(invRulInMS);
    output.writeInt(maxSatInMS);
    output.writeInt(execInMS);
    // output.writeInt(execStdOutInMS);
    output.writeInt(numOfResults);
    output.writeInt(numOfLineageLevels);
    output.writeInt(numOfDistinctSoftRules);
    output.writeInt(numOfDistinctHardRules);
    output.writeInt(numOfGroundedSoftRules);
    output.writeInt(numOfGroundedHardRules);
    output.writeInt(numOfDistinctFacts);
    output.writeInt(numOfDistinctRelations);
    output.writeInt(numOfDistinctEntities);
  }

}
