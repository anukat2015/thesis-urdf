/**
 *
 * 
 */
package urdf.api {
	
import flash.utils.IDataInput;
import flash.utils.IDataOutput;

/**
 * This class represents a query object that gives the user the opportunity to specify his information needs (via the GUI) regarding the reasoning process.
 * 
 * @author Timm Meiser
 * @since 07.01.10
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.UQuery")]
public class UQuery extends URule {

  /** The empty default constructor for the UQuery object. */
  public function UQuery() {
    super();
  }

  /**
   * Initializes the UQuery object and checks that at least one constant is contained in the query.
   * <p>
   * If the there is not at least one <i> constant </i> contained in one of the literals of the given conjunction, the method returns an error message.
   * 
   * @param name
   *          the name for the UQuery object.
   * @param conjunction
   *          the list of conjuncted literals that represent the query terms.
   * @throws Error
   * */
  public function initQuery(name:String, conjunction:Array):void {
    
    super.initRule(name, conjunction);

    // Domain restriction for queries
    
    if (this.numOfConstants() == 0 || !this.hasEntity())
      throw new Error("AT LEAST ONE ENTITY NEEDED PER QUERY: " + this, 0);
    // check proper variable chaining for atoms
    else if (!checkChaining(new Object(), 0))
      throw new Error("ATOMS NOT PROPERLY CHAINED: " + this, 1);
    
  }
  
  override public function toString():String {
    var str:String = "";
    var i:int = 0;
    for each (var literal:ULiteral in this.getLiterals()) {
      str += literal.toString() + (i < this.size() - 1 ? " + " : "");
      i++;
    }
    return str;
    //return literals.toString();
  }
  
  /*
  override public function toString():String {
    var str:String = "";
    var i:int = 0;
    for each (var literal:ULiteral in this.getLiterals()) {
      str += literal.toString() + (i < this.size() - 1 ? "\n" : "");
      i++;
    }
    return str;
    //return literals.toString();
  }
  */
  /**
   * Reads in the serialized data from the mapped UQuery Java class.
   * 
   * @param input the serialized input data.
   * */
  override public function readExternal(input:IDataInput):void{
	super.readExternal(input);
  }

  /**
   * Writes out the data to serialize to the mapped UQuery Java class.
   * 
   * @param output the output data to serialize.
   * */	
  override public function writeExternal(output:IDataOutput):void{
    super.writeExternal(output);
  }	
  
}

}