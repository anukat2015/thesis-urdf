package uviface.event
{
	import flash.events.Event;

	public class UTextAreaEvent extends Event
	{
		// Define static constant.
        public static const DROP_DOWN_OPEN_EVENT:String = "openDropDown";
		
		private var _index:int = 0;
		private var _offset:int = 0;
		
		public function UTextAreaEvent(type:String, index:int = 0, offset:int = 0, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			_index= index;
			_offset = offset;
		}
		
		public function set index(index:int):void {
			_index = index;
		}
		
		public function get index():int {
			return _index;
		}
		
		public function set offset(offset:int):void {
			_offset = offset;
		}
		
		public function get offset():int {
			return _offset;
		}
		
	}
}