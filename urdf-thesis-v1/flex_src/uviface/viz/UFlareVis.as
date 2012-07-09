package uviface.viz
{
	import flare.animate.FunctionSequence;
	import flare.animate.Transitioner;
	import flare.display.DirtySprite;
	import flare.display.TextSprite;
	import flare.util.Displays;
	import flare.util.Shapes;
	import flare.vis.Visualization;
	import flare.vis.data.Data;
	import flare.vis.data.DataSprite;
	import flare.vis.data.EdgeSprite;
	import flare.vis.data.NodeSprite;
	import flare.vis.data.Tree;
	import flare.vis.operator.layout.*;
	
	import flash.display.BitmapData;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.filters.DropShadowFilter;
	import flash.geom.Rectangle;
	import flash.system.*;
	import flash.text.TextFormat;
	import flash.utils.Timer;
	
	import mx.containers.Canvas;
	
	import urdf.api.ULineageAnd;
	import urdf.api.UQueryResult;
	
	import uviface.event.*;
	import uviface.viz.control.UGraphControls;
	import uviface.viz.control.UPanZoomControl;
	import uviface.viz.data.*;
	import uviface.viz.display.*;
	import uviface.viz.layout.*;
	import uviface.viz.operator.*;
	import uviface.viz.util.*;

	public class UFlareVis extends Canvas
	{		
		private var _updateSequence:FunctionSequence = new FunctionSequence();
		
		private var _currentResultLineage:ULineageAnd;
		private var _currentCompleteLineage:Array;
		private var _currentLineageQueryString:String;
		
		// an hash object that stores temporary pulsating values for the force layout
		private var _tempPulsatingValues:Object = {massValue: 1, springLength: 200, pull: true};
		private var _pulsatingDurationTimer:Timer = new Timer(30000,1);
		private var _pulsatingTimer:Timer = new Timer(1000,0);
	    //private var _pulsatingTimerPush:Timer = new Timer(500,1);
	    //private var _pulsatingTimerPull:Timer = new Timer(1000,1);
		
		private var _currentFactID:String;
		private var _lineageRootNode:NodeSprite;	
		
		//private var _glowModeEnabled:Boolean = false;
		private var _compareModeEnabled:Boolean = false;
		
		private var _edgeFiltersShadow:Array;// = new DropShadowFilter();
		private var _edgeFiltersNoShadow:Array = null;// = new DropShadowFilter();
		private var _edgeFilters:Array;// = new DropShadowFilter();
		private var _edgeShadowFilter:DropShadowFilter;
		
		private var _nodeFiltersShadow:Array;// = new DropShadowFilter();
		private var _nodeFiltersNoShadow:Array = null;// = new DropShadowFilter();
		private var _nodeFilters:Array;// = new DropShadowFilter();
		private var _nodeShadowFilter:DropShadowFilter;
		
		private var _nodeShadowEnabled:Boolean = true;
		private var _edgeShadowEnabled:Boolean = true;
		
		public static const RADIAL_LAYOUT:int = 0;
		public static const FORCE_LAYOUT:int = 1;
		
		/** this node is needed to reset the root node that was set before entering lineage mode. */
		private var _rootNodeBeforeLineage:NodeSprite = null;
		/** these are the current focus nodes for the graph distance filter. */
		private var _focusNodes:Array = null;
		
		private var _visHelper:UVisualizationHelper;
		
		private var _radialLayout:URadialTreeLayout;
		private var _forceDirectedLayout:UForceDirectedLayout;//ForceDirectedLayout;
		private var _lineageTreeLayout:UNodeLinkTreeLayout;
		/** The hop size for the heighborhood of the root node. */
		private var _numOfHops:int = 0;//1;
		/** The radius increase for the depth levels of the radial layout. */
		private var _depthRadiusIncrement:Number = 200;
		
		/** The pan/zoom control for the visualization itself. */
		private var _panZoomCtrlVis:UPanZoomControl = new UPanZoomControl();
		
		/** 
		 * The graph distance filter for this visualization.  
		 * */
		private var _graphDistanceFilter:UGraphDistanceFilter;
		
		// set the force layout as the default layout
		private var _currentLayout:int = RADIAL_LAYOUT; // which means radial;
		
		/** The bounds for the visualization. */
		private var _bounds:Rectangle = new Rectangle();
		
		/** The internally used Flare visualization. */
		private var _vis:Visualization;
		/** The internally used margin */
		private var _margin:int = 2;//10;
		/** The list of different graph layouts. */
		
		/** The internally produced and managed graph data. */
		private var _graphData:UGraphData;
		
		private static var _temporaryTextSprite:TextSprite;
		private static var _temporaryTextFormat:TextFormat;
		
		private static var _inLineageMode:Boolean = false;
		
		/** We embed the font. */
		//[Embed(source="/assets/font/verdana.TTF", fontName="Verdana", mimeType='application/x-font', advancedAntiAliasing='true')]
		[Embed(source="/assets/font/verdana.TTF", fontName="Verdana", mimeType='application/x-font', advancedAntiAliasing='true')]
		private static var _font:Class;
		
		/** 
		 * The constructor for the UFlareVis object.
		 * 
		 * @param queryResult the query result that represents the 
		 *                    source for this visualization object.
		 * */
		public function UFlareVis(queryResult:UQueryResult = null)
		{
			super();
			
			_pulsatingDurationTimer.addEventListener(TimerEvent.TIMER,stopPulsation,false,0,true);
		    _pulsatingTimer.addEventListener(TimerEvent.TIMER,pulsate,false,0,true);
		    //_pulsatingTimerPush.addEventListener(TimerEvent.TIMER,pulsatePush,false,0,true);
		    //_pulsatingTimerPull.addEventListener(TimerEvent.TIMER,pulsatePull,false,0,true);
			
			// set the filter values for the edge shadow filter
			_edgeShadowFilter = new DropShadowFilter();
		    _edgeShadowFilter.distance = 10;
		    _edgeShadowFilter.color = UColors.BLACK;
		    _edgeShadowFilter.alpha = 0.7;
		    _edgeFiltersShadow = [_edgeShadowFilter];
		    _edgeFilters = _edgeFiltersShadow;
		    
		    // set the filter values for the node shadow filter
		    _nodeShadowFilter = new DropShadowFilter();
		    _nodeShadowFilter.distance = 10;
		    _nodeShadowFilter.color = UColors.BLACK;
		    _nodeShadowFilter.alpha = 0.7;
		    _nodeFiltersShadow = [_nodeShadowFilter];
		    _nodeFilters = _nodeFiltersShadow;
		    			
			// the structure that holds the labeling functionality
			// and the controls for the visualization
			_visHelper = new UVisualizationHelper();
			
			// the parameters for the used force layout
			//_forceDirectedLayout = new ForceDirectedLayout(false,1);
			_forceDirectedLayout = new UForceDirectedLayout(false,1);
			_forceDirectedLayout.defaultParticleMass = 1;     // default:  1
			_forceDirectedLayout.defaultSpringTension = 0.05;//0.001;  // default:  0.1
			_forceDirectedLayout.defaultSpringLength = 200;   // default: 30
			_forceDirectedLayout.simulation.nbodyForce.gravitation = -10;
			
			// adjust the spring length for the edges separately
			_forceDirectedLayout.restLength = function(e:EdgeSprite):Number {
			     
			     if(e.data.hasOwnProperty(UStringConstants.SUPER_EDGE))
			        return _forceDirectedLayout.defaultSpringLength;
			     
			     var numOfEdges:Number = _vis.data.edges.length;
			     var numOfNodes:Number = _vis.data.nodes.length;
			     var value:Number = 0;
			     var avgDegree:Number = Math.ceil(numOfEdges / numOfNodes);
			     var sourceDegree:Number = e.source.degree;//Math.ceil((e.source.degree / numOfEdges) * 10);
			     var targetDegree:Number = e.target.degree;//Math.ceil((e.target.degree / numOfEdges) * 10);	
			     
			      if(sourceDegree > avgDegree) {
			      	  if(targetDegree > avgDegree) {
			      	    value = 2.25;
			      	    //e.lineWidth =  2* UEdge.DEFAULT_EDGE.lineWidth;
			      	  }
			      	  else {
			      	    value = 0.75;
			      	    //e.lineWidth = UEdge.DEFAULT_EDGE.lineWidth;
			      	  }
			      }
			      else {
			      	 value = 0.75;
			      	 //e.lineWidth = UEdge.DEFAULT_EDGE.lineWidth;
			      }
			      
			     return _forceDirectedLayout.defaultSpringLength * value;// * sourceDegree * targetDegree;
			}
			
			/*
			_forceDirectedLayout.mass = function(d:DataSprite):Number {
			     
			     if(d is NodeSprite) {
				     
				     var numOfEdges:Number = _vis.data.edges.length;
				     var value:Number = Math.ceil(((d as NodeSprite).degree / numOfEdges) * 10);
				     
				     return _forceDirectedLayout.defaultParticleMass * value;
			     }
			     else
			        return _forceDirectedLayout.defaultParticleMass;
			}
			*/
			
			
			_radialLayout = new URadialTreeLayout();
			_radialLayout.autoScale = false;
			_radialLayout.radiusIncrement = _depthRadiusIncrement;
			
			_lineageTreeLayout = new UNodeLinkTreeLayout("topToBottom",50,20,30);
			
			// define a first root node
			_rootNodeBeforeLineage = new NodeSprite();
			_rootNodeBeforeLineage.shape = Shapes.CIRCLE;
			_rootNodeBeforeLineage.renderer = UNodeRenderer.instance;//ShapeRenderer.instance;
			
			var rectBounds:Array = [1,1,10,10];
			_rootNodeBeforeLineage.data.rectBounds = rectBounds;
			
			//_graphDistanceFilter = new UGraphDistanceFilter([new NodeSprite()], _numOfHops);
			_graphDistanceFilter = new UGraphDistanceFilter([_rootNodeBeforeLineage], _numOfHops);
			
			_temporaryTextFormat = new TextFormat();
			_temporaryTextFormat.font = "Verdana";
			_temporaryTextFormat.color = UColors.BLACK;
			//_labelFormatterNodes.color = UColors.WHITE;
			_temporaryTextFormat.size = 12;
			_temporaryTextFormat.bold = true; 
            
			// the bounds for the visualization
			_bounds.x = this.x + 5;// + 5;
			_bounds.y = this.y + 5;// + 5;
			_bounds.width = this.width - 30;
			_bounds.height = this.height - 30;
			
			_graphData = new UGraphData(); 
			this.rawChildren.addChild(_vis = new Visualization(new Data()));
			_vis.x = _margin;
			updateVisBounds();
			_vis.operators.setup();
			
			// set the hit area as the flare vis instance 
			_panZoomCtrlVis.hitArea = this;
			_vis.controls.add(_panZoomCtrlVis);
			
			// repeatedly update the visualization
			_vis.continuousUpdates = false;
			// we want directed edges
			_vis.data.directedEdges = true;
			
			_visHelper.applyDefaultControls(this);
			
			// probably better than the timer hack
            //addEventListener(VisualizationEvent.UPDATE, 
              //       function ():void{ DirtySprite.renderDirty(); });

			  
		}
		
		/** 
		 * Sets the query result that is the source for this visualization object.
		 * 
		 * @param queryResult 
		 *           the source data for this visualization.
		 * */
		public function updateVisualization(queryResult:UQueryResult, queryString:String = null):void {
			
			if(queryResult == null || queryResult.getGlobalDependencyGraph() == null)
			   return;
	
			else
			{
			     _focusNodes = _graphData.produceGraphData(_vis.data, queryResult, queryString); 
			     
			    // apply all the controls of the _visHelper layout
			    //_visHelper.applyDefaultControls(this);
			    _vis.update();
			    
			    // update the duplicate reduced edge list for the force layout
			    //trace("Number of Edges is : " + _vis.data.edges.length.toString());
			    //this._forceDirectedLayout.forceEdgeList = _graphData.forceEdgeList;
		
			    // compute the highest node degree
			    computeHighestNodeDegree(); 
			    
			    // set the focus nodes and the root node
				if (_focusNodes == null)
				   return;
				_rootNodeBeforeLineage = requestHighestDegreeNode(_focusNodes);
				if (_rootNodeBeforeLineage == null)
				   return;
			     
			    // set the current query result id for the graph distance filter
	            _graphDistanceFilter.resultClusterID = _graphData.currentResultString; 
	            
	            // adjust the edge parameters
	            adjustEdgeParameters();
	            
	            // new on 15.11.10
	            //for each(var node:NodeSprite in _vis.data.nodes) {
	            	//UDisplays.sortChildren(node,USort.sortOn("tooltip"));
	            //}
			    
			    // visualize the data
			     visualize();
			 }
			
		}
      
        public function get numOfHops():int {
        	return _numOfHops;
        }
        
        public function set numOfHops(numOfHops:int):void {
        	_numOfHops = numOfHops;
        }
      
        /**
		 * Indicates, if we are in the lineage mode.
		 * 
		 * @return true, if we are in lineage mode, false otherwise.
		 * */
		public static function get lineageMode():Boolean {
			return _inLineageMode;
		}
		
		public function get focusNodes():Array {
			return _focusNodes;
		}
		
		public function get rootNodeBeforeLineage():NodeSprite {
			return _rootNodeBeforeLineage;
		}
		
		public function get graphDistanceFilter():UGraphDistanceFilter {
			return _graphDistanceFilter;
		}
		
		public function get radialLayout():URadialTreeLayout {
			return _radialLayout;
		}
		/*
		public function get forceDirectedLayout():ForceDirectedLayout {
			return _forceDirectedLayout;
		}
		*/
		
		public function get forceDirectedLayout():UForceDirectedLayout {
			return _forceDirectedLayout;
		}
		
		public function set layout(layout:int):void {
			
			if(_currentLayout == layout)
			   return;
			
			if(_graphDistanceFilter == null)
			   return;
			
			if(_vis.operators.length > 0) {   
			   _vis.operators.remove(_graphDistanceFilter)
			   _vis.operators.add(_graphDistanceFilter); //distance filter has to be added before the layout
			}
			
			// we know that the current layout is not our specified one
			if(_currentLayout == UFlareVis.FORCE_LAYOUT) {
			   _currentLayout = layout;
			   _vis.operators.remove(_forceDirectedLayout)
			   if(_vis.operators.length > 0)
			     _vis.operators.add(_radialLayout);
			   _vis.continuousUpdates = false;
			}
			else {
			   _currentLayout = layout;
			   _vis.operators.remove(_radialLayout)
			   if(_vis.operators.length > 0)
			     _vis.operators.add(_forceDirectedLayout); 
			   _vis.continuousUpdates = true;
			}
		}
		
		public function updateLineageLayout(event:ULineageLayoutEvent):void {
			
			//trace("Lineage Event");
        	
    		_lineageTreeLayout.orientation = event.orientation;
    	    _lineageTreeLayout.depthSpacing = event.depthSpacing;
			_lineageTreeLayout.breadthSpacing = event.breadthSpacing;
			_lineageTreeLayout.subtreeSpacing = event.subtreeSpacing;
			
		    if(_vis != null)
    	      _vis.update(2).play();
			
    	    return;
		}
		
		public function updateDataSpriteShadows(event:UDataSpriteEvent):void {
			//trace("Shadow Event");
        	
        	// set the node values
			_nodeShadowEnabled = event.nodeShadowEnabled;
			if(_nodeShadowEnabled)
			   _nodeFilters = _nodeFiltersShadow;
			else
			   _nodeFilters = _nodeFiltersNoShadow;
			_nodeShadowFilter.distance = event.nodeShadowLength;
			_nodeShadowFilter.alpha = event.nodeShadowAlpha;
			_nodeShadowFilter.color = event.nodeShadowColor;
			// set the edge values
			_edgeShadowEnabled = event.edgeShadowEnabled;
			if(_edgeShadowEnabled)
			   _edgeFilters = _edgeFiltersShadow;
			else
			   _edgeFilters = _edgeFiltersNoShadow;
			_edgeShadowFilter.distance = event.edgeShadowLength;
			_edgeShadowFilter.alpha = event.edgeShadowAlpha;
			_edgeShadowFilter.color = event.edgeShadowColor;
			
			if(!_compareModeEnabled) {
			//if(!_glowModeEnabled) {
				// iterate over all nodes
				_vis.data.nodes.visit(
				    function(ns:NodeSprite):void {
				       ns.filters = _nodeFilters;
				    }
				);
				
				// iterate over all nodes
				_vis.data.edges.visit(
				    function(es:EdgeSprite):void {
				       es.filters = _edgeFilters;
				    }
				);
				
				DirtySprite.renderDirty();
			}
			
			
			
			// update the node and edge values
			//setDataSpriteProperties();		  
        	   	
        	// stop the propagation of the event
        	event.stopImmediatePropagation();
		}
		
		public function updateForceLayout(event:UForceLayoutEvent):void {
			
			//trace("Force Event");
        	
        	//if(_inLineageMode) {
        	  // return;
        	//}  
        	
        	// stop the propagation of the event
        	event.stopImmediatePropagation();
        	
        	// ---------------------------------------------------------------
        	// set the dorce layout related properties
        	// ---------------------------------------------------------------
        	if(_currentLayout == FORCE_LAYOUT) {
        	    
        	    _vis.continuousUpdates = true;
        		
	    		_forceDirectedLayout.defaultParticleMass = event.particleMass;
	    	    _forceDirectedLayout.defaultSpringLength = event.springLength;
	    	    _forceDirectedLayout.defaultSpringTension = event.springTension;
	    		_forceDirectedLayout.iterations = event.iterations;
	    		//_forceDirectedLayout.enforceBounds = event.enforceBounds;
	    		_forceDirectedLayout.simulation.nbodyForce.gravitation = event.nBodyGravitation;
	    		
        	}	
				
		}
		
		public function updateRadiusOrDistance(event:URadiusDistanceEvent):void {
			
			//trace("Radius Event");
        	
        	//if(_inLineageMode) {
        	  // return;
        	//}  
        	
        	// stop the propagation of the event
        	event.stopImmediatePropagation();
        	
        	//trace("event type : " + event.typeOfAction);
        	
        	switch (event.typeOfAction) {
        		
        		/*
        		case URadiusDistanceEvent.CHANGE_DISTANCE_FILTER: {
	        		_alphaDistanceFilterActivated = (event.distanceFilterType == URadiusDistanceEvent.ALPHA) ? true : false;
	        		_distanceAlphaValue = event.alphaValue;
	        		//if(!_alphaDistanceFilterActivated) {
	        		  //if(UFlareVis.alphaDistanceFilterActivated) { 
		        	     _vis.data.nodes.visit(
						      function(ns:NodeSprite):void {
						         ns.fillColor = UNode.DEFAULT_NODE.fillColor;
						         ns.fillAlpha = UNode.DEFAULT_NODE.alpha;
						         ns.dirty();
						      }
						  );
						  _vis.data.edges.visit(
							  function(es:EdgeSprite):void {
						         //es.lineColor = UNode.DEFAULT_NODE.lineColor;
						         UEdge.changeEdgeColor(es,UEdge.DEFAULT_EDGE);
						         es.dirty();
							  }
						  );
	        	      // }
	        		//}
	        		flare.display.DirtySprite.renderDirty();
	        		trace("alpha activated : " + _alphaDistanceFilterActivated);
	        	    break;
	        	}
	        	case URadiusDistanceEvent.CHANGE_ALPHA_VALUE: {
	        		//if(_graphDistanceFilter != null) 
	        	    //_graphDistanceFilter.alphaValue = event.alphaValue;
	        	    _distanceAlphaValue = event.alphaValue;
	        	    /*
	        	    if(UFlareVis.alphaDistanceFilterActivated) { 
		        	     _vis.data.nodes.visit(
						      function(ns:NodeSprite):void {
						         if(!ns.data.hasOwnProperty("alpha") 
						            ns.fillAlpha = event.alphaValue;
						         else
						           if(ns.data.alpha == false)
						              ns.fillAlpha = event.alphaValue;
						      }
						  );
						  _vis.data.edges.visit(
							  function(es:EdgeSprite):void {
							     if(!ns.data.hasOwnProperty("alpha") 
						            es.lineAlpha = event.alphaValue;
						         else
						           if(ns.data.alpha == false)
						              es.lineAlpha = event.alphaValue;
							  }
						  );
	        	    }
	        	    */
	        	    /*
	        	    if(!UFlareVis.alphaDistanceFilterActivated) { 
		        	     _vis.data.nodes.visit(
						      function(ns:NodeSprite):void {
						          ns.fillAlpha = UNode.DEFAULT_NODE.alpha;
						      }
						  );
						  _vis.data.edges.visit(
							  function(es:EdgeSprite):void {
						           UEdge.changeEdgeAlpha(es);
							  }
						  );
	        	    }
	        	    
	        	    
	        	    //trace("alpha value : " + _graphDistanceFilter.alphaValue.toString());
	        	    trace("alpha value : " + _distanceAlphaValue.toString());
	        	    break;
	        	}
	        	*/
        		case URadiusDistanceEvent.INCREMENT_HOP_SIZE: {
	        		if(_graphDistanceFilter != null) {
	        	       _numOfHops = event.hopSize;
	        	       _graphDistanceFilter.distance = _numOfHops;
	        	       
	        	       if(_numOfHops < 0) {
	        	       	  _vis.data.nodes.visit(
						      function(ns:NodeSprite):void {
						         ns.visible = true;
						      }
						  );
						  _vis.data.edges.visit(
							  function(es:EdgeSprite):void {
							     es.visible = true;
							  }
						  );	
	        	       }
	        	       
	        	       _radialLayout.autoScale = event.enforceBounds;
	        	    }
	        	    break;
	        	}
	        	case URadiusDistanceEvent.INCREMENT_RADIUS: {
	        		if(_radialLayout != null) {
        		       _depthRadiusIncrement = event.radiusIncrement;
        		       _radialLayout.radiusIncrement = _depthRadiusIncrement; 
        		       _radialLayout.autoScale = event.enforceBounds;
        		    }
        		    break;
	        	}
	        	case URadiusDistanceEvent.ENFORCE_BOUNDS: {
	        		if(_radialLayout != null && _forceDirectedLayout != null) {
	        	      _radialLayout.autoScale = event.enforceBounds;
        	          _forceDirectedLayout.enforceBounds = event.enforceBounds;
        	        }
        	        break;
	        	}
	        	default: {
	        		if(_graphDistanceFilter != null) {
	        	       _numOfHops = event.hopSize;
	        	       _graphDistanceFilter.distance = _numOfHops;
	        	       
	        	       if(_numOfHops < 0) {
	        	       	  _vis.data.nodes.visit(
						      function(ns:NodeSprite):void {
						         ns.visible = true;
						      }
						  );
						  _vis.data.edges.visit(
							  function(es:EdgeSprite):void {
							     es.visible = true;
							  }
						  );	
	        	       }
	        	       
	        	       _radialLayout.autoScale = event.enforceBounds;
	        	    }
	        	    break;
	        	}
        		
        	}
        	
        	if(_vis != null)
        	      _vis.update(2).play();
				
		}
		
		public function updateLayout(event:ULayoutEvent):void {
			
			//trace("Layout Event");
        	
        	//if(_inLineageMode) {
        	  // return;
        	//}  
        	
        	// stop the propagation of the event
        	//event.stopImmediatePropagation();
        	
        	if(_forceDirectedLayout != null && _radialLayout != null) {
        		
        		if(_currentLayout != event.layout) {
	        		_currentLayout = event.layout;
	        		
	        		if(_inLineageMode)
        	           return;
	        		
	        		switch(_currentLayout) {
	        			case RADIAL_LAYOUT: {
	        			        _vis.operators.remove(_forceDirectedLayout);
	        			        _vis.continuousUpdates = false; 
	        			        _vis.operators.add(_radialLayout);
	        			        if(_vis.data.nodes.length == 0 || _vis.data.edges.length == 0)
        	                        return;
	        			        if(_vis != null)
        	                        _vis.update(2).play();
	        			        break;
	        			}
	        			case FORCE_LAYOUT: {
	        			         _vis.operators.remove(_radialLayout);
	        			         _vis.operators.add(_forceDirectedLayout);
	        			         _vis.continuousUpdates = true; 
	        			         if(_vis.data.nodes.length == 0 || _vis.data.edges.length == 0)
        	                        return;
	        			         break;
	        			}
	        			default: {_vis.operators.remove(
	        			         _forceDirectedLayout);
	        			         _vis.continuousUpdates = false; 
	        			         _vis.operators.add(_radialLayout);
	        			         if(_vis.data.nodes.length == 0 || _vis.data.edges.length == 0)
        	                        return;
	        			         if(_vis != null)
        	                        _vis.update(2).play();
	        			         break;
	        			}
	        		}
        		}
        		
        	}
				
		}
		
		public function updateGraph(event:UGraphConfigEvent):void {
			
			
    		_lineageTreeLayout.orientation = event.lineageLayout;
    	    _lineageTreeLayout.depthSpacing = event.depthSpacing;
			_lineageTreeLayout.breadthSpacing = event.breadthSpacing;
			_lineageTreeLayout.subtreeSpacing = event.subtreeSpacing;
        	
        	// set the node values
			_nodeShadowEnabled = event.nodeShadowEnabled;
			if(_nodeShadowEnabled)
			   _nodeFilters = _nodeFiltersShadow;
			else
			   _nodeFilters = _nodeFiltersNoShadow;
			_nodeShadowFilter.distance = event.nodeShadowLength;
			_nodeShadowFilter.alpha = event.nodeShadowAlpha;
			_nodeShadowFilter.color = event.nodeShadowColor;
			// set the edge values
			_edgeShadowEnabled = event.edgeShadowEnabled;
			if(_edgeShadowEnabled)
			   _edgeFilters = _edgeFiltersShadow;
			else
			   _edgeFilters = _edgeFiltersNoShadow;
			_edgeShadowFilter.distance = event.edgeShadowLength;
			_edgeShadowFilter.alpha = event.edgeShadowAlpha;
			_edgeShadowFilter.color = event.edgeShadowColor;
			
			// iterate over all nodes
			_vis.data.nodes.visit(
			    function(ns:NodeSprite):void {
			       ns.filters = _nodeFilters;
			    }
			);
			
			// iterate over all nodes
			_vis.data.edges.visit(
			    function(es:EdgeSprite):void {
			       es.filters = _edgeFilters;
			    }
			);	  
        	   
        	if(_graphDistanceFilter != null) {
        		// add the graph distance filter again
        	   //_vis.operators.add(_graphDistanceFilter);
        	   _numOfHops = event.hopSize;
        	   //_graphDistanceFilter.distance = _numOfHops;
        	   
        	   // please show all nodes and edges
        	   // we remove the graph distance filter anyway
        	   //if(_numOfHops == 0) 
        	     //  _vis.operators.remove(_graphDistanceFilter);
        	   //else
        	   _graphDistanceFilter.distance = _numOfHops;
    
        	}
        	if(_radialLayout != null) {
        		_depthRadiusIncrement = event.radiusIncrement;
        		_radialLayout.radiusIncrement = _depthRadiusIncrement; 
        		_radialLayout.autoScale = event.enforceBounds;
        	}
        	if(_forceDirectedLayout != null && _radialLayout != null) {
        		if(_currentLayout != event.layout) {
	        		_currentLayout = event.layout;
	        		switch(_currentLayout) {
	        			case RADIAL_LAYOUT: {_vis.operators.remove(_forceDirectedLayout);
	        			        _vis.continuousUpdates = false; 
	        			        _vis.operators.add(_radialLayout);
	        			        break;
	        			}
	        			case FORCE_LAYOUT: {_vis.operators.remove(_radialLayout);
	        			         _vis.operators.add(_forceDirectedLayout);
	        			         _vis.continuousUpdates = true; 
	        			         break;
	        			}
	        			default: {_vis.operators.remove(_forceDirectedLayout);
	        			         _vis.continuousUpdates = false; 
	        			         _vis.operators.add(_radialLayout);
	        			         break;
	        			}
	        		}
        		}
        		
        	}
        	
        	// stop the propagation of the event
        	event.stopImmediatePropagation();
        	
        	// ---------------------------------------------------------------
        	// set the force layout related properties
        	// ---------------------------------------------------------------
        		
    		_forceDirectedLayout.defaultParticleMass = event.particleMass;
    		_forceDirectedLayout.defaultSpringLength = event.springLength;
    		_forceDirectedLayout.defaultSpringTension = event.springTension;
    		_forceDirectedLayout.iterations = event.iterations;
    		_forceDirectedLayout.enforceBounds = event.enforceBounds;
    		_forceDirectedLayout.simulation.nbodyForce.gravitation = event.nBodyGravitation;
    		
    		// reset the node colors, alpha values and node label colors
			_visHelper.changeLabelFormatterNodes("Verdana", UColors.WHITE, 12, true);
			UNode.DEFAULT_NODE.fillColor = UColors.red(2);
			UNode.DEFAULT_NODE.alpha = 1.0;	
					
			_vis.data.nodes.visit(
			      function(ns:NodeSprite):void {
			         
			         if(!ns.data.hasOwnProperty("linTree")) {
			            ns.fillColor = UColors.red(2);
			            ns.fillAlpha = 1.0;
			            (ns.props.label as TextSprite).color = UColors.WHITE;
			         }
			      }
			);
			
			// reset the edge colors
			UEdge.DEFAULT_EDGE.trueEdgeColor = UColors.green(7);
		    UEdge.DEFAULT_EDGE.falseEdgeColor = UColors.red(7); 
		    
		    var edge:EdgeSprite;
				
			//for each (edge in _vis.data.edges) {
				//UEdge.applyValuesToEdge(edge, UEdge.DEFAULT_EDGE, _compareModeEnabled, _glowModeEnabled);
			//}
			for each (edge in _vis.data.edges) {
				UEdge.changeEdgeColor(edge,UEdge.DEFAULT_EDGE);
			}	
    
    		if(_vis != null)
    	      _vis.update(2).play();
      
		}
		
		/** 
		 * Delivers the graphData that is the source for this visualization object.
		 * 
		 * @param graphData 
		 *           the source graph data for this visualization.
		 * */
		public function get graphData():UGraphData {
			return _graphData;
		}
		
		public function get panZoomCtrlVis():UPanZoomControl {
			return _panZoomCtrlVis;
		}
		
		public function get rootNode():NodeSprite {
			return _rootNodeBeforeLineage;
		}
		
		public function get visHelper():UVisualizationHelper {
			return _visHelper;
		}
		
		/** Returns the backing Flare visualization instance. */
		public function get visualization():Visualization {
			return _vis;
		}
		
		public function get visWidth():Number { return _vis.bounds.width; }
		public function set visWidth(w:Number):void {
			_vis.bounds.width = w;
			_vis.update();
			invalidateSize();
		}
		
		public function get visHeight():Number { return _vis.bounds.height; }
		public function set visHeight(h:Number):void {
			_vis.bounds.height = h;
			_vis.update();
			invalidateSize();
		}
		
		/** @private */
		public override function getExplicitOrMeasuredWidth():Number {
			DirtySprite.renderDirty(); // make sure everything is _visHelper
			var w:Number = _vis.bounds.width;
			if (_vis.width > w) {
				// TODO: this is a temporary hack. fix later!
				_vis.x = _margin + Math.abs(_vis.getBounds(_vis).x);
				w = _vis.width;
			}
			return 2*_margin + Math.max(super.getExplicitOrMeasuredWidth(), w);
		}
		
		/** @private */
		public override function getExplicitOrMeasuredHeight():Number {
			DirtySprite.renderDirty(); // make sure everything is _visHelper
			return Math.max(super.getExplicitOrMeasuredHeight(),
							_vis.bounds.height,
							_vis.height);
		}
		
		private function updateVisBounds():void {
			if(_vis) {
				_vis.bounds = _bounds;
				invalidateSize();
				_vis.update();
			}		
		} 
		
		public function resize(visBounds:Rectangle):void
		{
			_bounds.x = visBounds.x + 5;// + 5;
			_bounds.y = visBounds.y + 5;// + 5;
			_bounds.width = visBounds.width - 30;
			_bounds.height = visBounds.height - 30;
			
			super.scrollRect = visBounds;
			
			if(_vis) {
				_vis.bounds = _bounds;
			}
			
			// set visualization bounds and update axes
            _vis.setAspectRatio((visBounds.width/visBounds.height), _bounds.width, _bounds.height);
            //_vis.axes.update();
			
			_vis.update();
			invalidateSize();
		}
		
		/** Visualizes the data with default values. */ 
		private function visualize(t:Transitioner = null):void {
			
			// we need this to update the statistics
			this.dispatchEvent(new UStatisticsUpdateEvent(UStatisticsUpdateEvent.UPDATE_STATISTICS, true));
			
			// sort to ensure that children nodes are drawn over parents
			//_vis.data.nodes.sortBy("depth"); 
			//_vis.data.nodes.sortBy("degree"); 
			//_vis.data.nodes.sortBy("data.label");
			
			// set the property values for the nodes and edges
			setDataSpriteProperties();
		    
		    // add the labeler
		    _visHelper.applyDefaultControls(this);
		    _vis.operators.add(_visHelper.labelerNodes);
		    //_vis.operators.add(_visHelper.labelerEdges);
			
			if(t != null)
			   _vis.update(t);//update();
			else
			   _vis.update();//update();
			
			//if(rootNodeBeforeLineage.props.hasOwnProperty("label"))
			  // (_rootNodeBeforeLineage.props.label as TextSprite).verticalAnchor = TextSprite.TOP;
			
			// set the property values for the nodes and edges
			//setDataSpriteProperties();
			// compute the shapes and set the labels of the nodes
			drawNodes();
			
			_vis.operators.add(_graphDistanceFilter); //distance filter has to be added before the layout
			if(_currentLayout == FORCE_LAYOUT) {
			   _vis.operators.add(_forceDirectedLayout);
			   _vis.continuousUpdates = true;
			   //_vis.update(2).play();
			}
			else {
			   _vis.operators.add(_radialLayout); 
			   _vis.continuousUpdates = false;
			}
			
			//updateGraphRoot(root);
			if(t != null)
			   updateGraphRoot(null,true,t);
			else
			   updateGraphRoot();
			
			/*
			trace(" ------------------------------- Edges -------------------------------------- "); 
			trace("\n");
			for each (var es:EdgeSprite in _vis.data.edges) {
				trace("Edge tooltip is : " + es.data.tooltip);
				trace("Edge shape is : " + es.shape);
				trace("\n"); 
				//trace("Hansi Hinterseer !!!!!"); 
			}
			trace(" ------------------------------- Nodes -------------------------------------- "); 
			trace("\n");
			for each (var ns:NodeSprite in _vis.data.nodes) {
				trace("Node tooltip is : " + ns.data.tooltip);
				trace("Node shape is : " + ns.shape);
				trace("\n"); 
				//trace("Hansi Hinterseer !!!!!"); 
			}
			*/
			
		}
		
		/** Visualizes the data with default values. */ 
		private function visualizeLineage(lineageRootNode:NodeSprite, t:Transitioner = null):void {
			
			// we need this to update the statistics
			this.dispatchEvent(new UStatisticsUpdateEvent(UStatisticsUpdateEvent.UPDATE_STATISTICS, true));
			
			var lineageNodes:Array = _graphData.graphCache.lineageNodes;// 
			//var lineageEdges:Array = _graphData.graphCache.lineageEdges;//
			var tempNode:NodeSprite;
			
			for(var i:int = 0; i<lineageNodes.length; i++) {
				tempNode = NodeSprite(lineageNodes[i]);
				//if(tempNode === lineageRootNode)
				  // continue;
				//UNode.applyValuesToNode(tempNode, UNode.LINEAGE_NODE);
				drawNode(tempNode);
			}
			
			//setDataSpriteProperties(true);
			setDataSpriteProperties(true,t);
			//_vis.data.root = lineageRootNode;
			updateGraphRoot(lineageRootNode,true,t);
			//setDataSpriteProperties(true);
			//_vis.update();
			
			// apply all the controls of the _visHelper layout
			//_visHelper.applyLineageControls(this);
			
			//updateGraphRoot(lineageRootNode);
			// hide the selected data srpites again
			// this redundant, because we do this while entering the lineage mode, 
			// but properties of alle nodes are set again above 
			// -> so hide the selected DataSprite instances again
			//hideSelectedDataSprites();
			
			//setSingleNodeProperties(lineageRootNode, UNode.LINEAGE_NODE).play();
			//UNode.applyValues(lineageRootNode, UNode.LINEAGE_NODE);
			//_vis.operators.remove(_radialLayout);
			//_vis.operators.remove(_graphDistanceFilter);
			
			
			//setDataSpriteProperties(true,t);
			//_vis.update();
			//DirtySprite.renderDirty();
		}
		
		public function updateRoot(event:URootNodeUpdateEvent):void {
			
			// reset the root node shape
			//if(_rootNodeBeforeLineage != null) {
			   //_rootNodeBeforeLineage.shape = UNode.DEFAULT_NODE.shape;
			   //(_rootNodeBeforeLineage.props.label as TextSprite).verticalAnchor = TextSprite.TOP;
			//}
			
			event.stopImmediatePropagation();
			
			if (_inLineageMode) {
			   return;
			}
			
			if(event.useResultCluster) {
				_focusNodes = _graphData.queryResults[event.rootNodeName];
				//trace("-----------");
				//trace("event.rootNodeName : " + event.rootNodeName);
				//trace("-----------");
				if (_focusNodes == null)
				   return;
				_rootNodeBeforeLineage = requestHighestDegreeNode(_focusNodes);
				if (_rootNodeBeforeLineage == null)
				   return;
				   
				// set the result cluster id
				_graphDistanceFilter.resultClusterID = event.rootNodeName;
			}
			else {
				_rootNodeBeforeLineage = event.rootNode;
				_focusNodes = [_rootNodeBeforeLineage];
			}
			
			// differentiate the cases
			if (_rootNodeBeforeLineage == null) {// || _inLineageMode)  
			   //return;
			   _rootNodeBeforeLineage = _graphData.graphCache.usedNodes[event.rootNodeName];
			   _focusNodes = [_rootNodeBeforeLineage];
			}
			
			// set the shape for the new root node
			//if(_rootNodeBeforeLineage != null) {
			  //  _rootNodeBeforeLineage.shape = UShapes.DIAMOND;
			   //(_rootNodeBeforeLineage.props.label as TextSprite).verticalAnchor = TextSprite.MIDDLE;
			//}
			
			// do the root update animation
			updateGraphRoot();
			
		}
		
		public function updateGraphRoot(node:NodeSprite = null, doTransition:Boolean = true, t:Transitioner = null):void {
			
			// reset the root node shape
			//if(_rootNodeBeforeLineage != null) {
			  // _rootNodeBeforeLineage.shape = UNode.DEFAULT_NODE.shape;
			   //(_rootNodeBeforeLineage.props.label as TextSprite).verticalAnchor = TextSprite.TOP;
			//}
			
			if(node != null && !_inLineageMode) {
				_focusNodes = [node];
				_rootNodeBeforeLineage = node;
			}
			
			// if we are in lineage mode -> use the current passed node as new root node
			if(_inLineageMode)
			   _vis.data.root = node;
			// else, use the root node that we set before the entering of the lineage mode
			else
			   if(_rootNodeBeforeLineage != null)
			      _vis.data.root = _rootNodeBeforeLineage;
			    else
                  _vis.data.root = requestHighestDegreeNode(null);
            
            //if(_currentLayout == UFlareVis.RADIAL_LAYOUT) {
              
            // compute the spanning tree for the radial layout, not really needed for the force layout    
            var tree:Tree = _vis.data.tree; 

            //_graphDistanceFilter.focusNodes = [node];
            _graphDistanceFilter.focusNodes = _focusNodes;
		   	//_graphDistanceFilter.focusNodes = [node];

			//var t1:Transitioner = new Transitioner(2);
            
            // set the shape for the new root node
			//if(_rootNodeBeforeLineage != null) {
			  // _rootNodeBeforeLineage.shape = UShapes.DIAMOND;
			   //(_rootNodeBeforeLineage.props.label as TextSprite).verticalAnchor = TextSprite.MIDDLE;
			//}

            // if we use an alpha distance filter
            /*
            if(UFlareVis._alphaDistanceFilterActivated) {
            	updateAsSequence(t);
            	return;
            }*/

            if(doTransition) {
			   if(t == null) {
			   	  if(_currentLayout == UFlareVis.RADIAL_LAYOUT)
			         _vis.update(2).play();
			      else
			         //_vis.update(1).play();
			         _vis.update();
			   }
			   else
			      _vis.update(t);
            }
			else
			   _vis.update();
            //}
			
		}
		
		private function setDataSpriteProperties(lineageMode:Boolean = false, t:Transitioner = null):void {
			
			// --------------------------------------
			// first, set the values for the nodes
			// --------------------------------------
			
			_vis.data.nodes.visit(
			   function(ns:NodeSprite):void {
			      //UNode.applyValuesToNode(ns,UNode.DEFAULT_NODE,_nodeShadowEnabled,_nodeFilters,_glowModeEnabled);
			        
			      if(lineageMode) {  
			      
			          ns.filters = _nodeFilters;
			          //ns.filters = _nodeFiltersNoShadow;
			      
			         //we first set the filters, then we assume the filters as overwritten
			         //UNode.applyValuesToNode(ns,UNode.LINEAGE_NODE,_compareModeEnabled,_glowModeEnabled,t);
			         UNode.applyValuesToNode(ns,UNode.LINEAGE_NODE,_compareModeEnabled,t);
			      }
			      else {
			         
			         // if we want shadows -> set the filter
			         
			         //if(_nodeShadowEnabled)
			            ns.filters = _nodeFilters;
			         //else
			           // ns.filters = null;
			         
			         //we first set the filters, then we assume the filters as overwritten
			         //UNode.applyValuesToNode(ns,UNode.DEFAULT_NODE,_compareModeEnabled,_glowModeEnabled,t);
			         UNode.applyValuesToNode(ns,UNode.DEFAULT_NODE,_compareModeEnabled,t);
			      }
			   }
			);
			
			// now set the shape for the root node separately
			//_rootNodeBeforeLineage.shape = UShapes.DIAMOND;
			_graphDistanceFilter.changeFocusSpriteShapes();
			//if(rootNodeBeforeLineage.props.hasOwnProperty("label"))
			  //  (_rootNodeBeforeLineage.props.label as TextSprite).verticalAnchor = TextSprite.MIDDLE;
			
			// --------------------------------------
			// now, set the values for the edges
			// --------------------------------------
			
			_vis.data.edges.visit(
			   function(es:EdgeSprite):void {
			      //UEdge.applyValuesToEdge(es,UEdge.DEFAULT_EDGE);
			      
			      if(lineageMode) {  
			      
			         //es.filters = _edgeFilters;
			         es.filters = _edgeFiltersNoShadow;
			         
			         //we first set the filters, then we assume the filters as overwritten
			         //UEdge.applyValuesToEdge(es,UEdge.LINEAGE_EDGE,_compareModeEnabled,_glowModeEnabled);
			         UEdge.applyValuesToEdge(es,UEdge.LINEAGE_EDGE,_compareModeEnabled);
			      }
			      else {
			         
			         // if we want shadows -> set the filter
				     
				    // if(_edgeShadowEnabled)
				        es.filters = _edgeFilters;
				    // else
				      //  es.filters = null;
			         
			         //we first set the filters, then we assume the filters as overwritten
			         //UEdge.applyValuesToEdge(es,UEdge.DEFAULT_EDGE,_compareModeEnabled,_glowModeEnabled);
			         UEdge.applyValuesToEdge(es,UEdge.DEFAULT_EDGE,_compareModeEnabled);
			      }
			   }
			);
			
			//if(t != null)
			  // _vis.update(t);
			if(t == null)
			  _vis.update(2);
			else
			  _vis.update(t); 
		}
		
		private function drawNodes():void {  
			_vis.data.nodes.visit(function(ns:NodeSprite):void {
                  drawNode(ns);       
            });
		}
		
		public static function drawNodePolyblob(node:NodeSprite):void {
               
               var labelSize:int = computeLabelSize(node);
            
               _temporaryTextFormat.size = labelSize;//int(Math.ceil(14 * ratio));
               
               _temporaryTextSprite = new TextSprite(node.data.label,_temporaryTextFormat);
       
               var points:Array = new Array();
           
               var x:int = _temporaryTextSprite.x - _temporaryTextSprite.width / 2;
               var y:int = _temporaryTextSprite.y;// - 5;
               var width:int = _temporaryTextSprite.width;
               var height:int = _temporaryTextSprite.height;// + 10;
               
               node.props.label.size = _temporaryTextFormat.size;
               //ns.props.label.y = y + (ns.props.label.size);// / 2);
               //ns.props.label.y = y + (height / 2);
               
               points.push(x);//data.size/2);
               points.push(y);//ns.data.size/2);
               points.push(x);//-ns.data.size/2);
               points.push(y + height);//;ns.data.size/2);
               points.push(x + width);//ns.data.size/2);
               points.push(y + height);// ns.data.size/2);
               points.push(x + width);// ns.data.size/2);
               points.push(y);// -ns.data.size/2);
               node.points = points;
               
               // do not use caching for now
               //ns.cacheAsBitmap = true;
           
               //_visHelper.labelerNodes.verticalAnchor = 0;      
		}
		
		public static function drawNode(node:NodeSprite):void {
               
               var labelSize:int = computeLabelSize(node);
            
               _temporaryTextFormat.size = labelSize;//int(Math.ceil(14 * ratio));
               
               _temporaryTextSprite = new TextSprite(node.data.label,_temporaryTextFormat);
       
               var points:Array = new Array();
               var rectBounds:Array = new Array();
           
               var x:int = _temporaryTextSprite.x - _temporaryTextSprite.width / 2;
               var y:int = _temporaryTextSprite.y;// - 5;
               // the 10 pixel increase is needed because we cannot embed the textsprite anymore to support all languages 
               var width:int = _temporaryTextSprite.width + 2; 
               var height:int = _temporaryTextSprite.height;// + 10;
               
               node.props.label.size = _temporaryTextFormat.size;
               //ns.props.label.y = y + (ns.props.label.size);// / 2);
               //ns.props.label.y = y + (height / 2);
               
               rectBounds.push(x);//data.size/2);
               rectBounds.push(y-(height/2));//ns.data.size/2);
               rectBounds.push(width);//-ns.data.size/2);
               rectBounds.push(height);//;ns.data.size/2);
               
               // point x,y, width and heigth for the rounded rect as shape for the node
               node.data.rectBounds = rectBounds;
               
               points.push(x);//data.size/2);
               points.push(y);//ns.data.size/2);
               points.push(x);//-ns.data.size/2);
               points.push(y + height);//;ns.data.size/2);
               points.push(x + width);//ns.data.size/2);
               points.push(y + height);// ns.data.size/2);
               points.push(x + width);// ns.data.size/2);
               points.push(y);// -ns.data.size/2);
               node.points = points;
               
               // adjust the size of the node
               //node.size = (width > height) ? width : height;
               //node.radius = (width > height) ? (width/2) : (height/2);
               
               // new on 03.01.2011
               node.h = height;
               node.w = width;
               
               // do not use caching for now
               //ns.cacheAsBitmap = true;
           
               //_visHelper.labelerNodes.verticalAnchor = 0;      
		}
		
		public function showLineage(event:ULineageEvent):void {
		
		    // we do not want to draw the not lineage data in 
		    // correct colors -> the lineage visualization is messed up in this case
		    // so skip lineage mode if in glow mode
		    //if(_glowModeEnabled)
		      // return;
		    
		    // we are currently already in lineage mode -> abort   
		    if(_inLineageMode)
		       return;
		     
		    // set the lineage flag that indicates that we are in lineage mode
		    _inLineageMode = true;
		    // we do not want contiuous updates in lineage mode
		    _vis.continuousUpdates = false;
		    
		    //set the label text chaching to false
		    // -> this is needed for the Expand-Control
		    this._visHelper.labelerNodes.cacheText = false;
		    //this._visHelper.labelerEdges.cacheText = false;
		
		    var factID:String;
		    var seq:FunctionSequence
		    
		    // clear our fact-node mapping
		    UGraphUtil.clearFacNodeMapping();
		
		    switch(event.typeOfLineage) {
		    	case ULineageEvent.FACT_LINEAGE: {
		    		factID = event.factID;
				    _currentFactID = factID;
				    //var edge:EdgeSprite = _graphData.graphCache.usedEdges.factID;
					
					seq = new FunctionSequence();
					seq.push(playLineageTreeRootCreation, 1);
					//seq.push(playLineageTreeCreation, 0.01);
					
					// changed on 07.10.10
					seq.push(prepareLineageVisualization, 0.1);
					//seq.push(playLineageVisualization, 1.0);
					//seq.push(prepareLineageVisualization, 0.05);
					seq.push(playAddingTheTreeOperatorAndUpdate, 2);
					seq.push(playLineageVisualization, 2.0);
					//seq.push(playLineageSpritesPainting, 1);
					//seq.push(playContextEdgePainting, 0.05);
					//seq.push(_vis.updateLater(),0.1);
					seq.play();
					break;
		    	}
		    	case ULineageEvent.MATCHED_PATTERN_LINEAGE: {
		    		// set the current result lineage
					_currentResultLineage = event.lineageFacts;
					seq = new FunctionSequence();
					seq.push(playLineageTreeRootCreationMatchedPattern, 1);
					//seq.push(playLineageTreeCreation, 0.01);
					
					// changed on 07.10.10
					seq.push(prepareLineageVisualization, 0.1);
					//seq.push(playLineageVisualization, 1.0);
					//seq.push(prepareLineageVisualization, 0.05);
					seq.push(playAddingTheTreeOperatorAndUpdate, 2);
					seq.push(playLineageVisualization, 2.0);
					//seq.push(playLineageSpritesPainting, 1);
					//seq.push(playContextEdgePainting, 0.05);
					//seq.push(_vis.updateLater(),0.1);
					seq.play();
					break;
		    	}
		    	case ULineageEvent.FULL_LINEAGE: {
		    		// set teh current result lineage
					_currentCompleteLineage = event.completeResultLineage;
					_currentLineageQueryString = event.queryString;
					seq = new FunctionSequence();
					seq.push(playLineageTreeRootCreationComplete, 1);
					//seq.push(playLineageTreeCreation, 0.01);
					
					// changed on 07.10.10
					seq.push(prepareLineageVisualization, 0.1);
					//seq.push(playLineageVisualization, 1.0);
					//seq.push(prepareLineageVisualization, 0.05);
					seq.push(playAddingTheTreeOperatorAndUpdate, 2);
					seq.push(playLineageVisualization, 2.0);
					//seq.push(playLineageSpritesPainting, 1);
					//seq.push(playContextEdgePainting, 0.05);
					//seq.push(_vis.updateLater(),0.1);
					seq.play();
					break;
		    	}
		    	default : {
		    		return;
		    	}
		    }
		}
		
		private function repositionLineageNodes(t:Transitioner):void {
			//t.$(_lineageRootNode).x = _lineageRootNode.data.x;
			//t.$(_lineageRootNode).y = _lineageRootNode.data.y;
			/* for each (var node:NodeSprite in _graphData.graphCache.lineageNodes) {
			   t.$(node).x = _lineageTreeLayout.layoutRoot.x;
			   t.$(node).y = _lineageTreeLayout.layoutRoot.y;
			} */
			/* for each (var node:NodeSprite in _graphData.graphCache.lineageNodes) {
			   node.x = _rootNodeBeforeLineage.x;//_lineageTreeLayout.layoutRoot.x;
			   node.y = _rootNodeBeforeLineage.y;//_lineageTreeLayout.layoutRoot.y;
			} */
			_lineageTreeLayout.autoAnchorNodePositions(_graphData.graphCache.lineageNodes,t);
		}
		
		private function playLineageTreeRootCreation(t:Transitioner):void {
			_vis.operators.remove(_graphDistanceFilter);
		    // add the lineage root node and remap the edges of the source and target node of
		    // the clicked edge to be connected to the new lineage node
		    var tempLinRootNode:NodeSprite  = UGraphUtil.produceLineageGraph(_currentFactID,this,t);
		    if(tempLinRootNode != null)
		       _lineageRootNode = tempLinRootNode;
		  
		    _vis.update(t);
		}
		
		private function playLineageTreeRootCreationMatchedPattern(t:Transitioner):void {
			_vis.operators.remove(_graphDistanceFilter);
		    // add the lineage root node and remap the edges of the source and target node of
		    // the clicked edge to be connected to the new lineage node
		    var tempLinRootNode:NodeSprite  = 
		          //UGraphUtil.produceLineageGraphMatchedPattern(_currentResultLineage,this,t);
		          UGraphUtil.produceLineageGraphMatchedPattern(_currentResultLineage,this,t,null);
		    if(tempLinRootNode != null)
		       _lineageRootNode = tempLinRootNode;
		  
		    _vis.update(t);
		}
		
		private function playLineageTreeRootCreationComplete(t:Transitioner):void {
			_vis.operators.remove(_graphDistanceFilter);
		    // add the lineage root node and remap the edges of the source and target node of
		    // the clicked edge to be connected to the new lineage node
		    var tempLinRootNode:NodeSprite  = 
		          UGraphUtil.produceLineageGraphFullResult(_currentCompleteLineage,this,t,_currentLineageQueryString);
		    if(tempLinRootNode != null)
		       _lineageRootNode = tempLinRootNode;
		  
		    _vis.update(t);
		}
		
		
		/* private function playLineageNodeHiding(t:Transitioner):void {
			UGraphUtil.buildLineageGraph(_currentFactID,_lineageRootNode,this);	  
		    _vis.update(t);
		    // adjust the edge label properties
		    adjustEdgeParameters(); 
		} */
		
		
		private function playLineageVisualization(t:Transitioner):void {
			// restrict the lineage tree to depth 1 first
			//UGraphUtil.cutTree(_lineageRootNode,t);
			visualizeLineage(_lineageRootNode,t);
			/*
			var node:NodeSprite;
			var edge:EdgeSprite;
			for each (node in _graphData.graphCache.lineageNodes) {
				node.dirty();
			}
			for each (edge in _graphData.graphCache.lineageEdges) {
				edge.dirty();
			}	
			*/  
		}
		
		private function playAddingTheTreeOperatorAndUpdate(t:Transitioner):void {
			//prepareLineageVisualization();
			_vis.operators.add(_lineageTreeLayout); 
			repositionLineageNodes(t);
		    _vis.update(t);//(t);
		    //repositionLineageRootNode(t);
		}
		
		private function playLineageSpritesPainting(t:Transitioner):void {
			setDataSpriteProperties(true,t);
		}
		
		/*
		private function playContextEdgePainting(t:Transitioner):void {
			_vis.data.edges.visit(
				      function(es:EdgeSprite):void {
				          if(es.data.hasOwnProperty("context") && es.data.context) {
				   	        t.$(es).visible = UEdge.LINEAGE_EDGE.showContextEdges;//false;
				   	        //t.$(es).alpha = 0.5;
				   	        t.$(es).lineAlpha = 0.5;
				          }
				      }
			);
		}
		*/
		
		private function prepareLineageVisualization(t:Transitioner):void {
			
			//_vis.operators.remove(_graphDistanceFilter);
			_vis.operators.remove(_radialLayout);
			_vis.operators.remove(_forceDirectedLayout);
	        _vis.update(t);
		}
		
		public function leaveLineageMode(event:ULineageLeaveEvent):void {
			
			// if we are not in lineage mode, why doing anything?? !!
			if(!_inLineageMode)
			   return;
			
			//set the label text chaching to true again
		    this._visHelper.labelerNodes.cacheText = true;
		    //this._visHelper.labelerEdges.cacheText = true;
			
			var seq:FunctionSequence = new FunctionSequence();
			seq.push(cleanLineageProperties,0.2);
			//seq.push(removeLineageData,1);
			seq.push(visualize,2);
			//seq.push(resetNodes,1);
			seq.push(removeLineageData,1);
			seq.play();
			 /* remove all the lineage nodes and edges from the visualization`s data instance
			 * and add the source and target nodes that were merged, as well as their connecting
			 * edges to the data instance again
			 */
			 //removeLineageData();
			 
			 // currently, only sets the mouse-enabled flag for all nodes to true
			 //resetNodes();
			
			// set the lineage flag that indicates that we are no longer in lineage mode
		    //_inLineageMode = false;
			
			// switch to the default mode again and visualize the graph
			//visualize();
		}
		
		private function cleanLineageProperties(t:Transitioner):void {
			//clear the current operators first
		    _vis.operators.clear();
		    clearControls();
		    _vis.update(t);
			
			// set the lineage flag that indicates that we are no longer in lineage mode
		    _inLineageMode = false;
		}
		
		private function resetNodes(t:Transitioner = null):void {
			// all nodes and edges should be mouse-enabled again -> brute force
			 if(t == null) {
			 	_vis.data.nodes.visit(
			      function(ns:NodeSprite):void {
			      	  ns.data.context = false;
			      	  ns.mouseEnabled = true;
			      	  ns.fillAlpha = UNode.DEFAULT_NODE.alpha;
			      	  //UNode.applyValuesToNode(ns,UNode.DEFAULT_NODE,false,false,t);
			      	  /*
			      	  ns.data.context = false;
			      	  ns.mouseEnabled = true;
			      	  ns.alpha = 1.0;
			      	  ns.fillAlpha = 1.0;
			      	  ns.lineAlpha = 1.0;
			      	  ns.expanded = true;
			      	  */
			      }
			    );
			 }
			 else {
			 	_vis.data.nodes.visit(
			      function(ns:NodeSprite):void {
			      	  ns.data.context = false;
			      	  t.$(ns).mouseEnabled = true;
			      	  t.$(ns).fillAlpha = UNode.DEFAULT_NODE.alpha;
			      	  //UNode.applyValuesToNode(ns,UNode.DEFAULT_NODE,false,false,t);
			      	  /*
			      	  ns.data.context = false;
			      	  t.$(ns).mouseEnabled = true;
			      	  t.$(ns).alpha = 1.0;
			      	  t.$(ns).fillAlpha = 1.0;
			      	  t.$(ns).lineAlpha = 1.0;
			      	  t.$(ns).expanded = true;
			      	  */
			      }
			    );
			 } 
			 
		}
		
		private function removeLineageData(t:Transitioner = null):void {
			// temporary node and edge references
			var node:NodeSprite;
			var edge:EdgeSprite;
			
			// remove the nodes and edges
			for each (node in _graphData.graphCache.lineageNodes) {
				_vis.data.removeNode(node);
				// we do it in the cache as well, when reallocating a node
				// so it is redundant, but probably necessary to prevent
				// the visualization of showing not connected edges
				node.removeAllEdges();
				//node = null;
			}
			for each (edge in _graphData.graphCache.lineageEdges) {
				_vis.data.removeEdge(edge);
			}
			
			// clear the lineage data
			_graphData.graphCache.clearLineageData();
			
			// we need this to update the statistics
			this.dispatchEvent(new UStatisticsUpdateEvent(UStatisticsUpdateEvent.UPDATE_STATISTICS, true));
		}
		
		private function clearControls():void {
			var size:int = _vis.controls.length;
			if(size <= 1)
			   return;
			   // skip the pan zoom control
			for (var i:int = 1; i<size; i++) {
				_vis.controls.removeControlAt(i);
			}
		}
		
		public function loadGraphData(queryResult:UQueryResult, dataLoadNode:NodeSprite = null, 
		                  compareMode:Boolean = false, queryString:String = null):void {
			
			// set the compare mode flag
			_compareModeEnabled = compareMode;//_compareModeEnabled;
			
			// set the Old values for the current data
			// this is needed to differentiate the old nodes and edges
			// from teh new ones
			if(_compareModeEnabled) {
				_vis.data.nodes.visit(
				      function(ns:NodeSprite):void {
				          ns.data.Old = true;
				          ns.data.New = false;
				      }
				);
				_vis.data.edges.visit(
				      function(es:EdgeSprite):void {
				          es.data.Old = true;
				          es.data.New = false;
				      }
				);
			}
			
			else {
				_vis.data.nodes.visit(
				      function(ns:NodeSprite):void {
				          delete ns.data.Old;
				          delete ns.data.New;
				      }
				);
				_vis.data.edges.visit(
				      function(es:EdgeSprite):void {
				          delete es.data.Old;
				          delete es.data.New;
				      }
				);
			}
			
			// load the data
			_focusNodes = _graphData.loadGraphData(_vis.data,queryResult,compareMode,queryString,dataLoadNode);
			
			// set the focus nodes and the root node
			if (_focusNodes == null)
			   return;
			if(dataLoadNode == null)
			   _rootNodeBeforeLineage = requestHighestDegreeNode(_focusNodes);
			else 
			   _rootNodeBeforeLineage = dataLoadNode;
			if (_rootNodeBeforeLineage == null)
			   return;
			
			// temporarily fix the current root node -> layout stabilizes sooner
			if(!_rootNodeBeforeLineage.fixed) {
			   _rootNodeBeforeLineage.fix();
			   _rootNodeBeforeLineage.data.fixed = true;
			}
			
			// update the duplicate reduced edge list for the force layout
			//trace("Number of Edges is : " + _vis.data.edges.length.toString());
			//this._forceDirectedLayout.forceEdgeList = _graphData.forceEdgeList;
			
			// set the current query result id for the graph distance filter
	        _graphDistanceFilter.resultClusterID = _graphData.currentResultString; 
			
			//all nodes connected to the dynamically reloaded node start in the same position as this node
			var signX:int;
			var signY:int;
			//var t:Transitioner = new Transitioner(2);
			//var circleRange:number = 2*Math.PI*150;
			if(dataLoadNode != null) {
			     dataLoadNode.visitNodes(
			        function(ns:NodeSprite):void {
			   	      
			   	      // only unfixed nodes are repositioned
			   	      if(!ns.fixed) {
				   	      signX = (Math.random() < 0.5) ? -1 : 1;
				   	      signY = (Math.random() < 0.5) ? -1 : 1;
				   	      
				   	      ns.x = dataLoadNode.x + (signX * (int(Math.random() * 100)));
				   	      ns.y = dataLoadNode.y + (signY * (int(Math.random() * 100)));
				   	      
				   	      //ns.x = dataLoadNode.x;
				   	      //ns.y = dataLoadNode.y;
				   	      ns.fix();
				   	      ns.data.fixed = true;
			   	      }
			   	     
			        }
			     );
			}
			
			//clear the _visHelper operators first
			_vis.operators.clear();
			clearControls();
			_vis.update();
			
			// compute the highest node degree
			computeHighestNodeDegree();
			
			// adjust the edge parameters
	        adjustEdgeParameters();
	        
	        // new on 15.11.10
	        //for each(var node:NodeSprite in _vis.data.nodes) {
	          //  UDisplays.sortChildren(node,USort.sortOnLabel(true));
	        //}
	        //UDisplays.sortChildren(dataLoadNode,USort.sortOnLabel(true));
			    
			// visualizae the changes
			visualize();
			
			// unfix the current root node again if fixed before
			if(_rootNodeBeforeLineage.fixed && _rootNodeBeforeLineage.data.fixed) {
			   _rootNodeBeforeLineage.unfix();
			   _rootNodeBeforeLineage.data.fixed = false;
			}
			   
			if(dataLoadNode != null) {
				dataLoadNode.visitNodes(
			        function(ns:NodeSprite):void {
			   	      if(ns.data.fixed) {
				   	      ns.unfix();
				   	      ns.data.fixed = false;
				   	  }
			        }
				 );
			}
		}
		
		public function compareClusters(event:UClusterCompareEvent, compareMode:Boolean = false):void {
			
			// set the compare mode flag
			_compareModeEnabled = compareMode;//_compareModeEnabled;
			
			// set the Old values for the current data
			// this is needed to differentiate the old nodes and edges
			// from teh new ones
			if(_compareModeEnabled) {
				
				_vis.data.nodes.visit(
				      function(ns:NodeSprite):void {
				          delete ns.data.Old;
				          delete ns.data.New;
				          // set a new color
				      }
				);
				
				_vis.data.edges.visit(
				      function(es:EdgeSprite):void {
			          	 delete es.data.Old;
			             delete es.data.New;
				      }
				);
				
				var blackCluster:Array = _graphData.queryResults[event.blackClusterID]; 
			    var whiteCluster:Array = _graphData.queryResults[event.whiteClusterID];
			    var node:NodeSprite;
			    var edge:EdgeSprite;
			    var ds:DataSprite
			    var tempDataHash:Object = new Object();
			    var label:String;
			    
			    // all black cluster nodes
			    for each (ds in blackCluster) {
			    	ds.data.Old = true;
			    	ds.data.New = false;
			    	if(ds is NodeSprite) {
			    	   node = ds as NodeSprite;
			    	   tempDataHash[node.data.tooltip] = node;
			    	}
			    	else {
			    	   edge = ds as EdgeSprite;
			    	   tempDataHash[edge.data.factID] = edge;
			    	}
			    }
			    
			    for each (ds in whiteCluster) {
			    	
			    	if(ds is NodeSprite) { 
			    	
			    	   node = ds as NodeSprite;
			    	
			    	   // a shared node
				       if(tempDataHash.hasOwnProperty(node.data.tooltip)) {
					    	node.data.Old = true;
					    	node.data.New = true;
				       }
				       // the node is only in the white cluster
				       else {
				    		node.data.Old = false;
					    	node.data.New = true;
					    	tempDataHash[node.data.tooltip] = node;
				       }
			    	}
			    	else {
			    	 
			    	   edge = ds as EdgeSprite;
			    	
			    	   // a shared node
				       if(tempDataHash.hasOwnProperty(edge.data.factID)) {
					    	edge.data.Old = true;
					    	edge.data.New = true;
				       }
				        // the node is only in the white cluster
				       else {
				    		edge.data.Old = false;
					    	edge.data.New = true;
					    	tempDataHash[edge.data.factID] = edge;
				       }
			    	}
			    		
			    }
			    
			    // clear the data
			    //for (nodeLabel in tempDataHash) {
			    	//tempDataHash[nodeLabel] = null;
			    	//delete tempDataHash[nodeLabel];
			    //}
			    //tempDataHash = null;
				/*
				_vis.data.edges.visit(
				      function(es:EdgeSprite):void {
				          
				          // the edge is in the black cluster
				          if(es.data.hasOwnProperty(event.blackClusterID)) {
				          	 // the edge is also in the white cluster
				          	 if(es.data.hasOwnProperty(event.whiteClusterID)) {
				          	 	 es.data.Old = true;
				          	 	 es.data.New = true;
				          	 }
				          	 // the edge is only in the black cluster
				          	 else {
				          	 	 es.data.Old = false;
				          	 	 es.data.New = true;
				          	 }    
				          }
				          else
				            // the edge is only in the white cluster
				            if(es.data.hasOwnProperty(event.whiteClusterID)) {
				          	 	 es.data.Old = false;
				          	 	 es.data.New = true;
				          	 }
				           // the edge is only neither in the black nor in the white cluster 
				          else {
				          	 delete es.data.Old;
				             delete es.data.New;
				          }
				      }
				);
				*/
				
				var sprites:Array = new Array();
				
				for each (ds in tempDataHash) {
					sprites.push(ds);
				}
				
				// add these data sprites as focus sprites
				_graphDistanceFilter.updateFocusSprites(sprites,tempDataHash);
                
                // now get the current focus sprite (nodes and edges) from the distance filter
				_focusNodes = sprites;
				
				if (_focusNodes == null)
				   return;
				
				// reset the root node shape
				if(_rootNodeBeforeLineage != null) {
				   _rootNodeBeforeLineage.shape = UNode.DEFAULT_NODE.shape;
				   //if(rootNodeBeforeLineage.props.hasOwnProperty("label"))
				     //  (_rootNodeBeforeLineage.props.label as TextSprite).verticalAnchor = TextSprite.TOP;
				}
				
				_rootNodeBeforeLineage = requestHighestDegreeNode(_focusNodes);
				
				_vis.data.root = _rootNodeBeforeLineage;
                
                var tree:Tree = _vis.data.tree; 
				
				// set the shape for the new root node
				//if(_rootNodeBeforeLineage != null) {
				  // _rootNodeBeforeLineage.shape = UShapes.DIAMOND;
				   //(_rootNodeBeforeLineage.props.label as TextSprite).verticalAnchor = TextSprite.MIDDLE;
				//}
				
				event.stopImmediatePropagation();
				
				setDataSpriteProperties();
				
				_vis.update(2).play();
			}
			
			else 
			   return;
			     	
		}
		
		/**
		 * Computes and returns the avg degree that is present in the node list.
		 * 
		 * @return the avg node degree.
		 * */
		private function computeAvgNodeDegree():Number {
			var avgDegree:Number = 0;
			_vis.data.nodes.visit(function(ns:NodeSprite):void {
				   avgDegree += ns.degree;
			});
			// return the avg degree
			return (avgDegree / _vis.data.nodes.length);
		}
		
		/**
		 * Computes the highest node degree of the nodes in the data.
		 * */
		private function computeHighestNodeDegree():void {
			var highestDegree:int = 0;
			_vis.data.nodes.visit(function(ns:NodeSprite):void {
				if(ns.degree >= highestDegree)
				   highestDegree = ns.degree;
			});
			_graphData.highestNodeDegree = highestDegree;
		}
		
		/**
		 * Computes the label size for a given node.
		 * 
		 * @return the labal size.
		 * */
		private static function computeLabelSize(node:NodeSprite):int {
			
			// if we have this special node -> return the highest possible lable size
			if(node.data.hasOwnProperty(UStringConstants.REQUEST_NODE)) 
			   return 20;
			
			// compute a _visHelper node degree - highest degree ratio
            //var ratio:Number = node.degree / _graphData.highestNodeDegree; 
            var ratio:Number = node.degree / UGraphData.highestNodeDegree;  
            // translate that into an upscaled (ceiling) integer value     
	        var size:int = int(Math.ceil(ratio*10));
	        // return 5 different sizes, depending on the degree 
	        // of the node and the resulting size indicator
	        // the node labels will be in the range of 9 to 16 ([9,16])
            switch(size) {
		   	  case 0: return 9;
		   	  case 1: return 9;
		   	  case 2: return 9;
		   	  case 3: return 10;
		   	  case 4: return 11;
		   	  case 5: return 12;
		   	  case 6: return 13;
		   	  case 7: return 14;
		   	  case 8: return 15;
		   	  // case 9 and 10 summarized to the default case
		   	  default: return 16;
		    }
            
		}
		
		private function requestHighestDegreeNode(focusSprites:Array = null):NodeSprite {
			var highestDegree:int = 0;
			var highestDegreeNode:NodeSprite;
			var node:NodeSprite;
			var ds:DataSprite;
			
			if(focusSprites == null) {
				_vis.data.nodes.visit(function(ns:NodeSprite):void {
					if(ns.degree >= highestDegree) {
					   highestDegree = ns.degree;
					   highestDegreeNode = ns;
					}
			    });
			    
			    _graphData.highestNodeDegree = highestDegree;
			}
			else {
				for each (ds in focusSprites) {
					if(ds is EdgeSprite)
					   continue;
					
					node = ds as NodeSprite;
					
					if(node.degree >= highestDegree) {
					   highestDegree = node.degree;
					   highestDegreeNode = node;
					}
			    }
			}
			
			return highestDegreeNode;
		}
		
		public function centerVisualization(event:Event):void {
			_panZoomCtrlVis.resetPanZoom();
		}
		
		private function setAllSpritesVisible():void {
			if(_numOfHops < 0) {
	       	  _vis.data.nodes.visit(
			      function(ns:NodeSprite):void {
			         ns.visible = true;
			      }
			  );
			  _vis.data.edges.visit(
				  function(es:EdgeSprite):void {
				     es.visible = true;
				  }
			  );	
	       }
		}
		
		 public function activateCompareMode(compareModeEnabled:Boolean = false):void {
			
			//if(_inLineageMode) 
			  // return;
			
			//_glowModeEnabled = glowModeEnabled;
			_compareModeEnabled = compareModeEnabled;
			
			// --------------------------------
			// we enter the comparison mode
			// --------------------------------
			
			if(_compareModeEnabled) {
				_vis.data.nodes.visit(
				      function(ns:NodeSprite):void {
				         UNode.applyGlowToNode(ns,UNode.DEFAULT_NODE);
				      }
				);
				_vis.data.edges.visit(
				      function(es:EdgeSprite):void {
				         UEdge.applyGlowToEdge(es,UEdge.DEFAULT_EDGE);
				      }
				);	
			}
			
			// --------------------------------
			// we leave the comparison mode
			// --------------------------------
			
			else {	
				_vis.data.nodes.visit(
				      function(ns:NodeSprite):void {
				         ns.filters = _nodeFilters;
				      }
				);
				if(_inLineageMode) {
					_vis.data.edges.visit(
					      function(es:EdgeSprite):void {
					         es.filters = this._edgeFiltersNoShadow;
					      }
					);	
				}
				else {
					_vis.data.edges.visit(
					      function(es:EdgeSprite):void {
					         es.filters = _edgeFilters;
					      }
					);	
				}
			}
				
		} 
		
		public function updateNodeAndLabelColors(event:UNodeAndLabelColorEvent):void {
			
			// we are in lineage mode and want to change the visibility of the context sprites
			/*
			if(event.changeContextNodesVisibility) {
				// we are in lineage mode 
			    if(_inLineageMode) {			
					_vis.data.nodes.visit(
					      function(node:NodeSprite):void {
					         if(node.data.hasOwnProperty("context") && node.data.context)
					            node.visible = !node.visible;
					      }
					);
					UNode.DEFAULT_NODE.showContextNodes = !UNode.DEFAULT_NODE.showContextNodes;
					UNode.LINEAGE_NODE.showContextNodes = !UNode.LINEAGE_NODE.showContextNodes;
					_vis.data.edges.visit(
					      function(es:EdgeSprite):void {
					         if(es.data.hasOwnProperty("context") && es.data.context)
					            es.visible = !es.visible;
					      }
					);
					UEdge.DEFAULT_EDGE.showContextEdges = !UEdge.DEFAULT_EDGE.showContextEdges;
					UEdge.LINEAGE_EDGE.showContextEdges = !UEdge.LINEAGE_EDGE.showContextEdges;
					return;
			    }
			    else 
			      return;
			}*/
			
			if(event.doLabelColor) {
				_visHelper.changeLabelFormatterNodes("Verdana", event.labelColor, 12, true);
				
				_vis.data.nodes.visit(
				      function(ns:NodeSprite):void {
				         (ns.props.label as TextSprite).color = event.labelColor;
				      }
				);
			}
			else {
				UNode.DEFAULT_NODE.fillColor = event.nodeColor;
				UNode.DEFAULT_NODE.alpha = event.nodeAlpha;
				
				_vis.data.nodes.visit(
				      function(ns:NodeSprite):void {
				         
				         if(!ns.data.hasOwnProperty("linTree")) {
				            ns.fillColor = event.nodeColor;
				            ns.fillAlpha = event.nodeAlpha;
				         }
				      }
				);
			}
		}
		
		public function updateEdgeColors(event:UNodeAndLabelColorEvent):void {
			
			// change label visibility for the edge labels
			if(event.changeLabelVisibility) {
				
				_graphData.graphCache.edgeLabelsVisible = !_graphData.graphCache.edgeLabelsVisible;
				
				var textSprite:TextSprite;
				_vis.data.edges.visit(
				      function(es:EdgeSprite):void {
				         textSprite = es.data.TS as TextSprite;
				         //textSprite.visible = !textSprite.visible;
				         try {
				            es.getChildIndex(textSprite);
				            es.removeChild(textSprite);
				            textSprite.removeEventListener(MouseEvent.MOUSE_OVER,UGraphControls.hoverOverEdgeLabel);
					        textSprite.removeEventListener(MouseEvent.MOUSE_OUT,UGraphControls.hoverOutEdgeLabel);
					        if(!es.data.linTree && !es.data.context && !es.data.hasOwnProperty(UStringConstants.REQUEST_EDGE))
					           textSprite.removeEventListener(MouseEvent.CLICK,UGraphControls.clickEdgeLabel);
			             }
				         catch(e:Error){
				         	textSprite.addEventListener(MouseEvent.MOUSE_OVER,UGraphControls.hoverOverEdgeLabel,false,0,true);
					        textSprite.addEventListener(MouseEvent.MOUSE_OUT,UGraphControls.hoverOutEdgeLabel,false,0,true);
					        if(!es.data.linTree && !es.data.context && !es.data.hasOwnProperty(UStringConstants.REQUEST_EDGE))
					           textSprite.addEventListener(MouseEvent.CLICK,UGraphControls.clickEdgeLabel,false,0,true);
				            es.addChild(textSprite);
				         }
				      }
				);
				
				return;
			}
			
			if(event.doLabelColor) {
				
				//_visHelper.changeLabelFormatterEdges("Verdana", event.labelColor, 12, true);
				_graphData.graphCache.labelFormatterEdges.color = event.labelColor;
				
				_vis.data.edges.visit(
				      function(es:EdgeSprite):void {
				         (es.data.TS as TextSprite).color = event.labelColor;
				      }
				);
				
				return;
			}
			
			// we are in lineage mode -> do nothing
			if(_inLineageMode)
			   return;
			
			if(event.changeEdgeAlpha) {
				
				UEdge.DEFAULT_EDGE.useAlpha = event.useEdgeAlpha;
				
				for each (edge in _vis.data.edges) {
					UEdge.changeEdgeAlpha(edge);
				}
				
				return;
			}
			
			if(event.setTrueEdgeColor)
				UEdge.DEFAULT_EDGE.trueEdgeColor = event.edgeColor;
		    else
		        UEdge.DEFAULT_EDGE.falseEdgeColor = event.edgeColor; 
			
			
			//UEdge.DEFAULT_EDGE.trueEdgeColor = UColors.magenta(7);
		    //UEdge.DEFAULT_EDGE.falseEdgeColor = UColors.yellow(7); 
			
			var edge:EdgeSprite;
				
			for each (edge in _vis.data.edges) {
				//UEdge.applyValuesToEdge(edge, UEdge.DEFAULT_EDGE, _compareModeEnabled, _glowModeEnabled);
				//edge.dirty();
				UEdge.changeEdgeColor(edge,UEdge.DEFAULT_EDGE);
			}
			
		}
		
		public function changeComparisonColors(event:UComparisonColorEvent):void {
			
			if(_inLineageMode)
			   return;
			
			UNode.changeComparisonFilterColor(event);
			UEdge.changeComparisonFilterColor(event);
			var node:NodeSprite;
			var edge:EdgeSprite;
			for each (node in _vis.data.nodes) {
				UNode.applyGlowToNode(node,UNode.DEFAULT_NODE);
			}
			for each (edge in _vis.data.edges) {
				UEdge.applyGlowToEdge(edge,UEdge.DEFAULT_EDGE);
			} 
		}
		
		public function selectFact(event:UFactSelectEvent):void {
			if(event == null)
			   return;
			   
			// get the corresponding edge and the source and target nodes
			var edge:EdgeSprite = graphData.graphCache.usedEdges[event.factID];
			var sourceNode:NodeSprite = edge.source;
			var targetNode:NodeSprite = edge.target;
			
			var factFocusArray:Array = new Array(); 
			
			factFocusArray.push(edge);
			factFocusArray.push(sourceNode);
			factFocusArray.push(targetNode);
			
			// add these data sprites as focus sprites
			_graphDistanceFilter.focusNodes = factFocusArray;//updateFocusSprites(sprites,tempDataHash);
            
            // now get the current focus sprite (nodes and edges) from the distance filter
			_focusNodes = factFocusArray;
			
			if (_focusNodes == null)
			   return;
			
			_rootNodeBeforeLineage = requestHighestDegreeNode(_focusNodes);
			
			_vis.data.root = _rootNodeBeforeLineage;
            
            var tree:Tree = _vis.data.tree; 
			
			event.stopImmediatePropagation();
			
			//setDataSpriteProperties();
			if(_currentLayout == UFlareVis.RADIAL_LAYOUT)
			    _vis.update(2).play();
			else
			    _vis.update();
		}
		
		private function adjustEdgeParameters():void {
			
			var edge:EdgeSprite;
			var s:NodeSprite;
			var t:NodeSprite;
            var duplicateOutCounter:int = 0;
            var duplicateInCounter:int = 0;
            var childNumber:int = 1;
            var pos:int = 0;
            var childEdge:EdgeSprite;
            var childNode:NodeSprite;
            var gap:Number = 1;
            
			for each (edge in _vis.data.edges) {
				
				if (edge == null) { return; } // TODO: throw exception?
				
				s = edge.source;
				t = edge.target;
	            duplicateOutCounter = 0;
	            duplicateInCounter = 0;
	            childNumber = 1;
	            pos = 0;
	            	
	            for(pos; pos < s.outDegree; pos++) {
	            	childEdge = s.getOutEdge(pos);
	            	//trace("childEdge.data.label: " + childEdge.data.label);
	            	
	            	// we have the same target node for the source node
	            	if(childEdge.target.data.label == t.data.label) {
	            		// this is our current edge
	            		if(childEdge.data.factID == edge.data.factID) {
	            		   duplicateOutCounter++;
	            		   childNumber = duplicateOutCounter;   
	            		}
	            		// we have another edge with the same source and target node
	            		else
	            		   duplicateOutCounter++; 
	            	}
	            	
	            }
	            
	            for(pos = 0; pos < t.outDegree; pos++) {
	            	childEdge = t.getOutEdge(pos);
	            	//trace("childEdge.data.label: " + childEdge.data.label);
	            	
	            	// we have the same target node for the source node
	            	if(childEdge.target.data.label == s.data.label) {
	            		duplicateInCounter++; 
	                }
	            }
	
	            //edge.data.DOC = duplicateOutCounter;
	            //edge.data.DIC = duplicateInCounter;
	            edge.data.NOC = duplicateOutCounter + duplicateInCounter;
	            gap = childNumber * 20;
	            edge.data.GAP = gap;
			}
		}
		
		/**
		 * Starts pulsating and fixes high degree nodes or stops 
		 * pulsating and unfixes high degree nodes.
		 * */
		public function doPulsating(event:UPulsateEvent = null):void {
			if(event.doPulsate) {
				// save the temp force values
				_tempPulsatingValues.massValue = _forceDirectedLayout.defaultParticleMass;
				_tempPulsatingValues.springLength = _forceDirectedLayout.defaultSpringLength;
				_tempPulsatingValues.pull = true;
				
			    //_forceDirectedLayout.defaultParticleMass = 1;     // default:  1
			    //_forceDirectedLayout.defaultSpringTension = 0.05;//0.001;  // default:  0.1
			    //_forceDirectedLayout.defaultSpringLength = 200;   // default: 30
			    //_forceDirectedLayout.simulation.nbodyForce.gravitation = -10;
				
				_pulsatingTimer.start();
				//_pulsatingTimerPush.start();
				var avgDegree:Number = computeAvgNodeDegree();
				_vis.data.nodes.visit(
			      function(ns:NodeSprite):void {
			        if(ns.degree >= avgDegree)
			           ns.fix();
			      }
			   );
			}
			else {
				_pulsatingTimer.stop();
				//_pulsatingTimerPull.stop();
			    //_pulsatingTimerPush.stop();
				_vis.data.nodes.visit(
			      function(ns:NodeSprite):void {
			        if(ns.fixed)
			           ns.unfix();
			      }
			   );
			   
			   // reset the force layout values
			   _forceDirectedLayout.defaultParticleMass = _tempPulsatingValues.massValue;
			   _forceDirectedLayout.defaultSpringLength = _tempPulsatingValues.springLength;
			   
			}
		}
		
		public function fixUnfixClusters(event:UClusterFixingEvent):void {
			var node:NodeSprite
			var avgDegree:Number = computeAvgNodeDegree();
			if(event.fixClusters){
				if(event.fixUnfixNumber == 0) {
				   _vis.data.nodes.visit(
				      function(ns:NodeSprite):void {
				        if(!ns.fixed)
				           ns.fix();
				      }
				   );
				}
				else {
					_vis.data.nodes.visit(
				      function(ns:NodeSprite):void {
				        if((!ns.fixed) && (ns.degree >= (event.fixUnfixNumber*avgDegree))) 
				           ns.fix();
				      }
				   );
			   }
			}
			else {
			   	  if(event.fixUnfixNumber == 0) {
			   	     _vis.data.nodes.visit(
				        function(ns:NodeSprite):void {     
						   if(ns.fixed) 
						      ns.unfix(); 
				        }
				     );
			   	  }
			   	  else {
				   	  _vis.data.nodes.visit(
				       function(ns:NodeSprite):void {
				           if((ns.fixed) && (ns.degree < (event.fixUnfixNumber*avgDegree))) {
				              ns.unfix();
				           }
				        }
				     );
			     }
			}
		}
		
		/**
		 * Stop the whole pulsation process.
		 * */
		private function stopPulsation(event:TimerEvent = null):void {
			_pulsatingDurationTimer.stop();
			_vis.data.nodes.visit(
			      function(ns:NodeSprite):void {
			        if(ns.fixed)
			           ns.unfix();
			      }
			);
			// reset the force layout values
		    _forceDirectedLayout.defaultParticleMass = _tempPulsatingValues.massValue;
			_forceDirectedLayout.defaultSpringLength = _tempPulsatingValues.springLength;
			//_pulsatingTimerPull.stop();
			//_pulsatingTimerPush.stop();
		}
		
		/*
		private function pulsatePush(event:TimerEvent = null):void {
				_pulsatingTimerPush.stop();
				_forceDirectedLayout.defaultParticleMass = 25;
				_forceDirectedLayout.defaultSpringLength *= 3;
				_tempPulsatingValues.pull = true;
				_pulsatingTimerPull.start();
				return;		
		}
		
		private function pulsatePull(event:TimerEvent = null):void {
				_pulsatingTimerPull.stop();
				_forceDirectedLayout.defaultParticleMass = 1;
				_forceDirectedLayout.defaultSpringLength /= 3;
				_tempPulsatingValues.pull = false;
				_pulsatingTimerPush.start();
				return;			
		}
		*/
		private function pulsate(event:TimerEvent = null):void {
			if(_tempPulsatingValues.pull == true) {
				_forceDirectedLayout.defaultParticleMass = 1;
				_forceDirectedLayout.defaultSpringLength /= 2;
				_tempPulsatingValues.pull = false;
				return;
			}
			else {
				_forceDirectedLayout.defaultParticleMass = 25;
				_forceDirectedLayout.defaultSpringLength *= 2;
				_tempPulsatingValues.pull = true;
				return;
			}
			
		}
		
		/**
		 * Change the flag of the edge renderer to indicate available lineage information
		 * for an edge(fact) or not.
		 * */
		public function changeLineageIndication(event:ULineageIndicationEvent):void {
			(UEdge.DEFAULT_EDGE.edgeRenderer as UEdgeRenderer).indicateLineage = event.indicateLineage;
			for each (var edge:EdgeSprite in _vis.data.edges) {
				// cast the edge renderer into our own edge renderer
				//(edge.renderer as UEdgeRenderer).indicateLineage = event.indicateLineage;
				edge.dirty();
			}
		}
		
		public function applyFilterToSprites(event:UFilterEvent):Boolean {
				
				var node:NodeSprite;
			    var edge:EdgeSprite;
			    var ds:DataSprite
			    var tempDataHash:Object = new Object();
			    var label:String;
			    var counter:int;
			    var key:String;
				
				switch(event.typeOfFilter) {
				
				   case UFilterEvent.CONFIDENCE_FILTER_EVENT: {				   
				          	//if(parseFloat(edge.data.confidence) <= parseFloat(event.confidence)) {
				          	if(event.multipleConfidences) {
				          		_vis.data.edges.visit(
					                function(edge:EdgeSprite):void {						          		
						          		if(event.minConfidence <= parseFloat(edge.data.confidence)
						          		   && event.maxConfidence >= parseFloat(edge.data.confidence)) {	
							          	   tempDataHash[edge.data.factID] = edge;
							          	   if(!tempDataHash.hasOwnProperty(edge.source.data.tooltip)) {
									    	  tempDataHash[edge.source.data.tooltip] = edge.source;
								           }
								           if(!tempDataHash.hasOwnProperty(edge.target.data.tooltip)) {
									    	  tempDataHash[edge.target.data.tooltip] = edge.target;
								           }
							          	}
					                }
					            );
				          	}
				          	else {
					          	_vis.data.edges.visit(
							         function(edge:EdgeSprite):void {
							          	if(parseFloat(edge.data.confidence) == parseFloat(event.confidence)) {	
							          	   tempDataHash[edge.data.factID] = edge;
							          	   if(!tempDataHash.hasOwnProperty(edge.source.data.tooltip)) {
									    	  tempDataHash[edge.source.data.tooltip] = edge.source;
								           }
								           if(!tempDataHash.hasOwnProperty(edge.target.data.tooltip)) {
									    	  tempDataHash[edge.target.data.tooltip] = edge.target;
								           }
							          	}
					                }
				          	   );
					       }
					   
					    counter = 0;
						for (key in tempDataHash) {
							counter++;
						}
						
						// we need at least one one edge and two connected nodes
						if(counter < 1)
						  return false;
					   
					   // adjust the distance filter
					   _numOfHops = 0;
	        	       _graphDistanceFilter.distance = _numOfHops;
					   break;   
				   }
				   
				   case UFilterEvent.PREDICATE_FILTER_EVENT: {				   
				      	if(event.multiplePredicates) {
					      	_vis.data.edges.visit(
						      function(edge:EdgeSprite):void {
						          	if(event.predicates.contains(edge.data.label as String)) {
						          	   tempDataHash[edge.data.factID] = edge;
						          	   if(!tempDataHash.hasOwnProperty(edge.source.data.tooltip)) {
								    	  tempDataHash[edge.source.data.tooltip] = edge.source;
							           }
							           if(!tempDataHash.hasOwnProperty(edge.target.data.tooltip)) {
								    	  tempDataHash[edge.target.data.tooltip] = edge.target;
							           }
						          	}
					          }
				            );
				        }
			          	else {
				          	_vis.data.edges.visit(
						      function(edge:EdgeSprite):void {
					          	if(edge.data.label == event.predicate) {
					          	   tempDataHash[edge.data.factID] = edge;
					          	   if(!tempDataHash.hasOwnProperty(edge.source.data.tooltip)) {
							    	  tempDataHash[edge.source.data.tooltip] = edge.source;
						           }
						           if(!tempDataHash.hasOwnProperty(edge.target.data.tooltip)) {
							    	  tempDataHash[edge.target.data.tooltip] = edge.target;
						           }
					          	}
						      }
					        );
			          	}
					   
					    counter = 0;
						for (key in tempDataHash) {
							counter++;
						}
						
						// we need at least one one edge and two connected nodes
						if(counter < 1)
						  return false;
					   
					   // adjust the distance filter
					   _numOfHops = 0;
	        	       _graphDistanceFilter.distance = _numOfHops;
					   break;   
				   }
				   
				   case UFilterEvent.TRUTH_VALUE_FILTER_EVENT: {
				      	if(event.multipleTruthValues) {
					      	_vis.data.edges.visit(
						        function(edge:EdgeSprite):void {
					          		//trace("is truth list null : " + (event.truthValues == null));
						          	if(event.truthValues.hasOwnProperty(_graphData.graphCache.determineTruthValue(edge.data.truthValue))) {
						          	   tempDataHash[edge.data.factID] = edge;
						          	   if(!tempDataHash.hasOwnProperty(edge.source.data.tooltip)) {
								    	  tempDataHash[edge.source.data.tooltip] = edge.source;
							           }
							           if(!tempDataHash.hasOwnProperty(edge.target.data.tooltip)) {
								    	  tempDataHash[edge.target.data.tooltip] = edge.target;
							           }
						          	}
						        }
						    );
				          	// delete the porperties of the object and the object itself
				          	for (var truthKey:String in event.truthValues) {
				          		delete event.truthValues[truthKey]; 
				          	}
				          	event.truthValues = null;
				       }
			           else {
				          	_vis.data.edges.visit(
						        function(edge:EdgeSprite):void {
						          	if(_graphData.graphCache.determineTruthValue(edge.data.truthValue) == event.truthValue) {
						          	   tempDataHash[edge.data.factID] = edge;
						          	   if(!tempDataHash.hasOwnProperty(edge.source.data.tooltip)) {
								    	  tempDataHash[edge.source.data.tooltip] = edge.source;
							           }
							           if(!tempDataHash.hasOwnProperty(edge.target.data.tooltip)) {
								    	  tempDataHash[edge.target.data.tooltip] = edge.target;
							           }
						          	}
						        }
						    );
			           }
					   
					    counter = 0;
						for (key in tempDataHash) {
							counter++;
						}
						
						// we need at least one one edge and two connected nodes
						if(counter < 1)
						  return false;
					   			   
					   // adjust the distance filter
					   _numOfHops = 0;
	        	       _graphDistanceFilter.distance = _numOfHops;
					   break;   
				   }
				   
				   case UFilterEvent.NODE_FILTER_EVENT: {				   
				      	_vis.data.nodes.visit(
					      function(node:NodeSprite):void {
				          	if(event.nodes.contains(node.data.tooltip)) {
					           if(!tempDataHash.hasOwnProperty(node.data.tooltip)) {
						    	  tempDataHash[node.data.tooltip] = node;
					           }
				          	}
					      }
					   );
					   
					    counter = 0;
						for (key in tempDataHash) {
							counter++;
						}
						
						// we need at least one one edge and two connected nodes
						if(counter < 1)
						  return false;
					   
					   // adjust the distance filter
					   _numOfHops = 1;
	        	       _graphDistanceFilter.distance = _numOfHops;
					   break;   
				   }
				   
				   default: break;
				
				}
				
				
				var sprites:Array = new Array();
				
				for each (ds in tempDataHash) {
					sprites.push(ds);
				}
				
				// add these data sprites as focus sprites
				_graphDistanceFilter.updateFocusSprites(sprites,tempDataHash);
                
                // now get the current focus sprite (nodes and edges) from the distance filter
				_focusNodes = sprites;
				
				if (_focusNodes == null)
				   return false;
				
				// reset the root node shape
				if(_rootNodeBeforeLineage != null) {
				   _rootNodeBeforeLineage.shape = UNode.DEFAULT_NODE.shape;
				   //if(rootNodeBeforeLineage.props.hasOwnProperty("label"))
				     //  (_rootNodeBeforeLineage.props.label as TextSprite).verticalAnchor = TextSprite.TOP;
				}
				
				_rootNodeBeforeLineage = requestHighestDegreeNode(_focusNodes);
				
				_vis.data.root = _rootNodeBeforeLineage;
                
                var tree:Tree = _vis.data.tree; 
				
				// set the shape for the new root node
				//if(_rootNodeBeforeLineage != null) {
				  // _rootNodeBeforeLineage.shape = UShapes.DIAMOND;
				   //(_rootNodeBeforeLineage.props.label as TextSprite).verticalAnchor = TextSprite.MIDDLE;
				//}
				
				event.stopImmediatePropagation();
				
				setDataSpriteProperties();
				
				_vis.update(2).play();
				
				return true;
			     	
		}
		
		public function updateGraphOverView(event:UGraphOverviewEvent):BitmapData {
			//return flare.util.Displays.thumbnail(_vis.marks,event.overviewBitmap.width,event.overviewBitmap.height,event.overviewBitmap);
			return flare.util.Displays.thumbnail(_vis,event.overviewBitmap.width,event.overviewBitmap.height,event.overviewBitmap);
		}
		
		public function showCompleteLineageTree(event:UShowCompleteLineageTreeEvent):void {
			if(!_inLineageMode)
			   return;
				
			var node:NodeSprite;
				
		   	for each (node in _graphData.graphCache.lineageNodes) {
	   	  	  node.data.label = node.data[UStringConstants.LABEL_COPY];
	   	  	  node.expanded = true;
	   	  	  // set the new node label   
			  (node.props.label as TextSprite).text = node.data[UStringConstants.LABEL];
			  // adjust the node size
			  UFlareVis.drawNode(node);
		   	}
			
			_vis.update(2).play();
		}
		
		/* private function showAllLineageSprites(t:Transitioner):void {
			var node:NodeSprite;
			var edge:EdgeSprite;
			for each (node in _graphData.graphCache.lineageNodes) {
				t.$(node).visible = true;
			}
			for each (edge in _graphData.graphCache.lineageEdges) {
				t.$(edge).visible = true;
			}
		} */
		
		/*
		private function updateAsSequence(t:Transitioner = null):void {
			_updateSequence.dispose();
			_updateSequence.push(_vis.update,((t==null) ? 2 : t));
			_updateSequence.push(updateAlphaDistance, ((t==null) ? 0.5 : t));
			_updateSequence.play();
		}
		
		private function updateAlphaDistance(t:Transitioner = null):void {
			if(t==null)
			   t = new Transitioner(0.5);
			if(UFlareVis.alphaDistanceFilterActivated) {
				 _vis.data.nodes.visit(
				      function(ns:NodeSprite):void {
				      	 if((!ns.data.hasOwnProperty("alpha")) || (ns.data.hasOwnProperty("alpha") && ns.data.alpha == true)) {
				            //ns.fillColor = UNode.DEFAULT_NODE.fillColor;
				            //ns.fillAlpha = UNode.DEFAULT_NODE.alpha;
				            t.$(ns).fillColor = UNode.DEFAULT_NODE.fillColor;
				            t.$(ns).fillAlpha = UNode.DEFAULT_NODE.alpha;
				         }
				         else {
				         	//ns.fillColor = UNode.DEFAULT_NODE.fillColor;
				            //ns.fillAlpha = UFlareVis._distanceAlphaValue;
				            t.$(ns).fillColor = UNode.DEFAULT_NODE.fillColor;
				            t.$(ns).fillAlpha = UFlareVis._distanceAlphaValue;
				         }
				         //ns.dirty();
				         ns.dirty();
				      }
				  );
				  _vis.data.edges.visit(
					  function(es:EdgeSprite):void {
				         //es.lineColor = UNode.DEFAULT_NODE.lineColor;
				         UEdge.changeEdgeAlpha(es,t,UEdge.DEFAULT_EDGE);
				         /*
				         if((!es.data.hasOwnProperty("alpha")) || (es.data.hasOwnProperty("alpha") && es.data.alpha == true)) {
				            //ns.fillColor = UNode.DEFAULT_NODE.fillColor;
				            //ns.fillAlpha = UNode.DEFAULT_NODE.alpha;
				            //t.$(es).lineAlpha = UNode.DEFAULT_NODE.fillColor;
				            UEdge.changeEdgeAlpha(es,t,UEdge.DEFAULT_EDGE);
				            //t.$(ns).fillAlpha = UNode.DEFAULT_NODE.alpha;
				         }
				         else {
				         	//ns.fillColor = UNode.DEFAULT_NODE.fillColor;
				            //ns.fillAlpha = UFlareVis._distanceAlphaValue;
				            t.$(ns).fillColor = UNode.DEFAULT_NODE.fillColor;
				            t.$(ns).fillAlpha = UFlareVis._distanceAlphaValue;
				         }
				         
				         //UEdge.changeEdgeColor(es,UEdge.DEFAULT_EDGE);
				         es.dirty();
					  }
				  );
		   }
		}
		*/
		
		/** Clears the internally used data structures. */
		public function clear():void {	
			
			// clear all the visualization data
			if(_vis != null && _vis.data != null)
			   _vis.data.clear();
			
			// clear the also stored additional graph data
			_graphData.clear();
						
			// if in lineage mode -> leave the lineage mode			
			if(_inLineageMode) {
				leaveLineageMode(null);
			   //_inLineageMode = false;	
			}
			else {
				this.clearControls();
				_vis.operators.clear();
			}
			
			// reset the root node and the focus nodes
			_rootNodeBeforeLineage = null;
		    _focusNodes = null;
			
		}
		 		
	}
}