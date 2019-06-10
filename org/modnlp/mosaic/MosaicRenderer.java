/**
 *  (c) 2014 S Sheehan <shane.sheehan@tcd.ie>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*/
package org.modnlp.mosaic;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import prefuse.render.AbstractShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.StrokeLib;
import prefuse.visual.VisualItem;

/**
 *
 * @author shane
 */
public class MosaicRenderer extends AbstractShapeRenderer
{
	//protected RectangularShape m_box = new Rectangle2D.Double();
	protected Rectangle2D m_box = new Rectangle2D.Double();
        protected int rows;
        protected int heigth;
        public int width = 98;
        
        public MosaicRenderer( int nrows, int nheigth) {
            rows=nrows;
            heigth= nheigth;
        
       
    }
	
	@Override
	protected Shape getRawShape(VisualItem item ) 
	{	
              
            double h=item.getSize();
            if(item.getSize()==1)
                h= Math.floor(heigth* ((Double) item.get("frequency")));
            item.set("height", (int)h);
            //adding 1 to boxes to correct heights of cols, calculated in layout
            int toAdd=(Integer)item.get("add1");
            h+= toAdd;
                
		m_box.setFrame(item.getX(), item.getY(),width,h);

		return m_box;
	}
}

