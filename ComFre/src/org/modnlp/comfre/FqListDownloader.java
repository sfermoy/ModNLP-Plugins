/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modnlp.comfre;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modnlp.Constants;
import modnlp.idx.database.Dictionary;
import modnlp.idx.headers.HeaderDBManager;
import modnlp.tec.client.ConcordanceBrowser;
import modnlp.tec.client.TecClientRequest;

/**
 *
 * @author shane
 */
public class FqListDownloader implements Runnable{
  
  private Thread ftwThread;
  private ConcordanceBrowser parent = null;
  private BufferedReader input;
  private PrintWriter fqlout = null;
  private HttpURLConnection exturlConnection;
  private int skipFirst = 0;      // print from the startItem^{th} most frequent word
  private int maxListSize = 0;   // print at most maxListSize items (0 means print until the last item)
  private int dldCount = 0;
  private double ttratio = 0;
  private int notokens = 0;
  private int dldi = 0;
  private DefaultTableModel model ;
  private DefaultTableModel noCaseModel = null; 
  private String dirName = System.getProperty("user.home")+ File.separator+"GOKCache";
  private String xqueryStr;
  FqlPrinter fl;
  
  
  public FqListDownloader(Object p, String query){
    parent = (ConcordanceBrowser)p;
    model = new DefaultTableModel();
    model.addColumn("Rank");
    model.addColumn("Type");
    model.addColumn("Frequency");
    model.addColumn("% total");
    dirName += File.separator+"ComFreCache"; 
    xqueryStr = query;    
    start();
  }
    @Override
    public void run() {
    String textLine = "";
    StringBuffer cstats = new StringBuffer();
    try {
      int i= 0;
      dldCount = 0;
      int rank = 1;
      int ttok = 0;
      //if (parent.isStandAlone()) {
      fl =  (new FqListDownloader.FqlPrinter());
      fl.start();
        //}

      
      NumberFormat nf =  NumberFormat.getInstance(); 
      //new java.text.DecimalFormat("###,###,###,###.#####");
      //NumberFormat pf =  NumberFormat.getIntegerInstance(); 
      // new java.text.DecimalFormat("###.###");
      //System.err.println("-----starting");

      while (input==null){
        ftwThread.sleep(100);
      }
      while ((maxListSize == 0 || dldCount <= maxListSize) && 
             (textLine = input.readLine()) != null 
             ) 
        {
          if (textLine.equals(""))
            continue;
          
            String [] row = textLine.split(Constants.LINE_ITEM_SEP);
            if (row[0].equals("0")) {
              ttratio = (new Double(row[2].toString())).doubleValue();
              cstats.append(row[1]+": "+nf.format(ttratio)+";  ");
              if (row[1].equals(modnlp.idx.database.Dictionary.TTOKENS_LABEL)){
                notokens = (new Integer(row[2])).intValue();

              }
            }
            else {
              Object [] orow = new Object[4]; 
              for (int j = 0; j < row.length; j++)
                orow[j] = row[j];
              if((new Integer(row[2])).intValue()<1)
                  continue;
              orow[3] = new Float((float)(new Integer(row[2])).intValue()/notokens);
              model.addRow(orow);
            }
        }

      saveCSV();
      stop();
      System.err.println("Fq list downloaded");
      notifyListeners();
      
    }
    catch (Exception e)
      {
        System.err.println("Exception: " + e);
        System.err.println("Line: |" + textLine+"|");
        e.printStackTrace();
      }
  }

  public void start() {
    stop();
    input = null;
    try {
      if (parent.isStandAlone()) {
        PipedWriter pipeOut = new PipedWriter();
        input = new BufferedReader(new PipedReader(pipeOut));
        fqlout = new PrintWriter(pipeOut); 
      }
      else {
        TecClientRequest rq = new TecClientRequest();
        rq.setServerURL("http://"+parent.getRemoteServer());
        rq.setServerPORT(parent.getRemotePort());
        rq.put("request","freqlist");
        //if (parent.isSubCorpusSelectionON()){
        rq.put("skipfirst",""+skipFirst);
        rq.put("maxlistsize",""+maxListSize);
        if (parent.isSubCorpusSelectionON())
          rq.put("xquerywhere",xqueryStr); 
        rq.put("casesensitive",parent.isCaseSensitive()?"TRUE":"FALSE");
        //}
        rq.setServerProgramPath("/freqlist");
        URL exturl = new URL(rq.toString());
        exturlConnection = (HttpURLConnection) exturl.openConnection();
        //exturlConnection.setUseCaches(false);
        exturlConnection.setRequestMethod("GET");
        //System.err.println("--input set---");
      }
    }
    catch(IOException e)
      {
        System.err.println("Exception: couldn't create stream socket"+e);
        JOptionPane.showMessageDialog(null, "Couldn't get frequency list: "+e);      
      }
    

  }
  
