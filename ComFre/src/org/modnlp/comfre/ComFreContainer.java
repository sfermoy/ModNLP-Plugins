/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modnlp.comfre;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.webkit.WebConsoleListener;
import java.awt.HeadlessException;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author shane
 */
public class ComFreContainer extends JFrame {
    
    private JFrame frame;
    private static ObservableList droplist;
    private static WebEngine engine;
    private static ComFre worker;

    public ComFreContainer(ObservableList d, ComFre c) throws HeadlessException {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(1100,1000);
        this.setVisible(true);
        worker = c;
        frame =this;
        droplist =d;
        createFX();
    }
    
    public void createFX() { 
        //swing run later thread
        SwingUtilities.invokeLater(new Runnable() {  
           public void run() { 
               JFXPanel fxPanel = new JFXPanel();
               initFX(fxPanel);
               frame.add(fxPanel);       
           }
       });   
    } 
    
    private static void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
        //cannot run in swing enviornment

          PlatformImpl.startup(
            new Runnable() {
                public void run() {             
                    Scene scene = createScene();
                    fxPanel.setScene(scene);
                    PlatformImpl.setImplicitExit(false);
                    
                }});
    }
     
    private static Scene createScene() {
        WebView view = new WebView();
        engine = view.getEngine();
        engine.setJavaScriptEnabled(true);
        
        VBox root = new VBox();
        
        HBox hbox = new HBox(300);
        hbox.setPadding(new Insets(12, 12, 12, 70));
        
 
        
        Button btnDraw = new Button();
        btnDraw.setText("Draw");
        ComboBox leftList = new ComboBox(droplist);
        ComboBox rightList = new ComboBox(droplist);
        //leftList.getSelectionModel().select(0);
        //rightList.getSelectionModel().select(0);
        
        leftList.setTooltip(new Tooltip("Select Corpus"));
        rightList.setTooltip(new Tooltip("Select Corpus"));
        
        btnDraw.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // fire off other thread to see if the files exist
                // if not download them
                worker.buildVis((String)leftList.getSelectionModel().getSelectedItem(), (String)rightList.getSelectionModel().getSelectedItem());
            }
        });
        
        WebConsoleListener.setDefaultListener(new WebConsoleListener(){
            @Override
            public void messageAdded(WebView webView, String message, int lineNumber, String sourceId) {
                System.out.println("Console: [" + sourceId + ":" + lineNumber + "] " + message);
            }
        });
        
        hbox.getChildren().addAll(leftList,btnDraw,rightList);     
        root.getChildren().add(hbox);
        VBox.setVgrow(view, javafx.scene.layout.Priority.ALWAYS);
        
        Scene scene = new Scene(root, 1100, 1000);
        root.getChildren().add(view);
        
        engine.load(ComFre.class.getResource("ComFre.html").toString());
        return (scene);
    }
    
    
    public void Redraw(String f1, String f2) {    
        PlatformImpl.startup(
            new Runnable() {
                public void run() {
                    engine.executeScript("redrawVis(\""+f1+"\", \""+f2+"\");");              
            }});
    }

}
