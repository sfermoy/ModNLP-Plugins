/**
 * Copyright 2018 Shane Sheehan
 * (c) 2018 S Sheehan <sheehas1@tcd.ie> S Luz <luzs@acm.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.modnlp.comfre;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import modnlp.tec.client.ConcordanceBrowser;
import modnlp.tec.client.Plugin;
import modnlp.tec.client.TecClientRequest;
import modnlp.tec.client.HeaderDownloadThread;

public class ComFre implements Plugin, Runnable, ThreadCompleteListener{
    
    ConcordanceBrowser parent =null;
    private BufferedReader input;
    private String dirName = System.getProperty("user.home")+ File.separator+"GOKCache" + File.separator+"ComFreCache";   
    private String serverStartdate;
    private String cachedDate= "";
    private String pattern = "MM/dd/yyyy HH:mm:ss";
    private DateFormat df = new SimpleDateFormat(pattern);
    
    private String[] nameStrings = { };
    private Thread thread;
    
    private String dlFile1 ="";
    private String dlFile2 ="";
    
    private String pathf1;
    private String pathf2;
    
    private FqListDownloader dl;
    ComFreContainer vis;
    
    
    @Override
    public void setParent(Object p){
    parent = (ConcordanceBrowser)p;
  }
    
    @Override
    public void activate() {
        JMenu subcorpList= parent.getRecentMenu();
        subcorpList.getItemCount();
        ObservableList<String> options = FXCollections.observableArrayList();
        for (int i = 0; i < subcorpList.getItemCount(); i++) {
            options.add(subcorpList.getItem(i).getText());
        }
         vis = new ComFreContainer(options,this);    
        serverStartdate = getServerStartDate();
        stop();
        start();
    } 
    
    @Override
    public void run() {
        validateCache();
    }
    
    public void start() {
        if (thread == null) {
          thread = new Thread(this);
          thread.setPriority(Thread.MIN_PRIORITY);
          thread.start();
        }
    }

    public void stop() {
      if (thread != null) {
        thread = null;
      }
    }
    
    
    public void buildVis(String f1, String f2) {
        dlFile1 ="";
        dlFile2 ="";
        String f1Query = getXquery(f1);
        String f2Query = getXquery(f2);
        pathf1 = dirName + File.separator+"file"+f1Query.hashCode()+".csv";
        pathf2 = dirName + File.separator+"file"+f2Query.hashCode()+".csv";
        File file1 = new File(pathf1);
        File file2 = new File(pathf2);
        
        if( file1.exists() && file2.exists()){ //both fqlists already downloaded
                //redraw using the xquery string.csv
            vis.Redraw(pathf1, pathf2);
        }else{
            if (!file1.exists() && !file2.exists()){//both fqlists not downloaded
               dlFile1 = f1Query;
               dlFile2 = f2Query;
            }
            else if (file1.exists() && !file2.exists()){//1 fqlist downloaded
               dlFile1 = f2Query;
            }
            else{
               dlFile1 = f1Query;
            }   
           FqListDownloader dl = new FqListDownloader(parent,dlFile1);
           dl.addListener(this);
           dl.run();
        }
    }
     
    public String getXquery(String queryName){
    // get query from the other cache folder
    String location = System.getProperty("user.home") + File.separator+"GOKCache" + File.separator+"namedCorpora";
    FileInputStream fis = null;
    ObjectInputStream in = null;
    String filename = location+File.separator+parent.getLanguage()+File.separator+queryName;
    File test = new File(filename);
    String result = null;
    if(test.exists()){
        try{
           fis = new FileInputStream(filename);
           in = new ObjectInputStream(fis);
           result = (String)in.readObject();
           in.close();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    else{
        JOptionPane.showMessageDialog(null, "Missing file");
    }
        return result;
    }


    @Override
    public void notifyOfThreadComplete(FqListDownloader thread) {
        if(!dlFile2.equalsIgnoreCase("")){
            FqListDownloader dl1  = new FqListDownloader(parent,dlFile2);
            dl1.addListener(this);
            dlFile2 ="";
            dl1.run();
        }else{
            vis.Redraw(pathf1, pathf2);
        }
    }

    
    public void validateCache() {
        Date cacheDate =null;
        Date serverDate=null;
        
        //Create date objects for cache validation
        String cacheDateStr = getCachedDate();
        if (cacheDateStr.equalsIgnoreCase("")  || serverStartdate.equalsIgnoreCase("") ){
            cacheDate =Calendar.getInstance().getTime();
            serverDate =Calendar.getInstance().getTime();
        }else{
            try{
                cacheDate =df.parse(cacheDateStr);   
                serverDate =df.parse(serverStartdate);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        
        //Is cache invalid?
        if( cacheDateStr.equalsIgnoreCase("") && !serverDate.before(cacheDate)){      
            File directory = new File(dirName);
            deleteDirectory(directory); //delete entire cache folder
            setCachedDate(); // create empty cache        
        }
    }
    
    private void setCachedDate() {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            File directory = new File(dirName);
            if(!directory.exists())
                directory.mkdirs();
            String filename = dirName + File.separator+"cdate"+parent.getRemoteServer()+".out";
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);
            Date today = Calendar.getInstance().getTime();        
            //Store date as string for caching on client side
            String dateCached = df.format(today);
            out.writeObject(dateCached);
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
     private String getCachedDate() { 
        String result = "";
        FileInputStream fis = null;
        ObjectInputStream in = null;
        String filename = dirName + File.separator+"cdate"+parent.getRemoteServer()+".out";
        File test = new File(filename);
        if(test.exists()){
            try{
               fis = new FileInputStream(filename);
               in = new ObjectInputStream(fis);
               result = (String)in.readObject();
               in.close();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        return result;
    }
     
       
    private String getServerStartDate() {
        String result = "";
        try{
            if (parent.isStandAlone()) {
                //for now we will just return empty string
            }
            else{
                TecClientRequest clRequest = new TecClientRequest();
                clRequest.setServerURL("http://" + parent.getRemoteServer());
                clRequest.setServerPORT(parent.getRemotePort());
                clRequest.put("request", "serverDate");
                if (parent.isSubCorpusSelectionON()) {
                  clRequest.put("xquerywhere", parent.getXQueryWhere());
                }
                clRequest.put("casesensitive", parent.isCaseSensitive() ? "TRUE" : "FALSE");
                clRequest.setServerProgramPath("/freqword");
                URL exturl = new URL(clRequest.toString());
                HttpURLConnection exturlConnection = (HttpURLConnection) exturl.openConnection();
                exturlConnection.setRequestMethod("GET");
                input = new BufferedReader(new InputStreamReader(exturlConnection.getInputStream(), "UTF-8"));
                result = input.readLine();
                exturlConnection.disconnect();
            }

          }
         catch (IOException e) {
          System.err.println("Exception: couldn't create stream socket" + e);
        }
        return result;
      }
    
     
    public static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    } 
  
}

