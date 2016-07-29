package org.modnlp.conctree;

import java.awt.event.MouseEvent;

import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import prefuse.controls.FocusControl;


/**
 * <p> Slight modification of FocusControl to allow refocusing
 */
public class CollapsingFocusControl extends FocusControl {

    private String group = Visualization.FOCUS_ITEMS;
    
    /**
     * Create a new FocusControl that changes the focus when an item is 
     * clicked the specified number of times. A click value of zero indicates
     * that the focus should be changed in response to mouse-over events.
     * @param clicks the number of clicks needed to switch the focus.
     * @param act an action run to upon focus change 
     */
    public CollapsingFocusControl(int clicks, String act) {
        ccount = clicks;
        activity = act;
    }
    
    /**
     * @see prefuse.controls.Control#itemClicked(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemClicked(VisualItem item, MouseEvent e) {
        if ( !filterCheck(item) ) return;
        if ( UILib.isButtonPressed(e, button) &&
             e.getClickCount() == ccount )
        {
          Visualization vis = item.getVisualization();
          TupleSet ts = vis.getFocusGroup(group);
          
          boolean ctrl = e.isControlDown();
          if ( !ctrl ) {
            curFocus = item;
            ts.setTuple(item);
          } else if ( ts.containsTuple(item) ) {
            ts.removeTuple(item);
          } else {
            ts.addTuple(item);
          }
          runActivity(vis);
          
        }
    }
    
    private void runActivity(Visualization vis) {
        if ( activity != null ) {
            vis.run(activity);
        }
    }
    
} // end of class FocusControl
