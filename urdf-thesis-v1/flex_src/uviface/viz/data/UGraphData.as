package uviface.viz.data
{
	import flare.display.TextSprite;
	import flare.vis.data.Data;
	import flare.vis.data.DataSprite;
	import flare.vis.data.EdgeSprite;
	import flare.vis.data.NodeSprite;
	
	import flash.events.MouseEvent;
	
	import urdf.api.UFact;
	import urdf.api.ULineageAbstract;
	import urdf.api.ULineageAnd;
	import urdf.api.ULineageOr;
	import urdf.api.UQueryResult;
	
	import util.UStringUtil;
	
	import uviface.viz.control.UGraphControls;
	import uviface.viz.operator.UNodeRenderer;
	import uviface.viz.operator.UShapes;
	import uviface.viz.util.UStringConstants;
	
	/** This class represents the graph data that is built and temporarily stored 
	 * to be available to fill the flare graph data. */
	public class UGraphData
	{
		private static const CLICK_RESULT_STRING:String = "Show only all facts of the specified result set!";
		private static const CLICK_DEPENDENCY_STRING:String = "Show only all facts of the specified dependency graph!";
		private static const CLICK_SINGLE_RESULT_LINEAGE_STRING:String = "Show the explanation tree of the selected answer!";
		private static const CLICK_COMPLETE_RESULT_LINEAGE_STRING:String = "Show the explanation tree for all answers of the query!";
		
		// -------------------------------------------------------------------------
		// this structures are used for the force layout
		//private var _forceNodeMap:Object;
		//private var _forceEdgeList:Array;
		// -------------------------------------------------------------------------
		
		/** The cache for the nodes and edges. */
		private var _graphCache:UCache;
		
		private var _temporaryHash:Object = new Object();
		
		private var _currentResultString:String;
		
		private var _allFactsRequestNode:NodeSprite;
		
		private var _allFactsRequestEdge:EdgeSprite;
		
		/** The map that shows the produced query results. */
		private var _queryResults:Object;// = new Object();
		private var _queryResultsMapping:Array;// = new Object();
		
		/** The factID - fact mapping. */
		private var _facts:Object;
		/** The fact name - fact id mapping. */
		private var _factNamesAndIDs:Array;
		/** The fact name - lineage available flag mapping. */
		private var _factLineageAvalaibilityMap:Array;
		
		/** The highest current node degree. */
		private static var _highestNodeDegree:int = 0;
		
		/** Counts the number of currently visualized sub graphs within the main visualization. */
		private var _compareResultNumber:int = 0;
		
		/** The flag to check if at least one node of the new query result already existed in the visualization. */
		private var _nodesInCommon:Boolean = false;
		
		/** The node in the unconnected remaining part of the visualization graph with the highest degree. */
		private var _lowestDegreeForeignNode:NodeSprite;
		/** The node in the current part of the visualization graph with the highest degree. */
		private var _lowestDegreeCurrentNode:NodeSprite;
		
		private var _nodeInUse:Boolean = false;
		private var _edgeInUse:Boolean = false;
		
		private var _subjectNode:NodeSprite;
		private var _objectNode:NodeSprite;
		private var _predicateEdge:EdgeSprite;
		
		/** 
		 * The constructor that initializes the graph data. 
		 * 
		 * @param bounds 
		 *     the bounds to use for this graph data structure.
		 * */
		public function UGraphData()
		{
			//_resultList.push(UStringConstants.DEPENDENCY_GRAPH);//{label: "Dependency Graph"});	
			_queryResults = new Object();
			_queryResultsMapping = new Array();	
			_graphCache = new UCache();
			_facts = new Object()
			_factNamesAndIDs = new Array();
			_factLineageAvalaibilityMap = new Array();
			//_forceNodeMap = new Object();
			//_forceEdgeList = new Array();
		}
		
		/*
		public function get forceEdgeList():Array {
			return _forceEdgeList;
		}
		*/
		
		/** 
		 * Delivers the query result used for this graph data structure.
		 * 
		 * @return the query result the query result used for this graph data structure.
		 *  */
		public function get queryResults():Object {
			return _queryResults;
		}
		
		public function get currentResultString():String {
			return _currentResultString;
		}	
		
		/** 
		 * Delivers all factID -> fact mappings produced. 
		 * 
		 * @return all factID -> fact mappings produced.
		 * */
		public function get facts():Object {
			return _facts;
		}
		
		/** 
		 * Delivers all fact name -> fact id mappings produced. 
		 * 
		 * @return all fact name -> fact id mappings produced.
		 * */
		public function get factNamesAndIDs():Array {
			return _factNamesAndIDs;
		}
		
		/** 
		 * Delivers all fact name -> lineage information mappings. 
		 * 
		 * @return all fact name -> lineage information mapping.
		 * */
		public function get factLineageAvalaibilityMap():Array {
			return _factLineageAvalaibilityMap;
		}
		
		/**
		 * Delivers the list of produced query results (objects containing only a label 
		 * with the result name).
		 * 
		 * @return the result list.
		 * */
		public function get resultList():Array {
			//return _resultList;
			/*
			var queryResult:String;
			var queryResultList:Array = new Array();
			for (queryResult in _queryResults) {
				queryResultList.push(queryResult);
			}
			return queryResultList;
			*/
			return _queryResultsMapping;
		}
		
		/**
		 * Delivers the currently highest node degree.
		 * 
		 * @return the currently highest node degree.
		 * */
		public static function get highestNodeDegree():int {
			return _highestNodeDegree;
		}
		
		/**
		 * Sets the currently highest node degree.
		 * 
		 * @return the currently highest node degree.
		 * */
		public function set highestNodeDegree(highestNodeDegree:int):void {
			_highestNodeDegree = highestNodeDegree;
		}
		
		public function get graphCache():UCache {
			return _graphCache;
		}
		
		/** 
		 * Produces the default graph data, using the internally stored query result. 
		 * 
		 * @param data
		 *     the Data instance to fill
		 * @param queryResult 
		 *     the query result to use for this graph data structure.
		 * */
		public function produceGraphData(data:Data, queryResult:UQueryResult = null, queryString:String = null):Array {
		     
		    if(queryResult == null)
		       throw new Error("No query result available !!");
		       
		    // -------------------------------------------------------------------------------------   
		    // we have to clear the force layout structures
		    //clearForceLayoutStructures();
		    // -------------------------------------------------------------------------------------
		    
		    var queryResults:Array = queryResult.getResultLineage();//queryResult.getQueryResults();
		    //var lineageFact:ULineageOr;
		    //var children:Array;
		    //trace("result size is : " + queryResults.length.toString());
		    //var dependencyGraph:Array = buildNewDependencyGraph(queryResults);//queryResult.getGlobalDependencyGraph().getFactSet();
		    var dependencyGraph:Array = queryResult.getGlobalDependencyGraph().getFactSet();
		    var dependencyGraphCluster:Array = new Array();//new DataList(UStringConstants.DEPENDENCY_GRAPH);
		    var queryResultString:String;
		    
		    if(queryString == null)
		       queryResultString = UStringConstants.DEPENDENCY_GRAPH + " " + _compareResultNumber.toString();
		    else
		       queryResultString = UStringConstants.DEPENDENCY_GRAPH + " " + _compareResultNumber.toString() + " - " + queryString;
		    
		    var queryString:String = queryString.substring(queryString.lastIndexOf(": "));  
		    var resultLabel:String = "Query Title:  " + queryString;// + " <- click to select result"; 

            // the map to hold the mappings of fact informations for the current dependency graph 
		    var dependencyGraphMapping:Object = {label: queryResultString, 
		            children: [{label: '', labelButton: CLICK_DEPENDENCY_STRING, key: UStringConstants.QUERY_RESULT, value: queryResultString, clickable: true}, 
		                 {label: resultLabel}
		                 , {label: 'Type of Query:  Regular Query'}, {label: 'Number of contained facts:  '}
		                 , {label: 'List of contained facts:  ', children: []}]};

            // ----------------------------------------------------
            // first, produce all distinct nodes and edges from the global dependency graph
		    for each (var fact:UFact in dependencyGraph) {
		    		processFact(fact,data,dependencyGraphCluster,dependencyGraphMapping,false);//dependencyGraphCluster,factGraphCluster);
		    }
		    
		    // add the number of contained facts
		    (dependencyGraphMapping.children as Array)[3].label = (dependencyGraphMapping.children as Array)[3].label 
		                                                        + ((dependencyGraphMapping.children as Array)[4].children as Array).length as String;
		    
		    // clear the temporary hash structure
		    clearTemporaryHash();
		    
		    // add the current dependency graph cluster to the hash of query results
		    _queryResults[queryResultString] = dependencyGraphCluster;
		    // add the current dependency graph mapping to the mapping map
		    _queryResultsMapping.push(dependencyGraphMapping);
		    
		    // set the id string for the query result
		    if(queryString == null)
		       queryResultString = UStringConstants.RESULT + " " + _compareResultNumber.toString();
		    else
		       queryResultString = UStringConstants.RESULT + " " + _compareResultNumber.toString() + " - " + queryString;		    
		    
		    resultLabel = "Query Title:  " + queryString;// + " <- click to select result";
		    
		    var queryResultCluster:Array = new Array();//new DataList(UStringConstants.DEPENDENCY_GRAPH);
		    var length:String = 'Number of contained results:  ' + queryResults.length;
		    // the map to hold the mappings of fact informations for the current query result 
		    var queryResultMapping:Object = {label: queryResultString, 
		            children: [{label: '', labelButton: CLICK_RESULT_STRING, key: UStringConstants.QUERY_RESULT, value: queryResultString, clickable: true}
		                 ,{label: '', labelButton: CLICK_COMPLETE_RESULT_LINEAGE_STRING, key: UStringConstants.COMPLETE_RESULT_LINEAGE
		                    , value: queryResults, query: queryString, clickable: true}
		                 ,{label: resultLabel}
		                 , {label: 'Type of Query:  Regular Query'}, {label: length}]};
		                 //, {label: 'List of contained facts:  ', children: []}]};
		    
		    /*
		    for each (var factSet:UFactSet in queryResults) {
		    	buildDataCluster(factSet, _compareResultNumber, queryResultCluster, queryResultMapping);
		    }
		    */
		    for each (var lineageAnd:ULineageAnd in queryResults) {
		    	buildDataCluster(lineageAnd, _compareResultNumber, queryResultCluster, queryResultMapping);
		    }
		    
		    // add the number of contained facts
		    //(queryResultMapping.children as Array)[2].label = (queryResultMapping.children as Array)[2].label 
		      //                                                  + ((queryResultMapping.children as Array)[3].children as Array).length as String;
		    
		    // add the current query result to the array of result clusters
		    _queryResults[queryResultString] = queryResultCluster;
		    // add the current query result mapping to the array of query result mappings
		    _queryResultsMapping.push(queryResultMapping);
		    // set the current query result to use by the visualization
		    _currentResultString = queryResultString;
		    
		    //trace("size of query results: " + queryResultCluster.length);
		    
		    // clear the temporary hash structure
		    clearTemporaryHash();
		    
		    // in crease the counter for the compare result number
		    _compareResultNumber++;
		    
		    // if we have more facts available in the result -> set the event for the special node
		    if(queryResult.getMoreFactsAvailable()) {
		       
		       // add the new node
		       addAllFactsRequestNode(data,queryResult.getTotalNumberOfFacts());
		       // get the highest degree node fro the current query result cluster 
		       var node:NodeSprite = requestHighestDegreeNode(queryResultString);
		       // now, connect both nodes
		       addAllFactsRequestEdge(data,node);
		    }
		    
		    // return the array that represents the current query result 
		    return queryResultCluster;
		}
		
		/** 
		 * Produces the default graph data, using the internally stored query result. 
		 * 
		 * @param data
		 *     the Data instance to fill
		 * */
		public function loadGraphData(data:Data, queryResult:UQueryResult = null, compareMode:Boolean = false, 
		                               queryString:String = null, dataLoadNode:NodeSprite = null):Array {
		     
		    if(queryResult == null)
		       throw new Error("No query result available !!");
		    
		    var queryResults:Array = queryResult.getResultLineage();//queryResult.getQueryResults();
		    var lineageFact:ULineageOr;
		    var children:Array;
		    //trace("result size is : " + queryResults.length.toString());
		    //var dependencyGraph:Array = buildNewDependencyGraph(queryResults);//queryResult.getGlobalDependencyGraph().getFactSet();
		    var dependencyGraph:Array = queryResult.getGlobalDependencyGraph().getFactSet();
		    
		    // reset the nodes in comon flag
		    _nodesInCommon = false;
		    // check for nodes in common bewteen the current graph data and the new dependency graph
		    checkNodesInCommon(dependencyGraph);

            var dependencyGraphCluster:Array = new Array();//new DataList(UStringConstants.DEPENDENCY_GRAPH);
            var queryResultString:String;
            
		    if(queryString == null)
		       queryResultString = UStringConstants.DEPENDENCY_GRAPH + " " + _compareResultNumber.toString();
		    else
		       queryResultString = UStringConstants.DEPENDENCY_GRAPH + " " + _compareResultNumber.toString() + " - " + queryString;

            var queryString:String = queryString.substring(queryString.lastIndexOf(": "));  
            var resultLabel:String = "Query Title:  " + queryString;// + " <- click to select result";

            // the map to hold the mappings of fact informations for the current dependency graph 
		    var dependencyGraphMapping:Object = {label: queryResultString, 
		            children: [{label: '', labelButton: CLICK_DEPENDENCY_STRING, key: UStringConstants.QUERY_RESULT, value: queryResultString, clickable: true},
		                 {label: resultLabel}
		                 , {label: 'Type of Query:  Data Loading (Exploration)'}, {label: 'Number of contained facts:  '}
		                 , {label: 'List of contained facts:  ', children: []}]};

            // ----------------------------------------------------
            // first, produce all distinct nodes and edges from the global dependency graph
		    for each (var fact:UFact in dependencyGraph) {
		    		processFact(fact,data,dependencyGraphCluster,dependencyGraphMapping,compareMode);//dependencyGraphCluster,factGraphCluster);
		    }
		    
		    // add the number of contained facts
		    (dependencyGraphMapping.children as Array)[3].label = (dependencyGraphMapping.children as Array)[3].label 
		                                                        + ((dependencyGraphMapping.children as Array)[4].children as Array).length as String;
		    
		    // clear the temporary hash structure
		    clearTemporaryHash();
		    
		    // add the current dependency graph cluster to the hash of query results
		    _queryResults[queryResultString] = dependencyGraphCluster;
		    // add the current dependency graph mapping to the mapping map
		    _queryResultsMapping.push(dependencyGraphMapping);
		    
		    // set the id string for the query result
		    if(queryString == null)
		       queryResultString = UStringConstants.RESULT + " " + _compareResultNumber.toString();
		    else
		       queryResultString = UStringConstants.RESULT + " " + _compareResultNumber.toString() + " - " + queryString;	
		    
		    resultLabel = "Query Title:  " + queryString;// + " <- click to select result";
		    
		    var queryResultCluster:Array = new Array();//new DataList(UStringConstants.DEPENDENCY_GRAPH);
		    var length:String = 'Number of contained results:  ' + queryResults.length;
		    // the map to hold the mappings of fact informations for the current query result 
		    var queryResultMapping:Object = {label: queryResultString, 
		            children: [{label: '', labelButton: CLICK_RESULT_STRING, key: UStringConstants.QUERY_RESULT, value: queryResultString, clickable: true}
		                 ,{label: '', labelButton: CLICK_COMPLETE_RESULT_LINEAGE_STRING, key: UStringConstants.COMPLETE_RESULT_LINEAGE
		                    , value: queryResults, query: queryString, clickable: true}
		                 ,{label: resultLabel}
		                 , {label: 'Type of Query:  Data Loading (Exploration)'}, {label: length}]};
		                 //, {label: 'List of contained facts:  ', children: []}]};
		    //var queryResultString:String = UStringConstants.RESULT + " " + _compareResultNumber.toString();
		    
		    /*
		    for each (var factSet:UFactSet in queryResults) {	
		    	buildDataCluster(factSet, _compareResultNumber, queryResultCluster, queryResultMapping);
		    }
		    */
		    for each (var lineageAnd:ULineageAnd in queryResults) {
		    	buildDataCluster(lineageAnd, _compareResultNumber, queryResultCluster, queryResultMapping);
		    }
		    
		    // add the number of contained facts
		    //(queryResultMapping.children as Array)[2].label = (queryResultMapping.children as Array)[2].label 
		      //                                                  + ((queryResultMapping.children as Array)[3].children as Array).length as String;
		    
		    //queryResultString = UStringConstants.RESULT + " " + _compareResultNumber.toString();
		    // add the current query result to the array of result clusters
		    _queryResults[queryResultString] = queryResultCluster;
		    // add the current query result mapping to the array of query result mappings
		    _queryResultsMapping.push(queryResultMapping);
		    // set the current query result to use by the visualization
		    _currentResultString = queryResultString;
		    
		    // clear the temporary hash structure
		    clearTemporaryHash();
		    
		    //trace("_nodesInCommon : " + _nodesInCommon);
		    // we have a disconnected subgraph
		    if(compareMode && (!_nodesInCommon)) {
		       //trace("We have no nodes in common !!!");
		       requestLowestDegreeForeignNode(data);
		       addSuperEdge(data,_lowestDegreeForeignNode, _lowestDegreeCurrentNode);
		    }    
		    
		    // in crease the counter for the compare result number
		    _compareResultNumber++;
		    
		    // if we have more facts available in the result -> set the event for the special node
		    if(queryResult.getMoreFactsAvailable()) {
		       
		       // add the new node
		       addAllFactsRequestNode(data,queryResult.getTotalNumberOfFacts());
		       // get the highest degree node for the current query result cluster
		       // or the data load node if it is not null 
		       var node:NodeSprite;
		       if(dataLoadNode == null)
		          node = requestHighestDegreeNode(queryResultString);
		       else
		          node = dataLoadNode
		       // now, connect both nodes
		       addAllFactsRequestEdge(data,node);
		    }
		    
		    // return the array that represents the current query result 
		    return queryResultCluster;
		}
		
		/**
		 * Processes the internally stored query result data (if available). 
		 * 
		 * More precisely, this function produces all the distinct nodes and edges 
		 * from the global dependency graph. In addition, the dependency graph cluster 
		 * is build.
		 * 
		 * @param fact the current fact to process.
		 * @param data the Data instance to fill
		 * */
		private function processFact(fact:UFact, data:Data, dependencyGraphCluster:Array = null, 
		              dependencyGraphMapping:Object = null, compareMode:Boolean = false):void {
			
			var arg1:String = fact.getFirstArgumentName();
			var arg2:String = fact.getSecondArgumentName();
			var relation:String = fact.getRelationName();
			var edgeIDString:String = fact.getId().toString(); 
			
			//trace("fact is : " + fact.toString());
			//trace("fact confidence is : " + fact.getConfidence().toFixed(4));
			
			var edgeConfidenceString:String;
			//if(fact.getConfidence() < 0.01 && fact.getConfidence() > 0) 
			  // edgeConfidenceString = "0.01";
			//else
			   edgeConfidenceString = fact.getConfidence().toFixed(2);
			
			// ------------------------------------------
			// check if this fact is already in our list
			// ------------------------------------------
			/*
			if(_facts.hasOwnProperty(edgeIDString)) {
				if(compareMode) {
					_subjectNode = _graphCache.usedNodes.arg1 as NodeSprite;
					_subjectNode.data.New = true;
					_objectNode = _graphCache.usedNodes.arg2 as NodeSprite;
					_objectNode.data.New = true;
					_predicateEdge = _graphCache.usedEdges.edgeIDString as EdgeSprite;
					_predicateEdge.data.New = true;
				}
	
				return;//edgeIDString)
			}
			*/
			
			// check if the node corresponding to the ficen subject name is already used
			_nodeInUse = _graphCache.nodeInUse(arg1);
			
			// set the current node, depending on the cache and the existence of
			// the node in the cache -> we get a new node instance or a already used one
			_subjectNode = _graphCache.useNode(arg1);
			
			if(!_nodeInUse) {
				
				// node is not already in use -> set the compare mode values
				// check if we are in compare mode
				if(compareMode) {
				   _subjectNode.data.Old = false;
			       _subjectNode.data.New = true;
			    }
				
				// set the node properties
				setNodeProperties(_subjectNode);
				
				_subjectNode.x = (int)(Math.random() * 800);//groupID * 20;
				_subjectNode.y = (int)(Math.random() * 600);//groupID * 20;
				
				//trace(" number of nodes before: " + data.nodes.length);
				data.addNode(_subjectNode);
				//trace(" number of nodes after: " + data.nodes.length);
				
			}
			
			else{
				// node is already in use -> set the compare mode values
				// check if we are in compare mode
				if(compareMode) {  
			       _subjectNode.data.New = true; 
			    }
		    
		    }
			
			// check if the node corresponding to the ficen subject name is already used
			_nodeInUse = _graphCache.nodeInUse(arg2);
			
			
			// set the current node, depending on the cache and the existence of
			// the node in the cache -> we get a new node instance or a already used one
			_objectNode = _graphCache.useNode(arg2);
			
			if(!_nodeInUse) {
				
				// node is not already in use -> set the compare mode values
				// check if we are in compare mode
				if(compareMode) {
				   _objectNode.data.Old = false;
			       _objectNode.data.New = true;
			    }
				
				// set the node properties
				setNodeProperties(_objectNode);
				
				_objectNode.x = (int)(Math.random() * 800);//groupID * 20 + 10;
				_objectNode.y = (int)(Math.random() * 600);//groupID * 20 + 10;
				
				//trace(" number of nodes before: " + data.nodes.length);
				data.addNode(_objectNode);
				//trace(" number of nodes after: " + data.nodes.length);
				
			}
			
			else {
				 // node is already in use -> set the compare mode values
				// check if we are in compare mode
				if(compareMode) {
			       _objectNode.data.New = true;
			    }
			}
			
			// check, if the edge has been in use already
			_edgeInUse = _graphCache.edgeInUse(edgeIDString);
			
			// get /produce the corresponding edge
			// we have to use the id=hasc-value of the edge, because there are multiple
			// edges with the given relation name
			_predicateEdge = _graphCache.useEdge(edgeIDString,relation,_subjectNode,
			             _objectNode,edgeConfidenceString,fact.getTruthValue(),fact,_factLineageAvalaibilityMap);
			// map the edge properties to the given edge
			_predicateEdge.data.factID = edgeIDString;          

            // make the edge visible
			_predicateEdge.visible = true;
			
			// set a hide flag
			_predicateEdge.data.hide = false;
			
			// edge is already in use -> set the compare mode values
			// check if we are in compare mode
			if(compareMode) {	
			  if(_edgeInUse) {			  	
			  	
			  	// the truth values are still the same
			  	if(_predicateEdge.data.truthValue == fact.getTruthValue() 
			  	   && UStringUtil.equalsIgnoreCase(_predicateEdge.data.confidence,edgeConfidenceString)) {
			  	   _predicateEdge.data.Old = true;
			  	   _predicateEdge.data.New = true;
			  	}
			  	else {
			  	   //overwrite the truth value for the edge
			  	   /* _predicateEdge.data.tooltip = relation + "(" + arg1 + "," + arg2 + ")" 
				                           + "[" + _graphCache.determineTruthValue(fact.getTruthValue()) + "|" + edgeConfidenceString + "]"
				                           + "\n" + "- lineage available: " + _predicateEdge.data.lineageAvailable
				                           + "\n" + "- old: [" + _graphCache.determineTruthValue(_predicateEdge.data.truthValue) 
				                           + "," + _predicateEdge.data.confidence + "] -> new: [" + _graphCache.determineTruthValue(fact.getTruthValue()) 
				                           + "," + edgeConfidenceString + "]"; */			                           
				     _predicateEdge.data.tooltip = relation + "(" + arg1 + "," + arg2 + ")<br/>" 
				                           + "<b> old truth value: </b>" + _graphCache.determineTruthValue(_predicateEdge.data.truthValue) + "<br/>"
				                           + "<b> new truth value: </b>" + _graphCache.determineTruthValue(fact.getTruthValue()) + "<br/>" 
				                           + "<b> old confidence: </b>" + _predicateEdge.data.confidence + "<br/>" 		
				                           + "<b> new confidence: </b>" + edgeConfidenceString + "<br/>"		                    
				                           + "<b> lineage available: </b>" + _predicateEdge.data.lineageAvailable;				                				                           				                       
				                           
				                           
			  	   _predicateEdge.data.truthValue = fact.getTruthValue();
			  	   _predicateEdge.data.confidence = edgeConfidenceString;
			  	   //_predicateEdge.data.tooltip = relation + "[" + edgeConfidenceString + "] - " + _graphCache.determineTruthValue(fact.getTruthValue());
			  	  
			  	   _predicateEdge.data.label = (relation.length > 20) ? relation.substr(0,20).concat(UStringConstants.THREE_DOTS) : relation;
			  	   _predicateEdge.data.Old = false;
		           _predicateEdge.data.New = true;
		           
		           // ----------------------------------------------------------------
		           // overwrite the fact-factID mapping
		           // ----------------------------------------------------------------
		           _facts[edgeIDString] = fact;
				   // overwrite the fact label and id mapping with the updated values
				    var obj:Object;
				   for each (obj in _factNamesAndIDs) {
				   	  if(obj.hasOwnProperty("id") && obj.id == edgeIDString) {
				   	     //obj.label = fact.toStringFact();
				   	     
				   	     // update fact string
				   	     obj.label = fact.toStringFact();
				   	     // update confidence
				         var confidenceStr:String = "Confidence:  " + edgeConfidenceString;
				         obj.confidence = edgeConfidenceString;
				         //(obj.children as Array)[4].label = confidenceStr;
				         (obj.children as Array)[4][UStringConstants.LABEL_BUTTON] = confidenceStr;
				         (obj.children as Array)[4].value = edgeConfidenceString;
				         // update truth value
				         var truthVal:String = _graphCache.determineTruthValue(_predicateEdge.data.truthValue);
				         obj.truth = _predicateEdge.data.truthValue;
				         var truthStr:String = "Truth Value:  " + truthVal;
				         //(obj.children as Array)[5].label = truthStr;
				         (obj.children as Array)[5][UStringConstants.LABEL_BUTTON] = truthStr;
				         (obj.children as Array)[5].value = _predicateEdge.data.truthValue;
				   	     
				   	     _predicateEdge.data.factInformation = obj;
				   	     
				   	     // immediately stop
				   	     break;
					 }
				   }
				   
			  	}
			  	
			  }
			  
			  else {
			  	_predicateEdge.data.Old = false;
		        _predicateEdge.data.New = true;
			  }	
			  
		    }
			
			// check, if we have the edge already in our visualization
			if(!_edgeInUse) {
				
				// ---------------------------------------------------------------
	            // ---------------------------------------------------------------
	            // we update the map for the force layout
				/*
				if(_forceNodeMap.hasOwnProperty(arg1)) {
					if(!(_forceNodeMap[arg1] as Object).hasOwnProperty(arg2)) {
					   trace("case 1: arg1 = " + arg1 + " , arg2 = " + arg2 + " , predicate = " + relation);
					   (_forceNodeMap[arg1] as Object)[arg2] = true;
					   if(!_forceNodeMap.hasOwnProperty(arg2))
					      _forceNodeMap[arg2] = new Object();
					   (_forceNodeMap[arg2] as Object)[arg1] = true;
					   _forceEdgeList.push(_predicateEdge);
					}
				}
				else 
				   if(_forceNodeMap.hasOwnProperty(arg2)) {
					if(!(_forceNodeMap[arg2] as Object).hasOwnProperty(arg1)) {
						trace("case 2: arg1 = " + arg1 + " , arg2 = " + arg2 + " , predicate = " + relation);
					   (_forceNodeMap[arg2] as Object)[arg1] = true;
					   if(!_forceNodeMap.hasOwnProperty(arg1))
					      _forceNodeMap[arg1] = new Object();
					   (_forceNodeMap[arg1] as Object)[arg2] = true;
					   _forceEdgeList.push(_predicateEdge);
					}
				   }
				else {
				   trace("case 3: arg1 = " + arg1 + " , arg2 = " + arg2 + " , predicate = " + relation);
				   _forceNodeMap[arg1] = new Object();
				   _forceNodeMap[arg2] = new Object();
				   (_forceNodeMap[arg1] as Object)[arg2] = true;
				   (_forceNodeMap[arg2] as Object)[arg1] = true;
				   _forceEdgeList.push(_predicateEdge);
				}
				*/
				// --------------------------------------------------------------
				// ---------------------------------------------------------------
				
				// map the facts to their factID`s
				_facts[edgeIDString] = fact;
				// push an object {label: fact.toString(), id: edge ID} into the fact list
				
				// ------------------------------------------------------------------------
				// ----------- produce fact related information to be diplayed ------------
				var factString:String = "Fact:  " + _predicateEdge.data.factName;
				var subjectString:String = "Subject:  " + arg1;
				var predicateString:String = "Predicate:  " + relation;
				var objectString:String = "Object:  " + arg2;
				var confidenceString:String = "Confidence:  " + edgeConfidenceString;
				var truthValue:String = _graphCache.determineTruthValue(_predicateEdge.data.truthValue);
				var truthString:String = "Truth Value:  " + truthValue;
				var factObject:Object = {label: '', labelButton: factString, key: UStringConstants.FACT, value: edgeIDString, clickable: true};
				var subjectObject:Object = {label: '', labelButton: subjectString, key: UStringConstants.SUBJECT, value: arg1, clickable: true};
				// id and value fields fir the predicate edge, a bit redundant for now
				var predicateObject:Object = {label: '', labelButton: predicateString, key: UStringConstants.PREDICATE, id: edgeIDString, value: relation, clickable: true}; 
				var objectObject:Object = {label: '', labelButton: objectString, key: UStringConstants.OBJECT, value: arg2, clickable: true};
				var confidenceObject:Object = {label: '', labelButton: confidenceString, key: UStringConstants.CONFIDENCE, value: edgeConfidenceString, clickable: true};
				var truthObject:Object = {label: '', labelButton: truthString, key: UStringConstants.TRUTH_VALUE, value: truthValue, clickable: true};
				
				// the fact - information mapping
				var factInformationObject:Object = {label: fact.toStringFact(), id: edgeIDString, 
				         subject: arg1, predicate: relation, object: arg2, confidence: edgeConfidenceString, truthValue: truthValue
				         , children: [factObject, subjectObject, predicateObject, objectObject, confidenceObject, truthObject]}
				
				_factNamesAndIDs.push(factInformationObject);
				// ------------------------------------------------------------------------
				// ----------- produce fact related information to be diplayed ------------
				
				// add the infomration to the respective edge 
				_predicateEdge.data.factInformation = factInformationObject;
				
				
				// add the current edge to the data instance
				data.addEdge(_predicateEdge);

				}
			
			// --------------------------------------------------------------
			// check if the nodes are already in the current dependency graph
			// --------------------------------------------------------------
			
			// check if we already put the current subject node into the list for the query result
			if(!_temporaryHash.hasOwnProperty(arg1)) {
				//_subjectNode = _graphCache.useNode(arg1);
				//trace("arg1: " + arg1);
				dependencyGraphCluster.push(_subjectNode);			
				//trace("_graphCache.usedNodes[arg1].data.label: " + _graphCache.usedNodes[arg1].data.label);
				_temporaryHash[arg1] = arg1;
			}
			// check if we already put the current object node into the list for the query result
			if(!_temporaryHash.hasOwnProperty(arg2)) {
				//_objectNode = _graphCache.useNode(arg2);
				//trace("arg2: " + arg2);
				dependencyGraphCluster.push(_objectNode);
				
				//trace("_graphCache.usedNodes[arg2].data.label: " + _graphCache.usedNodes[arg2].data.label);
				_temporaryHash[arg2] = arg2;
			}
			// check if we already put the current edge into the list for the query result
			if(!_temporaryHash.hasOwnProperty(edgeIDString)) {
				//_objectNode = _graphCache.useNode(arg2);
				//trace("arg2: " + arg2);
				dependencyGraphCluster.push(_predicateEdge);
				//trace("_predicateEdge.data.label: " + _predicateEdge.data.label);
				
				// add the fact to the current dependency graph mapping
				//(dependencyGraphMapping.children as Array).push(_predicateEdge.data.factInformation);
				((dependencyGraphMapping.children as Array)[4].children as Array).push({label: _predicateEdge.data.factInformation.label, 
				        key: _predicateEdge.data.factInformation.id});
				
				_temporaryHash[edgeIDString] = edgeIDString;
				// we add a flag to the edge , this flag indicates that the current edge belongs 
			    // to the dependency graph
				//_predicateEdge.data[queryResultString] = true;
			}
			
		}
		
		/**
		 * Builds an data cluster for the facts of a given query result (fact set).
		 * 
		 * @param factSet the current query result (fact set).
		 * @param clusterID the id for the current data (fact/query result) cluster.
		 * */
		//private function buildDataCluster(factSet:UFactSet, clusterID:int, queryResultCluster:Array = null, queryResultMapping:Object = null):void {
		private function buildDataCluster(lineageAnd:ULineageAnd, clusterID:int, queryResultCluster:Array = null, queryResultMapping:Object = null):void {
	    	
	    	var lineageHull:ULineageAbstract;
	    	var lineageFact:ULineageOr;
	    	var fact:UFact;
		    var children:Array;
		    
		    var edgeConfidenceString:String;
			//if(lineageAnd.getConf() < 0.01 && lineageAnd.getConf() > 0) 
			  // edgeConfidenceString = "0.01";
			//else
			   edgeConfidenceString = lineageAnd.getConf().toFixed(2).toString();
		    
		    var resultString:String = "";
		    var containedFactNumberString:String = 'Number of contained facts:  ' + lineageAnd.getChildren().length;
	    	
	    	//{label: 'List of contained facts:  ', children: []}
	    	var resultObj:Object = {label: '',  
	    	      children: [{label: '', labelButton: CLICK_SINGLE_RESULT_LINEAGE_STRING
	    	                , key: UStringConstants.LINEAGE_FACTS, lineageFacts: lineageAnd, clickable: true}, 
	    	        {label: containedFactNumberString},{label: 'List of contained facts:  ', children: []}]};
	    	
	    	var subject:String;
		    var object:String;
		    var edgeIDString:String;
			
			//for each (var fact:UFact in factSet.getFactSet()) {
			for each (lineageHull in lineageAnd.getChildren()) {
		    	
		    	// ------------------------
		    	// convert the data
		    	// ------------------------
		    	lineageFact = ULineageOr(lineageHull);
		    	fact = lineageFact.getFact();
		    	
		    	// the result string for the label
		    	resultString += fact.toStringSimple() + "^" ;
		    	
		    	// ---------------------------------------------------------------
				// ----------- add the nodes to the query result cluster ---------
				// ---------------------------------------------------------------	
		   	
				subject = fact.getFirstArgumentName();
				object = fact.getSecondArgumentName();
				edgeIDString = fact.getId().toString();
				
				// check if we already put the current subject node into the list for the query result
				if(!_temporaryHash.hasOwnProperty(subject)) {
					//_subjectNode = _graphCache.useNode(subject);
					queryResultCluster.push(_graphCache.usedNodes[subject]);
					_temporaryHash[subject] = subject;
					//trace("subject: " + subject);
				}
				// check if we already put the current object node into the list for the query result
				if(!_temporaryHash.hasOwnProperty(object)) {
					//_objectNode = _graphCache.useNode(object);
					queryResultCluster.push(_graphCache.usedNodes[object]);
					_temporaryHash[object] = object;
					//trace("object: " + object);
				}
				
				// get the current edge
				_predicateEdge = _graphCache.usedEdges[edgeIDString];
				
				// check if we already put the current edge into the list for the query result
			    if(!_temporaryHash.hasOwnProperty(edgeIDString)) {
					//_objectNode = _graphCache.useNode(arg2);
					//trace("arg2: " + arg2);
					queryResultCluster.push(_predicateEdge);
					
					// add the fact to the current dependency graph mapping
				    //(queryResultMapping.children as Array).push(_predicateEdge.data.factInformation);
				    //((queryResultMapping.children as Array)[1].children as Array).push({label: _predicateEdge.data.factInformation.label});
				    
				    //((resultObj.children as Array)[2].children as Array).push({label: _predicateEdge.data.factInformation.label});//, 
				    
				      //  key: _predicateEdge.data.factInformation.id});
					//trace("_graphCache.usedNodes[arg2].data.label: " + _graphCache.usedNodes[arg2].data.label);
					_temporaryHash[edgeIDString] = edgeIDString;
					// we add a flag to the edge , this flag indicates that the current edge belongs 
				    // to the dependency graph
					//_predicateEdge.data[queryResultString] = true;
					//trace("_predicateEdge.data.label: " + _predicateEdge.data.label);
			    }
			    
			    // add the fact to the answer for sure !!!!!
			    ((resultObj.children as Array)[2].children as Array).push({label: _predicateEdge.data.factInformation.label});//, 
				
			}	
			
			// cut the result string
			//resultString = resultString.substring(0,resultString.length-2);	
			resultString = resultString.substring(0,resultString.length-1);	
			resultObj.label = resultString;	
			// add this mapping
			(queryResultMapping.children as Array).push(resultObj);	
		}
		
		private function setNodeProperties(node:NodeSprite):void {
			// a flag to indicate if we already explored this particular node
			//node.data.explored = false; 
			
			// the start shape for the node
			//node.shape = Shapes.POLYBLOB;
			node.shape = UShapes.ROUNDED_RECT;
			node.size = 1;
			
			node.visible = true;
			node.buttonMode = true;
			
			// set a hide flag
			//node.data.hide = false;
			
			// set the current compare query result id for the node
		    node.data.CompareID = _compareResultNumber;
		}
		
		/**
		 * Computes the highest degree node for the current query result and the highest degree node for all other nodes
		 * not connected to the current result (subgraph).
		 * */
		private function requestLowestDegreeForeignNode(data:Data):void {
			var lowestDegreeForeign:int = int.MAX_VALUE;
			var lowestDegreeCurrent:int = int.MAX_VALUE;
			data.nodes.visit(function(ns:NodeSprite):void {
				// we want the lowest degree node not connected to our sub grah, if we have an additonal subgraph at all
				if(ns.data.CompareID == _compareResultNumber) {
					if(ns.degree <= lowestDegreeCurrent) {
						lowestDegreeCurrent = ns.degree;
				        _lowestDegreeCurrentNode = ns;
				        //trace("current node has lower degree!");
				        //trace("current node: " + _lowestDegreeCurrentNode + " , current degree: " + lowestDegreeCurrent);
					}
				}
				else {
					if(ns.degree <= lowestDegreeForeign) {
						lowestDegreeForeign = ns.degree;
				        _lowestDegreeForeignNode = ns;
				        //trace("foreign node has lower degree!");
				        //trace("foreign node: " + _lowestDegreeForeignNode + " , foreign degree: " + lowestDegreeForeign);
				    }
				}
			});
		}
		
		/** Checks if the current graph data and the new dependency graph have nodes in common.
		 * If this is the case, the nodesInCommon flag is set to true, otherwise this flag is false. */
		private function checkNodesInCommon(dependencyGraph:Array):void {
			var fact:UFact;
			for each (fact in dependencyGraph) {
				if(_graphCache.nodeInUse(fact.getFirstArgumentName())) {
				   _nodesInCommon = true;
				   return;
				}
				if(_graphCache.nodeInUse(fact.getSecondArgumentName())) {
				   _nodesInCommon = true;
				   return;
				}
			}
		}
		
		private function addSuperEdge(data:Data, sourceNode:NodeSprite, targetNode:NodeSprite):void {
			
			var superEdgeLabel:String = UStringConstants.SUPER_EDGE + _compareResultNumber.toString();
			var superEdgeID:String = UStringUtil.hashCode(superEdgeLabel).toString();
			
			_predicateEdge = _graphCache.useEdge(superEdgeID,superEdgeLabel,sourceNode,targetNode);
	
			_predicateEdge.visible = true;
			
			// set a hide flag
			//_predicateEdge.data.hide = false;
			
			_predicateEdge.data.SuperEdge = true;
			
			// add the current edge to the data instance
			data.addEdge(_predicateEdge);
		}
		
		private function addAllFactsRequestNode(data:Data, numOfFacts:int):void {
			
			_allFactsRequestNode = new NodeSprite();//_graphCache.useNode("LOAD ALL FACTS " + "(" + numOfFacts + ")");
	
			_allFactsRequestNode.visible = true;
			// set the special request flag 
			_allFactsRequestNode.data[UStringConstants.REQUEST_NODE] = true;
			// set the label 
			_allFactsRequestNode.data.label = "all facts " + "(" + numOfFacts + ")" + " ...";
			// set the tooltip 
			_allFactsRequestNode.data.tooltip = "all facts " + "(" + numOfFacts + ")" + " ...";
			
			// set the chosen node renderer
			_allFactsRequestNode.renderer = UNodeRenderer.instance
			
			// add the current edge to the data instance
			data.addNode(_allFactsRequestNode);
			// add the node to the respective current cluster array
			(_queryResults[_currentResultString] as Array).push(_allFactsRequestNode);
		}
		
		private function addAllFactsRequestEdge(data:Data, sourceNode:NodeSprite):void {
			
			_allFactsRequestEdge = new EdgeSprite(sourceNode,_allFactsRequestNode,true);
	
			_allFactsRequestEdge.visible = true;
			
			// set the special request flag 
			_allFactsRequestEdge.data[UStringConstants.REQUEST_EDGE] = true;
			// set the label 
			_allFactsRequestEdge.data.label = "LOAD ALL FACTS Edge";
			// set the tooltip 
			_allFactsRequestEdge.data.tooltip = "LOAD ALL FACTS Edge";
			// set the confidence value, if any exist
		    _allFactsRequestEdge.data.confidence = 1.0;
		    // set the truth value, if any exist
		    _allFactsRequestEdge.data.truthValue = 1;
			
			// set the edges of the corresponding nodes
			_allFactsRequestEdge.source.addOutEdge(_allFactsRequestEdge);
	        _allFactsRequestEdge.target.addInEdge(_allFactsRequestEdge);
	        
	        // ------------------------------
			// ------------------------------
			// just a test for adding a marker to an edge
			// in case the fact represented by the edge
			// contains lineage information
	        var textSprite:TextSprite = new TextSprite("LOAD ALL FACTS Edge");
	        textSprite.horizontalAnchor = TextSprite.CENTER;
			textSprite.verticalAnchor = TextSprite.MIDDLE;
			//textSprite.visible = _graphCache.edgeLabelsVisible
			textSprite.textFormat = _graphCache.labelFormatterEdges;
			textSprite.textMode = TextSprite.EMBED;
			_allFactsRequestEdge.data.TS = textSprite;
			//_allFactsRequestEdge.addChild(textSprite);
			if(_graphCache.edgeLabelsVisible)
			   _allFactsRequestEdge.addChild(textSprite);
			// ------------------------------
			// ------------------------------
			
			textSprite.mouseEnabled = true;
			//textSprite.mouseChildren = true;
			//textSprite.buttonMode = true;
			textSprite.addEventListener(MouseEvent.MOUSE_OVER,UGraphControls.hoverOverEdgeLabel,false,0,true);
			textSprite.addEventListener(MouseEvent.MOUSE_OUT,UGraphControls.hoverOutEdgeLabel,false,0,true);
		    _allFactsRequestEdge.mouseChildren = true;
			
			// add the current edge to the data instance
			data.addEdge(_allFactsRequestEdge);
			// add the edge to the respective current cluster array
			(_queryResults[_currentResultString] as Array).push(_allFactsRequestEdge);
		}
		
		public function removeAllFactsRequestSprites(data:Data):void {
			
			if(_allFactsRequestNode != null && _allFactsRequestEdge != null) {
				_allFactsRequestNode.removeAllEdges();
				data.removeNode(_allFactsRequestNode);
				data.removeEdge(_allFactsRequestEdge);
				_allFactsRequestNode = null;
				_allFactsRequestEdge = null;
				
				// remove the node and the edge
				if(_currentResultString != null) {
					(_queryResults[_currentResultString] as Array).pop();
				    (_queryResults[_currentResultString] as Array).pop();
				}
				
			}
			else
			   return;	
		}
		
		private function requestHighestDegreeNode(clusterID:String):NodeSprite {
			var highestDegree:int = 0;
			var highestDegreeNode:NodeSprite;
			var resultCluster:Array = _queryResults.hasOwnProperty(clusterID) ? _queryResults[clusterID] : null;
			var ds:DataSprite;
			var node:NodeSprite;
			
			if(resultCluster == null)
				return null;
			else {
				for each (ds in resultCluster) {
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
		
		
		private function buildNewDependencyGraph(resultLineage:Array):Array {
			var dependencyGraph:Object = new Object();
			var dependencyGraphArray:Array = new Array();
			
			for each (var lineageAnd:ULineageAnd in resultLineage) {
				findNodeItemAnd(lineageAnd, dependencyGraph, 0);
			}
			
			for each (var obj:Object in dependencyGraph) {
				dependencyGraphArray.push(obj);
			}
			
			return dependencyGraphArray;
		}
		
		private function findNodeItemOr(lineageOr:ULineageOr, dependencyGraph:Object, lineageLevel:int):void {
			var fact:UFact = lineageOr.getFact();
			if(fact != null) {
				if(!dependencyGraph.hasOwnProperty(fact.getId().toString())) {
				    // set the new confidence value and the appropriate truth value for the fact
				    //if(lineageLevel < fact.min_level) {
				    fact.setConfidence(lineageOr.getConf());
			        fact.min_level = lineageLevel;
			        dependencyGraph[fact.getId().toString()] = fact;
				}
				else {
					// set the new confidence value and the appropriate truth value for the fact
					// and set overwrite the current fact instance with the new one
					// the reason?: the new instance of the fact could have a changed lineage
				    if(lineageLevel < fact.min_level) {
				       // we have to check if the currently used instance of the fact has lineage information available, 
				       // if not, this is a base fact, and so we have to use this instance and its confidence
				       if((dependencyGraph[fact.getId().toString()] as UFact).lineageAvailable()) {	
					       //fact.setConfidence(lineageOr.getConf());
					       (dependencyGraph[fact.getId().toString()] as UFact).setConfidence(lineageOr.getConf());
					       //fact.min_level = lineageLevel;
					       (dependencyGraph[fact.getId().toString()] as UFact).min_level = lineageLevel;
					       //dependencyGraph[fact.getId().toString()] = fact;
				       }
				    }
				    else if(lineageLevel == fact.min_level) {
				       // we have to check if the currently used instance of the fact has lineage information available, 
				       // if not, this is a base fact, and so we have to use this instance and its confidence
				       if((dependencyGraph[fact.getId().toString()] as UFact).lineageAvailable()
				           && lineageOr.getConf() > (dependencyGraph[fact.getId().toString()] as UFact).getConfidence()) {	
					       //fact.setConfidence(lineageOr.getConf());
					       (dependencyGraph[fact.getId().toString()] as UFact).setConfidence(lineageOr.getConf());
					       //fact.min_level = lineageLevel;
					       (dependencyGraph[fact.getId().toString()] as UFact).min_level = lineageLevel;
					       //dependencyGraph[fact.getId().toString()] = fact;
				       }
				    }
				}
				// increase the lineage level
				lineageLevel++;
			}
			
			if(lineageOr.getChildren() != null) {
				var level:int = lineageLevel;
				for each (var lineageAnd:ULineageAnd in lineageOr.getChildren()) {
					findNodeItemAnd(lineageAnd, dependencyGraph, level);
				}
			}
		}
		
		private function findNodeItemAnd(lineageAnd:ULineageAnd, dependencyGraph:Object, lineageLevel:int):void {
			if(lineageAnd.getChildren() == null || lineageAnd.getChildren().length == 0)
				return;
		    
			var level:int = lineageLevel;
			for each (var lineageOr:ULineageOr in lineageAnd.getChildren()) {
				findNodeItemOr(lineageOr, dependencyGraph, level);
			}
		}
		
		
		private function clearTemporaryHash():void {
			//var tempValue:NodeSprite;
			var tempKey:String;
			/*
			for each (tempValue in _temporaryHash) {
				tempValue = null;
			}
			*/
			
			for (tempKey in _temporaryHash) {
				delete _temporaryHash[tempKey];
			}
			
			//_temporaryHash = new Object();
		}
		
		/*
		private function clearForceLayoutStructures():void {
			var key:String;
			var obj:Object;
			for (key in _forceNodeMap) {
				obj = _forceNodeMap[key];
				for (key in obj) {
					obj[key] = null;
					delete obj[key];
				}
			}
			_forceNodeMap = new Object();
			
			while(_forceEdgeList.length > 0) {
				_forceEdgeList.pop();
			}
		}
		*/
		
		public function clear():void {
			
			var result:Array;
			var resultKey:String;
			var node:NodeSprite;
			var ds:DataSprite;
			
			// ------------------------------------
			// clear the results
			for each (result in _queryResults) {
				for each (ds in result) {
					ds = null;
				}
				while(result.length > 0) {
					result.pop();
				}
				result = null;
			}
			for (resultKey in _queryResults) {
				delete _queryResults[resultKey];
			}
			// ------------------------------------
			
			// clear the facts
			var fact:String;
			for (fact in _facts) {
				_facts[fact] = null;
				delete _facts[fact];
			}
			// clear the fact name - fact id mappings
			var mapping:Object;
			while (_factNamesAndIDs.length > 0) {
				mapping = _factNamesAndIDs.pop();
				mapping = null;
			}
			// clear the lineage information mappings
			while (_factLineageAvalaibilityMap.length > 0) {
				mapping = _factLineageAvalaibilityMap.pop();
				mapping = null;
			}
			
			// clear the lineage information mappings
			while (_queryResultsMapping.length > 0) {
				mapping = _queryResultsMapping.pop();
				mapping = null;
			}	
			
			// -------------------------------------------------------------------------------------   
		    // we have to clear the force layout structures
		    //clearForceLayoutStructures();
		    // -------------------------------------------------------------------------------------
			
			// clear only the usage cache
			_graphCache.clearUsedData();
			
			// reset the highest node degree
			_highestNodeDegree = 0;
			
			// reset the compare result number
		    _compareResultNumber = 0;
		
		    // reset the nodes in common flag
		    _nodesInCommon = false;
		    
		    // reset the highest degree foreign node
		    _lowestDegreeForeignNode = null;
		    // reset the highest degree current node
		    _lowestDegreeCurrentNode = null;
		    // reset the facts request node
		    _allFactsRequestNode = null;
		    // reset the facts request node
		    _allFactsRequestEdge = null;
			
		}

	}
}