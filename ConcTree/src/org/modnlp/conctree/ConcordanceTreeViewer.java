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

import modnlp.tec.client.Plugin;
import modnlp.tec.client.ConcordanceBrowser;
import modnlp.tec.client.ConcordanceObject;
import modnlp.tec.client.ConcordanceVector;
import modnlp.tec.client.Upload;
import modnlp.tec.client.gui.SubcorpusCaseStatusPanel;
import modnlp.idx.inverted.TokeniserRegex;
import modnlp.idx.inverted.TokeniserJPLucene;
import modnlp.util.Tokeniser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;

import java.io.File;
import java.lang.Integer;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JFileChooser;

import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import prefuse.Constants;
import prefuse.controls.ControlAdapter;
import prefuse.data.Table;
import prefuse.data.Tree;
import prefuse.data.Tuple;
import prefuse.data.Node;
import prefuse.data.Edge;
import prefuse.data.SpanningTree;
import prefuse.util.ui.JFastLabel;
import prefuse.util.FontLib;
import prefuse.visual.VisualItem;
import java.io.IOException;

/**
 *  Basic concordance tree generator. A concordance tree is a prefix
 *  tree (trie) encoding the context to the right of a concordance; or a
 *  (i.e. a right-to-left prefix tree) encoding the left
 *  context of a concordance
 *
 * @author  S Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
*/

