/**
 *  (c) 2008-2016 S Luz <luzs@acm.org>
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
package org.modnlp.conctree;

import java.util.Iterator;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.GroupAction;
import prefuse.data.Graph;
import prefuse.data.Tree;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.util.PrefuseLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

/**
 * A simple collapsing/expansion of subtree loosely based on FisheyeTreeFilter
 */
public class CollapsingBranchFilter extends GroupAction {

    private String m_sources;
    private Predicate m_groupP;
    
    private NodeItem m_root;
    
    
    /**
     * Create a new CollapsingBranchFilter that processes the given group.
     * @param group the data group to process. This should resolve to
     * a Graph instance, otherwise exceptions will result when this
     * Action is run.
     * @param distance the graph distance threshold from high-interest
     * nodes past which nodes will not be visible nor expanded.
     */
    public CollapsingBranchFilter(String group) {
        this(group, Visualization.FOCUS_ITEMS);
    }
    
    /**
     * Create a new CollapsingBranchFilter that processes the given group.
     * @param group the data group to process. This should resolve to
     * a Graph instance, otherwise exceptions will result when this
     * Action is run.
     * @param sources the group to use as source nodes, representing the
     * nodes of highest degree-of-interest.
     * @param distance the graph distance threshold from high-interest
     * nodes past which nodes will not be visible nor expanded.
     */
    public CollapsingBranchFilter(String group, String sources)
    {
        super(group);
        m_sources = sources;
        m_groupP = new InGroupPredicate(
                PrefuseLib.getGroupName(group, Graph.NODES));
    }
    
    /**
     * Get the name of the group to use as source nodes.
     * @return the source data group
     */
    public String getSources() {
        return m_sources;
    }
    
    /**
     * Set the name of the group to use as source nodes.
     * @param sources the source data group
     */
    public void setSources(String sources) {
        m_sources = sources;
    }
    
    /**
     * @see prefuse.action.GroupAction#run(double)
     */
    public void run(double frac) {
        Tree tree = ((Graph)m_vis.getGroup(m_group)).getSpanningTree();
        m_root = (NodeItem)tree.getRoot();
        
        // toggle (collapse/expand) focus nodes
        Iterator iter = m_vis.items(m_sources, m_groupP);
          
        while ( iter.hasNext() ){
          NodeItem i = (NodeItem)iter.next();
          boolean v = !i.isExpanded();
          i.setExpanded(v);
          setDescendants(i, v);
        }

        //m_vis.invalidateAll();
    }

  /**
   * Traverse tree descendents, setting their visibility to v.
   */
  private void setDescendants(NodeItem p, boolean v) {
    
    Iterator children = p.children();
    
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
