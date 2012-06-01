/**
 * 
 */
package urdf.api {

import flash.utils.IDataInput;
import flash.utils.IDataOutput;

import util.UStringUtil;	
//import flash.utils.IExternalizable;

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
[Bindable]
[RemoteClass(alias="urdf.api.URule")]
//public class URule extends UObject implements Iterable<ULiteral> {
public class URule extends UObject  {

  /** The free variables within the list of literals. */
  //private Set<UArgument> variables;
  private var variables:Object;

  /** The constants within the list of literals. */
  //private Set<UArgument> constants;
  private var constants:Object;

  /** Represents the list of literals. */
  //private List<ULiteral> literals;
  private var literals:Array;
  //private var literals:ArrayCollection;
  
  /** The internal counter for argument numbers. */
  private var counter:int = 0;
  
  /** The flag that indicates if at least one of the literal constants is an entity (arg1 or arg2). */
  private var _hasEntity:Boolean = false;

  /** The empty default constructor for the URule object. */
  public function URule() {
    super();
    //this.literals = new ArrayCollection();
    this.literals = new Array();
    this.variables = new Object();
    this.constants = new Object();
  }

  /**
   * Initializes the URule object.
   * 
   * @param name
   *          the name for the URule object.
   * @param literals
   *          the the list of literals that are used within the URule object.
   * @throws Error
   */
  public function initRule(name:String, literals:Array):void {
    super.init(name);
    this.addAll(literals);
  }
  
  /**
   * Checks whether the query contains at least one entity.
   * This is needed to lower computational effort for URDF.
   * */
  public function hasEntity():Boolean {
  	 return _hasEntity;
  }

  /**
   * Delivers the list of literals within the URule object.
   * 
   * @return the complete list of literals within the URule object.
   */
  //public List<ULiteral> getLiterals() {
  public function getLiterals():Array {
    return literals;
  }

  /**
   * Overrides the internal list of literals.
   * 
   * @param literals
   *          the list of literals to replace the previous internal list.
   */
  //public function setLiterals(List<ULiteral> literals):void {
  public function setLiterals(literals:Array):void {
    this.literals = literals;
  }

  /**
   * Adds a literal to the internal list of literals.
   * 
   * @param literal
   *          the literal to add to the list of currently stored literals.
   */
  public function addLiteral(literal:ULiteral):void {
    
    this.literals.push(literal);
    
    if (literal.getFirstArgument().isVariable())// if (arg1.startsWith("?"))
      variables[literal.getFirstArgument().hashCode()] = literal.getFirstArgument();// variables.add(arg1);
    else {
      constants[literal.getFirstArgument().hashCode()] = literal.getFirstArgument();// constants.add(arg1);
      _hasEntity = true;
    }

    if (literal.getSecondArgument().isVariable())// if (arg2.startsWith("?"))
      variables[literal.getSecondArgument().hashCode()] = literal.getSecondArgument();// variables.add(arg2);
    else {
      constants[literal.getSecondArgument().hashCode()] = literal.getSecondArgument();// constants.add(arg2);
      _hasEntity = true;
    }

    if (literal.getRelation().isVariable())// if (relationName.startsWith("?"))
      variables[literal.getRelation().hashCode()] = literal.getRelation();// variables.add(relationName);
    else
      constants[literal.getRelation().hashCode()] = literal.getRelation();
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
   * @throws Error 
   */
  public function addLiteralFull(relation:URelation, arg1:UArgument, arg2:UArgument):void {
    var literal:ULiteral= new ULiteral();
    literal.initLiteral(relation, arg1, arg2);
    this.addLiteral(literal);
  }

  /**
   * Adds all literals of the given list of literals to the internal list of literals.
   * 
   * @param literals
   *          the list of literals to add to the list of currently stored literals.
   * @throws Error
   */
  //public function addAll(List<ULiteral> literals):void throws Exception {
  public function addAll(literals:Array):void {
    for each (var literal:ULiteral in literals)
      this.addLiteral(literal);
  }

  /**
   * Swaps the literals at the two specified positions within the internal list of literals.
   * 
   * @param fromIdx
   *          the index of the first literal to swap.
   * @param toIdx
   *          the index of the second literal to swap.
   * @throws Error
   * */
  public function swap(fromIdx:int, toIdx:int):void {
    var tmpTo:ULiteral= (ULiteral)(literals[toIdx]);
    var tmpFrom:ULiteral= (ULiteral)(literals[fromIdx]);
    literals[toIdx] = tmpFrom;
    literals[fromIdx] = tmpTo;
  }

  /**
   * Deletes the literal at position <i> position </i> from the list of internally stored literals.
   * 
   * @param position
   *          the position of the literal to remove.
   * @return true, if the literal could be removed, false otherwise.
   */
  public function removeLiteralAtPosition(position:int):Boolean {
    if (position < 0 || position >= this.size())
      return false;

    var literal:ULiteral = (ULiteral)(this.literals[position]);
    this.literals.splice(position,1);

    if (literal.getFirstArgument().isVariable())
      this.variables[literal.getFirstArgument().hashCode()] = null;
    else
      this.constants[literal.getFirstArgument().hashCode()] = null;

    if (literal.getSecondArgument().isVariable())
      this.variables[literal.getSecondArgument().hashCode()] = null;
    else
      this.constants[literal.getSecondArgument().hashCode()] = null;

    if (literal.getRelation().isVariable())
      this.variables[literal.getRelation().hashCode()] = null;
    else
      this.constants[literal.getRelation().hashCode()] = null;

    return true;

  }

  /**
   * Deletes the given literal from the internally stored list of literals, in case the literal exists.
   * 
   * @param literal
   *          the literal to remove.
   * @return true, if the literal could be removed, false otherwise.
   */
  public function removeLiteral(literal:ULiteral):Boolean {
    
    var length:int= this.size();

    for (var i:int = 0; i < length; i++) {

      var tempLiteral:ULiteral = (ULiteral)(this.literals[i]);
    
      if (tempLiteral.equals(literal)) {

        this.literals.splice(i,1);

        if (literal.getFirstArgument().isVariable())
          this.variables[literal.getFirstArgument().hashCode()] = null;
        else
          this.constants[literal.getFirstArgument().hashCode()] = null;

        if (literal.getSecondArgument().isVariable())
          this.variables[literal.getSecondArgument().hashCode()] = null;
        else
          this.constants[literal.getSecondArgument().hashCode()] = null;

        if (literal.getRelation().isVariable())
          this.variables[literal.getRelation().hashCode()] = null;
        else
          this.constants[literal.getRelation().hashCode()] = null;

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
  public function getLiteral(position:int):ULiteral {
    if (position < 0 || position >= this.size())
      return null;
    else
      return (ULiteral)(this.literals[position]);
  }

  /**
   * Returns the string representation of the whole ULiteralSet object (all values within the object).
   * 
   * @return the string representation of the object.
   */
  override public function toString():String {
    return literals.toString();
  }

  /**
   * Returns the number of free variables in this URule object.
   * 
   * @return the number of free variables.
   */
  //public Set<UArgument> getVariables() {
  public function getVariables():Object {
    return this.variables;
  }

  /**
   * Returns the number of constants in this URule object.
   * 
   * @return the number of constants.
   */
  //public Set<UArgument> getConstants() {
  public function getConstants():Object {
    return this.constants;
  }
  
  /**
   * Delivers the number of constants in this URule object.
   * 
   * @return the number of constants.
   * */
  public function numOfConstants():int {
  	counter = 0;
  	for each (var arg:UArgument in constants) {
  		counter++;
  	}
  	return counter;
  }
  
  /**
   * Delivers the number of variables in this URule object.
   * 
   * @return the number of variables.
   * */
  public function numOfVariables():int {
  	counter = 0;
  	for each (var arg:UArgument in variables) {
  		counter++;
  	}
  	return counter;
  }

  /**
   * Checks whether this URule object equals the given URule object or not.
   * 
   * @param object the URule object to check.
   * @return true, if this URule object equals the given URule object, false otherwise.
   * */
  override public function equals(object:UObject):Boolean {
    
    if (this == object) {
      return true;
    }
    if (!super.equals(object)) {
      return false;
    }
    if (!(object is URule)) {
      return false;
    }
    
    return this.getId() == object.getId();
  }

  /**
   * Returns the number of internally stored literals in this URule object.
   * 
   * @return the number of stored literals.
   */
  public function size():int {
    return this.literals.length;//size();
  }
  
  /** 
   * Reads in the serialized data from the mapped URule Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
    var lit:Object = input.readObject();
    for each (var literal:ULiteral in lit)
       this.addLiteral(literal);
  }

  /**
   * Writes out the data to serialize to the mapped URule Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
	output.writeObject(literals);
  }	
  
  protected function checkChaining(literals:Object, idx:int):Boolean {
    var size:int = 0;
    for each (var obj:Object in literals) {
    	size++;
    }
    if (size == this.size())
      return true;

    var matched:Boolean = false;
    for (var i:int = 0; i < this.size(); i++) {
      if (!literals.hasOwnProperty(this.getLiteral(i).getId().toString())
          && (UStringUtil.equals(this.getLiteral(i).getFirstArgumentName(),this.getLiteral(idx).getFirstArgumentName(),true)
              || UStringUtil.equals(this.getLiteral(i).getFirstArgumentName(),this.getLiteral(idx).getSecondArgumentName(),true)
              || UStringUtil.equals(this.getLiteral(i).getSecondArgumentName(),this.getLiteral(idx).getFirstArgumentName(),true)
              || UStringUtil.equals(this.getLiteral(i).getSecondArgumentName(),this.getLiteral(idx).getSecondArgumentName(),true))) {
        literals[this.getLiteral(i).getId().toString()] = this.getLiteral(i);
        matched = matched || checkChaining(literals, i);
      }
      if (matched)
        break;
    }

    return matched;
  }
}

}