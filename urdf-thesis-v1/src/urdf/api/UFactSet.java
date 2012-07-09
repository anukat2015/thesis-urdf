/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cern.colt.map.OpenIntObjectHashMap;

/**
 * This class represents a set of facts and is used as a super class for grounded hard-rules (UGroundedHardRule instances) and grounded soft-rules
 * (UGroundedSoftRule instances).
 * <p>
 * Internally, there are two main data structures (an integer hash map and a list of facts) used to store facts which belong to this signed fact set and were
 * produced by a particular (also internally stored) soft rule. Further, several methods are provides by this class to access, deliver and delete particular
 * facts (from the internal storage).
 * 
 * 
 * @author Timm Meiser
 * @since 08.12.09
 * @version 1.0
 */

public class UFactSet extends UObject implements Iterable<UFact> {

  /**
   * This hash map maps the fact id`s (integer values) to facts for faster access to (checking of) the internally stored facts. The map is from the open source
   * package <i> cern.colt.map.OpenIntObjectHashMap </i>
   */
  private OpenIntObjectHashMap factIDMap;

  /** This list stores all the signed fact objects (references). */
  private List<UFact> factSet;

  /** The empty default constructor for the UFactSet object. */
  public UFactSet() {
    super();
    this.factIDMap = new OpenIntObjectHashMap();
    this.factSet = new ArrayList<UFact>();
  }

  /** The constructor for a variable list of facts. */
  public UFactSet(UFact... facts) {
    super();
    this.factIDMap = new OpenIntObjectHashMap();
    this.factSet = new ArrayList<UFact>();
    for (UFact fact : facts)
      this.add(fact);
  }

  /**
   * The constructor that initializes the UFactSet object.
   * 
   * @param size
   *          the predefined size for the internal fact list and map of this fact-set (UFactSet object).
   */
  public UFactSet(int size) {
    super();
    this.factIDMap = new OpenIntObjectHashMap(size);
    this.factSet = new ArrayList<UFact>(size);
  }

  /**
   * The constructor that initializes the UFactSet object.
   * 
   * @param factSet
   *          the set of facts that is used to initialize this fact-set (UFactSet object).
   */
  public UFactSet(UFactSet factSet) {
    this(factSet.size());
    this.addAll(factSet);
  }

  /**
   * The constructor that initializes the UFactSet object.
   * 
   * @param fact
   *          the fact that is added to the internal list of facts and that is used to initialize this fact-set (UFactSet object).
   */
  public UFactSet(UFact fact) {
    this(1);
    this.add(fact);
  }

  /**
   * Initializes the UFactSet object.
   * <p>
   * The numerical value specifies the initial size for the internally used fact list and map.
   * 
   * @param size
   *          the predefined size for the internal fact list and map of this fact-set (UFactSet object).
   * */
  public void init(int size) {
    this.factIDMap = new OpenIntObjectHashMap(size);
    this.factSet = new ArrayList<UFact>(size);
  }

  /**
   * Initializes the UFactSet object.
   * 
   * @param fact
   *          the fact that is added to the internal list of facts and that is used to initialize this fact-set (UFactSet object).
   */
  public void init(UFact fact) {
    this.init(1);
    this.add(fact);
  }

  /**
   * Initializes the UFactSet object.
   * 
   * @param factSet
   *          the set of facts that is used to initialize this fact-set (UFactSet object).
   */
  public void init(UFactSet facts) {
    this.init(facts.size());
    for (UFact fact : facts)
      this.add(fact);
  }

  /**
   * Adds the given fact to the internal list of stored facts.
   * 
   * @param fact
   *          the given fact to add to the internal list of facts.
   * @return true, if the facts could be added, false otherwise.
   * */
  public boolean add(UFact fact) {
    if (factIDMap.put(fact.hashCode(), fact)) {
      factSet.add(fact);
      return true;
    }
    return false;
  }

  /**
   * Adds all the given facts to the internal list of stored facts at once.
   * <p>
   * Uses the internal method <i> boolean add(UFact fact) </i>. There is no guarantee that all facts of the fact set will be added.
   * 
   * @param factSet
   *          the given set of facts to add to the internal list of facts.
   * */
  public void addAll(UFactSet facts) {
    if (facts != null)
      for (UFact fact : facts)
        this.add(fact);
  }

  /**
   * Adds all the given facts to the internal list of stored facts at once.
   * <p>
   * Uses the internal method <i> boolean add(UFact fact) </i>. There is no guarantee that all facts of the fact set will be added.
   * 
   * @param factList
   *          the given list of facts to add to the internal list of facts.
   * */
  public void addAll(List<UFact> factList) {
    if (factList != null)
      for (UFact fact : factList)
        this.add(fact);
  }

