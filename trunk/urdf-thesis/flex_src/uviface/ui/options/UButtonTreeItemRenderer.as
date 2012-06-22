package uviface.ui.options
{
	import flash.events.MouseEvent;
	
	import mx.controls.Button;
	import mx.controls.treeClasses.*;
	import mx.events.ListEvent;
	import uviface.viz.util.UStringConstants;

	public class UButtonTreeItemRenderer extends TreeItemRenderer
	{
		private var _button:Button;
		private var _clickable:Boolean = false;
        //private var _itemXml:XML;
		
		public function UButtonTreeItemRenderer()
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
            /* _button.setStyle("fontSize",10);
            _button.setStyle("fontFamily","Arial");
            _button.setStyle("fontThickness","bold");
            _button.setStyle("fontColor",0x000000);
            _button.setStyle("rolloverColor", "green");
            _button.setStyle("fillColors", [0xFFFFFF, 0xFFFFFF]); */
            _button.label = "Click";
            _button.toolTip = "Click button or label to perform action. You can also click the 'Help' button in the top-right corner for detailed information.";
            _button.width = _button.label.length * 5 + 10;//40;
            _button.height = 18;
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
			              /* _button.setStyle("fontSize",10);
					      _button.setStyle("fontFamily","Arial");
					      _button.setStyle("fontThickness","bold");
			              _button.setStyle("fontColor",0x000000);
				          _button.setStyle("rolloverColor", "green");
				          _button.setStyle("fillColors", [0xFFFFFF, 0xFFFFFF]); */
				          _button.addEventListener(MouseEvent.CLICK, handleButtonClick);
			              addChild(_button);
                	  }
			          
			          switch(tld.item.key) {
			          	case UStringConstants.FACT: {
			          		_button.label = tld.item[UStringConstants.LABEL_BUTTON];
			                _button.toolTip = "Click the button to show only the selected fact (subject node, predicate edge, object node).";
			                break;
			          	}
			          	case UStringConstants.SUBJECT: {
			          		_button.label = tld.item[UStringConstants.LABEL_BUTTON];
			                _button.toolTip = "Click the button to show only the facts containing the selected subject as subject or object.";
			                break;
			          	}
			          	case UStringConstants.PREDICATE: {
			          		_button.label = tld.item[UStringConstants.LABEL_BUTTON];
			                _button.toolTip = "Click the button to show only the facts containing the selected predicate.";
			                break;
			          	}
			          	case UStringConstants.OBJECT: {
			          		_button.label = tld.item[UStringConstants.LABEL_BUTTON];
			                _button.toolTip = "Click the button to show only the facts containing the selected object as subject or object.";
			                break;
			          	}
			          	case UStringConstants.CONFIDENCE: {
			          		_button.label = tld.item[UStringConstants.LABEL_BUTTON];
			                _button.toolTip = "Click the button to show only the facts attached with exactly the specified confidence value.";
			                break;
			          	}
			          	case UStringConstants.TRUTH_VALUE: {
			          		_button.label = tld.item[UStringConstants.LABEL_BUTTON];
			                _button.toolTip = "Click the button to show only the facts attached with the selected truth value.";
			                break;
			          	}
			          	case UStringConstants.QUERY_RESULT: {
			          		_button.label = tld.item[UStringConstants.LABEL_BUTTON];
			                _button.toolTip = "Click the button to show only the facts belonging to the specified fact result list.";
			                break;
			          	}
			          	case UStringConstants.LINEAGE_FACTS: {
			          		_button.label = tld.item[UStringConstants.LABEL_BUTTON];
			                _button.toolTip = "Click the button to show the explanation tree for the specific answer to the given query.";
			                break;
			          	}
			          	case UStringConstants.COMPLETE_RESULT_LINEAGE: {
			          		_button.label = tld.item[UStringConstants.LABEL_BUTTON];
			                _button.toolTip = "Click the button to show the explanation tree for the complete query result, including all different answers.";
			                break;
			          	}
			          	case UStringConstants.LINEAGE: {
			          		_button.label = tld.item[UStringConstants.LABEL_BUTTON];
			                _button.toolTip = "Click the button to show the explanation tree for the specified fact.";
			                break;
			          	}
			          }
			          
		            //_button.label = "Click";
		            //_button.toolTip = "Click button or label to perform action. You can also click the 'Help' button in the top-right corner for detailed information.";
		            _button.width = _button.label.length * 5 + 10;//40;
		            _button.height = 18;
		            _button.setStyle("paddingLeft", 0);
		            _button.setStyle("paddingRight", 0);
		            _button.invalidateProperties();
                	
                    //if the button is visible then
                    //reposition the controls to make room for button
                    _button.x = super.label.x
                    super.label.x = _button.x + this.button.width + 5;//17;
                    //this.chk.y = super.label.y+8;
                    _button.y = super.label.y;
                    _button.invalidateSize();
                }
            }
        }
               
        private function handleButtonClick(evt:MouseEvent):void{
            this.dispatchEvent(new ListEvent(ListEvent.CHANGE));
        }
		
	}
}