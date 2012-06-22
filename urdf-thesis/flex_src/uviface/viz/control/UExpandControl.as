package uviface.viz.control
{
	import flare.display.TextSprite;
	import flare.vis.Visualization;
	import flare.vis.controls.Control;
	import flare.vis.data.NodeSprite;
	
	import flash.display.InteractiveObject;
	import flash.events.MouseEvent;
	
	import uviface.viz.UFlareVis;
	import uviface.viz.util.UStringConstants;

	/**
	 * Interactive control for expaning and collapsing graph or tree nodes
	 * by clicking them. This control will only work when applied to a
	 * Visualization instance.
	 */
	public class UExpandControl extends Control
	{
		private var _cur:NodeSprite;
		
		/** Update function invoked after expanding or collapsing an item.
		 *  By default, invokes the <code>update</code> method on the
		 *  visualization with a 1-second transitioner. */
		public var update:Function = function():void {
			var vis:Visualization = _object as Visualization;
			if (vis) vis.update(1).play();
		}
		
		// --------------------------------------------------------------------
		
		/**
		 * Creates a new ExpandControl.
		 * @param filter a Boolean-valued filter function for determining which
		 *  item this control will expand or collapse
		 * @param update function invokde after expanding or collapsing an
		 *  item.
		 */		
		public function UExpandControl(filter:*=null, update:Function=null)
		{
			this.filter = filter;
			if (update != null) this.update = update;
		}
		
		/** @inheritDoc */
		public override function attach(obj:InteractiveObject):void
		{
			if (obj==null) { detach(); return; }
			if (!(obj is Visualization)) {
				throw new Error("This control can only be attached to a Visualization");
			}
			super.attach(obj);
			obj.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
		}
		
		/** @inheritDoc */
		public override function detach():InteractiveObject
		{
			if (_object != null) {
				_object.removeEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
			}
			return super.detach();
		}
		
		private function onMouseDown(event:MouseEvent) : void {
			var s:NodeSprite = event.target as NodeSprite;
			if (s==null) return; // exit if not a NodeSprite
			
			if (_filter==null || _filter(s)) {
				_cur = s;
				_cur.stage.addEventListener(MouseEvent.MOUSE_MOVE, onDrag);
				_cur.stage.addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
			}
			event.stopPropagation();
		}
		
		private function onDrag(event:MouseEvent) : void {
			_cur.stage.removeEventListener(MouseEvent.MOUSE_UP, onMouseUp);
			_cur.stage.removeEventListener(MouseEvent.MOUSE_MOVE, onDrag);
			_cur = null;
		}
		
		private function onMouseUp(event:MouseEvent) : void {
			_cur.stage.removeEventListener(MouseEvent.MOUSE_UP, onMouseUp);
			_cur.stage.removeEventListener(MouseEvent.MOUSE_MOVE, onDrag);
			
			if(_cur.data.hasOwnProperty("DB") || (_cur.inDegree == 1 && _cur.getInNode(0).data.hasOwnProperty("DB"))) {
				if(!_cur.expanded) {
				   _cur.expanded = !_cur.expanded;
				   _cur.data[UStringConstants.LABEL] = _cur.data[UStringConstants.LABEL_COPY];
				   // set the new node label   
				   (_cur.props.label as TextSprite).text = _cur.data[UStringConstants.LABEL];
				   // adjust the node size
				   UFlareVis.drawNode(_cur);
				}
			}
			else {
				
				_cur.expanded = !_cur.expanded;
				
				var prevNode:NodeSprite;
				var prevNode2:NodeSprite;
				var dbNode:NodeSprite;
				
				if(UFlareVis.lineageMode && _cur.data.hasOwnProperty("linTree")) {
					
					if(_cur.expanded) {	
					   _cur.data[UStringConstants.LABEL] = _cur.data[UStringConstants.LABEL_COPY];
					   if(_cur.data.label == UStringConstants.OR) {
					   	  for(var i:int = 0; i<_cur.inDegree; i++) {
					   	  	 prevNode = _cur.getInNode(i);
					   	  	 prevNode.data.label = prevNode.data[UStringConstants.LABEL_COPY];
					   	  	 prevNode.expanded = true;
					   	  	 // set the new node label   
							 (prevNode.props.label as TextSprite).text = prevNode.data[UStringConstants.LABEL];
							 // adjust the node size
							 UFlareVis.drawNode(prevNode);
					   	  	 for(var j:int = 0; j<prevNode.inDegree; j++) {
						   	  	 prevNode2 = prevNode.getInNode(j);
						   	  	 prevNode2.data.label = prevNode2.data[UStringConstants.LABEL_COPY];
						   	  	 prevNode2.expanded = true;
						   	  	 // set the new node label   
								 (prevNode2.props.label as TextSprite).text = prevNode2.data[UStringConstants.LABEL];
								 // adjust the node size
								 UFlareVis.drawNode(prevNode2);
								 // get the db node for the respective fact node
								 dbNode = prevNode2.getInNode(0);
								 if(dbNode.data.DB) {
								    dbNode.expanded = true;
								    dbNode.data.label = dbNode.data[UStringConstants.LABEL_COPY];
								    (dbNode.props.label as TextSprite).text = dbNode.data[UStringConstants.LABEL];
								    UFlareVis.drawNode(dbNode);
								 }								 								 
						   	 }
					   	  }
					   }
					   else if(_cur.data.label == UStringConstants.AND) {
					   	  for(var k:int = 0; k<_cur.inDegree; k++) {
					   	  	 prevNode = _cur.getInNode(k);
					   	  	 prevNode.data.label = prevNode.data[UStringConstants.LABEL_COPY];
					   	  	 prevNode.expanded = true;
					   	  	 // set the new node label   
							 (prevNode.props.label as TextSprite).text = prevNode.data[UStringConstants.LABEL];
							 // adjust the node size
							 UFlareVis.drawNode(prevNode);
							 // get the db node for the respective fact node
							 dbNode = prevNode.getInNode(0);
							 if(dbNode.data.DB) {
							    dbNode.expanded = true;
							    dbNode.data.label = dbNode.data[UStringConstants.LABEL_COPY];
							    (dbNode.props.label as TextSprite).text = dbNode.data[UStringConstants.LABEL];
							    UFlareVis.drawNode(dbNode);
							 }		
					   	  }
					   }
					}
					else
					   _cur.data[UStringConstants.LABEL] = UStringConstants.THREE_DOTS;
					
					// set the new node label   
					(_cur.props.label as TextSprite).text = _cur.data[UStringConstants.LABEL];
					// adjust the node size
					UFlareVis.drawNode(_cur);
					
				}
			
			}
			
			_cur = null;	
			event.stopPropagation();
			
			update();
		}
		
		/* public function showCompleteLineageTree(flareVis:UFlareVis) : void {
			
				
			var node:NodeSprite;
			
			if(UFlareVis.lineageMode) {
				
		   	  for each (node in flareVis.graphData.graphCache.lineageNodes) {
		   	  	 node.data.label = node.data[UStringConstants.LABEL_COPY];
		   	  	 node.expanded = true;
		   	  	 // set the new node label   
				 (node.props.label as TextSprite).text = node.data[UStringConstants.LABEL];
				 // adjust the node size
				 UFlareVis.drawNode(node);
		   	  }
				
			}
			
			update();
		} */
		
	} // end of class ExpandControl
}