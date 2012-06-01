/**
 * 
 */
package urdf.api {
	
import urdf.api.UFactSet;	
	
import flash.utils.IDataInput;
import flash.utils.IDataOutput;


/**
 * This class represents a grounded soft-rule inferred by the reasoner.
 * 
 * @author Timm Meiser
 * @since 19.01.10
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.UGroundedSoftRule")] 
public class UGroundedSoftRule extends UFactSet {

  /** The head fact that is inferred through grounding the used ungrounded soft-rule. */
  private var head:UFact;

  /** A reference to the soft rule that produced this UGroundedSoftRule object. */
  private var softRule:USoftRule = null;

  /** The empty default constructor for the UGroundedSoftRule object. */
  public function UGroundedSoftRule() {
    super();
  }

  /**
   * Initializes this UGroundedSoftRule object by adding all facts of the given fact-set as implicitly negative facts and the given single fact as an implicitly
   * positive one (this is the head of the UGroundedSoftRule object).
   * <p>
   * The given USoftRule objects acts as the source for this UGroundedSoftRule object (a grounded version of the specified soft-rule).
   * 
   * @param factSet
   *          the fact-set whose facts act as negative inputs to this UGroundedSoftRule object.
   * @param head
   *          the given fact that acts as the positive head of this UGroundedSoftRule object.
   * @param softRule
   *          the soft rule that is the source of this grounded soft rule.
   * */
  public function initGroundedSoftRuleFull(factSet:UFactSet, head:UFact, softRule:USoftRule):void {
    super.initFactSetFull(factSet);
    this.head = head;
    this.softRule = softRule;
    if (softRule != null) //{// check if the soft rule is accessible
      this.setWeight(softRule.getWeight());
      //this.setName(softRule.getName() + ":" + head.toString());
    //}
    //this.setName("null:" + head.toString());
  }

  /**
   * Initializes this UGroundedSoftRule object by adding all facts of the given fact-set as implicitly negative facts and the given single fact as an implicitly
   * positive one (this is the head of the UGroundedSoftRule object).
   * <p>
   * Internally, the source soft rule is set to null.
   * 
   * @param factSet
   *          the fact-set whose facts act as negative inputs to this UGroundedSoftRule object.
   * @param head
   *          the given fact that acts as the positive head of this UGroundedSoftRule object.
   * */
  public function initGroundedSoftRule(factSet:UFactSet, head:UFact):void {
    this.initGroundedSoftRuleFull(factSet, head, null);
  }
  
  /**
   * Sets the name for the UGroundedSoftRule instance.
   * 
   * @param name
   *          the name for the UGroundedSoftRule instance.
   */
  override public function setName(name:String):void {
    this.name = name;
    this.id = hashCode();//trace("name = " + name);
  }

  /**
   * Delivers the head of the grounded soft-rule (UGroundedSoftRule object).
   * 
   * @return the head of the soft-rule.
   */
  public function getHead():UFact {
    return head;
  }

  /**
   * Sets the head for the grounded soft-rule (UGroundedSoftRule object).
   * 
   * @param head
   *          the head of the soft-rule.
   */
  public function setHead(head:UFact):void {
    this.head = head;
  }

  /**
   * Delivers the soft-rule that produced to this signed fact set.
   * 
   * @return the softRule that was used to produce this grounded soft-rule.
   */
  public function getSoftRule():USoftRule {
    return this.softRule;
  }

  /**
   * Sets the soft-rule that produced this grounded soft-rule.
   * 
   * @param softRule
   *          the softRule that was used to produce this grounded soft-rule.
   */
  public function setSoftRule(softRule:USoftRule):void {
    this.softRule = softRule;
  }

  /**
   * Checks whether this UGroundedSoftRule object equals the given UGroundedSoftRule object or not.
   * 
   * @return true, if this UGroundedSoftRule object equals the given UGroundedSoftRule object, false otherwise.
   */
  override public function equals(object:UObject):Boolean {
    
    if (this == object) {
      return true;
    }
    if (!super.equals(object)) {
      return false;
    }
    if (!(object is UGroundedSoftRule)) {
      return false;
    }
    
    var groundedSoftRule:UGroundedSoftRule = (UGroundedSoftRule)(object);
    
    return super.equals(object)
        && ((this.getHead() == null && groundedSoftRule.getHead() == null) 
        || (this.getHead() != null && groundedSoftRule.getHead() != null 
        && this.getHead().equals(groundedSoftRule.getHead())));
  }

  /**
   * Returns the string representation of the whole UGroundedSoftRule object (all values within the object).
   * 
   * @return the string representation of the USoftRule object.
   */
  override public function toString():String {
    var s:String = "<b>Head: </b>" + head.toString();// + "</br>";
    //s += (size() > 0 ? "\n<= " : "");
    s += (size() > 0 ? "<br/><b>Body: </b>" : "");
    var i:int = 0;
    for each (var fact:UFact in this.getFactSet()) {
      s += fact + (i < this.size() - 1 ? "\n   , " : "");
      i++;
    }
    return s;
  }
  
  /** 
   * Reads in the serialized data from the mapped UGroundedSoftRule Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
	head = (UFact)(input.readObject()); 
	softRule = (USoftRule)(input.readObject());
  }

  /**
   * Writes out the data to serialize to the mapped UGroundedSoftRule Java class.
   * 
   * @param output the output data to serialize.
   * */
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
	output.writeObject(head);
	output.writeObject(softRule);
  }	
  
}

}