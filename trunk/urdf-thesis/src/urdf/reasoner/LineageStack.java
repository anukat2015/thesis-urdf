package urdf.reasoner;

import java.util.HashSet;
import java.util.LinkedList;

import urdf.api.UBindingSet;
import urdf.api.USoftRule;

public class LineageStack {

  private HashSet<String> cycleBreak;

  public LinkedList<LineageStackEntry> stack;

  public LineageStack() {
    this.cycleBreak = new HashSet<String>();
    this.stack = new LinkedList<LineageStackEntry>();
  }

  public boolean contains(USoftRule rule, UBindingSet bindings) {
    return cycleBreak.contains(rule.toString() + "$" + bindings.toString());
  }

  public boolean containsRule(USoftRule rule) {
	  for (LineageStackEntry entry: stack)
	    if (entry.getRule() == rule)
	      return true;
    return false;
  }

  public void remove(USoftRule rule, UBindingSet bindings) {
    String key = rule.toString() + "$" + bindings.toString();
    if (this.stack.isEmpty()) {
      System.err.println("tried to remove from empty lineage stack!");
      return;
    }
    if (!this.stack.getLast().getKey().equals(key)) {
      System.err.println("last entry on lineage stack was different!");
      return;
    }
    this.cycleBreak.remove(key);
    this.stack.removeLast();
  }

  public void add(USoftRule rule, UBindingSet bindings) {
    String key = rule.toString() + "$" + bindings.toString();
    if (this.cycleBreak.contains(key)) {
      System.err.println("cycle on stack!");
      return;
    }
    this.stack.add(new LineageStackEntry(rule, bindings));
    this.cycleBreak.add(key);
  }

  public LinkedList<LineageStackEntry> getStack() {
    return this.stack;
  }
}
