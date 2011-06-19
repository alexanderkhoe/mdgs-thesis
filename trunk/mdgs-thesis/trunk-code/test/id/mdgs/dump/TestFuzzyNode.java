package id.mdgs.dump;

import id.mdgs.neural.data.FuzzyNode;

import org.junit.Assert;
import org.junit.Test;


public class TestFuzzyNode {

	@Test
	public void test1(){
		FuzzyNode node = new FuzzyNode(0, 0.5, 1);
		
		for(double x=-0.5;x<=1.5;x+=0.1){
			System.out.format("%.4f\n",node.getMaxIntersection(x));
		}
	}
}
