/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents a triplet object which is used for several reasoner related data structures including soft rules, hard rules, queries and so on. This
 * class has the subclasses ULiteral and UFact.
 * 
 * @author Timm Meiser
 * @since 07.01.10
 * @version 1.0
 */
public class UTriplet extends UObject {

  /**
   * The first argument of the UTriplet object. Here, only a UVariable reference or a UEntity reference are allowed. Is checked at runtime.
   */
  private UArgument firstArgument;

  /**
   * The second argument for the UTriplet object. Here, only a UVariable reference or a UEntity reference are allowed. Is checked at runtime.
   */
  private UArgument secondArgument;

  /**
   * The relation reference for the UTriplet object. Here, only a UVariable reference or a URelation reference are allowed. Is checked at runtime.
   */
  private URelation relation;

  /** Selectivity estimate for this UTriplet object. */
  private double selectivity = Double.MAX_VALUE;

  /** The empty default constructor for the UTriplet object. */
  public UTriplet() {
    super();
  }

  /** The default constructor for the UTriplet object. */
  public UTriplet(String name) {
    super(name);
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
  public UTriplet(URelation relation, UArgument firstArgument, UArgument secondArgument) throws Exception {
    super();
    this.init(relation, firstArgument, secondArgument);
  }

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
   * @param rel
   *          the used relation.
   * @param arg1
   *          the first argument for the UTriplet object.
   * @param arg2
   *          the second argument for the UTriplet object.
   * @throws Exception
   */
  public void init(URelation rel, UArgument arg1, UArgument arg2) throws Exception {
    this.relation = rel;
    this.firstArgument = arg1;
    this.secondArgument = arg2;
    super.init(toString());
  }

  /**
   * Delivers the requested relation name of the internally used relation.
   * 
   * @return the relation name.
   */
  public String getRelationName() {
    return relation.getName();
  }

  /**
   * Delivers the relation reference.
   * 
   * @return the reference to the internally used relation.
   * */
  public URelation getRelation() {
    return this.relation;
  }

  /**
   * Sets the relation reference.
   * 
   * @param relation
   *          the relation reference to set (overwrite).
   */
  public void setRelation(URelation relation) {
    this.relation = relation;
    super.init(toString());
  }

  /**
   * Delivers the first argument of the UTriplet object.
   * 
   * @return the first argument of the UTriplet object.
   */
  public UArgument getFirstArgument() {
    return firstArgument;
  }

  /**
   * Sets (overwrites) the first argument of the UTriplet object.
   * 
   * @param firstArgument
   *          the first argument to set(overwrite).
   * @throws Exception
   */
  public void setFirstArgument(UArgument firstArgument) throws Exception {
    if (firstArgument.isRelation())
      throw new Exception("A URelation reference is not allowed for the first argument reference of the triplet!");
    this.firstArgument = firstArgument;
    super.init(toString());
  }

  /**
   * Delivers the name of first argument of the UTriplet object.
   * 
   * @return the name of the first argument of the UTriplet object.
   */
  public String getFirstArgumentName() {
    return firstArgument.getName();
  }

  /**
   * Delivers the second argument of the UTriplet object.
   * 
   * @return the second argument of the UTriplet object.
   */
  public UArgument getSecondArgument() {
    return secondArgument;
  }

  /**
   * Sets (overwrites) the second argument of the UTriplet object.
   * 
   * @param secondArgument
   *          the second argument to set(overwrite).
   * @throws Exception
   */
  public void setSecondArgument(UArgument secondArgument) throws Exception {
    if (secondArgument.isRelation())
      throw new Exception("A URelation reference is not allowed for the second argument reference of the triplet!");
    this.secondArgument = secondArgument;
    super.init(toString());
  }

  /**
   * Delivers the name of second argument of the UTriplet object.
   * 
   * @return the name of the second argument of the UTriplet object.
   */
  public String getSecondArgumentName() {
    return secondArgument.getName();
  }

  /**
   * Delivers the estimated selectivity for this UTriplet object.
   * 
   * @return the estimated selectivity.
   */
  public double getSelectivity() {
    return this.selectivity;
  }

  /**
   * Sets the estimated selectivity for this UTriplet object.
   * 
   * @param selectivity
   *          the estimated selectivity for this UTriplet object.
   */
  public void setSelectivity(double selectivity) {
    this.selectivity = selectivity;
  }

  /**
   * Returns a string representation of the UTriplet object.
   * 
   * @return the string representation of the UTriplet object.
   */
  @Override
  public String toString() {
    if (this.relation != null)
      return this.relation.getName() + "(" + this.firstArgument.getName() + ", " + this.secondArgument.getName() + ")";
    return this.getName();
  }

  /**
   * Produces a hash value for the UTriplet object.
   * <p>
   * This method is the default hash function for strings.
   * 
   * @return the computed hash value.
   * */
  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    return this.hashCode() == o.hashCode();
  }

  /**
   * Reads in the serialized data from the mapped UTriplet ActionScript class.
   * <p>
   * We do not deserialize the URelation object. This would be too costly. Instead, we deserialize the serialized name of the relation. This is enough
   * information to reconstruct the relation. If the name starts with "?", then we can be sure to have a variable to ground --> so we construct a new URelation
   * object with the given name (variable to bind). Otherwise, we bind the UTriplet property "relation" to the static URelation object that is returned from the
   * list of static URelation objects (by using the deserialized relation name.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);

    UArgument arg1 = (UArgument) input.readObject();
    if (arg1.getName().startsWith("?"))
      firstArgument = new UArgument(arg1.getName());
    else
      firstArgument = new UEntity(arg1.getName());
    UArgument arg2 = (UArgument) input.readObject();
    if (arg2.getName().startsWith("?"))
      secondArgument = new UArgument(arg2.getName());
    else
      secondArgument = new UEntity(arg2.getName());
    selectivity = input.readDouble();

    String rel = input.readUTF();
    if (rel.startsWith("?"))
      relation = new URelation(rel);
    else
      relation = URelation.valueOf(rel); // return the suitable static URelation object

    // System.out.println("Is first argument a variable? : " + firstArgument.isVariable());
    // System.out.println("First argument is : " + firstArgument.getName());
    // System.out.println("Is second argument a variable? : " + secondArgument.isVariable());
    // System.out.println("Second argument is : " + secondArgument.getName());
    // System.out.println("Is relation a variable? : " + relation.isVariable());
    // System.out.println("Relation is : " + relation.getName());
    // System.out.println("Is relation a relation? : " + relation.isRelation());
    // System.out.println(" --------------------------------------------- ");
    // System.out.println(" relation name is : " + rel);
    // System.out.println(" relation object is : " + relation.getName());
    // System.out.println(" --------------------------------------------- ");
  }

  /**
   * Writes out the data to serialize to the mapped UTriplet ActionScript class.
   * <p>
   * We do not serialize the URelation object. This would be too costly. Instead, we serialize the name of the relation. This is enough information to
   * reconstruct the relation on the client side.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeObject(firstArgument);
    output.writeObject(secondArgument);
    output.writeDouble(selectivity);
    output.writeUTF(relation.getName()); // only write the name of the URelation object
  }

}
