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

import modnlp.idx.headers.HeaderDBManager;
import modnlp.tec.client.Plugin;
import modnlp.tec.client.ConcordanceBrowser;
import modnlp.tec.client.ConcordanceObject;
import modnlp.idx.database.Dictionary;
import modnlp.idx.inverted.StringSplitter;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.BasicStroke;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.net.URL;
import java.net.HttpURLConnection;
import javax.swing.JFrame;
import java.io.BufferedReader;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.Timer;
import java.io.PrintWriter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import java.util.StringTokenizer;
import java.io.PipedWriter;
import java.io.PipedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import javax.swing.JOptionPane;
import java.util.Vector;
import java.util.Collections;
import java.awt.event.MouseAdapter;
import javax.swing.table.TableColumnModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;


import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.LocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.CollapsedSubtreeLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.BalloonTreeLayout;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.ControlAdapter;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.controls.ToolTipControl;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tree;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.io.TreeMLReader;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.GraphicsLib;
import prefuse.util.PrefuseLib;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.visual.VisualItem;
import prefuse.visual.NodeItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;


/**
 *  Concordance tree display. Display a prefix tree (trie) encoding
 *  the context to the right of a concordance; or a (i.e. a
 *  right-to-left prefix tree) encoding the left context of a
 *  concordance
 *
 * @author  S Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
 */

public class ConcordanceTree extends Display
{
  private static final String TREE = "tree";
  private static final String TREENODES = "tree.nodes";
  private static final String TREEEDGES = "tree.edges";
  public static final int WIDTH = 600;
  public static final int HEIGHT = 400;    
  public static final String NAME = "name";
  public static final String NAMEEMPTY = "_Empty_Concordance_Tree_";
  public static final String NODECOUNT = "nodecount";
  public static final String ROWCOUNT = "colcount";
  public static final int MAXCOLS = 200;

  protected Tree tree;
  private LabelRenderer m_nodeRenderer;
  private EdgeRenderer m_edgeRenderer;
  FisheyeTreeFilter fisheyetreefilter = new FisheyeTreeFilter(TREE, 6);
  private static Font defaultTreeFont = FontLib.getFont("Tahoma", 16);
  private Display m_display_self;  // myself
  
  private String m_label = "label";
  private int m_orientation = Constants.ORIENT_LEFT_RIGHT;
  private static int cutoff_frequency = 1;
  /**
   * Describe rowCount here.
   */
  private static int rowCount = 1;

  /**
   * Describe minFreqRatio here.
   */
  private static float minFreqRatio = 1;
    
  public ConcordanceTree() {
    super(new Visualization());

    m_display_self = this;
    m_label = NAME;

    tree = new Tree();
    Table ntable = tree.getNodeTable();
    ntable.addColumn(NAME,String.class);
    ntable.addColumn(NODECOUNT, int.class);
    ntable.addColumn(ROWCOUNT,int.class);
    resetTree();

    m_vis.add(TREE, tree);
    //m_vis.add(TREE, t, new WordCountPredicate());

   m_nodeRenderer = new LabelRenderer(m_label);
   m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
   m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
   m_nodeRenderer.setRoundedCorner(8,8);
   m_edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_CURVE);
   
   DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer);
   rf.add(new InGroupPredicate(TREEEDGES), m_edgeRenderer);
   m_vis.setRendererFactory(rf);
   
   // colors
   ItemAction nodeColor = new NodeColorAction(TREENODES);
   ItemAction textColor = new ColorAction(TREENODES,
                                          VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0));
   m_vis.putAction("textColor", textColor);
   
   ItemAction edgeColor = new ColorAction(TREEEDGES,
                                          VisualItem.STROKECOLOR, ColorLib.rgb(255,155,155));
   

   // quick repaint
   ActionList repaint = new ActionList();
   repaint.add(nodeColor);
   //WordSizeAction wsaction = new WordSizeAction(TREEEDGES);
   repaint.add(new RepaintAction());
   m_vis.putAction("repaint", repaint);

   // full paint
   ActionList fullPaint = new ActionList();
   fullPaint.add(nodeColor);
   m_vis.putAction("fullPaint", fullPaint);
   
   // animate paint change
   ActionList animatePaint = new ActionList(400);
   animatePaint.add(new ColorAnimator(TREENODES));
   animatePaint.add(new RepaintAction());
   m_vis.putAction("animatePaint", animatePaint);
   
   // create the tree layout action
   NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout(TREE,m_orientation, 2, 0, 0);

   //Point2D anchor = new Point2D.Double(25,HEIGHT/2);
   //treeLayout.setLayoutAnchor(anchor);
   m_vis.putAction("treeLayout", treeLayout);
   
   CollapsedSubtreeLayout subLayout = 
     new CollapsedSubtreeLayout(TREE, m_orientation);
   m_vis.putAction("subLayout", subLayout);
   
   AutoPanAction autoPan = new AutoPanAction();
   AutoCenterAction autocenter = new AutoCenterAction();
   AutoFitAction autofit = new AutoFitAction();

   ActionList positioning = new ActionList();
   positioning.add(autofit);
   m_vis.putAction("positioning", positioning);

   ActionList fna = new ActionList();
   fna.add(new WordFontAction(TREENODES, defaultTreeFont));
   //   fna.add(new EdgeWidthAction(TREEEDGES));
   //fna.add(new WordSizeAction(TREEEDGES));
   m_vis.putAction("fontnodeaction", fna);

   // create the filtering and layout
   ActionList filter = new ActionList();
   filter.add(fna);
   //VisibilityFilter visibfilter = new VisibilityFilter(new WordCountPredicate());
   //filter.add(visibfilter);
   filter.add(treeLayout);
   filter.add(subLayout);
   filter.add(textColor);
   //filter.add(nodeColor);
   filter.add(edgeColor);
   m_vis.putAction("filter", filter);

   ActionList fishactlist = new ActionList(1000);
   fishactlist.add(fisheyetreefilter);
   fishactlist.setPacingFunction(new SlowInSlowOutPacer());
   //fishactlist.add(new QualityControlAnimator());
   //   fishactlist.add(new LocationAnimator(TREENODES));
   // fishactlist.add(new RepaintAction());
   //m_vis.putAction("fishactlist", fishactlist);

   // This doesn't quite work as expected; the layout is calculated
   //for the entire tree and low freq nodes are simply not shown,
   //leaving gaps in the layout
   //
   //setPredicate(new WordCountPredicate());
   
   // animated transition
   ActionList animate = new ActionList(1000);
   animate.setPacingFunction(new SlowInSlowOutPacer());
   //animate.add(autoPan);
   animate.add(new QualityControlAnimator());
   animate.add(new VisibilityAnimator(TREE));
   animate.add(new LocationAnimator(TREENODES));
   animate.add(new ColorAnimator(TREENODES));
   animate.add(new RepaintAction());
   m_vis.putAction("animate", animate);
   m_vis.alwaysRunAfter("filter", "animate");
   
   // create animator for orientation changes
   ActionList orient = new ActionList(2000);
   orient.setPacingFunction(new SlowInSlowOutPacer());
   orient.add(autoPan);
   orient.add(autocenter);
   orient.add(new QualityControlAnimator());
   orient.add(new LocationAnimator(TREENODES));
   orient.add(new RepaintAction());
   //orient.add(autofit);

   m_vis.putAction("orient", orient);
   
   // ------------------------------------------------
   
   // initialize the display
   setSize(WIDTH,HEIGHT);
   setItemSorter(new TreeDepthItemSorter());
   addControlListener(new ZoomToFitControl());
   addControlListener(new ZoomControl());
   addControlListener(new WheelZoomControl());
   addControlListener(new PanControl());
   addControlListener(new FocusControl(1, "filter"));
   addControlListener(new ToolTipControl(NODECOUNT));

   registerKeyboardAction(
                          new OrientAction(Constants.ORIENT_LEFT_RIGHT),
                          "left-to-right", KeyStroke.getKeyStroke("ctrl 1"), WHEN_IN_FOCUSED_WINDOW);
   registerKeyboardAction(
                          new OrientAction(Constants.ORIENT_TOP_BOTTOM),
                          "top-to-bottom", KeyStroke.getKeyStroke("ctrl 2"), WHEN_IN_FOCUSED_WINDOW);
   registerKeyboardAction(
                          new OrientAction(Constants.ORIENT_RIGHT_LEFT),
                          "right-to-left", KeyStroke.getKeyStroke("ctrl 3"), WHEN_IN_FOCUSED_WINDOW);
   registerKeyboardAction(
                          new OrientAction(Constants.ORIENT_BOTTOM_TOP),
                          "bottom-to-top", KeyStroke.getKeyStroke("ctrl 4"), WHEN_IN_FOCUSED_WINDOW);
   registerKeyboardAction(
                          new FisheyeExpandAction(-1),
                          "collapse-one", KeyStroke.getKeyStroke("ctrl 5"), WHEN_IN_FOCUSED_WINDOW);
   registerKeyboardAction(
                          new FisheyeExpandAction(+1),
                          "expand-one", KeyStroke.getKeyStroke("ctrl 6"), WHEN_IN_FOCUSED_WINDOW);

   
   // ------------------------------------------------
   
   // filter graph and perform layout
   setOrientation(m_orientation);
   m_vis.run("filter");

   /*
 TupleSet search = new PrefixSearchTupleSet(); 
   m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);
   search.addTupleSetListener(new TupleSetListener() {
       public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
         m_vis.cancel("animatePaint");
         m_vis.run("fullPaint");
         m_vis.run("animatePaint");
       }
     });
    */ 
  
  }  

  public Tree getTree(){
    return tree;
  }


  public void resetTree(){
    tree.clear();
    Node cnode = tree.addRoot();
    cnode.setString(NAME,NAMEEMPTY);
    cnode.setInt(NODECOUNT,1);
    cnode.setInt(ROWCOUNT,1);
  }

  public void initialView(){
    reset();
    m_vis.cancel("filter");
    m_vis.run("filter");
    m_vis.run("positioning");
  }
    

  /**
   * Get the <code>DefaultTreeFont</code> value.
   *
   * @return a <code>Font</code> value
   */

  public final Font getDefaultTreeFont() {
    return defaultTreeFont;
  }

  /**
   * Set the <code>DefaultTreeFont</code> value.
   *
   * @param newDefaultTreeFont The new DefaultTreeFont value.
   */
  public final void setDefaultTreeFont(final Font dtf) {
    defaultTreeFont =  dtf;
  }


  // set orientation of layout
  public void setOrientation(int orientation) {
    NodeLinkTreeLayout rtl 
      = (NodeLinkTreeLayout)m_vis.getAction("treeLayout");
    CollapsedSubtreeLayout stl
      = (CollapsedSubtreeLayout)m_vis.getAction("subLayout");
    Point2D anchor;
    switch ( orientation ) {
    case Constants.ORIENT_LEFT_RIGHT:
      m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
      m_edgeRenderer.setHorizontalAlignment1(Constants.RIGHT);
      m_edgeRenderer.setHorizontalAlignment2(Constants.LEFT);
      m_edgeRenderer.setVerticalAlignment1(Constants.CENTER);
      m_edgeRenderer.setVerticalAlignment2(Constants.CENTER);
      anchor = new Point2D.Double(25,getHeight()/2);
      break;
    case Constants.ORIENT_RIGHT_LEFT:
      m_nodeRenderer.setHorizontalAlignment(Constants.RIGHT);
      m_edgeRenderer.setHorizontalAlignment1(Constants.LEFT);
      m_edgeRenderer.setHorizontalAlignment2(Constants.RIGHT);
      m_edgeRenderer.setVerticalAlignment1(Constants.CENTER);
      m_edgeRenderer.setVerticalAlignment2(Constants.CENTER);
      anchor = new Point2D.Double(getWidth()-25,getHeight()/2);
      break;
    case Constants.ORIENT_TOP_BOTTOM:
      m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
      m_edgeRenderer.setHorizontalAlignment1(Constants.CENTER);
      m_edgeRenderer.setHorizontalAlignment2(Constants.CENTER);
      m_edgeRenderer.setVerticalAlignment1(Constants.BOTTOM);
      m_edgeRenderer.setVerticalAlignment2(Constants.TOP);
      anchor = new Point2D.Double(getWidth()/2,35);
      break;
    case Constants.ORIENT_BOTTOM_TOP:
      m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
      m_edgeRenderer.setHorizontalAlignment1(Constants.CENTER);
      m_edgeRenderer.setHorizontalAlignment2(Constants.CENTER);
      m_edgeRenderer.setVerticalAlignment1(Constants.TOP);
      m_edgeRenderer.setVerticalAlignment2(Constants.BOTTOM);
      anchor = new Point2D.Double(getWidth()/2,getHeight()-35);
      break;
    default:
      throw new IllegalArgumentException("Unrecognized orientation value: "+orientation);
    }
    m_orientation = orientation;
    rtl.setOrientation(orientation);
    rtl.setLayoutAnchor(anchor);
    stl.setOrientation(orientation);

  }
    
  public int getOrientation() {
    return m_orientation;
  }

  /**
   * Get the <code>MinFreqRatio</code> value.
   *
   * @return a <code>float</code> value
   */
  public final float getMinFreqRatio() {
    return minFreqRatio;
  }

  /**
   * Set the <code>MinFreqRatio</code> value.
   *
   * @param newMinFreqRatio The new MinFreqRatio value.
   */
  public final void setMinFreqRatio(final float newMinFreqRatio) {
    this.minFreqRatio = newMinFreqRatio;
  }

  /**
   * Get the <code>ColumnCount</code> value.
   *
   * @return an <code>int</code> value
   */
  public final int getRowCount() {
    return rowCount;
  }

  /**
   * Set the <code>ColumnCount</code> value.
   *
   * @param newColumnCount The new ColumnCount value.
   */
  public final void setRowCount(final int rc) {
    this.rowCount = rc;
  }

  // Inner classes (actions etc)
  public class FisheyeExpandAction extends AbstractAction {
    private int expand;
    
    public FisheyeExpandAction(int b) {
      this.expand = b;
    }
    public void actionPerformed(ActionEvent evt) {
      fisheyetreefilter.setDistance(fisheyetreefilter.getDistance()+expand);
      getVisualization().cancel("fishactlist");
      getVisualization().run("fishactlist");
      getVisualization().run("treeLayout");
      System.err.println(fisheyetreefilter.getDistance());
    }
  }

  public class OrientAction extends AbstractAction {
    private int orientation;
    
    public OrientAction(int orientation) {
      this.orientation = orientation;
    }
    public void actionPerformed(ActionEvent evt) {
      setOrientation(orientation);
      m_vis.cancel("orient");
      m_vis.run("treeLayout");
      m_vis.run("orient");    
      // getVisualization().cancel("orient");
      //getVisualization().run("treeLayout");
      //getVisualization().run("orient");
    }
  }
    


  public class AutoPanAction extends Action {
    private Point2D m_start = new Point2D.Double();
    private Point2D m_end   = new Point2D.Double();
    private Point2D m_cur   = new Point2D.Double();
    private int     m_bias  = 150;
    
    public void run(double frac) {
      TupleSet ts = m_vis.getFocusGroup(Visualization.FOCUS_ITEMS);
      //System.err.println("auto-panning"+frac);
      if ( ts.getTupleCount() == 0 )
        return;
      if ( frac == 0.0 ) {
        int xbias=0, ybias=0;
        switch ( m_orientation ) {
        case Constants.ORIENT_LEFT_RIGHT:
          xbias = m_bias;
          break;
        case Constants.ORIENT_RIGHT_LEFT:
          xbias = -m_bias;
          break;
        case Constants.ORIENT_TOP_BOTTOM:
          ybias = m_bias;
          break;
        case Constants.ORIENT_BOTTOM_TOP:
          ybias = -m_bias;
          break;
        }
        VisualItem vi = (VisualItem)ts.tuples().next();
        m_cur.setLocation(getWidth()/2, getHeight()/2);
        getAbsoluteCoordinate(m_cur, m_start);
        m_end.setLocation(vi.getX()+xbias, vi.getY()+ybias);
      } else {
        m_cur.setLocation(m_start.getX() + frac*(m_end.getX()-m_start.getX()),
                          m_start.getY() + frac*(m_end.getY()-m_start.getY()));
        //panToAbs(m_cur);
        panTo(m_cur);
      }
    }
  }

  public class AutoFitAction extends Action {
    private int m_margin = 20;
    private long m_duration = 1000;
    private String m_group = Visualization.ALL_ITEMS;


    public void run(double frac) {
      Rectangle2D bounds = m_vis.getBounds(m_group);
      GraphicsLib.expand(bounds, m_margin + (int)(1/m_display_self.getScale()));
      DisplayLib.fitViewToBounds(m_display_self, bounds, m_duration);
    }
  }

  public class AutoCenterAction extends Action {
    private Point2D m_start = new Point2D.Double();
    private Point2D m_end   = new Point2D.Double();
    private Point2D m_cur   = new Point2D.Double();
    private int     m_bias  = 0;
    
    public void run(double frac) {
      System.err.println("auto-centering"+frac);
      if ( frac == 0.0 ) {
        int xbias=0, ybias=0;
        switch ( m_orientation ) {
        case Constants.ORIENT_LEFT_RIGHT:
          xbias = m_bias;
          break;
        case Constants.ORIENT_RIGHT_LEFT:
          xbias = -m_bias;
          break;
        case Constants.ORIENT_TOP_BOTTOM:
          ybias = m_bias;
          break;
        case Constants.ORIENT_BOTTOM_TOP:
          ybias = -m_bias;
          break;
        }
        Rectangle2D vb = m_vis.getBounds(TREE);
        m_cur.setLocation(getWidth()/2, getHeight()/2);
        getAbsoluteCoordinate(m_cur, m_start);
        m_end.setLocation(vb.getX()+xbias, vb.getY()+ybias);
      } else {
        m_cur.setLocation(m_start.getX() + frac*(m_end.getX()-m_start.getX()),
                          m_start.getY() + frac*(m_end.getY()-m_start.getY()));
        panToAbs(m_cur);
      }
    }
  }

  
  public static class NodeColorAction extends ColorAction {
    
    public NodeColorAction(String group) {
      super(group, VisualItem.FILLCOLOR);
    } 
    
    public int getColor(VisualItem item) {
      if ( m_vis.isInGroup(item, Visualization.SEARCH_ITEMS) )
        return ColorLib.rgb(255,190,190);
      else if ( m_vis.isInGroup(item, Visualization.FOCUS_ITEMS) )
        return ColorLib.rgb(198,229,229);
      else if ( item.getDOI() > -1 )
        return ColorLib.rgb(164,193,193);
      else
        return ColorLib.rgba(255,255,255,0);
    }
    
  }

  public static class WordFontAction extends FontAction {
    
    public WordFontAction(String group, Font df){
      super(group,df);
    }
        
    public Font getFont(VisualItem item) {
      int nc = item.getInt(NODECOUNT);
      //if (nc==1)
      //  return defaultTreeFont;

      //int cc = item.getInt(ROWCOUNT);

      //float smin = 1f/cc;
      
      float s = (float)nc/rowCount;
      float fs = defaultTreeFont.getSize();
      if (s > 1)
        fs = fs*2f;
      if (s > minFreqRatio*60)
        fs = fs*3f;
      else
      if (s > minFreqRatio*40)
        fs = fs*3f;
      else
        if (s > minFreqRatio*20)
         fs = fs*2.8f;
      else
        if (s > minFreqRatio*15)
         fs = fs*2.5f;
      else
        if (s > minFreqRatio*10)
         fs = fs*2f;
      else
        if (s > minFreqRatio*6)
         fs = fs*1.8f;
      else
        if (s > minFreqRatio*3)
          fs = fs*1.6f;
      else
        if (s > minFreqRatio*2)
          fs = fs*1.4f;
      else
        if (s > minFreqRatio*1.4)
          fs = fs*1.2f;
      else
        if (s <= minFreqRatio)
          fs = fs*0.5f;
      
      Font font = defaultTreeFont.deriveFont(fs);
      //System.err.println("w="+item.getString(NAME)+" fs="+fs+" rc="+rowCount+" s="+s+" minFreqRatio="+minFreqRatio+" fn="+font);
      return font;
      //}
      //return defaultFont;
    }
      }

  public static class EdgeWidthAction extends StrokeAction {
    
    public EdgeWidthAction(String group) {
      super(group);
      defaultStroke = this.getDefaultStroke();
    } 
    
    public BasicStroke getStroke(VisualItem item) {
      Node tn = ((Edge)item).getTargetNode();
      int nc = tn.getInt(NODECOUNT);
      if (nc==1)
        return new BasicStroke(0f);
      return defaultStroke;
    }
    
  }



 public static class WordSizeAction extends ItemAction {
        
    public WordSizeAction(String group){
      super(group);
    }
        
   public void process(VisualItem item, double frac) {
      //if (item instanceof Node ){
        //int dp = item.getDepth()+1;
        //double incfactor = 
      NodeItem tn = ((EdgeItem)item).getTargetItem();
      int nc = tn.getInt(NODECOUNT);
      if (nc == 1){
        System.err.println("hiding___"+item);
        PrefuseLib.updateVisible(tn,false);
        PrefuseLib.updateVisible(item,false);
      }

    }
    
  }

  public static class WordCountPredicate extends prefuse.data.expression.AbstractPredicate
  {
    
    public boolean getBoolean(Tuple t){
      if (t instanceof Node ){
            System.err.println("==="+((Node)t).getInt(NODECOUNT));
        if (((Node)t).getDepth() == 0 )
          return true;
        else
          return t.canGetInt(NODECOUNT) &&
            ((Node)t).getInt(NODECOUNT) > cutoff_frequency;
      }
      if ( ((Edge)t).getSourceNode().getInt(
NODECOUNT) <= cutoff_frequency )
        return false;
      if ( ((Edge)t).getTargetNode().getInt(NODECOUNT) <= cutoff_frequency )
        return false;
      return true;
    }
    
  }
  
}

