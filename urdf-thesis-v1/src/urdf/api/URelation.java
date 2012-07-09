/**
 * 
 */
package urdf.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import urdf.reasoner.function.FunctionCall;

/**
 * Acts as a function library and delivers generally required functionality that can be used globally.
 * 
 * @author Timm Meiser
 * @since 06.01.10
 * @version 1.0
 */
public class URelation extends UArgument {

  /** The list of used URelation objects within the reasoning engine. */
  private static Map<String, URelation> relations = new HashMap<String, URelation>();

  // Generic and arithmetic predicates
  public static final URelation DUMMY = new URelation("dummy", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
  public static final URelation EQUALS = new URelation("equals", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
  public static final URelation NOTEQUALS = new URelation("notEquals", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
  public static final URelation GREATER = new URelation("greater", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
  public static final URelation GREATEREQ = new URelation("greaterEq", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
  public static final URelation LOWER = new URelation("lower", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
  public static final URelation LOWEREQ = new URelation("lowerEq", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
  public static final URelation DIFFERENCELT = new URelation("difference", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
  public static final URelation YEARBEFORE = new URelation("yearBefore", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
  public static final URelation DATEBEFORE = new URelation("dateBefore", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);

  // Temporal predicate(s)
  public static final URelation ISWITHINHOURS = new URelation("isWithinHours", UDomain.DATE, UDomain.DATE, false, false, true, false);
  public static final URelation ISWITHINMINUTES = new URelation("isWithinMinutes", UDomain.DATE, UDomain.DATE, false, false, true, false);
  public static final URelation SAMEDAY = new URelation("sameDay", UDomain.DATE, UDomain.DATE, false, false, true, false);

  // Yago template facts
  public static final URelation BORNONDATE = new URelation("bornOnDate", UDomain.PERSON, UDomain.DATE, false, true, false, false);
  public static final URelation BORNIN = new URelation("bornIn", UDomain.PERSON, UDomain.CITY, false, true, false, false);
  public static final URelation ORIGIN = new URelation("originatesFrom", UDomain.PERSON, UDomain.REGION, false, true, false, false);
  public static final URelation DIEDONDATE = new URelation("diedOnDate", UDomain.PERSON, UDomain.DATE, false, true, false, false);
  public static final URelation DIEDIN = new URelation("diedIn", UDomain.PERSON, UDomain.CITY, false, true, false, false);
  public static final URelation LIVESIN = new URelation("livesIn", UDomain.PERSON, UDomain.REGION, false, true, false, false);
  public static final URelation ACADEMICADVISOR = new URelation("hasAcademicAdvisor", UDomain.PERSON, UDomain.PERSON, false, true, false, false);
  public static final URelation ALMAMATER = new URelation("graduatedFrom", UDomain.PERSON, UDomain.UNIVERSITY, false, true, false, false);
  public static final URelation WORKSAT = new URelation("worksAt", UDomain.PERSON, UDomain.LEGALINSTITUTION, false, true, false, false);
  public static final URelation NATIVENAME = new URelation("isNativeNameOf", UDomain.WORD, UDomain.GEOPOLITICAL, false, true, false, false);
  public static final URelation LEADER = new URelation("isLeaderOf", UDomain.PERSON, UDomain.LEGALACTORGEO, false, false, false, false);
  public static final URelation AREA = new URelation("hasArea", UDomain.REGION, UDomain.AREA, false, true, false, false);
  public static final URelation POPULATION = new URelation("hasPopulation", UDomain.REGION, UDomain.NONNEGATIVEINTEGER, false, true, false, false);
  public static final URelation POPULATIONDENSITY = new URelation("hasPopulationDensity", UDomain.REGION, UDomain.DENSITYPERAREA, false, true, false, false);
  public static final URelation UTCOFFSET = new URelation("hasUTCOffset", UDomain.REGION, UDomain.INTEGER, false, true, false, false);
  public static final URelation WEBSITE = new URelation("hasWebsite", UDomain.LEGALACTORGEO, UDomain.URL, false, false, false, false);
  public static final URelation ORDER = new URelation("isNumber", UDomain.PERSON, UDomain.NONNEGATIVEINTEGER, false, true, false, false);
  public static final URelation PREDECESSOR = new URelation("hasPredecessor", UDomain.PERSON, UDomain.PERSON, false, true, false, false);
  public static final URelation SUCCESSOR = new URelation("hasSuccessor", UDomain.PERSON, UDomain.PERSON, false, true, false, false);
  public static final URelation MARRIEDTO = new URelation("isMarriedTo", UDomain.PERSON, UDomain.PERSON, false, true, false, false);
  public static final URelation AFFILIATEDTO = new URelation("isAffiliatedTo", UDomain.LEGALACTOR, UDomain.LEGALACTOR, false, false, false, false);
  public static final URelation INFLUENCED = new URelation("influences", UDomain.PERSON, UDomain.PERSON, false, false, false, false);
  public static final URelation DIRECTED = new URelation("directed", UDomain.PERSON, UDomain.MOVIE, false, false, false, false);
  public static final URelation PRODUCED = new URelation("produced", UDomain.LEGALACTOR, UDomain.MOVIE, false, false, false, false);
  public static final URelation EDITED = new URelation("edited", UDomain.PERSON, UDomain.MOVIE, false, false, false, false);
  public static final URelation ACTEDIN = new URelation("actedIn", UDomain.ACTOR, UDomain.MOVIE, false, false, false, false);
  public static final URelation PUBLISHEDONDATE = new URelation("publishedOnDate", UDomain.ENTITY, UDomain.DATE, false, true, false, false);
  public static final URelation DURATION = new URelation("hasDuration", UDomain.ENTITY, UDomain.DURATION, false, true, false, false);
  public static final URelation PRODUCTIONLANGUAGE = new URelation("hasProductionLanguage", UDomain.MOVIE, UDomain.LANGUAGE, false, true, false, false);
  public static final URelation BUDGET = new URelation("hasBudget", UDomain.LEGALACTOR, UDomain.MONETARYVALUE, false, true, false, false);
  public static final URelation IMDB = new URelation("hasImdb", UDomain.MOVIE, UDomain.IDENTIFIER, false, true, false, false);
  public static final URelation PRODUCEDIN = new URelation("producedIn", UDomain.MOVIE, UDomain.COUNTRY, false, true, false, false);
  public static final URelation HASCHILD = new URelation("hasChild", UDomain.PERSON, UDomain.PERSON, false, false, false, false);
  public static final URelation MOTTO = new URelation("hasMotto", UDomain.LEGALACTORGEO, UDomain.STRING, false, false, false, false);
  public static final URelation OFFICIALLANGUAGE = new URelation("hasOfficialLanguage", UDomain.GEOPOLITICAL, UDomain.LANGUAGE, false, false, false, false);
  public static final URelation CAPITAL = new URelation("hasCapital", UDomain.REGION, UDomain.REGION, false, true, false, false); // A "capital" is not a city

  public static final URelation MEANS = new URelation("means", UDomain.WORD, UDomain.ENTITY, false, false, false, false);
  public static final URelation ISCALLED = new URelation("isCalled", UDomain.ENTITY, UDomain.WORD, false, false, false, false);
  public static final URelation TYPE = new URelation("type", UDomain.ENTITY, UDomain.CLASS, false, false, false, false);
  public static final URelation SUBCLASSOF = new URelation("subClassOf", UDomain.CLASS, UDomain.CLASS, true, false, false, false);
  public static final URelation DOMAIN = new URelation("domain", UDomain.RELATION, UDomain.CLASS, false, true, false, false);
  public static final URelation RANGE = new URelation("range", UDomain.RELATION, UDomain.CLASS, false, true, false, false);
  public static final URelation SUBPROPERTYOF = new URelation("subPropertyOf", UDomain.RELATION, UDomain.RELATION, true, false, false, false);

  public static final URelation FAMILYNAME = new URelation("familyNameOf", UDomain.WORD, UDomain.PERSON, false, true, false, false);
  public static final URelation GIVENNAME = new URelation("givenNameOf", UDomain.WORD, UDomain.PERSON, false, true, false, false);
  public static final URelation DESCRIBES = new URelation("describes", UDomain.URL, UDomain.ENTITY, false, false, false, false);
  public static final URelation ESTABLISHEDONDATE = new URelation("establishedOnDate", UDomain.GEOPOLITICAL, UDomain.DATE, false, true, false, false);
  public static final URelation HASWONPRIZE = new URelation("hasWonPrize", UDomain.LEGALACTORGEO, UDomain.PRIZE, false, false, false, false);
  public static final URelation WRITTENINYEAR = new URelation("writtenInYear", UDomain.BOOK, UDomain.DATE, false, true, false, false);
  public static final URelation LOCATEDIN = new URelation("locatedIn", UDomain.CITY, UDomain.REGION, true, true, false, false);
  public static final URelation POLITICIANOF = new URelation("politicianOf", UDomain.POLITICIAN, UDomain.REGION, false, true, false, false);
  public static final URelation CONTEXT = new URelation("context", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation ISCITIZENOF = new URelation("isCitizenOf", UDomain.PERSON, UDomain.COUNTRY, false, true, false, false);
  public static final URelation MEREONYMY = new URelation("isMereonymOf", UDomain.CLASS, UDomain.CLASS, false, false, false, false);
  public static final URelation MEMBEROF = new URelation("isMemberOf", UDomain.CLASS, UDomain.CLASS, false, false, false, false);
  public static final URelation SUBSTANCEOF = new URelation("isSubstanceOf", UDomain.CLASS, UDomain.CLASS, false, false, false, false);
  public static final URelation PARTOF = new URelation("isPartOf", UDomain.CLASS, UDomain.CLASS, false, false, false, false);
  public static final URelation FOUNDIN = new URelation("foundIn", UDomain.FACT, UDomain.URL, false, false, false, false);
  public static final URelation USING = new URelation("using", UDomain.FACT, UDomain.COMPUTERSYSTEM, false, true, false, false);
  public static final URelation DURING = new URelation("during", UDomain.FACT, UDomain.TIMEINTERVAL, false, true, false, false);
  public static final URelation SINCE = new URelation("since", UDomain.FACT, UDomain.TIMEINTERVAL, false, true, false, false);
  public static final URelation UNTIL = new URelation("until", UDomain.FACT, UDomain.TIMEINTERVAL, false, true, false, false);
  public static final URelation INLANGUAGE = new URelation("inLanguage", UDomain.FACT, UDomain.LANGUAGE, false, true, false, false);
  public static final URelation HASVALUE = new URelation("hasValue", UDomain.QUANTITY, UDomain.NUMBER, false, true, false, false);
  public static final URelation INUNIT = new URelation("inUnit", UDomain.FACT, UDomain.UNIT, false, true, false, false);

  // Data concerning inventions, by Gjergji
  public static final URelation DISCOVERED = new URelation("discovered", UDomain.PERSON, UDomain.ENTITY, false, false, false, false);
  public static final URelation DISCOVEREDONDATE = new URelation("discoveredOnDate", UDomain.ENTITY, UDomain.DATE, false, true, false, false);

  // LUBM benchmarks
  public static final URelation lubm_ADVISOR = new URelation("advisor", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_DOCTORALDEGREEFROM = new URelation("doctoralDegreeFrom", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_EMAIL = new URelation("emailAddress", UDomain.ENTITY, UDomain.WORD, false, false, false, false);
  public static final URelation lubm_HEADOF = new URelation("headOf", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_CHAIR = new URelation("Chair", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_MASTERSDEGREEFROM = new URelation("mastersDegreeFrom", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_MEMDEROF = new URelation("memberOf", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_NAME = new URelation("name", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_PUBLICATIONAUTHOR = new URelation("publicationAuthor", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_RESEARCHINTEREST = new URelation("researchInterest", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_SUBORGANISATIONOF = new URelation("subOrganizationOf", UDomain.ENTITY, UDomain.ENTITY, true, false, false, false);
  public static final URelation lubm_TAKESCOURSE = new URelation("takesCourse", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_TEACHEROF = new URelation("teacherOf", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_TEACHINGASSISTANTOF = new URelation("teachingAssistantOf", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_TELEPHONE = new URelation("telephone", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_TYPE = new URelation("type", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_UNDERGRADUATEDEGREEFROM = new URelation("undergraduateDegreeFrom", UDomain.ENTITY, UDomain.ENTITY, false, false, false,
      false);
  public static final URelation lubm_DEGREEFROM = new URelation("degreeFrom", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_HASALUMNUS = new URelation("hasAlumnus", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_WORKSFOR = new URelation("worksFor", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_ALLMEMBERSOF = new URelation("All members", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  public static final URelation lubm_STUDENT = new URelation("student", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);

  // Gazetteer data
  public static final URelation gaz_ISLOCATEDIN = new URelation("gaz_isLocatedIn", UDomain.CITY, UDomain.GEOPOLITICAL, true, true, false, false);
  public static final URelation gaz_HASNAME = new URelation("gaz_hasName", UDomain.IDENTIFIER, UDomain.STRING, false, true, false, false);
  public static final URelation gaz_HASLATITUDE = new URelation("gaz_hasLatitude", UDomain.IDENTIFIER, UDomain.NUMBER, false, true, false, false);
  public static final URelation gaz_HASLONGITUDE = new URelation("gaz_hasLongitude", UDomain.IDENTIFIER, UDomain.NUMBER, false, true, false, false);

  // Spatial predicate(s) (may have third argument)
  public static final URelation gaz_ISCLOSE = new URelation("gaz_isClose", UDomain.IDENTIFIER, UDomain.NUMBER, false, false, false, true);
  public static final URelation ISCLOSE = new URelation("isClose", UDomain.IDENTIFIER, UDomain.NUMBER, false, false, false, true);

  /** The domain reference of that URelation object. */
  private UDomain argDomain;

  /** The range reference of that URelation object. */
  private UDomain argRange;

  /** The reference to the super (father) URelation of the URelation object. */
  private URelation superURelation;

  /** The flag that indicates if the given URelation object is transitive. */
  private boolean transitive = false;

  /** The flag that indicates if the given URelation object is arithmetic. */
  private boolean arithmetic = false;

  /** The flag that indicates if the given URelation object is a function call. */
  private boolean isFunctionCall = false;

  /** The flag that indicates if the given URelation object is functional. */
  private boolean functional = false;

  /** The flag that indicates if the given URelation object is reflexive. */
  private boolean reflexive = false;

  /** The flag that indicates if the given URelation object is symmetric. */
  private boolean symmetric = false;

  /** A pointer to a FunctionCall object. */
  private FunctionCall functionCall;

  /** The empty constructor for the URelation instance. */
  public URelation() {
    super();
  }

  /**
   * The constructor that initializes the URelation object.
   * 
   * @param name
   *          the name for the URelation object.
   * */
  public URelation(String name) {
    this.init(name, UDomain.DEFAULT_DOMAIN, UDomain.DEFAULT_DOMAIN, null, false, false, false, false);
  }

  /**
   * The constructor that initializes the URelation object.
   * 
   * @param name
   *          the name for the URelation object.
   * @param domain
   *          the domain for the URelation object.
   * @param range
   *          the range for the URelation object.
   * @param isTransitive
   *          the flag that indicates whether this URelation instance is transitive or not.
   * @param isFunction
   *          the flag that indicates whether this URelation instance is a function or not.
   * @param isArithmetic
   *          the flag that indicates whether this URelation instance is arithmetic or not.
   * */
  public URelation(String name, UDomain domain, UDomain range, boolean isTransitive, boolean isFunction, boolean isArithmetic, boolean isFunctionCall) {
    this.init(name, domain, range, null, isTransitive, isFunction, isArithmetic, isFunctionCall);
  }

  /**
   * The constructor that initializes the URelation object.
   * 
   * @param name
   *          the name for the URelation object.
   * @param domain
   *          the domain for the URelation object.
   * @param range
   *          the range for the URelation object.
   * @param superURelation
   *          the super-relation for the URelation object.
   * @param isTransitive
   *          the flag that indicates whether this URelation instance is transitive or not.
   * @param isFunction
   *          the flag that indicates whether this URelation instance is a function or not.
   * @param isArithmetic
   *          the flag that indicates whether this URelation instance is arithmetic or not.
   * */
  public URelation(String name, UDomain domain, UDomain range, URelation superURelation, boolean isTransitive, boolean isFunction, boolean isArithmetic,
      boolean isFunctionCall) {
    this.init(name, domain, range, superURelation, isTransitive, isFunction, isArithmetic, isFunctionCall);
  }

  /**
   * Delivers the URelation object to the given name of a requested relation.
   * <p>
   * In case the requested relation does not exist, a new URelation object with the given name is built.
   * 
   * @param name
   *          the name of the requested URelation object.
   * @return the requested URelation object or a new URelation object.
   * */
  public static URelation valueOf(String name) {
    URelation relation = relations.get(name);
    if (relation == null)
      relation = new URelation(name);
    return relation;
  }

  public static Collection<URelation> instances() {
    return relations.values();
  }

  /**
   * Delivers the URelation object to the given name of a requested relation.
   * <p>
   * In case the requested relation does not exist, a URelation object with the given parameter is built.
   * 
   * @param name
   *          the name for the URelation object.
   * @param domain
   *          the domain for the URelation object.
   * @param range
   *          the range for the URelation object.
   * @param superURelation
   *          the super-relation for the URelation object.
   * @param isTransitive
   *          the flag that indicates whether this URelation instance is transitive or not.
   * @param isFunction
   *          the flag that indicates whether this URelation instance is a function or not.
   * @param isArithmetic
   *          the flag that indicates whether this URelation instance is arithmetic or not.
   * @return the requested URelation object or the recently built one.
   * */
  public static URelation valueOf(String name, UDomain domain, UDomain range, URelation superURelation, boolean isTransitive, boolean isFunction,
      boolean isArithmetic, boolean isFunctionCall) {
    URelation relation = relations.get(name);
    if (relation == null)
      relation = new URelation(name, domain, range, superURelation, isTransitive, isFunction, isArithmetic, isFunctionCall);
    return relation;
  }

  /**
   * Initializes the URelation object.
   * 
   * @param name
   *          the name for the URelation.
   * @param domain
   *          the domain for the URelation.
   * @param range
   *          the range for the URelation.
   * @param superRel
   *          the super URelation for the URelation.
   * */
  public void init(String name, UDomain domain, UDomain range, URelation superRel) {
    super.init(name);
    this.argDomain = domain;
    this.argRange = range;
    this.superURelation = superRel;
    relations.put(name, this);
  }

  /**
   * Initializes the URelation object.
   * 
   * @param name
   *          the name for the URelation object.
   * @param domain
   *          the domain for the URelation object.
   * @param range
   *          the range for the URelation object.
   * @param superRel
   *          the super-relation for the URelation object.
   * @param isTransitive
   *          the flag that indicates whether this URelation instance is transitive or not.
   * @param isFunction
   *          the flag that indicates whether this URelation instance is a function or not.
   * @param isArithmetic
   *          the flag that indicates whether this URelation instance is arithmetic or not.
   * */
  public void init(String name, UDomain domain, UDomain range, URelation superRel, boolean isTransitive, boolean isFunctional, boolean isArithmetic,
      boolean isFunction) {
    super.init(name);
    this.argDomain = domain;
    this.argRange = range;
    this.superURelation = superRel;
    this.transitive = isTransitive;
    this.functional = isFunctional;
    this.arithmetic = isArithmetic;
    this.isFunctionCall = isFunction;
    relations.put(name, this);
  }

  /**
   * Sets the name (actual value) for the URelation object.
   * <p>
   * This method overrides the super method, because we need to check if the given name of this URelation starts with a "?". This way, we can identify, if this
   * URelation instance is a variable or actually a real URelation instance.
   * 
   * @param name
   *          the name for the URelation object.
   */
  /*
   * @Override public void setName(String name) { // here was the failure //this.setName(name); super.setName(name); }
   */

  /**
   * Delivers the domain reference of the URelation object.
   * 
   * @return the domain of the URelation object.
   */
  public UDomain getArgDomain() {
    return argDomain;
  }

  /**
   * Sets the domain reference for the URelation object.
   * 
   * @param domain
   *          the domain for the URelation object.
   */
  public void setArgDomain(UDomain domain) {
    this.argDomain = domain;
  }

  /**
   * Sets the reference to a FunctionCall for this URelation object.
   * 
   * @param functionCall
   *          the reference to the FunctionCall object.
   */
  public void setFunctionCall(FunctionCall functionCall) {
    this.functionCall = functionCall;
  }

  /**
   * Gets the reference to the FunctionCall of this URelation object.
   * 
   * @param functionCall
   *          the reference to the FunctionCall object.
   */
  public FunctionCall getFunctionCall() {
    return functionCall;
  }

  /**
   * Delivers the range reference of the URelation object.
   * 
   * @return the range of the URelation object.
   */
  public UDomain getArgRange() {
    return argRange;
  }

  /**
   * Sets the range reference for the URelation object.
   * 
   * @param range
   *          the range for the URelation object.
   */
  public void setArgRange(UDomain range) {
    this.argRange = range;
  }

  /**
   * Delivers the super (father) URelation of the URelation object.
   * 
   * @return the super URelation of the URelation object.
   */
  public URelation getSuperURelation() {
    return superURelation;
  }

  /**
   * Sets the super (father) URelation for the URelation object.
   * 
   * @param superURelation
   *          the super URelation for the URelation object.
   */
  public void setSuperURelation(URelation superURelation) {
    this.superURelation = superURelation;
  }

  /**
   * Checks whether this URelation object is transitive or not.
   * 
   * @return true, if the URelation object is transitive, false otherwise.
   */
  public boolean isTransitive() {
    return transitive;
  }

  /**
   * Sets the flag that indicates if the URelation object is transitive or not.
   * 
   * @param transitive
   *          the transitivity flag for the URelation object.
   */
  public void setTransitive(boolean transitive) {
    this.transitive = transitive;
  }

  /**
   * Checks whether this URelation object is arithmetic or not.
   * 
   * @return true, if the URelation object is arithmetic, false otherwise.
   */
  public boolean isArithmetic() {
    return arithmetic;
  }

  /**
   * Checks whether this URelation object is a function call or not.
   * 
   * @return true, if the URelation object is a function call, false otherwise.
   */
  public boolean isFunctionCall() {
    return isFunctionCall;
  }

  /**
   * Sets the flag that indicates if the URelation object is transitive or not.
   * 
   * @param arithmetic
   *          the arithmetic flag for the URelation object.
   */
  public void setArithmetic(boolean arithmetic) {
    this.arithmetic = arithmetic;
  }

  /**
   * Checks whether this URelation object is functional or not.
   * 
   * @return true, if the URelation object is functional, false otherwise.
   */
  public boolean isFunctional() {
    return functional;
  }

  /**
   * Sets the flag that indicates if the URelation object is a function or not.
   * 
   * @param function
   *          the function flag for the URelation object.
   */
  public void setFunctional(boolean isFunctional) {
    this.functional = isFunctional;
  }

  /**
   * Checks whether this URelation object is reflexive or not.
   * 
   * @return true, if the URelation object is reflexive, false otherwise.
   */
  public boolean isReflexive() {
    return reflexive;
  }

  /**
   * Sets the flag that indicates if the URelation object is reflexive or not.
   * 
   * @param reflexive
   *          the reflexivity flag for the URelation object.
   */
  public void setReflexive(boolean reflexive) {
    this.reflexive = reflexive;
  }

  /**
   * Checks whether this URelation object is symmetric or not.
   * 
   * @return true, if the URelation object is symmetric, false otherwise.
   */
  public boolean isSymmetric() {
    return symmetric;
  }

  /**
   * Sets the flag that indicates if the URelation object is symmetric or not.
   * 
   * @param symmetric
   *          the symmetry flag for the URelation object.
   */
  public void setSymmetric(boolean symmetric) {
    this.symmetric = symmetric;
  }

  /**
   * Returns the string representation of all the used URelation objects within the URDF framework.
   * 
   * @return the string representation.
   * */
  public String printURelations() {

    StringBuilder URelations = new StringBuilder();

    URelations.append("[");
    for (String rel : relations.keySet())
      URelations.append(rel + ",");
    URelations.append("]");

    return ("used URelations: " + URelations.toString());
  }

  /**
   * Delivers the internal map of used URelation objects within the reasoner.
   * 
   * @return the map of used URelation objects.
   */
  public Map<String, URelation> getRelations() {
    // if (relations == null)
    // loadURelationsList(URelationS_FILE);
    return relations;
  }

  /**
   * Checks whether this object is a URelation object or a variable..
   * 
   * @return true, if this object is a URelation object, false if it is a variable.
   * */
  @Override
  public boolean isRelation() {
    return true;
  }

  /**
   * Returns a string representation of the URelation object.
   * 
   * @return the string representation of the URelation object.
   */
  @Override
  public String toString() {
    return getName(); // super.toString() + " URelation [argDomain = " + argDomain + ", argRange = " + argRange + ((superURelation != null) ?
    // (", superURelation = " + superURelation.getName() + "]") : "]");
  }

  /**
   * Reads in the serialized data from the mapped URelation ActionScript class.
   * 
   * @param input
   *          the serialized input data.
   * */
  @Override
  public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
    super.readExternal(input);
    argDomain = (UDomain) input.readObject();
    argRange = (UDomain) input.readObject();
    superURelation = (URelation) input.readObject();
    transitive = input.readBoolean();
    arithmetic = input.readBoolean();
    functional = input.readBoolean();
    reflexive = input.readBoolean();
    symmetric = input.readBoolean();
    isFunctionCall = input.readBoolean();
  }

  /**
   * Writes out the data to serialize to the mapped URelation ActionScript class.
   * 
   * @param output
   *          the output data to serialize.
   * */
  @Override
  public void writeExternal(ObjectOutput output) throws IOException {
    super.writeExternal(output);
    output.writeObject(argDomain);
    output.writeObject(argRange);
    output.writeObject(superURelation);
    output.writeBoolean(transitive);
    output.writeBoolean(arithmetic);
    output.writeBoolean(functional);
    output.writeBoolean(reflexive);
    output.writeBoolean(symmetric);
    output.writeBoolean(isFunctionCall);
  }
}
