package uviface.event
{
	import flash.events.Event;
	import uviface.viz.UFlareVis;
	
	public class ULayoutEvent extends Event
	{
		
		// Define static constant.
        public static const LAYOUT:String = "layout";
       
        // the layouts for the normal visualization
        //public static const RADIAL:int = 0;
        //public static const FORCE:int = 1;
        
        /* The type of layout for the visualization. */
        private var _layout:int = UFlareVis.RADIAL_LAYOUT;
		
		public function ULayoutEvent(type:String, layout:int, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
			if(layout != UFlareVis.RADIAL_LAYOUT && layout != UFlareVis.FORCE_LAYOUT)
			   _layout = UFlareVis.RADIAL_LAYOUT;
			else
			   _layout = layout;
		}
        
        public function get layout():int {
        	return _layout;
        }
        
         public function set layout(layout:int):void {
        	_layout = layout;
        }
        
        // Override the inherited clone() method.
        override public function clone():Event {
            return new ULayoutEvent(type, _layout);
        }
		
	}
}