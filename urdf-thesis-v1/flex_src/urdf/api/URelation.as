/**
 * 
 */
package urdf.api {

import flash.utils.IDataInput;
import flash.utils.IDataOutput;

import mx.collections.ArrayCollection;
import mx.collections.Sort;
import mx.collections.SortField;

import util.UStringHashMap;

/**
 * Acts as a function library and delivers generally required functionality that can be used globally.
 * 
 * @author Timm Meiser
 * @since 06.01.10
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.URelation")]
public class URelation extends UArgument {

  /** The list of used URelation within the reasoning engine. */
  //private static Map<String, URelation> relations = new HashMap<String, URelation>();
  private static var relations:UStringHashMap = new UStringHashMap();

  // Generic and arithmetic predicates
  public static const DUMMY:URelation= new URelation();//"dummy", UDomain.ENTITY, UDomain.ENTITY, false, false, true);
  public static const EQUALS:URelation= new URelation();//"equals", UDomain.ENTITY, UDomain.ENTITY, false, false, true);
  public static const NOTEQUALS:URelation= new URelation();//"notEquals", UDomain.ENTITY, UDomain.ENTITY, false, false, true);
  public static const GREATER:URelation= new URelation();//"greater", UDomain.ENTITY, UDomain.ENTITY, false, false, true);
  public static const GREATEREQ:URelation= new URelation();//"greaterEq", UDomain.ENTITY, UDomain.ENTITY, false, false, true);
  public static const LOWER:URelation= new URelation();//"lower", UDomain.ENTITY, UDomain.ENTITY, false, false, true);
  public static const LOWEREQ:URelation= new URelation();//"lowerEq", UDomain.ENTITY, UDomain.ENTITY, false, false, true);
  public static const DIFFERENCELT:URelation= new URelation();//"difference_85", UDomain.ENTITY, UDomain.ENTITY, false, false, true);
  public static const YEARBEFORE:URelation= new URelation();//"yearBefore", UDomain.ENTITY, UDomain.ENTITY, false, false, true);
  public static const DATEBEFORE:URelation= new URelation();//"difference_65", UDomain.ENTITY, UDomain.ENTITY, false, false, true); 
  
  // Temporal predicate(s)
  public static const ISWITHINHOURS:URelation = new URelation();//"isWithinHours", UDomain.DATE, UDomain.DATE, false, false, true, false);
  public static const ISWITHINMINUTES:URelation = new URelation();//"isWithinMinutes", UDomain.DATE, UDomain.DATE, false, false, true, false);
  public static const SAMEDAY:URelation = new URelation();//"sameDay", UDomain.DATE, UDomain.DATE, false, false, true, false);

  // Yago template facts
  public static const BORNONDATE:URelation= new URelation();//"bornOnDate", UDomain.PERSON, UDomain.DATE, false, true, false);
  public static const BORNIN:URelation= new URelation();//"bornIn", UDomain.PERSON, UDomain.CITY, false, true, false);
  public static const ORIGIN:URelation= new URelation();//"originatesFrom", UDomain.PERSON, UDomain.GEOPOLITICAL, false, true, false);
  public static const DIEDONDATE:URelation= new URelation();//"diedOnDate", UDomain.PERSON, UDomain.DATE, false, true, false);
  public static const DIEDIN:URelation= new URelation();//"diedIn", UDomain.PERSON, UDomain.CITY, false, true, false);
  public static const LIVESIN:URelation= new URelation();//"livesIn", UDomain.PERSON, UDomain.AREA, false, true, false);
  public static const ACADEMICADVISOR:URelation= new URelation();//"hasAcademicAdvisor", UDomain.PERSON, UDomain.PERSON, false, true, false);
  public static const ALMAMATER:URelation= new URelation();//"graduatedFrom", UDomain.PERSON, UDomain.UNIVERSITY, false, true, false);
  public static const WORKSAT:URelation= new URelation();//"worksAt", UDomain.PERSON, UDomain.LEGALINSTITUTION, false, true, false);
  public static const NATIVENAME:URelation= new URelation();//"isNativeNameOf", UDomain.WORD, UDomain.GEOPOLITICAL, false, true, false);
  public static const LEADER:URelation= new URelation();//"isLeaderOf", UDomain.PERSON, UDomain.LEGALACTORGEO, false, false, false);
  public static const AREA:URelation= new URelation();//"hasArea", UDomain.GEOPOLITICAL, UDomain.AREA, false, true, false);
  public static const POPULATION:URelation= new URelation();//"hasPopulation", UDomain.GEOPOLITICAL, UDomain.NONNEGATIVEINTEGER, false, true, false);
  public static const POPULATIONDENSITY:URelation= new URelation();//"hasPopulationDensity", UDomain.GEOPOLITICAL, UDomain.DENSITYPERAREA, false, true, false);
  public static const UTCOFFSET:URelation= new URelation();//"hasUTCOffset", UDomain.GEOPOLITICAL, UDomain.INTEGER, false, true, false);
  public static const WEBSITE:URelation= new URelation();//"hasWebsite", UDomain.LEGALACTORGEO, UDomain.URL, false, false, false);
  public static const ORDER:URelation= new URelation();//"isNumber", UDomain.PERSON, UDomain.NONNEGATIVEINTEGER, false, true, false);
  public static const PREDECESSOR:URelation= new URelation();//"hasPredecessor", UDomain.PERSON, UDomain.PERSON, false, true, false);
  public static const SUCCESSOR:URelation= new URelation();//"hasSuccessor", UDomain.PERSON, UDomain.PERSON, false, true, false);
  public static const MARRIEDTO:URelation= new URelation();//"isMarriedTo", UDomain.PERSON, UDomain.PERSON, false, true, false);
  public static const AFFILIATEDTO:URelation= new URelation();//"isAffiliatedTo", UDomain.LEGALACTOR, UDomain.LEGALACTOR, false, false, false);
  public static const INFLUENCED:URelation= new URelation();//"influences", UDomain.PERSON, UDomain.PERSON, false, false, false);
  public static const DIRECTED:URelation= new URelation();//"directed", UDomain.PERSON, UDomain.MOVIE, false, false, false);
  public static const PRODUCED:URelation= new URelation();//"produced", UDomain.LEGALACTOR, UDomain.MOVIE, false, false, false);
  public static const EDITED:URelation= new URelation();//"edited", UDomain.PERSON, UDomain.MOVIE, false, false, false);
  public static const ACTEDIN:URelation= new URelation();//"actedIn", UDomain.ACTOR, UDomain.MOVIE, false, false, false);
  public static const PUBLISHEDONDATE:URelation= new URelation();//"publishedOnDate", UDomain.ENTITY, UDomain.DATE, false, true, false);
  public static const DURATION:URelation= new URelation();//"hasDuration", UDomain.ENTITY, UDomain.DURATION, false, true, false);
  public static const PRODUCTIONLANGUAGE:URelation= new URelation();//"hasProductionLanguage", UDomain.MOVIE, UDomain.LANGUAGE, false, true, false);
  public static const BUDGET:URelation= new URelation();//"hasBudget", UDomain.LEGALACTOR, UDomain.MONETARYVALUE, false, true, false);
  public static const IMDB:URelation= new URelation();//"hasImdb", UDomain.MOVIE, UDomain.IDENTIFIER, false, true, false);
  public static const PRODUCEDIN:URelation= new URelation();//"producedIn", UDomain.MOVIE, UDomain.COUNTRY, false, true, false);
  public static const HASCHILD:URelation= new URelation();//"hasChild", UDomain.PERSON, UDomain.PERSON, false, false, false);
  public static const MOTTO:URelation= new URelation();//"hasMotto", UDomain.LEGALACTORGEO, UDomain.STRING, false, false, false);
  public static const OFFICIALLANGUAGE:URelation= new URelation();//"hasOfficialLanguage", UDomain.GEOPOLITICAL, UDomain.LANGUAGE, false, false, false);
  public static const CAPITAL:URelation= new URelation();//"hasCapital", UDomain.GEOPOLITICAL, UDomain.GEOPOLITICAL, false, true, false); // A "capital" is not a city

  public static const MEANS:URelation= new URelation();//"means", UDomain.WORD, UDomain.ENTITY, false, false, false);
  public static const ISCALLED:URelation= new URelation();//"isCalled", UDomain.ENTITY, UDomain.WORD, false, false, false);
  public static const TYPE:URelation= new URelation();//"type", UDomain.ENTITY, UDomain.CLASS, false, false, false);
  public static const SUBCLASSOF:URelation= new URelation();//"subClassOf", UDomain.CLASS, UDomain.CLASS, true, false, false);
  public static const DOMAIN:URelation= new URelation();//"domain", UDomain.RELATION, UDomain.CLASS, false, true, false);
  public static const RANGE:URelation= new URelation();//"range", UDomain.RELATION, UDomain.CLASS, false, true, false);
  public static const SUBPROPERTYOF:URelation= new URelation();//"subPropertyOf", UDomain.RELATION, UDomain.RELATION, true, false, false);

  public static const FAMILYNAME:URelation= new URelation();//"familyNameOf", UDomain.WORD, UDomain.PERSON, false, true, false);
  public static const GIVENNAME:URelation= new URelation();//"givenNameOf", UDomain.WORD, UDomain.PERSON, false, true, false);
  public static const DESCRIBES:URelation= new URelation();//"describes", UDomain.URL, UDomain.ENTITY, false, false, false);
  public static const ESTABLISHEDONDATE:URelation= new URelation();//"establishedOnDate", UDomain.GEOPOLITICAL, UDomain.DATE, false, true, false);
  public static const HASWONPRIZE:URelation= new URelation();//"hasWonPrize", UDomain.LEGALACTORGEO, UDomain.PRIZE, false, false, false);
  public static const WRITTENINYEAR:URelation= new URelation();//"writtenInYear", UDomain.BOOK, UDomain.DATE, false, true, false);
  public static const LOCATEDIN:URelation= new URelation();//"locatedIn", UDomain.CITY, UDomain.GEOPOLITICAL, true, true, false);
  public static const POLITICIANOF:URelation= new URelation();//"politicianOf", UDomain.POLITICIAN, UDomain.USSTATE, false, true, false);
  public static const CONTEXT:URelation= new URelation();//"context", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const ISCITIZENOF:URelation= new URelation();//"isCitizenOf", UDomain.PERSON, UDomain.COUNTRY, false, true, false);
  public static const MEREONYMY:URelation= new URelation();//"isMereonymOf", UDomain.CLASS, UDomain.CLASS, false, false, false);
  public static const MEMBEROF:URelation= new URelation();//"isMemberOf", UDomain.CLASS, UDomain.CLASS, false, false, false);
  public static const SUBSTANCEOF:URelation= new URelation();//"isSubstanceOf", UDomain.CLASS, UDomain.CLASS, false, false, false);
  public static const PARTOF:URelation= new URelation();//"isPartOf", UDomain.CLASS, UDomain.CLASS, false, false, false);
  public static const FOUNDIN:URelation= new URelation();//"foundIn", UDomain.FACT, UDomain.URL, false, false, false);
  public static const USING:URelation= new URelation();//"using", UDomain.FACT, UDomain.COMPUTERSYSTEM, false, true, false);
  public static const DURING:URelation= new URelation();//"during", UDomain.FACT, UDomain.TIMEINTERVAL, false, true, false);
  public static const SINCE:URelation= new URelation();//"since", UDomain.FACT, UDomain.TIMEINTERVAL, false, true, false);
  public static const UNTIL:URelation= new URelation();//"until", UDomain.FACT, UDomain.TIMEINTERVAL, false, true, false);
  public static const INLANGUAGE:URelation= new URelation();//"inLanguage", UDomain.FACT, UDomain.LANGUAGE, false, true, false);
  public static const HASVALUE:URelation= new URelation();//"hasValue", UDomain.QUANTITY, UDomain.NUMBER, false, true, false);
  public static const INUNIT:URelation= new URelation();//"inUnit", UDomain.FACT, UDomain.UNIT, false, true, false);

  // Data concerning inventions, by Gjergji
  public static const DISCOVERED:URelation= new URelation();//"discovered", UDomain.PERSON, UDomain.ENTITY, false, false, false);
  public static const DISCOVEREDONDATE:URelation= new URelation();//"discoveredOnDate", UDomain.ENTITY, UDomain.DATE, false, true, false);


  
  // LUBM benchmarks
  public static const lubm_ADVISOR:URelation= new URelation();//"advisor", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_DOCTORALDEGREEFROM:URelation= new URelation();//"doctoralDegreeFrom", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_EMAIL:URelation= new URelation();//"emailAddress", UDomain.ENTITY, UDomain.WORD, false, false, false);
  public static const lubm_HEADOF:URelation= new URelation();//"headOf", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_CHAIR:URelation= new URelation();//"Chair", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_MASTERSDEGREEFROM:URelation= new URelation();//"mastersDegreeFrom", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_MEMDEROF:URelation= new URelation();//"memberOf", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_NAME:URelation= new URelation();//"name", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_PUBLICATIONAUTHOR:URelation= new URelation();//"publicationAuthor", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_RESEARCHINTEREST:URelation= new URelation();//"researchInterest", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_SUBORGANISATIONOF:URelation= new URelation();//"subOrganizationOf", UDomain.ENTITY, UDomain.ENTITY, true, false, false);
  public static const lubm_TAKESCOURSE:URelation= new URelation();//"takesCourse", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_TEACHEROF:URelation= new URelation();//"teacherOf", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_TEACHINGASSISTANTOF:URelation= new URelation();//"teachingAssistantOf", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_TELEPHONE:URelation= new URelation();//"telephone", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_TYPE:URelation= new URelation();//"type", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_UNDERGRADUATEDEGREEFROM:URelation= new URelation();//"undergraduateDegreeFrom", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_DEGREEFROM:URelation= new URelation();//"degreeFrom", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_HASALUMNUS:URelation= new URelation();//"hasAlumnus", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_WORKSFOR:URelation= new URelation();//"worksFor", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_ALLMEMBERSOF:URelation= new URelation();//"All members", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  public static const lubm_STUDENT:URelation= new URelation();//"student", UDomain.ENTITY, UDomain.ENTITY, false, false, false);
  
  
  // Gazetteer data
  public static const gaz_ISLOCATEDIN:URelation = new URelation();//"gaz_isLocatedIn", UDomain.CITY, UDomain.GEOPOLITICAL, true, true, false, false);
  public static const gaz_HASNAME:URelation = new URelation();//"gaz_hasName", UDomain.IDENTIFIER, UDomain.STRING, false, true, false, false);
  public static const gaz_HASLATITUDE:URelation = new URelation();//"gaz_hasLatitude", UDomain.IDENTIFIER, UDomain.NUMBER, false, true, false, false);
  public static const gaz_HASLONGITUDE:URelation = new URelation();//"gaz_hasLongitude", UDomain.IDENTIFIER, UDomain.NUMBER, false, true, false, false);

  // Spatial predicate(s) (may have third argument)
  public static const gaz_ISCLOSE:URelation = new URelation();//"gaz_isClose", UDomain.IDENTIFIER, UDomain.NUMBER, false, false, false, true);
  public static const ISCLOSE:URelation = new URelation();//"isClose", UDomain.IDENTIFIER, UDomain.NUMBER, false, false, false, true);
  
  // MMCI relations
  /*
  public static const HASDESTINATION:URelation = URelation.valueOfRelation("hasDestination");
  public static const HASEVENT:URelation = URelation.valueOfRelation("hasEvent");
  public static const HASEVENTPARTICIPANT:URelation = URelation.valueOfRelation("hasParticipant");
  public static const HASJOINTEVENT:URelation = URelation.valueOfRelation("hasJointEvent");
  public static const EVENTLOCATION:URelation = URelation.valueOfRelation("eventLocation");
  public static const EVENTDATE:URelation = URelation.valueOfRelation("eventDate");
  public static const JOINTCORRESPONDENCE:URelation = URelation.valueOfRelation("hasEmailCorrespondence");
  public static const LOCATIONREFERENCE:URelation = URelation.valueOfRelation("hasLocation");
  public static const LOCATIONNAME:URelation = URelation.valueOfRelation("hasLocationName");
  public static const LOCATIONDISPLAY:URelation = URelation.valueOfRelation("hasLocationDisplay");
  public static const DATEREFERENCE:URelation = URelation.valueOfRelation("hasDate");
  public static const HASEMAIL:URelation = URelation.valueOfRelation("hasEmailAddress");
  public static const RECIPIENT:URelation = URelation.valueOfRelation("hasRecipient");
  public static const SENDER:URelation = URelation.valueOfRelation("hasSender");
  public static const ROOMMATES:URelation = URelation.valueOfRelation("roomMates");
  public static const HASPHONE:URelation = URelation.valueOfRelation("hasPhone");
  public static const FIRSTNAME:URelation = URelation.valueOfRelation("hasFirstName");
  public static const LASTNAME:URelation = URelation.valueOfRelation("hasLastName");
  */

  /** The domain reference of that URelation object. */
  private var argDomain:UDomain;

  /** The range reference of that URelation object. */
  private var argRange:UDomain;

  /** The reference to the super (father) URelation of the URelation object. */
  private var superURelation:URelation;

  /** The flag that indicates if the given URelation object is transitive. */
  private var transitive:Boolean = false;

  /** The flag that indicates if the given URelation object is arithmetic. */
  private var arithmetic:Boolean = false;

  /** The flag that indicates if the given URelation object is a function call. */
  private var _functionCall:Boolean = false;

  // we have to add "_" because function is a predefined ActionScript word
  /** The flag that indicates if the given URelation object is a function. */
  private var _function:Boolean = false; 

  /** The flag that indicates if the given URelation object is reflexive. */
  private var reflexive:Boolean = false;

  /** The flag that indicates if the given URelation object is symmetric. */
  private var symmetric:Boolean = false;

  
  /* The static initializer is needed in order to use the init methods for the UDomain objects. That`s because
   * we only have one constructor, the empty default constructor. */
  {
  	
  	// Generic and arithmetic predicates
    DUMMY.initStaticRelation("dummy", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
    EQUALS.initStaticRelation("equals", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
    NOTEQUALS.initStaticRelation("notEquals", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
    GREATER.initStaticRelation("greater", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
    GREATEREQ.initStaticRelation("greaterEq", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
    LOWER.initStaticRelation("lower", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
    LOWEREQ.initStaticRelation("lowerEq", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
    DIFFERENCELT.initStaticRelation("difference", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
    YEARBEFORE.initStaticRelation("yearBefore", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);
    DATEBEFORE.initStaticRelation("dateBefore", UDomain.ENTITY, UDomain.ENTITY, false, false, true, false);

    // Temporal predicate(s)
    ISWITHINHOURS.initStaticRelation("isWithinHours", UDomain.DATE, UDomain.DATE, false, false, true, false);
    ISWITHINMINUTES.initStaticRelation("isWithinMinutes", UDomain.DATE, UDomain.DATE, false, false, true, false);
    SAMEDAY.initStaticRelation("sameDay", UDomain.DATE, UDomain.DATE, false, false, true, false);

    // Yago template facts
    BORNONDATE.initStaticRelation("bornOnDate", UDomain.PERSON, UDomain.DATE, false, true, false, false);
    BORNIN.initStaticRelation("bornIn", UDomain.PERSON, UDomain.CITY, false, true, false, false);
    ORIGIN.initStaticRelation("originatesFrom", UDomain.PERSON, UDomain.REGION, false, true, false, false);
    DIEDONDATE.initStaticRelation("diedOnDate", UDomain.PERSON, UDomain.DATE, false, true, false, false);
    DIEDIN.initStaticRelation("diedIn", UDomain.PERSON, UDomain.CITY, false, true, false, false);
    LIVESIN.initStaticRelation("livesIn", UDomain.PERSON, UDomain.REGION, false, true, false, false);
    ACADEMICADVISOR.initStaticRelation("hasAcademicAdvisor", UDomain.PERSON, UDomain.PERSON, false, true, false, false);
    ALMAMATER.initStaticRelation("graduatedFrom", UDomain.PERSON, UDomain.UNIVERSITY, false, true, false, false);
    WORKSAT.initStaticRelation("worksAt", UDomain.PERSON, UDomain.LEGALINSTITUTION, false, true, false, false);
    NATIVENAME.initStaticRelation("isNativeNameOf", UDomain.WORD, UDomain.GEOPOLITICAL, false, true, false, false);
    LEADER.initStaticRelation("isLeaderOf", UDomain.PERSON, UDomain.LEGALACTORGEO, false, false, false, false);
    AREA.initStaticRelation("hasArea", UDomain.REGION, UDomain.AREA, false, true, false, false);
    POPULATION.initStaticRelation("hasPopulation", UDomain.REGION, UDomain.NONNEGATIVEINTEGER, false, true, false, false);
    POPULATIONDENSITY.initStaticRelation("hasPopulationDensity", UDomain.REGION, UDomain.DENSITYPERAREA, false, true, false, false);
    UTCOFFSET.initStaticRelation("hasUTCOffset", UDomain.REGION, UDomain.INTEGER, false, true, false, false);
    WEBSITE.initStaticRelation("hasWebsite", UDomain.LEGALACTORGEO, UDomain.URL, false, false, false, false);
    ORDER.initStaticRelation("isNumber", UDomain.PERSON, UDomain.NONNEGATIVEINTEGER, false, true, false, false);
    PREDECESSOR.initStaticRelation("hasPredecessor", UDomain.PERSON, UDomain.PERSON, false, true, false, false);
    SUCCESSOR.initStaticRelation("hasSuccessor", UDomain.PERSON, UDomain.PERSON, false, true, false, false);
    MARRIEDTO.initStaticRelation("isMarriedTo", UDomain.PERSON, UDomain.PERSON, false, true, false, false);
    AFFILIATEDTO.initStaticRelation("isAffiliatedTo", UDomain.LEGALACTOR, UDomain.LEGALACTOR, false, false, false, false);
    INFLUENCED.initStaticRelation("influences", UDomain.PERSON, UDomain.PERSON, false, false, false, false);
    DIRECTED.initStaticRelation("directed", UDomain.PERSON, UDomain.MOVIE, false, false, false, false);
    PRODUCED.initStaticRelation("produced", UDomain.LEGALACTOR, UDomain.MOVIE, false, false, false, false);
    EDITED.initStaticRelation("edited", UDomain.PERSON, UDomain.MOVIE, false, false, false, false);
    ACTEDIN.initStaticRelation("actedIn", UDomain.ACTOR, UDomain.MOVIE, false, false, false, false);
    PUBLISHEDONDATE.initStaticRelation("publishedOnDate", UDomain.ENTITY, UDomain.DATE, false, true, false, false);
    DURATION.initStaticRelation("hasDuration", UDomain.ENTITY, UDomain.DURATION, false, true, false, false);
    PRODUCTIONLANGUAGE.initStaticRelation("hasProductionLanguage", UDomain.MOVIE, UDomain.LANGUAGE, false, true, false, false);
    BUDGET.initStaticRelation("hasBudget", UDomain.LEGALACTOR, UDomain.MONETARYVALUE, false, true, false, false);
    IMDB.initStaticRelation("hasImdb", UDomain.MOVIE, UDomain.IDENTIFIER, false, true, false, false);
    PRODUCEDIN.initStaticRelation("producedIn", UDomain.MOVIE, UDomain.COUNTRY, false, true, false, false);
    HASCHILD.initStaticRelation("hasChild", UDomain.PERSON, UDomain.PERSON, false, false, false, false);
    MOTTO.initStaticRelation("hasMotto", UDomain.LEGALACTORGEO, UDomain.STRING, false, false, false, false);
    OFFICIALLANGUAGE.initStaticRelation("hasOfficialLanguage", UDomain.GEOPOLITICAL, UDomain.LANGUAGE, false, false, false, false);
    CAPITAL.initStaticRelation("hasCapital", UDomain.REGION, UDomain.REGION, false, true, false, false); // A "capital" is not a city

    MEANS.initStaticRelation("means", UDomain.WORD, UDomain.ENTITY, false, false, false, false);
    ISCALLED.initStaticRelation("isCalled", UDomain.ENTITY, UDomain.WORD, false, false, false, false);
    TYPE.initStaticRelation("type", UDomain.ENTITY, UDomain.CLASS, false, false, false, false);
    SUBCLASSOF.initStaticRelation("subClassOf", UDomain.CLASS, UDomain.CLASS, true, false, false, false);
    DOMAIN.initStaticRelation("domain", UDomain.RELATION, UDomain.CLASS, false, true, false, false);
    RANGE.initStaticRelation("range", UDomain.RELATION, UDomain.CLASS, false, true, false, false);
    SUBPROPERTYOF.initStaticRelation("subPropertyOf", UDomain.RELATION, UDomain.RELATION, true, false, false, false);

    FAMILYNAME.initStaticRelation("familyNameOf", UDomain.WORD, UDomain.PERSON, false, true, false, false);
    GIVENNAME.initStaticRelation("givenNameOf", UDomain.WORD, UDomain.PERSON, false, true, false, false);
    DESCRIBES.initStaticRelation("describes", UDomain.URL, UDomain.ENTITY, false, false, false, false);
    ESTABLISHEDONDATE.initStaticRelation("establishedOnDate", UDomain.GEOPOLITICAL, UDomain.DATE, false, true, false, false);
    HASWONPRIZE.initStaticRelation("hasWonPrize", UDomain.LEGALACTORGEO, UDomain.PRIZE, false, false, false, false);
    WRITTENINYEAR.initStaticRelation("writtenInYear", UDomain.BOOK, UDomain.DATE, false, true, false, false);
    LOCATEDIN.initStaticRelation("locatedIn", UDomain.CITY, UDomain.REGION, true, true, false, false);
    POLITICIANOF.initStaticRelation("politicianOf", UDomain.POLITICIAN, UDomain.REGION, false, true, false, false);
    CONTEXT.initStaticRelation("context", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    ISCITIZENOF.initStaticRelation("isCitizenOf", UDomain.PERSON, UDomain.COUNTRY, false, true, false, false);
    MEREONYMY.initStaticRelation("isMereonymOf", UDomain.CLASS, UDomain.CLASS, false, false, false, false);
    MEMBEROF.initStaticRelation("isMemberOf", UDomain.CLASS, UDomain.CLASS, false, false, false, false);
    SUBSTANCEOF.initStaticRelation("isSubstanceOf", UDomain.CLASS, UDomain.CLASS, false, false, false, false);
    PARTOF.initStaticRelation("isPartOf", UDomain.CLASS, UDomain.CLASS, false, false, false, false);
    FOUNDIN.initStaticRelation("foundIn", UDomain.FACT, UDomain.URL, false, false, false, false);
    USING.initStaticRelation("using", UDomain.FACT, UDomain.COMPUTERSYSTEM, false, true, false, false);
    DURING.initStaticRelation("during", UDomain.FACT, UDomain.TIMEINTERVAL, false, true, false, false);
    SINCE.initStaticRelation("since", UDomain.FACT, UDomain.TIMEINTERVAL, false, true, false, false);
    UNTIL.initStaticRelation("until", UDomain.FACT, UDomain.TIMEINTERVAL, false, true, false, false);
    INLANGUAGE.initStaticRelation("inLanguage", UDomain.FACT, UDomain.LANGUAGE, false, true, false, false);
    HASVALUE.initStaticRelation("hasValue", UDomain.QUANTITY, UDomain.NUMBER, false, true, false, false);
    INUNIT.initStaticRelation("inUnit", UDomain.FACT, UDomain.UNIT, false, true, false, false);

    // Data concerning Inventions, by Gjergji
    DISCOVERED.initStaticRelation("discovered", UDomain.PERSON, UDomain.ENTITY, false, false, false, false);
    DISCOVEREDONDATE.initStaticRelation("discoveredOnDate", UDomain.ENTITY, UDomain.DATE, false, true, false, false);

    
    // LUBM benchmarks
    lubm_ADVISOR.initStaticRelation("advisor", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_DOCTORALDEGREEFROM.initStaticRelation("doctoralDegreeFrom", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_EMAIL.initStaticRelation("emailAddress", UDomain.ENTITY, UDomain.WORD, false, false, false, false);
    lubm_HEADOF.initStaticRelation("headOf", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_CHAIR.initStaticRelation("Chair", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_MASTERSDEGREEFROM.initStaticRelation("mastersDegreeFrom", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_MEMDEROF.initStaticRelation("memberOf", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_NAME.initStaticRelation("name", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_PUBLICATIONAUTHOR.initStaticRelation("publicationAuthor", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_RESEARCHINTEREST.initStaticRelation("researchInterest", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_SUBORGANISATIONOF.initStaticRelation("subOrganizationOf", UDomain.ENTITY, UDomain.ENTITY, true, false, false, false);
    lubm_TAKESCOURSE.initStaticRelation("takesCourse", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_TEACHEROF.initStaticRelation("teacherOf", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_TEACHINGASSISTANTOF.initStaticRelation("teachingAssistantOf", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_TELEPHONE.initStaticRelation("telephone", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_TYPE.initStaticRelation("type", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_UNDERGRADUATEDEGREEFROM.initStaticRelation("undergraduateDegreeFrom", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_DEGREEFROM.initStaticRelation("degreeFrom", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_HASALUMNUS.initStaticRelation("hasAlumnus", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_WORKSFOR.initStaticRelation("worksFor", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_ALLMEMBERSOF.initStaticRelation("All members", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
    lubm_STUDENT.initStaticRelation("student", UDomain.ENTITY, UDomain.ENTITY, false, false, false, false);
  	
  	
  	// Gazetteer data
    gaz_ISLOCATEDIN.initStaticRelation("gaz_isLocatedIn", UDomain.CITY, UDomain.GEOPOLITICAL, true, true, false, false);
    gaz_HASNAME.initStaticRelation("gaz_hasName", UDomain.IDENTIFIER, UDomain.STRING, false, true, false, false);
    gaz_HASLATITUDE.initStaticRelation("gaz_hasLatitude", UDomain.IDENTIFIER, UDomain.NUMBER, false, true, false, false);
    gaz_HASLONGITUDE.initStaticRelation("gaz_hasLongitude", UDomain.IDENTIFIER, UDomain.NUMBER, false, true, false, false);

    // Spatial predicate(s) (may have third argument)
    gaz_ISCLOSE.initStaticRelation("gaz_isClose", UDomain.IDENTIFIER, UDomain.NUMBER, false, false, false, true);
    ISCLOSE.initStaticRelation("isClose", UDomain.IDENTIFIER, UDomain.NUMBER, false, false, false, true);

  }

 
  /** The empty constructor for the URelation instance. */
  public function URelation() {
    super();
  }

  /**
   * The constructor that initializes the URelation object.
   * 
   * @param name the name for the URelation object.
   * */
  /*public function URelation(name:String) {
    this.init(name, null, null, null, false, false, false);
  }*/

  /**
   * The constructor that initializes the URelation object.
   * 
   * @param name the name for the URelation object.
   * @param domain the domain for the URelation object.
   * @param range the range for the URelation object.
   * @param isTransitive the flag that indicates whether this URelation instance is transitive or not.
   * @param isFunction the flag that indicates whether this URelation instance is a function or not.
   * @param isArithmetic the flag that indicates whether this URelation instance is arithmetic or not.
   * */
  /*public function URelation(name:String, domain:UDomain, range:UDomain, isTransitive:Boolean, isFunction:Boolean, isArithmetic:Boolean) {
    this.init(name, domain, range, null, isTransitive, isFunction, isArithmetic);
  }*/

  /**
   * The constructor that initializes the URelation object.
   * 
   * @param name the name for the URelation object.
   * @param domain the domain for the URelation object.
   * @param range the range for the URelation object.
   * @param superURelation the super-relation for the URelation object.
   * @param isTransitive the flag that indicates whether this URelation instance is transitive or not.
   * @param isFunction the flag that indicates whether this URelation instance is a function or not.
   * @param isArithmetic the flag that indicates whether this URelation instance is arithmetic or not.
   * */
  /*public function URelation(name:String, domain:UDomain, range:UDomain, superURelation:URelation, isTransitive:Boolean, isFunction:Boolean, isArithmetic:Boolean) {
    this.init(name, domain, range, superURelation, isTransitive, isFunction, isArithmetic);
  }*/

  /**
   * Delivers the URelation object to the given name of a requested relation.
   * <p>
   * In case the requested relation does not exist, the return value is a new URelation object.
   * 
   * @param name the name of the requested URelation object.
   * @return the requested URelation object or a new created one.
   * */
  public static function valueOfRelation(name:String):URelation {
    var relationObject:Object = relations.getValue(name);
    //var relation:URelation = (URelation)(relations.getValue(name));
    //var relation:URelation = (URelation)(relations.getValue(name));
    if (relationObject == null) {
      var relation:URelation = new URelation();//(name);   we do not have such an constructor
      relation.initRelationFull(name, null, null, null, false, false, false, false);
      return relation;
    }
    else
      return relationObject as URelation;
  }

  /**
   * Delivers the URelation object to the given name of a requested relation.
   * <p>
   * In case the requested relation does not exist, a URelation object with the given parameter is built.
   * 
   * @param name the name for the URelation object.
   * @param domain the domain for the URelation object.
   * @param range the range for the URelation object.
   * @param superURelation the super-relation for the URelation object.
   * @param isTransitive the flag that indicates whether this URelation instance is transitive or not.
   * @param isFunction the flag that indicates whether this URelation instance is a function or not.
   * @param isArithmetic the flag that indicates whether this URelation instance is arithmetic or not.
   * @return the requested URelation object or the recently built one.
   * */
  public static function valueOfRelationFull(name:String, domain:UDomain, range:UDomain, superURelation:URelation, isTransitive:Boolean, isFunction:Boolean,
      isArithmetic:Boolean, isFunctionCall:Boolean = false):URelation {
    var relation:URelation = (URelation)(relations.getValue(name))
    if (relation == null) {
      relation = new URelation();
      relation.initRelationFull(name, domain, range, superURelation, isTransitive, isFunction, isArithmetic, isFunctionCall);
    }
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
   * @param superURelation
   *          the super URelation for the URelation.
   * */
  public function initRelation(name:String, domain:UDomain, range:UDomain, superURelation:URelation):void {
    super.init(name);
    this.argDomain = domain;
    this.argRange = range;
    this.superURelation = superURelation;
    relations.put(name, this);
  }
  
  /**
   * The constructor that initializes the URelation object.
   * 
   * @param name the name for the URelation object.
   * @param domain the domain for the URelation object.
   * @param range the range for the URelation object.
   * @param isTransitive the flag that indicates whether this URelation instance is transitive or not.
   * @param isFunction the flag that indicates whether this URelation instance is a function or not.
   * @param isArithmetic the flag that indicates whether this URelation instance is arithmetic or not.
   * */
  public function initStaticRelation(name:String, domain:UDomain, range:UDomain, isTransitive:Boolean, isFunction:Boolean, isArithmetic:Boolean, isFunctionCall:Boolean = false):void {
    this.initRelationFull(name, domain, range, null, isTransitive, isFunction, isArithmetic, isFunctionCall);
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
   * @param superURelation
   *          the super URelation for the URelation.
   * @param variable
   *          the flag that indicates if this rule is a real rule instance or just a free variable.
   * */
  /*public function initRelation(name:String, domain:UDomain, range:UDomain, superURelation:URelation, variable:Boolean):void {
    super.init(name);
    this.argDomain = domain;
    this.argRange = range;
    this.superURelation = superURelation;
    relations.put(name, this);
  }*/

  /**
   * Initializes the URelation object.
   * 
   * @param name the name for the URelation object.
   * @param domain the domain for the URelation object.
   * @param range the range for the URelation object.
   * @param superURelation the super-relation for the URelation object.
   * @param isTransitive the flag that indicates whether this URelation instance is transitive or not.
   * @param isFunction the flag that indicates whether this URelation instance is a function or not.
   * @param isArithmetic the flag that indicates whether this URelation instance is arithmetic or not.
   * @param isFunctionCall the flag that indicates whether this URelation instance is a function call or not.
   * */
  public function initRelationFull(name:String, domain:UDomain, range:UDomain, superURelation:URelation, isTransitive:Boolean, isFunction:Boolean, isArithmetic:Boolean, isFunctionCall:Boolean = false):void {
    super.init("",name);
    this.argDomain = domain;
    this.argRange = range;
    this.superURelation = superURelation;
    this.transitive = isTransitive;
    this._function = isFunction;
    this.arithmetic = isArithmetic;
    this._functionCall = isFunctionCall;
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
  /*@Override
  public void setName(String name) {
      // here was the failure
	  //this.setName(name);
	  super.setName(name);
  }*/

  /**
   * Delivers the domain reference of the URelation object.
   * 
   * @return the domain of the URelation object.
   */
  public function getArgDomain():UDomain {
    return argDomain;
  }

  /**
   * Sets the domain reference for the URelation object.
   * 
   * @param domain
   *          the domain for the URelation object.
   */
  public function setArgDomain(domain:UDomain):void {
    this.argDomain = domain;
  }

  /**
   * Delivers the range reference of the URelation object.
   * 
   * @return the range of the URelation object.
   */
  public function getArgRange():UDomain {
    return argRange;
  }

  /**
   * Sets the range reference for the URelation object.
   * 
   * @param range
   *          the range for the URelation object.
   */
  public function setArgRange(range:UDomain):void {
    this.argRange = range;
  }

  /**
   * Delivers the super (father) URelation of the URelation object.
   * 
   * @return the super URelation of the URelation object.
   */
  public function getSuperURelation():URelation {
    return superURelation;
  }

  /**
   * Sets the super (father) URelation for the URelation object.
   * 
   * @param superURelation
   *          the super URelation for the URelation object.
   */
  public function setSuperURelation(superURelation:URelation):void {
    this.superURelation = superURelation;
  }

  /**
   * Checks whether this URelation object is transitive or not.
   * 
   * @return true, if the URelation object is transitive, false otherwise.
   */
  public function isTransitive():Boolean {
    return transitive;
  }

  /**
   * Sets the flag that indicates if the URelation object is transitive or not.
   * 
   * @param transitive
   *          the transitivity flag for the URelation object.
   */
  public function setTransitive(transitive:Boolean):void {
    this.transitive = transitive;
  }

  /**
   * Checks whether this URelation object is arithmetic or not.
   * 
   * @return true, if the URelation object is arithmetic, false otherwise.
   */
  public function isArithmetic():Boolean {
    return arithmetic;
  }

  /**
   * Sets the flag that indicates if the URelation object is transitive or not.
   * 
   * @param arithmetic
   *          the arithmetic flag for the URelation object.
   */
  public function setArithmetic(arithmetic:Boolean):void {
    this.arithmetic = arithmetic;
  }
  
  /**
   * Checks whether this URelation object is a function call or not.
   * 
   * @return true, if the URelation object is a function call, false otherwise.
   */
  public function isFunctionCall():Boolean {
    return _functionCall;
  }
  
  /**
   * Checks whether this URelation object is a function call or not.
   * 
   * @return true, if the URelation object is a function call, false otherwise.
   */
  public function setFunctionCall(functionCall:Boolean):void {
     _functionCall = functionCall;
  }

  /**
   * Checks whether this URelation object is a function or not.
   * 
   * @return true, if the URelation object is a function, false otherwise.
   */
  public function isFunction():Boolean {
    return _function;
  }

  /**
   * Sets the flag that indicates if the URelation object is a function or not.
   * 
   * @param function
   *          the function flag for the URelation object.
   */
  public function setFunction(_function:Boolean):void {
    this._function = _function; 
  }

  /**
   * Checks whether this URelation object is reflexive or not.
   * 
   * @return true, if the URelation object is reflexive, false otherwise.
   */
  public function isReflexive():Boolean {
    return reflexive;
  }

  /**
   * Sets the flag that indicates if the URelation object is reflexive or not.
   * 
   * @param reflexive
   *          the reflexivity flag for the URelation object.
   */
  public function setReflexive(reflexive:Boolean):void {
    this.reflexive = reflexive;
  }

  /**
   * Checks whether this URelation object is symmetric or not.
   * 
   * @return true, if the URelation object is symmetric, false otherwise.
   */
  public function isSymmetric():Boolean {
    return symmetric;
  }

  /**
   * Sets the flag that indicates if the URelation object is symmetric or not.
   * 
   * @param symmetric
   *          the symmetry flag for the URelation object.
   */
  public function setSymmetric(symmetric:Boolean):void {
    this.symmetric = symmetric;
  }

  /**
   * Returns the string representation of all the used URelation objects within the URDF framework.
   * 
   * @return the string representation.
   * */
  public function printURelations():String {

    var uRelations:String = "";

    uRelations += "[";
    for (var rel:String in relations.getMap())
       uRelations += rel + ",";
    uRelations += "]";

    return ("used URelations: " + uRelations);
  }

  /**
   * Delivers the internal map of used URelation objects within the reasoner.
   * 
   * @return the map of used URelation objects.
   */
  //public Map<String, URelation> getRelations() {
  public function getRelations():UStringHashMap {
    //if (relations == null)
      //loadURelationsList(URelationS_FILE);
    return relations;
  }

 /**
   * Checks whether this object is a URelation object or a variable..
   * 
   * @return true, if this object is a URelation object, false if it is a variable.
   * */
  override public function isRelation():Boolean {
    return true;
  }

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
/*@Override
public String toString() {
	return "URelation [argDomain=" + argDomain + ", argRange=" + argRange
			+ ", arithmetic=" + arithmetic + ", function=" + function
			+ ", reflexive=" + reflexive + ", superURelation=" + superURelation
			+ ", symmetric=" + symmetric + ", transitive=" + transitive + "]";
}*/

  /**
   * Delivers all the used relations as a bindable ArrayCollection instance. 
   * 
   * @return all the managed and used relations as strings.
   * */
  public static function getRelations():ArrayCollection {
  	  var rel:ArrayCollection = relations.keyArray();
  	  
  	  // add the question mark
  	  //rel.addItem("?");
  	  
  	  var relationsSortField:SortField = new SortField();
      //relationsSortField.name = "relations";
      relationsSortField.caseInsensitive = true;
      //relationsSortField.descending = true;

      var relationsSort:Sort = new Sort();
      relationsSort.fields = [relationsSortField];

      rel.sort = relationsSort;
      rel.refresh();
      

  	  return rel;
  }

  /**
   * Returns a string representation of the URelation object.
   * 
   * @return the string representation of the URelation object.
   */
  override public function toString():String {
    return getName(); //super.toString() + " URelation [argDomain = " + argDomain + ", argRange = " + argRange + ((superURelation != null) ? (", superURelation = " + superURelation.getName() + "]") : "]");
  }
  
  /** 
   * Reads in the serialized data from the mapped URelation Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);//if this.isVariable() then serialize less
	argDomain = (UDomain)(input.readObject());
	argRange = (UDomain)(input.readObject());
	superURelation = (URelation)(input.readObject());
	transitive = input.readBoolean();
	arithmetic = input.readBoolean();
	_function = input.readBoolean();
	reflexive = input.readBoolean();
	symmetric = input.readBoolean();
	_functionCall = input.readBoolean();
	/*var name:String = super.getName();
	var constant:Boolean = UStringUtil.startsWith(name,"?");
	// we do not have to serialize all the data of a relation, just use the static ones defined in ActionScript
	if(constant) {
		var relation:URelation = URelation.valueOf(name);
		argDomain = relation.getDomain();
	    argRange = relation.getArgRange();
	    superURelation = relation.getSuperURelation();
	    transitive = relation.isTransitive;
	    _function = relation.isFunction;
	    arithmetic = relation.isArithmetic;
	    symmetric = relation.isSymmetric();
	}*/
  }

  /**
   * Writes out the data to serialize to the mapped URelation Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
	output.writeObject(argDomain);
	output.writeObject(argRange);
	output.writeObject(superURelation);
	output.writeBoolean(transitive);
	output.writeBoolean(arithmetic);
	output.writeBoolean(_function);
	output.writeBoolean(reflexive);
	output.writeBoolean(symmetric);
	output.writeBoolean(_functionCall);
  }	
  
}

}