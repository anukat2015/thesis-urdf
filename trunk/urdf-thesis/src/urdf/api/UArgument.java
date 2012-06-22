/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents the super class for UEntity and URelation objects.
 * 
 * @author Timm Meiser
 * @since 06.01.10
 * @version 1.0
 */
public class UArgument extends UObject implements Comparable<UArgument> {

  /** The domain reference of that argument. */
  private UDomain domain;

  /** The flag that indicates whether this argument is a variable or not. */
  private boolean variable;

  /** The flag that indicates whether this argument is groundable or not. */
  private boolean groundable;

  /** The empty default constructor for the UArgument object. */
  public UArgument() {
    super();
  }

  /**
   * The constructor that initializes the UArgument object.
   * 
   * @param name
   *          the name for the UArgument object.
   * */
  public UArgument(String name) {
    super();
    init(name);
  }

  /**
   * The constructor that initializes the UArgument object.
   * 
   * @param name
   *          the name for the UArgument object.
   * @param domain
   *          the domain for the UArgument object.
   * */
  public UArgument(String name, UDomain domain) {
    super();
    init(name, domain);
  }

  /**
   * Initializes the UArgument object.
   * 
   * @param name
   *          the name for the UArgument instance.
   * */
  public void init(String name) {
    this.init(name, null);
  }

  /**
   * Initializes the UArgument object.
   * 
   * @param name
   *          the name for the UArgument instance.
   * @param dom
   *          the domain for the UArgument instance.
   * 
   * */
  public void init(String name, UDomain dom) {
    this.domain = dom;
    this.setName(name);
  }

  /**
   * Delivers the domain to which the the UArgument instance belongs.
   * 
   * @return the domain of the UArgument instance.
   */
  public UDomain getDomain() {
    return domain;
  }

  /**
   * Sets the domain to which the the UArgument instance belongs.
   * 
   * @param domain
   *          the domain of the UArgument object.
   */
  public void setDomain(UDomain domain) {
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
  @Override
  public void setName(String name) {
    super.setName(name);
    this.variable = name.startsWith("?");
    this.groundable = !name.startsWith("??");
  }

  /**
   * Checks whether this UArgument instance equals another UArgument instance.
   * 
   * @return true, if this UArgument instance equals the given object, false otherwise.
   * */
  public boolean equals(UArgument arg) {
    return this.hashCode() == arg.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return this.hashCode() == obj.hashCode();
  }

  /**
   * Compares this UArgument instance to another one.
   * 
   * @param argument
   *          the UArgument object to compare with this instance.
   * @return the value 0 if this UArgument instance has the same name as the given one;
   *         <p>
   *         a value less than 0 if the name of this UArgument instance is lexically smaller as the one of the given UArgument instance to compare;
   *         <p>
   *         and a value greater than 0 if the name of this UArgument instance is lexically bigger as the one of the given UArgument instance to compare.
   * */
  @Override
  public int compareTo(UArgument argument) {
    return this.getName().compareTo(argument.getName());
  }

  /**
   * Checks whether this UArgument instance is a variable.
   * 
   * @return true, if this UArgument instance is a variable, false otherwise.
   * */
  public boolean isVariable() {
    return variable;
  }

  /**
   * Checks whether this UArgument instance is groundable.
   * 
   * @return true, if this UArgument instance is groundable, false otherwise.
   * */
  public boolean isGroundable() {
    return groundable;
  }

  /**
   * Checks whether this UArgument instance is an entity.
   * 
   * @return true, if this UArgument instance is an entity, false otherwise.
   * */
  public boolean isEntity() {
    return false;
  }

  /**
   * Checks whether this UArgument instance is a relation.
   * 
   * @return true, if this UArgument instance is a relation, false otherwise.
   * */
  public boolean isRelation() {
    return false;
  }

  /**
   * Returns a string representation of the UArgument object.
   * 
   * @return the string representation of the UArgument object.
   */
  @Override
  public String toString() {
    return getName();
  }

  /**
   * Reads in the serialized data from the mapped UArgument ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
    domain = (UDomain) input.readObject();
    groundable = input.readBoolean();
    variable = input.readBoolean();
  }

  /**
   * Writes out the data to serialize to the mapped UArgument ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeObject(domain);
    output.writeBoolean(groundable);
    output.writeBoolean(variable);
  }
}
