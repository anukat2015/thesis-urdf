package urdf.api;

import java.io.BufferedWriter;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ULineageOr extends ULineageAbstract {

  protected UFact fact;

  public ULineageOr(UFact f) {
    super();
    this.fact = f;
  }

  public UFact getFact() {
    return this.fact;
  }

  protected ULineageAbstract oneFactPerPath(ULineageAbstract parent, HashSet<UFact> factsAbove, UFactSet factsBelow, HashSet<UGroundedSoftRule> softRules,
      HashSet<UGroundedHardRule> hardRules) {
    // a fact may occur only once per path
    if (factsAbove.contains(this.fact)) {
      return null;
    }

    factsAbove.add(this.fact);

    boolean modified = false;
    HashSet<ULineageAbstract> newChildren = new HashSet<ULineageAbstract>();
    for (ULineageAbstract child : super.children) {
      HashSet<UGroundedSoftRule> softRulesFromChild = new HashSet<UGroundedSoftRule>();
      HashSet<UGroundedHardRule> hardRulesFromChild = new HashSet<UGroundedHardRule>();
      UFactSet factsBelowChild = new UFactSet();
      ULineageAbstract result = child.oneFactPerPath(this, factsAbove, factsBelowChild, softRulesFromChild, hardRulesFromChild); // down
      modified = modified || result != child;
      if (result != null) {
        newChildren.add(result);
        factsBelow.addAll(factsBelowChild);
        softRules.addAll(softRulesFromChild);
        hardRules.addAll(hardRulesFromChild);
      }
    }

    factsAbove.remove(this.fact);

    if (newChildren.isEmpty()) {
      return null;
    }
    if (this.fact != null) {
      factsBelow.add(this.fact);
      if (this.fact.getGroundedHardRule() != null) {
        hardRules.add(this.fact.getGroundedHardRule());
        for (UFact f : this.fact.getGroundedHardRule()) {
          if (f != this.fact)
            factsBelow.add(f);
        }
      }
    }
    if (!modified)
      return this;
    ULineageOr newOne = new ULineageOr(this.fact);
    newOne.conf = this.conf;
    newOne.children = newChildren;
    newOne.size = 1;
    for (ULineageAbstract l : newChildren)
      newOne.size += l.size;
    return newOne;
  }

  protected boolean equivalentSubTreesSpecific(ULineageAbstract other) {
    ULineageOr or = (ULineageOr) other;
    if (or.fact != this.fact)
      return false;
    return true;
  }

  protected String getDescription() {
    if (this.fact != null)
      return "OR " + this.fact.getRelationName() + "(" + this.fact.getFirstArgumentName() + "," + this.fact.getSecondArgumentName() + ")";
    return "OR";
  }

  protected void getSuccessorsSpecific(HashMap<ULineageAbstract, Integer> nodes) {
    if (this.fact != null && this.fact.getGroundedHardRule() != null) {
      for (UFact f : this.fact.getGroundedHardRule()) {
        if (f != this.fact && f.getLineage() != null)
          f.getLineage().getSuccessors(nodes);
      }
    }
  }

  protected void writeSpecificGraphVizEdges(BufferedWriter w, HashMap<ULineageAbstract, Integer> nodes) throws Exception {
    if (this.fact != null && this.fact.getGroundedHardRule() != null) {
      for (UFact f : this.fact.getGroundedHardRule()) {
        if (f != this.fact && f.getLineage() != null)
          w.append(nodes.get(this) + " -- " + nodes.get(f.getLineage()) + " [dir=forward color=red];\n");
      }
    }
  }

  public boolean mergePossible(ULineageAbstract other) {
    return this.fact == ((ULineageOr) other).fact;
  }

  protected String getStringAtLevel(int level) {
    String s = "";
    int i = 0;
    for (ULineageAbstract child : super.children) {
      if (i > 0)
        s += urdf.main.URDF_main.getSp(level) + "OR\n";
      if (this.fact != null) {
        s += urdf.main.URDF_main.getSp(level) + (((ULineageAnd) child).groundedRule == null || child.children.isEmpty() ?
        //this.fact.getRelationName() + "(" + this.fact.getFirstArgumentName()
        //+ "," + this.fact.getSecondArgumentName() + ")[" + UFact.TRUTH_LABELS[this.fact.getTruthValue()] + "|"
        //+ NumberFormat.getInstance().format(super.conf) + "]" 
        this.fact.toString() + "@" + NumberFormat.getInstance().format(super.conf)
            : "");
        // if (super.tmp != 0)
        // s += " t:" + super.tmp + " " + super.conf + " ";
      }
      s += child.toString(level);
      i++;
    }
    return s;
  }

  /**
   * Reads in the serialized data from the mapped ULineageOr ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
  }

  /**
   * Writes out the data to serialize to the mapped ULineageOr ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeObject(this.fact);
  }

}
