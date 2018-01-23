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

import modnlp.tec.client.ConcordanceBrowser;
import prefuse.Visualization;

import prefuse.controls.ControlAdapter;
import prefuse.controls.Control;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.util.ColorLib;
import prefuse.util.StrokeLib;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.util.ArrayList;


/**
 *
 * @author shane
 */


public class HoverTooltip extends ControlAdapter implements Control {
    private ConcordanceBrowser p=null;
    private Visualization v=null;
    private ConcordanceMosaic themosaic =null;
    private BasicStroke s = new BasicStroke();
    private ArrayList colors = null;
  
    
    public HoverTooltip(ConcordanceBrowser parent, Visualization vis, ConcordanceMosaic m){
        v=vis;
        p=parent;
        themosaic=m;
        
    }
    
    public void itemClicked(VisualItem item, MouseEvent e){    
       if(e.getModifiers()==InputEvent.BUTTON1_MASK) {
        if(item instanceof NodeItem)
		{
                    VisualItem old = themosaic.getSelected();
                    
                    if(old!=null){
                    old.setStrokeColor(ColorLib.color(new Color(0.0f,0.0f,0.0f,0.0f)));
                    old.setStroke(StrokeLib.getStroke(1));
                    old.setFillColor(ColorLib.color(themosaic.getSelectedColor()));
                    old.setHighlighted(false);
                    ArrayList<Integer> oldsentenceNos = (ArrayList<Integer>)old.get("sentences");
                    for (Integer sentenceNo : oldsentenceNos ) {
                        for (VisualItem node : themosaic.getVisualItemsInSentence(sentenceNo) ) {
                            node.setStrokeColor(ColorLib.color(new Color(0.0f,0.0f,0.0f,0.0f)));
                            node.setStroke(StrokeLib.getStroke(1));
                            node.setFillColor(node.getEndFillColor());
                            node.setHighlighted(false);  
                            
                        }
                        
                        
                    }
                     v.repaint();
                    }
                    if(old!=item){
                        themosaic.setSelectedColor(ColorLib.getColor(item.getFillColor()));

                        ArrayList<Integer> sentenceNos = (ArrayList<Integer>)item.get("sentences");
                        for (Integer sentenceNo : sentenceNos ) {
                            for (VisualItem node : themosaic.getVisualItemsInSentence(sentenceNo) ) {
                                //node.setStroke(StrokeLib.getStroke(1));                
                                node.setStrokeColor(ColorLib.color(Color.black));
                                node.setFillColor(ColorLib.color(Color.WHITE.brighter()));  
                                node.setHighlighted(true);
                                if(!node.isInteractive())
                                    node.setFillColor(node.getEndFillColor());

                            }


                        }

                        item.setStroke(StrokeLib.getStroke(1));                
                        item.setStrokeColor(ColorLib.color(Color.BLACK));
                        item.setFillColor(ColorLib.color(Color.WHITE));     
                        item.setHighlighted(true);

                        themosaic.setSelected(item);
                        String text = ((String) item.get("word"));
                        p.showContext((Integer)item.get("column"), text);
                        v.repaint();
                    }else{
                        themosaic.setSelected(null);
                        String text = ((String) item.get("word"));
                        p.showContext((Integer)item.get("column"), text);
                    }
                }
       }
        
        //???*** right click to search word+[wildcards before]keyword or reverse for after
       
        if(e.getModifiers()==InputEvent.BUTTON3_MASK) {
            int column = (Integer) item.get("column");
            //if(column== 3 || column ==5){
            if(column>0){
                
                String text = ((String) item.get("word"));
                //middle column search
                if (column == 4)
                {
                    System.out.println(text);
                    p.requestConcordance(text);
                }
                
                int position = 4- column;
                if (position > 0){
                    int rel_value = position - 1;
                    String spaces = "+";
                    for (int i = 0; i <rel_value; i++) {
                        spaces+="*+";
                    }
                    
                   // p.requestConcordance(text+"+"+"["+rel_value+"]"+ themosaic.getKeyword());
                    p.requestConcordance(text+spaces+themosaic.getKeyword());
                }else{
                    int rel_value = Math.abs(position + 1);
                    String spaces = "+";
                    for (int i = 0; i <rel_value; i++) {
                        spaces+="*+";
                    }
                    p.requestConcordance(themosaic.getKeyword()+spaces+text);
                }
            }

          }
        
    }
	public void itemEntered(VisualItem item, MouseEvent e) 
	{
             if(item!=themosaic.getSelected())
		if(item instanceof NodeItem)
		{
		
                                   
                    item.setStrokeColor(ColorLib.color(Color.BLACK));
                    item.setStroke(StrokeLib.getStroke(1));
                    v.repaint();
                    
                    
		}
	}
        
        public void itemExited(VisualItem item, MouseEvent e) 
	{
            if(item!=themosaic.getSelected()){
		if(item instanceof NodeItem)
		{
                    if(!item.isHighlighted())
                        item.setStrokeColor(ColorLib.color(new Color(0.0f,0.0f,0.0f,0.0f)));
                    
              
                  
                    
		}
            }
        }
}

