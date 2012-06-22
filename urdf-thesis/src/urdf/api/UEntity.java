/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents an entity object which is used as a constant within the reasoning process.
 * 
 * @author Timm Meiser
 * @since 01.02.10
 * @version 1.0
 */
public class UEntity extends UArgument {

  /**
   * the constructor of the entity class.
   * <p>
   * We need an empty constructor for BlazeDS to create an instance of the class from the Flex client side.
   * 
   * */
  public UEntity() {
    super();
  }

  /**
   * The constructor that initializes the UEntity object.
   * 
   * @param name
   *          the name for the UArgument object.
   * */
  public UEntity(String name) {
    super(name);
  }

  /**
   * The constructor that initializes the UEntity object.
   * 
   * @param name
   *          the name for the UEntity object.
   * @param domain
   *          the domain for the UEntity object.
   * */
  public UEntity(String name, UDomain domain) {
    super(name, domain);
  }

  /**
   * Initializes the entity object by setting the necessary attribute values.
   * 
   * @param name
   *          the name of the entity.
   * */
  public void init(String name) {
    super.init(name);
  }

  /**
   * Checks whether this object is an entity.
   * 
   * @return true, if this object is an entity, false otherwise.
   * */
  @Override
  public boolean isEntity() {
    return true;
  }

  /**
   * Checks whether this entity object equals the given object or not.
   * 
   * @return true, if this entity object equals the given object, false otherwise.
   * */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof UEntity)) {
      return false;
    }

    return this.hashCode() == obj.hashCode();
  }

  /**
   * Reads in the serialized data from the mapped UEntity ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
  }

  /**
   * Writes out the data to serialize to the mapped UEntity ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
  }

}
