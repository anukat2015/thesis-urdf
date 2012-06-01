/**
 * 
 */
package urdf.api {

import flash.utils.IDataInput;
import flash.utils.IDataOutput;

/**
 * This class encapsulates all the necessary information, that are produced by the reasoner, to send back to the client.
 * 
 * @author Timm Meiser
 * @since 05.01.10
 * @version 1.0
 * 
 */
[Bindable]
[RemoteClass(alias="urdf.api.UQueryResult")]
public class UQueryResult extends UObject {
  
  /** The global dependency graph, which contains every produced fact (no duplicates). */	
  //private UFactSet globalDependencyGraph;
  private var globalDependencyGraph:UFactSet;
  
  /** The list of produced fact sets that represent the various possible query results. */
  //private List<UFactSet> queryResults;
  //private var queryResults:Array;
  //private var queryResults:ArrayCollection;
  
  /** The result lineage structures. */
  //private ArrayList<ULineageAnd> resultLineage;
  private var resultLineage:Array;

  /** The statistics regarding the produced query results. */
  private var statistics:UStatistics;
  
   /** The flag to indicate if more facts are available for this query result. */
  private var moreFactsAvailable:Boolean = false;
  
  /** The total number of distinct result facts -> usually the dependency graph size. */
  private var totalNumberOfFacts:int = 0;

  /** The empty default constructor for the UQueryResult object. */
  public function UQueryResult() {
    super();
    globalDependencyGraph = new UFactSet();
    //queryResults = new Array();
    resultLineage = new Array();
    statistics = new UStatistics();
  }

  /**
   * Initializes this UQueryResult object.
   * 
   * @param globalDependencyGraph
   *          the set of all distinct facts produced during reasoning.
  * @param resultLineage
	 *          the list of result lineage structures for the latest query.
   * @param statistics
   *          the produced statistics for the latest query.
   * */
  //public function init(List<UFactSet> queryResults, statistics:UStatistics):void {
  public function initQueryResult(globalDependencyGraph:UFactSet, resultLineage:Array, statistics:UStatistics, moreFactsAvailable:Boolean, totalNumberOfFacts:int):void {
  	this.globalDependencyGraph = globalDependencyGraph;	
    //this.queryResults = queryResults;
    this.resultLineage = resultLineage;
    this.statistics = statistics;
    this.moreFactsAvailable = moreFactsAvailable;
    this.totalNumberOfFacts = totalNumberOfFacts;
  }

  /**
   * Delivers the global dependency graph (set of all produced facts).
   * 
   * @return the set of all distinct facts produced during reasoning.	
   */
  public function getGlobalDependencyGraph():UFactSet {
    return globalDependencyGraph;
  }

  /**
   * Delivers the list of result UFactSet objects.
   * 
   * @return the result fact-sets.
   */
  //public List<UFactSet> getQueryResults() {
  /*public function getQueryResults():Array {
    return queryResults;
  }
  */

  /**
   * @return the resultLineage
   */
  public function getResultLineage():Array {
	  return resultLineage;
  }

  /**
   * Delivers the statistics, regarding the produced query results.
   * 
   * @return the statistics.
   */
  public function getStatistics():UStatistics {
    return statistics;
  }
  
  /**
   * Delivers the flag to indicate if more than the sent facts are available.
   * 
   * @return the flag for the fact number indication.
   */
  public function getMoreFactsAvailable():Boolean {
	  return this.moreFactsAvailable;
  }
  
  /**
   * Delivers the total number of distinct facts available for this query result.
   * 
   */
  public function getTotalNumberOfFacts():int {
	  return this.totalNumberOfFacts;
  }
  
  /**
   * Returns the string representation of the whole UQueryResult object (all values within the object).
   * 
   * @return the string representation of the object.
   */
  override public function toString():String {
    
    var str:String = "";
    str += "DependencyGraph : " + this.globalDependencyGraph.toString();
    str += "QueryResult : " + this.resultLineage.toString();//this.queryResults.toString();
    //str += " || Statistics : " + this.statistics.toString();
    
    return str;
  }
  
  /** 
   * Reads in the serialized data from the mapped UQueryResult Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
	// read in the global dependency graph
	//globalDependencyGraph = (UFactSet)(input.readObject());
	
	// set the dependency graph
	globalDependencyGraph = (UFactSet)(input.readObject());
	
	// read in the query results
	var tempSet:Object = input.readObject();
    //for each (var factSet:UFactSet in tempSet)
    //for each (var factSet:Object in tempSet)
      // queryResults.push(factSet);
    for each (var lineageResult:Object in tempSet)
       resultLineage.push(lineageResult);
	
	// read in the server statistics
	statistics = (UStatistics)(input.readObject());
	// read in the more facts available flag
	moreFactsAvailable = input.readBoolean();
	// read in the total number of facts available for this query result
	totalNumberOfFacts = input.readInt();
	
	/*
	var i:int=0;
	var j:int=0;
	trace("----------------------------");
	trace("Lineage at the client side!");
	trace("----------------------------");
	for each (var result:ULineageAnd in resultLineage) {
		trace("\nRESULT [" + i + "|" + (j + 1) + "/" + resultLineage.length + "]\t\t");
		trace(result.toStringPerLevel(0));
		// result.writeGraphVizFile(); // write a graph viz file including the hard rule lineage
		j++;
	}
	*/
  }

  /**
   * Writes out the data to serialize to the mapped UQueryResult Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
	/*output.writeObject(queryResults);
	output.writeObject(statistics);
	output.writeObject(groundedSoftRules);
	output.writeObject(groundedHardRules);
	output.writeObject(bindings);
    */
  }	

}

}