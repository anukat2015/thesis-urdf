package uviface.event
{
	import flash.events.Event;
	
	public class URadiusDistanceEvent extends Event
	{
		
		// Define static constant.
        public static const RADIUS_AND_DISTANCE_EVENT:String = "radiusAndDistanceEvent";
        public static const VISIBILITY:int = 5;
        public static const ALPHA:int = 6;
       
        //private var _doRadiusIncrement:Boolean = false;
        
        //private var _doBoundsEnforcing:Boolean = false;
        
        public static const INCREMENT_HOP_SIZE:int = 0;
        public static const INCREMENT_RADIUS:int = 1;
        public static const ENFORCE_BOUNDS:int = 2;
        public static const CHANGE_DISTANCE_FILTER:int = 3;
        public static const CHANGE_ALPHA_VALUE:int = 4;
        
        private var _distanceFilterType:int = VISIBILITY;
        private var _alphaValue:Number = 0.5;
        
        private var _typeOfAction:int = 0;
        
        /* The hop size for the graph distance filter. */
        private var _hopSize:int;
        /* The radius increment for the radial layout. */
        private var _radiusIncrement:Number;
        
        private var _enforceBounds:Boolean = false;
		
		public function URadiusDistanceEvent(type:String, typeOfAction:int, hopSize:int, radiusIncrement:Number, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
			if(typeOfAction < 0)// || typeOfAction > 2)
			   typeOfAction = 0;
			
			_typeOfAction = typeOfAction;
			_hopSize = hopSize;
			_radiusIncrement = radiusIncrement;
			//_enforceBounds = enforceBounds;
		}
		
		public function get typeOfAction():int {
        	return _typeOfAction;
        }
        
         public function set typeOfAction(typeOfAction:int):void {
        	_typeOfAction = typeOfAction;
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
        /*
        public function get doBoundsEnforcing():Boolean {
        	return _doBoundsEnforcing;
        }
        
         public function set doBoundsEnforcing(doBoundsEnforcing:Boolean):void {
        	_doBoundsEnforcing = _doBoundsEnforcing;
        }
        */
         public function get enforceBounds():Boolean {
        	return _enforceBounds;
        }
        
         public function set enforceBounds(enforceBounds:Boolean):void {
        	_enforceBounds = enforceBounds;
        }
        
         public function get distanceFilterType():int {
        	return _distanceFilterType;
        }
        
         public function set distanceFilterType(distanceFilterType:int):void {
        	_distanceFilterType = distanceFilterType;
        }
        
         public function get alphaValue():Number {
        	return _alphaValue;
        }
        
         public function set alphaValue(alphaValue:Number):void {
        	_alphaValue = alphaValue;
        }
       
		
	}
}