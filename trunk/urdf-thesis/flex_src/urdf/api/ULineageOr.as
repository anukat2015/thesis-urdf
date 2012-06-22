package urdf.api
{
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;	
    
    [Bindable]
    [RemoteClass(alias="urdf.api.ULineageOr")]
	public class ULineageOr extends ULineageAbstract {
	
		private var fact:UFact;
		
		public function ULineageOr() {
			super();
		}
		
		public function initLineageOr(fact:UFact):void {
			//super.init("ULineageOr Instance","LinOrInst");//super.init("getDiscription()","LinOrInst");
			this.fact = fact;
		}
	
		public function getFact():UFact {
			return this.fact;
		}
		
		override public function lineageAvailable():Boolean {
			//return (this.children.length > 0) && (this.children[0] as ULineageAnd).lineageAvailable();
			if (this.children.length > 0) {
			   for each (var lineageAnd:ULineageAnd in this.getChildren()) {
			      if(lineageAnd.lineageAvailable())
			         return true;
			   }
			   return false;
			}
			return false;
		}
		
		override public function isAndNode():Boolean {
			return false;
		}
		
		override public function isOrNode():Boolean {
			return true;
		}
		
		override public function isAbstractNode():Boolean {
			return false;
		}
	    
	    override protected function getDescription():String {
			if (this.fact != null)
				return "OR " + this.fact.getRelationName() + "(" + this.fact.getFirstArgumentName() + "," + this.fact.getSecondArgumentName() + ")";
			else
				return "OR";
		}
	    
		override protected function getStringAtLevel(level:int):String {
			var s:String = "";
			if (this.fact != null) {
				s += //urdf.main.URDF_main.getSp(level) + this.fact.getRelationName() + "(" + this.fact.getFirstArgumentName() + "," + this.fact.getSecondArgumentName()
				    this.fact.getRelationName() + "(" + this.fact.getFirstArgumentName() + "," + this.fact.getSecondArgumentName()
				    + ")";
				s += "@" + super.conf;
				//if (super.tmp != 0)
					//s += " t:" + super.tmp;
				s += "\n";
			}
			var i:int = 0;
			for each (var child:ULineageAbstract in super.children) {
				if (i > 0)
					s += "OR\n";//urdf.main.URDF_main.getSp(level) + "OR\n";
				s += child.toStringPerLevel(level);
				i++;
			}
			return s;
		}
		
		/** 
		 * Reads in the serialized data from the mapped ULineageOr Java class.
		 * 
		 * @param input the serialized input data.
		 * */
		 override public function readExternal(input:IDataInput):void{
			super.readExternal(input);
			this.fact = UFact(input.readObject());
		 }
		
		/**
		 * Writes out the data to serialize to the mapped ULineageOr Java class.
		 * 
		 * @param output the output data to serialize.
		 * */
		override public function writeExternal(output:IDataOutput):void{
		   super.writeExternal(output);
		}
		
	}

}
