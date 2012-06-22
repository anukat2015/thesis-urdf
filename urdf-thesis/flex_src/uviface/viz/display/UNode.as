package uviface.viz.display
{
	import flare.animate.Transitioner;
	import flare.util.Shapes;
	import flare.vis.data.NodeSprite;
	
	import flash.filters.*;
	
	import mx.collections.ArrayCollection;
	
	import util.UMath;
	
	import uviface.event.UComparisonColorEvent;
	import uviface.viz.UFlareVis;
	import uviface.viz.operator.UNodeRenderer;
	import uviface.viz.operator.UShapes;
	import uviface.viz.util.UColors;
	import uviface.viz.util.UStringConstants;
	
	public class UNode
	{
		
		private static var _blackNodeGlowFilter:GlowFilter = new GlowFilter(UColors.BLACK,1,6.0,6.0,15);
		private static var _yellowNodeGlowFilter:GlowFilter = new GlowFilter(UColors.yellow(7),1,6.0,6.0,15);
		private static var _whiteNodeGlowFilter:GlowFilter = new GlowFilter(UColors.WHITE,1,6.0,6.0,15);
		//private static var _filters:Array;
		private static var _compareFiltersBlackGlow:Array = [_blackNodeGlowFilter];
		private static var _compareFiltersYellowGlow:Array = [_yellowNodeGlowFilter];
		private static var _compareFiltersWhiteGlow:Array = [_whiteNodeGlowFilter];
		
		/** The name of the node. */
		//private var _name:String = "Default";
		/** The shape of the node. */
		//private var _shape:String = Shapes.CIRCLE;
		/** The fill color of the node. */
		//private var _fillColor:uint = 0x88aaaaaa;
		/** The line color of the node. */
		//private var _lineColor:uint = 0xdddddddd;
		/** The line width of the node. */
	    //private var _lineWidth:uint  = 1;
	    /** The size of the node. */
		//private var _size:Number = 1.5;
		/** The alpha valu of the node. */
		//private var _alpha:Number = 1;
		/** The visibility of the node. */
		//private var _visible:Boolean = true; 
		
		/** The dynamic object that manages all the relevant node properties.
		 * Flare needs this properties object structure. */
		private var _nodeProperties:Object = 
		   {
		   	    /* The name of the node. */
				name: UStringConstants.DEFAULT,
				/* The shape of the node. */
				shape: Shapes.POLYBLOB,//null,//Shapes.CIRCLE,//Shapes.POLYBLOB,
				/* The fill color of the node. */
				fillColor: UColors.grey(5),
				/* The line color of the node. */
				lineColor: UColors.blue(3),//UColors.BLACK,
				/* The line width of the node. */
			    lineWidth: 3,
			    /* The size of the node. */
				size: 3.0,
				/* The alpha valu of the node. */
				fillAlpha: 1,
				/* The visibility of the node. */
				visible: true,//neq("data.hide",true),//true,
				/* The button mode of the node */
				buttonMode : true,//eq("data.lineage",true),//true,
				/* The mouse mode of the node */
				mouseEnabled: true,//eq("data.lineage",true)//true
				/* The filters of the node (array of filters) */
				//filters : [new GlowFilter(4)] //[new DropShadowFilter(4),new GlowFilter(4)] 
				/* Sets the preferred dege renderer, in this case my own renderer. */
				nodeRenderer: UNodeRenderer.instance//computeEdgeRenderer()//UEdgeRenderer.instance
		   }
		
		private var _trueFactColor:uint = UColors.green(3);
		private var _falseFactColor:uint = UColors.red(3);
		private var _unknownFactColor:uint = UColors.grey(3);
		private var _oldColor:uint = UColors.BLACK;
		private var _bothColor:uint = UColors.yellow(4);
		private var _newColor:uint = UColors.WHITE;
		
		/** The filters of the node. */
		//private var _filters:Array = null;
		/** The internal list of nodes. */
		private var _nodesArray:Array = new Array();
		/** The internal map of nodes. */
		private static var _nodesMap:Object = new Object();
		/** The internal list of node names. */
		private var _nodeNames:ArrayCollection = new ArrayCollection();
		
		/** The default node. */
		public static const DEFAULT_NODE:UNode = new UNode(UStringConstants.DEFAULT,UColors.red(2),UColors.yellow(4));
		/** The lineage node. */
		public static var LINEAGE_NODE:UNode = new UNode(UStringConstants.LINEAGE,UColors.grey(1),UColors.yellow(4));
		/** The lineage node. */
		//public static var INACTIVE_NODE:UNode = new UNode(UStringConstants.INACTIVE,UColors.grey(5),UColors.blue(3));
		/** The lineage node. */
		//public static var DEPENDENCY_NODE:UNode = new UNode(UStringConstants.DEPENDENCY,UColors.grey(5),UColors.blue(3));
		
		// the static initializer
		{
			//DEFAULT_NODE.initUNodeFull(Shapes.POLYBLOB, 5, 5, 1, true, true, true);
			//LINEAGE_NODE.initUNodeFull(Shapes.POLYBLOB, 5, 5, 1, true, true, true);
			DEFAULT_NODE.initUNodeFull(UShapes.ROUNDED_RECT, 4, 5, 1, true, true, true);
			LINEAGE_NODE.initUNodeFull(UShapes.ROUNDED_RECT, 4, 5, 1, true, true, true);
			//INACTIVE_NODE.initUNode(Shapes.POLYBLOB, 3, 3, 0.3);
			//DEPENDENCY_NODE.initUNodeFull(null, 3, 0, 1, true, true, true);
		}
		
		/**
		 * The constructor of the UNode object.
		 * 
		 * @param name the name for the UNode object.
		 * @param fillColor the fill color for the UNode object.
		 * @param lineColor the line color for the UNode object.
		 * */
		public function UNode(name:String, fillColor:uint, lineColor:uint)
		{
			_nodeProperties[UStringConstants.NAME] = name;
			_nodeProperties[UStringConstants.FILL_COLOR] = fillColor;
			_nodeProperties[UStringConstants.LINE_COLOR] = lineColor;
			// add this node to the internal list of nodes
			_nodesArray.push(this);
			_nodesMap[name] = this;
			_nodeNames.addItem(name);
		}
		
		/**
		 * Initializes the UNode object.
		 * 
		 * @param shape the shape for the UNode object.
		 * @param lineWidth the line width for the UNode object.
		 * @param alpha the alpha value for the UNode object.
		 * @param visible the visibility flag for the UNode object.
		 * @param filters the filters for the UNode object.
		 * */
		public function initUNodeFull(shape:String, lineWidth:uint, size:Number, alpha:Number, visible:Boolean = true, mouseEnabled:Boolean =  true,
		                           buttonMode:Boolean = true, filters:Array = null):void {
			_nodeProperties[UStringConstants.SHAPE] = shape;
			_nodeProperties[UStringConstants.LINE_WIDTH] = lineWidth;
			_nodeProperties[UStringConstants.SIZE] = size;
			_nodeProperties["fillAlpha"] = alpha;
			_nodeProperties[UStringConstants.VISIBLE] = visible;
			_nodeProperties[UStringConstants.BUTTON_MODE] = buttonMode;
			_nodeProperties[UStringConstants.MOUSE_ENABLED] = mouseEnabled;
			//_nodeProperties[UStringConstants.FILTERS] = filters;
		}
		
		public function initUNode(shape:String, lineWidth:uint, size:Number, alpha:Number):void {
			_nodeProperties[UStringConstants.SHAPE] = shape;
			_nodeProperties[UStringConstants.LINE_WIDTH] = lineWidth;
			_nodeProperties[UStringConstants.SIZE] = size;
			_nodeProperties["fillAlpha"] = alpha;
			//_nodeProperties[UStringConstants.FILTERS] = filters;
		}
		
		public static function changeComparisonFilterColors(blackFilter:int, yellowFilter:int, whiteFilter:int):void {
			_blackNodeGlowFilter.color = blackFilter;
		    _yellowNodeGlowFilter.color = yellowFilter;
		    _whiteNodeGlowFilter.color = whiteFilter;
		}
		
		public static function changeComparisonFilterColor(event:UComparisonColorEvent):void {
			switch(event.colorToChange) {
			   case UComparisonColorEvent.BLACK_CLUSTER: _blackNodeGlowFilter.color = event.color;break;
		       case UComparisonColorEvent.YELLOW_CLUSTER: _yellowNodeGlowFilter.color = event.color;break;
		       case UComparisonColorEvent.WHITE_CLUSTER: _whiteNodeGlowFilter.color = event.color;break;
		    }
		}
		
		/** 
		 * Delivers the name of the UNode object. 
		 * 
		 * @return the name.
		 * */
		public function get name():String {
			return _nodeProperties[UStringConstants.NAME];
		}
		
		/**
		 * Delivers the shape (shape string) of this UNode object.
		 * 
		 * @return the shape in use.
		 * */
		public function get shape():String {
			return _nodeProperties[UStringConstants.SHAPE];
		}
		
		/**
		 * Sets the shape (shape string) for this UNode object.
		 * 
		 * @param shape the shape to use.
		 * */
		public function set shape(shape:String):void {
			if(Shapes.getShape(shape) == null)
			   return;
			else _nodeProperties[UStringConstants.SHAPE] = shape;
		}
		
		/**
		 * Delivers the renderer of this UNode object.
		 * 
		 * @return the renderer in use.
		 * */
		public function get nodeRenderer():UNodeRenderer {
			return _nodeProperties[UStringConstants.NODE_RENDERER];
		}
		
		/**
		 * Sets the renderer for this UNode object.
		 * 
		 * @param nodeRenderer the renderer to use.
		 * */
		public function set nodeRenderer(nodeRenderer:UNodeRenderer):void {
			//if(Shapes.getShape(shape) == null)
			  // return;
			_nodeProperties[UStringConstants.NODE_RENDERER] = nodeRenderer;
		}
		
		/**
		 * Delivers the fill color of this UNode object.
		 * 
		 * @return the fill color in use.
		 * */
		public function get fillColor():uint {
			return _nodeProperties[UStringConstants.FILL_COLOR];
		}
		
		/**
		 * Sets the fill color for this UNode object.
		 * 
		 * @param fillColor the fill color to use.
		 * */
		public function set fillColor(fillColor:uint):void {
			_nodeProperties[UStringConstants.FILL_COLOR] = fillColor;
		}
		
		/**
		 * Delivers the fill color of this UNode object.
		 * 
		 * @return the fill color in use.
		 * */
		public function get oldColor():uint {
			return _oldColor;
		}
		
		/**
		 * Sets the fill color for this UNode object.
		 * 
		 * @param fillColor the fill color to use.
		 * */
		public function set oldColor(oldColor:uint):void {
			_oldColor = oldColor;
		}
		
		/**
		 * Delivers the fill color of this UNode object.
		 * 
		 * @return the fill color in use.
		 * */
		public function get bothColor():uint {
			return _bothColor;
		}
		
		/**
		 * Sets the fill color for this UNode object.
		 * 
		 * @param fillColor the fill color to use.
		 * */
		public function set bothColor(bothColor:uint):void {
			_bothColor = bothColor;
		}
		
		/**
		 * Delivers the fill color of this UNode object.
		 * 
		 * @return the fill color in use.
		 * */
		public function get newColor():uint {
			return _newColor;
		}
		
		/**
		 * Sets the fill color for this UNode object.
		 * 
		 * @param fillColor the fill color to use.
		 * */
		public function set newColor(newColor:uint):void {
			_newColor = newColor;
		}
		
		/**
		 * Delivers the fill color of this UNode object.
		 * 
		 * @return the fill color in use.
		 * */
		public function get trueFactColor():uint {
			return _trueFactColor;
		}
		
		/**
		 * Sets the fill color for this UNode object.
		 * 
		 * @param fillColor the fill color to use.
		 * */
		public function set trueFactColor(trueFactColor:uint):void {
			_trueFactColor = trueFactColor;
		}
		
		/**
		 * Delivers the fill color of this UNode object.
		 * 
		 * @return the fill color in use.
		 * */
		public function get falseFactColor():uint {
			return _falseFactColor;
		}
		
		/**
		 * Sets the fill color for this UNode object.
		 * 
		 * @param fillColor the fill color to use.
		 * */
		public function set falseFactColor(falseFactColor:uint):void {
			_falseFactColor = falseFactColor;
		}
		
		/**
		 * Delivers the fill color of this UNode object.
		 * 
		 * @return the fill color in use.
		 * */
		public function get unknownFactColor():uint {
			return _unknownFactColor;
		}
		
		/**
		 * Sets the fill color for this UNode object.
		 * 
		 * @param fillColor the fill color to use.
		 * */
		public function set unknownFactColor(unknownFactColor:uint):void {
			_unknownFactColor = unknownFactColor;
		}
		
		/**
		 * Delivers the line color of this UNode object.
		 * 
		 * @return the line color in use.
		 * */
		public function get lineColor():uint {
			return _nodeProperties[UStringConstants.LINE_COLOR];
		}
		
		/**
		 * Sets the line color for this UNode object.
		 * 
		 * @param lineColor the line color to use.
		 * */
		public function set lineColor(lineColor:uint):void {
			_nodeProperties[UStringConstants.LINE_COLOR] = lineColor;
		}
		
		/**
		 * Delivers the line width of this UNode object.
		 * 
		 * @return the line width of the node.
		 * */
		public function get lineWidth():uint {
			return _nodeProperties[UStringConstants.LINE_WIDTH];
		}
		
		/**
		 * Sets the line width for this UNode object.
		 * 
		 * @param lineWidth the line width for the node.
		 * */
		public function set lineWidth(lineColor:uint):void {
			_nodeProperties[UStringConstants.LINE_WIDTH] = lineWidth;
		}
		
		/**
		 * Delivers the size of this UNode object.
		 * 
		 * @return the size of the node.
		 * */
		public function get size():Number {
			return _nodeProperties[UStringConstants.SIZE];
		}
		
		/**
		 * Sets the size for this UNode object.
		 * 
		 * @param size the size for this node.
		 * */
		public function set size(size:Number):void {
			_nodeProperties[UStringConstants.SIZE] = size;
		}
		
		/**
		 * Delivers the alpha value of this UNode object.
		 * 
		 * @return the alpha value of the node.
		 * */
		public function get alpha():Number {
			return _nodeProperties["fillAlpha"];
		}
		
		/**
		 * Sets the alpha value for this UNode object.
		 * 
		 * @param alpha the alpha value for this node.
		 * */
		public function set alpha(alpha:Number):void {
			_nodeProperties["fillAlpha"] = alpha;
		}
		
		/**
		 * Delivers the visibility flag of this UNode object.
		 * 
		 * @return the visibility flag of the node.
		 * */
		public function get visible():Boolean {
			return _nodeProperties[UStringConstants.VISIBLE];
		}
		
		/**
		 * Sets the visibility flag for this UNode object.
		 * 
		 * @param alpha the visibility flag for this node.
		 * */
		public function set visible(visible:Boolean):void {
			_nodeProperties[UStringConstants.VISIBLE] = visible;
		}
		
		/**
		 * Delivers the button mode of this UNode object.
		 * 
		 * @return the button mode of the node.
		 * */
		public function get buttonMode():Boolean {
			return _nodeProperties[UStringConstants.BUTTON_MODE];
		}
		
		/**
		 * Sets the button mode for this UNode object.
		 * 
		 * @param alpha the button mode for this node.
		 * */
		public function set buttonMode(buttonMode:Boolean):void {
			_nodeProperties[UStringConstants.BUTTON_MODE] = buttonMode;
		}
		
		/**
		 * Delivers the button mode of this UNode object.
		 * 
		 * @return the button mode of the node.
		 * */
		public function get mouseEnabled():Boolean {
			return _nodeProperties[UStringConstants.MOUSE_ENABLED];
		}
		
		/**
		 * Sets the button mode for this UNode object.
		 * 
		 * @param alpha the button mode for this node.
		 * */
		public function set mouseEnabled(mouseEnabled:Boolean):void {
			_nodeProperties[UStringConstants.MOUSE_ENABLED] = mouseEnabled;
		}
		
		/**
		 * Delivers the used filters of this UNode object.
		 * 
		 * @return the used filters of the node.
		 * */
		public function get filters():Array {
			return _nodeProperties[UStringConstants.FILTERS];
		}
		
		/**
		 * Sets the filters for this UNode object.
		 * 
		 * @param alpha the filters for this node.
		 * */
		public function set filters(filters:Array):void {
			_nodeProperties[UStringConstants.FILTERS] = filters;
		}
		
		/**
		 * Delivers the list of available UNode objects.
		 * 
		 * @return the list of available UNode objects.
		 * */
		public function get nodesArray():Array {
			return _nodesArray;
		}
		
		/**
		 * Delivers the map of available UNode objects.
		 * 
		 * @return the map of available UNode objects.
		 * */
		public function get nodesMap():Object {
			return _nodesMap;
		}
		
		/**
		 * Delivers the names of the available UNode objects.
		 * 
		 * @return the names of the available UNode objects.
		 * */
		public function get nodeNames():ArrayCollection {
			return _nodeNames;
		}
		
		public function get nodeProperties():Object {
			//trace("properties are : ");
			//for (var prop:String in _nodeProperties)
			  // trace(prop);//_nodeProperties.toString())
			return _nodeProperties;
		}
		
		/**
		 * Delivers the UNode object belonging to the given name.
		 * 
		 * @return the UNode object belonging to the given name
		 *         , or null (undefined), if no such node exists.
		 * */
		public static function node(node:String):UNode {
			
			if(!_nodesMap.hasOwnProperty(node))
			   return null;
			else 
			   return _nodesMap[node];
		}
		
		/**
		 * Applies the internally managed values to all the given NodeSprite instances.
		 * 
		 * @param nodeType the UNode object whose values should be applied.
		 * @param nodes the list of NodeSprite instances.
		 * */
		public static function applyValuesToNode(node:NodeSprite, nodeType:UNode, 
		                           compareModeEnabled:Boolean = false, t:Transitioner = null):void {
			
			if(t==null) {
				
				//for each (var node:NodeSprite in nodeType.nodeProperties) {
					   
				//node.fillColor = nodeType.fillColor;
				node.lineColor = UColors.BLACK
				
				// compute the color
				if(node.data.hasOwnProperty(UStringConstants.TRUTH_VALUE)) {
					switch(node.data.truthValue) {
						case 0: { 
						   node.fillColor = nodeType.falseFactColor; break;//node.lineColor = nodeType.falseFactColor; break;//UColors.red(5);break;
						}
						case 1: {
						   node.fillColor = nodeType.trueFactColor; break;//node.lineColor = nodeType.trueFactColor; break;//nodeType.fillColor;break;
						}
						case 2: {
						   node.fillColor = nodeType.unknownFactColor; break;//node.lineColor = nodeType.unknownFactColor; break;//UColors.grey(5);break;
						}
						default: {
						   node.fillColor = nodeType.falseFactColor; break;//node.lineColor = nodeType.falseFactColor; break;//UColors.red(5);break;
						}
					}
				}
				else {
				   if(node.data.hasOwnProperty("DB"))
				      node.fillColor = UColors.grey(1);
				   else
				      node.fillColor = nodeType.fillColor;
				}
				
				// set the node shape
				node.shape = nodeType.shape;		   
				
				//node.lineWidth = nodeType.lineWidth;
				node.lineWidth = 2;
				//node.lineColor = UColors.red(2)
				node.size = nodeType.size;
				
				node.fillAlpha = nodeType.alpha;
				
				// compute the alpha
				if(UFlareVis.lineageMode) {
				
					if(node.data.hasOwnProperty("linTree")) {
					   node.visible = true;//nodeType.visible;
					   node.alpha = UMath.roundNumber(parseFloat(node.data.confidence),2);//node.data.confidence;
					}
					node.visible = false;
				
				}			      	
				
				node.buttonMode = true;//nodeType.buttonMode;	
				node.mouseEnabled = true;//nodeType.mouseEnabled;	
				
				// a bit redundant, but it works
				/* if(!node.data.hasOwnProperty(UStringConstants.OLD)) {
				   //node.lineColor = nodeType.lineColor;
				   node.lineWidth = 2;
				   //node.fillColor = nodeType.fillColor;
				   node.lineColor = UColors.BLACK
				   return;
				}
				else {
				
					// the node existed before the changed query result came in
					if(node.data.hasOwnProperty(UStringConstants.OLD) && node.data.Old == true) {
						// the node exists in the new query result as well
						if(node.data.hasOwnProperty(UStringConstants.NEW) && node.data.New == true) {
							node.lineColor = nodeType.lineColor;
							node.lineWidth = nodeType.lineWidth;
						}
						// the node does not exist in the new query result
					    else {
							node.lineColor = UColors.BLACK;
							node.lineWidth = nodeType.lineWidth;
					    }
					}
					else {
					   node.lineColor = UColors.WHITE;
					   node.lineWidth = nodeType.lineWidth;
					}
				   
				} */
				
				// ---------------------------------------------------------------
				// ---------------------------------------------------------------
				// we are in the compare mode
				// ---------------------------------------------------------------
				// ---------------------------------------------------------------
				
				if(compareModeEnabled) {
				
				    // we do not set a new filter in lineage mode
				    //if(UFlareVis.lineageMode)
				      // return;
				
					// a bit redundant, but it works
					if(!node.data.hasOwnProperty(UStringConstants.OLD)) {
					    node.filters = null;
			            return;
					}
					
					// the node existed before the changed query result came in
					if(node.data.hasOwnProperty(UStringConstants.OLD) && node.data.Old == true) {
						// the node exists in the new query result as well
						if(node.data.hasOwnProperty(UStringConstants.NEW) && node.data.New == true) {
							node.filters = _compareFiltersYellowGlow;	
							return;	
						}
						// the node does not exist in the new query result
					    else {
							node.filters = _compareFiltersBlackGlow;	    	
					    	return;
					    }
					}
					else {
					   node.filters = _compareFiltersWhiteGlow;		   
					   return;
				    }
			    
			        return;
			    }
				    
			}
			
			else {
				
				t.$(node).lineColor = UColors.BLACK
				
				// compute the color
				if(node.data.hasOwnProperty(UStringConstants.TRUTH_VALUE)) {
					switch(node.data.truthValue) {
						case 0: { 
						   t.$(node).fillColor = nodeType.falseFactColor; break;//t.$(node).lineColor = nodeType.falseFactColor; break;//UColors.red(5);break;
						}
						case 1: {
						   t.$(node).fillColor = nodeType.trueFactColor; break;//t.$(node).lineColor = nodeType.trueFactColor; break;//nodeType.fillColor;break;
						}
						case 2: {
						   t.$(node).fillColor = nodeType.unknownFactColor; break;//t.$(node).lineColor = nodeType.unknownFactColor; break;//UColors.grey(5);break;
						}
						default: {
						   t.$(node).fillColor = nodeType.falseFactColor; break;//t.$(node).lineColor = nodeType.falseFactColor; break;//UColors.red(5);break;
						}
					}
				}
				
				else {
				   if(node.data.hasOwnProperty("DB"))
				      t.$(node).fillColor = UColors.grey(1);
				   else
				      t.$(node).fillColor = nodeType.fillColor;
				}
				
				t.$(node).shape = nodeType.shape;
				
				t.$(node).lineWidth = 2;
				//node.lineColor = UColors.red(2)
				t.$(node).size = nodeType.size;
				
				t.$(node).fillAlpha = nodeType.alpha;
				
				// compute the alpha
				if(UFlareVis.lineageMode) {
				
					if(node.data.hasOwnProperty("linTree")) {
					   t.$(node).visible = true;//nodeType.visible;
					   t.$(node).alpha = UMath.roundNumber(parseFloat(node.data.confidence),2);//node.data.confidence;
					}
				    else 
				       t.$(node).visible = false;
				
				}
		
				t.$(node).buttonMode = true;//nodeType.buttonMode;	
				t.$(node).mouseEnabled = true;//nodeType.mouseEnabled;			
				
				// a bit redundant, but it works
				/* if(!node.data.hasOwnProperty(UStringConstants.OLD)) {
				   //node.lineColor = nodeType.lineColor;
				   t.$(node).lineWidth = 2;
				   //node.fillColor = nodeType.fillColor;
				   t.$(node).lineColor = UColors.BLACK
				   return;
				}
				else {
				
					// the node existed before the changed query result came in
					if(node.data.hasOwnProperty(UStringConstants.OLD) && node.data.Old == true) {
						// the node exists in the new query result as well
						if(node.data.hasOwnProperty(UStringConstants.NEW) && node.data.New == true) {
							t.$(node).lineColor = nodeType.lineColor;
							t.$(node).lineWidth = nodeType.lineWidth;
						}
						// the node does not exist in the new query result
					    else {
							t.$(node).lineColor = UColors.BLACK;
							t.$(node).lineWidth = nodeType.lineWidth;
					    }
					}
					else {
					   t.$(node).lineColor = UColors.WHITE;
					   t.$(node).lineWidth = nodeType.lineWidth;
					}
				   
				} */
				
				// ---------------------------------------------------------------
				// ---------------------------------------------------------------
				// we are in the comopare mode
				// ---------------------------------------------------------------
				// ---------------------------------------------------------------
				
				if(compareModeEnabled) {
				
				    // we do not set a new filter in lineage mode
				    //if(UFlareVis.lineageMode)
				      // return;
				
					// a bit redundant, but it works
					if(!node.data.hasOwnProperty(UStringConstants.OLD)) {
					    // we set the filter to a single shadow filter before
					    //, so no need to do anything, because we have only
					    // one filter in our array now
					    //t.$(node).filters = null;
					    node.filters = null;
			            return;				    
					}
					
					// the node existed before the changed query result came in
					if(node.data.hasOwnProperty(UStringConstants.OLD) && node.data.Old == true) {
						// the node exists in the new query result as well
						if(node.data.hasOwnProperty(UStringConstants.NEW) && node.data.New == true) {
							//t.$(node).filters = _compareFiltersYellowGlow;
							node.filters = _compareFiltersYellowGlow;	
							return;
							
						}
						// the node does not exist in the new query result
					    else {
							//t.$(node).filters = _compareFiltersBlackGlow;
							node.filters = _compareFiltersBlackGlow;					    	
					    	return;
					    }
					}
					else {
					   //t.$(node).filters = _compareFiltersWhiteGlow;
					   node.filters = _compareFiltersWhiteGlow;				   
					   return;
				    }
			    
			        return;
			    }
				
			}
			    
		}
		
		/**
		 * Applies the internally managed values to all the given NodeSprite instances.
		 * 
		 * @param nodeType the UNode object whose values should be applied.
		 * @param nodes the list of NodeSprite instances.
		 * */
		public static function applyValuesToNodeAlternative(node:NodeSprite, nodeType:UNode, compareModeEnabled:Boolean = false):void {			
			
				node.lineColor = UColors.BLACK
				
				// compute the color
				if(node.data.hasOwnProperty(UStringConstants.TRUTH_VALUE)) {
					switch(node.data.truthValue) {
						case 0: { 
						   node.fillColor = nodeType.falseFactColor; break;//node.lineColor = nodeType.falseFactColor; break;//UColors.red(5);break;
						}
						case 1: {
						   node.fillColor = nodeType.trueFactColor; break;//node.lineColor = nodeType.trueFactColor; break;//nodeType.fillColor;break;
						}
						case 2: {
						   node.fillColor = nodeType.unknownFactColor; break;//node.lineColor = nodeType.unknownFactColor; break;//UColors.grey(5);break;
						}
						default: {
						   node.fillColor = nodeType.falseFactColor; break;//node.lineColor = nodeType.falseFactColor; break;//UColors.red(5);break;
						}
					}
				}
				else
				   node.fillColor = nodeType.fillColor;
				
				node.shape = nodeType.shape;
	
				node.lineWidth = 2;
				//node.lineColor = UColors.red(2)
				node.size = nodeType.size;
				
				node.fillAlpha = nodeType.alpha;
				
				// compute the alpha
				if(UFlareVis.lineageMode) {
				
					if(node.data.hasOwnProperty("linTree")) {
					   node.visible = true;//nodeType.visible;
					   node.alpha = UMath.roundNumber(parseFloat(node.data.confidence),2);//node.data.confidence;
					}
				    else 
				       node.visible = false;
				
				}
		
				node.buttonMode = true;//nodeType.buttonMode;	
				node.mouseEnabled = true;//nodeType.mouseEnabled;				
				
				// a bit redundant, but it works
				/* if(!node.data.hasOwnProperty(UStringConstants.OLD)) {
				   //node.lineColor = nodeType.lineColor;
				   node.lineWidth = 2;
				   //node.fillColor = nodeType.fillColor;
				   node.lineColor = UColors.BLACK
				   return;
				}
				else {
				
					// the node existed before the changed query result came in
					if(node.data.hasOwnProperty(UStringConstants.OLD) && node.data.Old == true) {
						// the node exists in the new query result as well
						if(node.data.hasOwnProperty(UStringConstants.NEW) && node.data.New == true) {
							node.lineColor = nodeType.lineColor;
							node.lineWidth = nodeType.lineWidth;
						}
						// the node does not exist in the new query result
					    else {
							node.lineColor = UColors.BLACK;
							node.lineWidth = nodeType.lineWidth;
					    }
					}
					else {
					   node.lineColor = UColors.WHITE;
					   node.lineWidth = nodeType.lineWidth;
					}
				   
				} */
				
				// ---------------------------------------------------------------
				// ---------------------------------------------------------------
				// we are in the cmpare mode
				// ---------------------------------------------------------------
				// ---------------------------------------------------------------
				
				if(compareModeEnabled) {
				
				    // we do not set a new filter in lineage mode
				    //if(UFlareVis.lineageMode)
				      // return;
				
					// a bit redundant, but it works
					if(!node.data.hasOwnProperty(UStringConstants.OLD)) {
					    node.filters = null;
			            return;
					}
					
					// the node existed before the changed query result came in
					if(node.data.hasOwnProperty(UStringConstants.OLD) && node.data.Old == true) {
						// the node exists in the new query result as well
						if(node.data.hasOwnProperty(UStringConstants.NEW) && node.data.New == true) {
							node.filters = _compareFiltersYellowGlow;							
							return;							
						}
						// the node does not exist in the new query result
					    else {
							node.filters = _compareFiltersBlackGlow;				    	
					    	return;
					    }
					}
					else {
					   node.filters = _compareFiltersWhiteGlow;					   
					   return;
				    }
			    
			        return;
			    }
			    
		}
		
		/**
		 * Applies the internally managed values to all the given NodeSprite instances.
		 * 
		 * @param nodeType the UNode object whose values should be applied.
		 * @param nodes the list of NodeSprite instances.
		 * */
		public static function applyGlowToNode(node:NodeSprite, nodeType:UNode):void {
			
			// ---------------------------------------------------------------
			// ---------------------------------------------------------------
			// we are in the comparison mode
			// ---------------------------------------------------------------
			// ---------------------------------------------------------------
		    
		    // we do not set a new filter in lineage mode
		    //if(UFlareVis.lineageMode)
		      // return;
		
			// a bit redundant, but it works
			if(!node.data.hasOwnProperty(UStringConstants.OLD)) {
			    node.filters = null;
			    return;
			}
			
			// the node existed before the changed query result came in
			if(node.data.hasOwnProperty(UStringConstants.OLD) && node.data.Old == true) {
				// the node exists in the new query result as well
				if(node.data.hasOwnProperty(UStringConstants.NEW) && node.data.New == true) {
					node.filters = _compareFiltersYellowGlow;				
					return;
					
				}
				// the node does not exist in the new query result
			    else {
					node.filters = _compareFiltersBlackGlow;			    	
			    	return;
			    }
			}
			else {
			   node.filters = _compareFiltersWhiteGlow;
			   
			   return;
		    }
	    
	        return;
			
		}

	}
}