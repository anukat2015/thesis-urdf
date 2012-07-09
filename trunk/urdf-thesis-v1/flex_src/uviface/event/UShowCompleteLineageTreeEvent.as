package uviface.event
{	
	import flash.events.Event;

	public class UShowCompleteLineageTreeEvent extends Event
	{
		// Define static constant.
        public static const SHOW_COMPLETE_TREE:String = "showCompleteLineageTreeEvent";
		
		public function UShowCompleteLineageTreeEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
		}
		
	}
}