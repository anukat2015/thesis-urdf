package uviface.ui.query
{
	import mx.containers.Panel;
	import mx.controls.Button;
	import mx.controls.Label;

	public class UQueryErrorPanel extends Panel
	{
		private var _firstLineLabel:Label = new Label();
		private var _secondLineLabel:Label = new Label();
		private var _okButton:Button = new Button();
		
		public function UQueryErrorPanel()
		{
			//TODO: implement function
			super();
			
			title="Query Constant Error!";
			layout="absolute"; 
	        width=300; 
			height=200; 
			this.setStyle("backgroundColor","#EBE806");
			this.setStyle("textAlign","center");
			this.setStyle("fontSize",15);
		    this.setStyle("fontWeight","bold"); 
		    this.setStyle("borderColor","#746565"); 
		    this.setStyle("barColor",0xFFFFFF00); 
		    this.setStyle("headerColors",[0xFFFFFF00]);  
		    this.setStyle("color","#030000"); 
		    this.setStyle("cornerRadius",8); 
		    this.setStyle("alpha",1.0); 
		    this.setStyle("backgroundAlpha",0.8); 
		    this.setStyle("borderAlpha",0.5);
		    
		    _firstLineLabel.id = "FirstLineLabel";
		    _firstLineLabel.text = "You must specify at";
		    _firstLineLabel.x = 10;
		    _firstLineLabel.y = 25;
		    _firstLineLabel.width = 260;
		    _firstLineLabel.height = 39;
		    _firstLineLabel.setStyle("fontWeight","bold");
		    _firstLineLabel.setStyle("fontSize",20);
		    _firstLineLabel.setStyle("textAlign","center");
		    
		    _secondLineLabel.id = "SecondLineLabel";
		    _secondLineLabel.text = "least one constant!";
		    _secondLineLabel.x = 10;
		    _secondLineLabel.y = 66;
		    _secondLineLabel.width = 260;
		    _secondLineLabel.height = 39;
		    _secondLineLabel.setStyle("fontWeight","bold");
		    _secondLineLabel.setStyle("fontSize",20);
		    _secondLineLabel.setStyle("textAlign","center");
		    
		    _okButton.id = "OKButton";
		    _okButton.x = 114.5;
		    _okButton.y = 113;
		    _okButton.label = "OK";
		    _okButton.setStyle("fillAlphas",[0.8, 0.8, 0.8, 0.8]);
		    
		    this.addChild(_firstLineLabel);
		    this.addChild(_secondLineLabel);
		    this.addChild(_okButton);
		    
		}
		
		public function get firstLineLabel():Label {
			return _firstLineLabel;
		}
		
		public function get secondLineLabel():Label {
			return _secondLineLabel;
		}
		
		public function get okButton():Button {
			return _okButton;
		}
		
		
		
	}
}