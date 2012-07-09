/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a domain object which specifies the domain or the range of a relation.
 * 
 * @author Timm Meiser
 * @since 07.01.10
 * @version 1.0
 */
public class UDomain extends UObject {

  /** The list of used UDomain objects within the reasoning engine. */
  private static Map<String, UDomain> domains = new HashMap<String, UDomain>();

  public static final UDomain DEFAULT_DOMAIN = new UDomain("DEFAULT_DOMAIN");

  // top-level
  public static final UDomain ENTITY = new UDomain("ENTITY");
  public static final UDomain LITERAL = new UDomain("LITERAL");
  public static final UDomain LANGUAGE = new UDomain("LANGUAGE");

  // Reasoning about facts and relations, to be refined
  public static final UDomain FACT = new UDomain("FACT");
  public static final UDomain RELATION = new UDomain("RELATION");
  public static final UDomain CLASS = new UDomain("CLASS", RELATION);

  // entities
  public static final UDomain GEOPOLITICAL = new UDomain("GEOPOLITICAL", ENTITY);
  public static final UDomain AREA = new UDomain("AREA", LITERAL);
  public static final UDomain DENSITYPERAREA = new UDomain("DENSITYPERAREA", LITERAL);
  public static final UDomain REGION = new UDomain("REGION", GEOPOLITICAL);
  public static final UDomain CITY = new UDomain("CITY", REGION);
  public static final UDomain COUNTRY = new UDomain("Country", REGION);
  public static final UDomain LEGALINSTITUTION = new UDomain("LEGALINSTITUTION", GEOPOLITICAL);
  public static final UDomain UNIVERSITY = new UDomain("UNIVERSITY", LEGALINSTITUTION);
  public static final UDomain MOVIE = new UDomain("MOVIE", ENTITY);
  public static final UDomain BOOK = new UDomain("BOOK", ENTITY);
  public static final UDomain COMPUTERSYSTEM = new UDomain("COMPUTERSYSTEM", ENTITY);

  // people
  public static final UDomain PERSON = new UDomain("PERSON", ENTITY);
  public static final UDomain LEGALACTOR = new UDomain("LEGALACTOR", ENTITY);
  public static final UDomain LEGALACTORGEO = new UDomain("LEGALACTORGEO", ENTITY);
  public static final UDomain ACTOR = new UDomain("ACTOR", PERSON);
  public static final UDomain POLITICIAN = new UDomain("POLITICIAN", PERSON);

  // literals
  public static final UDomain QUANTITY = new UDomain("QUANTITY", LITERAL);
  public static final UDomain UNIT = new UDomain("UNIT", LITERAL);
  public static final UDomain DATE = new UDomain("DATE", LITERAL);
  public static final UDomain TIMEINTERVAL = new UDomain("TIMEINTERVAL", LITERAL);
  public static final UDomain NUMBER = new UDomain("NUMBER", LITERAL);
  public static final UDomain DURATION = new UDomain("DURATION", LITERAL);
  public static final UDomain INTEGER = new UDomain("INTEGER", NUMBER);
  public static final UDomain NONNEGATIVEINTEGER = new UDomain("NONNEGATIVEINTEGER", INTEGER);
  public static final UDomain URL = new UDomain("URL", LITERAL);
  public static final UDomain IDENTIFIER = new UDomain("IDENTIFIER", LITERAL);
  public static final UDomain STRING = new UDomain("STRING", LITERAL);
  public static final UDomain MONETARYVALUE = new UDomain("MONETARYVALUE", LITERAL);
  public static final UDomain PRIZE = new UDomain("PRIZE", MONETARYVALUE);

  // language
  public static final UDomain WORD = new UDomain("WORD", LANGUAGE);

  /** The super domain of this UDomain object. */
  private UDomain superDomain;

  /** The constructor of the UDomain object. */
  public UDomain() {
    super();
  }

  /**
   * The constructor of the UDomain object.
   * 
   * @param name
   *          the name for the UDomain object.
   * */
  public UDomain(String name) {
    this(name, null);
  }

