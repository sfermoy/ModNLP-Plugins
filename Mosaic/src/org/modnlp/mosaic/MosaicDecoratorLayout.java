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
            //if(decoratedItem.get("column")==4)
              //  decorator.setTextColor(ColorLib.color(java.awt.Color.WHITE));
            
            
            double x = bounds.getCenterX();
            double y = bounds.getCenterY();
            
            setX(decorator, null, x);
            setY(decorator, null, y);
        }
    }
}
