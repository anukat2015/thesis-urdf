package uviface.event
{
	import flash.events.Event;

	public class UStatisticsUpdateEvent extends Event
	{
		// Define static constant.
        public static const UPDATE_STATISTICS:String = "updateStatistics";
		
		public function UStatisticsUpdateEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
		}
        
        // Override the inherited clone() method.
        override public function clone():Event {
            return new UStatisticsUpdateEvent(type);
        }
		
	}
}