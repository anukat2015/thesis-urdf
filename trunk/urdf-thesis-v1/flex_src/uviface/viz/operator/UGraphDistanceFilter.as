package uviface.viz.operator
{
	import flare.animate.Transitioner;
	import flare.vis.data.DataSprite;
	import flare.vis.data.EdgeSprite;
	import flare.vis.data.NodeSprite;
	import flare.vis.operator.Operator;
	
	import flash.utils.Dictionary;
	
	import mx.core.Application;
	
	import uviface.viz.UFlareVis;
	import uviface.viz.display.*;
	import uviface.viz.util.UStringConstants;

	/**
	 * Filter operator that sets visible all items within a specified graph
	 * distance from a set of focus nodes.
	 */
	public class UGraphDistanceFilter extends Operator
	{
		/** A string to indicate the result cluster to check for the edges. */
		private var _resultClusterID:String = null;
		/** An array of focal NodeSprites. */
		private var _focusNodes:/*NodeSprite*/Array;
		/** Graph distance within which which items wll be visible. */
		private var _distance:int;
		/** Flag indicating which graph links to traverse. */
		public var links:int;
		
		private var _depths:Dictionary = new Dictionary();
		
		// added by Timm Meiser, on 30.07.10
		private var _focusNodesHash:Object = new Object();
		
		//private var _alphaValue:Number = 0.5;
		
		/**
		 * Creates a new GraphDistanceFilter.
		 * @param focusNodes an array of focal NodeSprites. Graph distance is
		 *  measured as the minimum number of edge-hops to one of these nodes.
		 * @param distance graph distance within which items will be visible
		 * @param links flag indicating which graph links to traverse. The
		 *  default value is <code>NodeSprite.GRAPH_LINKS</code>.
		 */		
		public function UGraphDistanceFilter(focusNodes:Array=null,
			distance:int=1, links:int=3/*NodeSprite.GRAPH_LINKS*/)
		{
			_focusNodes = focusNodes;
			if(_focusNodes == null)
			   _focusNodes = new Array();
			_distance = distance;
			this.links = links;
		}
		
		/** @inheritDoc */
		public override function operate(t:Transitioner=null):void
		{
			// added by Timm Meiser
			if(!this.enabled)
			   return;
			 
			t = (t==null ? Transitioner.DEFAULT : t); 
			var q:Array = [], _depths:Dictionary = new Dictionary(); 
			
			var ds:DataSprite;
			var ns:NodeSprite;
			var es:EdgeSprite;
			
			// if we have a distance smaller than 0 -> set all sprites visible  
			// added by Timm Meiser, on 30.07.10
			// ----------------------------------------------------------
			// ----------------------------------------------------------
			// we have a distance of -1 -> this means everything is shown
			// ----------------------------------------------------------
			// ----------------------------------------------------------
			if(_distance == -1) {
				
		       	  visualization.data.nodes.visit(
				      function(n:NodeSprite):void {
				         //t.$(ns).visible = true;
				         _depths[n] = -1;
				      }
				  );
				  visualization.data.edges.visit(
					  function(es:EdgeSprite):void {
					     //t.$(es).visible = true;
					     if (!es.data.hasOwnProperty("SuperEdge")) {
								_depths[es] = -1;
						 }
					  }
				  );	
				  
				  //return;			
			}
			   
			// added by Timm Meiser, on 30.07.10
			// --------------------------------
			// --------------------------------
			// we have a distance of 0
			// --------------------------------
			// --------------------------------
			/*
			else if(_distance == 0) {
				
				if(_focusNodes.length > 1) {
				
					for each (ns in _focusNodes) {
					   for(var pos:int = 0; pos<ns.outDegree; pos++) {
					       targetNode = ns.getOutNode(pos);
					       // this is one of the focus nodes
					       if(_focusNodesHash.hasOwnProperty(targetNode.data.label)) {
					          edge = ns.getOutEdge(pos);
					          if(edge.data.hasOwnProperty(_resultClusterID)) {
					             _depths[targetNode] = -1;
					             _depths[edge] = -1;      
					          }
					          //edge = ns.getOutEdge(pos);
					          //if(edge.data.hasOwnProperty(_resultClusterID))
					            // _depths[edge] = -1;
					       }
					   }
					   
					   for(pos = 0; pos<ns.inDegree; pos++) {
					       targetNode = ns.getInNode(pos);
					       // this is one of the focus nodes
					       if(_focusNodesHash.hasOwnProperty(targetNode.data.label)) {
					          edge = ns.getInEdge(pos);
					          if(edge.data.hasOwnProperty(_resultClusterID)) {
					             _depths[targetNode] = -1;
					             _depths[edge] = -1;      
					          }    
					          //edge = ns.getInEdge(pos);
					          //if(edge.data.hasOwnProperty(_resultClusterID))
					            // _depths[edge] = -1;
					       }
					   }
					}
				}	
				else {
					targetNode = _focusNodes[0];
					_depths[targetNode] = -1;
				}
				
			} 
			*/
			// --------------------------------
			// --------------------------------
			// we have a distance bigger than 0
			// --------------------------------
			// --------------------------------
			else if(_distance > 0) {
			  
				//t = (t==null ? Transitioner.DEFAULT : t);
		        
		        // initialize breadth-first traversal
		        //var q:Array = [], _depths:Dictionary = new Dictionary();
				
				// --------------------------------------------------------
				// we have a distance bigger than 0 and several focus nodes
				// --------------------------------------------------------
				
				if(_focusNodes.length > 1) {
					
					for each (ds in _focusNodes) {				
						
			            if(ds is NodeSprite) {
			               ns = ds as NodeSprite;
			               // add the depts value to the data sprite
						   _depths[ns] = 0;
						   
						   ns.visitEdges(function(e:EdgeSprite):void {
							
								// if we see a super edge or the edge is already in the focus has, abort
								if(!e.data.hasOwnProperty("SuperEdge")) {
								  if(_focusNodesHash.hasOwnProperty(e.source.data.tooltip) 
								      && _focusNodesHash.hasOwnProperty(e.target.data.tooltip)) {
										if(_focusNodesHash.hasOwnProperty(e.data.factID) || (e.data.hasOwnProperty(UStringConstants.REQUEST_EDGE))) { 
									         //_depths[e] = 1;
									         //q.push(e);
									         _depths[e] = 0;
										}
								  }
								  else {
								   	   _depths[e] = 1;
								       q.push(e);
								  }
								
						        }
							
						   }, links);
			            }
			
					}
					
				}
				
				// --------------------------------------------------------
				// we have a distance bigger than 0 and only one focus node
				// --------------------------------------------------------
				else {
					
					ns = _focusNodes[0] as NodeSprite				
					_depths[ns] = 0;
					
					ns.visitEdges(function(e:EdgeSprite):void {
						
						// if we see a super edge, the cluster ends, abort
						if(!e.data.hasOwnProperty("SuperEdge")) {
						   _depths[e] = 1;
						   q.push(e);
						}
						
					}, links);
					
				}
				
				// perform breadth-first traversal
				var xe:EdgeSprite, xn:NodeSprite, d:int;
				while (q.length > 0) {
					xe = q.shift(); d = _depths[xe];
					// -- fix to bug 1924891 by goosebumps4all
					if (_depths[xe.source] == undefined) {
						xn = xe.source;
					} else if (_depths[xe.target] == undefined) {
						xn = xe.target;
					} else {
						continue;
					}
					
					/*
					if(_focusNodesHash.hasOwnProperty(xn.data.tooltip)) { 
						if(_focusNodesHash.hasOwnProperty(xe.data.factID)) {
							_depths[xn] = d;
					    }
					}
					*/
					//else {
					  // _depths[xn] = d;
				    //}
					// -- end fix
					
					
					
					_depths[xn] = d;
					/*
					if (d == distance) {
						xn.visitEdges(function(e:EdgeSprite):void {	
							if (_depths[e.target]==d && _depths[e.source]==d) {
								_depths[e] = d+1;
							}
						}, links);
					} else {
						xn.visitEdges(function(e:EdgeSprite):void {
							if (_depths[e] == undefined) {
								_depths[e] = d+1;
								q.push(e);
							}
						}, links);
					}
					*/
					if (d == _distance) {
						/*
						xn.visitEdges(function(e:EdgeSprite):void {	
							if (!e.data.hasOwnProperty("SuperEdge") && _depths[e.target]==d && _depths[e.source]==d) {
								_depths[e] = d+1;
							}
						}, links);
						*/
						continue;
					} else {
						xn.visitEdges(function(e:EdgeSprite):void {
							if (!e.data.hasOwnProperty("SuperEdge") && _depths[e] == undefined) {
								_depths[e] = d+1;
								q.push(e);
							}
						}, links);
					}
				}
			
			}
			
			// --------------------------------------------------------------------------------
			// now compute all the visisbility and alpha values, based on the traversal results
			// --------------------------------------------------------------------------------
	        	
        	var visible:Boolean;
        	var hasFullAlpha:Boolean;
	        var alpha:Number;
	        var obj:Object;
	       // var ns:NodeSprite;
        	
        	// distance is smaller than 0 -> we want to show every node and edge in the graph
        	if(_distance < 0) {
        	
        	    // now set visibility based on traversal results
                visualization.data.visit(function(ds:DataSprite):void {
        	
	        		visible = (_depths[ds] == -1);    		   
		        	alpha = visible ? 1 : 0;
					obj = t.$(ds);
					
					obj.alpha = alpha;
					if (ds is NodeSprite) {
						ns = ds as NodeSprite;
						ns.expanded = visible;//(visible && _depths[ds] < _distance);
						ns.mouseEnabled = true;
					}
					if (t.immediate) {
						ds.visible = visible;
						ds.mouseEnabled = true;
						if(ds is EdgeSprite)
						   UEdge.changeEdgeAlpha(ds as EdgeSprite);//ds.alpha = UMath.roundNumber(parseFloat(ds.data.confidence),2);
					} else {
						obj.visible = visible;
						obj.mouseEnabled = true;
						if(ds is EdgeSprite)
						   UEdge.changeEdgeAlphaOfObject(obj, ds as EdgeSprite);//obj.alpha = UMath.roundNumber(parseFloat(ds.data.confidence),2);
					}
					
					//if(UFlareVis.alphaDistanceFilterActivated)
					  // ds.data.alpha = visible;
				
                });                             
                
        	}
        	else
        	  // distance is 0 -> we want to show only the focus nodes and edges
        	  if(distance == 0) {
        	    
        	    var label:String;
	    	      
			    	   // now set visibility based on traversal results
                visualization.data.visit(function(ds:DataSprite):void {
		    	   
                   if(ds is NodeSprite)
                      label = ds.data.tooltip;
                   else {
                      if(ds.data.hasOwnProperty(UStringConstants.REQUEST_EDGE))
                         label = ds.data.label;
                      else
                         label = ds.data.factID;
                   }
			    	
			       //visible = (_focusNodesHash.hasOwnProperty(label) && !ds.data.hasOwnProperty(UStringConstants.REQUEST_EDGE)
		    	   //          && !ds.data.hasOwnProperty(UStringConstants.REQUEST_NODE)) ? true : false; 
		    	   visible = _focusNodesHash.hasOwnProperty(label) ? true : false;    		   
		           alpha = visible ? 1 : 0;
				   obj = t.$(ds);
					
				   obj.alpha = alpha;
				   if (ds is NodeSprite) {
						ns = ds as NodeSprite;
						ns.expanded = visible;//(visible && _depths[ds] < _distance);
						ns.mouseEnabled = true;
				   }
				   if (t.immediate) {
						ds.visible = visible;
						ds.mouseEnabled = true;
						if(ds is EdgeSprite)
						   UEdge.changeEdgeAlpha(ds as EdgeSprite);//ds.alpha = UMath.roundNumber(parseFloat(ds.data.confidence),2);
				   } else {
						obj.visible = visible;
						obj.mouseEnabled = true;
						if(ds is EdgeSprite)
						   UEdge.changeEdgeAlphaOfObject(obj, ds as EdgeSprite);//obj.alpha = UMath.roundNumber(parseFloat(ds.data.confidence),2);
				   }
				   
				   
				   //if(UFlareVis.alphaDistanceFilterActivated)
				     //  ds.data.alpha = visible;
			  });
		       
	        }
        	else {
        	
        	     // now set visibility based on traversal results
                 visualization.data.visit(function(ds:DataSprite):void {
        	
		        	visible = (_depths[ds] != undefined && _depths[ds] <= _distance);
		        	alpha = visible ? 1 : 0;
					obj = t.$(ds);
					
					obj.alpha = alpha;
					if (ds is NodeSprite) {
					    ns = ds as NodeSprite;
						ns.expanded = (visible && _depths[ds] <= _distance);
						ns.mouseEnabled = true;
						//trace("ns.data.tooltip : " + ns.data.tooltip);
						//trace("visible : " + visible);
					}
					if (t.immediate) {
						ds.visible = visible;
						ds.mouseEnabled = true;
						if(ds is EdgeSprite)
						   UEdge.changeEdgeAlpha(ds as EdgeSprite);//ds.alpha = UMath.roundNumber(parseFloat(ds.data.confidence),2);
					} else {
						obj.visible = visible;
						obj.mouseEnabled = true;
						if(ds is EdgeSprite)
						   UEdge.changeEdgeAlphaOfObject(obj, ds as EdgeSprite);//obj.alpha = UMath.roundNumber(parseFloat(ds.data.confidence),2);
					}
				
				    //if(UFlareVis.alphaDistanceFilterActivated)
					  // ds.data.alpha = visible;
				
                 });
        	   
        	   
        	}
	        
	        // delete all the depths entries of the depths hash
	        var key:String;
	        
	        for (key in _depths) {
	           _depths[key] = null;
	           delete _depths[key];
	        }
	        
	        // should fix some bugs
	        // added on 25.10.10
	        _depths = new Dictionary();
		}
		
		// added by Timm Meiser, on 30.07.10
		public function set distance(distance:int):void {
		    
		    _distance = distance;
		    
		    /*
		    if(_distance == 0) {
		    	// first, clear the focus node hash
		    	clearFocusNodeHash();
		    	
		    	var focusNode:NodeSprite;
		    
		        for each (focusNode in _focusNodes) {
		    	   _focusNodesHash[focusNode.data.label] = focusNode;
		        }
		    }		    
		    */
		}
		
		// added by Timm Meiser, on 30.07.10
		public function get distance():int {
		    return _distance;
		}
		
		// added by Timm Meiser, on 30.07.10
		public function set focusNodes(focusNodes:Array):void {
		    
		    _focusNodes = focusNodes;
		    
		    //if(_distance == 0) {
		    	// first, clear the focus node hash
	    	clearFocusNodeHash();
	    	
	    	//var focusNode:NodeSprite;
	        /*
	        for each (focusNode in _focusNodes) {
	    	   _focusNodesHash[focusNode.data.label] = focusNode;
	        }
		    */   
		    
		    var ds:DataSprite;
		    var es:EdgeSprite;
		    
		    for each (ds in _focusNodes) {
	    	   if(ds is NodeSprite) {
	    	      _focusNodesHash[ds.data.tooltip] = ds as NodeSprite;
	    	      (ds as NodeSprite).shape = UShapes.COMPLEX_ROUNDED_RECT;
	    	      // indicate that the focus nodes have changed
	    	      mx.core.Application.application.showFocusNodesChange();
	    	      //(ds as NodeSprite).renderer = UNodeRenderer.instance;
	    	   }
	    	   else {
	    	   	  es = ds as EdgeSprite;
	    	   	  if(es.data.hasOwnProperty(UStringConstants.REQUEST_EDGE))
	    	   	     _focusNodesHash[es.data.label] = es;
	    	   	  else
	    	         _focusNodesHash[es.data.factID] = es;
	    	   }
	        }
	        
	        /*
	        for each (ds in _focusNodesHash) {
	        	if(ds is NodeSprite)
	        	   trace("ds.data.tooltip : " + ds.data.tooltip);
	        	else
	        	   trace("ds.data.factID : " + ds.data.factID);
	        }
		    */    
		    //}		    
		    
		}
		
		public function changeFocusSpriteShapes():void {
			var ds:DataSprite;
			if(_focusNodes == null)
			   return;
			   
			for each (ds in _focusNodes) {
	    	   if(ds is NodeSprite) {
	    	      //_focusNodesHash[ds.data.tooltip] = ds as NodeSprite;
	    	      (ds as NodeSprite).shape = UShapes.COMPLEX_ROUNDED_RECT;
	    	      // indicate that the focus nodes have changed
	    	      mx.core.Application.application.showFocusNodesChange();
	    	      //(ds as NodeSprite).renderer = UNodeRenderer.instance;
	    	   }
	        }
		}
		
		// added by Timm Meiser, on 30.07.10
		public function get focusNodes():Array {
		    return _focusNodes;
		}
		
		public function updateFocusSprites(focusSprites:Array,focusSpriteHash:Object):void {
			_focusNodes = focusSprites;
			clearFocusNodeHash();
			for each (var ds:DataSprite in _focusNodes) {
	    	   if(ds is NodeSprite) {
	    	      (ds as NodeSprite).shape = UShapes.COMPLEX_ROUNDED_RECT;
	    	      // indicate that the focus nodes have changed
	    	      mx.core.Application.application.showFocusNodesChange();
	    	      //(ds as NodeSprite).renderer = UNodeRenderer.instance;
	    	   }
	        }
			_focusNodesHash = focusSpriteHash;
		}
		
		private function clearFocusNodeHash():void {
			var sprite:DataSprite;
			for each (sprite in _focusNodesHash) {
				if(sprite is NodeSprite)
			        (sprite as NodeSprite).shape = UNode.DEFAULT_NODE.shape;
			        //(sprite as NodeSprite).renderer = UNodeRenderer.instance;
			}
			_focusNodesHash = new Object();
		}
		
		/*
		public function set focusNodesHash(focusNodesHash:Object):void {
		   _focusNodesHash = focusNodesHash;
		   
		   var ds:DataSprite;
		   
		   // clear the current focus nodes
		   for (var pos:int = 0; pos < _focusNodes.length; pos++) {
		   	  _focusNodes[pos] = null;
		   }
		   while(_focusNodes.length > 0) {
		   	  _focusNodes.pop();
		   }
		   
		   for each (ds in _focusNodesHash){
		   	  _focusNodes.push(ds);
		   }
		   
		}
		*/
		// added by Timm Meiser, on 05.08.10
		public function set resultClusterID(resultClusterID:String):void {
		    _resultClusterID = resultClusterID;	    	    
		}
		
		// added by Timm Meiser, on 05.08.10
		public function get resultClusterID():String {
		    return _resultClusterID;
		}
		
		/*
		public function get alphaValue():Number {
			return _alphaValue;
		}
		
		public function set alphaValue(alphaValue:Number):void {
			_alphaValue = alphaValue;
		}
		*/
		
	} // end of class GraphDistanceFilter
}