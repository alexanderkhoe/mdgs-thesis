/**
 * 
 */
package id.mdgs.utils;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

/**
 * @author madeagus
 *
 */
public class utils {

	public static class timer {
		private static long elapsed;
		public static void start(){
			timer.elapsed = System.currentTimeMillis(); 
		}
		
		public static long stop(){
			return System.currentTimeMillis() - timer.elapsed;
		}
	}
	
	public static String getDefaultPath(){	
		return new java.io.File("").getAbsolutePath();		
	}
	
	public static void header(String s){			
		System.out.println(String.format("== %s %s", 
				s, StringUtils.repeat("=", 75 - s.length())));
	}
	
	public static void header(String s, PrintWriter writer){
		writer.println(String.format("== %s %s", 
				s, StringUtils.repeat("=", 75 - s.length())));		
	}
	
	public static BufferedWriter openWriter(String fname, boolean append){
		FileWriter fw = null;
		BufferedWriter bw = null; 
		
		try {
			fw = new FileWriter(fname, append);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			throw new RuntimeException(e.toString());
		}
		
		return bw;
	}
	
	public static void closeWriter(BufferedWriter bw){
		if(bw != null){
			try {
				bw.flush();
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	public static void log(String s){
		System.out.println(s);
	}
	
	public static void log(PrintWriter writer, String s){
		writer.println(s);
	}
	
	public static String elapsedTime(long millis){
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
					   TimeUnit.MINUTES.toSeconds(minutes);
		long milliss = millis - TimeUnit.MINUTES.toMillis(minutes) -
					   TimeUnit.SECONDS.toMillis(seconds);
		return	String.format("%02d:%02d.%03d", minutes, seconds, milliss);
	}
	
	
	public static void serialized(Object ob, String fname){
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		
		long time = System.currentTimeMillis();
		
		try
		{
			fos = new FileOutputStream(fname);
			out = new ObjectOutputStream(fos);
			out.writeObject(ob);
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}		
		
		utils.log("Serialized time : " + (System.currentTimeMillis() - time) + "ms");
	}
	
	public static Object deserialized(String fname){
		Object ob = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		
		long time = System.currentTimeMillis();
		
		try
		{
			 fis = new FileInputStream(fname);
			 in = new ObjectInputStream(fis);
			 ob = in.readObject();
			 in.close();
		} 
		catch(IOException ex)
		{
			ex.printStackTrace();
		} 
		catch(ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		
		utils.log("Deserialized time : " + (System.currentTimeMillis() - time) + "ms");
		
		return ob;		
	}
}
