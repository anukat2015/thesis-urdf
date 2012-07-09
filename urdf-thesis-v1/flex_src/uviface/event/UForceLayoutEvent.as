package uviface.event
{
	import flash.events.Event;
	
	public class UForceLayoutEvent extends Event
	{
		
		// Define static constant.
        public static const FORCE_LAYOUT_EVENT:String = "forceLayoutEvent";
        
        private var _particleMass:Number = 0;
        
        private var _springLength:Number = 0;
        
        private var _springTension:Number = 0;
        
        private var _iterations:int = 0;
        
        private var _enforceBounds:Boolean = false;
        
        private var _nBodyGravitation:Number = 0;
		
		public function UForceLayoutEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
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
        
         public function get nBodyGravitation():Number {
        	return _nBodyGravitation;
        }
        
         public function set nBodyGravitation(nBodyGravitation:Number):void {
        	_nBodyGravitation = nBodyGravitation;
        }
        
        // Override the inherited clone() method.
        override public function clone():Event {
            var forceLayoutEvent:UForceLayoutEvent = new UForceLayoutEvent(type);
            forceLayoutEvent.particleMass = _particleMass;
            forceLayoutEvent.springLength = _springLength,
            forceLayoutEvent.springTension = _springTension;
            forceLayoutEvent.iterations = _iterations;
            forceLayoutEvent.enforceBounds = _enforceBounds;
            forceLayoutEvent.nBodyGravitation = _nBodyGravitation;
            return forceLayoutEvent;
        }
		
	}
}