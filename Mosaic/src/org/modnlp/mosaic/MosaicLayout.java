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

/**
 *
 * @author shane
 */

import prefuse.action.layout.Layout;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;


/**
 * Implements a uniform grid-based layout. This component can either use
 * preset grid dimensions or analyze a grid-shaped graph to determine them
 * automatically.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class MosaicLayout extends Layout {

    protected int rows;
    protected int cols;
   
    
    /**
     * Create a new GridLayout without preset dimensions. The layout will
     * attempt to analyze an input graph to determine grid parameters.
     * @param group the data group to layout. In this automatic grid
     * analysis configuration, the group <b>must</b> resolve to a set of
     * graph nodes.
     */
    public MosaicLayout(String group, int nrows, int ncols) {
        super(group);
        rows = nrows;
        cols = ncols;
       
    }
    
    
    
    /**
     * @see prefuse.action.Action#run(double)
     */
    public void run(double frac) {
        Rectangle2D b = getLayoutBounds();
        double bx = b.getMinX(), by = b.getMinY();
        double w = b.getWidth(), h = b.getHeight();
        int height_used =0;
        int previous_column=0;
        TupleSet ts = m_vis.getGroup(m_group);
        int m = rows, n = cols;
       
        Iterator iter = ts.tuples();
        // layout grid contents
       
        for ( int i=0; iter.hasNext() && i < m*n; ++i ) {
            VisualItem item = (VisualItem)iter.next();
            if((Integer)item.get("column")==4){
                //item.setFillColor(ColorLib.color(java.awt.Color.BLUE));
                item.setEndFillColor(item.getFillColor());
                item.setInteractive(false);
            
            }
            
            if(previous_column<(Integer)item.get("column")){
                height_used=0;
            }
            
            item.setVisible(true);
            int xoffset = 98*((Integer)item.get("column"));
            double x = bx + (double)xoffset;
           
            
            double y = by + height_used;
           
            setX(item,null,x);
            setY(item,null,y);
            height_used +=  Math.round((450 )* ((Double) item.get("frequency")));
            previous_column=(Integer)item.get("column");
            
        }
        // set left-overs invisible
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            item.setVisible(false);
        }
    }
    
   
    /**
     * Get the number of grid columns.
     * @return the number of grid columns
     */
    public int getNumCols() {
        return cols;
    }
    
    /**
     * Set the number of grid columns.
     * @param cols the number of grid columns to use
     */
    public void setNumCols(int cols) {
        this.cols = cols;
    }
    
    /**
     * Get the number of grid rows.
     * @return the number of grid rows
     */
    public int getNumRows() {
        return rows;
    }
    
    /**
     * Set the number of grid rows.
     * @param rows the number of grid rows to use
     */
    public void setNumRows(int rows) {
        this.rows = rows;
    }
    
} // end of class GridLayout

