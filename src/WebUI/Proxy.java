package WebUI;

import java.io.*;
import java.net.*;
import java.util.*;

public class Proxy implements TCPClientListener{
	
	static Hashtable instances = new Hashtable();
	static int session_tmp = 0;
	static int[] allowed_ports = {3143, 3025};
	
	Socket client;
	TCPClient server;
	PrintStream stream;
	Thread thread;
	int port;
	String body;
	
	static final byte[] EOL = {(byte)'\r', (byte)'\n' };
	
	Proxy(PrintStream ps, Socket s, String body, int port) {
		this.port = port;
		this.body = body;
		this.client = s;
		this.stream = ps;
    }
    
    public void connect(){
    	try{
			server = new TCPClient((TCPClientListener) this, "localhost", port);
			thread = new Thread(server, "socket");
			thread.start();
			   
			if(!body.equals(""))
				server.write(body);
		} catch(Exception e){
			e.printStackTrace();
		}
    }
    
    public static void parse(PrintStream ps, Socket s, String request){
    	request = request.substring(request.indexOf("\r\n") + 2);
    	
    	//WebServer.log(request);
    	int index = request.indexOf("\r\n\r\n");
    	String headers_str = request.substring(0, index);
    	String body = request.substring(index + 4);
    	boolean close = false;
    	
    	String [] headers_arr = headers_str.split("\r\n");
    	Hashtable headers = new Hashtable();
    	for(int i = 0; i < headers_arr.length; i++){
    		int j = headers_arr[i].indexOf(": ");
    		headers.put((Object) headers_arr[i].substring(0, j), (Object) headers_arr[i].substring(j + 2));
    	}
    	
    	//WebServer.log((String) headers.get("port"));
    	
    	if(headers.containsKey("session")){
			// write to existing proxy-connection request
			
			Object sid = headers.get("session");
			
			WebServer.log("Proxy push(session " + (String) sid + "): " + body);
			
			Object instance = instances.get(sid);
			
			if(instance == null){
				//ps.print("Session not found");
				WebServer.log("Session not found");
				response(ps, "Session not found", null);
			} else {
				WebServer.log("ok");
				((Proxy) instance).onClientData(body);
				response(ps, "ok", null);
			}
			close = true;
		} else {
			// new proxy-connection request
				
			int port = Integer.parseInt((String) headers.get("port"));
			boolean allowed = false;
			for(int i = 0; i < allowed_ports.length; i++){
				if(allowed_ports[i] == port){
					allowed = true;
					break;
				}
			}
			
			if(allowed){			
				WebServer.log("New proxied request on port " + String.valueOf(port));
				
				String session = generateSession();
				
				Proxy proxy = new Proxy(ps, s, body, port);
				instances.put((Object) session, proxy); 
					
				Hashtable hdrs = new Hashtable();
				hdrs.put("session", (Object) session);	
				response(ps, null, hdrs);
				
				proxy.connect();
				close = false;
			} else {
				WebServer.log("Access denied: port " + String.valueOf(port) + " is not allowed");
				close = true;
			}
		} 
		
		if(close){
			try{
				s.close();
			} catch(IOException e){
				e.printStackTrace();
			}
		}
			
    }
    
    private static String generateSession(){
    	return String.valueOf(session_tmp = session_tmp + 1);
    }
    
    private static void response(PrintStream ps, String body, Hashtable headers){
    	String response = "HTTP/1.1 200\r\n" ;
    	body = body == null ? "" : body;
    	
    	if(headers != null){
			for(Enumeration e = headers.keys(); e.hasMoreElements();){
				Object key = e.nextElement();
				response += (String) key + ": " + (String) headers.get(key) + "\r\n";
			}
		}
		ps.print(response + "\r\n" + body);
    }
    
    public synchronized void onTCPClientData(String data){
    	try{
    		WebServer.log("Proxy onServerData: " + data);
    		stream.print(data);
    		stream.write(EOL);
		} catch(IOException e){
			e.printStackTrace();
		}
    }
    
    public synchronized void log(String s){
    	WebServer.log(s);
    }
    
    public void onClientData(String data){
    	WebServer.log("Proxy onClientData: " + data);
    	//WebServer.log(data.substring(data.length() - 2, data.length()));
    	server.write(data);
    }
	
}

/*

		onClientClose: function(){
			log("Proxy: Client connection closed");
			this.session && delete Proxy.instances[this.session];
			this.server.close(); // ???
		},
		
		onServerClose: function(data){
			log("Proxy: Server connection closed");
			this.client.write("* ERROR connection failed");
			this.client.close();
		}
*/
