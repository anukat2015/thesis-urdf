package urdf.api
{

	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;	
	import flash.utils.IExternalizable;
    
    [Bindable]
    [RemoteClass(alias="urdf.api.ULineageAbstract")]
	public class ULineageAbstract implements IExternalizable //extends UObject
	{	
		//protected HashSet<ULineageAbstract> children;
		protected var children:Array;
	
		protected var size:int;
		protected var conf:Number;
	
		private var cycle:Boolean; // used for toString(int)
	
		public function ULineageAbstract() {
			this.children = new Array();// new HashSet<ULineageAbstract>();
			//this.size = 1;
			this.size = 0;
			//this.tmp = 0;
		}
	
		/*
		 * Get methods
		 */
		//public HashSet<ULineageAbstract> getChildren() {
		public function getChildren():Array {
			return this.children;
		}
	
		public function length():int {
			return this.size;
		}
	
		public function getConf():Number {
			return this.conf;
		}
		
		public function lineageAvailable():Boolean {
			return false;//return this.children.length > 0;
		}
		
		public function isAndNode():Boolean {
			return false;
		}
		
		public function isOrNode():Boolean {
			return false;
		}
		
		public function isAbstractNode():Boolean {
			return true;
		}
	
		protected function getDescription():String {return null}
		
		public function toString():String {
			var s:String = "[" + getDescription();
			s += "@" + this.conf;
			s += " c:" + this.children.length;
			s += " s:" + this.size;
			//if (this.tmp != 0)
				//s += " t:" + this.tmp;
			return s + "]";
		}
	
		protected function getStringAtLevel(level:int):String {return null;}
	
		public function toStringPerLevel(level:int):String {
			if (this.cycle) {
				throw new Error("cycle in " + this.toString());
				return "cycle\n";
			}
			this.cycle = true;
			var s:String = getStringAtLevel(level);
			this.cycle = false;
			return s;
		}
		
		/** 
		 * Reads in the serialized data from the mapped ULineageAbstract Java class.
		 * 
		 * @param input the serialized input data.
		 * */
		 public function readExternal(input:IDataInput):void{
			//super.readExternal(input);
			
			var numberOfChildren:int = input.readInt(); 
			var child:ULineageAbstract;
			while(numberOfChildren > 0) {
			   child = ULineageAbstract(input.readObject());
			   //this.size += child.size();
			   this.children.push(child);  
			   numberOfChildren--;
			}
			
	        this.size = input.readInt();
		    this.conf = input.readDouble();
	        this.cycle = input.readBoolean();
		 }
		
		/**
		 * Writes out the data to serialize to the mapped ULineageAbstract Java class.
		 * 
		 * @param output the output data to serialize.
		 * */
		public function writeExternal(output:IDataOutput):void{
		  //super.writeExternal(output);
		}
		
	}

}
