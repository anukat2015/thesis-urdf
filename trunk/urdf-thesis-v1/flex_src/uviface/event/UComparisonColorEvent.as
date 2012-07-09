package uviface.event
{	
	import flash.events.Event;

	public class UComparisonColorEvent extends Event
	{
		// Define static constant.
        public static const COMPARISON_COLOR_EVENT:String = "comparisonColorEvent";
        
        public static const BLACK_CLUSTER:int = 0;
        public static const YELLOW_CLUSTER:int = 1;
        public static const WHITE_CLUSTER:int = 2;
        
        // Define a public variable to hold the node name to set as new root node.
        private var _colorToChange:int = BLACK_CLUSTER;
        private var _color:int = 0;
		
		public function UComparisonColorEvent(type:String, colorToChange:int = BLACK_CLUSTER, color:int = 0, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
			_colorToChange = colorToChange;
			_color = color;
		}
        
        public function get colorToChange():int {
        	return _colorToChange;
        }
        
         public function set colorToChange(colorToChange:int):void {
        	_colorToChange = colorToChange;
        }
        
        public function get color():int {
        	return _color;
        }
        
         public function set color(color:int):void {
        	_color = color;
        }
		
	}
}