package id.mdgs.utils;

import java.util.StringTokenizer;

import org.junit.Test;

/**
 * Java StringTokenizer - Specify Delimiter example.
 * This example shows how a specify a delimiter for StringTokenizer object.
 * The default delimiters are
 * \t character (tab),
 * \n character (new line),
 * \r character (carriage return) and
 * \f character (form feed).
 * 
 */
public class TestStringToken {

	@Test
	public void testStringToken(){
		   /*
	     * There are two ways to specify a delimiter for a StringTokenizer object.
	     * 1. At the creating time by specifying in the StringTokenizer constructor
	     * 2. Specify it in nextToken() method
	     */
		
		 //1. Using StringTokenizer constructor
	    StringTokenizer st1 = new StringTokenizer("Java|StringTokenizer|Example 1", "|");
	 
	    //iterate through tokens
	    while(st1.hasMoreTokens())
	      System.out.println(st1.nextToken());
	 
	    //2. Using nextToken() method. Note that the new delimiter set remains the
	    //default after this method call
	    StringTokenizer st2 = new StringTokenizer("Java|StringTokenizer|Example 2");
	 
	    //iterate through tokens
	    while(st2.hasMoreTokens())
	      System.out.println(st2.nextToken("|"));
	    
	    //3
	    StringTokenizer st3 = new StringTokenizer("Java|StringTokenizer|Example\t3");
		 
	    //iterate through tokens
	    while(st3.hasMoreTokens())
	      System.out.println(st3.nextToken("|\t"));
	}
	
	@Test
	public void testStringTokenWithdouble(){
		String str = "0.18024	-0.073131	0.038729	-0.16646	4.5998e-005	2";
	    StringTokenizer st3 = new StringTokenizer(str);
		 
	    //iterate through tokens
	    while(st3.hasMoreTokens())
	      System.out.println(st3.nextToken("|\t,"));
	}
	
	@Test
	public void testStringTokenWithdouble2(){
		String str = "5.1,3.5,1.4,0.2,\"class1\"";
	    StringTokenizer st3 = new StringTokenizer(str);
		 
	    //iterate through tokens
	    while(st3.hasMoreTokens())
	      System.out.println(st3.nextToken("|\t,"));
	    
	    
	}
}
