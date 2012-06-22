/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.NumberFormat;

/**
 * This class represents a fact (grounded atom) that was produced during the reasoning process.
 * 
 * @author Timm Meiser
 * @since 25.11.09
 * @version 1.0
 * 
 */
public class UFact extends UTriplet implements Comparable<UFact> {

  /** The truth values necessary to mark the different facts. */
  public static final int FALSE = 0, TRUE = 1, UNKNOWN = 2;

  /** The truth labels as strings. */
  public static final String[] TRUTH_LABELS = { "FALSE", "TRUE", "UNKNOWN" };

  /** The base confidence of the fact in the knowledge base. */
  private double baseConfidence = 0;

  /** The truth value of this fact. */
  private int truthValue = UNKNOWN;

  /** The maximum lineage level at which this fact was derived, needed as key for lookups in lineage table. */
  public int max_level = 0;

  /** The minimum lineage level at which this fact was derived, needed as key for lookups in lineage table. */
  public int min_level = Integer.MAX_VALUE;

  /** The grounded hard rule that this signed fact depends on. */
  private UGroundedHardRule groundedHardRule = null;

  /** Entry point to the lineage DAG. */
  private ULineageOr lineage = null;

  // ----------- transient fields, only needed temporarily by the reasoner, not to be serialized ! ----------

  public transient double w_i, p_i;

  public transient int tmp_truth;

  // --------------------------------------------------------------------------------------------------------

  /** The empty default constructor for the UFact object. */
  public UFact() {
    super();
  }

  /** The default constructor for the UFact object with a name and confidence. */
  public UFact(String name, double conf) {
    super(name);
    this.setBaseConfidence(conf);
  }

  /**
   * The constructor that initializes the UFact object.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the first argument of the fact.
   * @param secondArgument
   *          the second argument of the fact.
   * @throws Exception
   */
  public UFact(URelation relation, UArgument firstArgument, UArgument secondArgument) throws Exception {
    this(relation, firstArgument, secondArgument, 1.0);
  }

  /**
   * The constructor that initializes the UFact object.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the first argument of the fact.
   * @param secondArgument
   *          the second argument of the fact.
   * @param baseConfidence
   *          the confidence in the correctness of the fact in the knowledge base.
   * @throws Exception
   */
  public UFact(URelation relation, UArgument firstArgument, UArgument secondArgument, double baseConfidence) throws Exception {
    super(relation, firstArgument, secondArgument);
    this.baseConfidence = baseConfidence;
  }

  /**
   * The constructor that initializes the UFact object.
   * <p>
   * The set of bindings is used to set the attribute values for the arguments and the relation, that define this fact. It is checked whether to use the
   * bindings from the bindings set or the attributes of the literal for the internal fact attributes. If an attribute of the literal is a constant, then this
   * constant is used to set the matching attribute for the fact. Otherwise a binding from the binding set is used.
   * 
   * @param literal
   *          the literal which is the ungrounded source for this fact (UFact object).
   * @param bindings
   *          a set of bindings to use for the internal argument and relation references (attributes).
   * @param confidence
   *          the confidence (believe) in the correctness of the fact.
   * @throws Exception
   */
  public UFact(ULiteral literal, UBindingSet bindings, double baseConfidence) throws Exception {
    this(literal.getRelation().isVariable() ? (URelation) bindings.getBinding(literal.getRelation()) : literal.getRelation(), literal.getFirstArgument()
        .isVariable() ? bindings.getBinding(literal.getFirstArgument()) : literal.getFirstArgument(), literal.getSecondArgument().isVariable() ? bindings
        .getBinding(literal.getSecondArgument()) : literal.getSecondArgument(), baseConfidence);
  }

  /**
   * Initializes the UFact object.
   * <p>
   * The relation and argument references (internal attribute values are checked) are checked to be constants, otherwise exceptions will be thrown.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the first argument for the fact.
   * @param secondArgument
   *          the second argument for the fact.
   * @throws Exception
   */
  public void init(URelation relation, UArgument firstArgument, UArgument secondArgument) throws Exception {
    this.init(relation, firstArgument, secondArgument, 1.0);
  }

