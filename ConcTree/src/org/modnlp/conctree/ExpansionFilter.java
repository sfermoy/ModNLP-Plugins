package org.modnlp.conctree;

import java.util.Iterator;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.GroupAction;
import prefuse.data.expression.OrPredicate;
import prefuse.data.expression.Predicate;
import prefuse.util.PrefuseLib;
import prefuse.visual.NodeItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;

/**
 * Filter Action that sets visible all items that meet a given Predicate
 * condition and sets all other items invisible.
 * 
 * @author luzs@acm.org
 */
public class ExpansionFilter extends GroupAction {
    
    private Predicate m_filter;
    private Predicate m_predicate;
    
    /**
     * Create a new ExpansionFilter.
     * @param p the test predicate used to determine visibility
     */
    public ExpansionFilter(Predicate p) {
        setPredicate(p);
    }

    /**
     * Create a new ExpansionFilter.
     * @param group the data group to process
     * @param p the test predicate used to determine visibility
     */
    public ExpansionFilter(String group, Predicate p) {
        super(group);
        setPredicate(p);
    }

    /**
     * Create a new ExpansionFilter.
     * @param vis the Visualization to process
     * @param group the data group to process
     * @param p the test predicate used to determine visibility
     */
    public ExpansionFilter(Visualization vis, String group, Predicate p) {
        super(vis, group);
        setPredicate(p);
    }

    /**
     * Set the test predicate used to determine visibility.
     * @param p the test predicate to set
     */
    protected void setPredicate(Predicate p) {
        m_predicate = p;
        m_filter = new OrPredicate(p, VisiblePredicate.TRUE);
    }
    
    /**
     * @see prefuse.action.Action#run(double)
     */
    public void run(double frac) {
        Iterator items = m_vis.items(m_group, m_filter);
        while ( items.hasNext() ) {
            NodeItem item = (NodeItem)items.next();
            if (!m_predicate.getBoolean(item)){
              PrefuseLib.updateVisible(item,false);
              item.setExpanded(false);
              setDescendants(item,false);
              //System.err.println(item+" collapsed? "+m_predicate.getBoolean(item));
            }
        }
        //m_vis.invalidateAll();
    }
  /**
   * Traverse tree descendents, setting their visibility to .
   */
  private void setDescendants(NodeItem p, boolean v) {
    
    Iterator children = p.children();
    
    p.setExpanded(children.hasNext());

    for ( int i=0; children.hasNext(); ++i ) {
      NodeItem c = (NodeItem)children.next();
      PrefuseLib.updateVisible(c,v);
      c.setExpanded(v);
      EdgeItem e = (EdgeItem)c.getParentEdge();
      PrefuseLib.updateVisible(e, v);
      setDescendants(c,v);
    }
  }
  
}
