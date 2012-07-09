/**
 * 
 */
package urdf.api {

import flash.utils.IDataInput;
import flash.utils.IDataOutput;

/**
 * This class represents a fact (grounded atom) that was produced during the reasoning process.
 * 
 * @author Timm Meiser
 * @since 25.11.09
 * @version 1.0
 * 
 */
[Bindable]
[RemoteClass(alias="urdf.api.UFact")]
public class UFact extends UTriplet {

  /** The truth values necessary to mark the different facts. */
  public static const FALSE:int = 0;
  public static const TRUE:int = 1;
  public static const UNKNOWN:int = 2;

  /** The base confidence of the fact in the knowledge base. */
  private var baseConfidence:Number = 0;
  
  private var adjustedConfidence:Number = 0;

  /** The derived confidence of the fact (incl. lineage). */
  //private var derivedConfidence:Number = 0;

  /** The truth value of this fact. */
  private var truthValue:int = UNKNOWN;

  /** The maximum lineage level at which this fact was derived, needed as key for lookup in lineage table. */
  //public var max_level:int;
  
  /** The minimum lineage level at which this fact was derived, needed as key for lookups in lineage table. */
  private var _min_level:int = int.MAX_VALUE;

  /** The grounded hard rule that this signed fact depends on. */
  private var groundedHardRule:UGroundedHardRule = null;

  /** The lineage for this fact. */
  //private var lineage:ULineage = new ULineage();
  /** Entry point to the lineage DAG. */
  private var lineage:ULineageOr = null;


  /** The empty default constructor for the UFact object. */
  public function UFact() {
    super();
  }

  /**
   * Initializes the UFact object.
   * <p>
   * The relation and argument references (internal attribute values are checked) are checked to be constants, otherwise exceptions will be thrown.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the first argument for the fact.
   * @param secondArgument
   *          the second argument for the fact.
   * @throws Error
   */
  public function initFact(relation:URelation, firstArgument:UArgument, secondArgument:UArgument):void {
    this.initFactFull(relation, firstArgument, secondArgument, 1.0);
  }

  /**
   * Initializes the UFact object.
   * <p>
   * The relation and argument references (internal attribute values are checked) are checked to be constants, otherwise exceptions will be thrown.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the first argument for the fact.
   * @param secondArgument
   *          the second argument for the fact.
   * @param confidence
   *          the confidence value for this UFact object to be correct.
   * @throws Error
   */
  public function initFactFull(relation:URelation, firstArgument:UArgument, secondArgument:UArgument, baseConfidence:Number):void {
    // check that all parameters conform to the restrictions to be constants for building the fact object.
    /*
     * if (!relation.isRelation()) throw new Exception("Only a URelation reference is allowed for the relation reference, nothing else!"); if
     * (!firstArgument.isEntity()) throw new Exception("Only a UEntity reference is allowed for the first argument reference, nothing else!"); if
     * (!secondArgument.isEntity()) throw new Exception("Only a UEntity reference is allowed for the second argument reference, nothing else!");
     */
    super.initTriplet(relation, firstArgument, secondArgument);
    //this.baseConfidence = baseConfidence;
    this.baseConfidence = -1;
  }

  /**
   * Delivers the base confidence for (belief in) the correctness of UFact object.
   * 
   * @return the degree of belief in this fact.
   */
  /*public function getBaseConfidence():Number {
    return this.baseConfidence;
  }*/
  
  /**
   * Delivers the base confidence for (belief in) the correctness of UFact object.
   * 
   * @param confidence
   *          the degree of belief in this fact.
   */
  /*public function setBaseConfidence(baseConfidence:Number):void {
    this.baseConfidence = baseConfidence;
  }*/

  /**
   * Delivers the derived confidence for (belief in) the correctness of UFact object.
   * 
   * @return the degree of belief in this fact.
   */
  public function getConfidence():Number {
	//return this.baseConfidence;
	//adjustedConfidence = (baseConfidence == -1) ? lineage.getConf() : baseConfidence;
	adjustedConfidence = (lineage == null) ? baseConfidence : lineage.getConf();
	adjustedConfidence = (adjustedConfidence < 0.01 && adjustedConfidence > 0) ? 0.01 : adjustedConfidence;
	return adjustedConfidence;
  }

  /**
   * Delivers the derived confidence for (belief in) the correctness of UFact object.
   * 
   * @param confidence
   *          the degree of belief in this fact.
   */
  public function setConfidence(baseConfidence:Number):void {
    this.baseConfidence = baseConfidence;
  }

  /**
   * Delivers the truth value of the fact.
   * <p>
   * The following truth values are allowed and possible:
   * <p>
   * <i>UNKNOWN</i>, <i>FALSE</i> and <i>TRUE</i>
   * 
   * @return the truth value.
   */
  public function getTruthValue():int {
    return this.truthValue;//this.getLineage().getFact().getTruthValue();//this.truthValue;
  }
  
  private function getTruthString():String {
  	  //var truthString:String;
  	  
  	  switch(getTruthValue()) {//this.truthValue) {
  	  	 case 0: return "false";
  	  	 case 1: return "true";
  	  	 default: return "unknown";
  	  }
  	  //return (this.truthValue == UFact.FALSE) ? "false" : "true";//((this.truthValue == UFact.TRUE) ? "true" : "unknown");
  }

  /**
   * Sets the truth value for the UFact object.
   * <p>
   * The following truth values are allowed and possible:
   * <p>
   * <i>UNKNOWN</i>, <i>FALSE</i> and <i>TRUE</i>
   * 
   * @param truthValue
   *          the truth value for the UFact object.
   * @return true, if the given truth value could be set and is conform with the available truth values, false otherwise.
   */
  public function setTruthValue(truthValue:int):Boolean {
    // check if the truth value is a valid one
    if (truthValue != FALSE && truthValue != TRUE && truthValue != UNKNOWN)
      return false;

    this.truthValue = truthValue;
    return true;
  }
  
  public function get min_level():int {
  	return _min_level;
  }
  
  public function set min_level(min_level:int):void {
  	  _min_level = min_level;
  }

  /**
   * Delivers the grounded hard-rule (UGroundedHardRule object) the fact belongs to.
   * 
   * @return the grounded hard-rule the fact belongs to.
   */
  public function getGroundedHardRule():UGroundedHardRule {
    return this.groundedHardRule;
  }

  /**
   * Sets the grounded hard-rule (UGroundedHardRule object) the fact belongs to.
   * 
   * @param groundedHardRule
   *          grounded hard-rule the fact belongs to.
   */
  public function setGroundedHardRule(groundedHardRule:UGroundedHardRule):void {
    this.groundedHardRule = groundedHardRule;
  }
  
  override public function toString():String {
    return super.getRelationName() + "(" + super.getFirstArgument().getName() + "," + super.getSecondArgument().getName() + ") - " + "[" + getTruthString() //+ ":"
        //+ "|" + NumberFormat.getInstance().format(this.confidence) + "]";
        + "|" + getConfidence().toFixed(2) + "]";
  }
  
  public function toStringSimple():String {
    return super.getRelationName() + "(" + super.getFirstArgument().getName() + "," + super.getSecondArgument().getName() + ")";
  }
  
  public function toStringFact():String {
    return super.getRelationName() + "(" + super.getFirstArgument().getName() + "," + super.getSecondArgument().getName() + ") - " + "[" + getTruthString() //+ ":"
        //+ "|" + NumberFormat.getInstance().format(this.confidence) + "]";
        + "|" + getConfidence().toFixed(2) + "]";//this.derivedConfidence.toFixed(3) + "]";//this.confidence.toFixed(3) + "]";
  }
  
  public function toStringOrFact(confidence:String):String {
    return super.getRelationName() 
        + "(" + super.getFirstArgument().getName() + "," + super.getSecondArgument().getName() + ")<br/>"
        + "<b>truth value: </b>" + getTruthString() + "<br/>"//+ ":"
        //+ "|" + NumberFormat.getInstance().format(this.confidence) + "]";
        + "<b>confidence: </b>" + confidence;//this.derivedConfidence.toFixed(3) + "]";//this.confidence.toFixed(3) + "]";
  }
  
  /**
   * Delivers the lineage for this UFact object.
   * 
   * @return the lineage for this UFact object.
   */
  public function getLineage():ULineageOr {
  	return lineage;
  }

  public function setLineage(lineage:ULineageOr):void {
	this.lineage = lineage;
  }
  
  public function lineageAvailable():Boolean {
  	return (this.lineage == null) ? false : this.lineage.lineageAvailable();
  }
  
  /** 
   * Reads in the serialized data from the mapped UFact Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
    baseConfidence = input.readDouble();
	truthValue = input.readInt();
	groundedHardRule = (UGroundedHardRule)(input.readObject());
	lineage = (ULineageOr) (input.readObject());
  }

  /**
   * Writes out the data to serialize to the mapped UFact Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
    output.writeDouble(baseConfidence);
	output.writeInt(truthValue);
	output.writeObject(groundedHardRule);
	output.writeObject(lineage);
  }	
  
}

  
}