package uviface.event
{
	import flash.events.Event;

	public class ULineageLayoutEvent extends Event
	{
		// Define static constant.
        public static const LINEAGE_LAYOUT_EVENT:String = "lineageLayoutEvent";
        /* The type of layout for the lineage tree. */
        private var _orientation:String;
        private var _depthSpacing:Number = 0;
        
        private var _breadthSpacing:Number = 0;
        
        private var _subtreeSpacing:Number = 0;
		
		public function ULineageLayoutEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
		}
        
        public function get orientation():String {
        	return _orientation;
        }
        
         public function set orientation(orientation:String):void {
        	_orientation = orientation;
        }
        
         public function get depthSpacing():Number {
        	return _depthSpacing;
        }
        
         public function set depthSpacing(depthSpacing:Number):void {
        	_depthSpacing = depthSpacing;
        }
        
         public function get breadthSpacing():Number {
        	return _breadthSpacing;
        }
        
         public function set breadthSpacing(breadthSpacing:Number):void {
        	_breadthSpacing = breadthSpacing;
        }
        
         public function get subtreeSpacing():Number {
        	return _subtreeSpacing;
        }
        
         public function set subtreeSpacing(subtreeSpacing:Number):void {
        	_subtreeSpacing = subtreeSpacing;
        }
		
	}
}