package uviface.event
{	
	import flash.events.Event;

	public class ULineageIndicationEvent extends Event
	{
		// Define static constant.
        public static const LINEAGE_INDICATION_EVENT:String = "lineageIndicationEvent";
        
        // Define a public variable to hold the node name to set as new root node.
        private var _indicateLineage:Boolean = false;
		
		public function ULineageIndicationEvent(type:String, indicateLineage:Boolean = false, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
			_indicateLineage = indicateLineage;
		}
        
        public function get indicateLineage():Boolean {
        	return _indicateLineage;
        }
        
         public function set indicateLineage(indicateLineage:Boolean):void {
        	_indicateLineage = indicateLineage;
        }
		
	}
}