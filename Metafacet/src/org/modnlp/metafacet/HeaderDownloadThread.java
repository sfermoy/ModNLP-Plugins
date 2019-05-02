/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modnlp.metafacet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.JProgressBar;
import modnlp.tec.client.TecClientRequest;

/**
 *
 * @author shane
 */
public class HeaderDownloadThread implements Runnable {
    
// String returned by server for reference
//     String queryFileVisReturn = "{data($s/document/@filename)}<sep/>"
//                + "{data($s/document/@format)}<sep/>"
//                + "{data($s/document/collection_title)}<sep/>"
//                + "{data($s/document/editor)}<sep/>"
//                + "{for $section in $s/section return"
//                    + " concat (\"<section/>\",data($section/@id),\"<sep/>\","
//+ "data($section/@title),\"<sep/>\","    
//                    + "data($section/@outlet),\"<sep/>\","
//                    + "data($section/@internet_outlet),\"<sep/>\","
//                    + "data($section/@publication_date),\"<sep/>\","
//                    + "data($section/@authorship_date),\"<sep/>\","
//                        + "string-join($section/author/name, ', '),\"<sep/>\","
//                        + "string-join($section/translation/translator/name, ', '),\"<sep/>\","
//                        + "data($section/translation/source/@date),\"<sep/>\","
//                        + "data($section/translation/source/@filename),\"<sep/>\","
//                        + "data($section/translation/source/@language),\"<sep/>\","
//                        + "data($section/translation/source/original_title),\"<sep/>\""
//                +")}";
    
private Thread dldThread;
private HashMap<String,String> headermap = null;    
String[] fileAttrNames = null;
String[] sectionAttrNames = null;
Boolean finished = false;
private String commandToServer = null;
  /** default port; normally reset by Browser */

boolean stop = false;
boolean serverResponded = false;
int ctRead = 0;
private TecClientRequest request = null;

Socket  socket = null;
//PrintStream output;
BufferedReader input = null;
String encoding = "UTF-8";  // the name of the charset
JProgressBar bar= null;

  
  /** Starts a new connection and performs a request to
   *  be displayed in the <code>ListDisplay</code> provided.
   *
   * @param server  The default server to which we'll connect
   * @param upd     The ListDisplay to update
   * @param ch      The request to be passed on to <code> server</code>
   */
  public HeaderDownloadThread(TecClientRequest cr, HashMap<String,String> headers, JProgressBar b)
  {
    headermap = headers;
    request = cr;
    //HASHMAP GPES HERE
    serverResponded = false;
    bar=b;
  }
  
    public HeaderDownloadThread(BufferedReader in,TecClientRequest cr, HashMap<String,String> headers, JProgressBar b)
  {
    headermap = headers;
    input = in;
    request = cr;
    //HASHMAP GPES HERE
    serverResponded = false;
    bar=b;

  }

  
  public void setEncoding (String e){
    encoding = e;
  }

  /** Starts a new connection and performs a request to
   *  be displayed in the <code>ListDisplay</code> provided.
   *
   * @param server  The default server to which we'll connect
   * @param upd     The ListDisplay to update
   * @param ch      The request to be passed on to <code> server</code>
   */
  private void initConcordanceThread( )
  {
    try {
      if (input == null) {
        //socket = new Socket(InetAddress.getByName(SERVER), PORTNUM);
        URL concurl = new URL( request.toString());
        HttpURLConnection concurlConnection =
          (HttpURLConnection) concurl.openConnection();
        concurlConnection.setUseCaches(false);
        concurlConnection.setRequestMethod("GET");
        concurlConnection.connect();  
        input = new
          BufferedReader(new
                         InputStreamReader(concurlConnection.getInputStream(),
                                           encoding));
      }

      serverResponded = true;

    }
    catch (java.lang.Exception e){
      stop();
    }
  }
  
    @Override
    public void run() {
    stop = false;
    String[] sections =null;
    String[] fileAttributes = null;
    String[] sectionAttributes = null;
    Boolean first = true;
    int progress=0;
    try {
      initConcordanceThread();
      String header = null;
      while (!stop ) {

        if ((header = input.readLine()) == null ){
          break;
        }
        if (header.length() > 0)
        {
            if (first){
                String[] s = header.split("<section/>");
                fileAttrNames = s[0].split("<sep/>");
                sectionAttrNames = s[1].split("<sep/>");
                first = false;           
            }else{
             sections = header.split("<section/>"); 
             for (int i = 0; i < sections.length; i++) {
                String jsonString = "{";
                String section = sections[i];     
                  if(i == 0){
                      fileAttributes = section.split("<sep/>");
                  }else{
                      sectionAttributes = section.split("<sep/>");
                      for (int j = 0; j < fileAttributes.length; j++) {
                        jsonString += fileAttrNames[j] +":\"" + fileAttributes[j].replace("\"", "").trim()+"\", ";
                      }
                      for (int j = 0; j < sectionAttrNames.length-1; j++) {
                          jsonString += sectionAttrNames[j] +":\"" + sectionAttributes[j].replace("\"", "").trim()+"\", ";
                      }
                      jsonString += sectionAttrNames[sectionAttrNames.length-1] +":\"" + sectionAttributes[sectionAttrNames.length-1]+"\" }";
                      headermap.put(fileAttributes[0].trim()+sectionAttributes[0].trim(), jsonString);

                  }
              }
            }
        }
        else {
          System.err.println("__headerThread:skipped: "+ctRead);
          continue;
        }
        if(ctRead%200 ==0){
               progress+=10;
               if (progress<90)
                bar.setValue(progress);
        }
        ctRead++;
      
      } // end while
        notifyListeners();
        bar.setValue(99);
        System.out.println("Header Download Finished");
        finished = true;
       
        stop();
    }
    catch (Exception e){
        System.err.println("HeaderThread Error " +e.getMessage() +"\n "+e.getLocalizedMessage() +"\n\n end");
        e.printStackTrace();
      stop();
    }
  }
    
    public void start(){
    if ( dldThread == null ){
      dldThread = new Thread(this);
      dldThread.start();
      try
        { 
            dldThread.join(); 
        } 
        catch(Exception ex) 
        { 
            System.out.println("Exception has " + 
                                "been caught" + ex); 
        } 
    }
  }
    
  public Boolean isFinished(){
    return finished;
  }

  public void stop() {
    synchronized(this){
        this.notifyAll();
    }
    if (dldThread == null)
      return;
    dldThread = null;
    try{
      if (socket != null) 
        socket.close();
      input = null;
      socket = null;
    }
    catch (java.lang.Exception e){
      System.err.println("Error Stopping thread "+e);
    }
  }
  
  private final Set<ThreadCompleteListener> listeners
                   = new CopyOnWriteArraySet<ThreadCompleteListener>();
  public final void addListener(final ThreadCompleteListener listener) {
    listeners.add(listener);
  }
  public final void removeListener(final ThreadCompleteListener listener) {
    listeners.remove(listener);
  }
  private final void notifyListeners() {
    for (ThreadCompleteListener listener : listeners) {

      listener.notifyOfThreadComplete(this);
    }
  }
}


