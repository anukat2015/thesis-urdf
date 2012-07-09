package uviface.event
{
	import flare.vis.data.NodeSprite;
	
	import flash.events.Event;

	public class UQueryEvent extends Event
	{
		// Define static constant.
        public static const PROCESS_QUERY:String = "processQuery";
        public static const SUBMIT_RULE_CHANGED_QUERY:String = "submitRuleChangedQuery";
        public static const QUERY_STRING_UPDATE_EVENT:String = "updateQueryString";
		
		// Define a public variable to hold the state of the enable property.
        private var _queries:Array;
        // the node whose related data should be loaded.
        private var _dataLoadNode:NodeSprite;
        
        // the flag to indicate that the soft rules have changed in the meantime
        //private var _softRulesChanged:Boolean = false;
        // the flag to indicate that the hard rules have changed in the meantime
        //private var _hardRulesChanged:Boolean = false;
		
		public function UQueryEvent(type:String, queries:Array, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			_queries = queries;
		}
        
        public function get queries():Array {
        	return _queries;
        }
        
         public function set queries(queries:Array):void {
        	_queries = queries;
        }
        
        public function get dataLoadNode():NodeSprite {
        	return _dataLoadNode;
        }
        
         public function set dataLoadNode(dataLoadNode:NodeSprite):void {
        	_dataLoadNode = dataLoadNode;
        }
        
        /*
        public function get softRulesChanged():Boolean {
        	return _softRulesChanged;
        }
        
         public function set softRulesChanged(softRulesChanged:Boolean):void {
        	_softRulesChanged = softRulesChanged;
        }
        
        public function get hardRulesChanged():Boolean {
        	return _hardRulesChanged;
        }
        
         public function set hardRulesChanged(hardRulesChanged:Boolean):void {
        	_hardRulesChanged = hardRulesChanged;
        }
        */
		
	}
}