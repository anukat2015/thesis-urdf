package uviface.viz.data
{
	import flare.display.TextSprite;
	import flare.vis.data.DataList;
	import flare.vis.data.EdgeSprite;
	import flare.vis.data.NodeSprite;
	
	import flash.events.MouseEvent;
	import flash.text.TextFormat;
	
	import urdf.api.UFact;
	
	import util.UStringUtil;
	
	import uviface.viz.control.UGraphControls;
	import uviface.viz.display.UNode;
	import uviface.viz.operator.UNodeRenderer;
	import uviface.viz.util.UColors;
	import uviface.viz.util.UStringConstants;
	
	public class UCache
	{
		private var _numOfUsedNodes:uint = 0;
		private var _numOfCachedNodes:uint = 0;
		private var _numOfUsedEdges:uint = 0;
		private var _numOfCachedEdges:uint = 0;
		
		private var _cachedNodes:Array;
		private var _cachedEdges:Array;
		
		private var _usedNodes:Object;
		private var _usedEdges:Object;
		private var _usedPredicates:Object;
		
		private var _usedLineageNodes:Array;
		private var _usedLineageEdges:Array;
		private var _tempName:String;
		
		// a text formatter for the edge sprite labels
		private var _labelFormatterEdges:TextFormat;
		// a flag to indicate if edge labels are visible
		private var _edgeLabelsVisible:Boolean = false;
		
		public function UCache()
		{
			_cachedNodes = new Array();
		    _cachedEdges = new Array();
		    _usedNodes = new Object();
		    _usedEdges = new Object();
		    _usedPredicates = new Object();
		    _usedLineageNodes = new Array();
		    _usedLineageEdges = new Array();
		    
		    // configure the text format for the edge labels
		    _labelFormatterEdges = new TextFormat();
			_labelFormatterEdges.font = "Verdana";
			//_labelFormatterNodes.color = UColors.BLACK;
			_labelFormatterEdges.color = UColors.WHITE;
			_labelFormatterEdges.size = 14;//16;
			_labelFormatterEdges.bold = true;
		}
		
		public function get labelFormatterEdges():TextFormat {
			return _labelFormatterEdges;
		}
		
		public function get edgeLabelsVisible():Boolean {
			return _edgeLabelsVisible;
		}
		
		public function set edgeLabelsVisible(edgeLabelsVisible:Boolean):void {
			_edgeLabelsVisible = edgeLabelsVisible;
		}
		
		public function nodeInUse(nodeName:String):Boolean {
			return _usedNodes.hasOwnProperty(nodeName);
		}
		
		public function edgeInUse(edgeName:String):Boolean {
			return _usedEdges.hasOwnProperty(edgeName);
		}
		
		public function useNode(nodeName:String):NodeSprite {
			
			if(_usedNodes.hasOwnProperty(nodeName)) {
				return _usedNodes[nodeName] as NodeSprite;
			}		   
			
			else {				
					// create new node instance and push it into the appropriate node cache array
					var node:NodeSprite = new NodeSprite();
					
					// set an addiditonal 'tooltip' field which is used for 
			        // displaying a tooltip on a node
			        // the 'label' field is used for all other node name related things  
			        node.data.tooltip = nodeName;
				
				    // set the name of the node
				    node.data.label = nodeName;
				    if(UStringUtil.startsWith(nodeName,"wordnet_")) {
						  node.data.label = nodeName.slice(nodeName.indexOf("_") + 1, nodeName.lastIndexOf("_"));
						  //trace("Name is: " + node.data.label);
					}
					else
						if(UStringUtil.startsWith(nodeName,"wikicategory_")) {
						   node.data.label = nodeName.slice(nodeName.indexOf("_") + 1);
						   //trace("Name is: " + node.data.label);
						}
				   
				    node.data.label = (node.data.label.length > 20) ? node.data.label.substr(0,20).concat(UStringConstants.THREE_DOTS) : node.data.label;
					
					// we need an additional 'lineage' flag that tells the
				    // property setting function to include/exclude this 
				    // this node from a setting application within the visualization
				    node.data.lineage = false;
					
					// set the name of the node
					//node.data.label = nodeName; 
					// push the new node into the cache array 
					//_cachedNodes.push(node);
					// put the node into the usage hash as well
					_usedNodes[nodeName] = node;
					// increase the chached nodes counter and the used nodes counter
					//_numOfCachedNodes++;
					_numOfUsedNodes++;
					
					// set the chosen node renderer
					node.renderer = UNodeRenderer.instance
					// activate bitmap caching for the node
					node.cacheAsBitmap = true;
					
					return node;				
			}
		}
		
		public function useEdge(edgeName:String, predicate:String, sourceNode:NodeSprite, 
		                        targetNode:NodeSprite, confidence:String = null, 
		                        truthValue:int = -1, fact:UFact = null, lineageMapping:Array = null):EdgeSprite {
			
			if(_usedEdges.hasOwnProperty(edgeName)) {
			   return _usedEdges[edgeName] as EdgeSprite;
			}
			
			else {
				
				// add the current predicate
				if(!_usedPredicates.hasOwnProperty(predicate))
				   _usedPredicates[predicate] = _usedPredicates;
				
					// create new edge instance and push it into the appropriate edge cache array
					// we use the given source and target nodes and want the edge to be directed
					// -> the third parameter in the edge constructor indicates, that we want
					// a directed edge
					var edge:EdgeSprite = new EdgeSprite(sourceNode,targetNode,true);
					
					if(fact != null) {
						// check if there are lineage information available
						//if(fact.getLineage().getRulesAtLevel(fact.min_level) != null) {
						if(fact.getLineage().lineageAvailable()) 
							edge.data.lineageAvailable = true;
						else
						    edge.data.lineageAvailable = false;
					}
					
					// set an addiditonal 'tooltip' field which is used for 
			        // displaying a tooltip on a node
			        // the 'label' field is used for all other node name related things
			        //edge.data.tooltip = (confidence == null) ? predicate : (predicate + "[" + confidence + "] - " + determineTruthValue(truthValue));
				    edge.data.tooltip = (confidence == null) 
				                      ? (predicate)//predicate 
				                      : //(UStringConstants.EDGE_RELATION + predicate 
				                        //+ ", " + UStringConstants.REPRESENTED_FACT + 
				                        /* (predicate
				                        + "(" + sourceNode.data.tooltip + "," + targetNode.data.tooltip + ")" 
				                        + "[" + determineTruthValue(truthValue) + "|" + confidence + "]" 
				                        + "\n" + "- lineage available: " + edge.data.lineageAvailable); */
				                        (predicate + "(" + sourceNode.data.tooltip + "," 
				                        + targetNode.data.tooltip + ")<br/>" 
				                        + "<b> truth value: </b>" + determineTruthValue(truthValue) + "<br/>"
				                        + "<b> confidence: </b>" + confidence + "<br/>"
				                        + "<b> lineage available: </b>" + edge.data.lineageAvailable);				                        				
				
				    // set the name of the node
				    //node.data.label = nodeName; 
				    edge.data.label = (predicate.length > 20) ? predicate.substr(0,20).concat(UStringConstants.THREE_DOTS) : predicate;
				    
				    edge.data.factName = predicate + "(" + sourceNode.data.tooltip + "," + targetNode.data.tooltip + ")";
					
					// we need an additional 'lineage' flag that tells the
				    // property setting function to include/exclude this 
				    // this node from a setting application within the visualization
				    edge.data.lineage = false;
				    
				    // set the confidence value, if any exist
				    if(confidence != null)
				      edge.data.confidence = confidence;
				    // set the truth value, if any exist
				    if(truthValue != -1)
				      edge.data.truthValue = truthValue;
					
					// set the name of the edge
					//edge.data.label = edgeName; 
					// set the edges of the corresponding nodes
					edge.source.addOutEdge(edge);
			        edge.target.addInEdge(edge);
			        
					// push the new edge into the cache array 
					//_cachedEdges.push(edge);
					// put the edge into the usage hash as well
					_usedEdges[edgeName] = edge;
					// increase the chached edges counter and the used edges counter
					//_numOfCachedEdges++;
					_numOfUsedEdges++;
					
					
					// check for extended lineage information
					if(lineageMapping != null) {
						// check if we have lineage information available
			            // if so, add an object descibing the fact and its lineage
			            if(edge.data.hasOwnProperty("lineageAvailable") && edge.data.lineageAvailable) {
			            	//var lineageString:String = "Click here to get the explanation tree for this fact!";
			            	var lineageString:String = "Show the Explanation tree for this fact!";
			            	//_factLineageAvalaibilityMap.push({label: fact.toString(), 
			            	lineageMapping.push({label: edge.data.factName
			            	     //children: [{label: minLevel}, {label: maxLevel}]});
			            	     , children: [{label: '', labelButton: lineageString, key: UStringConstants.LINEAGE, id: fact.getId().toString(), clickable: true}]});
			            }
		            }					
					
					// ------------------------------
					// ------------------------------
					// just a test for adding a marker to an edge
					// in case the fact represented by the edge
					// contains lineage information
					var textSprite:TextSprite = new TextSprite(edge.data.label);
					textSprite.horizontalAnchor = TextSprite.CENTER;
					textSprite.verticalAnchor = TextSprite.MIDDLE;
					//textSprite.visible = _edgeLabelsVisible;
					textSprite.textFormat = _labelFormatterEdges;
					textSprite.textMode = TextSprite.EMBED;
					edge.data.TS = textSprite;
					if(_edgeLabelsVisible)
					   edge.addChild(textSprite);
					// ------------------------------
					// ------------------------------	
					
					// set a flag to indicate that the textsprite should be mouse enabled as well
					textSprite.mouseEnabled = true;
					//textSprite.mouseChildren = true;
					//textSprite.buttonMode = true;
					textSprite.addEventListener(MouseEvent.MOUSE_OVER,UGraphControls.hoverOverEdgeLabel,false,0,true);
					textSprite.addEventListener(MouseEvent.MOUSE_OUT,UGraphControls.hoverOutEdgeLabel,false,0,true);
					textSprite.addEventListener(MouseEvent.CLICK,UGraphControls.clickEdgeLabel,false,0,true);
					edge.mouseChildren = true;
					
					return edge;	
			}
		}
		
		public function useLineageNode(nodeName:String, confidence:String = null, truthValue:int = -1):NodeSprite {
				// create new node instance and push it into the appropriate node cache array
				var node:NodeSprite = new NodeSprite();
				
				node.data.label = nodeName;
				// we need an additional 'lineage' flag that tells the
				// property setting function to include/exclude
				// this node from a setting application within the visualization
				node.data.lineage = true;
				// set the confidence value, if any exist
				if(confidence != null) {
				   node.data.confidence = confidence;
				   
				   if(nodeName.indexOf("ANSWER") >= 0) {
				   	  node.data.tooltip = "<b>" + nodeName.substring(0,nodeName.indexOf(":")+1) + "</b><br/>" 
				   	       + nodeName.substring(nodeName.indexOf("\n")+1,nodeName.length) + "<br/>"
			               + "<b> confidence: </b>" + confidence; 
				   }
				   else {
				       node.data.tooltip = nodeName + "<br/>"
			               + "<b> truth value: </b>" + determineTruthValue(truthValue) + "<br/>"
			               + "<b> confidence: </b>" + confidence;
			       }
				}
				else 
				   if(nodeName.indexOf("Base") >= 0) {
				   	  node.data.tooltip = nodeName.substring(0,nodeName.indexOf(":")) + "<br/>" 
				   	  + "<b> confidence: </b>" + nodeName.substring(nodeName.indexOf(":")+1,nodeName.length);
				   }
				   else
				     if(nodeName.indexOf("Query") >= 0) {
				   	  node.data.tooltip = "<b>" + nodeName.substring(0,nodeName.indexOf(":")+1) + "</b><br/>" 
				   	  + nodeName.substring(nodeName.indexOf(":")+1,nodeName.length);
				   }
				   else
				      node.data.tooltip = nodeName;
				// set the truth value, if any exist
				if(truthValue != -1)
				   node.data.truthValue = truthValue;
				
				// push the new node into the cache array 
				//_cachedNodes.push(node);
				// put the node into the lineage node array as well
				_usedLineageNodes.push(node);
				// increase the chached nodes counter
				//_numOfCachedNodes++;
				
				// set the chosen node renderer
				node.renderer = UNode.LINEAGE_NODE.nodeRenderer;//UNodeRenderer.instance;
				
				return node;
			//}
			
			/*
			// we have still some chached nodes free for usage by the visualization
			else {
				// get the next free chached node
				// an array starts at 0, so if we look for the number of used nodes as array posititon
				// of the next unused node -> this is correct, because we want the next free position
				// and not the currently last used position
				var cachedNode:NodeSprite = NodeSprite(_cachedNodes[_numOfUsedNodes + _usedLineageNodes.length])
				// remove all connections to the node
				cachedNode.removeAllEdges();
				// clear all properties
				clearNodeProperties(cachedNode);
				
				// set an addiditonal 'tooltip' field which is used for 
			    // displaying a tooltip on a node
			    // the 'label' field is used for all other node name related things
			    cachedNode.data.tooltip = (confidence == null) ? nodeName : (nodeName + "[" + confidence + "]");
				
				// set the name of the node
				//node.data.label = nodeName; 
				cachedNode.data.label = (nodeName.length > 20) ? nodeName.substr(0,20).concat(UStringConstants.THREE_DOTS) : nodeName;
				
				// we need an additional 'lineage' flag that tells the
				// property setting function to include/exclude this 
				// this node from a setting application within the visualization
				cachedNode.data.lineage = true;
				
				// set the node name
				//cachedNode.data.label = nodeName; 
				// put the node into the lineage node array as well
				_usedLineageNodes.push(cachedNode);
				return cachedNode;	
			}
			*/
		}
		
		public function useLineageEdge(edgeName:String, sourceNode:NodeSprite, targetNode:NodeSprite, edgeToolTip:String = null):EdgeSprite {
			// we are using all cached edges at the moment
			// request another edge instance
			//if((_numOfUsedEdges  + _usedLineageEdges.length) == _numOfCachedEdges) {
				// create new edge instance and push it into the appropriate edge cache array
				// we use the given source and target nodes and want the edge to be directed
				// -> the third parameter in the edge constructor indicates, that we want
				// a directed edge
				var edge:EdgeSprite = new EdgeSprite(sourceNode,targetNode,true);
				// set the edges of the corresponding nodes
				edge.source.addOutEdge(edge);
			    edge.target.addInEdge(edge);
			    
			    // set an addiditonal 'tooltip' field which is used for 
			    // displaying a tooltip on a node
			    // the 'label' field is used for all other node name related things
			    // in addition, we have the possibility to set an extra edge tooltip
			    if(edgeToolTip == null)
			       edge.data.tooltip = edgeName;
			    else {
				   if(edgeToolTip.indexOf("<=") < 0 && edgeToolTip.indexOf("<b>") < 0) {
				       edge.data.tooltip = (edgeToolTip.indexOf("ANSWER") >= 0) 
				                        ? ("<b>" + edgeToolTip.substring(0,edgeToolTip.indexOf(":")+1) + "</b><br/>" 
				                           + edgeToolTip.substring(edgeToolTip.indexOf("\n")+1,edgeToolTip.length))
				                        : edgeToolTip;
				   }
			       else
			          edge.data.tooltip = edgeToolTip;
			    }
				// set the name of the edge 
				//edge.data.label = edgeName; 
				//edge.data.label = edgeName;//(edgeName == null) ? edgeName : ((edgeName.length > 20) ? edgeName.substr(0,20).concat(UStringConstants.THREE_DOTS) : edgeName);
				//if((edgeName == null) || edgeName.indexOf("<=") < 0)
				if(edgeName == null || (edgeName.indexOf("<=") < 0 && edgeName.indexOf("<b>") < 0))
				   edge.data.label = edgeName;
				else
				   //edge.data.label = edgeName.substr(0,(edgeName.lastIndexOf("<=") + 1)).concat(" ...");
				   //edge.data.label = edgeName.substr(0,(edgeName.indexOf("]") + 1)).concat(" <= ...");
				   //edge.data.label = edgeName.substr(0,(edgeName.indexOf(")") + 1)).concat("\n<= ...");
				   //edge.data.label = edgeName.substring(edgeName.indexOf("</b>")+4,edgeName.indexOf("<br/>")).concat(" <= ...");
				   edge.data.label = edgeName.substring(0,edgeName.indexOf(")")+1).concat(" <= ...");
				   
				// we need an additional 'lineage' flag that tells the
				// property setting function to include/exclude this 
				// this node from a setting application within the visualization
				edge.data.lineage = true;
				
				// push the new edge into the cache array 
				//_cachedEdges.push(edge);
				// put the node into the lineage edge array as well
				_usedLineageEdges.push(edge);
				// increase the chached edges counter
				//_numOfCachedEdges++;
				
				// ------------------------------
				// ------------------------------
				// just a test for adding a marker to an edge
				// in case the fact represented by the edge
				// contains lineage information
				var textSprite:TextSprite = new TextSprite(edge.data.label);
				textSprite.textField.backgroundColor = UColors.BLACK;
				textSprite.textField.alpha = 1.0;
				textSprite.horizontalAnchor = TextSprite.CENTER;
				textSprite.verticalAnchor = TextSprite.MIDDLE;
				//textSprite.visible = _edgeLabelsVisible;
				textSprite.textFormat = _labelFormatterEdges;
				textSprite.textMode = TextSprite.EMBED;
				edge.data.TS = textSprite;
				if(_edgeLabelsVisible)
				   edge.addChild(textSprite);
				// ------------------------------
				// ------------------------------	
				
				// set a flag to indicate that the textsprite should be mouse enabled as well
				textSprite.mouseEnabled = true;
				//textSprite.mouseChildren = true;
				//textSprite.buttonMode = true;
				textSprite.addEventListener(MouseEvent.MOUSE_OVER,UGraphControls.hoverOverEdgeLabel,false,0,true);
				textSprite.addEventListener(MouseEvent.MOUSE_OUT,UGraphControls.hoverOutEdgeLabel,false,0,true);
				edge.mouseChildren = true;
				
				return edge;
			//}
			
			/*
			// we have still some chached edges free for usage by the visualization
			else {
				// get the next free chached edge
				// an array starts at 0, so if we look for the number of used edges as array posititon
				// of the next unused edge -> this is correct, because we want the next free position
				// and not the currently last used position
				var cachedEdge:EdgeSprite = EdgeSprite(_cachedEdges[_numOfUsedEdges + _usedLineageEdges.length])
				
				// clear all properties
				clearEdgeProperties(cachedEdge);
				
				// set the new source and target nodes for this edge
				cachedEdge.source = sourceNode;
				cachedEdge.target = targetNode;
				// set the edges of the corresponding nodes
				cachedEdge.source.addOutEdge(cachedEdge);
			    cachedEdge.target.addInEdge(cachedEdge);
		
		        // set an addiditonal 'tooltip' field which is used for 
			    // displaying a tooltip on a node
			    // the 'label' field is used for all other node name related things
			    cachedEdge.data.tooltip = edgeName;
				// set the name of the edge
				//edge.data.label = edgeName; 
				cachedEdge.data.label = (edgeName.length > 20) ? edgeName.substr(0,20).concat(UStringConstants.THREE_DOTS) : edgeName;
		
		        // we need an additional 'lineage' flag that tells the
				// property setting function to include/exclude this 
				// this node from a setting application within the visualization
				cachedEdge.data.lineage = true;
		
				// set the edge name
				//cachedEdge.data.label = edgeName; 
				// put the node into the lineage edge array as well
				_usedLineageEdges.push(cachedEdge);
				return cachedEdge;	
			}
			*/
		}
		
		public function get numOfCachedNodes():int {
			return _numOfCachedNodes;
		}
		
		public function get numOfCachedEdges():int {
			return _numOfCachedEdges;
		}
		
		public function get usedNodes():Object {
			return _usedNodes;
		}
		
		public function get usedEdges():Object {
			return _usedEdges;
		}
		
		public function get usedNodesList():Array {
			var nodeList:Array = new Array();
			var node:String;
			for (node in _usedNodes) {
				nodeList.push(node);
			}
			return nodeList;
		}
		
		public function get usedPredicatesList():Array {
			var predicateList:Array = new Array();
			var predicate:String;
			for (predicate in _usedPredicates) {
				predicateList.push(predicate);
			}
			return predicateList;
		}
		
		public function get lineageNodes():Array {
			return _usedLineageNodes;
		}
		
		public function get lineageEdges():Array {
			return _usedLineageEdges;
		}	
		
		public function lineageNodesList():DataList {
			var nodeList:DataList = new DataList(UStringConstants.NODES);
			var node:NodeSprite;
			for each (node in _usedLineageNodes) {
				nodeList.add(node);
			}
			return nodeList;
		}
		
		public function lineageEdgesList():DataList {
			var edgeList:DataList = new DataList(UStringConstants.EDGES);
			var edge:EdgeSprite;
			for each (edge in _usedLineageEdges) {
				edgeList.add(edge);
			}
			return edgeList;
		}
		
		public function determineTruthValue(truthValue:int):String {
			switch(truthValue) {
				case 0: return "false";
				case 1: return "true";
				default: return "unknown";
			}
		}
		
		private function clearNodeProperties(node:NodeSprite):void {
			
			var property:String;
			//for each (var property in node.data)
			for (property in node.data) {
			   //node.data[property] = null
			   delete node.data[property];
			   //node.data[property] = undefined;
			}
			//for each (property in node.props)
			for (property in node.props) {
			   //node.props[property] = null;
			   delete node.props[property];
			   //node.props[property] = undefined;
			}
			
			node.data = null;
			node.data = new Object();
			node.props = null;
			node.props = new Object();
			
			//while(node.points.length > 0)
			  // node.points.pop();
			// remove all child objects
			for (var i:int=0; i<node.numChildren; i++) {
				//trace("node.numChildren = " + node.numChildren);
			   //trace("node.removeChildAt(i) = " + node.removeChildAt(i).toString());
			   node.removeChildAt(i);
			}
			//node.removeChildAt(0);
		}
		
		private function clearEdgeProperties(edge:EdgeSprite):void {
			
			var property:String;
			//for each (property in edge.data)
			for (property in edge.data) {
			   //edge.data[property] = null;
			   delete edge.data[property];
			   //edge.data[property] = undefined;
			}
			//for each (property in edge.props)
			for (property in edge.props) {
			   //edge.props[property] = null;
			   delete edge.props[property];
			   //edge.props[property] = undefined;
			}
			
			edge.data = null;
			edge.data = new Object();
			edge.props = null;
			edge.props = new Object();
			
			// remove all child objects
			//for (var i:int=0; i<edge.numChildren; i++)
			  // edge.removeChildAt(i);
		}
		
		public function clearLineageData():void {
			
			var node:NodeSprite;
			var edge:EdgeSprite;
			
			for each (node in _usedLineageNodes) {
				node = null;
			}
			
			for each (edge in _usedLineageEdges) {
				edge = null;
			}
			
			while (_usedLineageNodes.length > 0) {
				_usedLineageNodes.pop();
			}
			while (_usedLineageEdges.length > 0) {
				_usedLineageEdges.pop();
			}
		}
		
		/** Clears the hashes for the used nodes and edges. */
		public function clearUsedData():void {
			// first, delete all used nodes
			
			var nodeSprite:NodeSprite;
			var edgeSprite:EdgeSprite;
			
			for each (nodeSprite in _usedNodes) {
			   nodeSprite = null;
			}
			// second, delete all used edges
			for each (edgeSprite in _usedEdges) {
			   edgeSprite = null;
			}
			
			for (var node:String in _usedNodes) {
			   delete _usedNodes[node];
			   //_usedNodes[node] = undefined;
			}
			// second, delete all used edges
			for (var edge:String in _usedEdges) {
			   delete _usedEdges[edge];
			   //_usedEdges[edge] = undefined;
			}
			// second, delete all used edges
			for (var predicate:String in _usedPredicates) {
			   delete _usedPredicates[predicate];
			   //_usedEdges[edge] = undefined;
			}
			
			// currently, we use new hashs, because there is some problem with the hashing
			_usedNodes = new Object();
		    _usedEdges = new Object();
		    _usedPredicates = new Object();
			
			// now, set the used nodes and edges counters to 0 again
			_numOfUsedNodes = 0;
			_numOfUsedEdges = 0;
			
			// clear the lineage data as well
			clearLineageData();
		}

	}
}