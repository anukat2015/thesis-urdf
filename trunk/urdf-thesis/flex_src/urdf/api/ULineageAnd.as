package urdf.api
{
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;	
    
    [Bindable]
    [RemoteClass(alias="urdf.api.ULineageAnd")]
	public class ULineageAnd extends ULineageAbstract 
	{
	
		private var rule:USoftRule;
		private var groundedRule:UGroundedSoftRule;
		
		public function ULineageAnd() {
			super();
			this.groundedRule = null;
		}
		
		public function initLineageAnd(rule:USoftRule):void {
			this.rule = rule;
		}
	
		public function getRule():USoftRule {
			return this.rule;
		}
	
		public function getGroundedRule():UGroundedSoftRule {
			return this.groundedRule;
		}
		
		override public function lineageAvailable():Boolean {
			return (this.getGroundedRule() != null) && (this.rule != null);//this.children.length > 0;
		}
		
		override public function isAndNode():Boolean {
			return true;
		}
		
		override public function isOrNode():Boolean {
			return false;
		}
		
		override public function isAbstractNode():Boolean {
			return false;
		}
	
		override protected  function getDescription():String {
			if (this.groundedRule != null)
				return "AND " + this.groundedRule.toString();
			if (this.rule != null)
				return "AND " + this.rule.toString();
			return "AND";
		}
	
		override protected function getStringAtLevel(level:int):String {
			//if (super.children.isEmpty())
			if (super.children.length == 0)
				return "In Database or Arithmetic."//urdf.main.URDF_main.getSp(level + 1) + "DB or Ar\n";
			var s:String = "";
			if (this.groundedRule != null) {
				s += this.groundedRule.toString();//urdf.main.URDF_main.getSp(level) + this.groundedRule.toString();
				s += "@" + this.conf;
				//if (super.tmp != 0)
					//s += " t:" + super.tmp;
				s += "\n";
			} else if (this.rule != null) {
				s += this.rule.toString();//urdf.main.URDF_main.getSp(level) + this.rule.toString();
				s += "@" + this.conf;
				//if (super.tmp != 0)
					//s += " t:" + super.tmp;
				s += "\n";
			}
			for each (var child:ULineageAbstract in super.children) {
				s += child.toStringPerLevel(level + 1);
			}
			return s;
		}
		
		/** 
		 * Reads in the serialized data from the mapped ULineageAnd Java class.
		 * 
		 * @param input the serialized input data.
		 * */
		 override public function readExternal(input:IDataInput):void{
			super.readExternal(input);
			this.rule = USoftRule(input.readObject());
	        this.groundedRule = UGroundedSoftRule(input.readObject());
		 }
		
		/**
		 * Writes out the data to serialize to the mapped ULineageAnd Java class.
		 * 
		 * @param output the output data to serialize.
		 * */
		override public function writeExternal(output:IDataOutput):void{
		  super.writeExternal(output);
		}
	}
	
}
