package urdf.api {
	
import flash.utils.IDataInput;
import flash.utils.IDataOutput;


/**
 * This class manages the lineage information for a single fact.
 * 
 * @author Timm Meiser and Martin Theobald
 * @since 07.02.2010
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.ULineage")]
//public class ULineage extends UObject implements Iterable<UGroundedSoftRule> {
public class ULineage extends UObject {

  /** The map that manages the access to the different lineage levels. */
  //private Map<Integer, Set<UGroundedSoftRule>> lineageMap;
  //private var lineageMap:UStringHashMap = new UStringHashMap();
  private var lineageMap:Object = new Object();

  /** The empty default constructor for the ULineage object. */
  public function ULineage() {
    super();
    //this.lineageMap = new HashMap<Integer, Set<UGroundedSoftRule>>();
  }


  /**
   * Delivers all grounded soft-rules that were produced at the specified level. 
   * 
   * @param level
   *          the lineage level from which to get the grounded soft-rules.
   * @return the set of grounded soft-rules that were derived at the specified lineage level.
   * */
  //public Set<UGroundedSoftRule> getRulesAtLevel(var level:int) {
  public function getRulesAtLevel(level:int):Array {
    return lineageMap[level.toString()];
  }

  /**
   * Delivers all lineage facts depending on the given fact (UFact object).
   * <p>
   * The returned fact-set is the result fact-set that contains all found 
   * lineage-facts for the fact instance.
   * <p>
   * The algorithm starts at the maximum grounding level for the given fact.
   * 
   * @return the found lineage-facts (collected in a set) for the given fact.
   * */
  public function getAllLineageFacts(fact:UFact, onlyBase:Boolean):UFactSet {
    var lineageFacts:UFactSet = null;
    this.getLineageFactsFromLevel(lineageFacts, fact, fact.max_level, onlyBase); 
    return lineageFacts;
  }

  /**
   * Delivers all facts that are part of the lineage of the given fact.
   * <p>
   * This method recursively calls itself with every found fact within the 
   * lineage belonging to the given fact.
   * <p>
   * The given-fact set is the result fact-set that contains all found dependent
   *  lineage-facts for the given fact.
   * 
   * @param lineageFacts
   *          the result fact-set, containing all lineage-facts for the given fact.
   * @param level
   *          the lineage level to process in this recursion step.
   * */
  private function getLineageFactsFromLevel(lineageFacts:UFactSet, derivedFact:UFact, level:int, onlyBase:Boolean):void { // this is for stratified Datalog only

    // check if there is any lineage for this fact and the specified level at all 
    // --> fact was derived at this level and not in the database directly
    //Set<UGroundedSoftRule> softRules;
    var softRules:Array;
    var isDerived:Boolean = false;
    
    if ((softRules = (Array)(lineageMap[level.toString()])) != null) {

      // iterate over all fact sets that belong to this lineage level and the given fact
      for each (var groundedSoftRule:UGroundedSoftRule in softRules) {

        // check whether the head of the rule matches the derived fact, i.e., whether this rule produced the derived fact

        if (groundedSoftRule.getHead() != null && groundedSoftRule.getHead().hashCode() == derivedFact.hashCode()) {
          isDerived = true;

          // iterate over all facts in the body of the grounded rule
          for each (var fact:UFact in groundedSoftRule.getFactSet()) {

            // no ? --> get its lineage information in the next level (increase the lineage level to look at)
            fact.getLineage().getLineageFactsFromLevel(lineageFacts, fact, level + 1, onlyBase);

            // the grounded hard rule connected to the current fact is not null, 
            // so there is a grounded hard rule connected to the current fact ?
            if (fact.getGroundedHardRule() != null) {
              // get every fact belonging to this grounded hard rule
              for each (var f:UFact in fact.getGroundedHardRule().getFactSet())
                // call the function itself again with the current fact and the next lineage level
                f.getLineage().getLineageFactsFromLevel(lineageFacts, f, level + 1, onlyBase);
            }
          }
        }
      }
    }

    // add the found fact to the fact set of lineage facts
    if ((onlyBase && !isDerived) || !onlyBase)
      lineageFacts.add(derivedFact);
  }
  
  /** 
   * Reads in the serialized data from the mapped ULineage Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
	
	//lineageMap = new UStringHashMap();
	// ------------------------------
	// could be more efficient to call the constructor once, here, 
	// instead of multiple times within the loop
	//var key:String = new String();
	//var value:UObjectHashSet = UObjectHashSet();
	// ------------------------------
	var size:int = input.readInt();
	//trace("size of the lineage map = " + size);
	while(size > 0) {
		var key:String = input.readUTF(); // read the current key
		//trace("key is = " + key);
		
		// get the corresponding set of grounded soft rules
	    //Set<UGroundedSoftRule> grSR = lineageMap.get(key);
	    var grSR:Array = new Array();
	    // get the number of grounded soft rules to receive
	    //int numOfGrSR = grSR.size();
	    var numOfGrSR:int = input.readInt();
	    if(numOfGrSR > 0)
	      grSR = new Array();
	    else {
	      size--;	
	      continue;
	    }
	    
	    // receive the grounded soft rules
	    while(numOfGrSR > 0) {
	    	grSR.push((UGroundedSoftRule)(input.readObject()));
	    	//grSR.push(input.readObject());
	    	numOfGrSR--;
	    } 
	    
	    //trace("number of grounded soft rules = " + grSR.length);
	    //trace("grounded soft rules are:");
	    //for each (var rule:UGroundedSoftRule in grSR) {
	    	//trace(rule.toString() + "\n");
	    //}
		
		
		//value.fillHashSetFO(input.readObject()); // read the current value
		//value.setEntries(input.readObject()); // read the current value
		//value.fillHashSetNative((Array)(input.readObject())); // read the current value
		lineageMap[key] = grSR; // put those values as a mapping into the hashmap
		size--;
	}

  }

  /**
   * Writes out the data to serialize to the mapped ULineage Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
	
	/*var size:int = lineageMap.size();
	output.writeInt(size); // at first send the size of the map
	  
	for(var key:String in lineageMap.getMap()) {
		//output.writeUTF(key.toString()); // write the current key
		output.writeUTF(key); // write the current key
		output.writeObject(lineageMap.getValue(key)); // write the current value
	}*/
	
  }	
  
}

}