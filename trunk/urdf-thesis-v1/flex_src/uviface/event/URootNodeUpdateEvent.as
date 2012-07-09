package uviface.event
{
	import flare.vis.data.NodeSprite;
	
	import flash.events.Event;

	public class URootNodeUpdateEvent extends Event
	{
		// Define static constant.
        public static const UPDATE_ROOT_NODE:String = "updateRootNode";
        
        // Define a public variable to hold the node to set as new root node.
        private var _rootNode:NodeSprite;
        // Define a public variable to hold the node name to set as new root node.
        private var _rootNodeName:String = null;
        // A flag to indicate if we want to use one root node or a result cluster.
        private var _useResultCluster:Boolean = false;
        
        private var _nodeClicked:Boolean = false;
		
		public function URootNodeUpdateEvent(type:String, rootNode:NodeSprite, rootNodeName:String = null, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
			_rootNode = rootNode;
			_rootNodeName = rootNodeName;
		}
		
		public function get rootNode():NodeSprite {
        	return _rootNode;
        }
        
         public function set rootNode(rootNode:NodeSprite):void {
        	_rootNode = rootNode;
        }
        
        public function get rootNodeName():String {
        	return _rootNodeName;
        }
        
         public function set rootNodeName(rootNodeName:String):void {
        	_rootNodeName = rootNodeName;
        }
        
        public function get useResultCluster():Boolean {
        	return _useResultCluster;
        }
        
         public function set useResultCluster(useResultCluster:Boolean):void {
        	_useResultCluster = useResultCluster;
        }
        
        public function get nodeClicked():Boolean {
        	return _nodeClicked;
        }
        
         public function set nodeClicked(nodeClicked:Boolean):void {
        	_nodeClicked = nodeClicked;
        }
		
	}
}