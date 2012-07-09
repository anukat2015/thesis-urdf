package uviface.viz.util
{
	import flare.animate.Transitioner;
	import flare.vis.data.EdgeSprite;
	import flare.vis.data.NodeSprite;
	
	import flash.display.DisplayObjectContainer;
	import flash.geom.Rectangle;
	
	import urdf.api.UFact;
	import urdf.api.UGroundedSoftRule;
	import urdf.api.ULineageAnd;
	import urdf.api.ULineageOr;
	import urdf.api.USoftRule;
	
	import uviface.viz.UFlareVis;
	import uviface.viz.data.*;
	import uviface.viz.operator.UShapes;
	
	public class UGraphUtil
	{		
		private static var _processingLevel:int = 0;
		
		private static var _fullLineageMode:Boolean = false;
		
		private static var _nodeLabel:String;
		private static var _edgeLabel:String;
		
		private static var _sourceNode:NodeSprite;
		private static var _targetNode:NodeSprite;
		
		private static var _factNodeMapping:Object = new Object();
		
		public static function get factNodeMapping():Object {
			return _factNodeMapping;
		}
		
		public static function set factNodeMapping(factNodeMapping:Object):void {
			_factNodeMapping = factNodeMapping;
		}
		
		/** Request all nodes that represent the fact matching the specified fact id. */
		public static function requestSameFactNodes(factID:String):Array {
			return _factNodeMapping[factID];
		}
		
		/** Request all nodes that represent the fact matching the specified fact id. */
		public static function highlightSameFactNodes(factNode:NodeSprite, fillColor:uint):void {
			if(_factNodeMapping.hasOwnProperty(factNode.data.factID)) {
			   var p:DisplayObjectContainer;
			   var _idx:int;
			   for each (var node:NodeSprite in (_factNodeMapping[factNode.data.factID] as Array)) {
			   	  // only alraedy visible nodes should be highlighted, 
			   	  // exclude the already changed source node of that function call
			   	  if(node.visible == false || node == factNode)
			   	     continue;
			   	  
			   	  // move to front   
			   	  p = node.parent;
				  _idx = p.getChildIndex(node);
				  p.setChildIndex(node, p.numChildren-1);
			   	     
			   	  node.data.fillColor = node.fillColor;
			   	  node.scaleX *= 2;
				  node.scaleY *= 2;
			   	  node.fillColor = fillColor;
			   }
			}
			else
			   return;
		}
		
		/** Request all nodes that represent the fact matching the specified fact id. */
		public static function resetSameFactNodes(factNode:NodeSprite):void {
			if(_factNodeMapping.hasOwnProperty(factNode.data.factID)) {
			   var i:int = 0;
			   var edge:EdgeSprite;
			   for each (var node:NodeSprite in (_factNodeMapping[factNode.data.factID] as Array)) {
			   	  
			   	  // only alraedy visible nodes should be resetted, 
			   	  // exclude the already changed source node of that function call
			   	  if(node.visible == false || node == factNode)
			   	     continue;
			   	  
			   	  node.scaleX /= 2;
				  node.scaleY /= 2;
			   	  node.fillColor = node.data.fillColor;
			   	  
			   	  // reset i
				  i=0;
			   	  
				  for (i; i<node.inDegree; i++) {
					edge = node.getInEdge(i);
					// mark the edge as dirty
					edge.dirty();
				  }
					
				  // reset i
				  i=0;
				  
				  for (i; i<node.outDegree; i++) {
					edge = node.getOutEdge(i);
					edge.dirty();
				  }
			   }
			}
			else
			   return;
		}
		
		public static function clearFacNodeMapping():void {
			for (var key:String in _factNodeMapping) {
				delete _factNodeMapping[key];
			}
			_factNodeMapping = new Object();
		}
		
		// ---------- UViz specific Graph Generators ---------------------
		
		public static function degreePrioritizedGraph(bounds:Rectangle,graphData:UCache):int {
			
			if(graphData == null)
			   return 0;
			
			//var nodes:Object = graphData.nodes;
			//var edges:Object = graphData.edges;
			var nodes:Object = graphData.usedNodes;
			var edges:Object = graphData.usedEdges;

            if(nodes == null)
               return 0;
            
            var nodeCounter:int = 0;
            
            for each (var n:NodeSprite in nodes) {
            	nodeCounter++;
            }
            // check if the object is empty
            if(nodeCounter == 0)
               return 0;


            // this is the variable that streos te highest degree value
            var highestDegree:int = 0;
            // this is the variable that stores the node holding the highest degree 
            var highestDegreeNode:NodeSprite; 
			
			// iterate over the data aka. dependency graph (in our case) 
			for each (var node:NodeSprite in nodes) {
				var degree:uint = node.degree;//node.inDegree + node.outDegree;
				// compare the degrees, if the degree is the same, replace
				// so, the last node with the highest degree will be chosen, no matter how many
				// nodes with this degree do exist
				if(degree >= highestDegree) {
					highestDegree = degree;
					highestDegreeNode = node;
				}
				
			}
			
			// compute a random sign to add or subtract from the given x value
		    var randomSignedX:Boolean = (Math.random() > 0.5 ? true : false);	
			
			// compute a random sign to add or subtract from the given y value
		    var randomSignedY:Boolean = (Math.random() > 0.5 ? true : false); 
		    
		    // center the node with the highest degree
		    highestDegreeNode.x = bounds.x + 0.5 * bounds.width;
			highestDegreeNode.y = bounds.y + 0.5 * bounds.height;
			
			// ---------------------------------------------
			// add this node as the root of the graph (tree)
			// ---------------------------------------------
			//graphData.data.root = highestDegreeNode;
			
			// compute a useful node - visualization size ratio
			var nodeSizeRatio:Number = ((bounds.width + bounds.height) / nodeCounter);
			// all visited nodes so far
			var visitedNodes:Object = new Object();
			if(highestDegreeNode.data.hasOwnProperty("label"))
			   visitedNodes[highestDegreeNode.data.label] = highestDegreeNode;
			else
			   return 0; // no "label" property present -> do not build the graph
		    
		    /*if(highestDegreeNode.data.hasOwnProperty("label")) {
		       trace("The highest degree node is: " + highestDegreeNode.data.label);
		       trace("The highest degree node x is: " + highestDegreeNode.x);
		       trace("The highest degree node y is: " + highestDegreeNode.y);
		       trace("bounds.width: " + bounds.width);
		       trace("bounds.height: " + bounds.height);
		    }*/
		    
		    traverseChildNodes(highestDegreeNode, highestDegreeNode.x, highestDegreeNode.y, 
		            randomSignedX, randomSignedY, nodeSizeRatio, visitedNodes);
		            
		    return highestDegree;
			
		}
		
		private static function traverseChildNodes(fatherNode:NodeSprite, 
		                  x:int, y:int, signedX:Boolean, signedY:Boolean, 
		                  nodeSizeRatio:Number, visitedNodes:Object):void {
		                  	
		    if(fatherNode.data.hasOwnProperty("label")) {
		        
		        //  we have already visited this node
		        if(visitedNodes.hasOwnProperty(fatherNode.data.label))
		           return;
		    }
		    else // we need such a label for the node name
		       return
		
		    fatherNode.visitNodes(function(visitedNodes:Object):void {
		        
		        var inDegree:int = fatherNode.inDegree;
		        var outDegree:int = fatherNode.outDegree;
		        
		        // compute a random sign to add or subtract from the given x value
		        var randomSignedX:Boolean = (Math.random() > 0.5 ? true : false);
		        
		        // do we have a sign (negative sign) -> subtract, otherwise add
		        if(randomSignedX)
		           fatherNode.x = x + nodeSizeRatio;
		        else
		           fatherNode.x = x - nodeSizeRatio;
		    
		        // compute a random sign to add or subtract from the given y value
		        var randomSignedY:Boolean = (Math.random() > 0.5 ? true : false);
		        // do we have a sign (negative sign) -> subtract, otherwise add
		        
		        if(randomSignedY)
		           fatherNode.y = y + nodeSizeRatio;
		        else
		           fatherNode.y = y - nodeSizeRatio; 
		        
		        visitedNodes[fatherNode.data.label] = fatherNode;
		        
		        if(fatherNode.data.hasOwnProperty("label"))
		           trace("The current node is: " + fatherNode.data.label);
		        
		        // the index variable to iterate through all the in- and out-nodes
		        var i:int;
		           
		        for (i=0; i<inDegree; i++) {
		           var sourceNode:NodeSprite = fatherNode.getChildNode(i);
		           traverseChildNodes(sourceNode, fatherNode.x, fatherNode.y, 
		              randomSignedX, randomSignedY, nodeSizeRatio, visitedNodes);
		        }
		        
		        for (i=0; i<outDegree; i++) {
		           var targetNode:NodeSprite = fatherNode.getChildNode(i);
		           traverseChildNodes(targetNode, fatherNode.x, fatherNode.y, 
		              randomSignedX, randomSignedY, nodeSizeRatio, visitedNodes);
		        }
		    
		    });
		
		} 
		
		/**
		 * Biuld the lineage graph for a distinct matching of the query patterns (sub-result).
		 * */
		public static function produceLineageGraphFullResult(lineageRoot:Array, flareVis:UFlareVis, t:Transitioner = null, rootName:String = null):NodeSprite {
			
			_fullLineageMode = true;
			
			var graphData:UGraphData = flareVis.graphData;
			//var data:Data = flareVis.visualization.data;
			
			// no useful lineage to show
			if(lineageRoot == null || lineageRoot.length == 0)
			   return null;
			   
		    // set the label for the root lineage node
			//var rootLabel:String = "Query: \n" + ((rootName == null) ? "'Current Query'" : rootName.substr(1)); 
			var rootLabel:String = "Query: " + ((rootName == null) ? "'Current Query'" : rootName.substr(1));
			//var lineageHull:ULineageAbstract;
	    	var lineageResult:ULineageAnd;
          	
			var lineageRootNode:NodeSprite = graphData.graphCache.useLineageNode(rootLabel,null,-1);//1);//fact.getTruthValue());
					
			// set the basic node properties
			//setRootNodeProperties(lineageRootNode);//, convexHull);
			// connect the existing edges of both fact connected nodes
			// to the new fact node
			//collectTempLineageEdges(sourceNode,targetNode,flareVis);
			
			// add the fact/lineage node temporarily
			flareVis.visualization.data.addNode(lineageRootNode);
			
			lineageRootNode.x = flareVis.radialLayout.layoutAnchor.x;//flareVis.width / 2;
			lineageRootNode.y = flareVis.radialLayout.layoutAnchor.y;//flareVis.height / 2;
			
			var childEdge:EdgeSprite;
			var childNode:NodeSprite;
			//var orNode:NodeSprite;
			var disjunctEdge:EdgeSprite;
			
		    /* if(lineageRoot.length > 1) {  
	            
	            orNode = graphData.graphCache.useLineageNode(UStringConstants.OR,null,-1);
	            
	            // add this node to the data set
	            flareVis.visualization.data.addNode(orNode);
	            // bring the lineageNode to the top center of the visualization 
			    orNode.x = lineageRootNode.x;//bounds.x + 0.5 * bounds.width;
			    orNode.y = lineageRootNode.y;//bounds.y + 10;
				  
	      	    //disjunctEdge = graphData.graphCache.useLineageEdge("v",node,lineageNode);//grSRNode.parentEdge;
	      	    disjunctEdge = graphData.graphCache.useLineageEdge(null,orNode,lineageRootNode,"Several Answers for the query '<b>" + rootName.substr(1) + "</b>' exist!"); 
			  
			    // set the properties for this node
			    // it is needed here, because we need the connection to other node of the related edge		            
	            setRootNodeProperties(orNode);
			  
			    //var disjunctEdge:EdgeSprite = graphData.graphCache.useLineageEdge("v",lineageNode,disjunctNode)//grSRNode.parentEdge;
	            // add a lineage tree flag
	            disjunctEdge.data.linTree = true;
	            // set a hide flag
		        //disjunctEdge.data.hide = false;
		        // at first, hide the edge
			    disjunctEdge.visible = true;
	          
	            // add this edge to the visualization
	            flareVis.visualization.data.addEdge(disjunctEdge);
        	        
	        } */
			
			var counter:int = 1;
			for each (lineageResult in lineageRoot) {
			    childNode = produceLineageGraphMatchedPattern(lineageResult, flareVis, t, UStringConstants.ANSWER + counter.toString());
			    // request an edge from the cache
	          	//if(orNode == null)
	          	childEdge = graphData.graphCache.useLineageEdge(UStringConstants.ANSWER + counter.toString(),childNode,lineageRootNode,childNode.data.label);
	          	//else
	          	  //  childEdge = graphData.graphCache.useLineageEdge(UStringConstants.ANSWER + counter.toString(),childNode,orNode,childNode.data.label);
	      
                // add a lineage tree flag
	            childEdge.data.linTree = true;
                // set a hide flag
		        //edge.data.hide = false;
		        // at first, hide the edge
		        childEdge.visible = true;

                // add this edge to the visualization
	            flareVis.visualization.data.addEdge(childEdge); 
	            
	            // increment the counter for the child label
	            counter++; 			
			}
			
			// set the basic node properties
			setRootNodeProperties(lineageRootNode);//, convexHull);
			
			// now, we return the lineage root node
			return lineageRootNode;
		}
		
		/**
		 * Biuld the lineage graph for a distinct matching of the query patterns (sub-result).
		 * */
		public static function produceLineageGraphMatchedPattern(lineageRoot:ULineageAnd, flareVis:UFlareVis, t:Transitioner = null, rootString:String = null):NodeSprite {
			
			_fullLineageMode = true;
			
			var graphData:UGraphData = flareVis.graphData;
			//var data:Data = flareVis.visualization.data;
			
			// no useful lineage to show
			if(lineageRoot.getChildren().length == 0)
			   return null;
			   
		    // set the label for the root lineage node
			//var rootLabel:String = ((rootString == null) ? "Lineage Root: " : "Matched Query Pattern: "); 
			var rootLabel:String = ((rootString == null) ? (UStringConstants.ANSWER + ":") : (rootString + ": \n")); 
			//var lineageHull:ULineageAbstract;
	    	var lineageFact:ULineageOr;
	    	var fact:UFact;
			/*
			for each (var resultLineage:ULineageOr in lineageRoot.getChildren()) {
				lineageFact = ULineageOr(resultLineage);
		    	fact = lineageFact.getFact();
		    	
		    	// the result string for the label
		    	rootLabel += fact.toStringSimple() + "^" ;
			}*/
			for each (lineageFact in lineageRoot.getChildren()) {
				//lineageFact = ULineageOr(resultLineage);
		    	fact = lineageFact.getFact();
		    	
		    	// the result string for the label
		    	rootLabel += fact.toStringSimple() + "\n" ;
			}
			rootLabel = rootLabel.substring(0,rootLabel.length-1);
			//rootLabel = rootLabel.substring(0,rootLabel.length-2);
			
			// request the root fact node
			var conf:Number = lineageRoot.getConf();
          	//conf = (conf <= 0.01 && conf > 0) ? 0.01 : conf; 
          	var confString:String = conf.toFixed(2);
          	
          	// currently, we have to set this to true, manually -> which truth value to take
			var lineageRootNode:NodeSprite = graphData.graphCache.useLineageNode(rootLabel,confString,-1);//1);//fact.getTruthValue());
					
			// set the basic node properties
			setRootNodeProperties(lineageRootNode);//, convexHull);
			// connect the existing edges of both fact connected nodes
			// to the new fact node
			//collectTempLineageEdges(sourceNode,targetNode,flareVis);
			
			// add the fact/lineage node temporarily
			flareVis.visualization.data.addNode(lineageRootNode);
			
			lineageRootNode.x = flareVis.radialLayout.layoutAnchor.x;//flareVis.width / 2;
			lineageRootNode.y = flareVis.radialLayout.layoutAnchor.y;//flareVis.height / 2;
			
			var childEdge:EdgeSprite;
			var childNode:NodeSprite;
			var andNode:NodeSprite;
			var conjunctEdge:EdgeSprite;
			
		    if(lineageRoot.getChildren().length > 1) {  
	            
	            andNode = graphData.graphCache.useLineageNode(UStringConstants.AND,null,-1);
	            
	            // add this node to the data set
	            flareVis.visualization.data.addNode(andNode);
	            // bring the lineageNode to the top center of the visualization 
			    andNode.x = lineageRootNode.x;//bounds.x + 0.5 * bounds.width;
			    andNode.y = lineageRootNode.y;//bounds.y + 10;
				  
	      	    //disjunctEdge = graphData.graphCache.useLineageEdge("v",node,lineageNode);//grSRNode.parentEdge;
	      	    conjunctEdge = graphData.graphCache.useLineageEdge(null,andNode,lineageRootNode,"<b>The answer '" + rootString + "</b>' contains several facts!"); 
			  
			    // set the properties for this node
			    // it is needed here, because we need the connection to other node of the related edge		            
	            setRootNodeProperties(andNode);
			  
			    //var disjunctEdge:EdgeSprite = graphData.graphCache.useLineageEdge("v",lineageNode,disjunctNode)//grSRNode.parentEdge;
	            // add a lineage tree flag
	            conjunctEdge.data.linTree = true;
	            // set a hide flag
		        //disjunctEdge.data.hide = false;
		        // at first, hide the edge
			    conjunctEdge.visible = true;
	          
	            // add this edge to the visualization
	            flareVis.visualization.data.addEdge(conjunctEdge);
        	        
	        }
			
			for each (var child:ULineageOr in lineageRoot.getChildren()) {   
			    // request an edge from the cache
	          	if(andNode == null)
	          	    buildLineageLevelOr(child, lineageRootNode, flareVis, true, true);	
	          	else
	          	    buildLineageLevelOr(child, andNode, flareVis, true, true);	
			    //buildLineageLevelOr(child, lineageRootNode, flareVis, true, true);			
			}
			
			// now, we return the lineage root node
			return lineageRootNode;
		}
		
		private static function buildLineageLevelOr(lineageOr:ULineageOr, lineageNode:NodeSprite, flareVis:UFlareVis, drawNode:Boolean = true, firstLevel:Boolean = false):void {
			
			var graphData:UGraphData = flareVis.graphData;
			
			// the hash of rukles for that level
			var rules:Array;
			// the x-and y-values of the current fact node
			// these are needed to position the remaining nodes
			var xPosition:int = lineageNode.x;
			var yPosition:int = lineageNode.y;//lineageNode.y + 100;
			
			// a temporary node for the disjunction of grounded soft rules
			var fact:UFact;
			
			rules = lineageOr.getChildren();
			//trace("rules are : " + rules);  
		    fact = lineageOr.getFact();
		    
		    var nodeLabel:String;
		    var conf:Number;
	        var confString:String;
	        var node:NodeSprite = lineageNode;
	        var orNode:NodeSprite = lineageNode;
	        var factId:String = fact.getId().toString();
	        
	        if(rules != null && rules.length > 0) {
	        	
	        	if(drawNode) {
	        	
		        	// add the node label
		          	nodeLabel = fact.getRelationName() + "(" + fact.getFirstArgumentName() + "," 
					                  + fact.getSecondArgumentName() + ")";// [" + f.getConfidence().toFixed(2).toString() + "]";
		          	// request a node from the cache
		          	//var f_node:NodeSprite = graphData.graphCache.useLineageNode(_nodeLabel,
		          	  //      f.getConfidence().toFixed(2).toString(),f.getTruthValue());
		          	conf = lineageOr.getConf();//f.getConfidence();//Sampling.getConf(f, level);
		          	//conf = (conf <= 0.01 && conf > 0) ? 0.01 : conf; 
		          	confString = conf.toFixed(2);
		          	
		          	//trace("node label is : " + nodeLabel);
		          	//trace("node confidence is : " + confString);
		          	
		          	node = graphData.graphCache.useLineageNode(nodeLabel,confString,fact.getTruthValue());
		          	
		          	// get the corresponding edge and the source and target nodes
		          	var fact_edge:EdgeSprite = graphData.graphCache.usedEdges[fact.getId()];
		          	
		          	// add the values of the edge for the compare mode
		            if(fact_edge.data.hasOwnProperty("Old"))
		               node.data.Old = fact_edge.data.Old;
		            if(fact_edge.data.hasOwnProperty("New"))
		               node.data.New = fact_edge.data.New;
		          	
		          	// bring the lineageNode to the top center of the visualization 
					node.x = xPosition;//bounds.x + 0.5 * bounds.width;
					node.y = yPosition;//bounds.y + 10;
		          	
		          	// add this node to the data set
		            flareVis.visualization.data.addNode(node);          	
		          	
		          	_edgeLabel = fact.toStringOrFact(confString);//null;
		          	
		          	// request an edge from the cache
		          	var new_edge:EdgeSprite = graphData.graphCache.useLineageEdge(null,node,lineageNode,_edgeLabel);//[f.getId().toString()];;
		      
		            // set the node properties
		          	// the node properties are set here because we need the edge connection first
		          	setNodeProperties(node,firstLevel);
		          	//if(firstLevel)
		          	  // node.data.FL = true;
		      
	                // add a lineage tree flag
		            new_edge.data.linTree = true;
	                // set a hide flag
			        //edge.data.hide = false;
			        // at first, hide the edge
			        new_edge.visible = false;
	
	                // add this edge to the visualization
		            flareVis.visualization.data.addEdge(new_edge);  
		            
		            // add the current fact - node mapping to the mapping hash
		            // we have the id already in the hash -> add this node to
		            // this id - node array mapping
		            if(_factNodeMapping.hasOwnProperty(factId)) {
		            	(_factNodeMapping[factId] as Array).push(node);
		            }
		            else
		            	_factNodeMapping[factId] = [node];
		            node.data.factID = factId;
		            node.data.conf = confString;
		           
		        }
		        
		        if(rules != null && rules.length > 1) {  
		            
		            orNode = graphData.graphCache.useLineageNode(UStringConstants.OR,null,-1);
		            
		            // add this node to the data set
		            flareVis.visualization.data.addNode(orNode);
		            // bring the lineageNode to the top center of the visualization 
				    orNode.x = xPosition;//bounds.x + 0.5 * bounds.width;
				    orNode.y = yPosition;//bounds.y + 10;
						
					// increase the x-position to place the next node
					//xPosition += 100;
					//xPosition -= (100 * (numOfFacts-1));//(0.5 * numOfRules * 50);
					//yPosition += 100;
					  
					var disjunctEdge:EdgeSprite;
					  
		      	    //disjunctEdge = graphData.graphCache.useLineageEdge("v",node,lineageNode);//grSRNode.parentEdge;
		      	    disjunctEdge = graphData.graphCache.useLineageEdge(null,orNode,node,UStringConstants.OR); 
				  
				    // set the properties for this node
				    // it is needed here, because we need the connection to other node of the related edge		            
		            setNodeProperties(orNode,firstLevel);
				  
				    //var disjunctEdge:EdgeSprite = graphData.graphCache.useLineageEdge("v",lineageNode,disjunctNode)//grSRNode.parentEdge;
		            // add a lineage tree flag
		            disjunctEdge.data.linTree = true;
		            // set a hide flag
			        //disjunctEdge.data.hide = false;
			        // at first, hide the edge
				    disjunctEdge.visible = false;
		          
		            // add this edge to the visualization
		            flareVis.visualization.data.addEdge(disjunctEdge);
			            
		           /* for each (var andLineageOr:ULineageAnd in lineageOr.getChildren()) {
			            buildLineageLevelAnd(andLineageOr, orNode, flareVis, firstLevel);
			            //return;
		            }*/
					
					for each (var andLineageOr:ULineageAnd in lineageOr.getChildren()) {
						buildLineageLevelAnd(andLineageOr, orNode, flareVis, firstLevel, null, fact);
						//return;
					}
	        	        
		        }
		        
		        else {
		        	/*for each (var andLineage:ULineageAnd in lineageOr.getChildren()) {
			            buildLineageLevelAnd(andLineage, node, flareVis, firstLevel, confString);
			            //return;
		            }*/
					
					for each (var andLineage:ULineageAnd in lineageOr.getChildren()) {
						buildLineageLevelAnd(andLineage, node, flareVis, firstLevel, confString, fact);
						//return;
					}
		        }
		        
	            return;	
		                                			            		            
		   }
		            
		   else {
		   	
		   	  if(drawNode) {
	        	
		        	// add the node label
		          	nodeLabel = fact.getRelationName() + "(" + fact.getFirstArgumentName() + "," 
					                  + fact.getSecondArgumentName() + ")";// [" + f.getConfidence().toFixed(2).toString() + "]";
					//trace("node label = " + _nodeLabel);
		          	// request a node from the cache
		          	//var f_node:NodeSprite = graphData.graphCache.useLineageNode(_nodeLabel,
		          	  //      f.getConfidence().toFixed(2).toString(),f.getTruthValue());
		          	conf = lineageOr.getConf();//f.getConfidence();//Sampling.getConf(f, level);
		          	//conf = (conf <= 0.01 && conf > 0) ? 0.01 : conf; 
		          	confString = conf.toFixed(2);
		          	
		          	//trace("node label is : " + nodeLabel);
		          	//trace("node confidence is : " + confString);
		          	
		          	node = graphData.graphCache.useLineageNode(nodeLabel,confString,fact.getTruthValue());
		          	
		          	// get the corresponding edge and the source and target nodes
		          	fact_edge = graphData.graphCache.usedEdges[fact.getId()];
		          	
		          	// add the values of the edge for the compare mode
		            if(fact_edge.data.hasOwnProperty("Old"))
		               node.data.Old = fact_edge.data.Old;
		            if(fact_edge.data.hasOwnProperty("New"))
		               node.data.New = fact_edge.data.New;
		          	
		          	// bring the lineageNode to the top center of the visualization 
					node.x = xPosition;//bounds.x + 0.5 * bounds.width;
					node.y = yPosition;//bounds.y + 10;
		          	
		          	// add this node to the data set
		            flareVis.visualization.data.addNode(node);          	
		          	
		          	_edgeLabel = fact.toStringOrFact(confString);//null;
		          	
		          	// request an edge from the cache
		          	new_edge = graphData.graphCache.useLineageEdge(null,node,lineageNode,_edgeLabel);//[f.getId().toString()];;
		      
		            // set the node properties
		          	// the node properties are set here because we need the edge connection first
		          	setNodeProperties(node,firstLevel);
		          	//if(firstLevel)
		          	  // node.data.FL = true;
		      
	                // add a lineage tree flag
		            new_edge.data.linTree = true;
	                // set a hide flag
			        //edge.data.hide = false;
			        // at first, hide the edge
			        new_edge.visible = false;
	
	                // add this edge to the visualization
		            flareVis.visualization.data.addEdge(new_edge);  
		            
		            // add the current fact - node mapping to the mapping hash
		            // we have the id already in the hash -> add this node to
		            // this id - node array mapping
		            if(_factNodeMapping.hasOwnProperty(factId)) {
		            	(_factNodeMapping[factId] as Array).push(node);
		            }
		            else
		            	_factNodeMapping[factId] = [node];
		            node.data.factID = factId;
		            node.data.conf = confString;
		           
		            return;
		        }
		   	
		   }			          
	
           return;
		}
		
		private static function buildLineageLevelAnd(lineageAnd:ULineageAnd, lineageNode:NodeSprite, flareVis:UFlareVis
		                         , firstLevel:Boolean = false, confidenceString:String = null, fact:UFact = null):void {
			
			var graphData:UGraphData = flareVis.graphData;
			
			var grSRule:UGroundedSoftRule = lineageAnd.getGroundedRule();
			var ugSRule:USoftRule = lineageAnd.getRule();
			
			//if((ugSRule = lineageAnd.getRule()) == null && (grSRule = lineageAnd.getGroundedRule()) == null)
			//if((ugSRule = lineageAnd.getRule()) == null || (grSRule = lineageAnd.getGroundedRule()) == null) {
			if(lineageAnd.getChildren() == null || lineageAnd.getChildren().length == 0) {	
			   //return;
		          	
		          	/*var node:NodeSprite = 
		          	    graphData.graphCache.useLineageNode(UStringConstants.DB_AR 
		          	    + ((confidenceString == null) 
		          	    ? (lineageNode.getOutNode(0).data.hasOwnProperty("conf") ? lineageNode.getOutNode(0).data.conf : "") 
		          	    : confidenceString),null,-1);*/
					
				/*var node:NodeSprite = 
					graphData.graphCache.useLineageNode(UStringConstants.DB_AR 
						+ ((confidenceString == null) 
							? (fact != null ? fact.getConfidence().toFixed(2).toString() : "") 
							: confidenceString),null,-1);*/
				
				var node:NodeSprite = 
					graphData.graphCache.useLineageNode(UStringConstants.DB_AR 
						+ ((fact == null) 
							? (lineageNode.getOutNode(0).data.hasOwnProperty("conf") ? lineageNode.getOutNode(0).data.conf : "")
							: fact.getConfidence().toFixed(2).toString()),null,-1);
		          	
		          	// add the values of the edge for the compare mode
		            if(lineageNode.data.hasOwnProperty("Old"))
		               node.data.Old = lineageNode.data.Old;
		            if(lineageNode.data.hasOwnProperty("New"))
		               node.data.New = lineageNode.data.New;
		          	
		          	// bring the lineageNode to the top center of the visualization 
					node.x = lineageNode.x;//bounds.x + 0.5 * bounds.width;
					node.y = lineageNode.y;//bounds.y + 10;
		          	
		          	// add this node to the data set
		            flareVis.visualization.data.addNode(node);          	
		          	
		          	// request an edge from the cache
		          	var new_edge:EdgeSprite = graphData.graphCache.useLineageEdge("DB or AR",node,lineageNode,"in <b>Database</b> or <b>Arithmetic</b>");//[f.getId().toString()];;
		      
		            // set the node properties
		          	// the node properties are set here because we need the edge connection first
		          	setNodeProperties(node,firstLevel);
		          	// set a DB flag for the node to be recognized by the Expand Control
		          	node.data.DB = true;
		          	//if(firstLevel)
		          	  // node.data.FL = true;
		      
	                // add a lineage tree flag
		            new_edge.data.linTree = true;
	                // set a hide flag
			        //edge.data.hide = false;
			        // at first, hide the edge
			        new_edge.visible = false;
	
	                // add this edge to the visualization
		            flareVis.visualization.data.addEdge(new_edge);  
		            
		            // add the current fact - node mapping to the mapping hash
		            // we have the id already in the hash -> add this node to
		            // this id - node array mapping
		            if(_factNodeMapping.hasOwnProperty(lineageNode.data.factID)) {
		            	(_factNodeMapping[lineageNode.data.factID] as Array).push(node);
		            }
		            else
		            	_factNodeMapping[lineageNode.data.factID] = [node];
		            node.data.factID = lineageNode.data.factID;
		           
		            return;
			}
			
			else {
				// a temporary node for the disjunction of grounded soft rules
				//var node:NodeSprite;
				//var fact:UFact;	     		     	          
		          var grSRNode:NodeSprite;
		          
		          grSRNode = graphData.graphCache.useLineageNode(UStringConstants.AND,null,-1);//new NodeSprite();
		          
		          // add this node to the data set
		          flareVis.visualization.data.addNode(grSRNode);
		          
		          // bring the lineageNode to the top center of the visualization 
				  grSRNode.x = lineageNode.x;//bounds.x + 0.5 * bounds.width;
				  grSRNode.y = lineageNode.y;//bounds.y + 10;					
				 
		          if(ugSRule != null)
		             _edgeLabel = ugSRule.toString();// + "  \n@ [" + lineageAnd.getConf().toFixed(4) + "]";
		          else
		             _edgeLabel = grSRule.toString() + "<br/>confidence: " + lineageAnd.getConf().toFixed(4);//" @ [" + lineageAnd.getConf().toFixed(4) + "]";
		          //if(grSRule != null)
		            // _edgeLabel += "\n" + grSRule.toString() + " @ [" + lineageAnd.getConf().toFixed(4) + "]";
		          
		          // request an edge from the cache and set the label,
		          // as well as the source and target nodes
		          // source node = lineageNode = father node
		          // and target node = grSRNode = child node
		          var e:EdgeSprite = graphData.graphCache.useLineageEdge(_edgeLabel,grSRNode,lineageNode)//grSRNode.parentEdge;
		          
		          // set the properties for this node
		          // node properties are set here because we need the edge connection first	          
		          setNodeProperties(grSRNode,firstLevel);
		          
		          // add a lineage tree flag
		          e.data.linTree = true;
		          // set a hide flag
			      //e.data.hide = false;
			      // at first, hide the edge
			      e.visible = false;
		          
		          // add this edge to the visualization
		          flareVis.visualization.data.addEdge(e);
		          
		          for each (var linOr:ULineageOr in lineageAnd.getChildren()) {
		          	 buildLineageLevelOr(linOr, grSRNode, flareVis); 
		          }
	          
	        }
		          
            return;
		}
		
		/**
		 * Produces a lineage graph for the given factID (fact/edge) and the specified graph data.
		 * 
		 * @param factID the id of the fact whose linerage information should be used to build a lineage tree.
		 * @param flareVis the UFlareVis instance to use for the construction.
		 * */
		public static function produceLineageGraph(factID:String, flareVis:UFlareVis, t:Transitioner = null):NodeSprite {
			
			_fullLineageMode = false;
			
			var graphData:UGraphData = flareVis.graphData;
			//var data:Data = flareVis.visualization.data;
			
			var fact:UFact = graphData.facts[factID];
			// get the corresponding edge and the source and target nodes
			var edge:EdgeSprite = graphData.graphCache.usedEdges[factID];
			var sourceNode:NodeSprite = edge.source;
			_sourceNode = sourceNode;
			var targetNode:NodeSprite = edge.target;
			_targetNode = targetNode;
			
			// do not show these nodes in lineage mode
			//sourceNode.data.context = false;
			//targetNode.data.context = false;		
			
			// hide all edges
			/*
			flareVis.visualization.data.edges.visit(
			      function(es:EdgeSprite):void {
			      	    // we deal with the current lineage edge in a separate step
			   	        //if(es != edge) {
			   	          //if(t != null)
			   	            // t.$(es).visible = false;
			   	          //else
			   	             es.visible = false;
			   	        //}
			      }
		    );
		    */
			
			// set the label for the root lineage node
			_nodeLabel = edge.data.label + "(" + sourceNode.data.label 
			                    + "," + targetNode.data.label + ")";// [" + fact.getConfidence().toFixed(2).toString() + "]";
			
			// request the root fact node
			var conf:Number = fact.getConfidence();//Sampling.getConf(fact, fact.min_level);
          	//conf = (conf <= 0.01 && conf > 0) ? 0.01 : conf; 
          	var confString:String = conf.toFixed(2);
          	//var f_node:NodeSprite = graphData.graphCache.useLineageNode(_nodeLabel,confString,f.getTruthValue());
			//var lineageNode:NodeSprite = graphData.graphCache.useLineageNode(_nodeLabel,fact.getConfidence().toFixed(2).toString(),fact.getTruthValue());//factNodes[factID];
			var lineageNode:NodeSprite = graphData.graphCache.useLineageNode(_nodeLabel,confString,fact.getTruthValue());//factNodes[factID];
			
			
			// set the basic node properties
			setRootNodeProperties(lineageNode);//, convexHull);
			// connect the existing edges of both fact connected nodes
			// to the new fact node
			//collectTempLineageEdges(sourceNode,targetNode,flareVis);
			
			// add the fact/lineage node temporarily
			flareVis.visualization.data.addNode(lineageNode);
			
			//var x_pos:Number = (sourceNode.x + targetNode.x) / 2;
			//var y_pos:Number = (sourceNode.y + targetNode.y) / 2;
			
			//sourceNode.visible = false;
			//targetNode.visible = false;
			
			lineageNode.x = flareVis.radialLayout.layoutAnchor.x;//(sourceNode.x + targetNode.x) / 2;
			lineageNode.y = flareVis.radialLayout.layoutAnchor.y;//(sourceNode.y + targetNode.y) / 2;
			
			lineageNode.data.x = lineageNode.x;
			lineageNode.data.y = lineageNode.y;
			
			// add the values of the edge for the compare mode
			// add the values of the edge for the compare mode
            if(edge.data.hasOwnProperty("Old"))
               lineageNode.data.Old = edge.data.Old;
            if(edge.data.hasOwnProperty("New"))
               lineageNode.data.New = edge.data.New;
			
			//buildLineageGraph(factID, lineageNode, flareVis);
			//buildLineageLevel(factID, lineageNode, flareVis);
			buildLineageLevelOr(fact.getLineage(),lineageNode,flareVis,false,true);
			
			// now, we return the lineage root node
			return lineageNode;
		}
		
		private static function buildLineageLevel(fact:UFact, lineageNode:NodeSprite, flareVis:UFlareVis):void {
			
			var graphData:UGraphData = flareVis.graphData;
			
			// the hash of rukles for that level
			var rules:Array;
			// the x-and y-values of the current fact node
			// these are needed to position the remaining nodes
			var xPosition:int = lineageNode.x;
			var yPosition:int = lineageNode.y;//lineageNode.y + 100;
			
			// a temporary node for the disjunction of grounded soft rules
			var disjunctNode:NodeSprite;
			//var groundedHardRule:UGroundedHardRule;
			//var groundedHardRuleString:String;
			
			//trace("size = fact.getLineage().getAllLineageFacts(fact,false).size() : " 
			  //   + fact.getLineage().getAllLineageFacts(fact,true).size());
			rules = fact.getLineage().getChildren();//getRulesAtLevel(level);
			//trace("rules are : " + rules);  
		    if (rules != null) {
		    	
		     // how many rules
		     var numOfRules:int = rules.length;
		     var numOfFacts:int = 0;
		     // recompute the x-position of the leftmost child node
		     // set the first child node left below the father fact node
		     // we take the ratio of number of grounded rules times 0.5 times 50 
		     //(given distance to the n ext node)
		     //xPosition -= (100 * (numOfRules-1));//(0.5 * numOfRules * 50);
		     
		     
		      // we have more than one  single grounded soft rule
	          // -> we need a intermediate disjunct node	
	          // -> remap the lineage node reference
	          if (numOfRules > 1) {
	          	  
	              disjunctNode = graphData.graphCache.useLineageNode("v",null,1);
	              //trace("Hallo Or-Lineage!");
	              
	              // add this node to the data set
	              flareVis.visualization.data.addNode(disjunctNode);
	              // bring the lineageNode to the top center of the visualization 
				  disjunctNode.x = xPosition;//bounds.x + 0.5 * bounds.width;
				  disjunctNode.y = yPosition;//bounds.y + 10;
					
				  // increase the x-position to place the next node
				  //xPosition += 100;
				  //xPosition -= (100 * (numOfFacts-1));//(0.5 * numOfRules * 50);
				  //yPosition += 100;
				  
				  var disjunctEdge:EdgeSprite;
				  
				  //groundedHardRule = fact.getGroundedHardRule();
	          	  /*
	          	  if(groundedHardRule != null) {
	          	  	 //trace("groundedHardRule.toString() : " + groundedHardRule.toString());
	          	  	 groundedHardRuleString = groundedHardRule.toString();
	          	  	 disjunctEdge = graphData.graphCache.useLineageEdge("v",disjunctNode,lineageNode,groundedHardRuleString);//grSRNode.parentEdge;
	          	  }
	          	  else
	          	  */
	          	  //disjunctEdge = graphData.graphCache.useLineageEdge("v",disjunctNode,lineageNode);//grSRNode.parentEdge;
	          	  disjunctEdge = graphData.graphCache.useLineageEdge(null,disjunctNode,lineageNode); 
				  
				  // set the properties for this node
				  // it is needed here, because we need the connection to other node of the related edge
	              setNodeProperties(disjunctNode);
				  
				  //var disjunctEdge:EdgeSprite = graphData.graphCache.useLineageEdge("v",lineageNode,disjunctNode)//grSRNode.parentEdge;
		          // add a lineage tree flag
		          disjunctEdge.data.linTree = true;
		          // set a hide flag
			      //disjunctEdge.data.hide = false;
			      // at first, hide the edge
				  disjunctEdge.visible = false;
		          
		          // add this edge to the visualization
		          flareVis.visualization.data.addEdge(disjunctEdge);
		          
		          // remap the OR node to the lineage node reference for later use
		          //lineageNode = disjunctNode;
	          }
		     
		     	
		      var i:int = 0;
		      var rule:UGroundedSoftRule;
		      //for each (var rule:UGroundedSoftRule in rules) {
		      for each (var ruleLineage:ULineageAnd in rules) {
		        
		        rule = ruleLineage.getGroundedRule();
		        
		        if (rule.getHead() != null && rule.getHead().getId() == fact.getId()) {//.hashCode() == fact.hashCode()) {
		        	     	
		          numOfFacts = rule.getFactSet().length;		          
		          var grSRNode:NodeSprite;
		          var ruleSize:int = rule.size();
		          //trace("ruleSize : " + ruleSize);
		          
		          if (ruleSize > 0) {
		          
			          grSRNode = graphData.graphCache.useLineageNode("^",null,1);//new NodeSprite();
			          
			          // add this node to the data set
			          flareVis.visualization.data.addNode(grSRNode);
			          
			          // bring the lineageNode to the top center of the visualization 
					  grSRNode.x = xPosition;//bounds.x + 0.5 * bounds.width;
					  grSRNode.y = yPosition;//bounds.y + 10;
						
					  // increase the x-position to place the next node
					  //xPosition += 100;
					  //xPosition -= (100 * (numOfFacts-1));//(0.5 * numOfRules * 50);
					  //yPosition += 100;
						
			          _edgeLabel = rule.getSoftRule().toString() + " [" + rule.getWeight().toFixed(4) + "]";
			          
			          // request an edge from the cache and set the label,
			          // as well as the source and target nodes
			          // source node = lineageNode = father node
			          // and target node = grSRNode = child node
			          var e:EdgeSprite;
			          if (numOfRules > 1) {
			          	e = graphData.graphCache.useLineageEdge(_edgeLabel,grSRNode,disjunctNode)//grSRNode.parentEdge;
			          }
			          else
			            e = graphData.graphCache.useLineageEdge(_edgeLabel,grSRNode,lineageNode)//grSRNode.parentEdge;
			          
			          // set the properties for this node
			          // node properties are set here because we need the edge connection first
			          setNodeProperties(grSRNode);
			          
			          // add a lineage tree flag
			          e.data.linTree = true;
			          // set a hide flag
				      //e.data.hide = false;
				      // at first, hide the edge
				      e.visible = false;
			          
			          // add this edge to the visualization
			          flareVis.visualization.data.addEdge(e);
			          
			          for (var pos:int =0; pos<rule.size(); pos++) { //var f:UFact in rule.getFactSet()) {
		          
			            var f:UFact = rule.getFactAtPosition(pos);//getFactSet()
			          	// retrive the node to the current fact
			          	
			          	// add the node label
			          	_nodeLabel = f.getRelationName() + "(" + f.getFirstArgumentName() + "," 
						                  + f.getSecondArgumentName() + ")";// [" + f.getConfidence().toFixed(2).toString() + "]";
			          	// request a node from the cache
			          	//var f_node:NodeSprite = graphData.graphCache.useLineageNode(_nodeLabel,
			          	  //      f.getConfidence().toFixed(2).toString(),f.getTruthValue());
			          	var conf:Number = f.getConfidence();//Sampling.getConf(f, level);
			          	//conf = (conf <= 0.01 && conf > 0) ? 0.01 : conf; 
			          	var confString:String = conf.toFixed(2);
			          	var f_node:NodeSprite = graphData.graphCache.useLineageNode(_nodeLabel,confString,f.getTruthValue());
			          	
			          	// get the corresponding edge and the source and target nodes
			          	var f_edge:EdgeSprite = graphData.graphCache.usedEdges[f.getId()];
			          	
			          	// add the values of the edge for the compare mode
			            if(f_edge.data.hasOwnProperty("Old"))
			               f_node.data.Old = f_edge.data.Old;
			            if(f_edge.data.hasOwnProperty("New"))
			               f_node.data.New = f_edge.data.New;
			          	
			          	// bring the lineageNode to the top center of the visualization 
						f_node.x = xPosition;//bounds.x + 0.5 * bounds.width;
						f_node.y = yPosition;//bounds.y + 10;
						
						// increase the x-position to place the next node
						//xPosition += 150;
			          	
			          	// add this node to the data set
			            flareVis.visualization.data.addNode(f_node);          	
			          	
			          	// set the label for the current edge
			          	/*
			          	if(pos == 0) 
			          	   _edgeLabel = "<-";
			          	else
			          	   _edgeLabel = "^";
			          	*/
			          	_edgeLabel = null;
			          	
			          	// request an edge from the cache
			          	var edge:EdgeSprite = graphData.graphCache.useLineageEdge(_edgeLabel,f_node,grSRNode);//[f.getId().toString()];;
			      
			            // set the node properties
			          	// the node properties are set here because we need the edge connection first
			          	setNodeProperties(f_node);
			      
		                // add a lineage tree flag
			            edge.data.linTree = true;
		                // set a hide flag
				        //edge.data.hide = false;
				        // at first, hide the edge
				        edge.visible = false;
		
		                // add this edge to the visualization
			            flareVis.visualization.data.addEdge(edge); 
						
			          	//lineageTree.addChild(grSRNode,f_node);
			          	buildLineageLevel(f, f_node, flareVis);
			          }
		          
		          }
		         
		          
		         // j++;
		        }
		        i++;
		      }
		    } 

            return;
		}
		
		private static function setRootNodeProperties(node:NodeSprite):void {
			
			// set the shape
			node.shape = UShapes.ROUNDED_RECT;
			node.data.rectBounds = [node.x,node.y,1,1];
			node.size = 1;
			
			// at first, hide the nodes
			//node.visible = false;
			node.expanded = true;
			
			// copy the node label for later usage
			node.data[UStringConstants.LABEL_COPY] = node.data.label;
			
			// set the flag that indicates if this node is
			// part of the real lineage tree
			node.data.linTree = true;
			
			// set a flag to indicate that this node belongs to the first level of the tree
			node.data.FL = true;
			
			node.visible = false;
		}
		
		private static function setNodeProperties(node:NodeSprite, firstLevel:Boolean = false):void {
			
			// set the shape
			node.shape = UShapes.ROUNDED_RECT;
			node.data.rectBounds = [node.x,node.y,1,1];
			node.size = 1;
			node.visible = false;
			var prevNode:NodeSprite;
			
			/*
			 if(node.data[UStringConstants.LABEL] == "^" || node.data[UStringConstants.LABEL] == "v") {
			    node.expanded = false;	
			    // copy the node label for later usage
			    node.data[UStringConstants.LABEL_COPY] = node.data[UStringConstants.LABEL];
			    // now, overwrite the current label with " ... "
			    node.data[UStringConstants.LABEL] = UStringConstants.THREE_DOTS;
			} 
			*/
			var parentNode:NodeSprite;
			// at first, hide the nodes
			//node.visible = false; node.data.FL
			if(node.data[UStringConstants.LABEL] == UStringConstants.AND || node.data[UStringConstants.LABEL] == UStringConstants.OR) {
				parentNode = node.getOutNode(0);
				//trace("parent node is : " + parentNode.data.label);
				if(parentNode != null && (parentNode.data.hasOwnProperty("FL") || firstLevel)) {
					// set the first level flag
					node.data.FL = true;
					node.expanded = true;	
					// copy the node label for later usage
				    node.data[UStringConstants.LABEL_COPY] = node.data[UStringConstants.LABEL];
				    // now, copy the node label for later usage
				    node.data[UStringConstants.LABEL] = node.data[UStringConstants.LABEL];
				}
				else {
					/*
					if(parentNode != null && (parentNode.data.hasOwnProperty("FL2") && _fullLineageMode)) {
						node.expanded = true;	
						// copy the node label for later usage
					    node.data[UStringConstants.LABEL_COPY] = node.data[UStringConstants.LABEL];
					    // now, copy the node label for later usage
					    node.data[UStringConstants.LABEL] = node.data[UStringConstants.LABEL];
					    for (var i:int=0; i<node.inDegree;i++) {
					    	prevNode = node.getInNode(i);
					    	if(node.data.hasOwnProperty("DB")) {
					    		trace("found node");
					    		prevNode.data.DBVisNode = true;
					    		prevNode.expanded = true;	
								// copy the node label for later usage
							    prevNode.data[UStringConstants.LABEL_COPY] = prevNode.data[UStringConstants.LABEL];
							    // now, copy the node label for later usage
							    prevNode.data[UStringConstants.LABEL] = prevNode.data[UStringConstants.LABEL];
					    	}
					    }
					}
					*/
					//else {
					    if(node.data.hasOwnProperty("DBVisNode"))
						   return;
					    node.expanded = false;	
					    // copy the node label for later usage
					    node.data[UStringConstants.LABEL_COPY] = node.data[UStringConstants.LABEL];
					    // now, overwrite the current label with " ... "
					    node.data[UStringConstants.LABEL] = UStringConstants.THREE_DOTS;
				    //}
				} 
			}
			// for the regular fact nodes, we do not set the first level flag, even
			// if they belong to the first level -> here ends the first level flag chain
			else {
				parentNode = node.getOutNode(0);
				//trace("parent node normal is : " + parentNode.data.label);
				if(parentNode != null && (parentNode.data.hasOwnProperty("FL") || firstLevel)) {
					// set the first level flag
					node.data.FL2 = true;
					node.expanded = true;	
					// copy the node label for later usage
				    node.data[UStringConstants.LABEL_COPY] = node.data[UStringConstants.LABEL];
				    // now, copy the node label for later usage
				    node.data[UStringConstants.LABEL] = node.data[UStringConstants.LABEL];
				}
				else {
					if(parentNode != null && parentNode.data.hasOwnProperty("FL2")) {
						node.expanded = true;	
						// copy the node label for later usage
					    node.data[UStringConstants.LABEL_COPY] = node.data[UStringConstants.LABEL];
					    // now, copy the node label for later usage
					    node.data[UStringConstants.LABEL] = node.data[UStringConstants.LABEL];
					    for (var k:int=0; k<node.inDegree;k++) {
					    	prevNode = node.getInNode(k);
					    	if(node.data.hasOwnProperty("DB")) {
					    		prevNode.expanded = true;	
								// copy the node label for later usage
							    prevNode.data[UStringConstants.LABEL_COPY] = prevNode.data[UStringConstants.LABEL];
							    // now, copy the node label for later usage
							    prevNode.data[UStringConstants.LABEL] = prevNode.data[UStringConstants.LABEL];
					    	}
					    }
					}
					else {
						if(node.data.hasOwnProperty("DBVisNode"))
						   return;
					    node.expanded = false;	
					    // copy the node label for later usage
					    node.data[UStringConstants.LABEL_COPY] = node.data[UStringConstants.LABEL];
					    // now, overwrite the current label with " ... "
					    node.data[UStringConstants.LABEL] = UStringConstants.THREE_DOTS;
				    }
				} 
			}
			
			// set a hide flag
			//node.data.hide = false;
			// set the flag that indicates if this node is
			// part of the real lineage tree
			node.data.linTree = true;
			
		}
		
	}
}