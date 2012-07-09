package uviface.event
{
	import flash.events.Event;
	import uviface.viz.util.UColors;

	public class UNodeAndLabelColorEvent extends Event
	{
		
		public static const NODE_AND_LABEL_COLOR_EVENT:String = "nodeAndLabelColorEvent";
		
		private var _changeContextNodesVisibility:Boolean = false;
		
		private var _changeLabelVisibility:Boolean = false;
		
		// the flag to indicate if node or edge properties should be changed
		private var _doEdgeColoring:Boolean = false;
		
		private var _setTrueEdgeColor:Boolean = true;
		private var _useEdgeAlpha:Boolean = true;
		private var _changeEdgeAlpha:Boolean = false;
		private var _doLabelColor:Boolean = false;
		
		private var _nodeColor:uint = UColors.BLACK;
		private var _nodeAlpha:Number = 1.0;
		private var _labelColor:uint = UColors.BLACK;
		private var _edgeColor:uint = UColors.green(7);
		
		public function UNodeAndLabelColorEvent(type:String, doLabelColor:Boolean = false, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			_doLabelColor = doLabelColor;
		}
		
		public function get nodeColor():uint {
			return _nodeColor;
		}
		
		public function set nodeColor(nodeColor:uint):void {
			_nodeColor = nodeColor;
		}
		
		public function get nodeAlpha():Number {
			return _nodeAlpha;
		}
		
		public function set nodeAlpha(nodeAlpha:Number):void {
			_nodeAlpha = nodeAlpha;
		}
		
		public function get labelColor():uint {
			return _labelColor;
		}
		
		public function set labelColor(labelColor:uint):void {
			_labelColor = labelColor;
		}
		
		public function get doLabelColor():Boolean {
			return _doLabelColor;
		}
		
		public function set doLabelColor(doLabelColor:Boolean):void {
			_doLabelColor = doLabelColor;
		}
		
		public function get doEdgeColoring():Boolean {
			return _doEdgeColoring;
		}
		
		public function set doEdgeColoring(doEdgeColoring:Boolean):void {
			_doEdgeColoring = doEdgeColoring;
		}
		
		public function get changeEdgeAlpha():Boolean {
			return _changeEdgeAlpha;
		}
		
		public function set changeEdgeAlpha(changeEdgeAlpha:Boolean):void {
			_changeEdgeAlpha = changeEdgeAlpha;
		}
		
		public function get useEdgeAlpha():Boolean {
			return _useEdgeAlpha;
		}
		
		public function set useEdgeAlpha(useEdgeAlpha:Boolean):void {
			_useEdgeAlpha = useEdgeAlpha;
		}
		
		public function get setTrueEdgeColor():Boolean {
			return _setTrueEdgeColor;
		}
		
		public function set setTrueEdgeColor(setTrueEdgeColor:Boolean):void {
			_setTrueEdgeColor = setTrueEdgeColor;
		}
		
		public function get edgeColor():uint {
			return _edgeColor;
		}
		
		public function set edgeColor(edgeColor:uint):void {
			_edgeColor = edgeColor;
		}
		
		public function get changeLabelVisibility():Boolean {
			return _changeLabelVisibility;
		}
		
		public function set changeLabelVisibility(changeLabelVisibility:Boolean):void {
			_changeLabelVisibility = changeLabelVisibility;
		}
		
		public function get changeContextNodesVisibility():Boolean {
			return _changeContextNodesVisibility;
		}
		
		public function set changeContextNodesVisibility(changeContextNodesVisibility:Boolean):void {
			_changeContextNodesVisibility = changeContextNodesVisibility;
		}
		
	}
}