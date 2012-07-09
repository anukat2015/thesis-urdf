package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a binding set which is used store all the produced distinct bindings.
 * 
 * @author Martin Theobald
 * @since 30.01.10
 * @version 1.0
 */
public class UBindingSet extends UObject implements Iterable<UBinding> {

	/** The map that stores all the bindings. */
	private Map<String, UBinding> bindings;

	/** The empty default constructor for the UBindingSet object. */
	public UBindingSet() {
		super("");
		this.bindings = new HashMap<String, UBinding>();
	}

	/**
	 * The constructor that initializes the UBindingSet object.
	 * 
	 * @param size
	 *          the initial estimated size for the internal map of bindings.
	 * */
	public UBindingSet(int size) {
		super();
		this.bindings = new HashMap<String, UBinding>(size);
	}

	/**
	 * The constructor that initializes the UBindingSet object.
	 * 
	 * @param bind
	 *          a UBindingSet object to initialize this UBindingSet instance with.
	 * */
	public UBindingSet(UBindingSet bind) {
	  this.init(bind);
	}

	/**
	 * Initializes the UBindingSet object.
	 * 
	 * @param size
	 *          the initial estimated size for the internal map of bindings.
	 * */
	public void init(int size) {
		this.bindings = new HashMap<String, UBinding>(size);
	}

	/**
	 * Initializes the UBindingSet object.
	 * 
	 * @param bind
	 *          a UBindingSet object to initialize this UBindingSet instance with.
	 * */
	public void init(UBindingSet bind) {
		this.init(bind.size());
		this.addAll(bind);
	}

	/**
	 * Adds a given binding to the internal map of bindings.
	 * 
	 * @param bind
	 *          the binding to add.
	 * */
	public void add(UBinding bind) {
		this.bindings.put(bind.getArgument().getName(), bind);
	}

	/**
	 * Adds all the given bindings of the UBindingSet object to the internal map of bindings.
	 * 
	 * @param bind
	 *          the bindings (of the given UBindingSet) to add.
	 * */
	public void addAll(UBindingSet bind) {
		for (UBinding binding : bind)
			this.add(binding);
	}

	/**
	 * Delivers the binding to the specified argument, if one exists.
	 * 
	 * @param argument
	 *          the argument whose binding is of interest.
	 * @return the binding object or null, if no binding for the given argument exists.
	 * */
	public UArgument getBinding(UArgument argument) {
		UBinding binding = bindings.get(argument.getName());
		if (binding != null)
			return binding.getBinding();
		return null;
	}

	// may be expensive
	/**
	 * Delivers all the arguments that exist for the specified binding.
	 * 
	 * @param binding
	 *          the binding whose bounded arguments are of interest.
	 * @return the set of bounded arguments (possibly length 0, if no bounded argument exists so far).
	 * */
	public Set<UArgument> getArguments(UArgument binding) {
		Set<UArgument> arguments = new HashSet<UArgument>();
		for (UBinding b : bindings.values()) {
			if (b.getBinding().equals(binding))
				arguments.add(b.getArgument());
		}
		return arguments;
	}

	/**
	 * Delivers an iterator to access the internal bindings list.
	 * 
	 * @return the iterator for the internal bindings list.
	 * */
	public Iterator<UBinding> iterator() {
		return bindings.values().iterator();
	}

	/**
	 * Delivers the size of the internal map of bindings (number of bindings so far).
	 * 
	 * @return the number of bindings so far.
	 * */
	public int size() {
		return bindings.size();
	}

	/**
	 * Delivers a string representation of the UBindingSet object.
	 * 
	 * @return the string representation of the binding-set.
	 */
	@Override
	public String toString() {
		return bindings.toString();
	}

	/**
	 * Reads in the serialized data from the mapped UBindingSet ActionScript class.
	 * 
	 * @param input
	 *          the serialized input data.
	 * */
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		super.readExternal(input);
		bindings = (HashMap<String, UBinding>) input.readObject();
	}

	/**
	 * Writes out the data to serialize to the mapped UBindingSet ActionScript class.
	 * 
	 * @param output
	 *          the output data to serialize.
	 * */
	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		// try {
		super.writeExternal(output);
		// System.out.println("hashmap size = " + this.bindings.size());
		// }
		// catch (IOException e) {System.out.println("Error is : " + e);System.exit(0);}
		output.writeObject(bindings);
	}

}
