package urdf.api {
	
import util.UStringUtil;

import flash.utils.IDataInput;
import flash.utils.IDataOutput;
import flash.utils.IExternalizable;

/**
 * This class represents the super class for all classes of the urdf_api package.
 * 
 * @author Timm Meiser
 * @since 24.01.10
 * @version 1.0
 */
[RemoteClass(alias="urdf.api.UObject")]
public class UObject implements IExternalizable {

  /** The id of the object. */
  public var id:int= 0;
  
  [Bindable]
  /** The name of the object */
  public var name:String = "";

  /** Empty constructor for object */
  public function UObject() {//trace("UObject constructor was called !!");
  }

  /**
   * Initializes the UObject object.
   * 
   * @param name
   *          the name for the UObject instance.
   * 
   * */
  public function init(name:String):void {
    this.setName(name);
  }

  /**
   * Delivers the name of the UObject instance.
   * 
   * @return the name of the UObject instance.
   */
  public function getName():String {
    return name;
  }

  /**
   * Sets the name for the UObject instance.
   * 
   * @param name
   *          the name for the UObject instance.
   */
  public function setName(name:String):void {
    this.name = name;
    this.id = hashCode();//trace("name = " + name);
  }

  /**
   * Delivers the id of the UObject instance.
   * <p>
   * If the internal id has not been set yet, this method first computes and sets its own id by the internal hash function. Then, it returns this computed id to
   * the caller.
   * 
   * @return the id of the UObject instance.
   */
  public function getId():int {
    return id;
  }

  /**
   * Computes the id (numerical representation) for the UObject instance.
   * <p>
   * No overriding of this function, because it is not a ActionScript related method that 
   * is valid for every ovject. But we derive every urdf_api class from this class here, so 
   * consequently, we can use the hashcode method for every class of our package.
   * <p>
   * A drawback is the absence of a hashcode-method for the String class. So I had to come-up 
   * with my own UStringUtil.hashcode()-method.
   * 
   * @return the computed hash id.
   * */
  //override public int hashCode() {
  public function hashCode():int {
  	//trace("hashcode is " + UStringUtil.hashCode(name));
  	//trace("this is UObject : " + (this is UObject));
  	//trace("this is UArgument : " + (this is UArgument));
    return (name == null) ? 0 : UStringUtil.hashCode(name);//.hashCode(); // name is used to calculate id
  }

  /**
   * Checks whether this UObject instance equals the given instance or not.
   * 
   * @return true, if this UObject instance equals the given instance, false otherwise.
   * */
  public function equals(object:UObject):Boolean {

    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }

    return (this.getId() == object.getId());
  }

  /**
   * Returns a string representation of the UObject object.
   * 
   * @return the string representation of the UObject object.
   */
  public function toString():String {
    return "UObject [id = " + id + ", name = " + name + "]";
  }
  
  /**
   * Reads in the serialized data from the mapped UObject Java class.
   * 
   * @param input the serialized input data.
   * */
  public function readExternal(input:IDataInput):void{
    id = input.readInt();
    name = input.readUTF();
  }

  /**
   * Writes out the data to serialize to the mapped UObject Java class.
   * 
   * @param output the output data to serialize.
   * */	
  public function writeExternal(output:IDataOutput):void{
    output.writeInt(id);
    output.writeUTF(name);
  }	
  
}

}