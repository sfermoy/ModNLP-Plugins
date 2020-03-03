/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modnlp.comfre;

import javafx.application.Platform;

/**
 *
 * @author shane
 */
public class Bridge {

    public void log(String text) {
        System.err.println("JS Console");
        System.out.println(text);
    }

}
