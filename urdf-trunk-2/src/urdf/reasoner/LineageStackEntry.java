package urdf.reasoner;

import urdf.api.UBindingSet;
import urdf.api.URule;

public class LineageStackEntry {
	private UBindingSet bindings;
	private URule softRule;
	private String key;

	public LineageStackEntry(URule s, UBindingSet b) {
		this.bindings = b;
		this.softRule = s;
		this.key = s.toString() + "$" + b.toString();
	}

	public UBindingSet getBindings() {
		return this.bindings;
	}

	public URule getRule() {
		return this.softRule;
	}

	public String getKey() {
		return this.key;
	}

	public boolean equals(LineageStackEntry other) {
		return this.key.equals(other.key);
	}

	public String toString() {
		return this.key;
	}
}
