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
package org.modnlp.ComFre;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserContext;
import com.teamdev.jxbrowser.chromium.BrowserPreferences;
import com.teamdev.jxbrowser.chromium.ProtocolHandler;
import com.teamdev.jxbrowser.chromium.ProtocolService;
import com.teamdev.jxbrowser.chromium.URLRequest;
import com.teamdev.jxbrowser.chromium.URLResponse;
import java.awt.BorderLayout;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;
import javax.swing.JFrame;
import modnlp.tec.client.ConcordanceBrowser;
import modnlp.tec.client.Plugin;

public class ComFre extends JFrame implements Plugin{
    ConcordanceBrowser parent =null;
    @Override
    public void setParent(Object p){
    parent = (ConcordanceBrowser)p;
  }
    @Override
    public void activate() {
        //Need to switch off to initally load files from csvs
        BrowserPreferences.setChromiumSwitches( "--disable-web-security", "--allow-file-access-from-files");
        Browser browser = new Browser();
        BrowserView view = new BrowserView(browser);

        JFrame frame = new JFrame();
        frame.add(view, BorderLayout.CENTER);
        frame.setSize(1200, 1000);
        frame.setVisible(true);
        
        BrowserContext browserContext = browser.getContext();
        ProtocolService protocolService = browserContext.getProtocolService();
        protocolService.setProtocolHandler("jar", new ProtocolHandler() {
            @Override
            public URLResponse onRequest(URLRequest request) {
                try {
                    URLResponse response = new URLResponse();
                    URL path = new URL(request.getURL());
                    InputStream inputStream = path.openStream();
                    DataInputStream stream = new DataInputStream(inputStream);
                    byte[] data = new byte[stream.available()];
                    stream.readFully(data);
                    response.setData(data);
                    response.getHeaders().setHeader("Content-Type", "text/html");
                    return response;
                } catch (Exception ignored) {}
                return null;
            }
        });

        //load html file in same folder as class
        browser.loadURL(ComFre.class.getResource("ComFre.html").toString());
    }    
}
