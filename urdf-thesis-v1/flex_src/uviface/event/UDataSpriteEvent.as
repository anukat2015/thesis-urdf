package uviface.event
{
	import flash.events.Event;
	
	import uviface.viz.util.UColors;
	
	public class UDataSpriteEvent extends Event
	{
		
		// Define static constant.
        public static const DATASPRITE_EVENT:String = "dataSpriteEvent";
        
        private var _nodeShadowEnabled:Boolean = false;
        
        private var _edgeShadowEnabled:Boolean = false;
        
        private var _nodeShadowLength:Number = 0;
        
        private var _edgeShadowLength:Number = 0;
        
        private var _nodeShadowAlpha:Number = 0;
        
        private var _edgeShadowAlpha:Number = 0;
        
        private var _nodeShadowColor:uint = UColors.BLACK;
        
        private var _edgeShadowColor:uint = UColors.BLACK;
		
		public function UDataSpriteEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
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
		
	}
}