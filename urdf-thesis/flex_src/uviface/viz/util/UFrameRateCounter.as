package uviface.viz.util
{
	
   import flash.display.Sprite;
   import flash.events.Event;
   import flash.utils.setInterval;
   /**
    * @class FPSMeter
    * @interface none
    * @author ABertl
    * @version 0.2b
    * @description Viewer for real FPS on console
    */
   public class UFrameRateCounter extends Sprite
   {

      private var j:int = 0;

      public function UFrameRateCounter ()
      {
         this.addEventListener ( Event.ENTER_FRAME, onEnterFrame ,false,0,true);
      }
       
      private function onEnterFrame ( event: Event ):void
      {
         j++;
      }
      
      public function showFPS():int
      {
  	 	var fps:int = j;// * updateRate;
        //trace(j);
        j = 0;
        return fps;	 
      }

   }
} 
	