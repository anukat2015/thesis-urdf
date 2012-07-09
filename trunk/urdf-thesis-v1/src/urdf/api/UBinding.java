package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents a binding object which is used to bind an argument of type UArgument to a value of type UArgument.
 * 
 * @author Martin Theobald
 * @since 30.01.10
 * @version 1.0
 */
public class UBinding extends UObject {

  /** The argument of the UBinding object. */
  private UArgument argument;

  /** The binding for that argument. */
  private UArgument binding;

  /** The empty default constructor for the UBinding object. */
  public UBinding() {
    super();
  }

  /**
   * The constructor that initializes the UBinding object.
   * 
   * @param name
   *          the name for the UBinding object.
   * @param arg
   *          the argument of the UBinding object.
   * @param bind
   *          the binding for that argument.
   * @throws Exception
   */
  public UBinding(String name, UArgument arg, UArgument bind) throws Exception {
    super(name);
    this.init(arg, bind);
  }

  /**
   * The constructor that initializes the UBinding object.
   * 
   * @param arg
   *          the argument of the UBinding object.
   * @param bind
   *          the binding for that argument.
   * @throws Exception
   */
  public UBinding(UArgument arg, UArgument bind) throws Exception {
    this.init(arg, bind);
  }

  /**
   * Initializes the UBinding object.
   * 
   * @param arg
   *          the argument of the UBinding object.
   * @param bind
   *          the binding for that argument.
   * @throws Exception
   */
  public void init(UArgument arg, UArgument bind) throws Exception {
    if (!arg.isVariable())
      throw new Exception("First argument must be a variable!");
    this.argument = arg;
    this.binding = bind;
  }

  /**
   * Delivers the argument of the UBinding object.
   * 
   * @return the argument of the UBinding object.
   */
  public UArgument getArgument() {
    return argument;
  }

  /**
   * Delivers the binding for the argument of the UBinding object.
   * 
   * @return the binding for the argument of the UBinding object.
   */
  public UArgument getBinding() {
    return binding;
  }

  /**
   * Delivers a string representation of the UBinding object.
   * 
   * @return the string representation of the binding.
   */
  @Override
  public String toString() {
    return binding.toString();
  }

  /**
   * Reads in the serialized data from the mapped UBinding ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
    argument = (UArgument) input.readObject();
    binding = (UArgument) input.readObject();
  }

  /**
   * Writes out the data to serialize to the mapped UBinding ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeObject(argument);
    output.writeObject(binding);
  }

}
