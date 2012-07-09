package uviface.event
{
	import flash.display.BitmapData;
	import flash.events.Event;

	public class UGraphOverviewEvent extends Event
	{
		public static const OVERVIEW_EVENT:String = "overviewEvent";
		
		private var _overviewBitmap:BitmapData;
		
		public function UGraphOverviewEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
		}
		
		public function get overviewBitmap():BitmapData {
        	return _overviewBitmap;
        }
        
         public function set overviewBitmap(overviewBitmap:BitmapData):void {
        	_overviewBitmap = overviewBitmap;
        }
		
	}
}