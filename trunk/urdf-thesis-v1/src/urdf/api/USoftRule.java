/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * This class represents a soft rule that is either specified by the user via the user interface of URDF or part of the internal rule storage.
 * <p>
 * The soft rules are needed within the reasoner to infer facts that are not directly stored in the database.
 * <p>
 * A soft rule is a conjunction of literals. There is a head of the rule (a single literal) and a body of several literals.
 * 
 * @author Timm Meiser
 * @since 18.01.10
 * @version 1.0
 * 
 */
public class USoftRule extends URule {

  /** The head of the soft rule. At least one of the head arguments should occur in the rule body as well. */
  private ULiteral head;

  /** The weight for constructing a horn clause. The weight reflects the confidence in the correctness of the rule. */
  private double weight = 0;

  // required for confidence sampling
  public transient int tmp;

  /** The empty default constructor for the USoftRule object. */
  public USoftRule() {
    super();
  }

  /**
   * The constructor that initializes the USoftRule object.
   * 
   * @param name
   *          the name for the USoftRule object.
   * @param head
   *          the head of the soft-rule this USoftRule object represents.
   * @param weight
   *          the weight for this soft-rule.
   * @param body
   *          list of literals that represent the body for this soft-rule.
   * @throws Exception
   * */
  public USoftRule(String name, ULiteral head, double weight, ULiteral... body) throws Exception {
    super();
    this.init(name, head, weight, Arrays.asList(body));
  }

  /**
   * The constructor that initializes the USoftRule object.
   * 
   * @param name
   *          the name for the USoftRule object.
   * @param head
   *          the head of the soft-rule this USoftRule object represents.
   * @param weight
   *          the weight for this soft-rule.
   * @param body
   *          list of literals that represent the body for this soft-rule.
   * @throws Exception
   * */
  public USoftRule(String name, ULiteral head, double weight, List<ULiteral> body) throws Exception {
    super();
    this.init(name, head, weight, body);
  }

  /**
   * Delivers the head literal of this USoftRule object.
   * 
   * @return the literal (atom) that represents the head of this USoftRule object.
   */
  public ULiteral getHead() {
    return head;
  }

  /**
   * Sets the head literal of the USoftRule object and automatically checks if at least one of the head literal arguments also occurs in the body of the rule.
   * <p>
   * If none of the arguments of the head literal occurs in the rule body as well ==> an error message is send back to caller of this method.
   * <p>
   * This method is the preferred one for the initialization of the USoftRule object, because all the needed checks-ups are done here.
   * 
   * @param name
   *          the name for the USoftRule object.
   * @param headFact
   *          the literal that should represent the head of this soft-rule.
   * @param bodyFacts
   *          the list of literals to override the internal one.
   * @throws Exception
   */
  public void init(String name, ULiteral headFact, List<ULiteral> bodyFacts) throws Exception {
    this.init(name, headFact, 1.0, bodyFacts);
  }

  /**
   * Sets the head literal of the USoftRule object and automatically checks if at least one of the head literal arguments also occurs in the body of the rule.
   * <p>
   * If none of the arguments of the head literal occurs in the rule body as well ==> an error message is send back to caller of this method.
   * <p>
   * This method is the preferred one for the initialization of the USoftRule object, because all the needed checks-ups are done here.
   * 
   * @param name
   *          the name for the USoftRule object.
   * @param headFact
   *          the literal that should represent the head for this soft-rule.
   * @param conf
   *          the a priori calculated weight of the soft-rule.
   * @param bodyFacts
   *          the list of literals to override the internal list.
   * @throws Exception
   */
  public void init(String name, ULiteral headFact, double conf, List<ULiteral> bodyFacts) throws Exception {
    super.init(name, bodyFacts);

    UArgument arg1 = headFact.getFirstArgument();
    UArgument arg2 = headFact.getSecondArgument();

    // Domain restriction for variables and constants
    boolean isFirstArgVariable = arg1.isVariable();
    boolean isSecondArgVariable = arg2.isVariable();

    this.head = headFact;
    this.weight = conf;

    if (isFirstArgVariable && !this.getVariables().contains(arg1))
      throw new Exception("HEAD VARIABLE 1 '" + arg1.getName() + "' MUST BE IN BODY OF CLAUSE: " + this);
    else if (isSecondArgVariable && !this.getVariables().contains(arg2))
      throw new Exception("HEAD VARIABLE 2 '" + arg2.getName() + "' MUST BE IN BODY OF CLAUSE: " + this);
    else if (!isFirstArgVariable && !this.getConstants().contains(arg1))
      throw new Exception("HEAD CONSTANT 1 '" + arg1.getName() + "' MUST BE IN BODY OF CLAUSE: " + this);
    else if (!isSecondArgVariable && !this.getConstants().contains(arg2))
      throw new Exception("HEAD CONSTANT 2 '" + arg2.getName() + "' MUST BE IN BODY OF CLAUSE: " + this);
    else if (!checkChaining(new HashSet<ULiteral>(), 0))
      throw new Exception("ATOMS NOT PROPERLY CHAINED: " + this); // check proper variable chaining for atoms
  }

  /**
   * Returns the weight of (belief in) the soft-rule.
   * 
   * @return the weight (computed belief in the rule) of this soft-rule
   */
  public double getWeight() {
    return weight;
  }

  /**
   * Sets the weight of the soft-rule.
   * 
   * @param weight
   *          the weight to set
   */
  public void setWeight(float weight) {
    this.weight = weight;
  }

  /**
   * Returns the string representation of the whole USoftRule object (all values within the object).
   * 
   * @return the string representation of the USoftRule object.
   */
  @Override
  public String toString() {
    String s = head.toString() + " <= ";
    int i = 0;
    for (ULiteral literal : this) {
      s += literal.toString() + (i < this.size() - 1 ? ", " : "");
      i++;
    }
    return s + " @ constants : " + this.getConstants().toString() + " , variables : " + this.getVariables().toString() + " ["
        + NumberFormat.getInstance().format(this.getWeight()) + "]";
  }

  /**
   * Reads in the serialized data from the mapped USoftRule ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
    head = (ULiteral) (input.readObject());
    weight = input.readDouble();
  }

  /**
   * Writes out the data to serialize to the mapped USoftRule ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeObject(head);
    output.writeDouble(weight);
  }
}
