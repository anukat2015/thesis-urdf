/**
 * 
 */
package urdf.api {
	
import urdf.api.UArgument;
import urdf.api.UObject;
import flash.utils.IDataInput;
import flash.utils.IDataOutput;
//import flash.utils.IExternalizable;
	
/**
 * This class represents an entity object which is used as a constant within the reasoning process.
 * 
 * @author Timm Meiser
 * @since 01.02.10
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.UEntity")] 
public class UEntity extends UArgument {

  /**
   * the constructor of the entity class.
   * <p>
   * We need an empty constructor for BlazeDS to create an instance of the class from the Flex client side.
   * 
   * */
  public function UEntity() {
    super();
  }

  /**
   * Initializes the entity object by setting the necessary attribute values.
   * 
   * @param name
   *          the name of the entity.
   * @param domain
   *          the domain of the entity.
   * */
  public function initEntity(name:String, domain:UDomain):void {
    super.initArgument(name, domain);
  }

  /**
   * Checks whether this object is an entity.
   * 
   * @return true, if this object is an entity, false otherwise.
   * */
  override public function isEntity():Boolean {
    return true;
  }
  
  /**
   * Reads in the serialized data from the mapped UEntity Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
  }

  /**
   * Writes out the data to serialize to the mapped UEntity Java class.
   * 
   * @param output the output data to serialize.
   * */	
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
  }	
  
}

}