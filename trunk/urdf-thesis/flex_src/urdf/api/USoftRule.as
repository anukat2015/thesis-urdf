/**
 * 
 */
package urdf.api {

import flash.utils.IDataInput;
import flash.utils.IDataOutput;	

/**
 * This class represents a soft rule that is either specified by the user via the user interface of URDF or part of the internal rule storage.
 * <p>
 * The soft rules are needed within the reasoner to infer facts that are not directly stored in the database.
 * <p>
 * A soft rule is a conjunction of literals. There is a head of the rule (a single literal) and a body of several literals.
 * 
 * @author Timm Meiser
 * @since 18.01.10
 * @version 1.0
 * 
 */
[Bindable]
[RemoteClass(alias="urdf.api.USoftRule")]
public class USoftRule extends URule {

  /** The head of the soft rule. At least one of the head arguments should occur in the rule body as well. */
  private var head:ULiteral = new ULiteral();

  /** The weight for constructing a horn clause. The weight reflects the confidence in the correctness of the rule. */
  private var weight:Number = 0;

  /** The empty default constructor for the USoftRule object. */
  public function USoftRule() {
    super();
  }

  /**
   * Delivers the head literal of this USoftRule object.
   * 
   * @return the literal (atom) that represents the head of this USoftRule object.
   */
  public function getHead():ULiteral {
    return head;
  }

  /**
   * Sets the head literal of the USoftRule object and automatically checks if at least one of the head literal arguments also occurs in the body of the rule.
   * <p>
   * If none of the arguments of the head literal occurs in the rule body as well ==> an error message is send back to caller of this method.
   * <p>
   * This method is the preferred one for the initialization of the USoftRule object, because all the needed checks-ups are done here.
   * 
   * @param name
   *          the name for the USoftRule object.
   * @param head
   *          the literal that should represent the head of this soft-rule.
   * @param body
   *          the list of literals to override the internal one.
   * @throws Error
   */
  public function initSoftRule(name:String, head:ULiteral, body:Array):void {
  
    super.initRule(name, body);

    var arg1:UArgument= head.getFirstArgument();
    var arg2:UArgument= head.getSecondArgument();

    // Domain restriction for variables and constants
    var isFirstArgVariable:Boolean= arg1.isVariable();
    var isSecondArgVariable:Boolean= arg2.isVariable();

    if (isFirstArgVariable && !this.getVariables().hasOwnProperty(arg1.getId()))
      throw new Error("HEAD VARIABLE '" + arg1.getName() + "' MUST BE IN BODY OF CLAUSE: " + this);
    else if (isSecondArgVariable && !this.getVariables().hasOwnProperty(arg2.getId()))
      throw new Error("HEAD VARIABLE '" + arg2.getName() + "' MUST BE IN BODY OF CLAUSE: " + this);
    else if (!isFirstArgVariable && !this.getConstants().hasOwnProperty(arg1.getId()))
      throw new Error("HEAD CONSTANT '" + arg1.getName() + "' MUST BE IN BODY OF CLAUSE: " + this);
    else if (!isSecondArgVariable && !this.getConstants().hasOwnProperty(arg2.getId()))
      throw new Error("HEAD CONSTANT '" + arg2.getName() + "' MUST BE IN BODY OF CLAUSE: " + this);
    else if (!checkChaining(new Object(), 0))
      throw new Error("ATOMS NOT PROPERLY CHAINED: " + this); // check proper variable chaining for atoms


    this.head = head;
    // this.ComputeWeight();
  }

  /**
   * Sets the head literal of the USoftRule object and automatically checks if at least one of the head literal arguments also occurs in the body of the rule.
   * <p>
   * If none of the arguments of the head literal occurs in the rule body as well ==> an error message is send back to caller of this method.
   * <p>
   * This method is the preferred one for the initialization of the USoftRule object, because all the needed checks-ups are done here.
   * 
   * @param name
   *          the name for the USoftRule object.
   * @param head
   *          the literal that should represent the head for this soft-rule.
   * @param weight
   *          the a priori calculated weight of the soft-rule.
   * @param body
   *          the list of literals to override the internal list.
   * @throws Error
   */
  public function initSoftRuleFull(name:String, head:ULiteral, weight:Number, body:Array):void {
  
    super.initRule(name, body);

    var arg1:UArgument= head.getFirstArgument();
    var arg2:UArgument= head.getSecondArgument();

    // Domain restriction for variables and constants
    var isFirstArgVariable:Boolean= arg1.isVariable();
    var isSecondArgVariable:Boolean= arg2.isVariable();

    if (isFirstArgVariable && !this.getVariables().hasOwnProperty(arg1.getId()))
      throw new Error("Head variable '" + arg1.getName() + "' must be in body of clause: " + this);
    else if (isSecondArgVariable && !this.getVariables().hasOwnProperty(arg2.getId()))
      throw new Error("Head variable '" + arg2.getName() + "' must be in body of clause: " + this);
    else if (!isFirstArgVariable && !this.getConstants().hasOwnProperty(arg1.getId()))
      throw new Error("Head constant '" + arg1.getName() + "' must be in body of clause: " + this);
    else if (!isSecondArgVariable && !this.getConstants().hasOwnProperty(arg2.getId()))
      throw new Error("Head constant '" + arg2.getName() + "' must be in body of clause: " + this);

    this.head = head;
    this.weight = weight;
  }

  /**
   * Returns the weight of (belief in) the soft-rule.
   * 
   * @return the weight (computed belief in the rule) of this soft-rule
   */
  public function getWeight():Number {
    return weight;
  }

  /**
   * Sets the weight of the soft-rule.
   * 
   * @param weight
   *          the weight to set
   */
  public function setWeight(weight:Number):void {
    this.weight = weight;
    //trace("SETWEIGHT2 " + weight);
  }

  /**
   * Returns the string representation of the whole USoftRule object (all values within the object).
   * 
   * @return the string representation of the USoftRule object.
   */
  /*
  override public function toString():String {
    var str:String = head.toString() + " <= ";
    var i:int = 0;
    for each (var literal:ULiteral in this.getLiterals()) {
      str += literal.toString() + (i < this.size() - 1 ? ", " : "");
      i++;
    }
    return str;// + " , constants : " + this.getConstants().toString() 
               //+ " , variables : " + this.getVariables().toString();
  }
  */
  override public function toString():String {
    //var str:String = head.toString() + " <= ";
    var str:String = head.toString();// + "</br>";
    str += size() > 0 ? " <b>&lt;=</b> " : "";
    var i:int = 0;
    for each (var literal:ULiteral in this.getLiterals()) {
      str += literal.toString() + (i < this.size() - 1 ? " ^ " : "<br/>");
      i++;
    }
    //return str + " @ [" + this.weight.toFixed(2) + "]";
    return str + "<b>confidence: </b>" + this.weight.toFixed(2);
  }
  
  /** 
   * Reads in the serialized data from the mapped USoftRule Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
	head = (ULiteral)(input.readObject());
	weight = input.readDouble();

  }

  /**
   * Writes out the data to serialize to the mapped USoftRule Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
	output.writeObject(head);
	output.writeDouble(weight);
  }	
  
}

}