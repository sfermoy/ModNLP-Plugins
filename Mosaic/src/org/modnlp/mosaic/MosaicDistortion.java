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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import prefuse.action.distortion.Distortion;
import prefuse.util.FontLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;

public class MosaicDistortion extends Distortion {
    private ConcordanceMosaic themosaic = null;
    int height,width;
    
    public MosaicDistortion(ConcordanceMosaic m,int twidth,int theight) {
        themosaic = m;
        this.m_distortX = false;
        this.m_distortY = false;
        height = theight;
        width = twidth;
    }
    
    public void run(double frac) {
        Rectangle2D bounds = getLayoutBounds();
        Point2D anchor = correct(m_anchor, bounds);
           
        final Iterator iter = getVisualization().visibleItems(m_group);
        double overlap = 0;
        double overlapAdditional = 0;
        //double overlapPrev = 0;
        int numExpand = 2;
        int expandSize = 42;
        VisualItem sel = themosaic.getSelected();
        double yprevious = 0;
        double hprevious = 42;
        boolean expandNext = false;
        if ( sel != null ) {
         yprevious= sel.getY();
        }
        VisualItem prev = null;
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            
            if ( item.isFixed() ) continue;
            double overlap2 = 0;
             double seloverlap = 0;
            // reset distorted values
            // TODO - make this play nice with animation?
            
            item.setX(item.getEndX());
            item.setY(item.getEndY());
            item.setSize(item.getEndSize());
            
            
            if( (boolean) item.get("makeInvis") == true){
                item.setVisible(false);
            }
            
            //VisualItem sel = themosaic.getSelected();
            //find how much the selected item wae expaned by
            if ( sel != null ) 
                if(((Double) sel.get("frequency")) * 100 < 5){
                    sel.setSize(42);
                    seloverlap = 42 - (sel.getBounds().getHeight());
            }
            
            // compute distortion if we have a distortion focus
                        
            Rectangle2D bbox = item.getBounds();
            double x = item.getX();
            double y = item.getY();
            double ay = 0, ax = 400;
            //bbox.getHeight();
            //if visual item is rectangle not text.
            if(x % width == 0){
                //if mouse in window get the position
                  if ( anchor != null ) {
                     ay = anchor.getY();
                     ax = anchor.getX();
                  }
                      
                //double sy = sel.getY(), sx = sel.getX();
                  
                //if there is a selected box and that box had to be expanded
                if ( sel != null )
                    if(((Double) sel.get("frequency")) * 100 < 5)
                        
                        //if this item is in same column as the selected one
                        if( sel.getX() == x){
                            //and is after the selected one
                                        if(y > sel.getEndY()){                       
                                            //item.setY(yprevious + 48 -1);
                                            overlap2 = yprevious +  hprevious ;
                                            hprevious = bbox.getHeight();
                                            yprevious = overlap2;
                                        }
                        }
                // if anchor in column
                if( ax > x){
                        if(ax < (x + width)){
                            double totalOverlap = y;
                            //if a rectangle is selected and is small enough to need expansion
                            if ( sel != null )
                                //if selected is in column
                                if( sel.getX()== x)
                                    if(((Double) sel.get("frequency")) * 100 < 5)
                                        //if rectangle is after selected
                                        if(y > sel.getY()){
                                            //set y to be pushed forward 
                                             y = overlap2;
                                             totalOverlap = overlap2 ;
                                        }
                            //if we d
                            if(y > 0)          
                                if (totalOverlap == 0)
                                    continue;
                            
                             //expanding the next few boxes after hover
                            if (expandNext){
                                    item.setSize(expandSize);
                                    overlapAdditional = expandSize - bbox.getHeight();
                                    numExpand--;
                                    expandSize += -15 ;
                                    if(numExpand==0){
                                    expandNext = false;
                                    expandSize = 42;
                                    }
                                   }

                            Rectangle2D bboxprev;
                            //if anchor after start of box
                            if (ay >= y)
                                //if ancor before end of box
                                if (ay < (y + bbox.getHeight()))
                                     if(bbox.getHeight() < 23){
////                                      bboxprev = prev.getBounds();
////                                    if(bboxprev.getHeight() < 23){
////                                        prev.setSize(42);
////                                         overlapPrev = 42 - bbox.getHeight();
////                                        totalOverlap+= overlapPrev;
//                                        //System.out.println(prev.get("word"));
//                                    } 
                                    item.setSize(42);
                                    overlap = 42 - bbox.getHeight();
                                    expandNext = true;
                                   }
                            
                            
                            
                            //since selected is moved down account for items which have been overlaped
                            if ( sel != null )
                                //if selected is in column
                                if( sel.getX() == x)
                                    if(((Double) sel.get("frequency")) * 100 < 5)
                                        if(ay < sel.getY())
                                            if(y > (sel.getEndY())){
                                                double tempy = y;
                                                if(y < sel.getY()){
                                                    y = overlap2-seloverlap;
                                                    totalOverlap = overlap2 - seloverlap ;
                                                }
                                                if(y > sel.getY()){
                                                    y = overlap2-overlap;
                                                    totalOverlap = overlap2 - overlap ;
                                                }
                                                if(y == tempy){
                                                    y = overlap2 - seloverlap;
                                                    totalOverlap = overlap2 - seloverlap ;

                                                }
                                            }
                             if(y > 0)
                                if (totalOverlap == 0)
                                   continue;

                            //if rectangle after expanded hovered box
                            if(y > ay)
                                totalOverlap += overlap;


                            item.setY(totalOverlap);
                            overlap += overlapAdditional;//+overlapPrev;
                            overlapAdditional = 0;
                            //overlapPrev = 0;
                    }else{
                            if ( sel != null )
                                if(((Double) sel.get("frequency")) * 100 < 5)
                                if( sel.getX() == x){
                                    if(y > sel.getY()){                                            
                                        item.setY(overlap2);                               
                                }
                 }
                }
            }else{
                    if ( sel != null )
                        if(((Double) sel.get("frequency")) * 100 < 5)
                if( sel.getX() == x){
                                if(y > sel.getY()){                       
                                    item.setY(overlap2);                                 
                                }
                 }
                }

                // remember last item
                prev=item;
            }else{
                    DecoratorItem decorator = (DecoratorItem)item;
                    VisualItem decoratedItem = decorator.getDecoratedItem();
                    Rectangle2D bounds2 = decoratedItem.getBounds();
                    int frq = (int) decoratedItem.get("height");
           
                    decorator.setFont(FontLib.getFont("Tahoma", Math.min(22,frq/2)));

                    if((int)decoratedItem.get("column")==4)
                        decorator.setFont(FontLib.getFont("Tahoma", 16));

                    double x2 = bounds2.getCenterX();
                    double y2 = bounds2.getCenterY();

                    setX(decorator, null, x2);
                    setY(decorator, null, y2);
                 }
            
        }
    }
    
    /**
     * @see prefuse.action.distortion.Distortion#distortX(double, java.awt.geom.Point2D, java.awt.geom.Rectangle2D)
     */
    protected double distortX(double x, Point2D a, Rectangle2D b) {
        return 1;
    }
    
    /**
     * @see prefuse.action.distortion.Distortion#distortY(double, java.awt.geom.Point2D, java.awt.geom.Rectangle2D)
     */
    protected double distortY(double y, Point2D a, Rectangle2D b) {
       return 1;
    }
    
    /**
     * @see prefuse.action.distortion.Distortion#distortSize(java.awt.geom.Rectangle2D, double, double, java.awt.geom.Point2D, java.awt.geom.Rectangle2D)
     */
    protected double distortSize(Rectangle2D bbox, double x, double y, 
            Point2D anchor, Rectangle2D bounds)
    {
             
         return 1;
    }
    
   
} 


