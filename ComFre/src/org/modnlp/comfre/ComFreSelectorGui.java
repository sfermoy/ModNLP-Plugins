/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modnlp.comfre;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.Timer;
import modnlp.idx.headers.HeaderDBManager;
import modnlp.tec.client.ConcordanceBrowser;
import modnlp.tec.client.RemoteSubcorpusOptionRequest;
import modnlp.tec.client.gui.AttributeChooser;
import modnlp.tec.client.gui.MultipleAttributeSelection;

/**
 *
 * @author shane
 */
public class ComFreSelectorGui extends JFrame {
    HeaderDBManager hdbm;
  MultipleAttributeSelection mas = new MultipleAttributeSelection("Subcorpus selection:");
  boolean loadingDone = false;
  boolean guiLayoutDone = false;
  boolean networkError = false;
  String[] attChsrSpecs;
  JCheckBox activeChecked = new JCheckBox("Sub-corpus selection is on");
  ConcordanceBrowser parent = null;
  private JFrame thisFrame = null;
  int progress = 0;
  Thread thread;
  private Timer timer;
  private boolean remoteServer = false;
  private boolean interrupted = false;
  
  final JButton saveButton = new JButton("Save Corpus");
  final JButton saveApplyButton = new JButton("Save+Select Corpus");
  final JButton removeButton = new JButton("Remove Named Subcorpus");
  final JButton ApplyButton = new JButton("Select Corpus");
  final JButton clearButton = new JButton("Clear");
  private JTextField namedCorp = new JTextField(20);
  private String dirName =".namedCorpora";
  private JLabel label2 = new JLabel("Corpus name: ");
  private String[] nameStrings = { "               " };
  private JComboBox remList = null;


  public ComFreSelectorGui(ConcordanceBrowser p){
    super("Save Sub-corpus");
    thisFrame = this;
    parent = p;
       
    if (parent.isStandAlone() ){
      attChsrSpecs = parent.getDictionary().getDictProps().getAttributeChooserSpecs();
      hdbm = parent.getHeaderDBManager();
      remoteServer = false;
    }
    else {
      remoteServer = true;
    }
    start();
  }

  public void start() {
    thread = new LoadOptionsThread();
    thread.start();
  }
  
