package uviface.event
{
	import flash.events.Event;
	
	import urdf.api.ULineageAnd;

	public class ULineageEvent extends Event
	{
		// Define static constant.
        public static const PRODUCE_LINEAGE_GRAPH:String = "produceLineageGraph";
        public static const FULL_LINEAGE:int = 0;
        public static const MATCHED_PATTERN_LINEAGE:int = 1;
        public static const FACT_LINEAGE:int = 2;
        
        private var _typeOfLineage:int = ULineageEvent.FACT_LINEAGE;
        
        // all ULineageAnd instances for the complete query result
        // collected in this array
        private var _completeResultLineage:Array;
        //private var _lineageResultConfidence:String = null;
        private var _queryString:String = null;
        private var _lineageFacts:ULineageAnd = null;
        
        // the fact id for the respective lineage fact
        private var _factID:String;
		
		public function ULineageEvent(type:String, factID:String = null, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
			//_edge = edge;
			_factID = factID;
			//per default
			_typeOfLineage = ULineageEvent.FACT_LINEAGE;
		}
        
        public function get factID():String {
        	return _factID;
        }
        
         public function set factID(factID:String):void {
        	_factID = factID;
        }
        
        public function get lineageFacts():ULineageAnd {
        	return _lineageFacts;
        }
        
         public function set lineageFacts(lineageFacts:ULineageAnd):void {
        	_lineageFacts = lineageFacts;
        }
        
        public function get typeOfLineage():int {
        	return _typeOfLineage;
        }
        
         public function set typeOfLineage(typeOfLineage:int):void {
        	if(typeOfLineage < 0 || typeOfLineage > 2)
        	   _typeOfLineage = 2;
        	else
        	   _typeOfLineage = typeOfLineage;   
        }
        
         public function get completeResultLineage():Array {
        	return _completeResultLineage;
        }
        
         public function set completeResultLineage(completeResultLineage:Array):void {
        	_completeResultLineage = completeResultLineage;
        }
        
         public function get queryString():String {
        	return _queryString;
        }
        
         public function set queryString(queryString:String):void {
        	_queryString = queryString;
        }
		
	}
}