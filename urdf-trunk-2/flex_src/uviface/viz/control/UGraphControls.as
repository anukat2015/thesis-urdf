package uviface.viz.control
{
	import flare.display.TextSprite;
	import flare.vis.Visualization;
	import flare.vis.controls.ClickControl;
	import flare.vis.controls.DragControl;
	import flare.vis.controls.HoverControl;
	import flare.vis.data.DataSprite;
	import flare.vis.data.EdgeSprite;
	import flare.vis.data.NodeSprite;
	import flare.vis.events.SelectionEvent;
	import flare.vis.events.TooltipEvent;
	
	import flash.events.MouseEvent;
	import flash.filters.*;
	
	import urdf.api.ULiteral;
	import urdf.api.UQuery;
	import urdf.api.URelation;
	
	import uviface.event.*;
	import uviface.viz.UFlareVis;
	import uviface.viz.display.UEdge;
	import uviface.viz.util.UColors;
	import uviface.viz.util.UGraphUtil;
	import uviface.viz.util.UStringConstants;
	
	public class UGraphControls
	{
		
		private static var _lineColor:int = 0;
		private static var _fillColor:int = 0;
		private static var _lineWidth:Number = 0;
		private static var _tooltip:String;
		private static var _htmlText:String;
		
		//private static var _nodeToolTip:UNodeToolTip = new UNodeToolTip();
		//private static var _edgeToolTip:UEdgeToolTip = new UEdgeToolTip();
		//private static var _lineageToolTip:UStringToolTip = new UStringToolTip();
		
		public function UGraphControls()
		{
			//_lineageToolTip.title = "Lineage Tooltip";
			//_lineageToolTip.ToolTipText = "...";
		}
		
		public static function nodeDragControl():DragControl {
			return new DragControl(NodeSprite);
			//return new UDragClickControl(NodeSprite);
		}
		
		public static function nodeExpandControl(vis:Visualization):UExpandControl {
			return new UExpandControl(NodeSprite,
				function():void { 
				   
				   if(!UFlareVis.lineageMode)
				      return;
				      
				   vis.update(1, "nodes","main").play(); 
				}
		     );
		}
		
		public static function nodeHoverColorSelect():HoverControl {
			return new HoverControl(NodeSprite,
				
				// by default, move highlighted items to front
				//HoverControl.MOVE_AND_RETURN, 
				HoverControl.MOVE_TO_FRONT, 
				// highlight node border on mouse over
				function(e:SelectionEvent):void {
					
					//if(UFlareVis.lineageMode == true && !e.node.data.hasOwnProperty("linTree"))
					  // return;
					
					// temporarily store the nodes fill color
					_fillColor = e.node.fillColor;
					e.node.fillColor = UColors.blue(7);
					
					e.node.scaleX *= 2;
					e.node.scaleY *= 2;
					
					var node:NodeSprite;
					var edge:EdgeSprite;
					var i:int = 0
					
					/*
					if(UFlareVis.lineageMode) {
						for (i; i<e.node.childDegree; i++) {
							edge = e.node.getChildEdge(i);
							node = edge.target;//e.node.getInNode(i);
							edge.lineColor = UColors.magenta(7);
							node.fillColor = UColors.magenta(7);
					    }
					}
					*/
					if(!UFlareVis.lineageMode) {
						
						// omit context nodes
						if(e.node.data.hasOwnProperty("context") && e.node.data.contex)
						   return;
						
						for (i; i<e.node.inDegree; i++) {
							edge = e.node.getInEdge(i);
							node = edge.source;//e.node.getInNode(i);
							edge.lineColor = UColors.blue(7);
							node.fillColor = UColors.blue(7);
						}
						
						// reset i
						i=0;
						
						for (i; i<e.node.outDegree; i++) {
							edge = e.node.getOutEdge(i);
							node = edge.target;//e.node.getInNode(i);
							//node = e.node.getOutNode(i);
							edge.lineColor = UColors.blue(7);
							node.fillColor = UColors.blue(7);
						}
					
					}
					else 
					  if(e.node.data.hasOwnProperty("factID"))
					     UGraphUtil.highlightSameFactNodes(e.node,UColors.blue(7));
					
				},
				// remove highlight on mouse out
				function(e:SelectionEvent):void {
					
					//if(UFlareVis.lineageMode == true && !e.node.data.hasOwnProperty("linTree"))
					  // return;
					
					// reset the original node color again
					e.node.fillColor = _fillColor;
					
					e.node.scaleX *= 0.5;
					e.node.scaleY *= 0.5;
					
					var node:NodeSprite;
					var edge:EdgeSprite;
					var i:int = 0
					
					/*
					if(UFlareVis.lineageMode) {
						for (i; i<e.node.childDegree; i++) {
							edge = e.node.getChildEdge(i);
							node = edge.target;//e.node.getInNode(i);
							edge.lineColor = UColors.yellow(7);
							node.fillColor = UColors.magenta(7);
					    }
					}
					*/	
					if(!UFlareVis.lineageMode) {
						
						// omit context nodes
						if(e.node.data.hasOwnProperty("context") && e.node.data.contex)
						   return;
						
						for (i; i<e.node.inDegree; i++) {
							edge = e.node.getInEdge(i);
							node = edge.source;//e.node.getInNode(i);
							/*
							if(edge.data.hasOwnProperty(UStringConstants.TRUTH_VALUE)) {
								switch(edge.data.truthValue) {
									case 0: edge.lineColor = UNode.DEFAULT_NODE.falseFactColor;break;
									case 1: edge.lineColor = UColors.green(7);break;
									case 2: edge.lineColor = UNode.DEFAULT_NODE.trueFactColor;break;
									default: edge.lineColor = UNode.DEFAULT_NODE.falseFactColor;break;
								}
							}
							*/
						    if(edge.data.hasOwnProperty("linTree"))
							   	edge.lineColor = UColors.yellow(7);
							else
							    UEdge.changeEdgeColor(edge,UEdge.DEFAULT_EDGE);
			
							node.fillColor = _fillColor;
							// mark the edge as dirty
							edge.dirty();
						}
						
						// reset i
						i=0;
						
						for (i; i<e.node.outDegree; i++) {
							edge = e.node.getOutEdge(i);
							node = edge.target;//e.node.getInNode(i);
							/*
							if(edge.data.hasOwnProperty(UStringConstants.TRUTH_VALUE)) {
								switch(edge.data.truthValue) {
									case 0: edge.lineColor = UNode.DEFAULT_NODE.falseFactColor;break;
									case 1: edge.lineColor = UColors.green(7);break;
									case 2: edge.lineColor = UNode.DEFAULT_NODE.trueFactColor;break;
									default: edge.lineColor = UNode.DEFAULT_NODE.falseFactColor;break;
								}
							}*/
							if(edge.data.hasOwnProperty("linTree"))
							   	edge.lineColor = UColors.yellow(7);
							else
							    UEdge.changeEdgeColor(edge,UEdge.DEFAULT_EDGE);
		
							node.fillColor = _fillColor;
							edge.dirty();
						}
					
					}
					
					// we are in lineage (explanation mode)
					else {
						
						for (i; i<e.node.inDegree; i++) {
							edge = e.node.getInEdge(i);
							// mark the edge as dirty
							edge.dirty();
						}
						
						// reset i
						i=0;
						
						for (i; i<e.node.outDegree; i++) {
							edge = e.node.getOutEdge(i);
							edge.dirty();
						}
					 
					    if(e.node.data.hasOwnProperty("factID"))
					       UGraphUtil.resetSameFactNodes(e.node);
					
					}
					
					// repaint all the edges
					//e.node.visitEdges(
				      // function(e:EdgeSprite):void {
				        //  e.dirty();
				       //}
					//);
			});
		}
		
		public static function edgeHoverColorSelect():HoverControl {
			//return new HoverControl(EdgeSprite,
			return new HoverControl(EdgeSprite,
				
				// by default, move highlighted items to front
				//HoverControl.MOVE_AND_RETURN, 
				HoverControl.DONT_MOVE, 
				// highlight node border on mouse over
				function(e:SelectionEvent):void {
					
					//if(UFlareVis.lineageMode == true && !e.edge.data.hasOwnProperty("linTree"))
					  // return;
					
					//if(e.object is EdgeSprite) {
						//trace("Chocolate");
						// temporarily store the line width and color of the edge
						_lineWidth = e.edge.lineWidth;
						_lineColor = e.edge.lineColor;
						e.edge.lineWidth = 10;
						e.edge.lineColor = UColors.blue(7);	
						if(!UFlareVis.lineageMode) {
						   _fillColor = e.edge.source.fillColor;
						   e.edge.source.fillColor = UColors.blue(7);
						   e.edge.target.fillColor = UColors.blue(7);
						}
					//}
					/*
					else if(e.object is TextSprite) {
						trace("Superdupa");
						// temporarily store the line width and color of the edge
						var edge:EdgeSprite = (e.object as TextSprite).parent as EdgeSprite;
						_lineWidth = edge.lineWidth;
						_lineColor = edge.lineColor;
						edge.lineWidth = 10;
						edge.lineColor = UColors.blue(7);	
						if(!UFlareVis.lineageMode) {
						   _fillColor = edge.source.fillColor;
						   edge.source.fillColor = UColors.blue(7);
						   edge.target.fillColor = UColors.blue(7);
						}
					}
					*/	
				},
				// remove highlight on mouse out
				function(e:SelectionEvent):void {
					
					//if(UFlareVis.lineageMode == true && !e.edge.data.hasOwnProperty("linTree"))
					  // return;
					
					//if(e.object is EdgeSprite) {
						e.edge.lineWidth = _lineWidth;
						//e.edge.lineColor = _lineColor;
						UEdge.changeEdgeColor(e.edge,UEdge.DEFAULT_EDGE);
						if(!UFlareVis.lineageMode) {
						   e.edge.source.fillColor = _fillColor;
						   e.edge.target.fillColor = _fillColor;
						}
						else {
							//e.edge.lineColor = UColors.yellow(6);
							e.edge.lineColor = _lineColor;//UColors.BLACK;
							if(e.edge.data.hasOwnProperty("linTree"))
							   e.edge.lineAlpha = 1.0;
							else
							    e.edge.lineAlpha = 0.5;
						}
					//}
					/*
					else if(e.object is TextSprite) {
						var edge2:EdgeSprite = (e.object as TextSprite).parent as EdgeSprite;
						edge2.lineWidth = _lineWidth;
						//e.edge.lineColor = _lineColor;
						UEdge.changeEdgeColor(edge2,UEdge.DEFAULT_EDGE);
						if(!UFlareVis.lineageMode) {
						   edge2.source.fillColor = _fillColor;
						   edge2.target.fillColor = _fillColor;
						}
						else {
							edge2.lineColor = UColors.yellow(6);
							if(edge2.data.hasOwnProperty("linTree"))
							   edge2.lineAlpha = 1.0;
							else
							    edge2.lineAlpha = 0.5;
						}
					}
					*/
					   
			});
		}
		
		public static function hoverOverEdgeLabel(event:MouseEvent):void {
			//trace("Juhuuuuu");
			var edge:EdgeSprite = (event.currentTarget is TextSprite) ? ((event.currentTarget as TextSprite).parent as EdgeSprite) : null;
			if(edge == null)
			   return;
			/*
			_lineWidth = edge.lineWidth;
			_lineColor = edge.lineColor;
			edge.lineWidth = 10;
			edge.lineColor = UColors.blue(7);	
			if(!UFlareVis.lineageMode) {
			   _fillColor = edge.source.fillColor;
			   edge.source.fillColor = UColors.blue(7);
			   edge.target.fillColor = UColors.blue(7);
			}
			*/
			var mouseEvent:MouseEvent = new MouseEvent(MouseEvent.MOUSE_OVER,true);
			mouseEvent.relatedObject = edge;
			edge.dispatchEvent(mouseEvent);
		}
		
		public static function hoverOutEdgeLabel(event:MouseEvent):void {
			//trace("Juhuuuuu222222");
			var edge:EdgeSprite = (event.currentTarget is TextSprite) ? ((event.currentTarget as TextSprite).parent as EdgeSprite) : null;
			if(edge == null)
			   return;
			/*
			edge2.lineWidth = _lineWidth;
			//e.edge.lineColor = _lineColor;
			UEdge.changeEdgeColor(edge2,UEdge.DEFAULT_EDGE);
			if(!UFlareVis.lineageMode) {
			   edge2.source.fillColor = _fillColor;
			   edge2.target.fillColor = _fillColor;
			}
			else {
				edge2.lineColor = UColors.yellow(6);
				if(edge2.data.hasOwnProperty("linTree"))
				   edge2.lineAlpha = 1.0;
				else
				    edge2.lineAlpha = 0.5;
			}
			*/
			var mouseEvent:MouseEvent = new MouseEvent(MouseEvent.MOUSE_OUT,true);
			mouseEvent.relatedObject = edge;
			edge.dispatchEvent(mouseEvent);
		}	
		
		public static function clickEdgeLabel(event:MouseEvent):void {
			
			// we are already in the lineage mode
			if(UFlareVis.lineageMode)
			   return;
			   
			var edge:EdgeSprite = (event.currentTarget is TextSprite) ? ((event.currentTarget as TextSprite).parent as EdgeSprite) : null;
			if(edge == null)
			   return;
			
			// this dynamic label exists, nevertheless check
			if(!edge.data.hasOwnProperty(UStringConstants.FACT_ID))
			   return;
			if(edge.data.hasOwnProperty("lineageAvailable") && edge.data.lineageAvailable ) {
				// request the factId of the edge we want to click   
				var factID:String = edge.data.factID;
				
				// mark the current edge as selected -> we need this for the tooltip control
				//edge.data["selected"] = true;
				
				// the third parameter is set to true, because the event should bubble
			    // means, the UViz.mxml file receives the event, when the event comes back from the 
			    // event dispatcher, this ClickControl object
			    var lineageEvent:ULineageEvent = new ULineageEvent(ULineageEvent.PRODUCE_LINEAGE_GRAPH,factID,true); 
				
				edge.dispatchEvent(lineageEvent);					
			}
			else {
				//mx.core.Application.application.showNoLineageToolTip((edge.x1 + edge.x2)/2,(edge.y1 + edge.y2)/2);
				return;
			}
		}	 
		/*
		public static function dataToolTipLabelDisplay():TooltipControl {
			return new TooltipControl(DataSprite, null, updateToolTip, updateToolTip);
		}
		*/
		public static function dataToolTipLabelDisplay():UTooltipControl {
			return new UTooltipControl(DataSprite, null, updateToolTip, updateToolTip);
		}
		
		private static function updateToolTip(event:TooltipEvent):void {
			
			// cast the event into a TextSprite object
			//TextSprite(event.tooltip).text = (event.node != null)
			
			if(UFlareVis.lineageMode) {
				TextSprite(event.tooltip).htmlText = (event.node != null)
			                               ? event.node.data.tooltip //event.node.data.label
			                               : event.edge.data.tooltip;//event.edge.data.label;
			}
			else {
			    TextSprite(event.tooltip).htmlText = (event.node != null)
			                               ? (event.node.data.tooltip + "<br/><b> in-degree: </b>" 
			                                 + event.node.inDegree.toString() + "<br/><b> out-degree: </b>" 
			                                 + event.node.outDegree.toString())//event.node.data.tooltip //event.node.data.label
			                               : event.edge.data.tooltip;//event.edge.data.label;
			}
			/* _tooltip = (event.node != null)
                               ? event.node.data.tooltip //event.node.data.label
                               : event.edge.data.tooltip;//event.edge.data.label;
            _htmlText = "";
			
			if(event.node == null) {
				if(event.edge.data.hasOwnProperty("linTree")) {
					
				}
				else {
					_htmlText = "<b> predicate: </b>" + _tooltip.substr(0,_tooltip.indexOf("(")) + "<br/>";
					_htmlText += "<b> subject: </b>" + _tooltip.substr(_tooltip.indexOf("("),_tooltip.indexOf(",")) + "<br/>";
					_htmlText += "<b> object: </b>" + _tooltip.substr(_tooltip.indexOf(","),_tooltip.indexOf(")")) + "<br/>";
					_tooltip = _tooltip.substring(_tooltip.indexOf("["));
					_htmlText += "<b> truth value: </b>" + _tooltip.substr(0,_tooltip.indexOf("|")) + "<br/>";
					_htmlText += "<b> truth value: </b>" + _tooltip.substr(_tooltip.indexOf("|"),_tooltip.indexOf("]")) + "<br/>";
					_tooltip = _tooltip.substring(_tooltip.indexOf("]"));
					if(_tooltip.indexOf("lineage") > 0) {
						_tooltip = _tooltip = _tooltip.substring(_tooltip.indexOf(":")+1);
						_htmlText += (_tooltip.indexOf("-") > 0) ? ("<b> lineage available: </b>" + _tooltip.substr(0,_tooltip.indexOf("\n")) + "<br/>") 
						          : ("<b> lineage available: </b>" + _tooltip.substr(0,_tooltip.length-1) + "<br/>");
					}
					if(_tooltip.indexOf("lineage") > 0) {
						_tooltip = _tooltip = _tooltip.substring(_tooltip.indexOf(":")+1);
						_htmlText += (_tooltip.indexOf("-") > 0) ? ("<b> lineage available: </b>" + _tooltip.substr(0,_tooltip.indexOf("\n")) + "<br/>") 
						          : ("<b> lineage available: </b>" + _tooltip.substr(0,_tooltip.length-1) + "<br/>");
					}
				}
			} */
			                              
			/* TextSprite(e.tooltip).htmlText = "<b>" + e.node.data.ne + "</b><br/>"
				+ "Date: <b>" + monthToString[month-1] + " " + year + "</b><br/>" 
				+ "Month: <b>" + monthInx + "</b>: <b>"
				+ Math.round(e.node.data[monthInx]*10000)/100 + "%</b><br/>"
				+ "tagId: <i>" + e.node.data.tid + "</i>"; */
			                             
		    if(event.edge == null && event.node == null)
			    event.tooltip.visible = false;
		}
		
		public static function nodeClickLeaveLineageMode():ClickControl {
			
			return new ClickControl(NodeSprite, 2, 
			    function(e:SelectionEvent):void {
			    	
			    	if(!UFlareVis.lineageMode)
				      return;
			    	
			    	//if(UFlareVis.lineageMode && !e.node.data.hasOwnProperty("linTree"))
					  // return;
			    	e.node.dispatchEvent(new ULineageLeaveEvent(ULineageLeaveEvent.LEAVE_LINEAGE_MODE,true));
			    }
			);
		}
		
		public static function nodeClickDataLoad():ClickControl {		
			
			return new ClickControl(NodeSprite, 2, 
				function(e:SelectionEvent):void {
						
						if(UFlareVis.lineageMode)
						   return;
						
						var node:NodeSprite = e.node;
						
						if(node.data.hasOwnProperty("linTree"))
						   return;
						
						// this is a special request-all-facts node
						if(node.data.hasOwnProperty(UStringConstants.REQUEST_NODE)) {
						   // first, set the flag
						   node.dispatchEvent(new URequestAllFactsEvent(URequestAllFactsEvent.REQUEST_ALL_FACTS,true));
						   // now send the event that led to this node again
						   //node.dispatchEvent(node.data.Event as Event);
						   //trace("node.data.Event : " + (node.data.Event as Event).toString())
						   // abort
						   return;
						}
						
						// ------------------------------------------------------
						// we disable the restriction to explore a node only one
						// ------------------------------------------------------
						
						/*
						// we already explored this node with rule usage
						//if(node.data.explored == true)
						if(node.data.hasOwnProperty("explored"))
						   return; 
						
						// we have this node explored without rules, so check the current condition
						if(node.data.hasOwnProperty(UStringConstants.EXPLORED_WITHOUT_RULES)) {
							
							if(UApplicationControlBar.useRulesForDataLoading)
							   // now, set the flag for data loading with rules
						       node.data.explored = true;
							else
							   // again without rules? -> abort
							   return;
						}
						else {		
							// do we use rules for data loading?
							if(UApplicationControlBar.useRulesForDataLoading)
							   // now, set the flag for data loading with rules
						       node.data.explored = true;
							else
							   // now, set the flag for data loading with rules
						       node.data[UStringConstants.EXPLORED_WITHOUT_RULES] = true;	
						}
						*/
						// now, set the flag
						//node.data.explored = true;
						
						// we normalized the node label, so now we have to use the un-normalized
						// node tooltip to reload data
						var nodeLabel:String = node.data.hasOwnProperty(UStringConstants.TOOL_TIP) ? node.data.tooltip : "Max_Planck";
					
					    // the array that holds the queries
					    var queryArray:Array = new Array();
					
					    // the two necessary queries
						var queryOne:UQuery = new UQuery();
						queryOne.name = "Q1";
						var queryTwo:UQuery = new UQuery();
						queryTwo.name = "Q2";
						// the two necessary literals
						var literalOne:ULiteral = new ULiteral();
						var literalTwo:ULiteral = new ULiteral();
					
					    // the two needed relations
						var relation:URelation = URelation.valueOfRelation(UStringConstants.X_VARIABLE);//new URelation;
		
						// build the literals
						// case : ?x(node,?y))
						literalOne.initLiteralFull(relation,nodeLabel,UStringConstants.Y_VARIABLE);
						// case : ?x(?y,node))
						literalTwo.initLiteralFull(relation,UStringConstants.Y_VARIABLE,nodeLabel);
							
						queryOne.addLiteral(literalOne);
					    queryTwo.addLiteral(literalTwo);
					    
					    queryArray.push(queryOne);
					    queryArray.push(queryTwo);
					    
					    // the third parameter is set to true, because the event should bubble
					    // means, the UViz.mxml file receives the event, when the event comes back ftom the 
					    // event dospatcher, this ClickControl object
					    var queryEvent:UQueryEvent = new UQueryEvent(UQueryEvent.PROCESS_QUERY,queryArray,true); 
						queryEvent.dataLoadNode = node;
						//trace("node was clicked !!!");
						
						node.dispatchEvent(queryEvent);
						
				}, null);
		}
		
		public static function nodeClickPositionFixing():ClickControl {		
			
			return new ClickControl(NodeSprite, 3, 
				function(e:SelectionEvent):void {
						
						if(UFlareVis.lineageMode)
						   return;
						
						var node:NodeSprite = e.node;
						
						if(node.data.hasOwnProperty("linTree"))
						   return;
						
						if(node.fixed)
						   node.unfix();
						else
						   node.fix();
						
				}, null);
		}
		
		public static function nodeClickGraphRootUpdate():UDragClickControl {		
			/*
			return new ClickControl(NodeSprite, 1,  
				function(e:SelectionEvent):void {
					
					if(UFlareVis.lineageMode)
				      return;
					
					var node:NodeSprite = e.node;
					node.dispatchEvent(new URootNodeUpdateEvent(URootNodeUpdateEvent.UPDATE_ROOT_NODE,node,null,true)); 	
					//trace("new root should be set !!!!!!");	
				}, null);
				*/
			return new UDragClickControl(NodeSprite, 1, true,  
				function(e:SelectionEvent):void {
					
					if(UFlareVis.lineageMode)
				      return;
					
					var node:NodeSprite = e.node;
					var nodeEvent:URootNodeUpdateEvent = new URootNodeUpdateEvent(URootNodeUpdateEvent.UPDATE_ROOT_NODE,node,null,true);
					nodeEvent.nodeClicked = true;
					//node.dispatchEvent(new URootNodeUpdateEvent(URootNodeUpdateEvent.UPDATE_ROOT_NODE,node,null,true)); 	
					node.dispatchEvent(nodeEvent); 	
					//trace("new root should be set !!!!!!");	
				}, function(e:SelectionEvent):void {if(e.node.fixed)e.node.unfix();});
		}
		
		/**
		 * Opens a lineage graph by clicking an edge (represents a fact).
		 * */
		public static function edgeClickShowLineage():ClickControl {
			
			return new ClickControl(EdgeSprite, 1,  
				function(e:SelectionEvent):void {
						
						// we are already in the lineage mode
						if(UFlareVis.lineageMode)
						   return;
						
						var edge:EdgeSprite = e.edge;
						// this dynamic label exists, nevertheless check
						if(!edge.data.hasOwnProperty(UStringConstants.FACT_ID))
						   return;
						if(edge.data.hasOwnProperty("lineageAvailable") && edge.data.lineageAvailable ) {
							// request the factId of the edge we want to click   
							var factID:String = edge.data.factID;
							
							// mark the current edge as selected -> we need this for the tooltip control
							//edge.data["selected"] = true;
							
							// the third parameter is set to true, because the event should bubble
						    // means, the UViz.mxml file receives the event, when the event comes back from the 
						    // event dispatcher, this ClickControl object
						    var lineageEvent:ULineageEvent = new ULineageEvent(ULineageEvent.PRODUCE_LINEAGE_GRAPH,factID,true); 
							
							edge.dispatchEvent(lineageEvent);					
						}
						else {
							//mx.core.Application.application.showNoLineageToolTip((edge.x1 + edge.x2)/2,(edge.y1 + edge.y2)/2);
							return;
						}
						   
						
				}, null);
			
		}

	}
}