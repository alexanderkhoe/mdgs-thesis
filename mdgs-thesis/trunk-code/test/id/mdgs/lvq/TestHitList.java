package id.mdgs.lvq;

import java.io.IOException;

import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.utils.utils;
import junit.framework.Assert;

import org.junit.Test;


public class TestHitList {

	@Test
	public void testHitList(){
		HitList hl = new HitList();
		HitList.HitEntry he;
		
		hl.addHit(0);
		hl.addHit(1);
		Assert.assertEquals(0, hl.get(0).label);
		
		hl.addHit(1);
		Assert.assertEquals(1, hl.get(0).label);
		
		hl.addHit(2);
		hl.addHit(2);
		Assert.assertEquals(2, hl.get(1).label);
		
		hl.addHit(3);
		hl.addHit(3);
		hl.addHit(3);
		Assert.assertEquals(3, hl.get(0).label);
		Assert.assertEquals(1, hl.get(1).label);
		Assert.assertEquals(2, hl.get(2).label);
		Assert.assertEquals(0, hl.get(3).label);
		
		utils.log(hl.toString());
		
		Assert.assertNull(hl.get(4));
	}
	
	@Test
	public void testRealData() throws IOException{
		LvqUtils.resetLabels();
		Dataset dataset = new Dataset(utils.getDefaultPath() + "/resources/trash/wlet.train86.txt");
		dataset.load();
		utils.log(dataset.toString());		

		HitList hl = new HitList();
		long start = System.currentTimeMillis();
		for(Entry e: dataset){
			hl.addHit(e.label);
		}
		long diff = System.currentTimeMillis() - start;
		utils.log(Long.toString(diff) + " miliseconds");		
		
		utils.log(hl.toString());
		utils.log(hl.toString(LvqUtils.getLabels()));
	}
}
