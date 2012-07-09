package uviface.event
{	
	import flash.events.Event;

	public class UFactSelectEvent extends Event
	{
		// Define static constant.
        public static const FACT_SELECT_EVENT:String = "factSelectEvent";
        
        // Define a public variable to hold the node name to set as new root node.
        private var _factID:String = "";
		
		public function UFactSelectEvent(type:String, factID:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
			_factID = factID;
		}
        
        public function get factID():String {
        	return _factID;
        }
        
         public function set factID(factID:String):void {
        	_factID = factID;
        }
		
	}
}