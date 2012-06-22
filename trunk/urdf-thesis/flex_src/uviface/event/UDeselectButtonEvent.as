package uviface.event
{
	import flash.events.Event;

	public class UDeselectButtonEvent extends Event
	{
		public static const DESELECT_HELP_BUTTON:String = "deselectHelpButton";
		
		public function UDeselectButtonEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
		}
		
	}
}