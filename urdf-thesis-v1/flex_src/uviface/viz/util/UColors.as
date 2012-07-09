package uviface.viz.util
{
	import mx.utils.ColorUtil;
	
	/** The colors are taken from the website http://www.websidesearch.de/content/farben1.php . */
	public class UColors
	{
		/** The blackest black that is possible. */
		public static const BLACK:uint = 0xff000000;
		/** The whitest white that is possible. */
		public static const WHITE:uint = 0xffffffff;//0x00000000;
		
		public function UColors()
		{
			//TODO: implement function
		}
		
		/**
		 * Delivers a red color.
		 * 
		 * The brightness specifies the brightness of the red color (how red the colour is).
		 * This value is limited to the range of 0 to 7, with 0=dark, 7=bright and everything else=default value.
		 * 
		 * @param brightness the brightness of the returned red color. 
		 * */
		public static function red(brightness:uint):uint {
           switch(brightness) {
           	  case 0:
		         return 0xff180000;
		         //break;
		      case 1:
		         return 0xff390000;
		         //break;
		      case 2:
		         return 0xff5a0000;
		         //break;
		      case 3:
		         return 0xff7b0000;
		         //break;
		      case 4:
		         return 0xff9c0000;
		         //break;
		      case 5:
		         return 0xffbd0000;
		         //break;
		      case 6:
		         return 0xffde0000;
		         //break;
		      case 7:
		         return 0xffff0000;
		         //break;
		      default : return 0xffff0000;
           }
        }
        
        /**
		 * Delivers a green color.
		 * 
		 * The brightness specifies the brightness of the green color (how green the colour is).
		 * This value is limited to the range of 0 to 7, with 0=dark, 7=bright and everything else=default value.
		 * 
		 * @param brightness the brightness of the returned green color. 
		 * */
		public static function green(brightness:uint):uint {
           switch(brightness) {
           	  case 0:
		         return 0xff001800;
		         //break;
		      case 1:
		         return 0xff003900;
		         //break;
		      case 2:
		         return 0xff005a00;
		         //break;
		      case 3:
		         return 0xff007b00;
		         //break;
		      case 4:
		         return 0xff009c00;
		         //break;
		      case 5:
		         return 0xff00bd00;
		         //break;
		      case 6:
		         return 0xff00de00;
		         //break;
		      case 7:
		         return 0xff00ff00;
		         //break;
		      default : return 0xff00ff00;
           }
        }
        
        /**
		 * Delivers a blue color.
		 * 
		 * The brightness specifies the brightness of the blue color (how blue the colour is).
		 * This value is limited to the range of 0 to 7, with 0=dark, 7=bright and everything else=default value.
		 * 
		 * @param brightness the brightness of the returned blue color. 
		 * */
		public static function blue(brightness:uint):uint {
           switch(brightness) {
           	  case 0:
		         return 0xff000018;
		         //break;
		      case 1:
		         return 0xff000039;
		         //break;
		      case 2:
		         return 0xff00005a;
		         //break;
		      case 3:
		         return 0xff00007b;
		         //break;
		      case 4:
		         return 0xff00009c;
		         //break;
		      case 5:
		         return 0xff0000bd;
		         //break;
		      case 6:
		         return 0xff0000de;
		         //break;
		      case 7:
		         return 0xff0000ff;
		         //break;
		      default : return 0xff0000ff;
           }
        }
        
        /**
		 * Delivers a yellow color.
		 * 
		 * The brightness specifies the brightness of the yellow color (how redyellowthe colour is).
		 * This value is limited to the range of 0 to 7, with 0=dark, 7=bright and everything else=default value.
		 * 
		 * @param brightness the brightness of the returned yellow color. 
		 * */
		public static function yellow(brightness:uint):uint {
           switch(brightness) {
           	  case 0:
		         return 0xff181800;
		         //break;
		      case 1:
		         return 0xff393900;
		         //break;
		      case 2:
		         return 0xff5a5a00;
		         //break;
		      case 3:
		         return 0xff7b7b00;
		         //break;
		      case 4:
		         return 0xff9c9c00;
		         //break;
		      case 5:
		         return 0xffbdbd00;
		         //break;
		      case 6:
		         return 0xffdede00;
		         //break;
		      case 7:
		         return 0xffffff00;
		         //break;
		      default : return 0xffffff00;
           }
        }
        
        /**
		 * Delivers a magenta color.
		 * 
		 * The brightness specifies the brightness of the magenta color (how magenta the colour is).
		 * This value is limited to the range of 0 to 7, with 0=dark, 7=bright and everything else=default value.
		 * 
		 * @param brightness the brightness of the returned magenta color. 
		 * */
		public static function magenta(brightness:uint):uint {
           switch(brightness) {
           	  case 0:
		         return 0xff180018;
		         //break;
		      case 1:
		         return 0xff390039;
		         //break;
		      case 2:
		         return 0xff5a005a;
		         //break;
		      case 3:
		         return 0xff7b007b;
		         //break;
		      case 4:
		         return 0xff9c009c;
		         //break;
		      case 5:
		         return 0xffbd00bd;
		         //break;
		      case 6:
		         return 0xffde00de;
		         //break;
		      case 7:
		         return 0xffff00ff;
		         //break;
		      default : return 0xffff00ff;
           }
        }
        
         /**
		 * Delivers a grey color.
		 * 
		 * The brightness specifies the brightness of the magenta color (how grey the colour is).
		 * This value is limited to the range of 0 to 7, with 0=dark, 7=bright and everything else=default value.
		 * 
		 * @param brightness the brightness of the returned grey color. 
		 * */
		public static function grey(brightness:uint):uint {
           switch(brightness) {
           	  case 0:
		         return 0xff181818;
		         //break;
		      case 1:
		         return 0xff393939;
		         //break;
		      case 2:
		         return 0xff5a5a5a;
		         //break;
		      case 3:
		         return 0xff7b7b7b;
		         //break;
		      case 4:
		         return 0xff9c9c9c;
		         //break;
		      case 5:
		         return 0xffbdbdbd;
		         //break;
		      case 6:
		         return 0xffdedede;
		         //break;
		      case 7:
		         return 0xffffffff;
		         //break;
		      default : return 0xffff00ff;
           }
        }
  
	}
}