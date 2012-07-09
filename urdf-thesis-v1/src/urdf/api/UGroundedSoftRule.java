/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.NumberFormat;
import java.util.Collection;

/**
 * This class represents a grounded soft-rule inferred by the reasoner.
 * 
 * @author Timm Meiser
 * @since 19.01.10
 * @version 1.0
 */
public class UGroundedSoftRule extends UFactSet {

  /** The head fact that is inferred through grounding the used ungrounded soft-rule. */
  private UFact head;

  /** A reference to the soft rule that produced this UGroundedSoftRule object. */
  private USoftRule softRule = null;

  /** The weight of this UGroundedSoftRule object. */
  private double weight = 0;

  public Collection<USoftRulePartition> partitions = null;

  /** The empty default constructor for the UGroundedSoftRule object. */
  public UGroundedSoftRule() {
    super();
  }

  public UGroundedSoftRule(UFact head) {
    this.init(new UFactSet(), head, null);
  }

  public UGroundedSoftRule(UFact head, double weight) {
    this.init(new UFactSet(), head, weight);
  }

  public UGroundedSoftRule(UFactSet factSet, UFact head, double conf) {
    this.init(factSet, head, conf);
  }

  public UGroundedSoftRule(UFactSet factSet, UFact head, USoftRule softRule) {
    this.init(factSet, head, softRule);
  }

  /**
   * Initializes this UGroundedSoftRule object by adding all facts of the given fact-set as implicitly negative facts and the given single fact as an implicitly
   * positive one (this is the head of the UGroundedSoftRule object).
   * <p>
   * The given USoftRule objects acts as the source for this UGroundedSoftRule object (a grounded version of the specified soft-rule).
   * 
   * @param factSet
   *          the fact-set whose facts act as negative inputs to this UGroundedSoftRule object.
   * @param head
   *          the given fact that acts as the positive head of this UGroundedSoftRule object.
   * @param softRule
   *          the soft rule that is the source of this grounded soft rule.
   * */
  public void init(UFactSet factSet, UFact headArg, USoftRule softRuleArg) {
    super.init(factSet);
    this.head = headArg;
    this.softRule = softRuleArg;
    if (softRule != null) { // check if the soft rule is accessible
      this.setSoftRule(softRuleArg);
    }
  }

  /**
   * Initializes this UGroundedSoftRule object by adding all facts of the given fact-set as implicitly negative facts and the given single fact as an implicitly
   * positive one (this is the head of the UGroundedSoftRule object).
   * <p>
   * Internally, the source soft rule is set to null.
   * 
   * @param factSet
   *          the fact-set whose facts act as negative inputs to this UGroundedSoftRule object.
   * @param head
   *          the given fact that acts as the positive head of this UGroundedSoftRule object.
   * */
  public void init(UFactSet factSet, UFact headArg) {
    this.init(factSet, headArg, null);
  }

  public void init(UFactSet factSet, UFact headArg, double conf) {
    super.init(factSet);
    this.head = headArg;
    this.weight = conf;
    this.softRule = null;
  }

  /**
   * Delivers the head of the grounded soft-rule (UGroundedSoftRule object).
   * 
   * @return the head of the soft-rule.
   */
  public UFact getHead() {
    return head;
  }

  /**
   * Sets the head for the grounded soft-rule (UGroundedSoftRule object).
   * 
   * @param head
   *          the head of the soft-rule.
   */
  public void setHead(UFact head) {
    this.head = head;
  }

  /**
   * Delivers the soft-rule that produced to this signed fact set.
   * 
   * @return the softRule that was used to produce this grounded soft-rule.
   */
  public USoftRule getSoftRule() {
    return this.softRule;
  }

  /**
   * Sets the soft-rule that produced this grounded soft-rule.
   * 
   * @param softRule
   *          the softRule that was used to produce this grounded soft-rule.
   */
  public void setSoftRule(USoftRule softRuleArg) {
    this.softRule = softRuleArg;
    this.weight = softRule.getWeight();
    this.setName(softRuleArg.getName() + ":" + head.toString());
  }

  /**
   * Returns the string representation of the whole UGroundedSoftRule object (all values within the object).
   * 
   * @return the string representation of the USoftRule object.
   */
  @Override
  public String toString() {
    return NumberFormat.getInstance().format(getWeight()) + "[" + this.toStringNoWeight() + "]";
  }

  public void setWeight(double w) {
    this.weight = w;
  }

  public double getWeight() {
    return weight;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj;
  }

  private String toStringNoWeight() {
    String s = head.toString();
    s += (size() > 0 ? " <= " : "");
    int i = 0;
    for (UFact fact : this) {
      s += fact + (i < this.size() - 1 ? ", " : "");
      i++;
    }
    return s;
  }

  public int isSatisfied() {
    int assigned = 0;
    if (head.getTruthValue() == UFact.TRUE)
      return UFact.TRUE;
    else if (head.getTruthValue() == UFact.FALSE)
      assigned++;
    for (UFact fact : this) {
      if (fact.getTruthValue() == UFact.FALSE)
        return UFact.TRUE;
      else if (fact.getTruthValue() == UFact.TRUE)
        assigned++;
    }
    if (assigned == this.size() + 1)
      return UFact.FALSE; // unsatisfied
    return UFact.UNKNOWN; // not yet all assigned, hence unknown
  }

  /**
   * Reads in the serialized data from the mapped UGroundedSoftRule ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
    head = (UFact) (input.readObject());
    softRule = (USoftRule) (input.readObject());
  }

  /**
   * Writes out the data to serialize to the mapped UGroundedSoftRule ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeObject(head);
    output.writeObject(softRule);
  }
}
