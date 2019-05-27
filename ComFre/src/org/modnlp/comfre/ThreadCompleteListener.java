/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modnlp.comfre;

import org.modnlp.metafacet.HeaderDownloadThread;

/**
 *
 * @author shane
 */
public interface ThreadCompleteListener {
    void notifyOfThreadComplete(final FqListDownloader thread);
}
