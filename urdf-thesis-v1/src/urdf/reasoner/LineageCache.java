package urdf.reasoner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import urdf.api.UFact;
import urdf.api.ULineageOr;

public class LineageCache {

  // lastContext -> Entries
  private HashMap<String, HashSet<LineageCacheEntry>> map;
  private LinkedList<LineageCacheEntry> entries;

  public LineageCache() {
    this.map = new HashMap<String, HashSet<LineageCacheEntry>>();
    this.entries = new LinkedList<LineageCacheEntry>();
  }

  public void put(LinkedList<LineageStackEntry> stack, HashMap<UFact, ULineageOr> lineage) {
    HashSet<String> context = new HashSet<String>();
    String lastContext = analyseStack(stack, context);
    // check for match!
    if (get(stack) != null) {
      // System.err.println("cache is more general! given : "+stack);
      return;
    }
    LineageCacheEntry entry = new LineageCacheEntry(lineage, context, lastContext);
    this.entries.add(entry);
    if (this.map.get(lastContext) == null)
      this.map.put(lastContext, new HashSet<LineageCacheEntry>());
    this.map.get(lastContext).add(entry);
    //System.out.println("PUT: "  + entry + " LINEAGE: " + entry.getLineage());
  }

  // returns null if nothing was found
  public HashMap<UFact, ULineageOr> get(LinkedList<LineageStackEntry> stack) {
    if (stack.isEmpty()) {
      System.err.println("empty stack was given!");
      return null;
    }
    HashSet<String> context = new HashSet<String>();
    String lastContext = analyseStack(stack, context);

    if (map.get(lastContext) == null)
      return null; // there is no match

    HashSet<LineageCacheEntry> results = new HashSet<LineageCacheEntry>();
    for (LineageCacheEntry e : this.map.get(lastContext)) {
      if (e.matches(context)) {
        //System.out.println("CACHE GET: " + e + " LINEAGE: " + e.getLineage());
        results.add(e);
      }
    }

    if (results.size() > 1)
      System.err.println("more than one match!!!");
    if (!results.isEmpty())
      return results.iterator().next().getLineage();
    
    return null;
  }

  public int size() {
    return this.entries.size();
  }

  public void clear() {
    this.entries.clear();
    this.map.clear();
  }

  private String analyseStack(LinkedList<LineageStackEntry> stack, HashSet<String> context) {
    if (stack.isEmpty()) {
      System.err.println("Empty Stack. Caching impossible!");
    }
    String lastContext = stack.getLast().getKey();
    for (LineageStackEntry e : stack) {
      if (e != stack.getLast())
        context.add(e.getKey());
    }
    return lastContext;
  }
}
