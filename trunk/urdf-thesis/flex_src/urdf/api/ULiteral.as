/**
 * 
 */
package urdf.api {
	
import flash.utils.IDataInput;
import flash.utils.IDataOutput;

import general.UComparable;

import mx.utils.ObjectUtil;

/**
 * This class represents a literal object which is used for several reasoner related data structures including soft rules, hard rules, queries and so on.
 * 
 * @author Timm Meiser
 * @since 18.01.10
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.ULiteral")]
public class ULiteral extends UTriplet implements UComparable {
	
  private var compareValue:int = 1;	

  /** The constructor that initializes the ULiteral object. */
  public function ULiteral() {
    super();
  }

  /**
   * The constructor that initializes the ULiteral object.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the name of the first argument of the ULiteral object.
   * @param secondArgument
   *          the name of the second argument of the ULiteral object.
   * @throws Error
   * */
  /*public function ULiteral(relation:URelation, firstArgument:String, secondArgument:String) {
    super(relation, new UArgument(firstArgument), new UArgument(secondArgument));
  }*/

  /**
   * The constructor that initializes the ULiteral object.
   * <p>
   * The allowed parameters are:
   * <p>
   * For the relation reference: An URelation reference.
   * <p>
   * For the first argument reference: An UEntity reference.
   * <p>
   * For the second argument reference: An UEntity reference.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the first argument of the ULiteral object.
   * @param secondArgument
   *          the second argument of the ULiteral object.
   * @throws Exception
   * */
  /*public function ULiteral(relation:URelation, firstArgument:UArgument, secondArgument:UArgument) throws Exception {
    super(relation, firstArgument, secondArgument);
  }*/

  /**
   * Initializes the ULiteral object.
   * <p>
   * The allowed parameters are:
   * <p>
   * For the relation reference: An URelation reference.
   * <p>
   * For the first argument reference: An UEntity reference.
   * <p>
   * For the second argument reference: An UEntity reference.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the first argument of the ULiteral object.
   * @param secondArgument
   *          the second argument of the ULiteral object.
   * @throws Error
   */
  public function initLiteral(relation:URelation, firstArgument:UArgument, secondArgument:UArgument):void {
    super.initTriplet(relation, firstArgument, secondArgument);
  }

  /**
   * Initializes the ULiteral object.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the name of the first argument of the ULiteral object.
   * @param secondArgument
   *          the name of the second argument of the ULiteral object.
   * @throws Error
   * */
  public function initLiteralFull(relation:URelation, firstArgument:String, secondArgument:String):void {
  	//var entityOne:UEntity = new UEntity();
  	//entityOne.init("",firstArgument);
  	//var entityTwo:UEntity = new UEntity();
  	//entityTwo.init("",secondArgument);
  	var argumentOne:UArgument = new UArgument();
  	argumentOne.init("",firstArgument);
  	var argumentTwo:UArgument = new UArgument();
  	argumentTwo.init("",secondArgument);
  	//super.initTriplet(relation, new UEntity(firstArgument), new UEntity(secondArgument));
    super.initTriplet(relation, argumentOne, argumentTwo);
  }
  
  /**
   * Initializes the ULiteral object.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the name of the first argument of the ULiteral object.
   * @param secondArgument
   *          the name of the second argument of the ULiteral object.
   * @throws Error
   * */
  public function initLiteralFullCompare(relation:URelation, firstArgument:String, secondArgument:String, compareValue:int):void {
  	//var entityOne:UEntity = new UEntity();
  	//entityOne.init("",firstArgument);
  	//var entityTwo:UEntity = new UEntity();
  	//entityTwo.init("",secondArgument);
  	var argumentOne:UArgument = new UArgument();
  	argumentOne.init("",firstArgument);
  	var argumentTwo:UArgument = new UArgument();
  	argumentTwo.init("",secondArgument);
  	//super.initTriplet(relation, new UEntity(firstArgument), new UEntity(secondArgument));
    super.initTriplet(relation, argumentOne, argumentTwo);
    
    this.compareValue = compareValue;
  }

  /**
   * Compares this ULiteral instance to a given one.
   * 
   * @param object
   *          the literal this ULiteral instance should be compared to.
   * @return 0 for equality, -1 for smaller, 1 for bigger (usually)
   */
  public function compareTo(object:Object):int {
  	
  	if (!(object is ULiteral)) {
      return -2;
    }
    
    // return Double.compare(this.getSelectivity(), literal.getSelectivity());
    return ObjectUtil.numericCompare(this.getSelectivity(), object.getSelectivity());
  }
  
  public function getCompareValue():int {
    return compareValue;
  }
  
  public function setCompareValue(compareValue:int):void {
    this.compareValue = compareValue;
  }
  
  /**
   * Reads in the serialized data from the mapped ULiteral Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
	this.compareValue = input.readInt();
  }

  /**
   * Writes out the data to serialize to the mapped ULiteral Java class.
   * 
   * @param output the output data to serialize.
   * */	
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
    output.writeInt(this.compareValue);
  }	
  
}


}