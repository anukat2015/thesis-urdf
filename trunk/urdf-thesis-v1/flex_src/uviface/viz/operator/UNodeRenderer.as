package uviface.viz.operator
{
	import flare.vis.data.DataSprite;
	import flare.vis.data.render.IRenderer;
	//import flare.util.Shapes;
	
	import flash.display.Graphics;

    /**
	 * Renderer that draws shapes. The ShapeRender uses a ShapePalette instance
	 * as needed to look up shape drawing routines based on the DataSprite
	 * <code>shape</code> property.
	 * @see flare.vis.palette.ShapePalette
	 */
	public class UNodeRenderer implements IRenderer
	{
		private static var _instance:UNodeRenderer = new UNodeRenderer();
		/** Static ShapeRenderer instance. */
		public static function get instance():UNodeRenderer { return _instance; }
		
		/** The default size value for drawn shapes. This value is multiplied
		 *  by a DataSprite's size property to determine the final size. */
		public var defaultSize:Number;
		
		/**
		 * Creates a new ShapeRenderer 
		 * @param defaultSize the default size (radius) for shapes
		 */
		public function UNodeRenderer(defaultSize:Number=6) {
			this.defaultSize = defaultSize;
		}
		
		/** @inheritDoc */
		public function render(d:DataSprite):void
		{
			var lineAlpha:Number = d.lineAlpha;
			var fillAlpha:Number = d.fillAlpha;
			var size:Number = d.size * defaultSize;
			
			var g:Graphics = d.graphics;
			g.clear();
			if (fillAlpha > 0) g.beginFill(d.fillColor, fillAlpha);
			if (lineAlpha > 0) g.lineStyle(d.lineWidth, d.lineColor, lineAlpha);
			
			switch (d.shape) {
				case null:
					break;
				case UShapes.BLOCK:
					//g.drawRect(d.u-d.x, d.v-d.y, d.w, d.h);
					g.drawRect(d.data.rectBounds[0], d.data.rectBounds[1]-(0.5*d.data.rectBounds[3]), d.data.rectBounds[2], d.data.rectBounds[3]*2);
					break;
				case UShapes.POLYGON:
					if (d.points!=null)
						UShapes.drawPolygon(g, d.points);
					break;
				case UShapes.POLYBLOB:
					if (d.points!=null)
						UShapes.drawCardinal(g, d.points,
											d.points.length/2, 0.15, true);
					break;
				case UShapes.VERTICAL_BAR:
					g.drawRect(-size/2, -d.h, size, d.h); 
					break;
				case UShapes.HORIZONTAL_BAR:
					g.drawRect(-d.w, -size/2, d.w, size);
					break;
				case UShapes.WEDGE:
					UShapes.drawWedge(g, d.origin.x-d.x, d.origin.y-d.y,
									 d.h, d.v, d.u, d.u+d.w);
				
				// d.data.rectBounds is a dynamic property set in UFlareVis.drawNode(node);
				case UShapes.CIRCLE:
					//g.drawRect(d.u-d.x, d.v-d.y, d.w, d.h);
					g.drawCircle(0, 0, d.data.rectBounds[2]/2);
					break;
				case UShapes.DIAMOND:
					UShapes.drawDiamond(g, d.data.rectBounds);
					break;
				case UShapes.ROUNDED_RECT:
					UShapes.drawRoundRect(g, d.data.rectBounds);
					break;
			    case UShapes.COMPLEX_ROUNDED_RECT:
					UShapes.drawRoundRectComplex(g, d.data.rectBounds);
					break;
				
									 
					break;
				default:
					UShapes.getShape(d.shape)(g, size);
			}
			
			if (fillAlpha > 0) g.endFill();
		}
		
	} // end of class ShapeRenderer
}