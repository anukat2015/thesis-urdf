package uviface.viz.operator
{
	import flare.display.TextSprite;
	import flare.util.Geometry;
	import flare.util.Shapes;
	import flare.vis.data.DataSprite;
	import flare.vis.data.EdgeSprite;
	import flare.vis.data.NodeSprite;
	import flare.vis.data.render.ArrowType;
	import flare.vis.data.render.EdgeRenderer;
	
	import flash.display.Graphics;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	import uviface.viz.UFlareVis;

	public class UEdgeRenderer extends EdgeRenderer
	{
		// a flag to state if availalbe lineage information for an edge should
		// be indicated by a cross
		private var _indicateLineage:Boolean = false;
		
		private static const ROOT3:Number = Math.sqrt(3);
		
		private static var _instance:UEdgeRenderer = new UEdgeRenderer();
		/** Static EdgeRenderer instance. */
		public static function get instance():UEdgeRenderer { return _instance; }
		
		// temporary variables
		private var _p:Point = new Point(), _q:Point = new Point();
		private var _pts:Array = new Array(20);
		
		public function UEdgeRenderer()
		{
			//TODO: implement function
			super();
		}
		
		public function get indicateLineage():Boolean {
			return _indicateLineage;
		}
		
		public function set indicateLineage(indicateLineage:Boolean):void {
			_indicateLineage = indicateLineage;
		}
		
		/** @inheritDoc */
		public override function render(d:DataSprite):void
		{
			var e:EdgeSprite = d as EdgeSprite;
			if (e == null) { return; } // TODO: throw exception?
			var s:NodeSprite = e.source;
			var t:NodeSprite = e.target;
			var g:Graphics = e.graphics;
			
			var ctrls:Array = e.points as Array;
			var x1:Number = e.x1, y1:Number = e.y1;
			var x2:Number = e.x2, y2:Number = e.y2;
			var xL:Number = ctrls==null ? x1 : ctrls[ctrls.length-2];
			var yL:Number = ctrls==null ? y1 : ctrls[ctrls.length-1];
			var dx:Number, dy:Number, dd:Number;

            // ----------------------------------------------------------
            // adjusted by Timm Meiser on 02.07.10
            // ----------------------------------------------------------
            
            var gap:Number = (e.data.hasOwnProperty("GAP")) ? e.data.GAP : 1;
            //var doc:Number = (e.data.hasOwnProperty("DOC")) ? e.data.DOC : 0;
            var noc:Number = (e.data.hasOwnProperty("NOC")) ? e.data.NOC : 1;
            var ts:TextSprite = e.data.TS as TextSprite;
            ts.alpha = e.lineAlpha;
            
            if (e.shape == Shapes.BEZIER || e.shape == Shapes.BSPLINE || e.shape == Shapes.CARDINAL) {
				//var gap:Number = 20; // Distance between each edge and an imaginary central straight line.
				//var gap:Number = 30; // Distance between each edge and an imaginary central straight line.
				
				//if((e.data.DOC + e.data.DIC) == 1)
				if(noc == 1) {
				   e.shape = Shapes.LINE; 
				   // new since 22.10.10
	               ts.x = (x1+x2)/2;
	               ts.y = (y1+y2)/2;
	               // we are already in the lineage mode
			       if(!UFlareVis.lineageMode)
			           ts.rotation = UShapes.computeEdgeLabelRotation(x1,y1,x2,y2);
				}
				else {
					ctrls = getControlPoint(gap, x1, y1, x2, y2);
			        // new since 22.10.10
	                //ts.x = ctrls[0]; 
	                //ts.y = ctrls[1];
	            }
            }
            else {  
	            // new since 22.10.10
	            ts.x = (x1+x2)/2;
	            ts.y = (y1+y2)/2;
	            // alter the y-position of the label to avoid overlapping labels
	            // between sibling trees 
	            /*
	            if(UFlareVis.lineageMode) {
	            	ts.x = Math.min(x1,x2) + (Math.abs(Math.random() - 0.5)) * Math.abs(x1-x2);//(Math.abs(Math.random() - 0.1));
	            	ts.y = Math.min(y1,y2) + (Math.abs(Math.random() - 0.5)) * Math.abs(y1-y2);//(Math.abs(Math.random() - 0.1));
	            }	
	            else
	               ts.rotation = UShapes.computeEdgeLabelRotation(x1,y1,x2,y2);	
	            */
	            if(!UFlareVis.lineageMode)
	               ts.rotation = UShapes.computeEdgeLabelRotation(x1,y1,x2,y2);	
	             		
            }
           // }
            
            // ----------------------------------------------------------
            // adjusted by Timm Meiser on 02.07.10
            // ----------------------------------------------------------

			// modify end points as needed to accomodate arrow
			if (e.arrowType != ArrowType.NONE)
			{
				// determine arrow head size
				var ah:Number = e.arrowHeight, aw:Number = e.arrowWidth/2;
				if (ah < 0 && aw < 0)
					aw = 1.5 * e.lineWidth;
				if (ah < 0) {
					ah = ROOT3 * aw;
				} else if (aw < 0) {
					aw = ah / ROOT3;
				}
				
				// get arrow tip point as intersection of edge with bounding box
				if (t==null) {
					_p.x = x2; _p.y = y2;
				} else {
					var r:Rectangle = t.getBounds(t.parent);
					if (Geometry.intersectLineRect(xL,yL,x2,y2, r, _p,_q) <= 0)
					{
						_p.x = x2; _p.y = y2;
					}
				}
				
				// get unit vector along arrow line
				dx = _p.x - xL; dy = _p.y - yL;
				dd = Math.sqrt(dx*dx + dy*dy);
				dx /= dd; dy /= dd;
				
				// set final point positions
				dd = e.lineWidth/2;
				// if drawing as lines, offset arrow tip by half the line width
				if (e.arrowType == ArrowType.LINES) {
					_p.x -= dd*dx;
					_p.y -= dd*dy;
					dd += e.lineWidth;
				}
				// offset the anchor point (the end point for the edge connector)
				// so that edge doesn't "overshoot" the arrow head
				dd = ah - dd;
				x2 = _p.x - dd*dx;
				y2 = _p.y - dd*dy;
			}

			// draw the edge
			g.clear(); // clear it out
			setLineStyle(e, g); // set the line style
			if (e.shape == Shapes.BEZIER && ctrls != null && ctrls.length > 1) {
				if (ctrls.length < 4)
				{
					g.moveTo(x1, y1);
					g.curveTo(ctrls[0], ctrls[1], x2, y2);
				}
				else
				{
					Shapes.drawCubic(g, x1, y1, ctrls[0], ctrls[1],
									 ctrls[2], ctrls[3], x2, y2);
				}
			}
			else if (e.shape == Shapes.CARDINAL)
			{
				Shapes.consolidate(x1, y1, ctrls, x2, y2, _pts);
				Shapes.drawCardinal(g, _pts, 2+ctrls.length/2);
			}
			// -------------------
			// changed on 27.10.10
			// -------------------
			else if (e.shape == Shapes.BSPLINE)
			{
				UShapes.consolidate(x1, y1, ctrls, x2, y2, _pts);
				UShapes.drawBSpline(g, _pts, ts, 2+ctrls.length/2);
				// new since 27.10.10
	            //ts.x = _pts[0]; 
	            //ts.y = _pts[1];
	            ts.rotation = UShapes.computeEdgeLabelRotation(x1,y1,x2,y2);
	            // now, draw a cross to indicate available lineage information
	            if(e.data.hasOwnProperty("lineageAvailable") && e.data.lineageAvailable && _indicateLineage) {
	            	g.moveTo(ts.x,ts.y);
	            	UShapes.drawCrossAt(g,ts.x,ts.y,10);
	            	UShapes.drawXAt(g,ts.x,ts.y,10);
	            }
			}
			else
			{
				g.moveTo(x1, y1);
				if (ctrls != null) {
					for (var i:uint=0; i<ctrls.length; i+=2)
						g.lineTo(ctrls[i], ctrls[i+1]);
				}
				g.lineTo(x2, y2);
				
				// new on 03.02.2011
				// now, draw a cross to indicate available lineage information
	            if(e.data.hasOwnProperty("lineageAvailable") && e.data.lineageAvailable && _indicateLineage) {
	            	g.moveTo(ts.x,ts.y);
	            	UShapes.drawCrossAt(g,ts.x,ts.y,10);
	            	UShapes.drawXAt(g,ts.x,ts.y,10);
	            }
			}
			
			// draw an arrow
			if (e.arrowType != ArrowType.NONE) {
				// get other arrow points
				x1 = _p.x - ah*dx + aw*dy; y1 = _p.y - ah*dy - aw*dx;
				x2 = _p.x - ah*dx - aw*dy; y2 = _p.y - ah*dy + aw*dx;
								
				if (e.arrowType == ArrowType.TRIANGLE) {
					g.lineStyle();
					g.moveTo(_p.x, _p.y);
					g.beginFill(e.lineColor, e.lineAlpha);
					g.lineTo(x1, y1);
					g.lineTo(x2, y2);
					g.endFill();
				} else if (e.arrowType == ArrowType.LINES) {
					g.moveTo(x1, y1);
					g.lineTo(_p.x, _p.y);
					g.lineTo(x2, y2);
				}
			}
		}
		
		/*
		public override function render(d:DataSprite):void
		{
			var e:EdgeSprite = d as EdgeSprite;
			if (e == null) { return; } // TODO: throw exception?
			var s:NodeSprite = e.source;
			var t:NodeSprite = e.target;
			var g:Graphics = e.graphics;
			
			var ctrls:Array = e.points as Array;
			var x1:Number = e.x1, y1:Number = e.y1;
			var x2:Number = e.x2, y2:Number = e.y2;
			var xL:Number = ctrls==null ? x1 : ctrls[ctrls.length-2];
			var yL:Number = ctrls==null ? y1 : ctrls[ctrls.length-1];
			var dx:Number, dy:Number, dd:Number;

            // ----------------------------------------------------------
            // adjusted by Timm Meiser on 02.07.10
            // ----------------------------------------------------------
            
            var duplicateOutCounter:int = 0;
            var duplicateInCounter:int = 0;
            var childNumber:int = 1;
            var pos:int = 0;
            var childEdge:EdgeSprite;
            var childNode:NodeSprite;
            	
            for(pos; pos < s.outDegree; pos++) {
            	childEdge = s.getOutEdge(pos);
            	//trace("childEdge.data.label: " + childEdge.data.label);
            	
            	// we have the same target node for the source node
            	if(childEdge.target.data.label == t.data.label) {
            		// this is our current edge
            		if(childEdge.data.factID == e.data.factID) {
            		   duplicateOutCounter++;
            		   childNumber = duplicateOutCounter;   
            		}
            		// we have another edge with the same source and target node
            		else
            		   duplicateOutCounter++; 
            	}
            	
            }
            
            for(pos = 0; pos < t.outDegree; pos++) {
            	childEdge = t.getOutEdge(pos);
            	//trace("childEdge.data.label: " + childEdge.data.label);
            	
            	// we have the same target node for the source node
            	if(childEdge.target.data.label == s.data.label) {
            		duplicateInCounter++; 
                }
            }

            if (e.shape == Shapes.BEZIER || e.shape == Shapes.BSPLINE || e.shape == Shapes.CARDINAL) {
				//var gap:Number = 20; // Distance between each edge and an imaginary central straight line.
				//var gap:Number = 30; // Distance between each edge and an imaginary central straight line.
				
				if((duplicateOutCounter + duplicateInCounter) == 1)
				   e.shape = Shapes.LINE; 
				 else {
					var gap:Number = childNumber * 20;
					//if(((childNumber - 1) % 2) == 1.0)
					  // gap *= childNumber;
					//else
					  // gap *= -childNumber; 
					//var gap:Number = childNumber * 20; // Distance between each edge and an imaginary central straight line.
					ctrls = getControlPoint(gap, x1, y1, x2, y2);
				}
            }
            
           // }
            
            // ----------------------------------------------------------
            // adjusted by Timm Meiser on 02.07.10
            // ----------------------------------------------------------

			// modify end points as needed to accomodate arrow
			if (e.arrowType != ArrowType.NONE)
			{
				// determine arrow head size
				var ah:Number = e.arrowHeight, aw:Number = e.arrowWidth/2;
				if (ah < 0 && aw < 0)
					aw = 1.5 * e.lineWidth;
				if (ah < 0) {
					ah = ROOT3 * aw;
				} else if (aw < 0) {
					aw = ah / ROOT3;
				}
				
				// get arrow tip point as intersection of edge with bounding box
				if (t==null) {
					_p.x = x2; _p.y = y2;
				} else {
					var r:Rectangle = t.getBounds(t.parent);
					if (Geometry.intersectLineRect(xL,yL,x2,y2, r, _p,_q) <= 0)
					{
						_p.x = x2; _p.y = y2;
					}
				}
				
				// get unit vector along arrow line
				dx = _p.x - xL; dy = _p.y - yL;
				dd = Math.sqrt(dx*dx + dy*dy);
				dx /= dd; dy /= dd;
				
				// set final point positions
				dd = e.lineWidth/2;
				// if drawing as lines, offset arrow tip by half the line width
				if (e.arrowType == ArrowType.LINES) {
					_p.x -= dd*dx;
					_p.y -= dd*dy;
					dd += e.lineWidth;
				}
				// offset the anchor point (the end point for the edge connector)
				// so that edge doesn't "overshoot" the arrow head
				dd = ah - dd;
				x2 = _p.x - dd*dx;
				y2 = _p.y - dd*dy;
			}

			// draw the edge
			g.clear(); // clear it out
			setLineStyle(e, g); // set the line style
			if (e.shape == Shapes.BEZIER && ctrls != null && ctrls.length > 1) {
				if (ctrls.length < 4)
				{
					g.moveTo(x1, y1);
					g.curveTo(ctrls[0], ctrls[1], x2, y2);
				}
				else
				{
					Shapes.drawCubic(g, x1, y1, ctrls[0], ctrls[1],
									 ctrls[2], ctrls[3], x2, y2);
				}
			}
			else if (e.shape == Shapes.CARDINAL)
			{
				Shapes.consolidate(x1, y1, ctrls, x2, y2, _pts);
				Shapes.drawCardinal(g, _pts, 2+ctrls.length/2);
			}
			else if (e.shape == Shapes.BSPLINE)
			{
				Shapes.consolidate(x1, y1, ctrls, x2, y2, _pts);
				Shapes.drawBSpline(g, _pts, 2+ctrls.length/2);
			}
			else
			{
				g.moveTo(x1, y1);
				if (ctrls != null) {
					for (var i:uint=0; i<ctrls.length; i+=2)
						g.lineTo(ctrls[i], ctrls[i+1]);
				}
				g.lineTo(x2, y2);
			}
			
			// draw an arrow
			if (e.arrowType != ArrowType.NONE) {
				// get other arrow points
				x1 = _p.x - ah*dx + aw*dy; y1 = _p.y - ah*dy - aw*dx;
				x2 = _p.x - ah*dx - aw*dy; y2 = _p.y - ah*dy + aw*dx;
								
				if (e.arrowType == ArrowType.TRIANGLE) {
					g.lineStyle();
					g.moveTo(_p.x, _p.y);
					g.beginFill(e.lineColor, e.lineAlpha);
					g.lineTo(x1, y1);
					g.lineTo(x2, y2);
					g.endFill();
				} else if (e.arrowType == ArrowType.LINES) {
					g.moveTo(x1, y1);
					g.lineTo(_p.x, _p.y);
					g.lineTo(x2, y2);
				}
			}
		}
		 
		 */
		
		/**
		 * Sets the line style for edge rendering.
		 * @param e the EdgeSprite to render
		 * @param g the Graphics context to draw with
		 */
		protected override function setLineStyle(e:EdgeSprite, g:Graphics):void
		{
			var lineAlpha:Number = e.lineAlpha;
			if (lineAlpha == 0) return;
			
			g.lineStyle(e.lineWidth, e.lineColor, lineAlpha, 
				pixelHinting, scaleMode, caps, joints, miterLimit);
		}
		
		/** 
		 * Delivers a control point to draw the curved line.
		 * 
		 * Thanks to http://sourceforge.net/projects/prefuse/forums/forum/757572/topic/2674969?message=5824928
		 *  */
		private function getControlPoint(h:Number, x1:Number, y1:Number, x2:Number, y2:Number):Array {
			var ratio:Number = (y1-y2)/(x1-x2);
			var yn:Number = Math.sqrt(h*h/(ratio*ratio+1));
			var xn:Number = Math.sqrt(h*h-yn*yn);
			
			if (ratio > 0 && (x1 > x2 || y1 < y2)) { yn *= -1; };
			if (h < 0) { yn *= -1; ; xn *= -1; };
			if (y2 > y1) { yn *= -1; xn *= -1; };
			
			var x3:Number = x1+(x2-x1)/2.0+xn;
			var y3:Number = y1+(y2-y1)/2.0+yn;
			
			return [x3, y3];
			}
		
	}
}