    public void stop() {
    if ( ftwThread != null ){
      //ftwThread.stop();
      if (!parent.isStandAlone() && exturlConnection != null){
         exturlConnection.disconnect();
      }
      ftwThread = null;
    }
  }

 public void saveCSV(){
     try{
        String filename = dirName+File.separator +"file"+ xqueryStr.hashCode()+".csv";
        File file = new File(filename);
        System.setProperty("file.encoding", "UTF-8");
        PrintWriter dlf =
          new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
        Object[] va =  (model.getDataVector()).toArray();
        dlf.println("Rank order,Type,Frequency,Total Types,Total Tokens");
        for (int i = 0; i < va.length ; i++) {
          if(i==0)
              dlf.println(((Vector)va[i]).get(0)+","
                     +((Vector)va[i]).get(1)+","
                     +((Vector)va[i]).get(2)+","
                     +va.length+","+notokens);
          else
              dlf.println(((Vector)va[i]).get(0)+","
                         +((Vector)va[i]).get(1)+","
                         +((Vector)va[i]).get(2));
                    }
                    //dlf.println("\nTOTAL:\t"+va.length+"\t"+notokens);
                    dlf.close();        
        }
      catch (Exception ex) {
        JOptionPane.showMessageDialog((JFrame)parent.getBrowserGUI(), "Error writing freqency list" + ex,
                                      "Error!", JOptionPane.ERROR_MESSAGE);
      }
     
 }
 
   private final Set<ThreadCompleteListener> listeners
                   = new CopyOnWriteArraySet<ThreadCompleteListener>();
  public final void addListener(final ThreadCompleteListener listener) {
    listeners.add(listener);
  }
  public final void removeListener(final org.modnlp.metafacet.ThreadCompleteListener listener) {
    listeners.remove(listener);
  }
  private final void notifyListeners() {
    for (ThreadCompleteListener listener : listeners) {

      listener.notifyOfThreadComplete(this);
    }
  }
    
  class FqlPrinter extends Thread {
    public FqlPrinter () {
      super("Frequency list producer");
    }
    public void run (){
      try {
        if (parent.isStandAlone()){
          Dictionary d = parent.getDictionary();
          if (parent.subCorpusSelected()){
            HeaderDBManager hdbm = parent.getHeaderDBManager();
            d.printSortedFreqList(fqlout, skipFirst, maxListSize,
                                  hdbm.getSubcorpusConstraints(xqueryStr),
                                  !parent.isCaseSensitive());
          }
          else
            d.printSortedFreqList(fqlout,  skipFirst, maxListSize,!parent.isCaseSensitive());
        } 
        else{
          input = new
            BufferedReader(new
                           InputStreamReader(exturlConnection.getInputStream(), "UTF-8"));
        }
      } catch (Exception e) {
        System.err.println("FqlPrinter: " + e);
        e.printStackTrace();
      }
    }
  }

  public class FNumberRenderer extends DefaultTableCellRenderer {
    NumberFormat formatter = null;

    public FNumberRenderer() {
      super();
      setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      formatter = NumberFormat.getInstance();
    }

    public void setValue(Object value) {
      if ((value != null) && (value instanceof Number)) {
        Number numberValue = (Number) value;
        value = formatter.format(numberValue.doubleValue());
      }
      super.setValue(value);
    }
  }

  public class PctRenderer extends DefaultTableCellRenderer {
    NumberFormat formatter = null;

    public PctRenderer() {
      super();
      setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      formatter = NumberFormat.getPercentInstance();
      formatter.setMinimumFractionDigits(3);
    }

    public void setValue(Object value) {
      if ((value != null) && (value instanceof Number)) {
        Number numberValue = (Number) value;
        value = formatter.format(numberValue.doubleValue());
      } 
      super.setValue(value);
    }
  }

}
