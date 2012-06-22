/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A URule object represents the general rule containing one or more literals (atoms).
 * <p>
 * It`s sub-classes UQuery, USoftRule and UHardRule extend the given functionality to deliver necessary extra expressiveness. Most of the required functionality
 * regarding queries and rules are already implemented in this base class.
 * 
 * @author Timm Meiser
 * @since 07.01.10
 * @version 1.0
 * 
 */
public class URule extends UObject implements Iterable<ULiteral> {

  /** The free variables within the list of literals. */
  private Set<UArgument> variables;

  /** The constants within the list of literals. */
  private Set<UArgument> constants;

  /** Represents the list of literals. */
  private List<ULiteral> literals;

  /** The empty default constructor for the URule object. */
  public URule() {
    super();
  }

  /**
   * The constructor that initializes the URule object.
   * 
   * @param name
   *          the name for the URule object.
   * */
  public URule(String name) throws Exception {
    super(name);
    this.init(name, null);
  }

  /**
   * The constructor that initializes the URule object.
   * 
   * @param name
   *          the name for the URule object.
   * @param literals
   *          the the list of literals that are used within the URule object.
   * @throws Exception
   * */
  public URule(String name, List<ULiteral> literals) throws Exception {
    super(name);
    this.init(name, literals);
  }

  /**
   * Initializes the URule object.
   * 
   * @param name
   *          the name for the URule object.
   * @param lit
   *          the the list of literals that are used within the URule object.
   * @throws Exception
   */
  public void init(String name, List<ULiteral> lit) throws Exception {
    super.init(name);
    this.literals = new ArrayList<ULiteral>();
    this.variables = new HashSet<UArgument>();
    this.constants = new HashSet<UArgument>();
    if (lit != null)
      this.addAll(lit);
  }

  /**
   * Delivers the list of literals within the URule object.
   * 
   * @return the complete list of literals within the URule object.
   */
  public List<ULiteral> getLiterals() {
    return literals;
  }

  /**
   * Overrides the internal list of literals.
   * 
   * @param literals
   *          the list of literals to replace the previous internal list.
   */
  public void setLiterals(List<ULiteral> literals) {
    this.literals = literals;
  }

  /**
   * Adds a literal to the internal list of literals.
   * 
   * @param literal
   *          the literal to add to the list of currently stored literals.
   */
  public void addLiteral(ULiteral literal) {
    this.literals.add(literal);
    if (literal.getFirstArgument().isVariable())
      variables.add(literal.getFirstArgument());
    else
      constants.add(literal.getFirstArgument());
    if (literal.getSecondArgument().isVariable())
      variables.add(literal.getSecondArgument());
    else
      constants.add(literal.getSecondArgument());
    if (literal.getRelation().isVariable())
      variables.add(literal.getRelation());
    else
      constants.add(literal.getRelation());
  }

  /**
   * Adds all literals of the given list of literals to the internal list of literals.
   * 
   * @param literals
   *          the list of literals to add to the list of currently stored literals.
   * @throws Exception
   */
  public void addAll(List<ULiteral> lit) throws Exception {
    for (ULiteral literal : lit)
      this.addLiteral(literal);
  }

  /**
   * Swaps the literals at the two specified positions within the internal list of literals.
   * 
   * @param fromIdx
   *          the index of the first literal to swap.
   * @param toIdx
   *          the index of the second literal to swap.
   * @throws Exception
   * */
  public void swap(int fromIdx, int toIdx) throws Exception {
    ULiteral tmp = literals.get(toIdx);
    literals.set(toIdx, literals.get(fromIdx));
    literals.set(fromIdx, tmp);
  }

  /**
   * Adds a literal to the list of internally stored literals by building a new ULiteral object from the given parameter values.
   * 
   * @param relation
   *          the relation for the new literal that will be added to the internal list of literals.
   * @param arg1
   *          the first argument of the new literal.
   * @param arg2
   *          the second argument of the new literal.
   * @throws Exception
   */
  public void addLiteral(URelation relation, UArgument arg1, UArgument arg2) throws Exception {
    ULiteral literal = new ULiteral();
    literal.init(relation, arg1, arg2);
    this.addLiteral(literal);
  }

  /**
   * Deletes the literal at position <i> position </i> from the list of internally stored literals.
   * 
   * @param position
   *          the position of the literal to remove.
   * @return true, if the literal could be removed, false otherwise.
   */
  public boolean removeLiteralAtPosition(int position) {
    if (position < 0 | position >= this.size())
      return false;

    ULiteral literal = this.literals.get(position);
    this.literals.remove(position);

    if (literal.getFirstArgument().isVariable())
      this.variables.remove(literal.getFirstArgument());
    else
      this.constants.remove(literal.getFirstArgument());

    if (literal.getSecondArgument().isVariable())
      this.variables.remove(literal.getSecondArgument());
    else
      this.constants.remove(literal.getSecondArgument());

    if (literal.getRelation().isVariable())
      this.variables.remove(literal.getRelationName());
    else
      this.constants.remove(literal.getRelationName());

    return true;

  }

  /**
   * Deletes the given literal from the internally stored list of literals, in case the literal exists.
   * 
   * @param literal
   *          the literal to remove.
   * @return true, if the literal could be removed, false otherwise.
   */
  public boolean removeLiteral(ULiteral literal) {
    int length = this.size();

    for (int i = 0; i < length; i++) {

      if (this.literals.get(i).equals(literal)) {

        this.literals.remove(i);

        if (literal.getFirstArgument().isVariable())
          this.variables.remove(literal.getFirstArgument());
        else
          this.constants.remove(literal.getFirstArgument());

        if (literal.getSecondArgument().isVariable())
          this.variables.remove(literal.getSecondArgument());
        else
          this.constants.remove(literal.getSecondArgument());

        if (literal.getRelation().isVariable())
          this.variables.remove(literal.getRelationName());
        else
          this.constants.remove(literal.getRelationName());

        return true;
      }

    }

    return false;
  }

  /**
   * Retrieves a literal from the specified position in the list of literals.
   * 
   * @param position
   *          the position in the list where to look for the literal to return.
   * @return the literal from the requested position.
   */
  public ULiteral getLiteral(int position) {
    if (position < 0 | position >= this.size())
      return null;
    return this.literals.get(position);
  }

  /**
   * Returns the string representation of the whole ULiteralSet object (all values within the object).
   * 
   * @return the string representation of the object.
   */
  @Override
  public String toString() {
    return literals.toString();
  }

  /**
   * Returns the number of free variables in this URule object.
   * 
   * @return the number of free variables.
   */
  public Set<UArgument> getVariables() {
    return this.variables;
  }

  /**
   * Returns the number of constants in this URule object.
   * 
   * @return the number of constants.
   */
  public Set<UArgument> getConstants() {
    return this.constants;
  }

  /**
   * Checks whether this rule equals the given object or not.
   * 
   * @return true, if this rule equals the given object, false otherwise.
   * */
  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof URule)) {
      return false;
    }

    return this.hashCode() == obj.hashCode();
  }

  /**
   * Returns the number of internally stored literals in this URule object.
   * 
   * @return the number of stored literals.
   */
  public int size() {
    return this.literals.size();
  }

  /**
   * Delivers an iterator to access the internal literals (only for the body of a rule).
   * 
   * @return the iterator for the internal literals.
   * */
  public Iterator<ULiteral> iterator() {
    return literals.iterator();
  }

  /**
   * Reads in the serialized data from the mapped URule ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
    ArrayList<ULiteral> lits = (ArrayList<ULiteral>) input.readObject();
    for (ULiteral lit : lits)
      this.addLiteral(lit);
  }

  /**
   * Writes out the data to serialize to the mapped URule ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeObject(literals);
  }

  protected boolean checkChaining(HashSet<ULiteral> lit, int idx) {
    if (lit.size() == this.size())
      return true;

    boolean matched = false;
    for (int i = 0; i < this.size(); i++) {
      if (!lit.contains(this.getLiteral(i))
          && (this.getLiteral(i).getFirstArgumentName().equals(this.getLiteral(idx).getFirstArgumentName())
              || this.getLiteral(i).getFirstArgumentName().equals(this.getLiteral(idx).getSecondArgumentName())
              || this.getLiteral(i).getSecondArgumentName().equals(this.getLiteral(idx).getFirstArgumentName()) || this.getLiteral(i).getSecondArgumentName()
              .equals(this.getLiteral(idx).getSecondArgumentName()))) {
        lit.add(this.getLiteral(i));
        matched = matched || checkChaining(lit, i);
      }
      if (matched)
        break;
    }

    return matched;
  }
}
