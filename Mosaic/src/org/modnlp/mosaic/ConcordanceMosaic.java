/**
 * (c) 2014 S Sheehan <shane.sheehan@tcd.ie>
 * 2016 S Sheehan, S Luz <luzs@acm.org>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, 59 Temple Place
 * - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.modnlp.mosaic;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ToolTipManager;
import modnlp.tec.client.TecClientRequest;
import modnlp.idx.database.Dictionary;
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
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import prefuse.action.distortion.Distortion;
import prefuse.controls.AnchorUpdateControl;
import prefuse.data.tuple.TupleSet;
import prefuse.render.LabelRenderer;
import java.net.URL;
import java.text.NumberFormat;
import javax.swing.JToggleButton;

/**
 *
 * @author shane
 */
public class ConcordanceMosaic extends JFrame
        implements Runnable, Plugin, StateChanged {

  private static Visualization vis;
  private static int nrows;
  public static final String NAME = "name";
  public static final String NODECOUNT = "nodecount";
  public static final String ROWCOUNT = "colcount";
  public static final int MAXCOLS = 200;
  public static final int GROW = 1;
  public static final int PRUNE = 2;
  private boolean is_rel_freq = false;
  private boolean is_pos_freq = false;
  private Graph graph = null;
  private JFrame thisFrame = null;
  private JProgressBar progressBar;
  private String currentKeyword;
  JPanel tpanel = new JPanel(new BorderLayout());
  
  private static String title = new String("MODNLP Plugin: ConcordanceMosaicViewer 0.3");
  private ConcordanceBrowser parent = null;
  private boolean guiLayoutDone = false;
  private Object[][] sentences;
  private Thread thread;

  private VisualItem selected = null;
  private Color selectedColor = null;
  private Map<Integer, ArrayList<VisualItem>> sentenceIndexToVisualitems = null;
  private TecClientRequest clRequest = new TecClientRequest();
  private TecClientRequest totRequest = new TecClientRequest();
  
  private Distortion dist;
  private HttpURLConnection exturlConnection;
  private HttpURLConnection toturlConnection;
  private Dictionary d = null;
  private BufferedReader input1;
  private BufferedReader input2;
  private int total_no_tokens;
  public List<Double> columnHeigths = new ArrayList<Double>();
  private boolean filterStopwords = false;
  private BufferedReader input;
  private Map<String, Integer> wordCounts = null;
  private String currentCorpusAddress = null;
  
  private int totalHeigth = 450;
  private int totalWidth =98;
  
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
    selected = i;
  }

  public VisualItem getSelected() {
    return selected;
  }

  public String getKeyword() {
    return currentKeyword;
  }
  
  public void setSelectedColor(Color c) {
    selectedColor = c;
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
    
    final JToggleButton frequencyButton = new JToggleButton("Column Frequency");
    final JToggleButton stopwordFrequencyButton = new JToggleButton("Column Frequency (No Stopwords)");
    final JToggleButton relFrequencyButton = new JToggleButton("Collocation Strength (Global)");
    final JToggleButton relFreqPosButton = new JToggleButton("Collocation Strength (Local)");
    
    JPanel pas = new JPanel();
    
    pas.add(frequencyButton);
    pas.add(stopwordFrequencyButton);
    pas.add(relFrequencyButton);
    pas.add(relFreqPosButton);
    frequencyButton.setSelected(true);
    final JFrame window = this;
   this.getRootPane().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Component c =(Component)e.getSource();
                totalWidth = (c.getWidth()-5)/9;
                totalHeigth = c.getHeight()-40;
                if( window.getExtendedState() != JFrame.MAXIMIZED_BOTH){
                    MakeMosaic();
                }
            }
        });
        
 
    
    frequencyButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          is_rel_freq = false;
          is_pos_freq = false;
          relFreqPosButton.setSelected(false);
          frequencyButton.setSelected(!is_rel_freq);
          relFrequencyButton.setSelected(is_rel_freq);
          stopwordFrequencyButton.setSelected(false);
          filterStopwords = false;
          MakeMosaic();
        }
      });
    
    stopwordFrequencyButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          is_rel_freq = false;
          is_pos_freq = false;
          relFreqPosButton.setSelected(false);
          frequencyButton.setSelected(false);
          relFrequencyButton.setSelected(false);
          stopwordFrequencyButton.setSelected(true);
          filterStopwords = true;
          MakeMosaic();
        }
      });
    
    relFrequencyButton.
      addActionListener(new ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            //stop();
            is_rel_freq = true;
            is_pos_freq = false;
            relFreqPosButton.setSelected(false);
            frequencyButton.setSelected(!is_rel_freq);
            relFrequencyButton.setSelected(is_rel_freq);
            stopwordFrequencyButton.setSelected(false);
            MakeMosaic();
          }
        });
    
    relFreqPosButton.
      addActionListener(new ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            //stop();
            is_rel_freq = true;
            is_pos_freq = true;
            relFreqPosButton.setSelected(true);
            frequencyButton.setSelected(false);
            relFrequencyButton.setSelected(false);
            stopwordFrequencyButton.setSelected(false);
            MakeMosaic();
          }
        });
    
    getContentPane().add(pas, BorderLayout.NORTH);
    
    guiLayoutDone = true;
    parent.addChangeListener(this);
    try {
      if (parent.isStandAlone()) {
        d = parent.getDictionary();
        total_no_tokens = d.getTotalNoOfTokens();
      } else {
        clRequest.setServerURL("http://" + parent.getRemoteServer());
        clRequest.setServerPORT(parent.getRemotePort());
        clRequest.put("request", "freqword");
        if (parent.isSubCorpusSelectionON()) {
          clRequest.put("xquerywhere", parent.getXQueryWhere());
        }
        clRequest.put("casesensitive", parent.isCaseSensitive() ? "TRUE" : "FALSE");
        clRequest.setServerProgramPath("/freqword");
        URL exturl = new URL(clRequest.toString());
        exturlConnection = (HttpURLConnection) exturl.openConnection();
        exturlConnection.setRequestMethod("GET"); 
      }
    } catch (IOException e) {
      System.err.println("Exception: couldn't create stream socket" + e);
    }
    currentCorpusAddress = getRemoteCorpusAddress();
    if (!parent.isStandAlone())
      buildFreqHash();
    MakeMosaic();
  }

  private String getRemoteCorpusAddress(){
    return parent.getRemoteServer()+parent.getRemotePort();
  }
  
  private int getTotalNoTokens() {
    int result = 0;
    try {
      if (parent.isStandAlone()) {
        d = parent.getDictionary();
        result = d.getTotalNoOfTokens();
      } else {
        totRequest.setServerURL("http://" + parent.getRemoteServer());
        totRequest.setServerPORT(parent.getRemotePort());
        totRequest.put("request", "nooftokens");
        if (parent.isSubCorpusSelectionON()) {
          totRequest.put("xquerywhere", parent.getXQueryWhere());
        }
        totRequest.put("casesensitive", parent.isCaseSensitive() ? "TRUE" : "FALSE");
        totRequest.setServerProgramPath("/totaltokens");
        URL toturl = new URL(totRequest.toString());
        toturlConnection = (HttpURLConnection) toturl.openConnection();
        toturlConnection.setRequestMethod("GET");
        input1 = new BufferedReader(new InputStreamReader(toturlConnection.getInputStream(), "UTF-8"));
        result = Integer.parseInt(input1.readLine());
        toturlConnection.disconnect();
      }
    } catch (IOException e) {
      System.err.println("Exception: couldn't create stream socket" + e);
    }
    return result;
  }
  
  private int getNoOfTokens(String word) {
    int result = 0;
    
    try {
      if (parent.isStandAlone()) {
        //System.out.println(d);
        //System.out.println(parent);
        d = parent.getDictionary();
        result = d.getFrequency(d.getCaseTable().getAllCases(word));
      } else {
        
        clRequest.setServerURL("http://" + parent.getRemoteServer());
        clRequest.setServerPORT(parent.getRemotePort());
        clRequest.put("request", "freqword");
        clRequest.put("keyword", word);
        if (parent.isSubCorpusSelectionON()) {
          clRequest.put("xquerywhere", parent.getXQueryWhere());
        }
        clRequest.put("casesensitive", parent.isCaseSensitive() ? "TRUE" : "FALSE");
        clRequest.setServerProgramPath("/freqword");
        URL exturl = new URL(clRequest.toString());
        exturlConnection = (HttpURLConnection) exturl.openConnection();
        exturlConnection.setRequestMethod("GET");
        input2 = new BufferedReader(new InputStreamReader(exturlConnection.getInputStream(), "UTF-8"));
        result = Integer.parseInt(input2.readLine());
        exturlConnection.disconnect();
        
      }
    } catch (IOException e) {
      System.err.println("Exception: couldn't create stream socket" + e);
    }
    return result;
  }
  
  private static final String STRING_SEPARATOR_REGEX = "@\\|\\$\\|@";
  
  public String[] concat(String[] a, String[] b) {
    int aLen = a.length;
    int bLen = b.length;
    String[] c = new String[aLen + bLen];
    System.arraycopy(a, 0, c, 0, aLen);
    System.arraycopy(b, 0, c, aLen, bLen);
    return c;
  }
  
  public void MakeMosaic() {
    if (!parent.isStandAlone() &&
        ( wordCounts == null || !currentCorpusAddress.equals(getRemoteCorpusAddress())  ) )
      {
        System.err.println(currentCorpusAddress+"!="+getRemoteCorpusAddress());
        currentCorpusAddress = getRemoteCorpusAddress();
        buildFreqHash();
      }
    if (!parent.isReceivingFromServer()) {
      try {
        selected = null;
        String current = "";
        graph = null;
        graph = new Graph();
        graph.addColumn("word", String.class);
        graph.addColumn("frequency", Double.class);
        graph.addColumn("tooltip", Double.class);
        graph.addColumn("tooltipFreq", Double.class);
        graph.addColumn("tooltipLayoutSwitch", Boolean.class);
        graph.addColumn("rel_freq", Boolean.class);
        graph.addColumn("makeInvis", Boolean.class);
        graph.addColumn("isStopwordView", Boolean.class);
        graph.addColumn("column", Integer.class);
        graph.addColumn("sentences", ArrayList.class);
        graph.addColumn("color", Integer.class);
        graph.addColumn("add1", Integer.class);
        graph.addColumn("height", Integer.class);
        
        sentenceIndexToVisualitems = new HashMap<Integer, ArrayList<VisualItem>>();
        Tokeniser ss;
        int la = parent.getLanguage();
        
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
        double rel_column_length = 0;
        double stopword_column_length = 0;
        for (Iterator<ConcordanceObject> p = parent.getConcordanceVector().iterator(); p.hasNext();) {
          ConcordanceObject co = p.next();
          if (co == null) {
            break;
          }
          Object[] tkns2;
          //left context
          Object[] tkns1 = (ss.split(co.getLeftContext())).toArray();
          //right context
          //tkns2 = (ss.splitWordOnly(co.getKeywordAndRightContext())).toArray();
          // @shane: not quite sure why you used splitWordOnly; reverting to split... 
          tkns2 = (ss.split(co.getKeywordAndRightContext())).toArray();
          Object[] tkns = new Object[9];
          //account for concordances at begining or end of docs
          if (tkns1.length > 4) {
            System.arraycopy(tkns1, (tkns1.length - 4), tkns, 0, 4);
          } else {
            System.arraycopy(tkns1, 0, tkns, 4 - tkns1.length, tkns1.length);
          }
          
          if (tkns2.length > 5) {
            System.arraycopy(tkns2, 0, tkns, 4, 5);
          } else {
            System.arraycopy(tkns2, 0, tkns, 4, tkns2.length);
          }
          sentences[current_sentence] = tkns;
          current_sentence++;
        }
        
        columnHeigths = new ArrayList<Double>();
        //for each column
        for (int i = 0; i < 9; i++) {
          rel_column_length = 0;
          String[] column = new String[nrows];
          //loop throught columns entries
          for (int j = 0; j < nrows; j++) {
            Object[] ls = sentences[j];
            current = (String) ls[i];
            if (current == null) {
              current = "*null*";
            }
            column[j] = current.toLowerCase();
          }
          // create maps required for sorting
          final Map<String, Integer> counter = new HashMap<String, Integer>();
          final Map<String, ArrayList<Integer>> wordToSentenceIndex = new HashMap<String, ArrayList<Integer>>();
          final Map<String, Double> Rel_freq_counter = new HashMap<String, Double>();
          int coli = 0;
          for (String str : column) {
            counter.put(str, 1 + (counter.containsKey(str) ? counter.get(str) : 0));
            ArrayList temp = null;
            if (wordToSentenceIndex.containsKey(str)) {
              temp = wordToSentenceIndex.get(str);
            } else {
              temp = new ArrayList();
            }
            temp.add(coli);
            wordToSentenceIndex.put(str, temp);
            coli++;
          }
          List<String> list1 = new ArrayList<String>(counter.keySet());
          column = list1.toArray(new String[list1.size()]);
          HashMap<String, Integer> hm = new HashMap<String, Integer>();
          //calculate the corpus frequencies of the words in the coulmn
          for (int x = 0; x < column.length; x++) {
            int corpus_word_count = 1;
            if (parent.isStandAlone()) {
              corpus_word_count = getNoOfTokens(column[x]);
            } else {
              //build hash map for stopwords later
              corpus_word_count = getWordCount(column[x]);
            }
            hm.put(column[x], corpus_word_count);
            // Set infrequent words to a very small value
            if (counter.get(column[x]) < 2) {
              Rel_freq_counter.put(column[x], 0.0000000001);
            } else {
              double temp = (((counter.get(column[x]) * 10.0) / nrows) / (((corpus_word_count * 10.0) / total_no_tokens)));
              if (temp < 10000000.0) {
                Rel_freq_counter.put(column[x], temp);
              } else {
                Rel_freq_counter.put(column[x], 0.0000000001);
              }
            }
            rel_column_length += Rel_freq_counter.get(column[x]);
          }
          //custom sorts for reletive frequency and simple frequency visulisation
          if (!is_rel_freq) {
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
          } else {
            Collections.sort(list1, new Comparator<String>() {
                @Override
                public int compare(String x, String y) {
                  Double a = (Rel_freq_counter.get(y) * 100);
                  Double b = (Rel_freq_counter.get(x) * 100);
                  return (a.intValue() - b.intValue());
                }
              });
          }
          column = list1.toArray(new String[list1.size()]);
          stopword_column_length = 0;
          for (int x = 0; x < column.length; x++) {
            Node n = graph.addNode();
            n.set("word", column[x].replace("-", "-\n"));
            n.set("add1", 0);
            n.set("isStopwordView", false);
            n.set("makeInvis", false);
            n.set("height", 0);
            if (is_rel_freq) {
              if (is_pos_freq) {
                double val = Rel_freq_counter.get(column[x]);
                n.set("frequency", val / rel_column_length);
                n.set("tooltip", val);
                n.set("tooltipFreq", (Double) (counter.get(column[x]) * 1.0) / nrows);
                n.set("rel_freq", false);
                n.set("tooltipLayoutSwitch", true);
              } else {
                double val = Rel_freq_counter.get(column[x]);
                n.set("frequency", val);
                n.set("tooltip", val);
                n.set("tooltipFreq", (Double) (counter.get(column[x]) * 1.0) / nrows);
                n.set("rel_freq", true);
                n.set("tooltipLayoutSwitch", true);
              }
              
            } else {
              double val = (Double) (counter.get(column[x]) * 1.0) / nrows;
              if (filterStopwords) {
                n.set("rel_freq", true);
                n.set("tooltipLayoutSwitch", false);
                n.set("tooltipFreq", 10.0);//(Double) (counter.get(column[x]) * 1.0) / nrows);
                Double thresh;
                if (parent.isStandAlone()) {
                  thresh = (getNoOfTokens(column[x]) * 1.0) / total_no_tokens;
                } else {
                  thresh = (hm.get(column[x]) * 1.0) / total_no_tokens;
                }
                
                if (thresh >= 0.0015 && i != 4) {
                  n.set("frequency", 0.0000000001);
                  n.set("tooltip", 0.0000000001);
                  n.set("makeInvis", true);
                } else {
                  
                  stopword_column_length += val;
                  n.set("frequency", val);
                  n.set("tooltip", val);
                }
              } else {
                n.set("frequency", val);
                n.set("rel_freq", false);
                n.set("tooltip", val);
                n.set("tooltipLayoutSwitch", false);
                n.set("tooltipFreq", val);
              }
            }
            n.set("column", i);
            n.set("sentences", wordToSentenceIndex.get(column[x]));
            int color = 0;
            if (i % 2 == 0) {
              color = 2;
            } else {
              color = 4;
            }
            if (x % 2 == 0) {
              color += 1;
            }
            if (i == 4) {
              color = 6;
              currentKeyword = column[x];
            }
            n.set("color", color);
          }
          if (is_rel_freq) {
            columnHeigths.add(rel_column_length);
          } else {
            columnHeigths.add(stopword_column_length);
          }
        }
        if (is_rel_freq) {
          // we need to scale each box relative to a max col heigth
          // and or calculate value for tooltip
          calculateRelFreqHeigths();
        }
        if (!is_rel_freq && filterStopwords) {
          calculateStopwordFreqHeigths();
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
  
  private void makeHashMap() {
    TupleSet ts = vis.getVisualGroup("graph.nodes");
    
    //Iterator iter = ts.tuples();
    for (Iterator iter = ts.tuples(); iter.hasNext();) {
      VisualItem item = (VisualItem) iter.next();
      for (Integer index : (ArrayList<Integer>) item.get("sentences")) {
        ArrayList temp = null;
        if (sentenceIndexToVisualitems.containsKey(index)) {
          temp = sentenceIndexToVisualitems.get(index);
        } else {
          temp = new ArrayList();
        }
        temp.add((VisualItem) item);
        sentenceIndexToVisualitems.put(index, temp);
      }
    }
  }
  
  private void calculateRelFreqHeigths() {
    double keyHeight = columnHeigths.get(4);
    columnHeigths.set(4, 0.0);
    double maxH = Collections.max(columnHeigths);
    
    for (Iterator iter = graph.nodes(); iter.hasNext();) {
      Node item = (Node) iter.next();
      //String word = (String)item.get("word");
      double value = (Double) item.get("tooltip");
      //scaled to be proportional to each word not column
      value = (value) / (maxH);
      //if local view
      if (!is_pos_freq) {
        if ((Integer) item.get("column") != 4) {
          item.set("frequency", value);
        } else {
          item.set("frequency", (Double) item.get("tooltip") / keyHeight);
        }
      }
      item.set("tooltip", value);
    }
  }
  
  private void calculateStopwordFreqHeigths() {
    columnHeigths.set(4, 0.0);
    double maxH = Collections.max(columnHeigths);
    for (Iterator iter = graph.nodes(); iter.hasNext();) {
      Node item = (Node) iter.next();
      double value = ((Double) item.get("tooltip"));
      value = ((value) / (maxH));
      if ((Integer) item.get("column") != 4) {
        item.set("frequency", value);
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
    d.setSize((totalWidth*9)+5, totalHeigth+5); //885,450 use 500 to see extra
    
    //d.addControlListener(new DragControl());
    d.addControlListener(new PanControl(true));
    d.addControlListener(new ZoomControl());
    d.addControlListener(new WheelZoomControl());
    d.addControlListener(new MosaicTooltip(parent));
    ToolTipManager.sharedInstance().setInitialDelay(650);
    ToolTipManager.sharedInstance().setReshowDelay(650);
    ToolTipManager.sharedInstance().setDismissDelay(1700);
    d.addControlListener(new HoverTooltip(parent, vis, this));
    d.addControlListener(new AnchorUpdateControl(dist, "distort"));
    tpanel.add(d, BorderLayout.CENTER);
    pack();
    setVisible(true);
    vis.run("color");
    vis.run("layout");
  }
  
  public void setUpRenderers() {
    MosaicRenderer r = new MosaicRenderer(nrows, totalHeigth);
    r.width = totalWidth;
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
    DataColorAction fill = null;
    if (!is_rel_freq) {
      fill = new DataColorAction("graph.nodes", "color",
                                 Constants.NOMINAL,
                                 VisualItem.FILLCOLOR,
                                 new int[]{ColorLib.color(new java.awt.Color(181, 222, 223)),
                                           ColorLib.color(new java.awt.Color(88, 170, 143)),
                                           ColorLib.color(new java.awt.Color(148, 222, 196)),
                                           ColorLib.color(new java.awt.Color(194, 228, 216)),
                                           ColorLib.color(new java.awt.Color(125, 232, 212))
                                 });
    } else {
      fill = new DataColorAction("graph.nodes", "color",
                                 Constants.NOMINAL,
                                 VisualItem.FILLCOLOR,
                                 new int[]{ColorLib.color(new java.awt.Color(255, 186, 69)),
                                           ColorLib.color(new java.awt.Color(225, 204, 164)),
                                           ColorLib.color(new java.awt.Color(225, 232, 212)),
                                           ColorLib.color(new java.awt.Color(255, 198, 92)),
                                           ColorLib.color(new java.awt.Color(210, 143, 91))
                                 });

    }
    // Create an action list containing all color assignments
    // ActionLists are used for actions that will be executed
    // at the same time.  
    ActionList color = new ActionList();
    color.add(fill);

    // The layout ActionList recalculates 
    // the positions of the nodes.
    ActionList layout = new ActionList();
    MosaicLayout gl = new MosaicLayout("graph.nodes", nrows, 9);
    gl.width = totalWidth;
    gl.heigth=totalHeigth;
    
    layout.add(gl);
    layout.add(new MosaicDecoratorLayout("boxlabel"));

    // fisheye distortion based on the current anchor location    
    ActionList distort = new ActionList();
    dist = new MosaicDistortion(this,totalWidth,totalHeigth);
    distort.add(dist);
    distort.add(new RepaintAction());
    vis.putAction("distort", distort);

    // We add a RepaintAction so that every time the layout is 
    // changed, the Visualization updates it's screen.
    layout.add(new RepaintAction());

    // add the actions to the visualization
    vis.putAction("color", color);
    vis.putAction("layout", layout);
  }

  public void start() {
    if (thread == null) {
      thread = new Thread(this);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
    downloadFreqList();
  }

  public void stop() {
    if (thread != null) {
      thread.stop();
      thread = null;
    }
  }

  public void run() {
    if (!parent.isStandAlone())
      buildFreqHash();
    MakeMosaic();
  }

  @Override
  public void concordanceStateChanged() {
    //stop();
    MakeMosaic();
    //start();

  }

  private void downloadFreqList() {
    input = null;
    System.err.println("Entered download of fqlist");
    try {
      if (parent.isStandAlone()) {

      } else {
        TecClientRequest rq = new TecClientRequest();
        rq.setServerURL("http://" + parent.getRemoteServer());
        rq.setServerPORT(parent.getRemotePort());
        rq.put("request", "freqlist");
        rq.put("skipfirst", "" + 0);
        rq.put("maxlistsize", "-1"); // print freq up to the first hapax
        // sl: perhaps we should always download the whole fq list
        // I'm commenting this out 
        //if (parent.isSubCorpusSelectionON()) {
        //rq.put("xquerywhere", parent.getXQueryWhere());
        //}
        rq.put("casesensitive", parent.isCaseSensitive() ? "TRUE" : "FALSE");
        rq.setServerProgramPath("/freqlist");
        URL exturl = new URL(rq.toString());
        exturlConnection = (HttpURLConnection) exturl.openConnection();
        exturlConnection.setRequestMethod("GET");
        //System.err.println("Setting up download of fqlist"+exturl);
      }
    } catch (IOException e) {
      System.err.println("Exception: couldn't create stream socket" + e);
      JOptionPane.showMessageDialog(null, "Couldn't get frequency list: " + e);
    }
  }

  class FqlPrinter extends Thread {

    public FqlPrinter() {
      //super("Frequency list producer");
    }

    public void run() {
      try {
        if (!parent.isStandAlone()) {
          //System.err.println("Connecting to: " + exturlConnection);
          input = new BufferedReader(new InputStreamReader(exturlConnection.getInputStream(),
                                                           "UTF-8"));
        }
      } catch (Exception e) {
        System.err.println("FqlPrinter: " + e);
        e.printStackTrace();
      }
    }
  }

  public void buildFreqHash() {
    String textLine = "";
    StringBuffer cstats = new StringBuffer();
    try {
      wordCounts = new HashMap<String, Integer>();
      int dldCount = 0;
      int maxListSize = 0;
      int notokens = 0;
      // downloadFreqList is a misnomer, as it only sets up the URL
      downloadFreqList();
      (new ConcordanceMosaic.FqlPrinter()).start();

      NumberFormat nf = NumberFormat.getInstance();
        
      while (input == null) {
        thread.sleep(100);
      }
      String[] row = null;
      while ((maxListSize == 0 || dldCount <= maxListSize)
             && (textLine = input.readLine()) != null) {
        if (textLine.equals("")) {
          continue;
        }
          
        row = textLine.split(modnlp.Constants.LINE_ITEM_SEP);
        if (row[0].equals("0")) {
          if (row[1].equals(modnlp.idx.database.Dictionary.TTOKENS_LABEL)) {
            notokens = (new Integer(row[2])).intValue();
            total_no_tokens = notokens;
          }
        } else {
          wordCounts.put(row[1].toString(), (new Integer(row[2])).intValue());
        }
      }
      System.err.println("stopped at "+row[1].toString()+"=="+(new Integer(row[2])).intValue());
      //System.err.println("read thread finished");
    } catch (Exception e) {
      System.err.println(cstats.toString());
      System.err.println("Exception: " + e);
      System.err.println("Line: |" + textLine + "|");
      e.printStackTrace();
    }
  }
 
  private int getWordCount(String word){
    int wc = wordCounts.getOrDefault(word, -1);    
    // wc can never be 0, since the word appears in the concordance list!
    if (wc == -1){
      //System.err.println("Mosaic: count for "+word+" not found");
      return 1;
    }
    return wc;
  }
  
}