  /**
   * Removes the given fact from the internal list of stored facts.
   * 
   * @param fact
   *          the fact to remove from the internal list of stored facts.
   * @return true, if the fact could be removed, false otherwise.
   * */
  public boolean removeFact(UFact fact) {
    if (factIDMap.removeKey(fact.hashCode())) {
      factSet.remove(fact);
      return true;
    }
    return false;
  }

  /**
   * Checks whether this fact set contains all facts of the given fact-set.
   * 
   * @param factSet
   *          the fact set whose facts should be checked for membership in the internal list of facts.
   * @return true, if the internal list of facts contains all of the facts of the given fact-set, false otherwise.
   * */
  public boolean containsAll(UFactSet facts) {
    for (UFact fact : facts)
      if (!this.contains(fact))
        return false;
    return true;
  }

  /**
   * Checks whether this fact set contains any of the facts of the given fact set.
   * 
   * @param factSet
   *          the fact-set whose facts should be checked for membership in the internal list of facts.
   * @return true, if the internal list of facts contains any of the facts of the given fact-set, false otherwise.
   * */
  public boolean containsAny(UFactSet facts) {
    for (UFact fact : facts)
      if (this.contains(fact))
        return true;
    return false;
  }

  /**
   * Delivers the internally stored fact that maps to the specified one.
   * <p>
   * This method acts as a lookup where it is checked, if a certain fact has already existed before. Then, this object is returned so that the caller of this
   * method can continue grounding the query with the original first instantiated UFact object instead of the equal one that has been created recently.
   * 
   * @param fact
   *          the specified fact whose internally mapped fact (the original first created object that represents the given fact) should be delivered.
   * @return the mapped fact.
   * */
  public UFact lookup(UFact fact) {
    return (UFact) factIDMap.get(fact.hashCode());
  }

  /**
   * Delivers all the facts (the list of facts) contained in this fact-set.
   * 
   * @return the list of facts.
   */
  public List<UFact> getFactSet() {
    return factSet;
  }

  /**
   * Checks whether the specified fact has already been stored internally.
   * 
   * @param fact
   *          the fact to check for existence in the internal fact-set.
   * @return true, if the specified fact (UFact object) already exists internally, false otherwise.
   * */
  public boolean contains(UFact fact) {
    return factIDMap.containsKey(fact.hashCode());
  }

  /**
   * Delivers an iterator to access the internal fact set.
   * 
   * @return the iterator for the internal fact set.
   * */
  public Iterator<UFact> iterator() {
    return factSet.iterator();
  }

  /**
   * Delivers the length of the internal fact list (number of stored facts so far).
   * 
   * @return the number of currently stored facts.
   * */
  public int size() {
    return factIDMap.size();
  }

  /**
   * Sorts the internal list of facts.
   * 
   * @param descending
   *          the flag that specifies if the sorting should be done in an ascending or a descending order.
   * */
  public void sort(boolean ascending) {
    Collections.sort(factSet);
    if (ascending)
      Collections.reverse(factSet);
  }

  /**
   * Delivers the UFact object that is stored at the specified position within the internal list of facts (UFact objects).
   * 
   * */
  public UFact getFact(int position) {
    return factSet.get(position);
  }

  /** Clears the internally used data structures. */
  public void clear() {
    factIDMap.clear();
    factSet.clear();
  }

  /**
   * Prints important information about the internally stored data.
   * 
   * @return the string representation of the internally stored data.
   * */
  public String toString() {
    return factSet.toString();
  }

  /**
   * Checks if the specified UFactSet object is equal to this UFactSet instance.
   * 
   * @param arg
   *          the UFactSet object to check for equality.
   * @return true, if the specified UFactSet object is equal to this UFactSet instance.
   * 
   */
  public boolean equals(UFactSet arg) {
    return this.size() == arg.size() && this.containsAll(arg) && arg.containsAll(this);
  }

  /**
   * Checks if the specified object is equal to this UFactSet instance.
   * 
   * @param obj
   *          the object to check for equality.
   * @return true, if the specified object is equal to this UFactSet instance.
   * 
   */
  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof UFactSet)) {
      return false;
    }

    UFactSet other = (UFactSet) obj;

    return this.size() == other.size() && this.containsAll(other) && other.containsAll(this);
  }

  /**
   * Reads in the serialized data from the mapped UArgument ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
    ArrayList<UFact> list = (ArrayList<UFact>) input.readObject();
    this.addAll(list);
  }

  /**
   * Writes out the data to serialize to the mapped UArgument ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeObject(factSet);
  }

}
