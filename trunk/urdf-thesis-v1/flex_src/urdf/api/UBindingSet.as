package urdf.api {
	
import flash.utils.IDataInput;
import flash.utils.IDataOutput;
//import flash.utils.IExternalizable;

import util.UObjectHashSet;
import util.UStringHashMap;

/**
 * This class represents a binding set which is used store all the produced distinct bindings.
 * 
 * @author Martin Theobald
 * @since 30.01.10
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.UBindingSet")]
public class UBindingSet extends UObject {//implements Iterable<UBinding> {

  /** The map that stores all the bindings. */
  //private Map<String, UBinding> bindings;
  private var bindings:UStringHashMap;

  /** The empty default constructor for the UBindingSet object. */
  public function UBindingSet() {
    super();
    //super.init("","");
    //this.bindings = new HashMap<String, UBinding>();
	this.bindings = new UStringHashMap();
  }

  /**
   * Initializes the UBindingSet object.
   * 
   * @param bindings
   *          a UBindingSet object to initialize this UBindingSet instance with.
   * */
  public function initBindingSet(bindings:UBindingSet):void {
    //this.init(bindings.size());
    this.addAll(bindings);
  }

  /**
   * Adds a given binding to the internal map of bindings.
   * 
   * @param binding
   *          the binding to add.
   * */
  public function add(binding:UBinding):void {
    this.bindings.put(binding.getArgument().getName(), binding);
  }

  /**
   * Adds all the given bindings of the UBindingSet object to the internal map of bindings.
   * 
   * @param bindings
   *          the bindings (of the given UBindingSet) to add.
   * */
  public function addAll(bindings:UBindingSet):void {
    for each (var binding:UBinding in bindings.getBindings())
      this.add(binding);
  }

  /**
   * Delivers the binding to the specified argument, if one exists.
   * 
   * @param argument
   *          the argument whose binding is of interest.
   * @return the binding object or null, if no binding for the given argument exists.
   * */
  public function getBinding(argument:UArgument):UArgument {
    var binding:UBinding = (UBinding)(bindings.getValue(argument.getName()));
    if (binding != null)
      return binding.getBinding();
    return null;
  }
  
  /**
   * Delivers the set (Object instance) of internally stored bindings.
   *  
   * @return the set of internally stored bindings.
   * */
  public function getBindings():Object {
  	return this.bindings.getMap();
  }

  // may be expensive
  /**
   * Delivers all the arguments that exist for the specified binding.
   * 
   * @param binding
   *          the binding whose bounded arguments are of interest.
   * @return the set of bounded arguments (possibly length 0, if no bounded argument exists so far).
   * */
  //public Set<UArgument> getArguments(var binding:UArgument) {
  public function getArguments(binding:UArgument):UObjectHashSet {
    //Set<UArgument> arguments = new HashSet<UArgument>();
	var arguments:UObjectHashSet = new UObjectHashSet();
    //for (UBinding b : bindings.values()) {
	for each (var b:UBinding in bindings.getMap()) {
      if (b.getBinding().equals(binding))
        arguments.add(b.getArgument());
    }
    return arguments;
  }

  /**
   * Delivers the size of the internal map of bindings (number of bindings so far).
   * 
   * @return the number of bindings so far.
   * */
  public function size():int {
    return bindings.size();
  }
  
  /**
   * Delivers a string representation of the UBindingSet object.
   *
   * @return the string representation of the binding-set.
   */
  override public function toString():String {
    return bindings.toString();
  }
  
  /** 
   * Reads in the serialized data from the mapped UBindingSet Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input); 
	//bindings = new UStringHashMap();
	bindings.setMap(input.readObject());
	//var obj:Object = input.readObject();
	//trace("values = " + obj.toString());
	//bindings = (UStringHashMap)(obj); 
  }

  /**
   * Writes out the data to serialize to the mapped UBindingSet Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
	//output.writeObject(bindings);
	output.writeObject(bindings.getMap());
  }	
  
}

}