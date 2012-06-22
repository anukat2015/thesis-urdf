package urdf.api;

import java.io.BufferedWriter;
import java.io.Externalizable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public abstract class ULineageAbstract implements Externalizable { // extends UObject {

  public int tmp; // always reset it to 0, after usage
  public boolean visited; // always reset to false after usage

  protected HashSet<ULineageAbstract> children;

  protected int size;
  protected double conf;

  private boolean cycle; // used for toString(int)

  public ULineageAbstract() {
    this.children = new HashSet<ULineageAbstract>();
    this.size = 1;
    this.tmp = 0;
  }

  /*
   * Set methods
   */
  public void addChild(ULineageAbstract child) {
    if (child == null) {
      return;
    }
    //System.out.println(" CONTAINS CHILD: " + child + " > " + this.children.contains(child));
    if (!this.children.contains(child)) {
      //System.out.println(children);
      this.children.add(child);
      //System.out.println(children);
      this.size += child.size;
    }
  }

  public void setConf(double conf) {
    this.conf = conf;
  }

  /*
   * Get methods
   */
  public HashSet<ULineageAbstract> getChildren() {
    return this.children;
  }

  public int size() {
    return this.size;
  }

  public double getConf() {
    return this.conf;
  }

  /*
   * Algorithmic Methods
   */

  /*
   * cuts the DAG such that on every path from this node to a leaf every fact occurs at most once
   * 
   * returns null, if the resulting DAG is empty
   */
  public ULineageAbstract oneFactPerPath(UFactSet dependencyGraph, HashSet<UGroundedSoftRule> softRules, HashSet<UGroundedHardRule> hardRules) {
    return this.oneFactPerPath(null, new HashSet<UFact>(), dependencyGraph, softRules, hardRules);
  }

  /*
   * if there were no deletions in the subtree, this method returns the same object. Otherwise it must return a new one. (Without this semantic we would alter
   * the datastructure in the cache!)
   */
  protected abstract ULineageAbstract oneFactPerPath(ULineageAbstract caller, HashSet<UFact> factsAbove, UFactSet factsBelow,
      HashSet<UGroundedSoftRule> softRules, HashSet<UGroundedHardRule> hardRules);

  /*
   * returns true, if 'this' and 'other' form roots of equivalent trees
   */
  public boolean equivalentSubTrees(ULineageAbstract other) {
    LinkedList<ULineageAbstract> trace = new LinkedList<ULineageAbstract>();
    if (!this.equivalentSubTrees(other, trace)) {
      System.err.println(trace);
      return false;
    }
    return true;
  }

  protected boolean equivalentSubTrees(ULineageAbstract other, LinkedList<ULineageAbstract> trace) {
    if (this == other)
      return true;

    trace.add(this);

    // compare both nodes
    if (this instanceof ULineageOr && this instanceof ULineageAnd)
      return false;
    if (this instanceof ULineageAnd && this instanceof ULineageOr)
      return false;
    if (!this.equivalentSubTreesSpecific(other))
      return false;

    // compare subtrees
    if (this.children.size() != other.children.size())
      return false;
    for (ULineageAbstract childThis : this.children) {
      boolean match = false;
      for (ULineageAbstract childOther : other.children) {
        if (childThis.equivalentSubTrees(childOther, trace)) {
          match = true;
          break;
        }
        trace.removeLast(); // results from call to child
      }
      if (!match)
        return false;
    }
    trace.removeLast();
    return true;
  }

  protected abstract boolean equivalentSubTreesSpecific(ULineageAbstract other);

  /*
   * collects all successors in the DAG in 'nodes', where the Integer in the map will be a unique ID
   * 
   * baseOnly == true -> nodes will only contain ULineageAnds denoting, that its parent is from DB / Ar
   */
  protected void getSuccessors(HashMap<ULineageAbstract, Integer> nodes) {
    if (nodes.containsKey(this))
      return;
    nodes.put(this, nodes.size());
    for (ULineageAbstract child : this.children)
      child.getSuccessors(nodes);
    this.getSuccessorsSpecific(nodes);
  }

  protected abstract void getSuccessorsSpecific(HashMap<ULineageAbstract, Integer> nodes);

  /*
   * copy
   */
  protected void copy(ULineageAbstract newOne) {
    newOne.conf = this.conf;
    newOne.size = this.size;
    newOne.children.addAll(this.children);
  }

  /*
   * merging two nodes
   */
  protected abstract boolean mergePossible(ULineageAbstract other);

  public void merge(ULineageAbstract other) {
    if (this.mergePossible(other)) {
      this.children.addAll(other.children);
      other.children.addAll(this.children);
      this.conf = 0.0;
      other.conf = 0.0;
      this.size = 1;
      for (ULineageAbstract child : this.children) {
        this.size += child.size;
      }
      other.size = this.size;
    } else
      System.err.println("invalid merge");
  }

  /*
   * Methods for producing string output
   */

  protected abstract String getDescription();

  public String toString() {
    String s = "[" + getDescription();
    // s += "@" + this.conf;
    // s += " c:" + this.children.size();
    // s += " s:" + this.size;
    // if (this.tmp != 0)
    // s += " t:" + this.tmp;
    return s + "]";
  }

  protected abstract String getStringAtLevel(int level);

  public String toString(int level) {
    if (this.cycle) {
      System.err.println("cycle in " + this.toString());
      return "cycle\n";
    }
    this.cycle = true;
    String s = getStringAtLevel(level);
    this.cycle = false;
    return s;
  }

  /*
   * Methods for writing file output
   */

  public void writeGraphVizFile(int i) {
    HashMap<ULineageAbstract, Integer> nodes = new HashMap<ULineageAbstract, Integer>();
    getSuccessors(nodes);
    writeGraphVizFile(getDescription().replaceAll("\\W", "") + i, nodes);
  }

  protected abstract void writeSpecificGraphVizEdges(BufferedWriter w, HashMap<ULineageAbstract, Integer> nodes) throws Exception;

  private void writeGraphVizFile(String fileName, HashMap<ULineageAbstract, Integer> nodes) {
    BufferedWriter b = null;

    try {
      File file = new File(fileName + ".gv");
      file.createNewFile();
      b = new BufferedWriter(new FileWriter(file));
      b.write("graph g {\n  rankdir=TB;\n  node [shape=box];\n");

      // print vertices
      for (ULineageAbstract v : nodes.keySet()) {
        b.write(nodes.get(v) + " [ label =\"" + v.getDescription() + "@" + v.getConf() + "\"];\n");
      }

      // print edges
      for (ULineageAbstract v : nodes.keySet()) {
        for (ULineageAbstract child : v.children) {
          b.write(nodes.get(v) + " -- " + nodes.get(child) + " [dir=forward];\n");
        }
        v.writeSpecificGraphVizEdges(b, nodes);
      }

      // print end
      b.write("};\n");
    } catch (Exception e) {
      System.err.println(e.getMessage());
    } finally {
      try {
        if (b != null) {
          b.flush();
          b.close();
        }
      } catch (Exception e) {
        System.err.println(e.getMessage());
      }
    }
  }

  /**
   * Reads in the serialized data from the mapped ULineageAbstract ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  // @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    // this.readExternal(input);
  }

  /**
   * Writes out the data to serialize to the mapped ULineageAbstract ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  // @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    // super.writeExternal(output);

    int counter = 0;
    for (@SuppressWarnings("unused")
    ULineageAbstract lin : this.children) {
      counter++;
    }
    output.writeInt(counter);

    for (ULineageAbstract lin : this.children) {
      output.writeObject(lin);
    }

    output.writeInt(size);
    output.writeDouble(conf);
    output.writeBoolean(cycle);
  }
}
