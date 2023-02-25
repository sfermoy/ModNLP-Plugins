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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
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
import modnlp.gui.*;
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
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.BoxLayout;
import java.awt.GridBagLayout;
import java.awt.Font;
import java.util.Arrays;

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

  public static final ArrayList<String> stopwords = new ArrayList<>(Arrays.asList("i","me","my","myself","we","our","ours","ourselves","you","your","yours","yourself","yourselves","he","him","his","himself","she","her","hers","herself","it","its","itself","they","them","their","theirs","themselves","what","which","who","whom","this","that","these","those","am","is","are","was","were","be","been","being","have","has","had","having","do","does","did","doing","a","an","the","and","but","if","or","because","as","until","while","of","at","by","for","with","about","against","between","into","through","during","before","after","above","below","to","from","up","down","in","out","on","off","over","under","again","further","then","once","here","there","when","where","why","how","all","any","both","each","few","more","most","other","some","such","no","nor","not","only","own","same","so","than","too","very","s","t","can","will","just","don","should","now"));
  public static final ArrayList<String> omcstopwords =
    new ArrayList<>(Arrays.asList("the",
                                  "and",
                                  "of",
                                  "to",
                                  "in",
                                  "a",
                                  "for",
                                  "with",
                                  "that ",
                                  "is",
                                  "on",
                                  "are",
                                  "as",
                                  "be",
                                  "by",
                                  "from",
                                  "have",
                                  "or",
                                  "this",
                                  "at",
                                  "s",
                                  "an",
                                  "their",
                                  "it",
                                  "was",
                                  "has",
                                  "more",
                                  "were",
                                  "all",
                                  "they",
                                  "which",
                                  "other",
                                  "these",
                                  "been",
                                  "can",
                                  "also",
                                  "among",
                                  "should",
                                  "such",
                                  "will",
                                  "than",
                                  "there",
                                  "but",
                                  "one",
                                  "including",
                                  "may",
                                  "had",
                                  "between",
                                  "about",
                                  "et",
                                  "al",
                                  "through",
                                  "its"));

  private JCheckBox stopWordsCheck = new JCheckBox("Stopwords");
  
  // NO LONGER USED
  //private int min_count = 0;
  //private int max_count = 100;
  // 
  private double minFreq = 0;
  private double maxFreq = 100;
  // range slider
  
  private JPanel rangeSliderPanel = new JPanel(new GridBagLayout());
  //rangeSliderPanel.setLayout(new GridBagLayout());
  private JLabel rangeSliderValue1 = new JLabel();
  private JLabel rangeSliderValue2 = new JLabel();
  private final ExpRangeSlider rangeSlider = new ExpRangeSlider();
  
  //private final JSlider min_count_slider =
  //  new JSlider(JSlider.HORIZONTAL, 0,40,1);
  //private final JSlider max_count_slider =
  //  new JSlider(JSlider.HORIZONTAL, 0,100,1);
  private boolean is_rel_freq = false;
  private boolean is_pos_freq = false;
  private Graph graph = null;
  private JFrame thisFrame = null;
  private JProgressBar progressBar;
  private String currentKeyword;
  JPanel tpanel = new JPanel(new BorderLayout());
  
  
  private static String title = new String("MODNLP Plugin: ConcordanceMosaicViewer 0.4");
  private ConcordanceBrowser parent = null;
  private boolean guiLayoutDone = false;
  private Object[][] sentences;
  private Thread thread;

  private VisualItem selected = null;
  private Color selectedColor = null;
  private Map<Integer, ArrayList<VisualItem>> sentenceIndexToVisualitems = null;
  private Map<String, ArrayList<VisualItem>> wordToVisualitems = null;
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
   public List<Integer> columnNumItems = new ArrayList<Integer>();
   public List<Integer> numStopwordsRemoved = new ArrayList<Integer>();
  private boolean sliderFilter = true;
   private boolean filterStopwords = false;
  private BufferedReader input;
  private Map<String, Integer> wordCounts = null;
  private String currentCorpusAddress = null;
  
  private int totalHeigth = 450;
  private int totalWidth = 98;
  private String[] metricStrings = { "Z-score", "Log-Log", "MI (EXP scale)", "MI3 (EXP scale)"};
  private JComboBox metricList = new JComboBox(metricStrings);
  
  private boolean max_changed = false;
  
  private Display display;
  
  public ConcordanceMosaic() {
    thisFrame = this;
  }

  public ArrayList<VisualItem> getVisualItemsInSentence(Integer i) {
    return sentenceIndexToVisualitems.get(i);
  }
  
   public ArrayList<VisualItem> getVisualItemsOfWord(String w) {
    return wordToVisualitems.get(w);
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
    
    final JToggleButton frequencyButton =
      new JToggleButton("Column Frequency");
    final JToggleButton relFrequencyButton =
      new JToggleButton("Collocation (Global)");
    final JToggleButton relFreqPosButton =
      new JToggleButton("Collocation (Local)");

    // range slider settings
    rangeSliderValue1.setHorizontalAlignment(JLabel.LEFT);
    rangeSliderValue2.setHorizontalAlignment(JLabel.LEFT);        
    rangeSlider.setPreferredSize(new Dimension(220,
                                               rangeSlider.getPreferredSize().height));
    rangeSlider.setMinimum(0);
    rangeSlider.setMaximum(100);
    
    rangeSlider.setValue(0);
    rangeSlider.setUpperValue(100);
        
    // Initialize value display.
    rangeSliderValue1.setText("0");
    rangeSliderValue2.setText(rangeSlider.getExpValueString(rangeSlider.getExpUpperValue()));
    final JLabel rangeSliderTitle = new JLabel("Frequency range", JLabel.CENTER);

    final JPanel pas = new JPanel();

    rangeSliderPanel.add(rangeSliderValue1,
        new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                               GridBagConstraints.NORTHWEST,
                               GridBagConstraints.NONE,
                               new Insets(0, 0, 3, 0), 0, 0));
    rangeSliderPanel.add(rangeSliderTitle,
        new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                               GridBagConstraints.NORTH,
                               GridBagConstraints.NONE,
                               new Insets(0, 0, 6, 0), 0, 0));
   rangeSliderPanel.add(rangeSliderValue2,
        new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                               GridBagConstraints.NORTHEAST,
                               GridBagConstraints.NONE,
                               new Insets(0, 0, 6, 0), 0, 0));
    rangeSliderPanel.add(rangeSlider      ,
        new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
                               GridBagConstraints.NORTHWEST,
                               GridBagConstraints.NONE,
                               new Insets(0, 0, 0, 0), 0, 0));
    
    
    pas.add(rangeSliderPanel);
    pas.add(stopWordsCheck);
    pas.add(frequencyButton);
    pas.add(relFrequencyButton);
    pas.add(relFreqPosButton);
    frequencyButton.setSelected(true);
    final JFrame window = this;
    
    // metricList.setSelectedIndex(0);

    this.getRootPane().addComponentListener(new ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
          Component c =(Component)e.getSource();
          totalWidth = (c.getWidth()-5)/9;
          if(frequencyButton.isSelected()){
            totalHeigth = c.getHeight()-116;
          }
          else{
          totalHeigth = c.getHeight()-36;
          }
          //                if( window.getExtendedState() != JFrame.MAXIMIZED_BOTH){
          //                    makeMosaic();
          //                }
        }
      });
    

    stopWordsCheck.setSelected(true);
    stopWordsCheck.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if(stopWordsCheck.isSelected()){
            filterStopwords = false;
          }
          else {
            filterStopwords = true;
          }
          makeMosaic();
        }});
    
    rangeSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          ExpRangeSlider slider = (ExpRangeSlider) e.getSource();
          minFreq = slider.getExpValue();
          maxFreq = slider.getExpUpperValue();
          rangeSliderValue1.setText(slider.getExpValueString(minFreq));
          rangeSliderValue2.setText(slider.getExpValueString(maxFreq));
                      if(frequencyButton.isSelected() && ( minFreq >0 || max_changed)){
                is_rel_freq = false;
                is_pos_freq = false;
                sliderFilter = true;
                pas.remove(metricList);
                makeMosaic();  
            }
            else{
                is_rel_freq = false;
                is_pos_freq = false;
                sliderFilter = false;
                pas.remove(metricList);
                makeMosaic();
            }
        }
      });


    metricList.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          makeMosaic();
        }
      });

    frequencyButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          is_rel_freq = false;
          is_pos_freq = false;
          filterStopwords = false;
          relFreqPosButton.setSelected(false);
          frequencyButton.setSelected(!is_rel_freq);
          relFrequencyButton.setSelected(is_rel_freq);
          sliderFilter = true;
          stopWordsCheck.setEnabled(true);
          rangeSlider.setEnabled(true);
          rangeSliderValue1.setEnabled(true);
          rangeSliderValue2.setEnabled(true);
          rangeSliderTitle.setEnabled(true);
          pas.remove(metricList);
          makeMosaic();
        }
      });
    
    
    relFrequencyButton.
      addActionListener(new ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            //stop();
            is_rel_freq = true;
            is_pos_freq = false;
            filterStopwords = false;
            relFreqPosButton.setSelected(false);
            frequencyButton.setSelected(!is_rel_freq);
            relFrequencyButton.setSelected(is_rel_freq);
//            stopwordFrequencyButton.setSelected(false);
            sliderFilter = false;
            stopWordsCheck.setEnabled(false);
            rangeSlider.setEnabled(false);
            rangeSliderValue1.setEnabled(false);
            rangeSliderValue2.setEnabled(false);
            rangeSliderTitle.setEnabled(false);
            pas.add(metricList);
            makeMosaic();
          }
        });
    
    relFreqPosButton.
      addActionListener(new ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            //stop();
            is_rel_freq = true;
            is_pos_freq = true;
            filterStopwords = false;
            relFreqPosButton.setSelected(true);
            frequencyButton.setSelected(false);
            relFrequencyButton.setSelected(false);
//            stopwordFrequencyButton.setSelected(false);
            sliderFilter = false;
            stopWordsCheck.setEnabled(false);
            rangeSlider.setEnabled(false);
            rangeSliderValue1.setEnabled(false);
            rangeSliderValue2.setEnabled(false);
            rangeSliderTitle.setEnabled(false);
            pas.add(metricList);
            makeMosaic();
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
        clRequest.setServerURL(parent.getRemoteWebcli());
        //clRequest.setServerPORT(parent.getRemotePort());
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
    makeMosaic();
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
        totRequest.setServerURL(parent.getRemoteWebcli());
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
  
  public void makeMosaic() {
    max_changed = false;  
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
        graph.addColumn("count", Integer.class);
        graph.addColumn("metric", String.class);

        
        sentenceIndexToVisualitems = new HashMap<Integer, ArrayList<VisualItem>>();
        wordToVisualitems = new HashMap<String, ArrayList<VisualItem>>();
        
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
          String left_context = co.getLeftContext();
          left_context = left_context.replaceAll("http.*?\\s", "");
          left_context = left_context.replaceAll("https.*?\\s", "");
          Object[] tkns1 = (ss.split(left_context)).toArray();
          
          //right context
          //tkns2 = (ss.splitWordOnly(co.getKeywordAndRightContext())).toArray();
          // @shane: not quite sure why you used splitWordOnly; reverting to split..
          
          String right_context = co.getKeywordAndRightContext();
          right_context = right_context.replaceAll("http.*?\\s", "");
          right_context = right_context.replaceAll("https.*?\\s", "");
          tkns2 = (ss.split(right_context)).toArray();
          Object[] tkns = new Object[9];
          
          //Check for URL at this point so that mosaic position counts are not affected
//          Object[] tkns1copy = tkns1.clone();
//            for (int i = 0; i < tkns1.length; i++) {
//                String token = (String)tkns1[i];
//                if token
//                
//            }
          
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
          //reverse token order for arrabic
          if(parent.getLanguage() == modnlp.Constants.LANG_AR){
            // System.out.println("dsaddsdasdsdsadsadsadsacacsdcwdcwd");
            for(int i = 0; i < tkns.length / 2; i++)
            {
                Object temp = tkns[i];
                tkns[i] = tkns[tkns.length - i - 1];
                tkns[tkns.length - i - 1] = temp;
            }
            sentences[current_sentence] = tkns;
          }else{
            sentences[current_sentence] = tkns;
          }
          
          current_sentence++;
        }
        
        columnHeigths = new ArrayList<Double>();
        columnNumItems = new ArrayList<Integer>();
        numStopwordsRemoved = new ArrayList<Integer>();
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
            if ( column[x].equalsIgnoreCase("*null*")) {
            //if (  column[x].equalsIgnoreCase("*null*")) {
              Rel_freq_counter.put(column[x], 0.0000000001);
            } else {
              if( sliderFilter ){
                  Rel_freq_counter.put(column[x], (double) counter.get(column[x]));
              }
              else{
                double temp = (((counter.get(column[x]) * 10.0) / nrows) / (((corpus_word_count * 10.0) / total_no_tokens)));
                
                if(true){
                  //Values for metric calculation
                  double N =(total_no_tokens*1.0);
                  double Fn = ( (nrows*1.0) );
                  double Fnc = counter.get(column[x]);
                  double Fc = (corpus_word_count*1.0);
                  
                  // Z score calculation
                  double p = Fc / ( N - Fn );
                  double E = p * Fn;
                  //Yeats correction
                  double zFnc = Fnc - 0.5;
                  if(Fnc <E)
                    zFnc =  Fnc + 0.5;
                  double Z =  (zFnc -E) / Math.sqrt(E*(1-p));
                  //default
                  temp =Z;
                  
                  //Mutual information cubed calculation
                  //Not taking log as a visua scaling choice. list order is still maintained
                  double intermediate = (Math.pow(Fnc, 3)*N) / ( (Fnc + Fn)*( Fnc + Fc) );
                  //double Mi3 = intermediate)/Math.log(2);
                  double Mi3 = intermediate;
                  
                  //Mutual information calculation
                  //Not taking log as a visua scaling choice. list order is still maintained
                  intermediate = (Fnc*N)/(Fn*Fc);
                  //double Mi =  Math.log(intermediate)/Math.log(2);
                  double Mi =  intermediate;
                  
                  // Observed expected metric calculation Visually the same as EXP(Mi)
                  double ObservedExpected = (Fnc* (N-Fn))/ (Fn*Fc);
                  
                  //Kilgarif log-log score calculation
                  double val1 = (Fnc*N) /(Fn*Fc);
                  double A = Math.log(val1)/Math.log(2);
                  double val2 = (Fnc);
                  double B = Math.log(val2)/Math.log(2);
                  double loglog = A*B;
                  
                  //log-likleyhood
                  //                    double S = (Fnc + Math.log(Fnc))+(Fn + Math.log(Fn))+(Fc + Math.log(Fc))+(N + Math.log(N));
                  //                    double D = ( (Fnc +Fn)* Math.log(Fnc+Fn) )+
                  //                               ( (Fnc +Fc)* Math.log(Fnc+Fc) )+
                  //                               ( (Fn + N)* Math.log(Fn + N) )+
                  //                               ( (Fc + N)* Math.log(Fc + N) );
                  //                    double A = (Fnc +Fn +Fn +N)* Math.log(D)
                  
                  
                  //"Select metric
                  String metric =(String) metricList.getSelectedItem();
                  //System.out.println(metric);
                  if(metric.equals("Modified MI")){
                    temp = (((counter.get(column[x]) * 10.0) / nrows) / (((corpus_word_count * 10.0) / total_no_tokens)));
                  }
                  if(metric.equals("MI (EXP scale)"))
                    {
                      temp=Mi;
                    }
                  if(metric.equals("MI3 (EXP scale)"))
                    {
                      temp=Mi3;
                    }
                  if(metric.equals("Z-score"))
                    {
                      temp=Z;
                    }
                  if(metric.equals("Log-Log"))
                    {
                      temp=loglog;
                    }
                  //                    if(metric.equals("Obs/Exp"))
                  //                    {
                  //                        temp= ObservedExpected;
                  //                    }
                  
                  //    if(temp <0)
                  //        System.out.println(column[x]+" "+ temp);
                  
                }
                
                if (temp < 1000000000000.0 ) {
                  Rel_freq_counter.put(column[x], temp);
                } else {
                  Rel_freq_counter.put(column[x], 0.0000000001);
                }
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
          int num_items =0;
          int stopwordsRemoved =0;
          for (int x = 0; x < column.length; x++) {
            Node n = graph.addNode();
            n.set("word", column[x].replace("-", "-\n"));
            n.set("add1", 0);
            n.set("isStopwordView", false);
            n.set("makeInvis", false);
            n.set("height", 0);
            n.set("count", counter.get(column[x]));
            n.set("metric", (String) metricList.getSelectedItem());
            
            
            if (is_rel_freq) {
              if (is_pos_freq) {
                double val = Rel_freq_counter.get(column[x]);
                double freqie = (counter.get(column[x]) * 0.01) / nrows;
                //System.out.println(column[x]+  "  "+val);
                
                n.set("rel_freq", false);
                n.set("tooltipLayoutSwitch", true);
                n.set("frequency", val / rel_column_length);
                n.set("tooltip", val);
                n.set("tooltipFreq", (Double) (counter.get(column[x]) * 1.0) / nrows);
                
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
              if (sliderFilter) {
                n.set("rel_freq", true);
                n.set("tooltipLayoutSwitch", false);
                n.set("tooltipFreq", 10.0);//(Double) (counter.get(column[x]) * 1.0) / nrows);
                
                Double min_frequency = minFreq/100;//min_count*.01;
                Double max_frequency = maxFreq/100;//max_count *.01; 
                if (min_frequency >= max_frequency){
                  n.set("frequency", 0.0000000001);
                  n.set("tooltip", 0.0000000001);
                  n.set("makeInvis", true);
                }else{
                  if (val >=  max_frequency && i != 4) { //threshold
                    n.set("frequency", 0.0000000001);
                    n.set("tooltip", 0.0000000001);
                    n.set("makeInvis", true);
                    max_changed = true;
                    //System.out.println("max_changed");
                  }
                  else {
                    if(val < min_frequency ){
                      n.set("frequency", 0.0000000001);
                      n.set("tooltip", 0.0000000001);
                      n.set("makeInvis", true);
                    }
                    else{
                      if(filterStopwords){
                        if(stopwords.contains(column[x])){
                          n.set("frequency", 0.0000000001);
                          n.set("tooltip", 0.0000000001);
                          n.set("makeInvis", true);
                          //stopwordsRemoved+=1;
                        }else{
                          stopword_column_length += val;
                          n.set("frequency", val);
                          n.set("tooltip", val);
                          num_items+=1;
                        }
                      }else{
                        stopword_column_length += val;
                        n.set("frequency", val);
                        n.set("tooltip", val);
                        num_items+=1;
                      }
                    }
                  }
                }
              } else { // if ! stopwords
                n.set("frequency", val);
//                 if (i == 4) {
//                    n.set("frequency", 1/(double)column.length);
//                 }
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
             // n.set("frequency", 1/(double)column.length);
            }
            n.set("color", color);
          }
          if (is_rel_freq) {
            columnHeigths.add(rel_column_length);
          } else {
            columnHeigths.add(stopword_column_length);
            columnNumItems.add(num_items);
            numStopwordsRemoved.add(stopwordsRemoved);
          }
        }
        if (is_rel_freq) {
          // we need to scale each box relative to a max col heigth
          // and or calculate value for tooltip
          calculateRelFreqHeigths();
        }
        if (!is_rel_freq && sliderFilter) {
          calculateStopwordFreqHeigths();
        }
        setDisplay();
        //add Visual item to hashmap with index of sentence numbers
        makeHashMap();
        makeWordHashMap();
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
  private void makeWordHashMap() {
    TupleSet ts = vis.getVisualGroup("graph.nodes");
    
    //Iterator iter = ts.tuples();
    for (Iterator iter = ts.tuples(); iter.hasNext();) {
      VisualItem item = (VisualItem) iter.next();
      String word = ((String) item.get("word"));
      ArrayList temp;
      if (wordToVisualitems.containsKey(word)) {
          temp = wordToVisualitems.get(word);
        } else {
          temp = new ArrayList();
        }
        temp.add((VisualItem) item);
        wordToVisualitems.put(word, temp);
      
    
    }
  }
  
  private void calculateRelFreqHeigths() {
    double keyHeight = columnHeigths.get(4);
    columnHeigths.set(4, 0.0);
    double maxH = Collections.max(columnHeigths);
    
    for (Iterator iter = graph.nodes(); iter.hasNext();) {
      Node item = (Node) iter.next();
      //String word = (String)item.get("word");
      double value = (Double) item.get("frequency");
      //scaled to be proportional to each word not column
      value = (value) / (maxH);
      //if local view
      if (!is_pos_freq) {
        if ((Integer) item.get("column") != 4) {
          item.set("frequency", value);
        } else {
          item.set("frequency", (Double) item.get("frequency") / keyHeight);
        }
      }
      //item.set("tooltip", value);
    }
  }
  
  private void calculateStopwordFreqHeigths() {
    //columnHeigths.set(4, 0.0);
    //use this for absolute frequency unstreched
    double maxH = Collections.max(columnHeigths);
    int curcol = -1;
    double totaL =0.0;
    for (Iterator iter = graph.nodes(); iter.hasNext();) {
      Node item = (Node) iter.next();
      int col = (Integer) item.get("column");
      if(col>curcol){
          curcol = col;
      }
          
      //total heigth of column comment out and add line aboue for abs freq
      double current = columnHeigths.get(col);
      double value = ((Double) item.get("tooltip"));
      
      value = (value *((maxH*1.0)/current));// 1/maxH to do positional encoding
      totaL +=value;
      if (col != 4) {
        item.set("frequency", value);
        item.set("isStopwordView", true);
        item.set("height", columnNumItems.get(curcol)- numStopwordsRemoved.get(curcol));
//        item.set("rel_freq", false);
      }
    }
  }
  
  private void setDisplay() {
    tpanel.removeAll();
    vis = new Visualization();
    vis.add("graph", graph);
    setUpActions();
    setUpRenderers();
    display = new Display(vis);
    display.setSize((totalWidth*9)+5, totalHeigth); //885,450 use 500 to see extra
    
    //d.addControlListener(new DragControl());
    display.addControlListener(new PanControl(true));
    display.addControlListener(new ZoomControl());
    display.addControlListener(new WheelZoomControl());
    display.addControlListener(new MosaicTooltip(parent));
    ToolTipManager.sharedInstance().setInitialDelay(650);
    ToolTipManager.sharedInstance().setReshowDelay(650);
    ToolTipManager.sharedInstance().setDismissDelay(1700);
    display.addControlListener(new HoverTooltip(parent, vis, this));
    display.addControlListener(new AnchorUpdateControl(dist, "distort"));
    tpanel.add(display, BorderLayout.CENTER);
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
    makeMosaic();
  }

  @Override
  public void concordanceStateChanged() {
    //stop();
    makeMosaic();
    //start();

  }

  private void downloadFreqList() {
    input = null;
    System.err.println("Entered download of fqlist");
    try {
      if (parent.isStandAlone()) {

      } else {
        TecClientRequest rq = new TecClientRequest();
        rq.setServerURL(parent.getRemoteWebcli());
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
        if(row.length == 1)
            continue;
        if("|null|".equals(textLine))
            continue;
        if (row[0].equals("0")) {
          if (row[1].equals(modnlp.idx.database.Dictionary.TTOKENS_LABEL)) {
            notokens = (new Integer(row[2])).intValue();
            total_no_tokens = notokens;             
          }
         
        } else {
          wordCounts.put(row[1].toString(), (new Integer(row[2])).intValue());
        }
      }
      //System.err.println("stopped at "+row[1].toString()+"=="+(new Integer(row[2])).intValue() + " ttratio ");
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


