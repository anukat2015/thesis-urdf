package uviface.viz.display
{
	import flare.animate.Transitioner;
	import flare.display.TextSprite;
	import flare.query.methods.eq;
	import flare.util.Shapes;
	import flare.vis.data.EdgeSprite;
	import flare.vis.data.render.ArrowType;
	import flare.vis.data.render.EdgeRenderer;
	
	import flash.filters.*;
	
	import uviface.event.UComparisonColorEvent;
	import uviface.viz.UFlareVis;
	import uviface.viz.operator.UEdgeRenderer;
	import uviface.viz.util.UColors;
	import uviface.viz.util.UStringConstants;
	
	public class UEdge
	{
		
		import mx.collections.ArrayCollection;
		
		/** The name of the edge. */
		//private var _name:String = "Default";
		/** The shape of the edge. */
		//private var _shape:String = Shapes.LINE;
		/** The line color of the edge. */
		//private var _lineColor:uint = UColors.grey(3);
		/** The line width of the edge. */
	    //private var _lineWidth:uint  = 1;
		/** The alpha valu of the edge. */
		//private var _alpha:Number = 1;
		/** The visibility of the edge. */
		//private var _visible:Boolean = true;
		/** The direction flag for the edge. */
		//private var _directed:Boolean = true;
		/** The type of arrow to use for this edge. */
		//private var _arrowType:String = ArrowType.TRIANGLE;
		/** The arrow width to use for this edge (-1 means auto). */
		//private var _arrowWidth:int = -1; // auto controlled
		/** The arrow height to use for this edge (-1 means auto). */
		//private var _arrowHeight:int = -1; // auto controlled	
		
		private static var _blackEdgeGlowFilter:GlowFilter = new GlowFilter(UColors.BLACK,1,6.0,6.0,15);
		private static var _yellowEdgeGlowFilter:GlowFilter = new GlowFilter(UColors.yellow(7),1,6.0,6.0,15);
		private static var _whiteEdgeGlowFilter:GlowFilter = new GlowFilter(UColors.WHITE,1,6.0,6.0,15);
		//public static var _magentaEdgeGlowFilter:GlowFilter = new GlowFilter(UColors.magenta(7),1,6.0,6.0,10);
		//private static var _filters:Array;
		private static var _compareFiltersBlackGlow:Array = [_blackEdgeGlowFilter];
		private static var _compareFiltersYellowGlow:Array = [_yellowEdgeGlowFilter];
		private static var _compareFiltersWhiteGlow:Array = [_whiteEdgeGlowFilter];
		//private static var _compareFiltersMagentaGlow:Array = [_magentaEdgeGlowFilter];
		
		/** The dynamic object that manages all the relevant edge properties.
		 * Flare needs this properties object structure. */
		private var _edgeProperties:Object = 
		   {
		   	    /* The name of the edge. */
				name: UStringConstants.DEFAULT,
				/* The shape of the edge. */
				shape: Shapes.LINE,//computeShape(),
				/* The line color of the edge. */
				lineColor: UColors.WHITE,//UColors.grey(3),
				/* The line width of the edge. */
			    lineWidth: 3,
				/* The alpha valu of the edge. */
				alpha: 1.0,//UMath.roundNumber(parseFloat("data.confidence"),2),
				/* The line alpha valu of the edge. */
				lineAlpha: 1.0,//UMath.roundNumber(parseFloat("data.confidence"),2),
				/* The visibility of the edge. */ 
				visible: true,//neq("data.hide",true),
				/* The direction flag for the edge. */
				directed: true,
				/* The mouse mode of the node */
				mouseEnabled: true,//eq("data.lineage",true),
				/* The type of arrow to use for this edge. */
				arrowType: ArrowType.TRIANGLE,
				/* The arrow width to use for this edge (-1 means auto). */
				arrowWidth: -1, // auto controlled
				/* The arrow height to use for this edge (-1 means auto). */
				arrowHeight: -1, // auto controlled	
				/* Sets the preferred dege renderer, in this case my own renderer. */
				edgeRenderer: UEdgeRenderer.instance//computeEdgeRenderer()//UEdgeRenderer.instance
		   }		
		
		/** A flag indicating if the confidences of the facts should be
		 * reflected in the alpha encoding of the edges. */
		private var _useAlpha:Boolean = true;
		
		/** The filters of the edge. */
		private var _filters:Array = null;
		/** The internal list of edges. */
		private var _edgesArray:Array = new Array();
		/** The internal map of edges. */
		private static var _edgesMap:Object = new Object();
		/** The internal list of edge names. */
		private var _edgeNames:ArrayCollection = new ArrayCollection();
		
		// the color values to indicate the truth values of edges
		private var _trueEdgeColor:uint = UColors.green(7);
		private var _falseEdgeColor:uint = UColors.red(7);
		
		/** The default edge. */
		public static const DEFAULT_EDGE:UEdge = new UEdge(UStringConstants.DEFAULT,UColors.green(7));
		/** The lineage node. */
		public static var LINEAGE_EDGE:UEdge = new UEdge(UStringConstants.LINEAGE,UColors.yellow(4));
		/** The lineage node. */
		//public static var INACTIVE_EDGE:UEdge = new UEdge(UStringConstants.INACTIVE,UColors.WHITE);
		/** The lineage node. */
		//public static var DEPENDENCY_EDGE:UEdge = new UEdge(UStringConstants.DEPENDENCY,UColors.WHITE);
		
		// the static initializer
		{
			DEFAULT_EDGE.initUEdgeFull(Shapes.BSPLINE, 3, 1.0, 1.0, ArrowType.TRIANGLE,UEdgeRenderer.instance, true);
			LINEAGE_EDGE.initUEdgeFull(Shapes.LINE, 3, 1.0, 1.0, ArrowType.TRIANGLE,UEdgeRenderer.instance, true);
			//LINEAGE_EDGE.trueEdgeColor = UColors.yellow(6);
			//LINEAGE_EDGE.falseEdgeColor = UColors.yellow(6);
			//INACTIVE_EDGE.initUEdge(3);
			//DEPENDENCY_EDGE.initUEdgeFull(Shapes.BEZIER, 3, 1, 1, ArrowType.TRIANGLE,UEdgeRenderer.instance, false);
		}
		
		/**
		 * The constructor of the UEdge object.
		 * 
		 * @param name the name for the UEdge object.
		 * @param lineColor the line color for the UEdge object.
		 * @param lineWidth the line width for the UEdge object.
		 * @param directed the direction flag for the UEdge object.
		 * */
		public function UEdge(name:String, lineColor:uint, directed:Boolean = true)
		{
			_edgeProperties[UStringConstants.NAME] = name;
			_edgeProperties[UStringConstants.LINE_COLOR] = lineColor;
			_edgeProperties[UStringConstants.DIRECTED] = directed;
			// add this node to the internal list of nodes
			_edgesArray.push(this);
			_edgesMap[name] = this;
			_edgeNames.addItem(name);
		}
		
		/**
		 * Initializes the UEdge object.
		 * 
		 * @param shape the shape for the UEdge object.
		 * @param lineWidth the line width for the UEdge object.
		 * */
		public function initUEdge(lineWidth:uint):void {
		                          	
			//_edgeProperties[UStringConstants.SHAPE] = shape;
			_edgeProperties[UStringConstants.LINE_WIDTH] = lineWidth;
		}
		
		public function initUEdgeFull(shape:String, lineWidth:uint, alpha:Number , lineAlpha:Number, arrowType:String, edgeRenderer:EdgeRenderer,   
		                          mouseEnabled:Boolean = true, arrowWidth:int = -1, arrowHeight:int = -1, 
		                          visible:Boolean = true, filters:Array = null):void {
		                          	
			_edgeProperties[UStringConstants.SHAPE] = shape;
			_edgeProperties[UStringConstants.LINE_WIDTH] = lineWidth;
			_edgeProperties[UStringConstants.ALPHA] = alpha;
			_edgeProperties[UStringConstants.LINE_ALPHA] = lineAlpha;
			_edgeProperties[UStringConstants.VISIBLE] = visible;
			_edgeProperties[UStringConstants.MOUSE_ENABLED] = mouseEnabled;
			_edgeProperties[UStringConstants.ARROW_TYPE] = arrowType;
			_edgeProperties[UStringConstants.ARROW_WIDTH] = arrowWidth;
			_edgeProperties[UStringConstants.ARROW_HEIGHT] = arrowHeight;
			_edgeProperties[UStringConstants.EDGE_RENDERER] = edgeRenderer;
			_filters = filters;
		}
		
		public static function changeComparisonFilterColors(blackFilter:int, yellowFilter:int, whiteFilter:int):void {
			_blackEdgeGlowFilter.color = blackFilter;
		    _yellowEdgeGlowFilter.color = yellowFilter;
		    _whiteEdgeGlowFilter.color = whiteFilter;
		}
		
		public static function changeComparisonFilterColor(event:UComparisonColorEvent):void {
			switch(event.colorToChange) {
			   case UComparisonColorEvent.BLACK_CLUSTER: _blackEdgeGlowFilter.color = event.color;break;
		       case UComparisonColorEvent.YELLOW_CLUSTER: _yellowEdgeGlowFilter.color = event.color;break;
		       case UComparisonColorEvent.WHITE_CLUSTER: _whiteEdgeGlowFilter.color = event.color;break;
		    }
		}
		
		public function get useAlpha():Boolean {
			return _useAlpha;
		}
		
		public function set useAlpha(useAlpha:Boolean):void {
			_useAlpha = useAlpha;
		}
		
		/** 
		 * Delivers the name of the UEdge object. 
		 * 
		 * @return the name.
		 * */
		public function get name():String {
			return _edgeProperties[UStringConstants.NAME];
		}
		
		/**
		 * Delivers the mouseEnabled flag of this UEdge object.
		 * 
		 * @return the mouseEnabled flag in use.
		 * */
		public function get mouseEnabled():Boolean {
			return _edgeProperties[UStringConstants.MOUSE_ENABLED];
		}
		
		/**
		 * Sets the mouseEnabled flag for this UEdge object.
		 * 
		 * @param mouseEnabled the mouseEnabled flag to use.
		 * */
		public function set mouseEnabled(mouseEnabled:Boolean):void {
			_edgeProperties[UStringConstants.MOUSE_ENABLED] = mouseEnabled;
		}
		
		/**
		 * Delivers the renderer of this UEdge object.
		 * 
		 * @return the renderer in use.
		 * */
		public function get edgeRenderer():EdgeRenderer {
			return _edgeProperties[UStringConstants.EDGE_RENDERER];
		}
		
		/**
		 * Sets the renderer for this UEdge object.
		 * 
		 * @param edgeRenderer the renderer to use.
		 * */
		public function set edgeRenderer(edgeRenderer:EdgeRenderer):void {
			//if(Shapes.getShape(shape) == null)
			  // return;
			_edgeProperties[UStringConstants.EDGE_RENDERER] = edgeRenderer;//shape;
		}
		
		/**
		 * Delivers the shape (shape string) of this UEdge object.
		 * 
		 * @return the shape in use.
		 * */
		public function get shape():String {
			return _edgeProperties[UStringConstants.SHAPE];
		}
		
		/**
		 * Sets the shape (shape string) for this UEdge object.
		 * 
		 * @param shape the shape to use.
		 * */
		public function set shape(shape:String):void {
			if(Shapes.getShape(shape) == null)
			   return;
			else _edgeProperties[UStringConstants.SHAPE] = shape;
		}
		
		/**
		 * Delivers the line color of this UEdge object.
		 * 
		 * @return the line color in use.
		 * */
		public function get lineColor():uint {
			return _edgeProperties[UStringConstants.LINE_COLOR];
		}
		
		/**
		 * Sets the line color for this UEdge object.
		 * 
		 * @param lineColor the line color to use.
		 * */
		public function set lineColor(lineColor:uint):void {
			_edgeProperties[UStringConstants.LINE_COLOR] = lineColor;
		}
		
		/**
		 * Delivers the line width of this UEdge object.
		 * 
		 * @return the line width of the edge.
		 * */
		public function get lineWidth():uint {
			return _edgeProperties[UStringConstants.LINE_WIDTH];
		}
		
		/**
		 * Sets the line width for this UEdge object.
		 * 
		 * @param lineWidth the line width for the edge.
		 * */
		public function set lineWidth(lineColor:uint):void {
			_edgeProperties[UStringConstants.LINE_WIDTH] = lineWidth;
		}
		
		/**
		 * Delivers the alpha value of this UEdge object.
		 * 
		 * @return the alpha value of the edge.
		 * */
		public function get alpha():Number {
			return _edgeProperties[UStringConstants.ALPHA];
		}
		
		/**
		 * Sets the alpha value for this UEdge object.
		 * 
		 * @param alpha the alpha value for this edge.
		 * */
		public function set alpha(alpha:Number):void {
			_edgeProperties[UStringConstants.ALPHA] = alpha;
		}
		
		/**
		 * Delivers the line alpha value of this UEdge object.
		 * 
		 * @return the line alpha value of the edge.
		 * */
		public function get lineAlpha():Number {
			return _edgeProperties[UStringConstants.LINE_ALPHA];
		}
		
		/**
		 * Sets the line alpha value for this UEdge object.
		 * 
		 * @param line alpha the alpha value for this edge.
		 * */
		public function set lineAlpha(lineAlpha:Number):void {
			_edgeProperties[UStringConstants.LINE_ALPHA] = lineAlpha;
		}
		
		/**
		 * Delivers the visibility flag of this UEdge object.
		 * 
		 * @return the visibility flag of the edge.
		 * */
		public function get visible():Boolean {
			return _edgeProperties[UStringConstants.VISIBLE];
		}
		
		/**
		 * Sets the visibility flag for this UEdge object.
		 * 
		 * @param visible the visibility flag for this edge.
		 * */
		public function set visible(visible:Boolean):void {
			_edgeProperties[UStringConstants.VISIBLE] = visible;
		}
		
		/**
		 * Delivers the directed flag of this UEdge object.
		 * 
		 * @return the directed flag of the edge.
		 * */
		public function get directed():Boolean {
			return _edgeProperties[UStringConstants.DIRECTED];
		}
		
		/**
		 * Sets the directed flag for this UEdge object.
		 * 
		 * @param directed the directed flag for this edge.
		 * */
		public function set directed(directed:Boolean):void {
			_edgeProperties[UStringConstants.DIRECTED] = directed;
		}
		
		/**
		 * Delivers the arrow type of this UEdge object.
		 * 
		 * @return the arrow type of the edge.
		 * */
		public function get arrowType():String {
			return _edgeProperties[UStringConstants.ARROW_TYPE];
		}
		
		/**
		 * Sets the arrow type for this UEdge object.
		 * 
		 * @param arrowType the arrow type for this edge.
		 * */
		public function set arrowType(arrowType:String):void {
			_edgeProperties[UStringConstants.ARROW_TYPE] = arrowType;
		}
		
		/**
		 * Delivers the arrow width of this UEdge object.
		 * 
		 * @return the arrow width of the edge.
		 * */
		public function get arrowWidth():int {
			return _edgeProperties[UStringConstants.ARROW_WIDTH];
		}
		
		/**
		 * Sets the arrow width for this UEdge object.
		 * 
		 * @param arrowWidth the arrow width for this edge.
		 * */
		public function set arrowWidth(arrowWidth:int):void {
			_edgeProperties[UStringConstants.ARROW_WIDTH] = arrowWidth;
		}
		
		/**
		 * Delivers the arrow height of this UEdge object.
		 * 
		 * @return the arrow height of the edge.
		 * */
		public function get arrowHeight():int {
			return _edgeProperties[UStringConstants.ARROW_HEIGHT];
		}
		
		/**
		 * Sets the arrow height for this UEdge object.
		 * 
		 * @param arrowHeight the height width for this edge.
		 * */
		public function set arrowHeight(arrowHeight:int):void {
			_edgeProperties[UStringConstants.ARROW_HEIGHT] = arrowHeight;
		}
		
		public function get trueEdgeColor():uint {
			return _trueEdgeColor;
		}
		
		public function set trueEdgeColor(trueEdgeColor:uint):void {
			_trueEdgeColor = trueEdgeColor;
		}
		
		public function get falseEdgeColor():uint {
			return _falseEdgeColor;
		}
		
		public function set falseEdgeColor(falseEdgeColor:uint):void {
			_falseEdgeColor = falseEdgeColor;
		}
		
		/**
		 * Delivers the used filters of this UEdge object.
		 * 
		 * @return the used filters of the edge.
		 * */
		public function get filters():Array {
			return _filters;
		}
		
		/**
		 * Sets the filters for this UEdge object.
		 * 
		 * @param alpha the filters for this edge.
		 * */
		public function set filters(filters:Array):void {
			_filters = filters;
		}
		
		/**
		 * Delivers the list of available UEdge objects.
		 * 
		 * @return the list of available UEdge objects.
		 * */
		public function get edgesArray():Array {
			return _edgesArray;
		}
		
		/**
		 * Delivers the map of available UEdge objects.
		 * 
		 * @return the map of available UEdge objects.
		 * */
		public function get edgesMap():Object {
			return _edgesMap;
		}
		
		/**
		 * Delivers the names of the available UEdge objects.
		 * 
		 * @return the names of the available UEdge objects.
		 * */
		public function get edgeNames():ArrayCollection {
			return _edgeNames;
		}
		
		public function get edgeProperties():Object {
			return _edgeProperties;
		}
		
		/**
		 * Delivers the UEdge object belonging to the given name.
		 * 
		 * @return the UEdge object belonging to the given name
		 *         , or null (undefined), if no such edge exists.
		 * */
		public static function edge(edge:String):UEdge {
			
			if(!_edgesMap.hasOwnProperty(edge))
			   return null;
			else 
			   return _edgesMap[edge];
		}
		
		private function computeShape():String {
			if(eq("data.lineage",true)) 
			   return Shapes.BEZIER; 
			else 
			   return Shapes.LINE;
		}
		
		private function computeEdgeRenderer():EdgeRenderer {
			if(eq("data.lineage",true)) 
			   return UEdgeRenderer.instance; 
			else 
			   return EdgeRenderer.instance;
		}
		
		public static function changeEdgeAlpha(edge:EdgeSprite, t:Transitioner = null, edgeType:UEdge = null):void {
			
			var confidence:Number;
			
			// check, if we use alpha encodings to reflect the confidences of facts
			if(edgeType==null) {
				if(UEdge.DEFAULT_EDGE.useAlpha == false) {
				   
				   if(t==null) {
				      edge.lineAlpha = 1.0;
				      //edge.lineColor = UApplicationControlBar.alphaRGB(edge.lineColor,1);
				   }
				   else {
				      t.$(edge).lineAlpha = 1.0;
				      //t.$(edge).lineColor = UApplicationControlBar.alphaRGB(edge.lineColor,1);
				   }
				   
				   return;
				}
			}
			else {
				if(edgeType.useAlpha == false) {
				   
				   if(t==null) {
				      edge.lineAlpha = 1.0;
				      //edge.lineColor = UApplicationControlBar.alphaRGB(edge.lineColor,1);
				   }
				   else {
				      t.$(edge).lineAlpha = 1.0;
				      //t.$(edge).lineColor = UApplicationControlBar.alphaRGB(edge.lineColor,1);
				   }
				   
				   return;
				}
			}
			
			confidence = parseFloat(edge.data.confidence);
			//if(confidence <= 0.01)
			  // confidence = 0.25;
			//else
			  // confidence = UMath.roundNumber(parseFloat(edge.data.confidence),2);
			
			if(t==null) {
				
				//edge.lineAlpha = confidence;
				//return;
				if(confidence >= 0 && confidence <= 0.25){
				   edge.lineAlpha = 0.25;
				   return;
				}
				if(confidence > 0.25 && confidence <= 0.5){
				   edge.lineAlpha = 0.5;
				   return;
				}
				if(confidence > 0.5 && confidence <= 0.75){
				   edge.lineAlpha = 0.75;
				   return;
				}
				if(confidence > 0.75 && confidence <= 1){
				   edge.lineAlpha = 1.0;
				   return;
				}
				
					
			}
			else {
				
				//t.$(edge).lineAlpha = confidence;
				//return;
				if(confidence >= 0 && confidence <= 0.25){
				   t.$(edge).lineAlpha = 0.25;
				   return;
				}
				if(confidence > 0.25 && confidence <= 0.5){
				   t.$(edge).lineAlpha = 0.5;
				   return;
				}
				if(confidence > 0.5 && confidence <= 0.75){
				   t.$(edge).lineAlpha = 0.75;
				   return;
				}
				if(confidence > 0.75 && confidence <= 1){
				   t.$(edge).lineAlpha = 1.0;
				   return;
				}
					
			}	
					
		}
		
		public static function changeEdgeAlphaOfObject(obj:Object, edge:EdgeSprite, edgeType:UEdge = null):void {
			
			var confidence:Number;
			
			// check, if we use alpha encodings to reflect the confidences of facts
			if(edgeType==null) {
				if(UEdge.DEFAULT_EDGE.useAlpha == false) {
				   obj.lineAlpha = 1.0;
				   return;
				}
			}
			else {
				if(edgeType.useAlpha == false) { 
				   obj.lineAlpha = 1.0;
				   return;
				}
			}
			
			confidence = parseFloat(edge.data.confidence);
			
			if(confidence >= 0 && confidence <= 0.25){
			   obj.lineAlpha = 0.25;
			   return;
			}
			if(confidence > 0.25 && confidence <= 0.5){
			   obj.lineAlpha = 0.5;
			   return;
			}
			if(confidence > 0.5 && confidence <= 0.75){
			   obj.lineAlpha = 0.75;
			   return;
			}
			if(confidence > 0.75 && confidence <= 1){
			   obj.lineAlpha = 1.0;
			   return;
			}
							
		}
		
		public static function changeEdgeColor(edge:EdgeSprite, edgeType:UEdge, t:Transitioner = null):void {
			// compute the color
			
			if(t==null) {
				if(edge.data.hasOwnProperty(UStringConstants.TRUTH_VALUE)) {
					switch(edge.data.truthValue) {
						case 0: edge.lineColor = edgeType.falseEdgeColor;break;
						case 1: edge.lineColor = edgeType.trueEdgeColor;break;
						case 2: edge.lineColor = UColors.grey(7);break;
						default: edge.lineColor = edgeType.falseEdgeColor;break;
					}
				}
				else {
				      edge.lineColor = edgeType.lineColor;
				}	
			}
			else {
				if(edge.data.hasOwnProperty(UStringConstants.TRUTH_VALUE)) {
					switch(edge.data.truthValue) {
						case 0: t.$(edge).lineColor = edgeType.falseEdgeColor;break;
						case 1: t.$(edge).lineColor = edgeType.trueEdgeColor;break;
						case 2: t.$(edge).lineColor = UColors.grey(7);break;
						default: t.$(edge).lineColor = edgeType.falseEdgeColor;break;
					}
				}
				else {
				      t.$(edge).lineColor = edgeType.lineColor;
				}	
			}
			
			// now get the alpha value again
			changeEdgeAlpha(edge,t);
		}
		
		public static function changeEdgeColorOfObject(obj:Object, edge:EdgeSprite, edgeType:UEdge = null):void {
			// compute the color
			
			if(edgeType == null)
			   edgeType = UEdge.DEFAULT_EDGE;
			
			if(edge.data.hasOwnProperty(UStringConstants.TRUTH_VALUE)) {
				switch(edge.data.truthValue) {
					case 0: obj.lineColor = edgeType.falseEdgeColor;break;
					case 1: obj.lineColor = edgeType.trueEdgeColor;break;
					case 2: obj.lineColor = UColors.grey(7);break;
					default: obj.lineColor = edgeType.falseEdgeColor;break;
				}
			}
			else {
			      obj.lineColor = edgeType.lineColor;
			}	
			
			// now get the alpha value again
			UEdge.changeEdgeAlphaOfObject(obj,edge,edgeType);
		}
		
		/**
		 * Applies the internally managed values to the given EdgeSprite instance.
		 * 
		 * @param edge the EdgeSprite instance to apply the properties on.
		 * @param edgeType the UEdge instance to use for the application.
		 * */
		public static function applyValuesToEdge(edge:EdgeSprite, edgeType:UEdge, compareModeEnabled:Boolean = false):void {
			
			edge.shape = edgeType.shape;
			
			if(edge.data.hasOwnProperty(UStringConstants.TRUTH_VALUE)) {
				switch(edge.data.truthValue) {
					case 0: edge.lineColor = edgeType.falseEdgeColor;break;
					case 1: edge.lineColor = edgeType.trueEdgeColor;break;
					case 2: edge.lineColor = UColors.grey(7);break;
					default: edge.lineColor = edgeType.falseEdgeColor;break;
				}
			}
			else {
			      edge.lineColor = edgeType.lineColor;
			}
			
			// apply the line width   
			edge.lineWidth = edgeType.lineWidth;
			
			// compute the alpha
			if(edge.data.hasOwnProperty(UStringConstants.CONFIDENCE)) {
			   changeEdgeAlpha(edge);
			}
			else {
			   // in case, we have a super edge connecting two subgraphs
			   if(edge.data.hasOwnProperty(UStringConstants.SUPER_EDGE)) {
			      edge.lineColor = UColors.magenta(7);
			      edge.lineAlpha = 0;
			   }
			   else {
			      edge.alpha = edgeType.alpha;
			   }
			}
			
			// set the edge renderer
			edge.renderer = edgeType.edgeRenderer;
			
			// compute the alpha
			if(UFlareVis.lineageMode) {
						
				if(edge.data.hasOwnProperty("linTree")) {
				   edge.visible = true;//nodeType.visible;
				   edge.lineAlpha = 1;
				}
				else
				   edge.visible = false;
			
			}
		
			edge.directed = edgeType.directed;
			edge.arrowType = edgeType.arrowType;
			edge.arrowWidth = edgeType.arrowWidth;
			edge.arrowHeight = edgeType.arrowHeight;
			edge.mouseEnabled = edgeType.mouseEnabled;
			
			// ---------------------------------------------------------------
			// ---------------------------------------------------------------
			// we are in the comparison mode
			// ---------------------------------------------------------------
			// ---------------------------------------------------------------
			
			if(compareModeEnabled) {
			    
			    // we do not set a new filter in lineage mode
			    //if(UFlareVis.lineageMode)
			      //return;
			    
			    if(edge.data.hasOwnProperty(UStringConstants.SUPER_EDGE)) {
					edge.filters = null;//_compareFiltersMagentaGlow;					
					return;		
			    }
			    
				// a bit redundant, but it works
				if(!edge.data.hasOwnProperty(UStringConstants.OLD)) {
				    edge.filters = null;
			        return;
				}
				
				// the node existed before the changed query result came in
				if(edge.data.hasOwnProperty(UStringConstants.OLD) && edge.data.Old == true) {
					// the node exists in the new query result as well
					if(edge.data.hasOwnProperty(UStringConstants.NEW) && edge.data.New == true) {
						edge.filters = _compareFiltersYellowGlow;					
						return;
						
					}
					// the node does not exist in the new query result
				    else {
						edge.filters = _compareFiltersBlackGlow;	
				    	return;
				    }
				}
				else {
				   edge.filters = _compareFiltersWhiteGlow;				   
				   return;
			    }
			    
			    
			    // remove the glow from the textsprite
			    TextSprite(edge.data.TS).filters = null;
		    
		    }
			
		}
		
		public static function applyGlowToEdge(edge:EdgeSprite, edgeType:UEdge):void {
			
			// ---------------------------------------------------------------
			// ---------------------------------------------------------------
			// we are in the comparison mode
			// ---------------------------------------------------------------
			// ---------------------------------------------------------------
			
		    // we do not set a new filter in lineage mode
		    //if(UFlareVis.lineageMode)
		      //return;
		    
		    if(edge.data.hasOwnProperty(UStringConstants.SUPER_EDGE)) {
				edge.filters = null;//_compareFiltersMagentaGlow;
				
				return;		
		    }
		    
			// a bit redundant, but it works
			if(!edge.data.hasOwnProperty(UStringConstants.OLD)) {
			    edge.filters = null;
			    return;
			}
			 
			// the node existed before the changed query result came in
			if(edge.data.hasOwnProperty(UStringConstants.OLD) && edge.data.Old == true) {
				// the node exists in the new query result as well
				if(edge.data.hasOwnProperty(UStringConstants.NEW) && edge.data.New == true) {
					edge.filters = _compareFiltersYellowGlow;					
					return;
					
				}
				// the node does not exist in the new query result
			    else {
					edge.filters = _compareFiltersBlackGlow;		    	
			    	return;
			    }
			}
			else {
			   edge.filters = _compareFiltersWhiteGlow;	   
			   return;
		    }
		    
		    // remove the glow from the textsprite
			TextSprite(edge.data.TS).filters = null;
		    		
		}

	}
}