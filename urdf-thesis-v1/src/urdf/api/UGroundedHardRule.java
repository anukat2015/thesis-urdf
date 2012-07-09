/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents a grounded hard rule inferred by the reasoner.
 * 
 * @author Timm Meiser
 * @since 19.01.10
 * @version 1.0
 */
public class UGroundedHardRule extends UFactSet {

  /** A reference to the hard rule that produced this fact set. */
  private UHardRule hardRule = null;

  /** The empty default constructor for the UGroundedHardRule object. */
  public UGroundedHardRule() {
    super();
  }

  /**
   * The constructor that initializes the UGroundedHardRule object by adding all facts of the given fact set and the given hard rule as the source for this
   * grounded instance.
   * 
   * @param factSet
   *          the set of facts to use for this UGroundedHardRule object.
   * @param hardRule
   *          the hard-rule which represents the source for this UGroundedHardRule object.
   */
  public UGroundedHardRule(UFactSet factSet, UHardRule hardRule) {
    super(factSet.size());
    this.addAll(factSet);
    this.hardRule = hardRule;
  }

  public UGroundedHardRule(UFactSet factSet) {
    super(factSet.size());
    this.addAll(factSet);
    this.hardRule = null;
  }

  /**
   * Initializes the UGroundedHardRule object by adding all facts of the given fact set and the given hard rule as the source for this grounded instance.
   * 
   * @param factSet
   *          the set of facts to use for this UGroundedHardRule object.
   * @param rule
   *          the hard-rule which represents the source for this UGroundedHardRule object.
   */
  public void init(UFactSet factSet, UHardRule rule) {
    super.init(factSet.size());
    this.addAll(factSet);
    this.hardRule = rule;
  }

  /**
   * Delivers the hard rule that belongs to that UGroundedHardRule object.
   * 
   * @return the used hard rule.
   */
  public UHardRule getHardRule() {
    return hardRule;
  }

  /**
   * Sets the hard rule that belongs to that UGroundedHardRule object.
   * 
   * @param hardRule
   *          the hard rule to set for this UGroundedHardRule object.
   */
  public void setHardRule(UHardRule hardRule) {
    this.hardRule = hardRule;
  }

  /**
   * Prints the facts in this hard rule.
   * 
   * @return the string representation of the facts in this hard rule.
   * */
  public String toString() {
    return getFactSet().toString();
  }

  /**
   * Reads in the serialized data from the mapped UGroundedHardRule ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
    hardRule = (UHardRule) (input.readObject());
  }

  /**
   * Writes out the data to serialize to the mapped UGroundedHardRule ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeObject(hardRule);
  }
}
