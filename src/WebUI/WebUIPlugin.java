package WebUI;

import java.io.*;
import java.util.jar.*;

import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginHTTP;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.PluginHTTPException;
import freenet.pluginmanager.PluginRespirator;


//import freenet.client.FetchException;
//import freenet.client.FetchResult;
//import freenet.client.HighLevelSimpleClient;

import freenet.support.api.HTTPRequest;
import freenet.support.Logger;

import WebUI.WebServer;

public class WebUIPlugin implements FredPlugin, FredPluginHTTP, FredPluginThreadless {

	//private PluginRespirator pr;
	private static Thread thread;
	private static WebUIPlugin instance;
	
	public WebUIPlugin() throws IOException {
		instance = this;
	}
	
	public String getVersion() {
		return "0.1";
	}
	
	public long getRealVersion() {
		return 1;
	}
	
	public void runPlugin(PluginRespirator pr) { 
		// Make sure to save that pr somewhere, it will be the starting point in interfacing with the node
		//this.pr = pr;
		try{
			
			//String defaultEncodingName = java.nio.charset.Charset.defaultCharset().name();

			//String defaultEncodingName2 = new OutputStreamWriter( System.out ).getEncoding();

			//Logger.debug(this, defaultEncodingName + " / " + defaultEncodingName2);
			Logger.debug(this, "Unpacking...");
			unpackWebDir();
			
			Logger.debug(this,"Starting...");
			WebServer server = new WebServer();
			
			thread = new Thread(server, "WebServer");
			thread.start();
			Logger.debug(instance,"Started");
		} catch(Exception e){
			Logger.error(this,"Error: " + e.getLocalizedMessage() );
		}
	}
	
	public static void log(String s){
		synchronized(thread){
			Logger.debug(instance, s);
		}
	}
	
	public void terminate(){
		WebServer.kill();
	}
	
	public String handleHTTPGet(HTTPRequest request) throws PluginHTTPException{
		// It is ok to return null
		return "<script>window.location='http://localhost:9999/'</script>";
	}
	
	public String handleHTTPPost(HTTPRequest request) throws PluginHTTPException {
		// It is ok to return null
		return null;
	}
	
	public String handleHTTPPut(HTTPRequest request) throws PluginHTTPException {
		// It is ok to return null
		return null;
	}
	
  
	private void unpackWebDir() throws IOException{
		String filename = "plugins/WebUI.jar";
		File file = new File(filename);
		if(!file.exists())
			Logger.debug(instance, "File not exists!");
		
		
		File web_dir = new File("web");
		if(web_dir.exists()){
			Logger.debug(this, web_dir.getCanonicalPath());
			Logger.debug(this, web_dir.getAbsolutePath());
			
			if(!web_dir.getCanonicalPath().equals( web_dir.getAbsolutePath() )){
				Logger.debug(this, "It seems web dir is symlink. Leaving it unmodified...");
				return;
			} else if(web_dir.isDirectory()){
				Logger.debug(this, "Deleting web dir...");
				deleteDir(web_dir);
			}
		}
		
		File root = new File(".");
		InputStream in = (InputStream) new FileInputStream(file);
		unpack(in, root);
	}
	
	public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }

	
	private void unpack(InputStream in, File bundleRoot) {
		try {
			byte buffer[] = new byte[1024];
			JarInputStream jin = new JarInputStream(in);
			JarEntry jarEntry = null;
			while ((jarEntry = jin.getNextJarEntry()) != null) {
				String name = jarEntry.getName();
				if(!name.startsWith("web/"))
					continue;
				Logger.debug(this, "Unpacked jar entry " + name);
				
				if (jarEntry.isDirectory()) {
					File dir = new File(bundleRoot, jarEntry.getName());
					dir.mkdirs();
				} else {
					File f = new File(bundleRoot, jarEntry.getName());
					f.createNewFile();
					FileOutputStream fos = new FileOutputStream(f);
					while (true) {
						int nRead = jin.read(buffer, 0, buffer.length);
						if (nRead <= 0)
							break;
						fos.write(buffer, 0, nRead);
					}
					fos.close();
					Logger.debug(this, "Unpacked jar entry " + jarEntry.getName());
				}
			}
			
			// Create the bundle
			//BundleBuilder bundleBuilder = BundleBuilderFactory.newInstance().newBundleBuilder();
			//return bundleBuilder.loadFromDirectory(bundleRoot);
		} catch (IOException e) {
			Logger.error(this, "Error extracting bundle from jar file: " + e.getMessage());
		}
	}

	
}