  /**
   * Initializes the UFact object.
   * <p>
   * The relation and argument references (internal attribute values are checked) are checked to be constants, otherwise exceptions will be thrown.
   * 
   * @param relation
   *          the used relation.
   * @param firstArgument
   *          the first argument for the fact.
   * @param secondArgument
   *          the second argument for the fact.
   * @param confidence
   *          the confidence value for this UFact object to be correct.
   * @throws Exception
   */
  public void init(URelation relation, UArgument firstArgument, UArgument secondArgument, double confidence) throws Exception {
    super.init(relation, firstArgument, secondArgument);
    this.baseConfidence = confidence;
  }

  /**
   * Delivers the base confidence for the correctness of this UFact object.
   * 
   * @return the base confidence for the correctness of this UFact object.
   */
  public double getBaseConfidence() {
    return this.baseConfidence;
  }

  /**
   * Sets the base confidence value for this UFact object.
   * 
   * @param confidence
   *          the confidence in this fact
   */
  public void setBaseConfidence(double confidence) {
    this.baseConfidence = confidence;
  }

  /**
   * Delivers the truth value of the fact.
   * <p>
   * The following truth values are allowed and possible:
   * <p>
   * <i>UNKNOWN</i>, <i>FALSE</i> and <i>TRUE</i>
   * 
   * @return the truth value.
   */
  public int getTruthValue() {
    return this.truthValue;
  }

  /**
   * Sets the truth value for the UFact object.
   * <p>
   * The following truth values are allowed and possible:
   * <p>
   * <i>UNKNOWN</i>, <i>FALSE</i> and <i>TRUE</i>
   * 
   * @param truthValue
   *          the truth value for the UFact object.
   * @return true, if the given truth value could be set and is conform with the available truth values, false otherwise.
   */
  public boolean setTruthValue(int truthValue) {
    if (truthValue != FALSE && truthValue != TRUE && truthValue != UNKNOWN)
      return false;
    this.truthValue = truthValue;
    return true;
  }

  /**
   * Delivers the grounded hard-rule (UGroundedHardRule object) the fact belongs to.
   * 
   * @return the grounded hard-rule the fact belongs to.
   */
  public UGroundedHardRule getGroundedHardRule() {
    return this.groundedHardRule;
  }

  /**
   * Sets the grounded hard-rule (UGroundedHardRule object) the fact belongs to.
   * 
   * @param groundedHardRule
   *          grounded hard-rule the fact belongs to.
   */
  public void setGroundedHardRule(UGroundedHardRule groundedHardRule) {
    this.groundedHardRule = groundedHardRule;
  }

  /**
   * Returns a string representation of the UFact object.
   * <p>
   * 
   * @return the string representation of the fact.
   */
  @Override
  public String toString() {
    return super.toString() + "[" + TRUTH_LABELS[this.truthValue] + "|" + NumberFormat.getInstance().format(baseConfidence) + "]";
  }

  /**
   * Delivers the lineage for this UFact object.
   * 
   * @return the lineage for this UFact object.
   */
  public ULineageOr getLineage() {
    return lineage;
  }

  public void setLineage(ULineageOr l) {
    this.lineage = l;
  }

  // needed for MAX-SAT algorithm
  @Override
  public int compareTo(UFact fact) {
    //System.out.println("COMP " + fact.w_i + " " + this.w_i + " " + Double.compare(fact.w_i, this.w_i));
    return Double.compare(fact.w_i, this.w_i);
  }

  /**
   * Reads in the serialized data from the mapped UFact ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
    baseConfidence = input.readDouble();
    truthValue = input.readInt();
    groundedHardRule = (UGroundedHardRule) input.readObject();
    lineage = (ULineageOr) input.readObject();
  }

  /**
   * Writes out the data to serialize to the mapped UFact ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeDouble(baseConfidence);
    output.writeInt(truthValue);
    output.writeObject(groundedHardRule);
    output.writeObject(lineage);
  }

}
