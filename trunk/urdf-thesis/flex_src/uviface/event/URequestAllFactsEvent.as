package uviface.event
{
	import flash.events.Event;

	public class URequestAllFactsEvent extends Event
	{   
        public static const REQUEST_ALL_FACTS:String = "requestAllFacts";
        public static const CHANGE_NUM_OF_RESULT_FACTS:String = "changeNumOfResultFacts";
        
        private var _numOfResultFacts:int = 100;
		
		public function URequestAllFactsEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
		}
		
		public function set numOfResultFacts(numOfResultFacts:int):void {
			_numOfResultFacts = numOfResultFacts;
		}
		
		public function get numOfResultFacts():int {
			return _numOfResultFacts;
		}
		
	}
}