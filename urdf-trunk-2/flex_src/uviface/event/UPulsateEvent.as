package uviface.event
{	
	import flash.events.Event;

	public class UPulsateEvent extends Event
	{
		public static const PULSATE_EVENT:String = "pulsateEvent";
		
		private var _doPulsate:Boolean = false;
		
		public function UPulsateEvent(type:String, doPulsate:Boolean = false, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
			_doPulsate = doPulsate;
		}
        
        public function get doPulsate():Boolean {
        	return _doPulsate;
        }
        
         public function set doPulsate(doPulsate:Boolean):void {
        	_doPulsate = doPulsate;
        }
		
	}
}