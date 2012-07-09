/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents a literal object which is used for several reasoner related data structures including soft rules, hard rules, queries and so on.
 * 
 * @author Timm Meiser
 * @since 18.01.10
 * @version 1.0
 */
public class ULiteral extends UTriplet implements Comparable<ULiteral> {

	private int compareValue = 1;

	/** The constructor that initializes the ULiteral object. */
	public ULiteral() {
		super();
	}

	/**
	 * The constructor that initializes the ULiteral object.
	 * 
	 * @param relation
	 *          the used relation.
	 * @param firstArgument
	 *          the name of the first argument of the ULiteral object.
	 * @param secondArgument
	 *          the name of the second argument of the ULiteral object.
	 * @throws Exception
	 * */
	public ULiteral(URelation relation, String firstArgument, String secondArgument) throws Exception {
		super(relation, new UArgument(firstArgument), new UArgument(secondArgument));
	}

	/**
	 * The constructor that initializes the ULiteral object.
	 * <p>
	 * The allowed parameters are:
	 * <p>
	 * For the relation reference: An URelation reference.
	 * <p>
	 * For the first argument reference: An UEntity reference.
	 * <p>
	 * For the second argument reference: An UEntity reference.
	 * 
	 * @param relation
	 *          the used relation.
	 * @param firstArgument
	 *          the first argument of the ULiteral object.
	 * @param secondArgument
	 *          the second argument of the ULiteral object.
	 * @throws Exception
	 * */
	public ULiteral(URelation relation, UArgument firstArgument, UArgument secondArgument) throws Exception {
		super(relation, firstArgument, secondArgument);
	}

	/**
	 * The constructor that initializes the ULiteral object.
	 * 
	 * @param relation
	 *          the used relation.
	 * @param firstArgument
	 *          the name of the first argument of the ULiteral object.
	 * @param secondArgument
	 *          the name of the second argument of the ULiteral object.
	 * @param compareValue
	 *          a (constant) compare value for arithmetic predicates
	 * @throws Exception
	 * */
	public ULiteral(URelation relation, String firstArgument, String secondArgument, int compareValue) throws Exception {
		super(relation, new UArgument(firstArgument), new UArgument(secondArgument));
		this.compareValue = compareValue;
	}

	/**
	 * The constructor that initializes the ULiteral object.
	 * <p>
	 * The allowed parameters are:
	 * <p>
	 * For the relation reference: An URelation reference.
	 * <p>
	 * For the first argument reference: An UEntity reference.
	 * <p>
	 * For the second argument reference: An UEntity reference.
	 * 
	 * @param relation
	 *          the used relation.
	 * @param firstArgument
	 *          the first argument of the ULiteral object.
	 * @param secondArgument
	 *          the second argument of the ULiteral object.
	 * @param compareValue
	 *          a (constant) compare value for arithmetic predicates
	 * @throws Exception
	 * */
	public ULiteral(URelation relation, UArgument firstArgument, UArgument secondArgument, int compareValue) throws Exception {
		super(relation, firstArgument, secondArgument);
		this.compareValue = compareValue;
	}

	/**
	 * Initializes the ULiteral object.
	 * <p>
	 * The allowed parameters are:
	 * <p>
	 * For the relation reference: An URelation reference.
	 * <p>
	 * For the first argument reference: An UEntity reference.
	 * <p>
	 * For the second argument reference: An UEntity reference.
	 * 
	 * @param relation
	 *          the used relation.
	 * @param firstArgument
	 *          the first argument of the ULiteral object.
	 * @param secondArgument
	 *          the second argument of the ULiteral object.
	 * @throws Exception
	 */
	public void init(URelation relation, UArgument firstArgument, UArgument secondArgument) throws Exception {
		super.init(relation, firstArgument, secondArgument);
	}

	/**
	 * Initializes the ULiteral object.
	 * 
	 * @param relation
	 *          the used relation.
	 * @param firstArgument
	 *          the name of the first argument of the ULiteral object.
	 * @param secondArgument
	 *          the name of the second argument of the ULiteral object.
	 * @throws Exception
	 * */
	public void init(URelation relation, String firstArgument, String secondArgument) throws Exception {
		super.init(relation, new UEntity(firstArgument), new UEntity(secondArgument));
	}

	/**
	 * Compares this ULiteral instance to a given one.
	 * 
	 * @param literal
	 *          the literal this ULiteral instance should be compared to.
	 * @return 0 for equality, -1 for smaller, 1 for bigger (usually)
	 */
	public int compareTo(ULiteral literal) {
		return Double.compare(this.getSelectivity(), literal.getSelectivity());
	}

	public int getCompareValue() {
		return compareValue;
	}

	/**
	 * Reads in the serialized data from the mapped ULiteral ActionScript class.
	 * 
	 * @param input
	 *          the serialized input data.
	 * */
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		super.readExternal(input);
		this.compareValue = input.readInt();
	}

	/**
	 * Writes out the data to serialize to the mapped ULiteral ActionScript class.
	 * 
	 * @param output
	 *          the output data to serialize.
	 * */
	public void writeExternal(ObjectOutput output) throws IOException {
		super.writeExternal(output);
		output.writeInt(this.compareValue);
	}

	/**
	 * Returns a string representation of the ULiteral object.
	 * 
	 * @return the string representation of the ULiteral object.
	 */
	@Override
	public String toString() {
		return ("" + this.getRelationName() + "(" + this.getFirstArgumentName() + ", " + this.getSecondArgumentName() + ", " + this.getCompareValue() + ")");
	}
}