  public void dispose(){
    interrupted = true;
    while (!loadingDone) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
      }
    }
    if (thread != null)
      thread = null;
    super.dispose();
  }

  public boolean isLoadingDone(){
    return loadingDone;
  }
  
  public void loadNamedCorpora(String name){
    FileInputStream fis = null;
    ObjectInputStream in = null;
    String filename = dirName+"/"+parent.getLanguage()+"/"+name;
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
        if (result != null && result.length() > 0 ){
            parent.setXQueryWhere(result);
            parent.setAdvConcFlag(true);
            activeChecked.setSelected(true);
        }
        else {
            parent.setAdvConcFlag(false);
            activeChecked.setSelected(false);
        }
    }
    else{
        JOptionPane.showMessageDialog(null, "Sub-corpus selection failed");
    }
  }

  public String getSelection(){
    return mas.getSelection();
  }

  public void activate() {
    if (!loadingDone) {
      //task = new LongTask();
      ProgressMonitor pm = 
        new ProgressMonitor(this,
                            "Loading subcorpus selector", 
                            "", 0, attChsrSpecs.length);      
      timer = new Timer(1000, new TimerListener(pm));
      timer.start();     
    }
    
    if (guiLayoutDone && loadingDone){
      thisFrame.setVisible(true);
      return;
    }
    JPanel pa0 = new JPanel();
    pa0.setLayout(new BorderLayout());
    pa0.add(mas, BorderLayout.CENTER);
    //pa0.add(new JLabel(" "), BorderLayout.EAST);
    //pa0.add(new JLabel(" "), BorderLayout.WEST);
    
    //JPanel pa1 = new JPanel();
    //pa1.add(activeChecked);
    JPanel pa2 = new JPanel();
    pa2.setBorder(BorderFactory.
              createCompoundBorder(BorderFactory.createTitledBorder("Save or Select"),
                                   BorderFactory.createEmptyBorder(5,5,5,5)));
    pa2.add(label2);
    pa2.add(namedCorp);
    pa2.add(saveButton);
    pa2.add(saveApplyButton);
    pa2.add(ApplyButton);
    pa2.add(clearButton);
    pa2.add(activeChecked);
    
    JPanel pa3 = new JPanel();
    pa3.setBorder(BorderFactory.
              createCompoundBorder(BorderFactory.createTitledBorder("Delete Saved Subcorpus"),
                                   BorderFactory.createEmptyBorder(5,5,5,5)));
    
    // create delete box
    remList = new JComboBox(nameStrings);
    remList.setSelectedIndex(0);
    loadRemoveMenu();
    pa3.add(remList);
    pa3.add(removeButton);


    thisFrame.add(pa0, BorderLayout.NORTH);
    thisFrame.add(pa2, BorderLayout.CENTER);
    thisFrame.add(new JLabel("   "), BorderLayout.SOUTH);
    thisFrame.add(pa3, BorderLayout.SOUTH);


    saveButton.addActionListener(new SaveListener());
    saveApplyButton.addActionListener(new SaveApplyListener());
    ApplyButton.addActionListener(new ApplyListener());
    clearButton.addActionListener(new ClearListener());
    activeChecked.addActionListener(new activeCheckedListener());
    mas.addActionListener(new ActionListener(){
                              public void actionPerformed(ActionEvent e){
                                saveApplyButton.setEnabled(true);
                              }});
    
    removeButton.addActionListener(new ActionListener(){
                              public void actionPerformed(ActionEvent e){
                                File file = new File(dirName+"/"+parent.getLanguage()+"/"+remList.getSelectedItem());
                                if (file.exists()){
                                    file.delete();
                                    remList.removeItem(remList.getSelectedItem());
                                    remList.addItem(nameStrings[0]);
                                    parent.lodeRecentMenu(); 
                                }
                              }});
    if (loadingDone){
      thisFrame.pack();
      thisFrame.setVisible(true);
      guiLayoutDone = true;
    }
  }

  public void loadRemoveMenu(){
    File folder = new File(dirName+"/"+parent.getLanguage()+"/");
    if (folder.exists()){
        File[] listOfFiles = folder.listFiles();
        remList.removeAllItems();
        remList.addItem(nameStrings[0]);
        for (int i = 0; i < listOfFiles.length; i++) {
          if (listOfFiles[i].isFile()) {
            System.out.println("File " + listOfFiles[i].getName());
            remList.addItem(listOfFiles[i].getName());
          } 
        }
    }
  }
  
  public boolean hasNetworkError(){
    return networkError;
  }

  class LoadOptionsThread extends Thread {

    public void run() {
      loadingDone = false;
      networkError = false;
      if (remoteServer) {
        RemoteSubcorpusOptionRequest ror = 
          new RemoteSubcorpusOptionRequest(parent.getRemoteServer(),
                                           parent.getRemotePort());
        try{
          attChsrSpecs = ror.getAttributeChooserSpecs();
        }
        catch(java.net.ConnectException e){
          //JOptionPane.showMessageDialog(null, e, "ERROR", JOptionPane.ERROR_MESSAGE);
          //          parent.showErrorMessage("Could not connect to remote server "+
          //                        parent.getRemoteServer()+":"+parent.getRemotePort()+": "+e);
          e.printStackTrace(System.err);
          loadingDone = true;
          guiLayoutDone = true;
          networkError = true;
          return;
        }
        catch(Exception e){
          thisFrame.add(new JLabel("Sub-corpus selection not supported by "+parent.getRemoteServer()));
          e.printStackTrace(System.err);
          final JButton doneButton = new JButton("OK");
          doneButton.addActionListener(new ActionListener(){
              public void actionPerformed(ActionEvent e){
                thisFrame.setVisible(false);
              }});
          loadingDone = true;
          guiLayoutDone = true;
          return;
        }
      }
      
      for (int i = 0; i < attChsrSpecs.length && !interrupted; i++) {
        progress = i;
        String[] o = {""};
        if (remoteServer) {
          RemoteSubcorpusOptionRequest ror = 
            new RemoteSubcorpusOptionRequest(parent.getRemoteServer(), parent.getRemotePort());
          try {
            System.err.println("Getting "+attChsrSpecs[i+1]);
            o = ror.getOptionSet(attChsrSpecs[i+1]);
          }
          catch(Exception e){
            System.err.println("Error getting remote subcorpus options for "+attChsrSpecs[i+1]+": "+e);
          }
        }
        else {
          o = hdbm.getOptionSet(attChsrSpecs[i+1]);
        }
        //System.err.println(java.util.Arrays.toString(o));
        mas.add(new AttributeChooser(attChsrSpecs[i], attChsrSpecs[++i],o));
      }
      loadingDone = true;
      //hdbm.finalize();
      //hdbm = null;
    }
  }
  
  class ApplyListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String q = mas.getSelection();
      if (q != null && q.length() > 0 ){
        parent.setSubcorpusName("Not Named");
        parent.setXQueryWhere(q);
        parent.setAdvConcFlag(true);
        activeChecked.setSelected(true);
        
      }
      else {
        parent.setAdvConcFlag(false);
        activeChecked.setSelected(false);
        //activeChecked.doClick();
      }

      thisFrame.setVisible(false);
      //dispose();
    }
  }

  class SaveListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
       //create dir for named corpora
      String dirStr = dirName+"/"+parent.getLanguage();
      File directory = new File(dirStr);
      directory.mkdirs();
      //get query and name
      String q = mas.getSelection();
      String name = namedCorp.getText();
      //make file
      if (q != null && q.length() > 0 && name!= null && name.length()>0){
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            String filename = dirStr+"/"+namedCorp.getText();
            System.out.println(filename);
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(q);
            out.close();
            parent.lodeRecentMenu();
            loadRemoveMenu();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
      }
      else {
        // do nothing if no name or xquery
      }
    }
  }

  class SaveApplyListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
     //create dir for named corpora
      String dirStr = dirName+"/"+parent.getLanguage();
      File directory = new File(dirStr);
      directory.mkdirs();
      //get query and name
      String q = mas.getSelection();
      String name = namedCorp.getText();
      //make file
      if (q != null && q.length() > 0 && name!= null && name.length()>0){
        parent.setSubcorpusName(namedCorp.getText());
        parent.setXQueryWhere(q);
        parent.setAdvConcFlag(true);
        activeChecked.setSelected(true);
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            String filename = dirStr+"/"+namedCorp.getText();
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(q);
            out.close();
            parent.lodeRecentMenu();
            loadRemoveMenu();            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
      thisFrame.setVisible(false);
      }
      else {
        // do nothing if no name or xquery
      }
    }
  }

  class ClearListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      mas.reset();
      parent.setXQueryWhere(null);
      parent.setAdvConcFlag(false);
      activeChecked.setSelected(false);
      //activeChecked.doClick();
    }
  }

  class activeCheckedListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      if (activeChecked.isSelected()){
        String q = mas.getSelection();
        if (q != null && q.length() > 0 ){
          parent.setXQueryWhere(q);
          parent.setAdvConcFlag(true);
        }
        else {
          parent.setAdvConcFlag(false);
          activeChecked.setSelected(false);
        }
      }
      else {
        parent.setAdvConcFlag(false);
      }
    }
  }

  class TimerListener implements ActionListener {
    ProgressMonitor progressMonitor;

    public TimerListener(ProgressMonitor p){
      progressMonitor = p;
    }

    public void actionPerformed(ActionEvent evt) {
      if (progressMonitor.isCanceled() || loadingDone) {
        progressMonitor.close();
        if (! guiLayoutDone){
          thisFrame.pack();
          thisFrame.setVisible(true);
          guiLayoutDone = true;
        }
        timer.stop();
      } else {
        progressMonitor.setProgress(progress);

      }
    }
  }


}

