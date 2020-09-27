package ru.undertaker.project.rover;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Handler;

public class ConnectedThred extends Thread {
    private  BluetoothSocket copyBtSocket;
    private  OutputStream outStrem;
    private  InputStream inStrem;
    private Handler h;

    public ConnectedThred(BluetoothSocket socket){
        copyBtSocket = socket;
        OutputStream tmpOut = null;
        InputStream tmpIn = null;
        try{
            tmpOut = socket.getOutputStream();
            tmpIn = socket.getInputStream();
        } catch (IOException e){}

        outStrem = tmpOut;
        inStrem = tmpIn;
    }


}
