package urdf.api;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents the super class for all classes of the urdf_api package.
 * 
 * @author Timm Meiser
 * @since 24.01.10
 * @version 1.0
 */
public class UObject implements Externalizable {

  /** The name of the UObject object. */
  private String name = "";

  /** The hash code of the UObject object. */
  protected int hash = 0;

  /** Empty constructor for the UObject object. */
  public UObject() {
  }

  /**
   * The constructor that initializes the UObject object.
   * 
   * @param name
   *          the name for the UObject object.
   * */
  public UObject(String name) {
    this.init(name);
  }

  /**
   * Initializes the UObject object.
   * 
   * @param n
   *          the name for the UObject instance.
   * */
  public void init(String n) {
    this.setName(n);
  }

  /**
   * Delivers the name of the UObject instance.
   * 
   * @return the name of the UObject instance.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name for the UObject instance.
   * 
   * @param name
   *          the name for the UObject instance.
   */
  public void setName(String name) {
    this.name = name;
    this.hash = name.hashCode();
    // System.out.println("SET: " + name + " > " + hash);
  }

  /**
   * Computes the id (numerical representation) for the UObject instance.
   * 
   * @return the computed hash id.
   * */
  @Override
  public int hashCode() {
    return hash;
  }

  /**
   * Checks whether this UObject instance equals the given instance or not.
   * 
   * @return true, if this UObject instance equals the given instance, false otherwise.
   * */
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null)
      return false;
    return (this.hashCode() == object.hashCode());
  }

  /**
   * Returns a string representation of the UObject object.
   * 
   * @return the string representation of the UObject object.
   */
  @Override
  public String toString() {
    return "UObject [" + name + "]";
  }

  /**
   * Reads in the serialized data from the mapped UObject ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    name = input.readUTF();
  }

  /**
   * Writes out the data to serialize to the mapped UObject ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  public void writeExternal(ObjectOutput output) throws IOException {
    output.writeUTF(name);
  }

}
