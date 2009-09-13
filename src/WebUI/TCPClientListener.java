package WebUI;

public interface TCPClientListener {

	void onTCPClientData(String data);
	
	void log(String s);
	
}