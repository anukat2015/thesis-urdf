/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a competitive (mutually exclusive) set of atomic literals (signs are ignored).
 * <p>
 * By using the constructors and/or the <i> init </i>-methods, it is checked that no arithmetic predicates (relations) are used. Otherwise, an error message
 * will be thrown.
 * 
 * 
 * @author Timm Meiser
 * @since 18.01.10
 * @version 1.0
 * 
 */
public class UHardRule extends URule {

  /** The empty default constructor for the UHardRule object. */
  public UHardRule() {
    super();
  }

  /**
   * The constructor that initializes the UHardRule object.
   * 
   * @param name
   *          the name for the UHardRule object.
   * @param literal
   *          the first literal to add to the internal list of literals.
   * @throws Exception
   * */
  public UHardRule(String name, ULiteral literal) throws Exception {
    super(name);
    List<ULiteral> literals = new ArrayList<ULiteral>(1);
    literals.add(literal);
    this.init(name, literals);
  }

  /**
   * The constructor that initializes the UHardRule object.
   * 
   * @param name
   *          the name for the UHardRule object.
   * @param literals
   *          the list of literals to overwrite the internal list of literals with.
   * @throws Exception
   * */
  public UHardRule(String name, List<ULiteral> literals) throws Exception {
    super();
    this.init(name, literals);
  }

  /**
   * Initializes the UHardRule object.
   * <p>
   * If the there is at least one <i> arithmetic </i> relation contained in one of the literals of the given list of literals, the method throws an exception
   * 
   * @param name
   *          the name for the hard rule.
   * @param literals
   *          the list of literals to replace the previous list (probably empty).
   * @throws Exception
   */
  @Override
  public void init(String name, List<ULiteral> literals) throws Exception {
    if (literals != null) {
      for (ULiteral literal : literals) {
        UArgument relationArgument = literal.getRelation();
        if (relationArgument != null && relationArgument.isRelation()) {
          URelation relation = (URelation) relationArgument;
          if (relation.isArithmetic())
            throw new Exception("Arithmetic predicate '" + literal.getRelationName() + "' is not allowed in a hard rule: " + this);
        }
      }
    }
    super.init(name, literals);
  }

  /**
   * Reads in the serialized data from the mapped UHardRule ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
  }

  /**
   * Writes out the data to serialize to the mapped UHardRule ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
  }
}
