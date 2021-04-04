package com.qzero.server.tunnel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RouteThread extends Thread {

    private byte[] preSentBytes;
    private Socket source;
    private Socket destination;

    private Logger log= LoggerFactory.getLogger(getClass());

    public RouteThread(byte[] preSentBytes, Socket source, Socket destination) {
        this.preSentBytes = preSentBytes;
        this.source = source;
        this.destination = destination;
    }

    public void setPreSentBytes(byte[] preSentBytes) {
        this.preSentBytes = preSentBytes;
    }

    public void setSource(Socket source) {
        this.source = source;
    }

    public void setDestination(Socket destination) {
        this.destination = destination;
    }

    @Override
    public void run() {
        super.run();

        try {
            InputStream sourceIs=source.getInputStream();
            OutputStream dstOs=destination.getOutputStream();

            if(preSentBytes!=null){
                dstOs.write(preSentBytes);
            }

            while (true){
                dstOs.write(sourceIs.read());
            }
        }catch (Exception e){
            log.error("Route failed",e);
        }

    }
}
