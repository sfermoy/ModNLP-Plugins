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
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JSlider;
import javax.swing.Timer;
import java.util.Hashtable;

/**
 *  Basic concordance tree generator. A concordance tree is a prefix
 *  tree encoding the context to the right of a concordance; or a
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
  Tree tmptree = null;

  public static final int GROW = 1;
  public static final int PRUNE = 2;

  private int current_action = GROW;
  private double max_nd1daughters = 20;
  final JTextField max_nd1daughters_field = new JTextField(""+max_nd1daughters, 4);
  int nrows = 0;
  int total_nrows = 0;
  final JSlider max_pctn1daughters_slider = new JSlider(JSlider.HORIZONTAL, 0,100,50);

  // prune_cutoff deprecated 
  private double prune_cutoff = 1;

  private boolean case_sensitive = false;
  private boolean tree_changed = true;
  /**
   * Describe left_context here.
   */
  private boolean left_context = false;

  private Thread thread;
  private JFrame thisFrame = null;

  JLabel statsLabel = new JLabel("                            ");
  SubcorpusCaseStatusPanel sccsPanel = new SubcorpusCaseStatusPanel(null);

  JButton growTreeButton = new JButton("Grow tree ->");
  JButton growTreeLeftButton = new JButton("<- Grow tree");
  JButton uldButton = new JButton("Read concordances...");

  private JProgressBar progressBar;
  private ConcordanceVector concVector =  null;
  private int language =  modnlp.Constants.LANG_EN;



  JPanel tpanel = new JPanel(new BorderLayout());

  private ConcordanceTree conc_tree =  null;

  private static String title = new String("MODNLP Plugin: ConcordanceTreeViewer 0.3"); 
  private ConcordanceBrowser parent = null;
  private boolean guiLayoutDone = false;
  int max_pctn1daughters = 50;

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
      //if (parent != null)
      //  growTree(); 
      return;
    }
    JButton dismissButton = new JButton("Quit");
    
    dismissButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          thisFrame.setVisible(false);
          if (parent == null)
            System.exit(0);
        }});

    ActionListener gtal = new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          stop();
          if (left_context)
            tree_changed = true;
          setLeftContext(false);
          current_action = GROW;
          disableButtons();
          start();
        }};
 
    growTreeButton.addActionListener(gtal);

    growTreeLeftButton.
      addActionListener(new ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (!left_context)
              tree_changed = true;
            setLeftContext(true);
            current_action = GROW;
            stop(); start();
          }});

    JPanel cop = new JPanel();
    cop.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    JLabel pruneTreeButton = new JLabel("Depth-1 width");
    cop.add(pruneTreeButton);
    //cop.add(max_nd1daughters_field);
    Hashtable lt = new Hashtable();
    lt.put( new Integer( 1 ), new JLabel("1%") );
    lt.put( new Integer( 100 ), new JLabel("100%") );
    max_pctn1daughters_slider.setLabelTable( lt );
    max_pctn1daughters_slider.setPaintLabels(true);
    cop.add(max_pctn1daughters_slider);

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
                  tree_changed = true;
                }
            }
          catch (java.io.IOException e){
            alertWindow("Error downloading concordances\n!"+e);
          }}}
      );

 
    max_pctn1daughters_slider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (!max_pctn1daughters_slider.getValueIsAdjusting()) {
            max_pctn1daughters = (int)max_pctn1daughters_slider.getValue();
            growTree();
          }
        }});


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

    setDisplay(new ConcordanceTree(new Tree()), 1);
    getContentPane().add(pas, BorderLayout.NORTH);
    getContentPane().add(tpanel, BorderLayout.CENTER);
    getContentPane().add(pabottom, BorderLayout.SOUTH);

    pack();
    setVisible(true);
    guiLayoutDone = true;
    /* // this grows the tree automatically when the plugin is first activated
       // However, sometimes it seems to cause freezes, so we will comment it out fro now
    if (parent != null){
      // delay 'pressing' of the grow tree button to avoid thread starvation
      //javax.swing.Timer tmr = new javax.swing.Timer(1000, gtal); 
      //tmr.setRepeats(false); 
      //tmr.start();
    }
    */
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
      disableButtons();
      growTree(); 
      enableButtons();
      break;
    case PRUNE:
      pruneTree(); break;
    default:
      disableButtons();
      growTree(); 
      enableButtons();
      break;
    }
  }

  private void disableButtons(){
    //growTreeButton.setEnabled(false);
    //growTreeLeftButton.setEnabled(false);
  }

  private void enableButtons(){
    //growTreeButton.setEnabled(true);
    //growTreeLeftButton.setEnabled(true);
  }

  public void pruneTree(){
    Tree pt = getPrunedTree(conc_tree.getTree());
    //    setDisplay(new ConcordanceTree(pt),pt.getRoot().getInt(ConcordanceTree.ROWCOUNT));
  }

  public synchronized void growTree() {
    try{   
      if (parent != null){
        concVector = parent.getConcordanceVector();
        sccsPanel.updateStatus();
        language = parent.getLanguage();
      }

      if (tree_changed){
        buildTreeFromConcordances();
      }
 
      // now prune at the kw+1 level
      Node cnode = tmptree.getRoot();
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
          int dg = cnode.getDegree();
      System.err.println(" deg="+dg+" tree_changed="+tree_changed);
      if (tree_changed){
        if (dg > 20 )
          max_nd1daughters = dg * max_pctn1daughters/100;
        else {
          max_nd1daughters = dg;
          max_pctn1daughters_slider.setValue(100);
          // new Integer(max_nd1daughters_field.getText()).intValue();
        }
        tree_changed = false;
      }
      else
        max_nd1daughters = dg * max_pctn1daughters/100;

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

      int nrows = total_nrows;
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

      System.err.println("rc="+nrows);

      Tree tree = getVisibleTree(tmptree);
      //setDisplay(new ConcordanceTree(tmptree),1);
      setDisplay(new ConcordanceTree(tree),1);      
      conc_tree.setRowCount(nrows);
      conc_tree.setMinFreqRatio(1f/nrows);      
      
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


  public Tree getVisibleTree(Tree s){

    Tree t = new Tree();
    Table ntable = t.getNodeTable();
    ntable.addColumn(ConcordanceTree.NAME,String.class);
    ntable.addColumn(ConcordanceTree.NODECOUNT, int.class);
    ntable.addColumn(ConcordanceTree.ROWCOUNT,int.class);
    ntable.addColumn(ConcordanceTree.ISVISIBLE,boolean.class);
    Node q = s.getRoot();
    Node r = t.addRoot();

    r.setString(ConcordanceTree.NAME, q.getString(ConcordanceTree.NAME));
    r.setInt(ConcordanceTree.NODECOUNT, q.getInt(ConcordanceTree.NODECOUNT) );
    r.setInt(ConcordanceTree.ROWCOUNT, q.getInt(ConcordanceTree.ROWCOUNT) );
    r.setBoolean(ConcordanceTree.ISVISIBLE, q.getBoolean(ConcordanceTree.ISVISIBLE) );
    
    for (Iterator<Node> children = q.children(); children.hasNext();){
      Node m = children.next();
      if (! m.getBoolean(ConcordanceTree.ISVISIBLE))
        continue;
      Node n = t.addChild(r);
      n.setString(ConcordanceTree.NAME, m.getString(ConcordanceTree.NAME));
      n.setInt(ConcordanceTree.NODECOUNT, m.getInt(ConcordanceTree.NODECOUNT) );
      n.setInt(ConcordanceTree.ROWCOUNT, m.getInt(ConcordanceTree.ROWCOUNT) );
      n.setBoolean(ConcordanceTree.ISVISIBLE, m.getBoolean(ConcordanceTree.ISVISIBLE) );
      addDescentants(t, m, n);
    }
    return t;
  }

  private void addDescentants(Tree t, Node q, Node r){
    for (Iterator<Node> children = q.children(); children.hasNext();){
      Node m = children.next();
      if (! m.getBoolean(ConcordanceTree.ISVISIBLE))
        continue;
      Node n = t.addChild(r);
      n.setString(ConcordanceTree.NAME, m.getString(ConcordanceTree.NAME));
      n.setInt(ConcordanceTree.NODECOUNT, m.getInt(ConcordanceTree.NODECOUNT) );
      n.setInt(ConcordanceTree.ROWCOUNT, m.getInt(ConcordanceTree.ROWCOUNT) );
      n.setBoolean(ConcordanceTree.ISVISIBLE, m.getBoolean(ConcordanceTree.ISVISIBLE) );
      addDescentants(t, m, n);
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


  public void buildTreeFromConcordances(){
    boolean inittree = true;
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
      total_nrows = concVector.size();
      progressBar.setMaximum(total_nrows);
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
          tmptree = new Tree();
          Table ntable = tmptree.getNodeTable();
          ntable.addColumn(ConcordanceTree.NAME,String.class);
          ntable.addColumn(ConcordanceTree.NODECOUNT, int.class);
          ntable.addColumn(ConcordanceTree.ROWCOUNT,int.class);
          ntable.addColumn(ConcordanceTree.ISVISIBLE,boolean.class);
 
          //tmptree.clear();
          //conc_tree.setRowCount(nrows);
          cnode = tmptree.addRoot();
          cnode.setString(ConcordanceTree.NAME,ctoken);
          cnode.setInt(ConcordanceTree.NODECOUNT, 1);
          //System.err.println("root = "+cnode+" "+cnode);
          cnode.setInt(ConcordanceTree.ROWCOUNT, total_nrows);
          cnode.setBoolean(ConcordanceTree.ISVISIBLE,true);
          //colcounts[0]++;
        }
        else {
          // update root node frequencies
          cnode = tmptree.getRoot();
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
          Node n = tmptree.addChild(cnode);
          n.setString(ConcordanceTree.NAME,ctoken);
          n.setInt(ConcordanceTree.NODECOUNT, 1);
          n.setInt(ConcordanceTree.ROWCOUNT, total_nrows);
          n.setBoolean(ConcordanceTree.ISVISIBLE,true);
          cnode = n;
        } // end updtate tree for words to the right/left of kw
      } // end update trie for whole concordance
      progressBar.setString("Done");

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
    ct.setSize(ConcordanceTree.WIDTH, ConcordanceTree.HEIGHT);//tpanel.getSize());
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

