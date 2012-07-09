package uviface.event
{
	import flash.events.Event;
	
	import uviface.viz.util.UColors;

	public class UQuickAccessEvent extends Event
	{
		
		public static const QUICK_ACCESS_EVENT:String = "quickAccesEvent";
		
		private var _onlyQuickMenu:Boolean = false;
		
		private var _applicationBackgroundColor:uint = UColors.WHITE;
		private var _backgroundColorChange:Boolean = false;
		
		private var _menuToAccess:int = 0;
		public static const QUERY_MENU:int = 0;
		public static const OPTIONS_MENU:int = 1;
		public static const RULE_MENU:int = 2;
		public static const STATISTICS_MENU:int = 3;
		public static const NOTHING:int = 5;
		
		public function UQuickAccessEvent(type:String, menuToAccess:int = 0, onlyQuickMenu:Boolean = false, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			//TODO: implement function
			super(type, bubbles, cancelable);
			_menuToAccess = menuToAccess;
			_onlyQuickMenu = onlyQuickMenu;
		}
		
		public function get applicationBackgroundColor():uint {
			return _applicationBackgroundColor;
		}
		
		public function set applicationBackgroundColor(applicationBackgroundColor:uint):void {
			_applicationBackgroundColor = applicationBackgroundColor;
		}
		
		public function get backgroundColorChange():Boolean {
			return _backgroundColorChange;
		}
		
		public function set backgroundColorChange(backgroundColorChange:Boolean):void {
			_backgroundColorChange = backgroundColorChange;
		}
		
		public function get menuToAccess():int {
			return _menuToAccess;
		}
		
		public function set menuToAccess(menuToAccess:int):void {
			_menuToAccess = menuToAccess;
		}
		
		public function get onlyQuickMenu():Boolean {
			return _onlyQuickMenu;
		}
		
		public function set onlyQuickMenu(onlyQuickMenu:Boolean):void {
			_onlyQuickMenu = onlyQuickMenu;
		}
		
	}
}