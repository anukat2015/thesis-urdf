/**
 *
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents a query object that gives the user the opportunity to specify his information needs (via the GUI) regarding the reasoning process.
 * 
 * @author Timm Meiser
 * @since 07.01.10
 * @version 1.0
 */
public class UQuery extends URule {

  /** The empty default constructor for the UQuery object. */
  public UQuery() {
    super();
  }

  /**
   * The constructor that initializes the UQuery object.
   * 
   * @param name
   *          the name for the UQuery object.
   * @param conjunction
   *          the list of conjuncted literals that represent the query terms.
   * @throws Exception
   */
  public UQuery(String name, List<ULiteral> conjunction) throws Exception {
    super(name, conjunction);
  }

  /**
   * The constructor that initializes the UQuery object.
   * 
   * @param name
   *          the name for the UQuery object.
   * @param conjunction
   *          the list of conjuncted literals (undefined number of literals) that represent the query terms.
   * @throws Exception
   */
  public UQuery(String name, ULiteral... conjunction) throws Exception {
    super(name, Arrays.asList(conjunction));
  }

  /**
   * Initializes the UQuery object and checks that at least one constant is contained in the query.
   * 
   * @param name
   *          the name for the UQuery object.
   * @param conjunction
   *          the list of conjuncted literals (undefined number of literals) that represent the query terms.
   * @throws Exception
   * */
  public void init(String name, ULiteral... conjunction) throws Exception {
    this.init(name, Arrays.asList(conjunction));
  }

  /**
   * Reads in the serialized data from the mapped UQuery ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
  }

  /**
   * Writes out the data to serialize to the mapped UQuery ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
  }
}
