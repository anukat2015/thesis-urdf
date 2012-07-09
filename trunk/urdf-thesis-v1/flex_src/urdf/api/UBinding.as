package urdf.api {
	
import urdf.api.UObject;	
	
import flash.utils.IDataInput;
import flash.utils.IDataOutput;
//import flash.utils.IExternalizable;	
	
/**
 * This class represents a binding object which is used to bind an argument of type UArgument 
 * to a value of type UArgument.
 * 
 * @author Martin Theobald
 * @since 30.01.10
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.UBinding")]
public class UBinding extends UObject {

  /** The argument of the UBinding object. */
  private var argument:UArgument;
  /** The binding for that argument. */
  private var binding:UArgument;

  /** The empty default constructor for the UBinding object. */
  public function UBinding() {
    super();
  }

  /**
   * Initializes the UBinding object.
   * 
   * @param name
   *          the name for the UBinding object.
   * @param argument
   *          the argument of the UBinding object.
   * @param binding
   *          the binding for that argument.
   * @throws Error
   */
  public function initBindingFull(name:String, argument:UArgument, binding:UArgument):void {
    if (!argument.isVariable())
       throw new Error("First argument must be a variable!");
    this.argument = argument;
    this.binding = binding;
  }

  /**
   * Initializes the UBinding object.
   * 
   * @param argument
   *          the argument of the UBinding object.
   * @param binding
   *          the binding for that argument.
   * @throws Error
   */
  public function initBinding(argument:UArgument, binding:UArgument):void {
    if (!argument.isVariable())
       throw new Error("First argument must be a variable!");
    this.argument = argument;
    this.binding = binding;
  }

  /**
   * Delivers the argument of the UBinding object.
   * 
   * @return the argument of the UBinding object.
   */
  public function getArgument():UArgument {
    return argument;
  }

  /**
   * Delivers the binding for the argument of the UBinding object.
   * 
   * @return the binding for the argument of the UBinding object.
   */
  public function getBinding():UArgument {
    return binding;
  }
  
  /**
   * Delivers a string representation of the UBinding object.
   *
   * @return the string representation of the binding.
   */ 
  override public function toString():String {
    return binding.toString();
  }
  
  /** 
   * Reads in the serialized data from the mapped UBinding Java class.
   * 
   * @param input the serialized input data.
   * */ 
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
	argument = (UArgument)(input.readObject());
	binding = (UArgument)(input.readObject()); 
  }

  /**
   * Writes out the data to serialize to the mapped UBinding Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
	output.writeObject(argument);
	output.writeObject(binding);
  }	
  
}


}