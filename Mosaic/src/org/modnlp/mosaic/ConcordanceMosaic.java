/**
 *  (c) 2014 S Sheehan <shane.sheehan@tcd.ie>
 *      2016 S Sheehan, S Luz <luzs@acm.org>
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

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ToolTipManager;
import modnlp.tec.client.TecClientRequest;
import modnlp.idx.database.Dictionary;
import modnlp.idx.database.FreqTable;
import modnlp.idx.inverted.TokeniserJP;
import modnlp.idx.inverted.TokeniserRegex;
import modnlp.tec.client.ConcordanceBrowser;
import modnlp.tec.client.ConcordanceObject;
import modnlp.tec.client.Plugin;
import modnlp.tec.client.StateChanged;
import modnlp.util.Tokeniser;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Node;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import prefuse.action.distortion.Distortion;
import prefuse.controls.AnchorUpdateControl;
import prefuse.data.tuple.TupleSet;
import prefuse.render.LabelRenderer;
import java.net.URL;

/**
 *
 * @author shane
 */
public class ConcordanceMosaic extends JFrame
  implements Runnable, Plugin, StateChanged{

  private static Visualization vis;
  private static int nrows;
  public static final String NAME = "name";
  public static final String NODECOUNT = "nodecount";
  public static final String ROWCOUNT = "colcount";
  public static final int MAXCOLS = 200;
  public static final int GROW = 1;
  public static final int PRUNE = 2;
  private boolean is_rel_freq = false;
  private Graph graph = null;
  private JFrame thisFrame = null;
  private JProgressBar progressBar;
  JPanel tpanel = new JPanel(new BorderLayout());
  //private ConcordanceGraph conc_tree = null;
  private static String title = new String("MODNLP Plugin: ConcordanceMosaicViewer 0.2");
  private ConcordanceBrowser parent = null;
  private boolean guiLayoutDone = false;
  private Object[][] sentences;
  private Thread thread;
    
  private VisualItem selected=null;
  private Color selectedColor = null;
  private Map<Integer, ArrayList<VisualItem> > sentenceIndexToVisualitems = null;
  private TecClientRequest clRequest = new TecClientRequest();
  private TecClientRequest totRequest = new TecClientRequest();
    
  private  Distortion dist;
  private HttpURLConnection exturlConnection;
  private HttpURLConnection toturlConnection;
  private Dictionary d =null;
  private BufferedReader input1;
  private BufferedReader input2;


  public ConcordanceMosaic() {
    thisFrame = this;
  }
    
  public ArrayList<VisualItem> getVisualItemsInSentence(Integer i) {
    return sentenceIndexToVisualitems.get(i);
  }

  // plugin interface method
  public void setParent(Object p) {
    parent = (ConcordanceBrowser) p;
        
  }
    
  public void setSelected(VisualItem i) {
    selected= i;
  }
     
  public VisualItem getSelected() {
    return selected;
  }
     
  public void setSelectedColor(Color c) {
    selectedColor= c;
  }
     
  public Color getSelectedColor() {
    return selectedColor;
  }

  // plugin interface method
  public void activate() {
    if (guiLayoutDone) {
      setVisible(true);
      return;
    }

    getContentPane().add(tpanel, BorderLayout.CENTER);
        
    JButton frequencyButton = new JButton("Concordance Word Frequency (WC)");
    JButton relFrequencyButton = new JButton("WCRelative to corpus frequency");
        
    JPanel pas = new JPanel();
       
    pas.add(frequencyButton);
    pas.add(relFrequencyButton);
        
    frequencyButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          //stop();
          is_rel_freq = false;
          MakeMosaic();
          //start();
        }});

    relFrequencyButton.
      addActionListener(new ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            //stop();
            is_rel_freq = true;
            MakeMosaic();
            //sort parent
            //start();
          }});
        
    getContentPane().add(pas, BorderLayout.NORTH);

    guiLayoutDone = true;
    parent.addChangeListener(this);
    try {
      if (parent.isStandAlone()) {
        d = parent.getDictionary();
      }else {
        clRequest.setServerURL("http://"+parent.getRemoteServer());
        clRequest.setServerPORT(parent.getRemotePort());
        clRequest.put("request","freqword");
        //clRequest.put("keyword","");
        if (parent.isSubCorpusSelectionON())
          clRequest.put("xquerywhere",parent.getXQueryWhere());
        clRequest.put("casesensitive",parent.isCaseSensitive()?"TRUE":"FALSE");
        clRequest.setServerProgramPath("/freqword");
        URL exturl = new URL(clRequest.toString());
        exturlConnection = (HttpURLConnection) exturl.openConnection();
        //exturlConnection.setUseCaches(false);
        exturlConnection.setRequestMethod("GET");
        //System.err.println("--input set---");
               
      }
    }
    catch(IOException e)
      {
        System.err.println("Exception: couldn't create stream socket"+e);
      }
    getTotalNoTokens();
    MakeMosaic();
  }
    
  private int getTotalNoTokens(){
    int result=0;
    try {
      if (parent.isStandAlone()) {
        result = d.getTotalNoOfTokens();
      }
      else {
        totRequest.setServerURL("http://"+parent.getRemoteServer());
        totRequest.setServerPORT(parent.getRemotePort());
        totRequest.put("request","nooftokens");
        //clRequest.remove(keyword)
        if (parent.isSubCorpusSelectionON())
          totRequest.put("xquerywhere",parent.getXQueryWhere());
        totRequest.put("casesensitive",parent.isCaseSensitive()?"TRUE":"FALSE");
        totRequest.setServerProgramPath("/totaltokens");
        URL toturl = new URL(totRequest.toString());
        toturlConnection = (HttpURLConnection) toturl.openConnection();
        //exturlConnection.setUseCaches(false);
        toturlConnection.setRequestMethod("GET");
        //System.err.println("--input set---");
                  
        input1 = new
          BufferedReader(new
                         InputStreamReader(toturlConnection.getInputStream() ));
        result =Integer.parseInt(input1.readLine()); 
        // System.out.println(result);
                    
        toturlConnection.disconnect();
                       
      }
    }
    catch(IOException e)
      {
        System.err.println("Exception: couldn't create stream socket"+e);
      }
    return result;
  }
    
  private int getNoOfTokens(String word){
    int result=0;
         
    try {
      if (parent.isStandAlone()) {
        result = d.getFrequency(d.getCaseTable().getAllCases(word));
      }
      else {
              
              
                
        clRequest.setServerURL("http://"+parent.getRemoteServer());
        clRequest.setServerPORT(parent.getRemotePort());
        clRequest.put("request","freqword");
        clRequest.put("keyword",word);
        //clRequest.put("keyword","");
        if (parent.isSubCorpusSelectionON())
          clRequest.put("xquerywhere",parent.getXQueryWhere());
        clRequest.put("casesensitive",parent.isCaseSensitive()?"TRUE":"FALSE");
        clRequest.setServerProgramPath("/freqword");
        URL exturl = new URL(clRequest.toString());
        exturlConnection = (HttpURLConnection) exturl.openConnection();
        //exturlConnection.setUseCaches(false);
        exturlConnection.setRequestMethod("GET");
        input2 = new
          BufferedReader(new
                         InputStreamReader(exturlConnection.getInputStream() ));
        result = Integer.parseInt(input2.readLine());      
        exturlConnection.disconnect();
                 
      }
    }
    catch(IOException e)
      {
        System.err.println("Exception: couldn't create stream socket"+e);
      }
    return result;
  }

  public void MakeMosaic() {
    if(!parent.isReceivingFromServer() ){
      try {

        selected=null;
        String current = "";
        graph = null;
        graph = new Graph();
        graph.addColumn("word", String.class);
        graph.addColumn("frequency", Double.class);
        graph.addColumn("rel_frequency", Double.class);
        graph.addColumn("column", Integer.class);
        graph.addColumn("sentences", ArrayList.class);
        graph.addColumn("color", Integer.class);
        //sentenceIndexToVisualitems =null;
        sentenceIndexToVisualitems = new HashMap<Integer, ArrayList<VisualItem> >();


        Tokeniser ss;
        int la = parent.getLanguage();


        //System.out.println(d.getFrequency(d.getCaseTable().getAllCases("ladies")));
        //System.out.println(d.getDictProps().size());
        //System.out.println(d.getFileFrequencyTable(1, true).getTokenCount());
        //System.out.println(d.getCaseTable().getAllCases("the").toString());
        //System.out.println(d.getFrequency(d.getCaseTable().getAllCases("the")));



        //System.out.println("did it work");
        //System.out.println(freqTable.getTotalNoOfTokens());
        //System.out.println(d.getCaseTable().getAllCases("opened"));



        switch (la) {
        case modnlp.Constants.LANG_EN:
          ss = new TokeniserRegex("");
          break;
        case modnlp.Constants.LANG_JP:
          ss = new TokeniserJP("");
          break;
        default:
          ss = new TokeniserRegex("");
          break;
        }


        int current_sentence = 0;

        nrows = parent.getConcordanceVector().size();
        sentences = new Object[nrows][];
        String keyword = (String)ss.splitWordOnly(parent.getConcordanceVector().elementAt(0).getKeywordAndRightContext()).toArray()[0];


        for (Iterator<ConcordanceObject> p = parent.getConcordanceVector().iterator(); p.hasNext();) {
          ConcordanceObject co = p.next();

          if (co == null) {
            break;
          }

          Object[] tkns2;
          //left context
          Object[] tkns1 = (ss.split(co.getLeftContext())).toArray();


          //right context
          tkns2 = (ss.splitWordOnly(co.getKeywordAndRightContext())).toArray();
          Object[] tkns = new Object[9];
          //account for concordances at begining or end of docs

          if(tkns1.length>4){
            System.arraycopy(tkns1, (tkns1.length - 4), tkns, 0, 4);
          }else{
            System.arraycopy(tkns1, 0, tkns, 4-tkns1.length, tkns1.length);

          }

          if(tkns2.length>5){
            System.arraycopy(tkns2, 0, tkns, 4, 5);
          }else{
            System.arraycopy(tkns2, 0, tkns, 4, tkns2.length);

          }
          sentences[current_sentence] = tkns;
          current_sentence++;


        }
        //for each column
        for (int i = 0; i < 9; i++) {
          double rel_column_length = 0;
          String[] column = new String[nrows];
          //loop throught columns entries
          for (int j = 0; j < nrows; j++) {
            Object[] ls = sentences[j];
            current = (String) ls[i];
            if(current == null)
              current ="*null*";

            column[j] = current.toLowerCase();



          }

          // create maps required for sorting
          final Map<String, Integer> counter = new HashMap<String, Integer>();
          final Map<String, ArrayList<Integer> > wordToSentenceIndex = new HashMap<String, ArrayList<Integer> >();
          final Map<String, Double> Rel_freq_counter = new HashMap<String, Double>();
          int coli =0;
          for (String str : column) {
            counter.put(str, 1 + (counter.containsKey(str) ? counter.get(str) : 0));

            ArrayList temp=null;
            if(wordToSentenceIndex.containsKey(str)){
              temp=wordToSentenceIndex.get(str);
            }else
              temp = new ArrayList();
            temp.add(coli);
            wordToSentenceIndex.put(str, temp);
            coli++;

          }
          List<String> list1 = new ArrayList<String>(counter.keySet());


          column = list1.toArray(new String[list1.size()]);


          //calculate the corpus frequencies of the words in the coulmn
          for (int x = 0; x < column.length; x++) {


            int corpus_word_frequency = getNoOfTokens(column[x]);
            // Set infrequent words to a very small value
            if (counter.get(column[x])<2) {
              Rel_freq_counter.put(column[x], 0.0000000001);
            }
            else{
              double temp=(( ((counter.get(column[x])*10.0) /nrows) / ( ((corpus_word_frequency*10.0)/getTotalNoTokens()) ) ));
              if (temp<10000000.0) {
                Rel_freq_counter.put(column[x], temp);
              } else {
                Rel_freq_counter.put(column[x], 0.0000000001);
              }
            }
            rel_column_length += Rel_freq_counter.get(column[x]);
          }

          //custom sorts for reletive frequency and simple frequency visulisation
          if (!is_rel_freq){
            Collections.sort(list1, new Comparator<String>() {
                @Override
                  public int compare(String x, String y) {
                  if (counter.get(y) == counter.get(x)) {
                    return x.compareTo(y);
                  } else {
                    return counter.get(y) - counter.get(x);
                  }
                }
              });
          }else{
            Collections.sort(list1, new Comparator<String>() {
                @Override
                  public int compare(String x, String y) {
                  Double a =(Rel_freq_counter.get(y)*100);
                  Double b =(Rel_freq_counter.get(x)*100);

                  return (a.intValue() - b.intValue());

                }

              });

          }

          column = list1.toArray(new String[list1.size()]);

          for (int x = 0; x < column.length; x++) {
            Node n = graph.addNode();
            n.set("word", column[x]);
            if(is_rel_freq){
              n.set("frequency", (Double) (Rel_freq_counter.get(column[x])/rel_column_length));
            }else{
              n.set("frequency", (Double) (counter.get(column[x]) * 1.0) / nrows);
            }

            n.set("column", i);
            n.set("sentences", wordToSentenceIndex.get(column[x]));
            int color= 0;
            if(i%2==0){
              color =2;
            }else{
              color=4;
            }
            if(x%2==0){
              color+=1;
            }
            if(i==4)
              color=6;
            n.set("color",color);





          }


        }
        setDisplay();
        //add Visual item to hashmap with index of sentence numbers
        makeHashMap();

      } // end try
      catch (Exception ex) {
        ex.printStackTrace(System.err);
        JOptionPane.showMessageDialog(null, "Error doing stuff" + ex,
                                      "Error!", JOptionPane.ERROR_MESSAGE);
      }
    }//enf if recieveing from server
  }
  private void makeHashMap(){
    TupleSet ts = vis.getVisualGroup("graph.nodes");
        
    //Iterator iter = ts.tuples();
    for ( Iterator iter = ts.tuples();iter.hasNext();){
      VisualItem item = (VisualItem)iter.next();
      for(Integer index : (ArrayList<Integer>)item.get("sentences"))
        {
          ArrayList temp=null;
          if(sentenceIndexToVisualitems.containsKey(index)){
            temp=sentenceIndexToVisualitems.get(index);
          }else
            temp = new ArrayList();
          temp.add((VisualItem)item);
          sentenceIndexToVisualitems.put(index, temp);
        }
                  
    }

                    
                            
                
  }

  private void setDisplay() {
    tpanel.removeAll();


    vis = new Visualization();
    vis.add("graph", graph);


    setUpActions();
    setUpRenderers();

    Display d = new Display(vis);
    d.setSize(885, 500); //885,450 use 500 to see extra

    //d.addControlListener(new DragControl());
    d.addControlListener(new PanControl(true));
    d.addControlListener(new ZoomControl());
    d.addControlListener(new WheelZoomControl());
    d.addControlListener(new MosaicTooltip(parent));
       
    ToolTipManager.sharedInstance().setInitialDelay(650);
    ToolTipManager.sharedInstance().setReshowDelay(650);
    ToolTipManager.sharedInstance().setDismissDelay(1700);
      
    d.addControlListener(new HoverTooltip(parent,vis,this));
        
    d.addControlListener(new AnchorUpdateControl(dist, "distort"));
        
    tpanel.add(d, BorderLayout.CENTER);
    pack();
    setVisible(true);
    vis.run("color");
    vis.run("layout");

  }

  public static void setUpRenderers() {
        
      
    // rf.add(new InGroupPredicate(treeEdges), m_edgeRenderer);
    //vis.setRendererFactory(rf);

    MosaicRenderer r = new MosaicRenderer(nrows, 450);
    DefaultRendererFactory drf = new DefaultRendererFactory(r);
    LabelRenderer lalala = new LabelRenderer("word");
        
       

    vis.setRendererFactory(drf);
    // We now have to have a renderer for our decorators.
    drf.add(new InGroupPredicate("boxlabel"), lalala);

    // -- Decorators
    final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();
    DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
    DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR,
                                ColorLib.rgb(0, 0, 0));
    DECORATOR_SCHEMA.setDefault(VisualItem.FONT,
                                FontLib.getFont("Tahoma", 14));

    vis.addDecorators("boxlabel", "graph.nodes", DECORATOR_SCHEMA);


  }
  // -- 4. the actions --------------------------------------

  public void setUpActions() {
    // We must color the nodes of the graph.  
    // Notice that we refer to the nodes using the text label for the graph,
    // and then appending ".nodes".  The same will work for ".edges" when we
    // only want to access those items.
    // The ColorAction must know what to color, what aspect of those 
    // items to color, and the color that should be used.
    //            ColorAction fill = new ColorAction("graph.nodes", 
    //                                                VisualItem.FILLCOLOR,
    //                                                ColorLib.rgb(0, 200, 0));
        
    DataColorAction fill = new DataColorAction("graph.nodes", "color",
                                               Constants.NOMINAL,
                                               VisualItem.FILLCOLOR,
                                               new int[]{ColorLib.color(new java.awt.Color(181,222,223)),
                                                         ColorLib.color(new java.awt.Color(88,170,143)),
                                                         ColorLib.color(new java.awt.Color(148,222,196)),
                                                         ColorLib.color(new java.awt.Color(194,228,216)),
                                                         ColorLib.color(new java.awt.Color(125,232,212))
                                               });
        
    // new int[]{ColorLib.color(new java.awt.Color(181,222,223)),
    //         ColorLib.color(new java.awt.Color(227,216,216)),
    //         ColorLib.color(new java.awt.Color(148,222,196)),
    //        ColorLib.color(new java.awt.Color(255,227,227)),
    //       ColorLib.color(new java.awt.Color(125,232,212))
    //       }
    System.out.println(ColorLib.color(java.awt.Color.CYAN));
    //.getInterpolatedPalette(5,ColorLib.color(java.awt.Color.CYAN), ColorLib.color(java.awt.Color.YELLOW))
    //5,ColorLib.color(java.awt.Color.PINK), ColorLib.color(java.awt.Color.MAGENTA)
    //ItemAction textColor = new ColorAction("graph.nodes",
    //                                  VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0));
    //m_vis.putAction("textColor", textColor);


    // Similarly to the node coloring, we use a ColorAction for the 
    // edges
    //FontAction size = new FontAction("graph.nodes", new Font("Tahoma", 0, 32));
    //ActionList fonts = new ActionList();
    //fonts.add(textColor);
    // fonts.add(size);
    // Create an action list containing all color assignments
    // ActionLists are used for actions that will be executed
    // at the same time.  
    ActionList color = new ActionList();
    color.add(fill);
    //color.add(edges);

    // The layout ActionList recalculates 
    // the positions of the nodes.
    ActionList layout = new ActionList();

    MosaicLayout gl = new MosaicLayout("graph.nodes", nrows, 9);

    layout.add(gl);
    layout.add(new MosaicDecoratorLayout("boxlabel"));

    // fisheye distortion based on the current anchor location
        
    ActionList distort = new ActionList();
    dist = new MosaicDistortion(this);
    distort.add(dist);
    distort.add(new RepaintAction());
    vis.putAction("distort", distort);


    // We add a RepaintAction so that every time the layout is 
    // changed, the Visualization updates it's screen.
    layout.add(new RepaintAction());

    // add the actions to the visualization
    vis.putAction("color", color);
    vis.putAction("layout", layout);
    //        vis.putAction("fonts", fonts);
        

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
    MakeMosaic();
  }
  
  @Override
  public void concordanceStateChanged() {
    //stop();
    MakeMosaic();
    //start();

  }
}
