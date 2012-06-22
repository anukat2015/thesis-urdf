package uviface.event
{
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	
	import mx.containers.Canvas;

	public class UPanZoomEvent extends Event
	{
		public static const ZOOM_EVENT:String = "zoomEvent";
		
		private var _doZoom:Boolean = true;
		private var _zoomIn:Boolean = true;
		private var _sourceCanvas:Canvas;
		private var _orgPoint:Point;
		private var _shiftedPoint:Point;
		
		public function UPanZoomEvent(type:String, doZoom:Boolean = true, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			_doZoom = doZoom;
		}
		
		public function set doZoom(doZoom:Boolean):void {
			_doZoom = doZoom;
		}
		
		public function get doZoom():Boolean {
			return _doZoom;
		}
		
		public function set zoomIn(zoomIn:Boolean):void {
			_zoomIn = zoomIn;
		}
		
		public function get zoomIn():Boolean {
			return _zoomIn;
		}
		
		public function set sourceCanvas(sourceCanvas:Canvas):void {
			_sourceCanvas = sourceCanvas;
		}
		
		public function get sourceCanvas():Canvas {
			return _sourceCanvas;
		}
		
		public function set orgPoint(orgPoint:Point):void {
			_orgPoint = orgPoint;
		}
		
		public function get orgPoint():Point {
			return _orgPoint;
		}
		
		public function set shiftedPoint(shiftedPoint:Point):void {
			_shiftedPoint = shiftedPoint;
		}
		
		public function get shiftedPoint():Point {
			return _shiftedPoint;
		}
		
	}
}