/**
 * 
 */
package urdf.api {
	
import urdf.api.URule;
import flash.utils.IDataInput;
import flash.utils.IDataOutput;

/**
 * Represents a competitive (mutually exclusive) set of atomic literals (signs are ignored).
 * <p>
 * By using the constructors and/or the <i> init </i>-methods, it is checked that no arithmetic predicates 
 * (relations) are used. Otherwise, an error message will be thrown.
 * 
 * 
 * @author Timm Meiser
 * @since 18.01.10
 * @version 1.0
 * 
 */
[Bindable]
[RemoteClass(alias="urdf.api.UHardRule")]
public class UHardRule extends URule {

  /** The empty default constructor for the UHardRule object. */
  public function UHardRule() {
    super();
  }

  /**
   * Initializes the UHardRule object.
   * 
   * @param name the name for the UHardRule object.
   * @param literal the first literal to add to the internal list of literals.
   * @throws Error 
   * */
  public function initHardRule(name:String, literal:ULiteral):void {
    var literals:Array = new Array();
    literals.push(literal);
    this.initRule(name, literals);
  }

  /**
   * Initializes the UHardRule object.
   * <p>
   * If the there is at least one <i> arithmetic </i> relation contained in one of the literals of the given list of literals, the method throws an exception
   * 
   * @param name
   *          the name for the hard rule.
   * @param literals
   *          the list of literals to replace the previous list (probably empty).
   * @throws Error
   */
  override public function initRule(name:String, literals:Array):void {
    // iterate over all literals contained in the list
    for each (var literal:ULiteral in literals) {
      // get the relation reference of the current literal
      var relationArgument:UArgument = literal.getRelation();

      // if the reference is null or is not a relation reference (instead a variable reference) 
      // --> skip this part and go on with the next literal
      if (relationArgument != null && relationArgument.isRelation()) {//isRelationArithmetic())

        // cast the relation reference
        var relation:URelation = (URelation)(relationArgument);
        // if the relation is arithmetic --> throw exception, because we do not allow arithmetic relations 
        // within a hard rule.
        if (relation.isArithmetic())
          throw new Error("Arithmetic predicate '" + literal.getRelationName() + "' is not allowed in a hard rule: " + this);
      }
    }

    super.initRule(name, literals);
  }
  
  /**
   * Reads in the serialized data from the mapped UHardRule Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
  }

  /**
   * Writes out the data to serialize to the mapped UHardRule Java class.
   * 
   * @param output the output data to serialize.
   * */	
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
  }	
  
}

}