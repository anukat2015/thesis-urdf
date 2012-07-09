package urdf.reasoner;

import java.util.HashMap;

import urdf.api.UArgument;
import urdf.api.UFact;
import urdf.api.UFactSet;

public class Cache extends HashMap<String, UFactSet> {

  private static final long serialVersionUID = 1L;

	public UFactSet get(UArgument binding1, UArgument binding2, UArgument binding3) {
		String key = (binding1 != null ? binding1.getName() : "") + "$" + (binding2 != null ? binding2.getName() : "") + "$"
		    + (binding3 != null ? binding3.getName() : "");
		return super.get(key);
	}

	public void put(UFact fact) {
		String key = fact.getFirstArgument().getName() + "$" + fact.getSecondArgument().getName() + "$" + fact.getRelation().getName();
		super.put(key, new UFactSet(fact));
	}

	public void put(UArgument binding1, UArgument binding2, UArgument binding3, UFactSet facts) {
		String key = (binding1 != null ? binding1.getName() : "") + "$" + (binding2 != null ? binding2.getName() : "") + "$"
		    + (binding3 != null ? binding3.getName() : "");
		super.put(key, facts);
	}
}
