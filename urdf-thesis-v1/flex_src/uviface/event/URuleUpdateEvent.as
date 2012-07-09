package uviface.event
{
	import flash.events.Event;

	public class URuleUpdateEvent extends Event
	{
       // Define static constant to indicate the update of the soft and hard rules.
        public static const UPDATE_RULES:String = "updateRules";
        
        public static const ALL:int = 0;
        public static const SOFT_ONLY:int = 1;
        public static const HARD_ONLY:int = 2
        
        // 0 means all rules, 1 means soft rules only, 2 means hard rules only
        private var _ruleType:int = 0;
		
		public function URuleUpdateEvent(type:String, ruleType:int, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
			if(ruleType < 0 || ruleType > 2)
			   _ruleType = 0;
			else
			   _ruleType = ruleType;
		}
		
		public function get ruleType():int {
        	return _ruleType;
        }
        
         public function set ruleType(ruleType:int):void {
        	_ruleType = ruleType;
        }
        
        // Override the inherited clone() method.
        override public function clone():Event {
            return new URuleUpdateEvent(type, _ruleType);
        }
		
	}
}