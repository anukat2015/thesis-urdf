package uviface.event
{
	import flash.events.Event;

	public class UPanelVisibilityEvent extends Event
	{
        public static const PANEL_CLOSED_EVENT:String = "panelClosed";
        public static const PANEL_OPENED_EVENT:String = "panelOpened";
		
		public function UPanelVisibilityEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);	
		}
		
	}
}