/**
 * 
 */
package id.mdgs.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Loging {
	private static PrintWriter writer;
	public static String LOG_FILE = utils.getDefaultPath() + "/resources/trash/log.txt";
	
	public static void start(PrintWriter writer) {
		
		if(Loging.writer != null){
			Loging.stop();
		}
		
		Loging.writer = writer;
	}
	
	public static void stop(){
		if(Loging.writer != null) {
			Loging.writer.flush();
			Loging.writer.close();
			Loging.writer = null;
		}
	}
	
	public static PrintWriter createConsoleWriter(){
		return new PrintWriter(System.out);
	}
	
	public static PrintWriter createFileWriter(String fname) throws IOException{
		return new PrintWriter(new FileWriter(fname), true);
	}
	
	public static void log(String s){
		if(Loging.writer == null) return;
		
		Loging.writer.print(s);
	}
}
