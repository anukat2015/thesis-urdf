package uviface.event
{
	import flash.events.Event;

	public class UKillSessionEvent extends Event
	{
        public static const KILL_SESSION_EVENT:String = "killSession";
		
		public function UKillSessionEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
		}
		
	}
}