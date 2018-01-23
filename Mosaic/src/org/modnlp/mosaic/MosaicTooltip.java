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

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import modnlp.tec.client.ConcordanceBrowser;
import prefuse.Display;
import prefuse.Visualization;

import prefuse.controls.ControlAdapter;
import prefuse.controls.Control;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.util.ColorLib;
import prefuse.util.StrokeLib;



/**
 *
 * @author shane
 */


public class MosaicTooltip extends ControlAdapter implements Control {
    private ConcordanceBrowser p=null;
    
    public MosaicTooltip(ConcordanceBrowser parent){
        p=parent;
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
       
    }
   
    
	public void itemEntered(VisualItem item, MouseEvent e) 
	{
		if(item instanceof NodeItem)
		{
                        //ToolTipManager.sharedInstance().setEnabled(true);
			String text = ((String) item.get("word"));
                        
			double fq = (Double) item.get("tooltip");
                        boolean layout = (boolean) item.get("tooltipLayoutSwitch");
			fq = Math.round(fq*10000);
                        fq = fq/100.0;
                        Display d = (Display)e.getSource();
                        d.setToolTipText(null); 
                        
                        //if collocation strength view and not middle col
                        if(layout || (Integer)item.get("column") !=4 ){
                            double fq1 = (Double) item.get("tooltipFreq");
                            fq1 = Math.round(fq1*10000);
                            fq1 = fq1/100.0;
                            d.setToolTipText("Text: \"" + text +"\" \n " +" Collocation Strength: " + fq +" Frequency: " + fq1);
                        }
                        else
                             d.setToolTipText("Text: \"" + text +"\" \n " +" Frequency: " + fq);
                       
		}
	}
        
        public void itemExited(VisualItem item, MouseEvent e) {
        Display d = (Display)e.getSource();
        d.setToolTipText(null);          
        
    }
}

