package uviface.event
{	
	import flash.events.Event;

	public class UClusterFixingEvent extends Event
	{
		// Define static constant.
        public static const CLUSTER_FIXING_EVENT:String = "clusterFixingEvent";
        
        private var _fixClusters:Boolean = true;
        private var _fixUnfixNumber:int = 2;
        //private var _unfixNumber:int = 2;
		
		public function UClusterFixingEvent(type:String, fixClusters:Boolean = true, 
		      fixUnfixNumber:int = 2, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			_fixClusters = fixClusters;
			_fixUnfixNumber = fixUnfixNumber;
			//_unfixNumber = unfixNumber;
		}
        
        public function get fixClusters():Boolean {
        	return _fixClusters;
        }
        
         public function set fixClusters(fixClusters:Boolean):void {
        	_fixClusters = fixClusters;
        }
        
        public function get fixUnfixNumber():int {
        	return _fixUnfixNumber;
        }
        
         public function set fixUnfixNumber(fixUnfixNumber:int):void {
        	_fixUnfixNumber = fixUnfixNumber;
        }
        
        /* public function get unfixNumber():int {
        	return _unfixNumber;
        }
        
         public function set unfixNumber(unfixNumber:int):void {
        	_unfixNumber = unfixNumber;
        } */
		
	}
}