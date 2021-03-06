/*
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.genericra.inbound.async;

import com.sun.genericra.util.*;

import java.util.logging.*;

import javax.resource.spi.work.*;


/**
 * Work object as per JCA 1.5 specification.
 * This class makes sure that each message delivery happens in
 * different thread.
 *
 * @author Binod P.G
 */
public class WorkImpl implements Work {
    private static Logger _logger;

    static {
        _logger = LogUtils.getLogger();
    }

    InboundJmsResource jmsResource;

    public WorkImpl(InboundJmsResource jmsResource) {
        this.jmsResource = jmsResource;
    }

    public void run() {
        try {
            _logger.log(Level.FINER, "Now running the message consumption");
            this.jmsResource.refresh();
            this.jmsResource.getSession().run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {                
                this.jmsResource.releaseEndpoint();
                DeliveryHelper helper = this.jmsResource.getDeliveryHelper();
                if (helper.markedForDMD()) {
                    helper.sendMessageToDMD();
                }
            } catch (Exception e) {
                _logger.log(Level.SEVERE,
                        "Exception while releasing the JMS endpoint" + e.getMessage());
            } finally {
                try {
                    this.jmsResource.release();
                } catch (Exception e) {
                    _logger.log(Level.SEVERE, 
                            "Exception while releasing the JMS resource" + e.getMessage());
                }
            }
            _logger.log(Level.FINER, "Freed the resource now");
        }
    }

    public void release() {
        // For now do nothing.
    }
}