  /**
   * The constructor of the UDomain object.
   * 
   * @param name
   *          the name for the UDomain object.
   * @param superDomain
   *          the super-domain for the UDomain object.
   * */
  public UDomain(String name, UDomain superDomain) {
    this.init(name, superDomain);
  }

  /**
   * Delivers the UDomain object to the given name of a requested domain.
   * <p>
   * In case the requested domain does not exist, a new UDomain object with the given name is built.
   * 
   * @param name
   *          the name of the requested UDomain object.
   * @return the requested UDomain object or a new UDomain object.
   * */
  public static UDomain valueOf(String name) {
    UDomain domain = domains.get(name);
    if (domain == null) {
      domain = new UDomain();
      domain.init(name, null);
    }
    return domain;
  }

  /**
   * Delivers the UDomain object to the given name of a requested domain.
   * <p>
   * In case the requested domain does not exist, a UDomain object with the given parameter is built.
   * 
   * @param name
   *          the name for the UDomain object.
   * @param superDomain
   *          the super-domain for the UDomain object.
   * @return the requested UDomain object or the recently built one.
   * */
  public static UDomain valueOf(String name, UDomain superDomain) {
    UDomain domain = domains.get(name);
    if (domain == null) {
      domain = new UDomain();// (name);
      domain.init(name, superDomain);
    }
    return domain;
  }

  /**
   * Initializes the UDomain object by setting the necessary attribute values.
   * 
   * @param name
   *          the domain name for the UDomain object.
   * */
  public void init(String name) {
    super.init(name);
    this.superDomain = null;
  }

  /**
   * Initializes the UDomain object by setting the necessary attribute values.
   * 
   * @param name
   *          the domain name for the UDomain object.
   * @param superDom
   *          the super domain for this UDomain object.
   * */
  public void init(String name, UDomain superDom) {
    super.init(name);
    this.superDomain = superDom;
  }

  /**
   * Delivers the super-domain of this UDomain object.
   * 
   * @return the super-domain of the UDomain object.
   */
  public UDomain getSuperDomain() {
    return superDomain;
  }

  /**
   * Sets the super-domain for this UDomain object.
   * 
   * @param superDom
   *          the super-domain for the UDomain object.
   */
  public void setSuperDomain(UDomain superDom) {
    this.superDomain = superDom;
  }

  /**
   * Checks whether this UDomain object is a sub-domain of the given UDomain object.
   * 
   * @param dom
   *          the UDomain object to check for being a super-domain.
   * @return true, if this UDomain object is a sub-domain of the given UDomain object, false otherwise.
   * */
  public boolean isSubDomainOf(UDomain dom) {
    return this.superDomain == null || dom == null || (this.superDomain.equals(dom) || this.superDomain.isSubDomainOf(dom));
  }

  /**
   * Checks whether this UDomain object is a super-domain of the given UDomain object.
   * 
   * @param dom
   *          the UDomain object to check for being a sub-domain.
   * @return true, if this UDomain object is a super-domain of the given UDomain object, false otherwise.
   * */
  public boolean isSuperDomainOf(UDomain dom) {
    return dom.superDomain == null || (dom.superDomain.equals(this) || dom.superDomain.isSuperDomainOf(this));
  }

  /**
   * Returns a string representation of the UDomain object.
   * 
   * @return the string representation of the UDomain object.
   */
  @Override
  public String toString() {
    return "Domain : " + super.getName() + " , SuperDomain : " + (this.superDomain == null ? "null" : this.superDomain.getName());
  }

  /**
   * Reads in the serialized data from the mapped UDomain ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
    String dom = input.readUTF();
    if (dom.equals("0"))
      return;
    if (dom.startsWith("?")) {
      superDomain = new UDomain(dom);
    } else
      superDomain = valueOf(dom); // return the suitable static URelation object
  }

  /**
   * Writes out the data to serialize to the mapped UDomain ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    if (superDomain == null)
      output.writeUTF("0");
    else
      output.writeUTF(superDomain.getName()); // only write the name of the UDomain object
  }

}
