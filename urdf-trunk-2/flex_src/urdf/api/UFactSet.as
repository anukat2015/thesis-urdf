/**
 * 
 */
package urdf.api {

import flash.utils.IDataInput;
import flash.utils.IDataOutput;

import general.UComparable;
import mx.utils.ObjectUtil;

/**
 * This class represents a set of facts and is used as a super class for grounded hard-rules (UGroundedHardRule instances) and grounded soft-rules
 * (UGroundedSoftRule instances).
 * <p>
 * Internally, there are two main data structures (an integer hash map and a list of facts) used to store facts which belong to this signed fact set and were
 * produced by a particular (also internally stored) soft rule. Further, several methods are provides by this class to access, deliver and delete particular
 * facts (from the internal storage).
 * 
 * 
 * @author Timm Meiser
 * @since 08.12.09
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.UFactSet")]
//public class UFactSet extends UObject implements Iterable<UFact>, Comparable<UFactSet> {
public class UFactSet extends UObject implements UComparable {

  /**
   * Instead of a hashmap, we use a hashset.Here, all the facts are stored.
   */
  //private OpenIntObjectHashMap factIDMap;
  //private var factIDMap:UObjectHashSet;

  /** This ArrayCollection stores all the signed fact objects (references). */
  //private List<UFact> factSet;
  private var factSet:Array;
  //private var factSet:ArrayCollection;

  /** The weight that is used for matching the fact set. */
  private var weight:Number;
  

  /** The empty default constructor for the UFactSet object. */
  public function UFactSet() {
    super();
    //this.factIDMap = new UObjectHashSet();
    //this.factSet = new ArrayCollection();
    this.factSet = new Array();
    this.weight = 0;
  }

  /**
   * Initializes the UFactSet object.
   * 
   * @param fact
   *          the fact that is added to the internal list of facts and that is used to initialize this fact-set (UFactSet object).
   */
  public function initFactSet(fact:UFact):void {
    this.add(fact);
    this.weight = fact.getConfidence();
  }

  /**
   * Initializes the UFactSet object.
   * 
   * @param factSet
   *          the set of facts that is used to initialize this fact-set (UFactSet object).
   */
  public function initFactSetFull(factSet:UFactSet):void {
    this.addAll(factSet);
    this.weight = factSet.getWeight();
  }

  /**
   * Adds the given fact to the internal list of stored facts.
   * 
   * @param fact
   *          the given fact to add to the internal list of facts.
   * */
  public function add(fact:UFact):void {
      factSet.push(fact);
  }
  
  /**
   * Adds the given fact to the internal list of stored facts, only if the 
   * fact is not already in the set.
   * 
   * @param fact
   *          the given fact to add to the internal list of facts.
   * @return true, if the facts had not been stored before and was added, 
   *         false otherwise.
   * */
  public function addChecked(fact:UFact):Boolean {
    if(!this.contains(fact)) {
      factSet.push(fact);
      return true;
    }
    return false;
  }

  /**
   * Adds all the given facts to the internal list of stored facts at once.
   * <p>
   * All facts will be added -> duplicates are possible.
   * 
   * @param factSet
   *          the given set of facts to add to the internal list of facts.
   * */
  public function addAll(factSet:UFactSet):void {
    if (factSet != null)
      for each (var fact:UFact in factSet)
        this.add(fact);
  }
  
  /**
   * Adds all the given facts to the internal list of stored facts at once.
   * <p>
   * Uses the internal method <i> boolean add(UFact fact) </i>. There is no 
   * guarantee that all facts of the fact set will be added.
   * 
   * @param factSet
   *          the given set of facts to add to the internal list of facts.
   * */
  public function addAllChecked(factSet:UFactSet):void {
    if (factSet != null)
      for each (var fact:UFact in factSet)
        this.addChecked(fact);
  }

  /**
   * Removes the given fact from the internal list of stored facts.
   * 
   * @param fact
   *          the fact to remove from the internal list of stored facts.
   * @return true, if the fact could be removed, false otherwise.
   * */
  public function removeFact(fact:UFact):Boolean {
    //if (this.contains(fact)) {
    var index:int = 0;
    if((index = this.indexOf(fact)) >= 0) {
      factSet.splice(index,1); // delete(startIndex,num);
      return true;
    }
    return false;
  }

  /**
   * Checks whether this fact set contains all facts of the given fact-set.
   * 
   * @param factSet
   *          the fact set whose facts should be checked for membership in the internal list of facts.
   * @return true, if the internal list of facts contains all of the facts of the given fact-set, false otherwise.
   * */
  public function containsAll(factSet:UFactSet):Boolean {
    for each (var fact:UFact in factSet) {
    	trace("hashCode is : " + fact.hashCode());
      if (!this.contains(fact))
        return false;
    }
    return true;
  }

  /**
   * Checks whether this fact set contains any of the facts of the given fact set.
   * 
   * @param factSet
   *          the fact-set whose facts should be checked for membership in the internal list of facts.
   * @return true, if the internal list of facts contains any of the facts of the given fact-set, false otherwise.
   * */
  public function containsAny(factSet:UFactSet):Boolean {
    for each (var fact:UFact in factSet)
      if (this.contains(fact))
        return true;
    return false;
  }

  /**
   * Delivers the internally stored fact that maps to the specified one.
   * <p>
   * This method acts as a lookup where it is checked, if a certain fact has already existed before. Then, this object is returned so that the caller of this
   * method can continue grounding the query with the original first instantiated UFact object instead of the equal one that has been created recently.
   * 
   * @param fact
   *          the specified fact whose internally mapped fact (the original first created object that represents the given fact) should be delivered.
   * @return the mapped fact.
   * */
  public function lookup(fact:UFact):UFact {
    //return (UFact) factIDMap.get(fact.getId());
    for each (var tempFact:UFact in factSet) {
    	if(tempFact.equals(fact))
    	   return tempFact;
    }
    return null;
  }

  /**
   * Delivers all the facts (the list of facts) contained in this fact-set.
   * 
   * @return the list of facts.
   */
  //public List<UFact> getFactSet() {
  public function getFactSet():Array {
    return factSet;
  }
  
  /**
   * Delivers the index of the specified fact within the set of facts(array of facts).
   * 
   * @param fact the fact whose index is of interest.
   * @return the index of the fact, if the fact ios present in the fact set, -1 otherwise.
   * */
  public function indexOf(fact:UFact):int {
  	for(var index:int = 0; index<factSet.length; index++) {// var tempFact:UFact in factSet) {
  		if(((UFact)(factSet[index])).equals(fact))
  			return index;
  	}
  	return -1;
  }

  /**
   * Checks whether the specified fact has already been stored internally.
   * 
   * @param fact
   *          the fact to check for existence in the internal fact-set.
   * @return true, if the specified fact (UFact object) already exists internally, false otherwise.
   * */
  public function contains(fact:UFact):Boolean {
    for each(var tempFact:UFact in factSet) {
    	if(tempFact.equals(fact))
    	   return true;
    }
    return false;
  }

  /**
   * Delivers the length of the internal fact list (number of stored facts so far).
   * 
   * @return the number of currently stored facts.
   * */
  public function size():int {
    return factSet.length;
  }

  /**
   * Delivers the UFact object that is stored at the specified position within the 
   * internal list of facts (UFact objects).
   * 
   * @param position the position of the fact to look for.
   * @return the fact found at the specified position or null, if the position was not a valid one.
   * */
  public function getFactAtPosition(position:int):UFact {
    
    if(position < 0 || position >= factSet.length)
       return null;
    
    return (UFact)(factSet[position]);
  }

  /** Clears the internally used data structures. */
  public function clear():void {
    factSet.splice(0,factSet.length);
    weight = 0;
  }

  /**
   * Prints important information about the internally stored data.
   * 
   * @return the string representation of the internally stored data.
   * */
  override public function toString():String {
    return this.weight + ":" + factSet.toString();
  }

  /**
   * Delivers the weight which represents a matching weight for the soft rule that produced this fact-set (UFactSet object).
   * 
   * @return the weight of the internally stored soft-rule.
   */
  public function getWeight():Number {
    return weight;
  }

  /**
   * Sets the weight which represents a matching weight for the soft rule that produced this fact-set (UFactSet object).
   * 
   * @param weight
   *          the weight of the internally stored soft-rule.
   */
  public function setWeight(weight:Number):void {
    this.weight = weight;
  }

  /**
   * Checks if the specified UFactSet object is equal to this UFactSet instance.
   * 
   * @param object
   *          the UFactSet object to check for equality.
   * @return true, if the specified UFactSet object is equal to this UFactSet instance.
   * 
   */
  override public function equals(object:UObject):Boolean {
    
    if (this == object) {
      return true;
    }
    if (!super.equals(object)) {
      return false;
    }
    if (!(object is UFactSet)) {
      return false;
    }
    
    var factSet:UFactSet = (UFactSet)(object);
    //trace("FactSet was checked !!");
    if(this.size() != factSet.size()){
       trace("Size does not match");
       return false;
    }
    if(!this.containsAll(factSet)) {
       trace("fact sets do not match 1");
       return false;
    }
    if(!factSet.containsAll(this)) {
        trace("fact sets do not match 1");
       return false;
    }
    
    return true;
  }

  /**
   * Compares this UFactSet object with a given one.
   * <p>
   * As a special case: The method returns -2, in case the given object is no UFactSet object.
   * 
   * @param factSet
   *          the UFactSet object to compare with this UFactSet instance.
   * @return the value 0 if the weight of this fact set is numerically equal to the weight of the given fact set;
   *         <p>
   *         a value less than 0 if the weight of this fact set instance is numerically less than the one of the given fact set;
   *         <p>
   *         and a value greater than 0 if the opposite is true.
   * */
  public function compareTo(object:Object):int {
  	
  	if (!(object is UFactSet)) {
      return -2;
    }
    
    var factSet:UFactSet = (UFactSet)(object);
    
    return ObjectUtil.numericCompare(this.getWeight(), factSet.getWeight());
  }
  
  /** 
   * Reads in the serialized data from the mapped UFactSet Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
	//factSet = (ArrayCollection)(input.readObject());
	//factSet = (Array)(input.readObject());
	var tempSet:Object = input.readObject();
    //for each (var fact:UFact in tempSet)
    for each (var fact:Object in tempSet)
       factSet.push(fact);
	//trace(this.weight + ":" + factSet.toString());
	weight= input.readDouble();
  }

  /**
   * Writes out the data to serialize to the mapped UFactSet Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
	output.writeObject(factSet);
	output.writeDouble(weight);
  }	
  
}

}