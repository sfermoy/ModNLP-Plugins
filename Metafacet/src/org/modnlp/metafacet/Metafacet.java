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
package org.modnlp.metafacet;

import com.sun.javafx.application.PlatformImpl;
import java.util.HashMap;
import java.util.Iterator;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import modnlp.tec.client.ConcordanceBrowser;
import modnlp.tec.client.ConcordanceObject;
import modnlp.tec.client.ConcordanceVector;
import modnlp.tec.client.Plugin;
import netscape.javascript.JSObject;

public class Metafacet extends JFrame implements Plugin{
    private static ConcordanceBrowser parent = null;
    private JFrame frame;
    private static HashMap< String, String> headers = null;
    private static String json ="[{key: \"test\", values:5},{key: \"t1\", values:2}]";
    private static boolean first = true;
    private static WebEngine engine;
    private static Button btn;
    private static ConcordanceVector vec;
    private  static VectorManager vMan;
    
    @Override
    public void setParent(Object p){
    parent = (ConcordanceBrowser)p;
    frame = this;
  }
    
    @Override
    public void activate() {
    
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(900,800);
        this.setVisible(true);
        headers = parent.getHeaderMap();
        //cycle through concordance and build metaddata array
        json = "[";
        vec = parent.getConcordanceVector();
        for (Iterator<ConcordanceObject> iterator = vec.iterator(); iterator.hasNext();) {
            ConcordanceObject next = iterator.next();
            String fn = next.sfilename;
            fn= fn.substring(0, fn.indexOf("."));
            String key = fn+ next.sectionID;
            //System.out.println(key);
            if (iterator.hasNext()){
                json += headers.getOrDefault(key, "error")+",";
            }else
                json += headers.getOrDefault(key, "error")+"]";
            
        }
        
        if( engine == null){
            //swing run later thread
            SwingUtilities.invokeLater(new Runnable() {  
               public void run() { 
                   JFXPanel fxPanel = new JFXPanel();
                   initFX(fxPanel);
                   frame.add(fxPanel);       
               }
           });  
        }
        else{
            btn.fire();
        }
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
        hbox.setPadding(new Insets(12, 12, 12, 100));
        btn = new Button();
        btn.setText("Update Bars/Load concordance");
        Button btn1 = new Button();

        
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                json = "[";
                vec = parent.getConcordanceVector();
                for (Iterator<ConcordanceObject> iterator = vec.iterator(); iterator.hasNext();) {
                    ConcordanceObject next = iterator.next();
                    String fn = next.sfilename;
                    fn= fn.substring(0, fn.indexOf("."));
                    String key = fn+ next.sectionID;
                    //System.out.println(key);
                    if (iterator.hasNext()){
                        json += headers.getOrDefault(key, "error")+",";
                    }else
                        json += headers.getOrDefault(key, "error")+"]";

                }
                engine.executeScript("loadData(" + json + ")");
                vMan.updateVector();
            }
        });
        
         
        
        hbox.getChildren().addAll(btn);     
        root.getChildren().add(hbox);
        
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {
                JSObject bridge = (JSObject) engine.executeScript("window");
                vMan = new VectorManager(parent);
                bridge.setMember("vec", vMan);
                engine.executeScript("loadData(" + json + ")");
            }
        });
        
        VBox.setVgrow(view, javafx.scene.layout.Priority.ALWAYS);
        
        Scene scene = new Scene(root, 1100, 1000);
        root.getChildren().add(view);
        
        engine.load(Metafacet.class.getResource("metafacet.html").toString());
        return (scene);
    }

  
}


