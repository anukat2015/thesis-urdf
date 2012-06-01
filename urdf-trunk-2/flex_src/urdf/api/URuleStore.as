package urdf.api
{
	
    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
	
	[Bindable]
	[RemoteClass(alias="urdf.api.URuleStore")] 
	public class URuleStore extends UObject
	{

	  /** The list of soft rules to use for the reasoning engine. */
	  private var softRules:Array;

	  /** The list of hard rules to use for the reasoning engine. */
	  private var hardRules:Array;
	  
	  private var softRulesChanged:Boolean = false;
	  private var hardRulesChanged:Boolean = false;
	
	  public function URuleStore() {
	  	 super();
	  	 this.softRules = new Array();
		 this.hardRules = new Array();
	  }
	  
	  public function initRuleStore(softRules:Array, hardRules:Array):void {
		  super.init("Rule Store", "Rule Store");
		  this.softRules = softRules;
		  this.hardRules = hardRules;
	  }
	  
	  public function setSoftRulesChanged(softRulesChanged:Boolean):void {
		  this.softRulesChanged = softRulesChanged;
	  }

	  public function isSoftRulesChanged():Boolean {
		  return softRulesChanged;
	  }
	  
	  public function setHardRulesChanged(hardRulesChanged:Boolean):void {
		  this.hardRulesChanged = hardRulesChanged;
	  }

	  public function isHardRulesChanged():Boolean {
		  return hardRulesChanged;
	  }
	  
	  /**
	   * @return the softRules
	   */
	  public function getSoftRules():Array {
		  return softRules;
	  }

	  /**
	   * @param softRules the softRules to set
	   */
	  public function setSoftRules(softRules:Array):void {
	  	  this.softRules = softRules;
	  }

	  /**
	   * @return the hardRules
	   */
	  public function getHardRules():Array {
		  return hardRules;
	  }

	  /**
	   * @param hardRules the hardRules to set
	   */
	  public function setHardRules(hardRules:Array):void {
		  this.hardRules = hardRules;
	  }

	  /**
	   * Reads in the serialized data from the mapped URuleStore ActionScript class.
	   * 
	   * @param input the serialized input data.
	   * */
	  override public function readExternal(input:IDataInput):void {
		  super.readExternal(input);
		  
		  // get the number of soft rules to deserialize
		  var size:int = input.readInt();
		  //trace("size = " + size);
		  while(size>0) {
			  this.softRules.push(USoftRule(input.readObject()));
			  size--;
		  }
		  // get the number of hard rules to deserialize
		  size = input.readInt();
		  //trace("size = " + size);
		  while(size>0) {
			  this.hardRules.push(UHardRule(input.readObject())); 
			  size--;
		  }  
		  
		  //this.changed = input.readBoolean();
		  //this.softRulesChanged = input.readBoolean();
		  //this.hardRulesChanged = input.readBoolean();
		  
	  }

	  /**
	   * Writes out the data to serialize to the mapped UTriplet ActionScript class.
	   * <p>
	   * We do not serialize the URelation object. This would be too costly. 
	   * Instead, we serialize the name of the relation. This is enough 
	   * information to reconstruct the relation on the client side.
	   * 
	   * @param output the output data to serialize.
	   * */
	  override public function writeExternal(output:IDataOutput):void {
		  super.writeExternal(output);
		  var i:int=0;
		  // get the number of soft rules to deserialize
		  var size:int = this.softRules.length;//size();
		  // send the size value to the client
		  output.writeInt(size);
		  for(i; i<size; i++){
			  output.writeObject(this.softRules[i]);
		  }
		  // get the number of hard rules to deserialize
		  size = this.hardRules.length;//size();
		  // send the size value to the client
		  output.writeInt(size);
		  for(i=0; i<size; i++){
			  output.writeObject(this.hardRules[i]);
		  }
		  
		  output.writeBoolean(this.softRulesChanged);
		  output.writeBoolean(this.hardRulesChanged);
	  }
	
}

}
