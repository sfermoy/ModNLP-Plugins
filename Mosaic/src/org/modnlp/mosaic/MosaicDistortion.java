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
    private ConcordanceMosaic themosaic =null;
  
    
    public MosaicDistortion(ConcordanceMosaic m) {
        themosaic=m;
        this.m_distortX =false;
        this.m_distortY =false;
    }
    
    public void run(double frac) {
        Rectangle2D bounds = getLayoutBounds();
        Point2D anchor = correct(m_anchor, bounds);
        
       
        final Iterator iter = getVisualization().visibleItems(m_group);
        double overlap=0;
        VisualItem sel = themosaic.getSelected();
        double yprevious=0;
        double hprevious=48;
        if ( sel != null ) {
         yprevious= sel.getY();
        }
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            if ( item.isFixed() ) continue;
            double overlap2=0;
             double seloverlap =0;
            // reset distorted values
            // TODO - make this play nice with animation?
            
            item.setX(item.getEndX());
            item.setY(item.getEndY());
            item.setSize(item.getEndSize());
            
            //VisualItem sel = themosaic.getSelected();
            if ( sel != null ) 
                if(((Double) sel.get("frequency"))*100 < 5){
                    sel.setSize(48);
                    seloverlap = 48 - (sel.getBounds().getHeight());
            }
            
            // compute distortion if we have a distortion focus
          
                
            Rectangle2D bbox = item.getBounds();
            double x = item.getX();
            double y = item.getY();
            double ay = 0, ax = 400;
            //bbox.getHeight();
            //if a rectangle else a word
            if(x%98==0){
                  if ( anchor != null ) {
                     ay = anchor.getY();
                     ax = anchor.getX();
                  }
                      
                //double sy = sel.getY(), sx = sel.getX();
                if ( sel != null )
                    if(((Double) sel.get("frequency"))*100<5)
                        if( sel.getX()==x){
                                        if(y>sel.getEndY()){                       
                                            //item.setY(yprevious + 48 -1);
                                            overlap2=yprevious +  hprevious ;
                                            hprevious=bbox.getHeight();
                                            yprevious=overlap2;
                                        }
                        }
                // if ancor in column
                if( ax>x){
                        if(ax<(x + 98)){
                            double totalOverlap = y;
                            //if a rectangle is selected and is small enough to need expansion
                            if ( sel != null )
                                //if selected is in column
                                if( sel.getX()==x)
                                    if(((Double) sel.get("frequency"))*100<5)
                                        //if rectangle is after selected
                                        if(y>sel.getY()){
                                            //set y to be pushed forward by selected
                                             y= overlap2;
                                             totalOverlap=overlap2 ;
                                        }
                            if(y>0)          
                                if (totalOverlap==0)
                                    continue;
                            //if ancor after start of box
                            if (ay>=y)
                                //if ancor before end of box
                                if (ay<(y+bbox.getHeight()))
                                   if(bbox.getHeight()<23){
                                    item.setSize(48);
                                    overlap=48 - bbox.getHeight();
                                   }

                            //since selected is moved down account for items which have been overlaped
                            if ( sel != null )
                                //if selected is in column
                                if( sel.getX()==x)
                                    if(((Double) sel.get("frequency"))*100<5)
                                        if(ay<sel.getY())
                                            if(y>(sel.getEndY())){
                                                double tempy=y;
                                                if(y<sel.getY()){
                                                    y= overlap2-seloverlap;
                                                    totalOverlap=overlap2 - seloverlap ;
                                                }
                                                if(y>sel.getY()){
                                                    y= overlap2-overlap;
                                                    totalOverlap=overlap2 - overlap ;
                                                }
                                                if(y==tempy){
                                                    y= overlap2-seloverlap;
                                                    totalOverlap=overlap2 - seloverlap ;

                                                }
                                            }
                             if(y>0)
                                if (totalOverlap==0)
                                   continue;

                            //if rectangle after expanded hovered box
                            if(y>ay)
                                totalOverlap+=overlap;


                            item.setY(totalOverlap);





                    }else{
                            if ( sel != null )
                                if(((Double) sel.get("frequency"))*100<5)
                                if( sel.getX()==x){
                                    if(y>sel.getY()){                                            
                                        item.setY(overlap2);                               
                                }
                 }
                }
            }else{
                    if ( sel != null )
                        if(((Double) sel.get("frequency"))*100<5)
                if( sel.getX()==x){
                                if(y>sel.getY()){                       
                                    item.setY(overlap2);                                 
                                }
                 }
                }

            }else{
                    DecoratorItem decorator = (DecoratorItem)item;
                    VisualItem decoratedItem = decorator.getDecoratedItem();
                    Rectangle2D bounds2 = decoratedItem.getBounds();
                    Double frq = (Double) decoratedItem.get("frequency");
                    if (frq > 0.05) {
                    decorator.setFont(FontLib.getFont("Tahoma", 16));
                    } else if (frq > 0.02) {
                        decorator.setFont(FontLib.getFont("Tahoma", 6));
                    }
                    else if (frq > 0.0045) {
                        decorator.setFont(FontLib.getFont("Tahoma", 2));
                    }
                    else{
                        decorator.setFont(FontLib.getFont("Tahoma", 0));
                    }
                    if(decoratedItem.getSize()==48)
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