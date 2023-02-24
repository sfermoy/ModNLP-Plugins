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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    int width = 98;
    int heigth =450;
   
    
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
        List<VisualItem> items_in_coulmn = new ArrayList<VisualItem>();
        boolean isRelFreq =false;
         boolean isSlider =false;
       
        Iterator iter = ts.tuples();
        // layout grid contents
       
        for ( int i=0; iter.hasNext() && i < m*n; ++i ) {
            VisualItem item = (VisualItem)iter.next();
            if(i==0){
                isRelFreq= (Boolean)item.get("rel_freq");
                isSlider = (Boolean)item.get("isStopwordView");
                
            }
            
            if((Integer)item.get("column") == 4){
                //item.setFillColor(ColorLib.color(java.awt.Color.BLUE));
                item.setEndFillColor(item.getFillColor());
                //item.setInteractive(false);
            
            }
            
            if(previous_column<(Integer)item.get("column")){
                if(!isRelFreq){
                    if(height_used < heigth){
                        int totalToAdd = 0;
                        int itemNumber = 0;
                        int amountToAdd = 1;
                        int num_items = m;
                        while(height_used < heigth){
                            VisualItem modify = items_in_coulmn.get(itemNumber);
                            setY(modify, null, modify.getY() + (amountToAdd*itemNumber));
                            modify.set("add1", amountToAdd);
                            totalToAdd += amountToAdd;
                            itemNumber++;

                            if(itemNumber > num_items){
                                itemNumber = 0;
                                amountToAdd += 1;
                            }
                            height_used++;
                        }
                        int colLength = items_in_coulmn.size();
                        for(int x = itemNumber; x < colLength; x++){
                            VisualItem modify = items_in_coulmn.get(x);
                            setY(modify, null, modify.getY() + (totalToAdd));
                        }
                    }
                }
                 if(isSlider ){
                    if(height_used < heigth){
                        int totalToAdd = 0;
                        int itemNumber = 0;
                        int amountToAdd = 1;
                        int num_items = m;
                        while(height_used < heigth){
                            VisualItem modify = items_in_coulmn.get(itemNumber);
                            //setY(modify, null, modify.getY() + (amountToAdd*itemNumber));
                            modify.set("add1", amountToAdd);
                            totalToAdd += amountToAdd;
                            itemNumber++;
                            if(isSlider)
                                num_items = (int) modify.get("height") -1;
                            if(itemNumber > num_items){
                                itemNumber = 0;
                                amountToAdd += 1;
                            }
                            height_used++;
                        }
                        int totalPix = 0;
                        for(int x = 0; x <= num_items; x++){
                            VisualItem modify = items_in_coulmn.get(x);
                            setY(modify, null, modify.getY() + totalPix);
                            totalPix += (int) modify.get("add1");
                        }
                    }
                }
                height_used = 0;
                items_in_coulmn = new ArrayList<VisualItem>();
            }
            
            
            if( (boolean) item.get("makeInvis") == true){
                item.setVisible(false);
            }
            else{
                item.setVisible(true);
                int xoffset = width*((Integer)item.get("column"));
                double x = bx + (double)xoffset;


                double y = by + height_used;

                setX(item,null,x);
                setY(item,null,y);
                height_used +=  Math.floor((heigth )* ((Double) item.get("frequency")));
                previous_column=(Integer)item.get("column");
                items_in_coulmn.add(item); 
            }
            
        }
        // for final column should change as we are repeating code
        if(height_used < heigth){
            if(!isRelFreq){
                    int totalToAdd=0;
                    int itemNumber=0;
                    int amountToAdd =1;
                    while(height_used < heigth){
                        VisualItem modify = items_in_coulmn.get(itemNumber);
                        setY(modify,null,modify.getY()+(amountToAdd*itemNumber));
                        modify.set("add1",amountToAdd);
                        totalToAdd+=amountToAdd;
                        itemNumber++;
                        if(itemNumber>m){
                            itemNumber=0;
                            amountToAdd+=1;
                        }
                        height_used++;
                    }
                    for(int x=itemNumber;x<items_in_coulmn.size();x++){
                        VisualItem modify = items_in_coulmn.get(x);
                        setY(modify,null,modify.getY()+(totalToAdd));
                    }
                }
                             if(isSlider ){
                    if(height_used < heigth){
                        int totalToAdd = 0;
                        int itemNumber = 0;
                        int amountToAdd = 1;
                        int num_items = m;
                        while(height_used < heigth){
                            VisualItem modify = items_in_coulmn.get(itemNumber);
                            //setY(modify, null, modify.getY() + (amountToAdd*itemNumber));
                            modify.set("add1", amountToAdd);
                            totalToAdd += amountToAdd;
                            itemNumber++;
                            if(isSlider)
                                num_items = (int) modify.get("height") -1;
                            if(itemNumber > num_items){
                                itemNumber = 0;
                                amountToAdd += 1;
                            }
                            height_used++;
                        }
                        int totalPix = 0;
                        for(int x = 0; x <= num_items; x++){
                            VisualItem modify = items_in_coulmn.get(x);
                            setY(modify, null, modify.getY() + totalPix);
                            totalPix += (int) modify.get("add1");
                        }
                    }
                }
        }
        // set left-overs invisible
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            item.setVisible(false);
        }
        
        //again to add back extra space
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




