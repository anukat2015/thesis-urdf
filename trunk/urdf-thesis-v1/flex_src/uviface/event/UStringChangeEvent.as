package uviface.event
{
	import flash.events.Event;

	public class UStringChangeEvent extends Event
	{
		public static const STRING_CHANGE_EVENT:String = "changedString";
		
		public static const SUBJECT:int = 0;
		public static const PREDICATE:int = 1;
		public static const OBJECT:int = 2;
		
		private var _changedString:String = "";
		private var _componentToChange:int = 1;
		
		public function UStringChangeEvent(type:String, componentToChange:int = 1, changedString:String = null, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			_changedString = changedString;
			if(componentToChange < 0 || componentToChange > 2)
			   _componentToChange = UStringChangeEvent.PREDICATE;
			_componentToChange = componentToChange;
		}
		
		public function set changedString(changedString:String):void {
			_changedString = changedString;
		}
		
		public function get changedString():String {
			return _changedString;
		}
		
		public function set componentToChange(componentToChange:int):void {
			if(componentToChange < 0 || componentToChange > 2)
			   _componentToChange = UStringChangeEvent.PREDICATE;
			_componentToChange = componentToChange;
		}
		
		public function get componentToChange():int {
			return _componentToChange;
		}
		
	}
}