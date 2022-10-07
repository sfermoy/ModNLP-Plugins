/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modnlp.comfre;

//import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import netscape.javascript.JSObject;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author shane
 */
public class ComFreContainer extends JFrame {
    
    private JFrame frame;
    private static ObservableList droplist;
    private static WebEngine engine;
    private static ComFre worker;
    private  static Bridge bridgeMan;

    public ComFreContainer(ObservableList d, ComFre c) throws HeadlessException {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(1200,1200);
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

          Platform.runLater(
            new Runnable() {
                public void run() {             
                    Scene scene = createScene();
                    fxPanel.setScene(scene);
                    Platform.setImplicitExit(false);
                    
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
        
        
        hbox.getChildren().addAll(leftList,btnDraw,rightList);     
        root.getChildren().add(hbox);
        
        
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject bridge = (JSObject) engine.executeScript("window");
                bridgeMan = new Bridge();
                bridge.setMember("vec", bridgeMan); 
            }
        });
             
//        //ZOOM on scroll
//        view.addEventFilter(ScrollEvent.SCROLL, (ScrollEvent e) -> {
//            double deltaY = e.getDeltaY();
//            if(e.isControlDown() && deltaY > 0) {
//               view.setZoom(view.getZoom() * 1.1);
//               e.consume();
//            } else if(e.isControlDown() && deltaY < 0) {
//               view.setZoom(view.getZoom() / 1.1);
//               e.consume();
//            }
//        });
        
        //ZOOM on 
        view.addEventFilter(KeyEvent.KEY_RELEASED, (KeyEvent e) -> {
            if (e.getCode() == KeyCode.ADD || e.getCode() == KeyCode.EQUALS
                    || e.getCode() == KeyCode.PLUS) { 
                view.setZoom(view.getZoom() * 1.1);
            }
            else if(e.getCode() == KeyCode.SUBTRACT || e.getCode() == KeyCode.MINUS ){
                view.setZoom(view.getZoom() / 1.1);
            }
        }); 
        
        
        
        VBox.setVgrow(view, javafx.scene.layout.Priority.ALWAYS);
        
        Scene scene = new Scene(root, 1100, 1000);
        root.getChildren().add(view);

        try
        {
            URI html = new URI("");
            html =ComFre.class.getResource("ComFre.html").toURI();
            engine.load(html.toURL().toString());
                }
        catch(Exception e){
//            html ="error loading html";
        }
        

        return (scene);
    }
    
    
    public void Redraw(String f1, String f2) {  
        try{
            String str1 = readFile(f1);
            String str2 = readFile(f2);
            Platform.runLater(
                new Runnable() {
                    public void run() {
                     String out1 = str1;
//                     System.out.print(out1);
                     out1 = out1.replaceAll("\'", "\\'");
                     out1 = out1.replaceAll("\"", "");
                     out1 = out1.replaceAll("\n", "*SPACE*");
                     out1 = out1.replaceAll("\r", "");
                            
                     //System.out.print(out1);
                     String out2 = str2;
                     out2 = out2.replaceAll("\'", "\\'");
                     out2 = out2.replaceAll("\"", "");
                     out2 = out2.replaceAll("\n", "*SPACE*");
                     out2 = out2.replaceAll("\r", "");
                
                     //out1 = "tribe";
//                    System.out.println("loadFiles([\""+out1+"\"],[2])");
                    engine.executeScript("loadFiles([\""+out1+"\"],[\""+out2+"\"])");
                      
            }});
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public String readFile(String path) throws IOException {
        File file = new File(path);
        String csvdata = FileUtils.readFileToString(file);
        return csvdata;
    }

}
