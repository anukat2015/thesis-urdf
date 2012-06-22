package util
{
	
	/**
	 * This class represents a library class for mathematical functions.
	 * 
	 * @author Timm Meiser
	 * @since 07.01.10
	 * @version 1.0
	 */
	public class UMath
	{

        /**
         * Compares to given integer values and returns either -1, 0 or 1, depending 
         * on the outcome of the comparison.
         * 
         * @param number1 the first number to compare.
         * @param number2 the number to which number1 should be compared.
         * @return -1, if number1 is smaller as number2, 
         *     <p>  0, if number1 equals number2 and
         *     <p>  1, if number1 is bigger than number2.
         * */
        public static function compareInt(number1:int, number2:int):int {
        	
        	if(number1 < number2)
        	   return -1;
        	if(number1 == number2)
        	   return 0;
        	else
        	   return 1;
        	
        }
        
        /**
         * Compares to given Number values and returns either -1, 0 or 1, depending 
         * on the outcome of the comparison.
         * 
         * @param number1 the first number to compare.
         * @param number2 the number to which number1 should be compared.
         * @return -1, if number1 is smaller as number2, 
         *     <p>  0, if number1 equals number2 and
         *     <p>  1, if number1 is bigger than number2.
         * */
        public static function compareNumber(number1:Number, number2:Number):int {
        	
			if (isNaN(number1)) {
				
				if (isNaN (number2)) 
				    return 0;
				else 
				    return 1;		
			}
			
			if (isNaN(number2)) {
				
				if (isNaN (number1)) 
				    return 0;
				else 
				    return -1;		
			}
			
        	if(number1 < number2)
        	   return -1;
        	if(number1 == number2)
        	   return 0;
        	else
        	   return 1;
        	
        }
        
	    
	    /** Rounds a number with a defined numbers of decimals. */
		public static function roundNumber(number:Number, decimals:Number):Number {
			//var factor:Number = Number(Math.ceil(decimals*10));
			//trace("factor : " + factor);
			var value:Number = (Math.round(number*Math.pow(10,decimals)))/Math.pow(10,decimals);
			//trace("value : " + value);
			return value;
		}
		
		/** Rounds a number with a defined numbers of decimals. */
		public static function roundNumberDirect(number:Number, decimals:Number):void {
			//var factor:Number = Number(Math.ceil(decimals*10));
			//trace("factor : " + factor);
			number = (Math.round(number*Math.pow(10,decimals)))/Math.pow(10,decimals);
			//trace("value : " + value);
			return;
		}
		
		public static function toDegrees(theta:Number):Number {
		   return theta * 180 / Math.PI;
		}
		
		public static function toRadians(theta:Number):Number {
		   return theta * Math.PI / 180;
		}
		
		//public static function toRadians(theta:Number):Number {
		  // return theta * Math.PI / 180;
		//}
		
		public static function cosd(theta:Number):Number {
		   return Math.cos(toRadians(theta));
		}
		
		public static function sind(theta:Number):Number {
		   return Math.sin(toRadians(theta));
		}


	}
}