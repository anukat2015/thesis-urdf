/**
 * 
 */
package urdf.api {
	
import flash.utils.IDataInput;
import flash.utils.IDataOutput;

import util.UStringUtil;

/**
 * This class represents a triplet object which is used for several reasoner related data structures including soft rules, hard rules, queries and so on. This
 * class has the subclasses ULiteral and UFact.
 * 
 * @author Timm Meiser
 * @since 07.01.10
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.UTriplet")] 
public class UTriplet extends UObject{

  /**
   * The first argument of the UTriplet object. Here, only a UVariable reference or a UEntity reference are allowed. Is checked at runtime.
   */
  private var firstArgument:UArgument;

  /**
   * The second argument for the UTriplet object. Here, only a UVariable reference or a UEntity reference are allowed. Is checked at runtime.
   */
  private var secondArgument:UArgument;

  /**
   * The relation reference for the UTriplet object. Here, only a UVariable reference or a URelation reference are allowed. Is checked at runtime.
   */
  private var relation:URelation;

  /** Selectivity estimate for this UTriplet object. */
  private var selectivity:Number = Number.MAX_VALUE;

  /** The empty default constructor for the UTriplet object. */
  public function UTriplet() {
    super();
  }

  /**
   * The constructor that initializes the UTriplet object.
   * <p>
   * Internally, it is checked whether the given attribute values represent free variables to bind by the program or already given constants to be used during
   * reasoning.
   * <p>
   * The allowed parameters are:
   * <p>
   * For the relation reference: URelation reference.
   * <p>
   * For the first argument reference: UEntity reference.
   * <p>
   * For the second argument reference: UEntity reference.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the first argument of the triplet.
   * @param secondArgument
   *          the second argument of the triplet.
   * @throws Exception
   */
  /*public function UTriplet(relation:URelation, firstArgument:UArgument, secondArgument:UArgument) throws Exception {
    super();
    this.init(relation, firstArgument, secondArgument);
  }*/

  /**
   * Sets the needed attribute values for the UTriplet object.
   * <p>
   * Internally, it is checked whether the given attribute values represent free variables to bind by the program or already given constants to be used during
   * reasoning.
   * <p>
   * The allowed parameters are:
   * <p>
   * For the relation reference: URelation reference.
   * <p>
   * For the first argument reference: UEntity reference.
   * <p>
   * For the second argument reference: UEntity reference.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the first argument for the UTriplet object.
   * @param secondArgument
   *          the second argument for the UTriplet object.
   * @throws Error
   */
  public function initTriplet(relation:URelation, firstArgument:UArgument, secondArgument:UArgument):void {
    this.relation = relation;
    this.firstArgument = firstArgument;
    this.secondArgument = secondArgument;
    super.init("",toString());
  }

  /**
   * Delivers the requested relation name of the internally used relation.
   * 
   * @return the relation name.
   */
  public function getRelationName():String {
    return relation.getName();
  }

  /**
   * Delivers the relation reference.
   * 
   * @return the reference to the internally used relation.
   * */
  public function getRelation():URelation {
    return this.relation;
  }

  /**
   * Sets the relation reference.
   * 
   * @param relation
   *          the relation reference to set (overwrite).
   */
  public function setRelation(relation:URelation):void {
    this.relation = relation;
  }

  /**
   * Delivers the first argument of the UTriplet object.
   * 
   * @return the first argument of the UTriplet object.
   */
  public function getFirstArgument():UArgument {
    return firstArgument;
  }
  
  /**
   * Sets (overwrites) the first argument of the UTriplet object.
   * 
   * @param firstArgument
   *          the first argument to set(overwrite).
   * @throws Error
   */
  public function setFirstArgument(firstArgument:UArgument):void {
    if (firstArgument.isRelation())
      throw new Error("A URelation reference is not allowed for the first argument reference of the triplet!");
    this.firstArgument = firstArgument;
  }

  /**
   * Delivers the name of first argument of the UTriplet object.
   * 
   * @return the name of the first argument of the UTriplet object.
   */
  public function getFirstArgumentName():String {
    return firstArgument.getName();
  }

  /**
   * Delivers the second argument of the UTriplet object.
   * 
   * @return the second argument of the UTriplet object.
   */
  public function getSecondArgument():UArgument {
    return secondArgument;
  }

  /**
   * Sets (overwrites) the second argument of the UTriplet object.
   * 
   * @param secondArgument
   *          the second argument to set(overwrite).
   * @throws Error
   */
  public function setSecondArgument(secondArgument:UArgument):void {
    if (secondArgument.isRelation())
      throw new Error("A URelation reference is not allowed for the second argument reference of the triplet!");
    this.secondArgument = secondArgument;
  }
  
  /**
   * Delivers the name of second argument of the UTriplet object.
   * 
   * @return the name of the second argument of the UTriplet object.
   */
  public function getSecondArgumentName():String {
    return secondArgument.getName();
  }

  /**
   * Delivers the estimated selectivity for this UTriplet object.
   * 
   * @return the estimated selectivity.
   */
  public function getSelectivity():Number {
    return this.selectivity;
  }

  /**
   * Sets the estimated selectivity for this UTriplet object.
   * 
   * @param selectivity
   *          the estimated selectivity for this UTriplet object.
   */
  public function setSelectivity(selectivity:Number):void {
    this.selectivity = selectivity;
  }

  /**
   * Returns a string representation of the UTriplet object.
   * 
   * @return the string representation of the UTriplet object.
   */
  override public function toString():String {
    return (("" + this.relation.getName() + "(" + this.firstArgument.getName() + ", " + this.secondArgument.getName() + ")"));
  }

  /**
   * Produces a hash value for the UTriplet object which should be unique.
   * <p>
   * This method is the default hash function for strings.
   * 
   * @return the computed hash value.
   * */
  //override public function hashCode():int {UStringUtil
  override public function hashCode():int {
    //return (this.firstArgument + "$" + this.secondArgument + "$" + this.relation.getName()).replace("\\W", "x").hashCode();
    return UStringUtil.hashCode((this.firstArgument + "$" + this.secondArgument + "$" + this.relation.getName()).replace("\\W", "x"));
  }

  /**
   * Compares this UTriplet object with a given one.
   * <p>
   * Internally, only the produced id`s are compared to check whether the two UTriplet object are the equal.
   * 
   * @param object
   *          the UTriplet object to check for equality with the given UObject(UTriplet) object.
   * @return true, if both UTriplet instances are references to the same object, false otherwise.
   * */
  override public function equals(object:UObject):Boolean {
    
    if (this == object) {
      return true;
    }
    if (!super.equals(object)) {
      return false;
    }
    if (!(object is UObject)) {
      return false;
    }
    
    var triplet:UTriplet = (UTriplet)(object);
    //trace("triplet was checked !!");
   // return this.getId() == object.getId();
    return this.hashCode() == object.hashCode();
  }
  
  /** 
   * Reads in the serialized data from the mapped UTriplet Java class.
   * <p>
   * We do not deserialize the URelation object. This would be too costly. 
   * Instead, we deserialize the serialized name of the relation. This is 
   * enough information to reconstruct the relation. If the name starts 
   * with "?", then we can be sure to have a variable to ground --> so we 
   * construct a new URelation object with the given name (variable to bind). 
   * Otherwise, we bind the UTriplet property "relation" to the static URelation 
   * object that is returned from the list of static URelation objects (by using 
   * the deserialized relation name.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void {
	super.readExternal(input);
	firstArgument = (UArgument)(input.readObject()); 
	secondArgument = (UArgument)(input.readObject());  
	selectivity = input.readDouble();
	//relation = (URelation)(input.readObject());
	var rel:String = input.readUTF();
	
	if(UStringUtil.startsWith(rel,"?")) {
	   relation = new URelation();
	   relation.init("",rel);
	}
	else 
	   relation = URelation.valueOfRelation(rel); // return the suitable static URelation object
  }

  /** 
   * Writes out the data to serialize to the mapped UTriplet Java class.
   * <p>
   * We do not serialize the URelation object. This would be too costly. 
   * Instead, we serialize the name of the relation. This is enough 
   * information to reconstruct the relation on the server side.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void {
    super.writeExternal(output);
	output.writeObject(firstArgument);
	output.writeObject(secondArgument);
	output.writeDouble(selectivity);
	//output.writeObject(relation);
	output.writeUTF(relation.getName()); // only write the name of the URelation object
  }	
  
}

}