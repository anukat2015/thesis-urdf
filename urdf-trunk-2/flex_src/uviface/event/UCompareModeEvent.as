package uviface.event
{
	import flash.events.Event;

	public class UCompareModeEvent extends Event
	{
		// Define static constant.
        public static const ACTIVATE_COMPARE_MODE:String = "activateCompareMode";
        public static const DEACTIVATE_COMPARE_MODE:String = "deactivateCompareMode";
        
        public static const ACTIVATE_GLOW_MODE:String = "activateGlowMode";
        public static const DEACTIVATE_GLOW_MODE:String = "deactivateGlowMode";
		
		public function UCompareModeEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
		}
        
        // Override the inherited clone() method.
        override public function clone():Event {
            return new UCompareModeEvent(type);
        }
		
	}
}