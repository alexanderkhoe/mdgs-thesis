/**
 * 
 */
package id.mdgs.dataset;

import id.mdgs.neural.data.FuzzyNode;
import id.mdgs.neural.data.Node;
import junit.framework.Assert;

import org.junit.Test;


/**
 * @author I Made Agus Setiawan
 *
 */
public class TestFuzzyNode {
	public static double[][] bobot = {
		{3, 4, 5},
		{3, 4.5, 8},
		{4, 5, 6},
		{5, 7, 9},
		{6, 8, 9},
		{4, 5, 6},
		{2, 6, 8}
	};
	public static double[][] data = {
		{6, 8, 9},
		{5, 7, 9},
		{4, 5, 6},
		{3, 4.5, 8},
		{3, 4, 5},
		{2, 6, 8},
		{4, 5, 6}
	};
	
	public static double[] result = {0, 0.55, 1, 0.55, 0, 0.8, 0.8};
	@Test
	public void testClass(){
		Node n = new FuzzyNode();
		
		Assert.assertTrue(n.getClass() == FuzzyNode.class);
	}
	
	@Test
	public void testMaxIntersection(){
		for(int i=0;i<bobot.length;i++){
			FuzzyNode fn1 = new FuzzyNode(bobot[i]);
			FuzzyNode fn2 = new FuzzyNode(data[i]);
			
			double miu = fn1.getMaxIntersection(fn2);

			Assert.assertEquals(result[i], miu, 0.01);
		}
	}
}
