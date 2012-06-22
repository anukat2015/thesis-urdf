/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tmeiser
 * 
 */
public class URuleStore extends UObject {

	/** The list of soft rules to use for the reasoning engine. */
	private List<USoftRule> softRules;

	/** The list of hard rules to use for the reasoning engine. */
	private List<UHardRule> hardRules;

	private boolean softRulesChanged = false;
	private boolean hardRulesChanged = false;

	public URuleStore() {
		super();
		this.softRules = new ArrayList<USoftRule>();
		this.hardRules = new ArrayList<UHardRule>();
	}

	public URuleStore(List<USoftRule> sRules, List<UHardRule> hRules) {
		super();
		this.softRules = sRules;
		this.hardRules = hRules;
	}

	public void init(List<USoftRule> sRules, List<UHardRule> hRules) {
		super.init("Rule Store");
		this.softRules = sRules;
		this.hardRules = hRules;
	}

	/**
	 * @param softRulesChanged
	 *          the softRulesChanged to set
	 */
	public void setSoftRulesChanged(boolean softRulesChanged) {
		this.softRulesChanged = softRulesChanged;
	}

	/**
	 * @return the softRulesChanged
	 */
	public boolean isSoftRulesChanged() {
		return softRulesChanged;
	}

	/**
	 * @param hardRulesChanged
	 *          the hardRulesChanged to set
	 */
	public void setHardRulesChanged(boolean hardRulesChanged) {
		this.hardRulesChanged = hardRulesChanged;
	}

	/**
	 * @return the hardRulesChanged
	 */
	public boolean isHardRulesChanged() {
		return hardRulesChanged;
	}

	/**
	 * @return the softRules
	 */
	public List<USoftRule> getSoftRules() {
		return softRules;
	}

	/**
	 * @param softRules
	 *          the softRules to set
	 */
	public void setSoftRules(List<USoftRule> softRules) {
		this.softRules = softRules;
	}

	/**
	 * @return the hardRules
	 */
	public List<UHardRule> getHardRules() {
		return hardRules;
	}

	/**
	 * @param hardRules
	 *          the hardRules to set
	 */
	public void setHardRules(List<UHardRule> hardRules) {
		this.hardRules = hardRules;
	}

	public void clear() {
		if (this.hardRules != null) {
			this.hardRules.clear();
			this.hardRules = null;
		}
		if (this.softRules != null) {
			this.softRules.clear();
			this.softRules = null;
		}
	}

	protected void finalize() {
		this.clear();
	}

	/**
	 * Reads in the serialized data from the mapped URuleStore ActionScript class.
	 * 
	 * @param input
	 *          the serialized input data.
	 * */
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		super.readExternal(input);

		// get the number of soft rules to deserialize
		int size = input.readInt();
		while (size > 0) {
			this.softRules.add((USoftRule) input.readObject());
			size--;
		}
		// get the number of hard rules to deserialize
		size = input.readInt();
		while (size > 0) {
			this.hardRules.add((UHardRule) input.readObject());
			size--;
		}

		this.softRulesChanged = input.readBoolean();
		this.hardRulesChanged = input.readBoolean();

	}

	/**
	 * Writes out the data to serialize to the mapped UTriplet ActionScript class.
	 * <p>
	 * We do not serialize the URelation object. This would be too costly. Instead, we serialize the name of the relation. This is enough information to
	 * reconstruct the relation on the client side.
	 * 
	 * @param output
	 *          the output data to serialize.
	 * */
	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		super.writeExternal(output);
		// get the number of soft rules to deserialize
		int size = this.softRules.size();
		// send the size value to the client
		output.writeInt(size);
		for (int i = 0; i < size; i++) {
			output.writeObject(this.softRules.get(i));
		}
		// get the number of hard rules to deserialize
		size = this.hardRules.size();
		// send the size value to the client
		output.writeInt(size);
		for (int i = 0; i < size; i++) {
			output.writeObject(this.hardRules.get(i));
		}

		// output.writeBoolean(changed);
		// output.writeBoolean(this.softRulesChanged);
		// output.writeBoolean(this.hardRulesChanged);
	}

}
