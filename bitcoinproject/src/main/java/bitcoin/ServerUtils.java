package bitcoin;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerUtils {
    private int block;
    public void Generator() {
        ServerSocket echoServer = null;
        String line;
        DataInputStream is;
        PrintStream os;
        Socket clientSocket = null;
        try {
            echoServer = new ServerSocket(9999);
        }
        catch (IOException e) {
            System.out.println(e);
        }
        String mess;
        int _block;
        try {
            while (true) {
                clientSocket = echoServer.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                is = new DataInputStream(clientSocket.getInputStream());
                out.println("Halo");
                while (true) {
                    if (out.checkError()) {
                        System.out.println("ERROR writing data to socket !!!");
                        break;
                    }
                    //System.out.println(clientSocket.isConnected());
                    mess = is.readLine();
                    if (mess!=null){
                        _block=Integer.parseInt(mess);
                        if(_block>0) {
                            block=_block;
                            _block=0;
                            System.out.println(block);
                            Thread t=new Thread(this::BlockNotify);
                            t.start();
//                            BlockNotify();
                        }
                    }
                    else
                        break;
                    out.println("Ok!");
                }
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    public void BlockNotify(){
        HandleBlockNotify.checkBlock(this.block);
        App.showListWallet();
    }

}
