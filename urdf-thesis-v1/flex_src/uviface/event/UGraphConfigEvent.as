package uviface.event
{
	import flash.events.Event;
	import uviface.viz.util.UColors;
	
	public class UGraphConfigEvent extends Event
	{
		
		// Define static constant.
        public static const GRAPH_CONFIG_EVENT:String = "graphConfigEvent";
        
        // the layouts for the normal visualization
        public static const RADIAL:int = 0;
        public static const FORCE:int = 1;
       
        // the layouts for the lineage tree
        //public static const LEFT_TO_RIGHT:int = 2; 
        //public static const RIGHT_TO_LEFT:int = 3;
        //public static const TOP_TO_BOTTOM:int = 4;
        //public static const BOTTOM_TO_TOP:int = 5;
        
        /* The hop size for the graph distance filter. */
        private var _hopSize:int;
        /* The radius increment for the radial layout. */
        private var _radiusIncrement:Number;
        /* The type of layout for the visualization. */
        private var _layout:int;
        
        /* The type of layout for the lineage tree. */
        private var _lineageLayout:String;
        
        private var _particleMass:Number = 0;
        
        private var _springLength:Number = 0;
        
        private var _springTension:Number = 0;
        
        private var _iterations:int = 0;
        
        private var _enforceBounds:Boolean = false;
        
        private var _nBodyGravitation:Number = 0;
        
        private var _nodeShadowEnabled:Boolean = false;
        
        private var _edgeShadowEnabled:Boolean = false;
        
        private var _nodeShadowLength:Number = 0;
        
        private var _edgeShadowLength:Number = 0;
        
        private var _nodeShadowAlpha:Number = 0;
        
        private var _edgeShadowAlpha:Number = 0;
        
        private var _nodeShadowColor:uint = UColors.BLACK;
        
        private var _edgeShadowColor:uint = UColors.BLACK;
        
        private var _depthSpacing:Number = 0;
        
        private var _breadthSpacing:Number = 0;
        
        private var _subtreeSpacing:Number = 0;
		
		public function UGraphConfigEvent(type:String, hopSize:int, radiusIncrement:Number, layout:int, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
			_hopSize = hopSize;
			_radiusIncrement = radiusIncrement;
			_layout = layout;
		}
		
		public function get hopSize():int {
        	return _hopSize;
        }
        
         public function set hopSize(hopSize:int):void {
        	_hopSize = hopSize;
        }
        
        public function get radiusIncrement():Number {
        	return _radiusIncrement;
        }
        
         public function set radiusIncrement(radiusIncrement:Number):void {
        	_radiusIncrement = radiusIncrement;
        }
        
        public function get layout():int {
        	return _layout;
        }
        
         public function set layout(layout:int):void {
        	_layout = layout;
        }
        
        public function get particleMass():Number {
        	return _particleMass;
        }
        
         public function set particleMass(particleMass:Number):void {
        	_particleMass = particleMass;
        }
        
        public function get springLength():Number {
        	return _springLength;
        }
        
         public function set springLength(springLength:Number):void {
        	_springLength = springLength;
        }
        
        public function get springTension():Number {
        	return _springTension;
        }
        
         public function set springTension(springTension:Number):void {
        	_springTension = springTension;
        }
        
        public function get iterations():int {
        	return _iterations;
        }
        
         public function set iterations(iterations:int):void {
        	_iterations = iterations;
        }
        
        public function get enforceBounds():Boolean {
        	return _enforceBounds;
        }
        
         public function set enforceBounds(enforceBounds:Boolean):void {
        	_enforceBounds = enforceBounds;
        }
        
        public function get lineageLayout():String {
        	return _lineageLayout;
        }
        
         public function set lineageLayout(lineageLayout:String):void {
        	_lineageLayout = lineageLayout;
        }
        
         public function get nBodyGravitation():Number {
        	return _nBodyGravitation;
        }
        
         public function set nBodyGravitation(nBodyGravitation:Number):void {
        	_nBodyGravitation = nBodyGravitation;
        }
        
         public function get nodeShadowEnabled():Boolean {
        	return _nodeShadowEnabled;
        }
        
         public function set nodeShadowEnabled(nodeShadowEnabled:Boolean):void {
        	_nodeShadowEnabled = nodeShadowEnabled;
        }
        
         public function get edgeShadowEnabled():Boolean {
        	return _edgeShadowEnabled;
        }
        
         public function set edgeShadowEnabled(edgeShadowEnabled:Boolean):void {
        	_edgeShadowEnabled = edgeShadowEnabled;
        }
        
         public function get nodeShadowLength():Number {
        	return _nodeShadowLength;
        }
        
         public function set nodeShadowLength(nodeShadowLength:Number):void {
        	_nodeShadowLength = nodeShadowLength;
        }
        
         public function get edgeShadowLength():Number {
        	return _edgeShadowLength;
        }
        
         public function set edgeShadowLength(edgeShadowLength:Number):void {
        	_edgeShadowLength = edgeShadowLength;
        }
        
        public function get nodeShadowAlpha():Number {
        	return _nodeShadowAlpha;
        }
        
         public function set nodeShadowAlpha(nodeShadowAlpha:Number):void {
        	_nodeShadowAlpha = nodeShadowAlpha;
        }
        
         public function get edgeShadowAlpha():Number {
        	return _edgeShadowAlpha;
        }
        
         public function set edgeShadowAlpha(edgeShadowAlpha:Number):void {
        	_edgeShadowAlpha = edgeShadowAlpha;
        }
        
        public function get nodeShadowColor():uint {
			return _nodeShadowColor;
		}
		
		public function set nodeShadowColor(nodeShadowColor:uint):void {
			_nodeShadowColor = nodeShadowColor;
		}
		
		public function get edgeShadowColor():uint {
			return _edgeShadowColor;
		}
		
		public function set edgeShadowColor(edgeShadowColor:uint):void {
			_edgeShadowColor = edgeShadowColor;
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