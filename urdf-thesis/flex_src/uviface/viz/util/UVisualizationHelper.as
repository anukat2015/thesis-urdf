package uviface.viz.util
{
	import flare.display.TextSprite;
	import flare.vis.controls.ClickControl;
	import flare.vis.controls.HoverControl;
	import flare.vis.controls.SelectionControl;
	import flare.vis.data.Data;
	import flare.vis.data.NodeSprite;
	
	import flash.text.TextFormat;
	
	import uviface.viz.UFlareVis;
	import uviface.viz.control.UDragClickControl;
	import uviface.viz.control.UExpandControl;
	import uviface.viz.control.UGraphControls;
	import uviface.viz.control.UTooltipControl;
	import uviface.viz.operator.ULabeler;
	
	public class UVisualizationHelper
	{
		// -------------------------------------------------
		// the default controls
		// -------------------------------------------------
		
		/** The drag control for the nodes. */
		//private var _dragCtrlNode:DragControl = UGraphControls.nodeDragControl();
		/** The click control for the nodes. */
		private var _clickCtrlDataLoadNode:ClickControl = UGraphControls.nodeClickDataLoad();//ClickControl = UGraphControls.nodeClickDataLoad();
		/** The click control for the nodes. */
		//private var _clickCtrlRootUpdateNode:ClickControl = UGraphControls.nodeClickGraphRootUpdate();//ClickControl = UGraphControls.nodeClickGraphRootUpdate();
		private var _clickCtrlRootUpdateNode:UDragClickControl = UGraphControls.nodeClickGraphRootUpdate();//ClickControl = UGraphControls.nodeClickGraphRootUpdate();
		/** The click control for the nodes. */
		private var _clickCtrlLeaveLineageNode:ClickControl = UGraphControls.nodeClickLeaveLineageMode();//ClickControl = UGraphControls.nodeClickLeaveLineageMode();
		/** The hover control for the nodes. */
		private var _hoverCtrlNode:HoverControl = UGraphControls.nodeHoverColorSelect() 
		/** The selection control for the nodes. */
		//private var _selectionCtrlNode:SelectionControl;
		/** The click control for the edges. */
		private var _clickCtrlEdge:ClickControl = UGraphControls.edgeClickShowLineage();//ClickControl = UGraphControls.edgeClickShowLineage();
		/** The hover control for the edges. */
		private var _hoverCtrlEdge:HoverControl = UGraphControls.edgeHoverColorSelect(); 
		/** The tooltip control for the data sprites. */
		//private var _toolTipCtrlData:TooltipControl = UGraphControls.dataToolTipLabelDisplay();
		private var _toolTipCtrlData:UTooltipControl = UGraphControls.dataToolTipLabelDisplay();
		/** The click control to fix and unfix nodes -> especially useful for demos */
		private var _clickCtrlPositionFixingNode:ClickControl = UGraphControls.nodeClickPositionFixing();//ClickControl = UGraphControls.nodeClickDataLoad();
		
		//private var _selectionCtrlData:SelectionControl = UGraphControls.dataSelection();
		
		// -------------------------------------------------
		// the lineage controls
		// -------------------------------------------------
		/** The expand control for the nodes. */
		private var _expandCtrlNode:UExpandControl = new UExpandControl(NodeSprite);
		
		private static var _labelFormatterNodes:TextFormat;// = new TextFormat();
		//private static var _labelFormatterEdges:TextFormat;// = new TextFormat();
		
		private static var _labelerNodes:ULabeler;// = new Labeler("data.label",Data.NODES,_labelFormatterNodes);
		//private static var _labelerNodes:Labeler;// = new Labeler("data.label",Data.NODES,_labelFormatterNodes);
		//private static var _radialLabelerNodes:RadialLabeler;// = new Labeler("data.label",Data.NODES,_labelFormatterNodes);
		//private static var _labelerEdges:UEdgeLabeler;//RadialLabeler;// = new Labeler("data.label",Data.EDGES,_labelFormatterEdges,EdgeSprite,Labeler.CHILD);
		
		/**
		 * The constructor for the UVisualizationHelper object.
		 * 
		 * */
		public function UVisualizationHelper()
		{
			
			// adjust the label formatter for the nodes
			_labelFormatterNodes = new TextFormat();
			_labelFormatterNodes.font = "Verdana";
			//_labelFormatterNodes.font = "Georgia";
			//_labelFormatterNodes.color = UColors.BLACK;
			_labelFormatterNodes.color = UColors.WHITE;
			_labelFormatterNodes.size = 12;//16;
			//_labelFormatterNodes.bold = true;
			_labelFormatterNodes.bold = false;
			//_labelFormatterNodes.italic = true;
					
			// adjust the label formatter for the edges
			/*
			_labelFormatterEdges = new TextFormat();
			_labelFormatterEdges.font = "Verdana";
			//_labelFormatterEdges.color = UColors.BLACK;
			_labelFormatterEdges.color = UColors.WHITE;
			_labelFormatterEdges.size = 12;//16;
			_labelFormatterEdges.bold = true;
			*/
			//_labelerNodes = new Labeler("data.label",Data.NODES,_labelFormatterNodes);
			_labelerNodes = new ULabeler("data.label",Data.NODES,_labelFormatterNodes);
			//_radialLabelerNodes = new RadialLabeler("data.label",true,_labelFormatterNodes);
		    //_labelerEdges = new RadialLabeler("data.label",Data.EDGES,_labelFormatterEdges,EdgeSprite,Labeler.CHILD);
		    //_labelerEdges = new RadialLabeler("data.label",true,_labelFormatterEdges,EdgeSprite,Labeler.CHILD);
		    //_labelerEdges = new UEdgeLabeler("data.label",true,_labelFormatterEdges,EdgeSprite,Labeler.CHILD);
		    
		    //_labelerEdges.group = Data.EDGES;
		    //_labelerEdges = new Labeler("data.label",Data.EDGES,_labelFormatterEdges,EdgeSprite,Labeler.CHILD);
		    //_labelerNodes = new RadialLabeler("data.label",true,_labelFormatterNodes);
		    //_labelerEdges = new Labeler("data.label",Data.EDGES,_labelFormatterEdges,EdgeSprite,Labeler.CHILD);
		    _labelerNodes.textMode = TextSprite.EMBED;
		    //_radialLabelerNodes.textMode = TextSprite.EMBED;
		    //_labelerNodes.textMode = TextSprite.DEVICE;
		    //_labelerNodes.cacheText = true;
		    //_radialLabelerNodes.cacheText = true;
		    _labelerNodes.cacheText = true;
		    //_labelerEdges.cacheText = true;
		   // _labelerNodes.verticalAnchor = TextSprite.TOP;
		   // _labelerNodes.horizontalAnchor = TextSprite.CENTER;
			
		}
		
		public function get labelerNodes():ULabeler {
			return _labelerNodes;
		}
		/*
		public function get labelerNodes():Labeler {
			return _labelerNodes;
		}
		*/
		/*
		public function get radialLabelerNodes():RadialLabeler {
			return _radialLabelerNodes;
		}
		*/
		/*
		public function get labelerEdges():UEdgeLabeler {
			return _labelerEdges;
		}
		*/
		public function get labelFormatterNodes():TextFormat {
			return _labelFormatterNodes;
		}
		/*
		public function get labelFormatterEdges():TextFormat {
			return _labelFormatterEdges;
		}
		*/
		public function changeLabelFormatterNodes(font:String = "Verdana", color:uint = UColors.WHITE, 
		                                          size:uint = 12, bold:Boolean = true):void {
			_labelFormatterNodes.font = font;
			_labelFormatterNodes.color = color;
			_labelFormatterNodes.size = size;
			_labelFormatterNodes.bold = bold;
			// apply the changes to the node labeler
			// not necessary, because we use a reference of the formatter in the 
			// labeler itself, so changes should directly be reflected
		}
		
		/*
		public function changeLabelFormatterEdges(font:String = "Verdana", color:uint = UColors.WHITE, 
		                                          size:uint = 12, bold:Boolean = true):void {
			_labelFormatterEdges.font = font;
			_labelFormatterEdges.color = color;
			_labelFormatterEdges.size = size;
			_labelFormatterEdges.bold = bold;
			// apply the changes to the node labeler
			// not necessary, because we use a reference of the formatter in the 
			// labeler itself, so changes should directly be reflected
		}
		*/
		/*
		public function get labelFormatterEdges():TextFormat {
			return _labelFormatterEdges;
		}
		
		public function changeLabelFormatterEdges(font:String = "Verdana", color:uint = UColors.BLACK, 
		                                          size:uint = 10, bold:Boolean = true):void {
			_labelFormatterEdges.font = font;
			_labelFormatterEdges.color = color;
			_labelFormatterEdges.size = size;
			_labelFormatterEdges.bold = bold;
			// apply the changes to the edge labeler
			// not necessary, because we use a reference of the formatter in the 
			// labeler itself, so changes should directly be reflected
		}
		*/
		
		/**
		 * Applies all available controls to the visualisation control list.
		 * 
		 * @param currentLayout the layout whose controls hould be applied to the visualisation.
		 * @param flareVis the UFlareVis instance whose visualization and father canvas are used.
		 * @return the used panning and zooming control of this layout instance.
		 * */
		public function applyDefaultControls(flareVis:UFlareVis):void {	
			
			// are alway accessible
			//flareVis.visualization.controls.add(_dragCtrlNode);
			flareVis.visualization.controls.add(_hoverCtrlEdge);
			flareVis.visualization.controls.add(_hoverCtrlNode);
			flareVis.visualization.controls.add(_toolTipCtrlData);
			
			// are only accessible when not in lineage mode
			flareVis.visualization.controls.add(_clickCtrlEdge);
			flareVis.visualization.controls.add(_clickCtrlDataLoadNode);
			flareVis.visualization.controls.add(_clickCtrlRootUpdateNode);
			
			// are only accessible in lineage mode
			_expandCtrlNode.update = function():void { 
			    if(!UFlareVis.lineageMode)
				    return;
				else
			        flareVis.visualization.update(1).play(); 
			};
			flareVis.visualization.controls.add(_expandCtrlNode);
			flareVis.visualization.controls.add(_clickCtrlLeaveLineageNode);
			flareVis.visualization.controls.add(_clickCtrlPositionFixingNode);
			
			//_anchorCtrlNode.layout = flareVis.distortionFilter;
			//flareVis.visualization.controls.add(_anchorCtrlNode);
		}
		
		/**
		 * Applies all available controls to the visualisation control list.
		 * 
		 * @param currentLayout the layout whose controls hould be applied to the visualisation.
		 * @param flareVis the UFlareVis instance whose visualization and father canvas are used.
		 * @return the used panning and zooming control of this layout instance.
		 * */
		public function applyLineageControls(flareVis:UFlareVis):void {		
			_expandCtrlNode.update = function():void { 
			    if(!UFlareVis.lineageMode)
				      return;
			    flareVis.visualization.update(1).play(); 
			};
			flareVis.visualization.controls.add(_expandCtrlNode);
			flareVis.visualization.controls.add(_clickCtrlLeaveLineageNode);
			//flareVis.visualization.controls.add(currentLayout.dragCtrlNode);
			flareVis.visualization.controls.add(_toolTipCtrlData);
			flareVis.visualization.controls.add(_hoverCtrlEdge);
			flareVis.visualization.controls.add(_hoverCtrlNode);
		}

	}
}