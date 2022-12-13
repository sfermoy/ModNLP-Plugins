/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modnlp.mosaic;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import prefuse.action.layout.Layout;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author shane
 */
public class MosaicDecoratorLayout extends Layout
{
    public MosaicDecoratorLayout(String group) {
        super(group);
    }

    public void run(double frac) {
        Iterator iter = m_vis.items(m_group);
        while ( iter.hasNext() ) {
            DecoratorItem decorator = (DecoratorItem)iter.next();
            VisualItem decoratedItem = decorator.getDecoratedItem();
            Rectangle2D bounds = decoratedItem.getBounds();
            int frq = (int) decoratedItem.get("height");
           
            decorator.setFont(FontLib.getFont("Tahoma", Math.min(22,frq/2)));
  
//            if((int)decoratedItem.get("column")==4)
//                decorator.setFont(FontLib.getFont("Tahoma", 16));
              //  decorator.setTextColor(ColorLib.color(java.awt.Color.WHITE));
            
            
            double x = bounds.getCenterX();
            double y = bounds.getCenterY();
            
            setX(decorator, null, x);
            setY(decorator, null, y);
             if( (boolean) decoratedItem.get("makeInvis") == true){
                decorator.setVisible(false);
            }
            else{
                decorator.setVisible(true);
            }
        }
    }
}

