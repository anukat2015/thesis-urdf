package uviface.ui.query
{
	
	import flash.display.DisplayObjectContainer;
	import flash.events.KeyboardEvent;
	import flash.geom.Point;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Menu;
	import mx.controls.TextArea;
	import mx.events.MenuEvent;
	
	import urdf.api.URelation;
	
	[Event(name="menuHide",type="mx.events.MenuEvent")]
	
	public class UPredicateMenu {
		
		private var _menu:Menu;
		[Bindable]
		private var _parentWidth:int;
		private var _rowHeight:int;
		
		[Bindable]
		private var _relations:ArrayCollection = URelation.getRelations();
		
		public function UPredicateMenu(parent:TextArea = null) {
			//addEventListener( MoveEvent.MOVE, handleMove, false, 0, true );
			
			//if (systemManager) {
				//systemManager.addEventListener( ResizeEvent.RESIZE, handleResize, false, 0, true );
			//}
			
    		_menu = Menu.createMenu( parent, _relations, false);
    		//_menu.showRoot = false;	
			//_menu.labelField="@label";
			_menu.setStyle( "openDuration", 0 );
			_menu.setStyle( "dropShadowEnabled", true);
			_menu.setStyle( "borderStyle", "none" );	
			_menu.rowCount = 10;			
			
			_menu.addEventListener( MenuEvent.MENU_HIDE, handleMenuHide );
			//_menu.addEventListener( MenuEvent.MENU_SHOW, handleMenuShow );
			//_menu.addEventListener( MenuEvent.ITEM_CLICK, handleItemClick );
			
			// the challeng we have is that we want to right align the menu
			// with the component however we don't know how wide the menu is
			// going to be until we show it. the wordaround is to initially place
			// it off screen and then (once we know it's size) we'll position it
			_menu.show( -1000, -1000 );		
			
		}
		
		public function get menu():Menu {
			return _menu;
		}

		private function handleMenuHide( event:MenuEvent ):void {
			if (event.menu == _menu)
			{
				//PopUpManager.removePopUp( this );
				_menu.hide();
			}
			
			dispatchEvent( event );
		}
		
		public function showMenu(x:Number,y:Number):void {
			positionMenu(x,y);
			_menu.show(10,10);
		}
		
		public function showDropDownMenu(keyboardEvent:KeyboardEvent):void
	    {
	      // get the menu parent from the mouse event
	      var parent:DisplayObjectContainer = 
	         DisplayObjectContainer(keyboardEvent.currentTarget);
	      
	      // position menu relative to the parent
	      //var point:Point = new Point( mouseEvent.localX, mouseEvent.localY );
	      //point = parent.localToGlobal( point );
	      
	      // show the menu
	      //menu.show( point.x, point.y );
	    }
					
		public function handleItemClick(event:MenuEvent):String {
			var tempString:String = _menu.selectedItem as String;
			_menu.selectedIndex = -1;
			return tempString;
		}
		/*
		private function getMenuPosition():Point
		{
			var localPoint:Point = new Point( 0, 0 );
			var globalPoint:Point = localToGlobal( localPoint ); 
			
			var x:int = globalPoint.x + (_parentWidth - _menu.width - RIGHT_PADDING);
			var y:int = globalPoint.y + _rowHeight + MENU_PADDING + TOP_PADDING;

			return new Point( x, y );
		}
		*/
		private function getMenuPosition(x:Number,y:Number):Point {
			var localPoint:Point = new Point(x,y);
			var globalPoint:Point = _menu.localToGlobal( localPoint ); 
			
			//var x:int = globalPoint.x + (_parentWidth - _menu.width - RIGHT_PADDING);
			//var y:int = globalPoint.y + _rowHeight + MENU_PADDING + TOP_PADDING;

			return new Point( x, y );
		}
		
		public function positionMenu(x:Number,y:Number):void {
			var point:Point = getMenuPosition(x,y);
			_menu.move( point.x, point.y );				
		}
		
		public function highlightFirstRow():void {
			_menu.selectedIndex = 0;
		}

	}
						

}