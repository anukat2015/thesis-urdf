package urdf.api;

import java.io.BufferedWriter;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ULineageAnd extends ULineageAbstract {

  protected USoftRule rule;
  protected UGroundedSoftRule groundedRule;

  public ULineageAnd(USoftRule rule) {
    super();
    this.rule = rule;
    this.groundedRule = null;
  }

  public ULineageAnd copyNonRecursive() {
    ULineageAnd newOne = new ULineageAnd(this.rule);
    newOne.groundedRule = this.groundedRule;
    super.copy(newOne);
    return newOne;
  }

  public USoftRule getRule() {
    return this.rule;
  }

  public UGroundedSoftRule getGroundedRule() {
    return this.groundedRule;
  }

  protected ULineageAbstract oneFactPerPath(ULineageAbstract parent, HashSet<UFact> factsAbove, UFactSet factsBelow, HashSet<UGroundedSoftRule> softRules,
      HashSet<UGroundedHardRule> hardRules) {
    // in the case of a fact from DB / Ar there are no children
    boolean modified = false;

    // the following sets may only be added to the method parameters, if the loop succeeds!
    UFactSet allFactsBelow = new UFactSet();
    HashSet<ULineageAbstract> newChildren = new HashSet<ULineageAbstract>();
    HashSet<UGroundedSoftRule> softRulesBelow = new HashSet<UGroundedSoftRule>();
    HashSet<UGroundedHardRule> hardRulesBelow = new HashSet<UGroundedHardRule>();
    UFactSet factsInChildren = new UFactSet();
    for (ULineageAbstract child : super.children) {
      UFactSet belowChild = new UFactSet();

      ULineageAbstract result = child.oneFactPerPath(this, factsAbove, belowChild, softRulesBelow, hardRulesBelow); // down
      modified = modified || result != child;
      if (result == null) {
        return null; // current node is deleted
      }
      newChildren.add(result);
      allFactsBelow.addAll(belowChild);
      factsInChildren.add(((ULineageOr) result).getFact());
    }

    // the node still exists
    if (parent != null && this.groundedRule == null) {
      if (this.rule != null)
        this.groundedRule = new UGroundedSoftRule(factsInChildren, ((ULineageOr) parent).getFact(), this.rule);
      else
        this.groundedRule = new UGroundedSoftRule(((ULineageOr) parent).getFact());
    }
    if (this.groundedRule != null)
      softRules.add(this.groundedRule);

    // pass sets to caller
    factsBelow.addAll(allFactsBelow);
    softRules.addAll(softRulesBelow);
    hardRules.addAll(hardRulesBelow);

    if (!modified)
      return this;

    ULineageAnd newOne = new ULineageAnd(this.rule);
    newOne.conf = this.conf;
    newOne.children = newChildren;
    newOne.size = 1;
    newOne.groundedRule = this.groundedRule;
    for (ULineageAbstract l : newChildren)
      newOne.size += l.size;
    return newOne;
  }

  protected boolean equivalentSubTreesSpecific(ULineageAbstract other) {
    ULineageAnd and = (ULineageAnd) other;
    if (and.rule != this.rule)
      return false;
    if (and.groundedRule == null && this.groundedRule != null)
      return false;
    if (and.groundedRule != null && this.groundedRule == null)
      return false;
    if (and.groundedRule != null && this.groundedRule != null && !and.groundedRule.equals(this.groundedRule))
      return false;
    return true;
  }

  protected void getSuccessorsSpecific(HashMap<ULineageAbstract, Integer> nodes) {
    // empty
  }

  protected void writeSpecificGraphVizEdges(BufferedWriter w, HashMap<ULineageAbstract, Integer> nodes) throws Exception {
    // empty
  }

  protected boolean mergePossible(ULineageAbstract other) {
    return this.rule == ((ULineageAnd) other).rule;
  }

  protected String getDescription() {
    if (this.children.isEmpty())
      return "DB/AR";
    if (this.groundedRule != null)
      return "AND " + this.groundedRule.toString();
    if (this.rule != null)
      return "AND " + this.rule.toString();
    return "AND";
  }

  protected String getStringAtLevel(int level) {
    if (super.children.isEmpty())
      return " <= DB/Ar\n";
    String s = "";
    if (this.groundedRule != null) {
      s += this.groundedRule.toString();
      s += "@" + NumberFormat.getInstance().format(this.conf);
      // if (super.tmp != 0)
      // s += " t:" + super.tmp + " ";
      s += "\n";
    } else if (this.rule != null) {
      s += this.rule.toString();
      s += "@" + NumberFormat.getInstance().format(this.conf);
      // if (super.tmp != 0)
      // s += " t:" + super.tmp + " ";
      s += "\n";
    }
    for (ULineageAbstract child : super.children) {
      s += child.toString(level + 1);
    }
    return s;
  }

  /**
   * Reads in the serialized data from the mapped ULineageAnd ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
  }

  /**
   * Writes out the data to serialize to the mapped ULineageAnd ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeObject(this.rule);
    output.writeObject(this.groundedRule);
  }
}
