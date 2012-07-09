package util
{
	import flash.external.*;
	import flash.utils.*;
	
	import mx.core.Application;

	public class UQueryURLString 
	{

		private var _queryString:String;
		private var _all:String;
		//private var _parameters:Object;
		private var _literals:Array;
		private var _parameters:Object;
		
		public function get queryString():String
		{
			return _queryString;
		}
		public function get url():String
		{
			return _all;
		}
		/*public function get parameters():Object
		{
			return _parameters;
		}*/
		public function get literals():Array
		{
			return _literals;
		}			

		
		public function UQueryURLString()
		{
			parseQueryString();
		}
		
		public function get parameters():Object {
			return _parameters;
		}
		
		private function splitQueryString():void {
			
			if(_queryString == null || _queryString.length == 0)
			   return;
			
			_parameters = new Object();
			
			var options:Array = _queryString.split('&');
			var numOfOptions:uint = options.length;
			var position:uint = 0;
			var index:int = -1;
			
			for (position,index; position<numOfOptions; position++) 
			{
				var keyValueString:String = options[position];
				if((index = keyValueString.indexOf("=")) > 0)
				{
					var key:String = keyValueString.substring(0,index);
					var value:String = keyValueString.substring(index+1);
					_parameters[key] = value;
				}
			}
			
		}

		private function parseQueryString():void
		{
			// new parameter object
			//_parameters = {};
			_literals = new Array();
			
			try 
			{
				_all =  ExternalInterface.call("window.location.href.toString");
				_queryString = ExternalInterface.call("window.location.search.substring", 1);
				
				if(_queryString != null)
				{
					/*
					var parameterArray:Array = _queryString.split('&');
					var length:uint = parameterArray.length;
					
					for (var i:uint=0,index:int=-1; i<length; i++) 
					{
						var kvPair:String = parameterArray[i];
						if((index = kvPair.indexOf("=")) > 0)
						{
							var key:String = kvPair.substring(0,index);
							var value:String = kvPair.substring(index+1);
							_parameters[key] = value;
						}
					}
					*/
					
					// split the query string into several pieces
					splitQueryString();
					
					// check if we have a query specified in the url string
					if(!_parameters.hasOwnProperty("query"))
					   return;
					   
					// overwrite the query string
					_queryString = _parameters["query"];   
					
					var literalArray:Array = _queryString.split('+');
					var literalSPO:Array;
					var length:uint = literalArray.length;
					var str:String = "";
					//var pattern1:RegExp = /\%22/;//gi;
					//var pattern2:RegExp = /%20/;//gi;

					
					
					for (var i:uint=0,index:int=-1; i<length; i++) 
					{
						var literal:String = literalArray[i];
						literalSPO = literal.split(',');
						//mx.core.Application.application.DebugInput.text = literalSPO.toString();
						for(var pos:int = 0; pos<literalSPO.length;pos++) {
							str = literalSPO[pos];
							str = (str.indexOf("\%22") > 0) ? ("\"" + str.substr(4,str.length-1)) : str;
							str = UStringUtil.replace(str,"%20"," ");
							str = (str.indexOf("\%22") > 0) ? (str.substr(0,str.length-4) + "\"") : str;
							literalSPO[pos] = str;
							//str = str.replace(pattern1,"\"");//("\%22","\"");
							//str = str.replace(pattern2," ");//("%20"," ");
						}
						//mx.core.Application.application.DebugInput2.text = literalSPO.toString();
						
						// we need all three constituents of the literal -> SPO=Subject,Predicate,Object
						if(literalSPO == null || literalSPO.length < 3)
						   continue;
						
						// add the literal   
						_literals.push(literalSPO);
						
						/*   
						if((index = literal.indexOf("=")) > 0)
						{
							var key:String = kvPair.substring(0,index);
							var value:String = kvPair.substring(index+1);
							_parameters[key] = value;
						}
						*/
					}
				}
			}catch(e:Error) { trace("Some error occured. ExternalInterface doesn't work in Standalone player."); }
		}

	}
}
