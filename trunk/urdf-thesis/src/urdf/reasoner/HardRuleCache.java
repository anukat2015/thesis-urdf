package urdf.reasoner;

import urdf.api.UBindingSet;
import urdf.api.UHardRule;

import java.util.HashSet;
import java.util.HashMap;

public class HardRuleCache {
	private HashSet<String> done;
	private HashMap<String, LineageStackEntry> groundingTasks;

	public HardRuleCache() {
		this.done = new HashSet<String>();
		this.groundingTasks = new HashMap<String, LineageStackEntry>();
	}

	public void put(UHardRule rule, UBindingSet bindings) {
		String key = rule.toString() + "$" + bindings.toString();
		if (!this.done.contains(key) && !this.groundingTasks.containsKey(key)) {
			this.groundingTasks.put(key, new LineageStackEntry(rule, bindings));
		}
	}

	public boolean hasNext() {
		return !this.groundingTasks.isEmpty();
	}

	public LineageStackEntry next() {
		String key = this.groundingTasks.keySet().iterator().next();
		this.done.add(key);
		LineageStackEntry result = this.groundingTasks.get(key);
		this.groundingTasks.remove(key);
		return result;
	}

	public void clear() {
		this.done.clear();
		this.groundingTasks.clear();
	}
}
