package uviface.event
{	
	import flash.events.Event;

	public class UClusterCompareEvent extends Event
	{
		// Define static constant.
        public static const CLUSTER_COMPARE_EVENT:String = "clusterCompareEvent";
        
        // Define a public variable to hold the node name to set as new root node.
        private var _blackClusterID:String = null;
        // Define a public variable to hold the node name to set as new root node.
        private var _whiteClusterID:String = null;
		
		public function UClusterCompareEvent(type:String, blackClusterID:String, whiteClusterID:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
			_blackClusterID = blackClusterID;
			_whiteClusterID = whiteClusterID;
		}
        
        public function get blackClusterID():String {
        	return _blackClusterID;
        }
        
         public function set blackClusterID(blackClusterID:String):void {
        	_blackClusterID = blackClusterID;
        }
        
        public function get whiteClusterID():String {
        	return _whiteClusterID;
        }
        
         public function set whiteClusterID(whiteClusterID:String):void {
        	_whiteClusterID = whiteClusterID;
        }
		
	}
}