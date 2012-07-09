/**
 * 
 */
package urdf.api {
	
import flash.utils.IDataInput;
import flash.utils.IDataOutput;
	
/**
 * This class represents the collection of statistical parameters to fill after a run of
 * the URDF-Engine. These statistics will be used by the user interface to give the user 
 * statistical information about the latest run of the engine. 
 * 
 * @author Timm Meiser 
 * @since 18.12.09
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.UStatistics")]
public class UStatistics extends UObject {

	/** The time needed to sort the input atoms by their selectivity. The time is
	 *  specified in milliseconds. */
	private var sortInMS:int = 0;
	/** The time needed to ground the input atoms. The time is
	 *  specified in milliseconds. */
	private var groundInMS:int = 0;
	/** The time needed to invert the grounded soft rules produced by the reasoner. 
	 * The time is specified in milliseconds. */
	private var invRulInMS:int = 0;
	/** The time needed to process the MAxSat algorithm on top of the results produced 
	 * by the reasoning engine. the input atoms by their selectivity. The time is 
	 * specified in milliseconds. */
	private var maxSatInMS:int = 0;
	/** The complete execution time needed on the server-side. */
	private var execInMS:int = 0;
	/** The complete execution time (standard output in the console included) needed on the server-side. */
	//private var execStdOutInMS:int = 0;
	
	/** The number of distinct results produced by the URDF-Engine, regarding the 
	 * given input query. */
	private var numOfResults:int = 0;
	/** The number of distinct lineage levels produced by the URDF-Engine, regarding the 
	 * given input query. */
	private var numOfLineageLevels:int = 0;
	/** The number of distinct soft rules used by the URDF-Engine during reasoning, regarding the 
	 * given input query. */
	private var numOfDistinctSoftRules:int = 0;
	/** The number of distinct hard rules used by the URDF-Engine during reasoning, regarding the 
	 * given input query. */
	private var numOfDistinctHardRules:int = 0;
	/** The number of distinct grounded soft rules produced by the URDF-Engine during reasoning, 
	 * regarding the given input query. */
	private var numOfGroundedSoftRules:int = 0;
	/** The number of distinct grounded grounded rules produced by the URDF-Engine during reasoning, 
	 * regarding the given input query. */
	private var numOfGroundedHardRules:int = 0;
	/** The number of distinct facts produced by the URDF-Engine during reasoning, 
	 * regarding the given input query. */
	private var numOfDistinctFacts:int = 0;
	/** The number of distinct relations used by the URDF-Engine during reasoning, 
	 * regarding the given input query. */
	private var numOfDistinctRelations:int = 0;
	/** The number of distinct entities used by the URDF-Engine during reasoning, 
	 * regarding the given input query. */
	private var numOfDistinctEntities:int = 0;

	
	/** The empty default constructor for the UStatistics object. */
	public function UStatistics() {}

	/**
	 * Delivers the time needed to sort the input query atoms by selectivity.
	 * <p>
	 * The output time is presented in milliseconds.
	 * 
	 * @return the needed time in milliseconds.
	 */
	public function getSortInMS():int {
		return sortInMS;
	}

	/**
	 * Delivers the time needed to ground the input query.
	 * <p>
	 * The grounding time is presented in milliseconds.
	 * 
	 * @return the needed time in milliseconds.
	 */
	public function getGroundInMS():int {
		return groundInMS;
	}

	/**
	 * Delivers the time needed to invert the grounded soft 
	 * rules produced by the reasoner.
	 * <p>
	 * The inverting time is presented in milliseconds.
	 * 
	 * @return the needed time in milliseconds.
	 */
	public function getInvRulInMS():int {
		return invRulInMS;
	}

	/**
	 * Delivers the time needed to process the MaxSat 
	 * algorithm on the produced reasoner results.
	 * <p>
	 * The MaxSat time is presented in milliseconds.
	 * 
	 * @return the needed time in milliseconds.
	 */
	public function getMaxSatInMS():int {
		return maxSatInMS;
	}

    /**
	 * Delivers the total time needed execute the query on the server.
	 * <p>
	 * The execution time is presented in milliseconds.
	 * 
	 * @return the total needed execution time on the server in milliseconds.
	 */
	public function getExecTime():int {
		return execInMS;
	}
	
	/**
	 * Delivers the total time needed execute the query on the server 
	 * (including standard output in the console).
	 * <p>
	 * The execution time is presented in milliseconds.
	 * 
	 * @param execStdOutInMS the total needed execution time on the server
	 *        in milliseconds (with standard output).
	 */
	/*public function setExecTimeStdOut():int {
		return execStdOutInMS;
	}*/

	/**
	 * Delivers the number of distinct results to the
	 * input query.
	 * 
	 * @return the number of distinct results to the input query.
	 */
	public function getNumOfResults():int {
		return numOfResults;
	}

	/**
	 * Delivers the number of distinct produced lineage levels.
	 * 
	 * @return the number of distinct lineage levels.
	 */
	public function getNumOfLineageLevels():int {
		return numOfLineageLevels;
	}

	/**
	 * Delivers the number of distinct soft rules used during
	 * reasoning.
	 * 
	 * @return the number of distinct soft rules used.
	 */
	public function getNumOfDistinctSoftRules():int {
		return numOfDistinctSoftRules;
	}

	/**
	 * Delivers the number of distinct hard rules used during
	 * reasoning.
	 * 
	 * @return the number of distinct hard rules used.
	 */
	public function getNumOfDistinctHardRules():int {
		return numOfDistinctHardRules;
	}

	/**
	 * Delivers the number of distinct grounded soft rules produced 
	 * during reasoning.
	 * 
	 * @return the number of distinct grounded soft rules produced.
	 */
	public function getNumOfGroundedSoftRules():int {
		return numOfGroundedSoftRules;
	}

	/**
	 * Delivers the number of distinct grounded hard rules produced 
	 * during reasoning.
	 * 
	 * @return the number of distinct grounded hard rules produced.
	 */
	public function getNumOfGroundedHardRules():int {
		return numOfGroundedHardRules;
	}

	/**
	 * Delivers the number of distinct facts produced during reasoning.
	 * 
	 * @return the number of distinct facts produced.
	 */
	public function getNumOfDistinctFacts():int {
		return numOfDistinctFacts;
	}

	/**
	 * Delivers the number of distinct relations used during reasoning.
	 * 
	 * @return the number of distinct facts used.
	 */
	public function getNumOfDistinctRelations():int {
		return numOfDistinctRelations;
	}

	/**
	 * Delivers the number of distinct relations produced during reasoning.
	 * 
	 * @return the number of distinct facts produced.
	 */
	public function getNumOfDistinctEntities():int {
		return numOfDistinctEntities;
	}
	
	/** 
     * Reads in the serialized data from the mapped UStatistics Java class.
     * 
     * @param input the serialized input data.
     * */
    override public function readExternal(input:IDataInput):void{
	  super.readExternal(input);
	  sortInMS = input.readInt();
	  groundInMS = input.readInt();
	  invRulInMS = input.readInt();
	  maxSatInMS = input.readInt();
	  execInMS = input.readInt();
	  //execStdOutInMS = input.readInt();
	  numOfResults = input.readInt();
	  numOfLineageLevels = input.readInt();
	  numOfDistinctSoftRules = input.readInt();
	  numOfDistinctHardRules = input.readInt();
	  numOfGroundedSoftRules = input.readInt();
	  numOfGroundedHardRules = input.readInt();
	  numOfDistinctFacts = input.readInt();
	  numOfDistinctRelations = input.readInt();
	  numOfDistinctEntities = input.readInt(); 
    }

    /**
     * Writes out the data to serialize to the mapped UStatistics Java class.
     * 
     * @param output the output data to serialize.
     * */
    override public function writeExternal(output:IDataOutput):void{
      //super.writeExternal(output);
	  /*output.writeInt(sortInMS);
	  output.writeInt(groundInMS);
	  output.writeInt(invRulInMS);
	  output.writeInt(maxSatInMS);
	  output.writeInt(numOfResults);
	  output.writeInt(numOfLineageLevels);
	  output.writeInt(numOfDistinctSoftRules);
	  output.writeInt(numOfDistinctHardRules);
	  output.writeInt(numOfGroundedSoftRules);
	  output.writeInt(numOfGroundedHardRules);
	  output.writeInt(numOfDistinctFacts);
	  output.writeInt(numOfDistinctRelations);
	  output.writeInt(numOfDistinctEntities);
      */
    }	
		
}

}