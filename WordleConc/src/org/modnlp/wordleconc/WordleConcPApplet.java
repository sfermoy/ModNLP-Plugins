package org.modnlp.wordleconc;

import processing.core.*; 
import processing.data.*; 
import processing.opengl.*; 

import wordcram.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class WordleConcPApplet extends PApplet implements ActionListener{


  WordCram wc;
  Word[] words;
  int drawcount = 0;
  WordleConc parent;
  boolean waiting = true;
  WordSizer wsizer;


  public WordleConcPApplet(WordleConc p){
    super();
    this.parent = p;
  }
  
  public void setup() {
    size(800, 600);
    noLoop();
    background(255);
    drawcount = 0;
    this.setWords(parent.populateWordle());
    
    //frame.setResizable(true);
  }

  public void startLoop(){
    background(255);
    redraw();
    loop();
    waiting = false;
  }

  public void setWords(Word[] w){
    words = w;
    wc = new WordCram(this).fromWords(words);//.sizedByRank(4,80);
    wsizer = new WordSizer() {
        float minSize = 4;
        float maxSize = 80;
        public float sizeFor(Word word, int wordRank, int wordCount) {
          return PApplet.lerp(minSize, maxSize, word.weight);
        }
      };
  }

  
  public void draw() {
    if (wc == null || waiting){
      noLoop();
      return;
    }

    if (drawcount == 0){
      drawcount++;
      parent.setStatsLabel("Click on a word to see its frequency.");
      //System.out.println(java.util.Arrays.toString(PFont.list()));
      
      //PFont f = createFont("Tahoma",32,true);
      //textFont(f);
      //fill(0);
      //textAlign(LEFT,TOP);
      //text("Wordle for '"+parent.getKeyword()+"':",10,20);
    }
    if (wc.hasMore()) {
      wc.drawNext();
      //print(","+drawcount++);
    }
    else {
      println("WordleConcPApplet: =====done=====");
      noLoop();
      // dump word list (for debugging)
      //Word[] w = wc.getWords();
      //for (int i =0; i < w.length; i++)
      //  System.out.print(w[i].getProperty("count")+",");
    }
  }

  public void mouseClicked() {
    Word w = wc.getWordAt(mouseX, mouseY);
    if (w == null){
      parent.setStatsLabel("No word selected; Click on a word to see its frequency.");
      return;
    }

    String contstring;
    float weight;
    switch (parent.getColourContext(w.getRenderedColor()) ) {
    case WordleConc.LEFT_CONTEXT:
      contstring = "left context";
      weight = w.weight;
      break;
    case WordleConc.RIGHT_CONTEXT:
      contstring = "right context";
      weight = w.weight;
      break;
    default:
      contstring = "keyword";
      weight = 1;
      break;
    }
     
    parent.setStatsLabel("Word '"+w.word+"', "+contstring+", "+
                         w.getProperty("count")+" occurrences, weight (excl. stop words): "+weight);

  }
  

  public void actionPerformed(ActionEvent evt) {
    if (evt.getActionCommand().equals("Show")) {
      this.setWords(parent.populateWordle());
      println("WordleConcPApplet: =====start loop===");
      startLoop();
      redraw();
    } else {
      println("actionPerformed(): can't handle " +evt.getActionCommand());
    }
  }




  /*public void mouseClicked() {
    background(255);
    redraw();
    loop();
    }*/
  
  

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "org.modnlp.WordleConc.WordleConcPApplet" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}


