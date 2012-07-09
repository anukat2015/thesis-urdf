/**
 * 
 */
package urdf.api {
	
import flash.utils.IDataInput;
import flash.utils.IDataOutput;

import urdf.api.UFactSet;	

/**
 * This class represents a grounded hard rule inferred by the reasoner.
 * 
 * @author Timm Meiser
 * @since 19.01.10
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.UGroundedHardRule")]
public class UGroundedHardRule extends UFactSet {

  /** A reference to the hard rule that produced this fact set. */
  private var hardRule:UHardRule= null;

  /** The empty default constructor for the UGroundedHardRule object. */
  public function UGroundedHardRule() {
    super();
  }

  /**
   * Initializes the UGroundedHardRule object by adding all facts of the given fact set and
   *  the given hard rule as the source for this grounded instance.
   * 
   * @param factSet
   *          the set of facts to use for this UGroundedHardRule object.
   * @param hardRule
   *          the hard-rule which represents the source for this UGroundedHardRule object.
   */
  public function initGroundedHardRule(factSet:UFactSet, hardRule:UHardRule):void {
    super.initFactSetFull(factSet);
    //this.addAll(factSet);
    this.hardRule = hardRule;
  }

  /**
   * Delivers the hard rule that belongs to that UGroundedHardRule object.
   * 
   * @return the used hard rule.
   */
  public function getHardRule():UHardRule {
    return hardRule;
  }

  /**
   * Sets the hard rule that belongs to that UGroundedHardRule object.
   * 
   * @param hardRule
   *          the hard rule to set for this UGroundedHardRule object.
   */
  public function setHardRule(hardRule:UHardRule):void {
    this.hardRule = hardRule;
  }
  
  /** 
   * Reads in the serialized data from the mapped UGroundedHardRule Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
	hardRule = (UHardRule)(input.readObject());
  }

  /**
   * Writes out the data to serialize to the mapped UGroundedHardRule Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
	output.writeObject(hardRule);
  }	

}

}