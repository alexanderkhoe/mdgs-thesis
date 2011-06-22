package id.mdgs.dataset;

import junit.framework.Assert;
import id.mdgs.neural.data.FuzzyNode;
import id.mdgs.neural.data.NodeNeuralData;

import org.junit.Test;


public class TestNodeNeuralData {
	
	@Test
	public void testNodeNeuralData(){
		NodeNeuralData data = new NodeNeuralData(4);
		
		Assert.assertEquals(4, data.size());
		Assert.assertNotNull(data.getData());
		Assert.assertNull(data.getData()[0]);
		
		FuzzyNode[] fns = new FuzzyNode[4];

		Assert.assertNotNull(fns);
		Assert.assertNull(fns[0]);
	}
}
