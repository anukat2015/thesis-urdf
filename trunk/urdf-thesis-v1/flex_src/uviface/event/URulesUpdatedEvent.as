package uviface.event
{
	import flash.events.Event;

	public class URulesUpdatedEvent extends Event
	{
       // Define static constant to indicate the update of the soft and hard rules.
        public static const RULES_UPDATED:String = "rulesUpdated";
		
		public function URulesUpdatedEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
		}
		
	}
}