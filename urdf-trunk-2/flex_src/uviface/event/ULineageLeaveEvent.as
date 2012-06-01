package uviface.event
{
	import flash.events.Event;

	public class ULineageLeaveEvent extends Event
	{
		// Define static constant.
        public static const LEAVE_LINEAGE_MODE:String = "leaveLineageMode";
        
        // Define a public variable to hold the state of the enable property.
        private var _factID:String;
		
		public function ULineageLeaveEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
		}
        
        // Override the inherited clone() method.
        override public function clone():Event {
            return new ULineageLeaveEvent(type);
        }
		
	}
}