package uviface.ui.options
{
	import flash.events.MouseEvent;
	
	import mx.controls.Button;
	import mx.controls.treeClasses.*;
	import mx.events.ListEvent;

	public class UButtonTreeItemRenderer2 extends TreeItemRenderer
	{
		private var _button:Button;
		private var _clickable:Boolean = false;
        //private var _itemXml:XML;
		
		public function UButtonTreeItemRenderer2()
		{
			super();
			mouseEnabled = false;
		}
		
		public function set button(button:Button):void {
			_button = button;
		}
		
		public function get button():Button {
			return _button;
		}
		
        override public function set data(value:Object):void{
            
            if(value == null)
               return;               
                
            super.data = value;
            
            //_button = new Button("Click");
            //_button.addEventListener(MouseEvent.CLICK, handleButtonClick);
            //addChild(_button);
            
        }
        override protected function createChildren():void{
            super.createChildren();
            _button = new Button();
            _button.label = "Click";
            _button.setStyle("fontSize",9);
            _button.setStyle("rolloverColor", "green");
            _button.toolTip = "Click button or label to perform action. You can also click the 'Help' button in the top-right corner for detailed information.";
            _button.width = 40;
            _button.height = 15;
            _button.setStyle("paddingLeft", 0);
            _button.setStyle("paddingRight", 0);
            _button.addEventListener(MouseEvent.CLICK, handleButtonClick);
            addChild(_button);
        }
        override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void{
            super.updateDisplayList(unscaledWidth,unscaledHeight);
            if(super.data){
                var tld:TreeListData = TreeListData(super.listData);
               
                if(tld.item.hasOwnProperty("clickable")){
                    _clickable = true;
                    //trace("clickable");
                }else{
                	//trace("not clickable");
                   _clickable = false;
                   if(_button != null) {
                      this.removeChild(_button);
                      _button.removeEventListener(MouseEvent.CLICK, handleButtonClick);
                      _button = null;
                   }
                }
                if(_clickable){
                	
                	if(_button == null) {
                      _button = new Button();
		              _button.label = "Click";
		              _button.setStyle("fontSize",9);
		              _button.setStyle("rolloverColor", "green");
		              _button.toolTip = "Click button or label to perform action. You can also click the 'Help' button in the top-right corner for detailed information.";
			          _button.width = 40;
			          _button.height = 15;
			          _button.setStyle("paddingLeft", 0);
                      _button.setStyle("paddingRight", 0);
		              _button.addEventListener(MouseEvent.CLICK, handleButtonClick);
		              addChild(_button);
                    }
                	
                    //if the button is visible then
                    //reposition the controls to make room for button
                    _button.x = super.label.x
                    super.label.x = _button.x + this.button.width + 5;//17;
                    //this.chk.y = super.label.y+8;
                    _button.y = super.label.y;
                }
            }
        }
               
        private function handleButtonClick(evt:MouseEvent):void{
            this.dispatchEvent(new ListEvent(ListEvent.CHANGE));
        }
		
	}
}