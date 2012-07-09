/**
 * 
 */
package urdf.api {
	
import util.UStringUtil;
import urdf.api.UObject;
//import urdf.api.UDomain;

import flash.utils.IDataInput;
import flash.utils.IDataOutput;
//import flash.utils.IExternalizable;
	
/**
 * This class represents the super class for UEntity and URelation objects.
 * 
 * @author Timm Meiser 
 * @since 06.01.10
 * @version 1.0 
 */
[Bindable]
[RemoteClass(alias="urdf.api.UArgument")] 
public class UArgument extends UObject { 

  //[Bindable]
  /** The domain reference of that argument. */
  private var domain:UDomain;
  /** The flag that indicates whether this argument is a variable or not. */
  private var variable:Boolean;
  /** The flag that indicates whether this argument is groundable or not. */
  private var groundable:Boolean;

  /** The empty default constructor for the UArgument object. */
  public function UArgument() {
    super();
  }

  /**
   * Initializes the UArgument object.
   * 
   * @param name
   *          the name for the UArgument instance.
   * 
   * */
  override public function init(name:String):void {
    super.init(name);
    this.domain = null;
    this.setName(name);
  }

  /**
   * Initializes the UArgument object.
   * 
   * @param name
   *          the name for the UArgument instance.
   * @param domain
   *          the domain for the UArgument instance.
   * 
   * */
  public function initArgument(name:String, domain:UDomain):void {
    super.init(name);
    this.domain = domain;
    this.setName(name);
  }

  /**
   * Delivers the domain to which the the UArgument instance belongs.
   * 
   * @return the domain of the UArgument instance.
   */
  public function getDomain():UDomain {
    return domain;
  }
  
  /**
   * Sets the domain to which the the UArgument instance belongs.
   * 
   * @param domain the domain of the UArgument object.
   */
  public function setDomain(domain:UDomain):void {
	this.domain = domain;
  }

  /**
   * Sets the name for the UArgument instance.
   * <p>
   * If the name starts with a "?", then the <i> variable </i> flag is set. If the name starts with "??", then the <i> groundable </i> flag is set too.
   * 
   * @param name
   *          the name for the argument.
   */
  override public function setName(name:String):void {
    super.setName(name);
    this.variable = UStringUtil.startsWith(name,"?");//name.startsWith("?");
    this.groundable = !UStringUtil.startsWith(name,"??"); //name.startsWith("??");
  }

  /**
   * Checks whether this UArgument object equals the given UArgument object or not.
   * 
   * @param object the UArgument object to check.
   * @return true, if this UArgument object equals the given UArgument object, false otherwise.
   * */
  override public function equals(object:UObject):Boolean {
    
    if (this == object) {
      return true;
    }
    if (!super.equals(object)) {
      return false;
    }
    if (!(object is UArgument)) {
      return false;
    }
    
    return this.getId() == object.getId();
  }

  /**
   * Checks whether this UArgument instance is a variable.
   * 
   * @return true, if this UArgument instance is a variable, false otherwise.
   * */
  public function isVariable():Boolean {
    return variable;
  }

  /**
   * Checks whether this UArgument instance is groundable.
   * 
   * @return true, if this UArgument instance is groundable, false otherwise.
   * */
  public function isGroundable():Boolean {
    return groundable;
  }

  /**
   * Checks whether this UArgument instance is an entity.
   * 
   * @return true, if this UArgument instance is an entity, false otherwise.
   * */
  public function isEntity():Boolean {
    return false;
  }

  /**
   * Checks whether this UArgument instance is a relation.
   * 
   * @return true, if this UArgument instance is a relation, false otherwise.
   * */
  public function isRelation():Boolean {
    return false;
  }

  /**
   * Returns a string representation of the UArgument object.
   * 
   * @return the string representation of the UArgument object.
   */
  override public function toString():String {
    return getName(); //super.toString() + " [domain = " + domain + ", groundable = " + groundable + ", variable = " + variable + "]";
  }
  
  /** 
   * Reads in the serialized data from the mapped UArgument Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
	domain = (UDomain)(input.readObject()); 
	groundable = input.readBoolean();
	variable = input.readBoolean();
  }

  /**
   * Writes out the data to serialize to the mapped UArgument Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
	output.writeObject(domain);
	output.writeBoolean(groundable);
	output.writeBoolean(variable);
  }	
  
}


}