public class ConcordanceTreeViewer extends JFrame
  implements Runnable, Plugin
{
  
  //String tknregexp  = "\\{L}[\\p{L}-.]*'?s?";

  public static final int GROW = 1;
  public static final int PRUNE = 2;

  private int current_action = GROW;
  private int max_nd1daughters = 20;
  final JTextField max_nd1daughters_field = new JTextField(""+max_nd1daughters, 4);
  
  // prune_cutoff deprecated 
  private double prune_cutoff = 1;

  private boolean case_sensitive = false;
  /**
   * Describe left_context here.
   */
  private boolean left_context = false;

  private Thread thread;
  private JFrame thisFrame = null;
  //private Tree tree = null;


  JLabel statsLabel = new JLabel("                            ");
  SubcorpusCaseStatusPanel sccsPanel = new SubcorpusCaseStatusPanel(null);

  private JProgressBar progressBar;
  private ConcordanceVector concVector =  null;
  private int language =  modnlp.Constants.LANG_EN;

  JPanel tpanel = new JPanel(new BorderLayout());


  private ConcordanceTree conc_tree =  null;

  private static String title = new String("MODNLP Plugin: ConcordanceTreeViewer 0.1"); 
  private ConcordanceBrowser parent = null;
  private boolean guiLayoutDone = false;

  public ConcordanceTreeViewer() {
    thisFrame = this;
  }

  // plugin interface method
  public void setParent(Object p){
    parent = (ConcordanceBrowser)p;
    sccsPanel = new SubcorpusCaseStatusPanel(p);

  }

  // plugin interface method
  public void activate() {
    if (guiLayoutDone){
      setVisible(true);
      if (parent != null)
        growTree(); 
      return;
    }

    JButton dismissButton = new JButton("Quit");
    JButton growTreeButton = new JButton("Grow tree ->");
    JButton growTreeLeftButton = new JButton("<- Grow tree");
    JButton uldButton = new JButton("Read concordances...");
    
    dismissButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          thisFrame.setVisible(false);
          if (parent == null)
            System.exit(0);
        }});

    ActionListener gtal = new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          stop();
          setLeftContext(false);
          current_action = GROW;
          start();
        }};
 
    growTreeButton.addActionListener(gtal);

    growTreeLeftButton.
      addActionListener(new ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            setLeftContext(true);
            current_action = GROW;
            stop(); start();
          }});

    JPanel cop = new JPanel();
    cop.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    JLabel pruneTreeButton = new JLabel("Depth-1 width");
    cop.add(pruneTreeButton);
    cop.add(max_nd1daughters_field);

    uldButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          try
            {
              JFileChooser filedial = new JFileChooser();
              int returnVal = filedial.showDialog(null, "Read concordances from...");
              if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                  File file = filedial.getSelectedFile();
                  //System.out.println(file.getName());
                  Upload ulf =
                    new Upload(file);
                  ulf.readConcordances();
                  concVector = ulf.getConcordanceVector();
                }
            }
          catch (java.io.IOException e){
            alertWindow("Error downloading concordances\n!"+e);
          }}}
      );

    JPanel pas = new JPanel();
    pas.add(uldButton);
    pas.add(growTreeLeftButton);
    pas.add(growTreeButton);
    pas.add(dismissButton);
    pas.add(cop);

    growTreeButton.setEnabled(true);
    
    progressBar = new JProgressBar(0,800);
    progressBar.setStringPainted(true);

    JPanel pabottom = new JPanel();

    pabottom.setLayout(new BorderLayout());
    JPanel pa2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel pa3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    pa2.add(progressBar);
    pa2.add(statsLabel);
    statsLabel.setSize(450,statsLabel.getHeight());

    pa3.add(sccsPanel);

    pabottom.add(pa2,BorderLayout.WEST);
    pabottom.add(pa3,BorderLayout.EAST);


    // set visualisation display    
    //setDisplay(new ConcordanceTree(),1);


    getContentPane().add(pas, BorderLayout.NORTH);
    getContentPane().add(tpanel, BorderLayout.CENTER);
    getContentPane().add(pabottom, BorderLayout.SOUTH);

    pack();
    setVisible(true);
    guiLayoutDone = true;
    if (parent != null){
      // delay 'pressing' of the grow tree button to avoid thread starvation
      javax.swing.Timer tmr = new javax.swing.Timer(1000, gtal); 
      tmr.setRepeats(false); 
      tmr.start();
    }
  }

  /**
   * Get the <code>Left_context</code> value.
   *
   * @return a <code>boolean</code> value
   */
  public final boolean isLeftContext() {
    return left_context;
  }

  /**
   * Set the <code>Left_context</code> value.
   *
   * @param newLeft_context The new Left_context value.
   */
  public final void setLeftContext(final boolean newLeft_context) {
    this.left_context = newLeft_context;
  }


  public void start(){
    if ( thread == null ){
      thread = new Thread(this);
      thread.setPriority (Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  public void stop() {
    if ( thread != null ){
      thread.stop();
      
      thread = null;
    }
  }

  public void run(){
    switch(current_action){
    case GROW:
      growTree(); break;
    case PRUNE:
      pruneTree(); break;
    default:
      growTree(); break;
    }
  }

  public void pruneTree(){
    Tree pt = getPrunedTree(conc_tree.getTree());
    //    setDisplay(new ConcordanceTree(pt),pt.getRoot().getInt(ConcordanceTree.ROWCOUNT));
  }

  public synchronized void growTree() {
    try{

      //setDisplay(new ConcordanceTree(),1);
      Tree ctree = null;
      boolean inittree = true;

      if (parent != null){
        concVector = parent.getConcordanceVector();
        sccsPanel.updateStatus();
        language = parent.getLanguage();
      }

      Vector columns = new Vector();
      Tokeniser ss;
      switch (language) {
      case modnlp.Constants.LANG_EN:
        ss = new TokeniserRegex("");
        break;
      case modnlp.Constants.LANG_JP:
        ss = new TokeniserJPLucene("");
        break;
      default:
        ss = new TokeniserRegex("");
        break;
      }
      
      int ct = 0;
      progressBar.setString("Growing tree...");
      int nrows = concVector.size();
      progressBar.setMaximum(nrows);
      progressBar.setValue(ct++);
      for (Iterator<ConcordanceObject> p = concVector.iterator(); p.hasNext();){
        ConcordanceObject co = p.next();
        progressBar.setValue(ct++);
        if (co == null)
          break;
        Object[] tkns;
        if (isLeftContext()){
          Object[] t = (ss.split(co.getLeftContext()+" "+co.getKeyword())).toArray();
          tkns = new Object[t.length];
          int j = t.length-1;
          for(int i=0; i<t.length; i++)
            tkns[j-i] = t[i];
        }
        else
          tkns = (ss.split(co.getKeywordAndRightContext())).toArray();
        Node cnode = null;
        String ctoken = (String)tkns[0];
        if (!case_sensitive)
          ctoken = ctoken.toLowerCase();
        
        // initialise (keyword/root node)
        if (inittree){
          inittree = false;
          ctree = new Tree();
          Table ntable = ctree.getNodeTable();
          ntable.addColumn(ConcordanceTree.NAME,String.class);
          ntable.addColumn(ConcordanceTree.NODECOUNT, int.class);
          ntable.addColumn(ConcordanceTree.ROWCOUNT,int.class);
          ntable.addColumn(ConcordanceTree.ISVISIBLE,boolean.class);
 
          //conc_tree.resetTree();
          //conc_tree.setRowCount(nrows);
          cnode = ctree.addRoot();
          cnode.setString(ConcordanceTree.NAME,ctoken);
          cnode.setInt(ConcordanceTree.NODECOUNT, 1);
          //System.err.println("root = "+cnode+" "+cnode);
          cnode.setInt(ConcordanceTree.ROWCOUNT, nrows);
          //colcounts[0]++;
        }
        else {
          // update root node frequencies
          cnode = ctree.getRoot();
          cnode.setInt(ConcordanceTree.NODECOUNT, cnode.getInt(ConcordanceTree.NODECOUNT)+1);
        }
        // update trie by reading string from left to right 
        for (int i = 1; i < tkns.length; i++){
          ctoken = (String)tkns[i];
          
          if (!case_sensitive)
            ctoken = ctoken.toLowerCase();
          
          boolean found = false;
          //colcounts[i]++;
          
          for (Iterator<Node> children = cnode.children(); children.hasNext();){
            Node n = children.next();
            if(ctoken.equals(n.getString(ConcordanceTree.NAME))){// found a matching child
              cnode = n;
              cnode.setInt(ConcordanceTree.NODECOUNT, cnode.getInt(ConcordanceTree.NODECOUNT)+1);
              found = true;
              break;
            }
          } // end iteration 
          
          if (found)
            continue;
          // couldn't find a matching child; make one
          Node n = ctree.addChild(cnode);
          n.setString(ConcordanceTree.NAME,ctoken);
          n.setInt(ConcordanceTree.NODECOUNT, 1);
          n.setInt(ConcordanceTree.ROWCOUNT, nrows);
          //Edge e = tree.addChildEdge(cnode,n);
          cnode = n;
        } // end updtate trie for words to the right of kw
      } // end update trie for whole concordance
      progressBar.setString("Done");      

      /*
        NOTE ON PRUNNING: the purpose of this branch (prefuselike) is
        to rewrite this code so that instead of starting off with a
        pruned tree, we always keep the original (full) tree and
        simply display the tree that results from the evaluation of
        certain prefuse.visual.expression.Predicates to the tree at
        display time. In other words, we want to do the pruning at the
        visual level, as opposed to the data level, as is done below.
        This is what we call a prefuse-like way of handling different
        levels of detail on the Concordance Tree. 

        In order to implement this we have created ExpansionFilter,
        which gets all nodes to be filtered out and recurses down
        their subtrees, marking all VisualItem's in them
        invisible. This is implemented in ConcordanceTree.java:278:

        ExpansionFilter visibfilter = new ExpansionFilter(TREENODES,new WordCountPredicate());

        This indeed has the effect of rendering them
        invisible, but it unfortunately it doesn't reformat the tree
        to use the space made available by the invisible items,
        resulting in gaps. This could be a bug in
        NodeLinkTreeLayout. We should investigate this next.

        I have also attempted creating a new FocusCgroup (VTREE) and
        setting the layout action to operate on this focus group
        instead of the original TREE primary group (eg
        m_vis.addFocusGroup(VTREE, vis_ts); and then new
        NodeLinkTreeLayout(VTREE,m_orientation, 4, 0, 0), where VTREE
        is ). This doesn't seem to work either, as the new FocusGroup
        is a TupleSet, which cannot be cast as a Tree for rendering by
        NodeLinkTreeLayout.
       */
      // now prune at the kw+1 level
      Node cnode = ctree.getRoot();
      Node[] togo = new Node[cnode.getChildCount()];
      Node[] tokeep = new Node[cnode.getChildCount()];
      Arrays.fill(togo,null);
      Arrays.fill(tokeep,null);
      int i = 0; int j =0;


      // hist store distribution of 20 least frequent children nodes (hist[0-19]) 
      // plus the frequency of the remaining nodes together hist[20]
      int[] hist = new int[21];
      Arrays.fill(hist,0);

      
      // create a 'histogram' of depth-1 word counts
      for (Iterator<Tuple> children = cnode.children(); children.hasNext();){
        Tuple n = children.next();
        int c = n.getInt(ConcordanceTree.NODECOUNT);
        
        //System.err.println("pruning "+n+" count = "+n.getInt(ConcordanceTree.NODECOUNT));
        //if (!(n instanceof Node) )
        //  continue;
        if (c > 20)
          hist[20]++;
        else
          hist[c-1]++;
      }

      // find 'cutoff point' (i.e. the number of occurrences below which a node is removed) 
      max_nd1daughters = new Integer(max_nd1daughters_field.getText()).intValue();

      int nr = 0;
      int cutoff = 0;
      for (int h = 20; h >= 0 ; h--) {
        nr = nr+hist[h];
        if (nr > max_nd1daughters && cutoff == 0)
          cutoff = h+1;
        //System.err.println(h+"="+hist[h]+" cutoff="+cutoff+", nr="+nr);
      }
      //System.err.println("nr="+nr+", prune_cutoff="+cutoff);

      int ndaughters = cnode.getDegree();
      int ngranddaughters = 0;

      // We must try to set visibility at the visual representation level instead
      for (Iterator<Tuple> children = cnode.children(); children.hasNext();){
        Node n = (Node)children.next();
        //System.err.println("testing = "+n+" ct ="+n.getInt(ConcordanceTree.NODECOUNT));
        if (n.getInt(ConcordanceTree.NODECOUNT) <= cutoff && ndaughters > max_nd1daughters){
          n.setBoolean(ConcordanceTree.ISVISIBLE,false);
          togo[i++] = n;
          ndaughters--;
          nrows = nrows-n.getChildCount();
          //nrows = nrows-(int)prune_cutoff;
          //System.err.println("marking for removal = "+n+" ct ="+n.getInt(ConcordanceTree.NODECOUNT)+" ndaughters="+ndaughters);
        }
        else {
          n.setBoolean(ConcordanceTree.ISVISIBLE,true);
          tokeep[j++] = n;
          ngranddaughters = ngranddaughters+n.getChildCount();
        } 
      }

      /*      for (int k = 0; k < togo.length; k++){
        if (togo[k] != null){
          System.err.println("removing = "+togo[k]);
          ctree.removeChild(togo[k]);
        }*/
/*        if ( ngranddaughters > (max_nd1daughters/3)*max_nd1daughters && tokeep[k] != null) {
          int keep = (int)Math.ceil(tokeep[k].getChildCount()/1.5);
          Node[] togo2 = new Node[tokeep[k].getChildCount()];
          Arrays.fill(togo2,null);
          int l = 0;
          for (Iterator<Node> children = tokeep[k].children(); children.hasNext();){
            Node n = children.next();
            if (n.getInt(ConcordanceTree.NODECOUNT) < 2 && keep-- > 0) {
              System.err.println("removing = "+n);
              togo2[l++] = n;
              nrows = nrows-n.getChildCount();
              //conc_tree.tree.removeChild(n);
            }}
          System.err.println("============removing layer-2 children of -"+tokeep[k]);
          for (int m = 0; m < togo2.length ; m++) {
            if (togo2[m] != null){
              System.err.println("removing layer-2 = "+togo2[m]);
              conc_tree.tree.removeChild(togo2[m]);
            }}
        }      
        
        }*/


      setDisplay(new ConcordanceTree(ctree),1);
      conc_tree.setRowCount(nrows);
      conc_tree.setMinFreqRatio(1f/nrows);

      System.err.println("rc="+nrows);
      
      //cnode = conc_tree.tree.getRoot();
      //for (Iterator<Node> children = cnode.children(); children.hasNext();){
      //  Node n = children.next();
      //  System.err.println("node="+n);//n.setInt(ConcordanceTree.ROWCOUNT, nrows);
      //}
      
      
      if (isLeftContext())
        conc_tree.setOrientation(Constants.ORIENT_RIGHT_LEFT);
      else
        conc_tree.setOrientation(Constants.ORIENT_LEFT_RIGHT);
      conc_tree.initialView();
      
      //setDisplay(ct, nrows);
        
    } // end try
    catch (Exception ex) {
      ex.printStackTrace(System.err);
      JOptionPane.showMessageDialog(null, "Error creating concordance tree" + ex,
                                    "Error!", JOptionPane.ERROR_MESSAGE);
    }
  }


  private Tree updateColCounts(Tree tree, int[] c) {
    for (Iterator<Tuple> spant = tree.tuples(); spant.hasNext();) {
      Tuple n = spant.next();

      if (!(n instanceof Node) )
        continue;

      int d = ((Node)n).getDepth();
      ((Node)n).setInt(ConcordanceTree.ROWCOUNT, c[d]);
      //System.err.println(new String(ch)+"c++ = "+n.getString(NAME)+" ct="+cnode.getInt(NODECOUNT));
    }
    return tree;
  }


  // placeholder for something future
  private Tree getPrunedTree(Tree tree) {
    return tree;
  }

  private void setDisplay(ConcordanceTree ct, int rc){
    ct.setRowCount(rc);
    ct.setMinFreqRatio(1f/rc);
    if (isLeftContext())
      ct.setOrientation(Constants.ORIENT_RIGHT_LEFT);
    ct.setDefaultTreeFont(FontLib.getFont("Tahoma", Font.PLAIN, 10));
    tpanel.removeAll();
    ct.setSize(tpanel.getSize());
    ct.setBackground(Color.WHITE);
    ct.setForeground(Color.BLACK);
    tpanel.add(ct, BorderLayout.CENTER);
    conc_tree = ct;
    //System.err.println("set tree"+conc_tree.getDefaultTreeFont());

  }

  private void setPruneCutoff(double c){
    prune_cutoff = c;
  }

 public void alertWindow (String msg)
  {
    JOptionPane.showMessageDialog(null,
                                  msg,
                                  "alert",
                                  JOptionPane.ERROR_MESSAGE);
  }  

  public static void main(String[] args){
    try{
      ConcordanceTreeViewer cv = new ConcordanceTreeViewer();
      if (args[0] != null){
        Upload ulf = new Upload(new File(args[0]));
        ulf.readConcordances();
        cv.concVector = ulf.getConcordanceVector();
        if (args.length > 1 && args[1] != null)
          cv.language = new Integer(args[1]).intValue();
      }
      cv.activate();
    }
    catch(Exception e){
      e.printStackTrace(System.err);
      System.err.println("Error reading "+args[0]+"\n"+e);
    }
  }


}

