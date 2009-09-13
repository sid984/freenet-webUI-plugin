package WebUI;

import java.io.*;
import java.net.*;

public class TCPClient implements Runnable {
	
	private Socket clientSocket;
	private OutputStream outToServer;
	private BufferedReader inFromServer;
	private TCPClientListener listener;
	
    public TCPClient(TCPClientListener listener, String host, int port) throws Exception
    {
    	this.listener = listener;
    	
    	clientSocket = new Socket(host, port);
		outToServer = clientSocket.getOutputStream();
		inFromServer = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
    }
    
    public synchronized void run(){
    	try{
			while(true){
				if(inFromServer.ready()){
					String line = inFromServer.readLine();
					listener.onTCPClientData(line);
				} else {
					Thread.sleep(100);
				}
			}
        } catch(Exception e){
        	e.printStackTrace();
        }
    }
    
    public boolean write(String data){
    	try{
    		data = data + '\n';
    		outToServer.write(data.getBytes());
    		return true;
    	} catch(IOException e){
    		return false;
    	}
    }
    
   public  synchronized void close(){
    	try{
    		clientSocket.close();
    	} catch (IOException e){
    	}
    }
}
