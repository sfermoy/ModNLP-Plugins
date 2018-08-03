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

import com.sun.javafx.application.PlatformImpl;
import java.io.File;
import javafx.application.Platform;
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
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import modnlp.tec.client.ConcordanceBrowser;
import modnlp.tec.client.Plugin;

public class ComFre extends JFrame implements Plugin{
    ConcordanceBrowser parent =null;
     private JFrame frame;
    
    @Override
    public void setParent(Object p){
    parent = (ConcordanceBrowser)p;
    frame = this;
  }
    
    @Override
    public void activate() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(1100,1000);
        this.setVisible(true);
        
        
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
                }});
    }
     
    private static Scene createScene() {
        WebView view = new WebView();
        WebEngine engine = view.getEngine();
        engine.setJavaScriptEnabled(true);
        
        VBox root = new VBox();
        
        HBox hbox = new HBox(300);
        hbox.setPadding(new Insets(12, 12, 12, 100));
        Button btn = new Button();
        btn.setText("Choose Left File");
             
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Resource File");
                File file =fileChooser.showOpenDialog(null);
                engine.executeScript("file1 = \""+file.getAbsolutePath()+"\"; title1 = \"" +file.getName() + "\";");           
            }
        });
        
        Button btn1 = new Button();
        btn1.setText("Choose Right File");
        
        btn1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Resource File");
                File file =fileChooser.showOpenDialog(null);
                engine.executeScript("file2 = \""+file.getAbsolutePath()+"\"; title2 = \"" +file.getName() + "\";");
            }
        });
        
        Button btnDraw = new Button();
        btnDraw.setText("ReDraw");
        
        btnDraw.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                engine.executeScript("redrawVis();");
            }
        });
        
        hbox.getChildren().addAll(btn,btnDraw,btn1);     
        root.getChildren().add(hbox);
                
        engine.load(ComFre.class.getResource("ComFre.html").toString());
        VBox.setVgrow(view, javafx.scene.layout.Priority.ALWAYS);
        
        Scene scene = new Scene(root, 1100, 1000);
        root.getChildren().add(view);
        return (scene);
    }



  
}

