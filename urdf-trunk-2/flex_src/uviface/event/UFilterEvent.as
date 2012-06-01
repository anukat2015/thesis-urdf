package uviface.event
{
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;

	public class UFilterEvent extends Event
	{
		// Define static constant.
		
		public static const FILTER_EVENT:String = "filterEvent";
		
		public static const NODE_FILTER_EVENT:String = "filterNodes";
        public static const PREDICATE_FILTER_EVENT:String = "filterPredicates";
        public static const TRUTH_VALUE_FILTER_EVENT:String = "filterTruthValues";
        public static const CONFIDENCE_FILTER_EVENT:String = "filterConfidences";
        
        private var _typeOfFilter:String = PREDICATE_FILTER_EVENT;
        
        private var _multiplePredicates:Boolean = false;
        private var _multipleConfidences:Boolean = false;
        private var _multipleTruthValues:Boolean = false;
        
        private var _predicate:String = null;
        private var _predicates:ArrayCollection = null;
        private var _nodes:ArrayCollection = null;
        private var _truthValues:Object;
        private var _truthValue:String = null;
        private var _confidence:String = null;
        private var _minConfidence:Number = 0;
        private var _maxConfidence:Number = 0;
		
		public function UFilterEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			
		}
        
        public function get typeOfFilter():String {
        	return _typeOfFilter;
        }
        
        public function set typeOfFilter(typeOfFilter:String):void {
        	_typeOfFilter = typeOfFilter;
        }
        
        public function get predicate():String {
        	return _predicate;
        }
        
        public function set predicate(predicate:String):void {
        	_predicate = predicate;
        }
        
        public function get predicates():ArrayCollection {
        	return _predicates;
        }
        
        public function set predicates(predicates:ArrayCollection):void {
        	_predicates = predicates;
        }
        
        public function get nodes():ArrayCollection {
        	return _nodes;
        }
        
        public function set nodes(nodes:ArrayCollection):void {
        	_nodes = nodes;
        }
        
        public function get truthValue():String {
        	return _truthValue;
        }
        
        public function set truthValue(truthValue:String):void {
        	_truthValue = truthValue;
        }
        
        public function get confidence():String {
        	return _confidence;
        }
        
        public function set confidence(confidence:String):void {
        	_confidence = confidence;
        }
        
        public function get minConfidence():Number {
        	return _minConfidence;
        }
        
        public function set minConfidence(minConfidence:Number):void {
        	_minConfidence = minConfidence;
        }
        
        public function get maxConfidence():Number {
        	return _maxConfidence;
        }
        
        public function set maxConfidence(maxConfidence:Number):void {
        	_maxConfidence = maxConfidence;
        }
        
        public function get multiplePredicates():Boolean {
        	return _multiplePredicates;
        }
        
        public function set multiplePredicates(multiplePredicates:Boolean):void {
        	_multiplePredicates = multiplePredicates;
        }
        
        public function get multipleConfidences():Boolean {
        	return _multipleConfidences;
        }
        
        public function set multipleConfidences(multipleConfidences:Boolean):void {
        	_multipleConfidences = multipleConfidences;
        }
        
        public function get multipleTruthValues():Boolean {
        	return _multipleTruthValues;
        }
        
        public function set multipleTruthValues(multipleTruthValues:Boolean):void {
        	_multipleTruthValues = multipleTruthValues;
        	_truthValues = new Object();
        }
        
        public function get truthValues():Object {
        	return _truthValues;
        }
        
        public function set truthValues(truthValues:Object):void {
        	_truthValues = truthValues;
        }
        
	}
}