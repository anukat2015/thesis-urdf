/**
 * 
 */
package urdf.api {
	
import flash.utils.IDataInput;
import flash.utils.IDataOutput;	
//import flash.utils.IExternalizable;

import urdf.api.UObject;
import util.UStringHashMap;
import util.UStringUtil;
	
/**
 * This class represents a domain object which specifies the domain or the range of a relation.
 * 
 * @author Timm Meiser
 * @since 07.01.10
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.UDomain")]
public class UDomain extends UObject {

  /** The list of used UDomain objects within the reasoning engine. */
  //private static Map<String, UDomain> domains = new HashMap<String, UDomain>();
  private static var domains:UStringHashMap = new UStringHashMap();	

  public static const DEFAULT_DOMAIN:UDomain = new UDomain();//"General domain.", "DEFAULT_DOMAIN");

  // top-level
  public static const ENTITY:UDomain = new UDomain();//"General entity domain.", "ENTITY");
  public static const LITERAL:UDomain = new UDomain();//"General literal domains.", "LITERAL");
  public static const LANGUAGE:UDomain = new UDomain();//"General language domain.", "LANGUAGE");

  // Reasoning about facts and relations, to be refined
  public static const FACT:UDomain = new UDomain();//"Fact domain.", "FACT");
  public static const RELATION:UDomain = new UDomain();//"Relation domain.", "RELATION");
  public static const CLASS:UDomain = new UDomain();//"Class domain.", "CLASS", RELATION);

  // entities
  public static const GEOPOLITICAL:UDomain = new UDomain();//"Geopolitical domain.", "GEOPOLITICAL", ENTITY);
  public static const AREA:UDomain = new UDomain();//"Area domain.", "AREA", LITERAL);
  public static const DENSITYPERAREA:UDomain = new UDomain();//"Density type area domain.", "DENSITYPERAREA", LITERAL);
  public static const REGION:UDomain = new UDomain();//"geopolitical region domain.", "REGION", GEOPOLITICAL);
  public static const CITY:UDomain = new UDomain();//"City domain.", "CITY", REGION);
  public static const COUNTRY:UDomain = new UDomain();//"Country domain.", "Country", REGION);
  public static const LEGALINSTITUTION:UDomain = new UDomain();//"Legal institution domain.", "LEGALINSTITUTION", GEOPOLITICAL);
  public static const UNIVERSITY:UDomain = new UDomain();//"University domain.", "UNIVERSITY", LEGALINSTITUTION);
  public static const MOVIE:UDomain = new UDomain();//"Movie domain.", "MOVIE", ENTITY);
  public static const BOOK:UDomain = new UDomain();//"Book domain.", "BOOK", ENTITY);
  public static const COMPUTERSYSTEM:UDomain = new UDomain();//"Computer system domain.", "COMPUTERSYSTEM", ENTITY);

  // people
  public static const PERSON:UDomain = new UDomain();//"Person domain.", "PERSON", ENTITY);
  public static const LEGALACTOR:UDomain = new UDomain();//"Legal actor/person domain.", "LEGALACTOR", ENTITY);
  public static const LEGALACTORGEO:UDomain = new UDomain();//"Legal actor/person geographic domain.", "LEGALACTORGEO", ENTITY);
  public static const ACTOR:UDomain = new UDomain();//"Movie actor domain.", "ACTOR", PERSON);
  public static const POLITICIAN:UDomain = new UDomain();//"Politician domain.", "POLITICIAN", PERSON);

  // literals
  public static const QUANTITY:UDomain = new UDomain();//"Quantity domain.", "QUANTITY", LITERAL);
  public static const UNIT:UDomain = new UDomain();//"Unit domain.", "UNIT", LITERAL);
  public static const DATE:UDomain = new UDomain();//"Date domain.", "DATE", LITERAL);
  public static const TIMEINTERVAL:UDomain = new UDomain();//"Time interval domain.", "TIMEINTERVAL", LITERAL);
  public static const NUMBER:UDomain = new UDomain();//"Number domain.", "NUMBER", LITERAL);
  public static const DURATION:UDomain = new UDomain();//"Duration domain.", "DURATION", LITERAL);
  public static const INTEGER:UDomain = new UDomain();//"Integer domain.", "INTEGER", NUMBER);
  public static const NONNEGATIVEINTEGER:UDomain = new UDomain();//"Nonnegative integer domain.", "NONNEGATIVEINTEGER", INTEGER);
  public static const URL:UDomain = new UDomain();//"URL domain.", "URL", LITERAL);
  public static const IDENTIFIER:UDomain = new UDomain();//"Unique identifier domain.", "IDENTIFIER", LITERAL);
  public static const STRING:UDomain = new UDomain();//"String domain.", "STRING", LITERAL);
  public static const MONETARYVALUE:UDomain = new UDomain();//"Monetary value domain.", "MONETARYVALUE", LITERAL);
  public static const PRIZE:UDomain = new UDomain();//"Prize domain.", "PRIZE", MONETARYVALUE);

  // language
  public static const WORD:UDomain = new UDomain();//"Word domain.", "WORD", LANGUAGE);

  /** The super domain of this UDomain object. */
  private var superDomain:UDomain;

  /* The static initializer is needed in order to use the init methods for the UDomain objects. That`s because
   * we only have one constructor, the empty default constructor. */
  { 
	  
	DEFAULT_DOMAIN.initDomain("General domain.", "DEFAULT_DOMAIN", null);

    // top-level
    ENTITY.initDomain("General entity domain.", "ENTITY", null);
    LITERAL.initDomain("General literal domains.", "LITERAL", null);
    LANGUAGE.initDomain("General language domain.", "LANGUAGE", null);

    // Reasoning about facts and relations, to be refined
    FACT.initDomain("Fact domain.", "FACT", null);
    RELATION.initDomain("Relation domain.", "RELATION", null);
    CLASS.initDomain("Class domain.", "CLASS", RELATION);

    // entities
    GEOPOLITICAL.initDomain("Geopolitical domain.", "GEOPOLITICAL", ENTITY);
    AREA.initDomain("Area domain.", "AREA", LITERAL);
    DENSITYPERAREA.initDomain("Density type area domain.", "DENSITYPERAREA", LITERAL);
    REGION.initDomain("geopolitical region domain.", "REGION", GEOPOLITICAL);
    CITY.initDomain("City domain.", "CITY", REGION);
    COUNTRY.initDomain("Country domain.", "Country", REGION);
    LEGALINSTITUTION.initDomain("Legal institution domain.", "LEGALINSTITUTION", GEOPOLITICAL);
    UNIVERSITY.initDomain("University domain.", "UNIVERSITY", LEGALINSTITUTION);
    MOVIE.initDomain("Movie domain.", "MOVIE", ENTITY);
    BOOK.initDomain("Book domain.", "BOOK", ENTITY);
    COMPUTERSYSTEM.initDomain("Computer system domain.", "COMPUTERSYSTEM", ENTITY);

    // people
    PERSON.initDomain("Person domain.", "PERSON", ENTITY);
    LEGALACTOR.initDomain("Legal actor/person domain.", "LEGALACTOR", ENTITY);
    LEGALACTORGEO.initDomain("Legal actor/person geographic domain.", "LEGALACTORGEO", ENTITY);
    ACTOR.initDomain("Movie actor domain.", "ACTOR", PERSON);
    POLITICIAN.initDomain("Politician domain.", "POLITICIAN", PERSON);

    // literals
    QUANTITY.initDomain("Quantity domain.", "QUANTITY", LITERAL);
    UNIT.initDomain("Unit domain.", "UNIT", LITERAL);
    DATE.initDomain("Date domain.", "DATE", LITERAL);
    TIMEINTERVAL.initDomain("Time interval domain.", "TIMEINTERVAL", LITERAL);
    NUMBER.initDomain("Number domain.", "NUMBER", LITERAL);
    DURATION.initDomain("Duration domain.", "DURATION", LITERAL);
    INTEGER.initDomain("Integer domain.", "INTEGER", NUMBER);
    NONNEGATIVEINTEGER.initDomain("Nonnegative integer domain.", "NONNEGATIVEINTEGER", INTEGER);
    URL.initDomain("URL domain.", "URL", LITERAL);
    IDENTIFIER.initDomain("Unique identifier domain.", "IDENTIFIER", LITERAL);
    STRING.initDomain("String domain.", "STRING", LITERAL);
    MONETARYVALUE.initDomain("Monetary value domain.", "MONETARYVALUE", LITERAL);
    PRIZE.initDomain("Prize domain.", "PRIZE", MONETARYVALUE);

    // language
    WORD.initDomain("Word domain.", "WORD", LANGUAGE); 
		    
  }
  
  /** The constructor of the UDomain object. */
  public function UDomain() {
    super();
  }

  /**
   * Initializes the UDomain object by setting the necessary attribute values.
   * 
   * @param name
   *          the domain name for the UDomain object.
   * @param superDomain 
   *          the super domain for this UDomain object.
   * */
  public function initDomain(name:String, superDomain:UDomain):void {
    super.init(name);
    this.superDomain = superDomain;
    domains.put(name, this);
  }
  
  /**
   * Delivers the super-domain of this UDomain object.
   * 
   * @return the super-domain of the UDomain object.
   */
  public function getSuperDomain():UDomain {
	return superDomain;
  }

  /**
   * Sets the super-domain for this UDomain object.
   * 
   * @param superDomain the super-domain for the UDomain object.
   */
  public function setSuperDomain(superDomain:UDomain):void {
	this.superDomain = superDomain;
  }

  /**
   * Checks whether this UDomain object is a sub-domain of the given UDomain object.
   * 
   * @param domain
   *          the UDomain object to check for being a super-domain.
   * @return true, if this UDomain object is a sub-domain of the given UDomain object, false otherwise.
   * */
  public function isSubDomainOf(domain:UDomain):Boolean {
    return this.superDomain == null || domain == null || (this.superDomain.equals(domain) || this.superDomain.isSubDomainOf(domain));
  }

  /**
   * Checks whether this UDomain object is a super-domain of the given UDomain object.
   * 
   * @param domain
   *          the UDomain object to check for being a sub-domain.
   * @return true, if this UDomain object is a super-domain of the given UDomain object, false otherwise.
   * */
  public function isSuperDomainOf(domain:UDomain):Boolean {
    return domain.superDomain == null || domain == null || (domain.superDomain.equals(this) || domain.superDomain.isSuperDomainOf(this));
  }

  /**
   * Delivers the UDomain object to the given name of a requested domain.
   * <p>
   * In case the requested domain does not exist, a new UDomain object with the given name is built.
   * 
   * @param name the name of the requested UDomain object.
   * @return the requested UDomain object or a new UDomain object.
   * */
  public static function valueOfDomain(name:String):UDomain {
    var domain:UDomain = (UDomain)(domains.getValue(name));
    if (domain == null) {
      domain = new UDomain();//(name);   we do not have such an constructor
      domain.initDomain("",name,null);
    }
    return domain;
  }
  
  /**
   * Delivers the UDomain object to the given name of a requested domain.
   * <p>
   * In case the requested domain does not exist, a UDomain object with the given parameter is built.
   * 
   * @param name the name for the UDomain object.
   * @param superDomain the super-domain for the UDomain object.
   * @return the requested UDomain object or the recently built one.
   * */
  public static function valueOfDomainFull(name:String, superDomain:UDomain):UDomain {
    var domain:UDomain = (UDomain)(domains.getValue(name));
    if (domain == null) {
      domain = new UDomain();//(name);   we do not have such an constructor
      domain.initDomain(name, superDomain);
    }
    return domain;
  }

  /**
   * Returns a string representation of the UDomain object.
   * 
   * @return the string representation of the UDomain object.
   */
  override public function toString():String { 
    return "Domain : " + super.getName() + " , SuperDomain : " + (this.superDomain == null ? "null" : this.superDomain.getName());
  }
  
  /** 
   * Reads in the serialized data from the mapped UDomain Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);

	var dom:String = input.readUTF();
	
	if(dom.charAt() == "0")
	  return;
		
	if(UStringUtil.startsWith(dom,"?")) {
	  superDomain = new UDomain();
	  superDomain.init("",dom);
	}
	else 
	  superDomain = valueOfDomain(dom); // return the suitable static UDomain object 
  }

  /**
   * Writes out the data to serialize to the mapped UDomain Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
	if(superDomain == null)
		  output.writeUTF("0"); 
	  else
		  output.writeUTF(superDomain.getName()); // only write the name of the UDomain object
  }	
  
}

}