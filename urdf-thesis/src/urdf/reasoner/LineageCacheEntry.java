package urdf.reasoner;

import java.util.HashMap;
import java.util.HashSet;

import urdf.api.UFact;
import urdf.api.ULineageOr;

public class LineageCacheEntry {
	private HashMap<UFact, ULineageOr> entry;
	private String lastContext;
	private HashSet<String> context;

	public LineageCacheEntry(HashMap<UFact, ULineageOr> entry, HashSet<String> context, String lastContext) {
		this.entry = entry;
		this.context = context;
		this.lastContext = lastContext;
	}

	public boolean matches(HashSet<String> otherContext) {
		if (otherContext.containsAll(this.context))
			return true;
		return false;
	}

	public HashMap<UFact, ULineageOr> getLineage() {
		return this.entry;
	}

	public String toString() {
		String s = "context: [" + this.context.toString() + " last: ";
		s += this.lastContext + "]";
		return s;
	}
}
