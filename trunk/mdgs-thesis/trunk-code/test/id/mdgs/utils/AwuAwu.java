package id.mdgs.utils;


import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import id.mdgs.matrices.WeightMatrix;
import id.mdgs.neural.data.FuzzyNode;
import id.mdgs.neural.data.Node;

import org.encog.mathutil.matrices.Matrix;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.junit.Test;


public class AwuAwu {
	double dTest;

	abstract class Tes1 {
		Object weights;
		public Tes1(){
			System.out.println("Tes1 Constructor");
		}
		
//		public Tes1(int in){
//			System.out.println("Tes1("+in+") Constructor");
//		}
		
		public abstract void show();
	}
	class Fuzzy extends Object{
		double min, mean, max;
	}
	
	class Tes11 extends Tes1 {
		public Tes11(){
			dTest = 1;
			System.out.println("Tes11 Constructor " + dTest);
			weights = new Fuzzy[3];
		}
		
		public void show(){
			if (weights instanceof Fuzzy[]){
				for(Fuzzy f: (Fuzzy[]) weights){
					System.out.println(f.min + f.mean + f.max);
				}				
			} else {
				System.out.println("false");
			}
		}
		
	
		protected void testOverride(){
			System.out.println("testOverride: Test11");
		}
	}
	
	class Tes12 extends Tes11 {
		public Tes12(){
			super();
			System.out.println("Tes12 Constructor");
		}

		@Override
		public void show() {
		}
		
		@Override
		protected void testOverride(){
			System.out.println("testOverride: Test12");
		}		
	}
	
	class Test2 {
		public Test2(){
			System.out.println("Tes2 Constructor");
		}
		
		public void testOverride(){
			System.out.println("Tes2 testOverride()");
		}
		
		public void testing(){
			this.testOverride();
		}
		
		public void iteration(){
			System.out.println("Test2.iteration");
			this.update();
		}
		
		public void update(){
			System.out.println("Test2.update");
		}
	}
	
	class Test21 extends Test2 {
		public Test21(){
			System.out.println("Tes21 Constructor");
		}
		
		@Override
		public void testOverride(){
			System.out.println("Tes21 testOverride()");
		}
		
		@Override
		public void iteration(){
			System.out.println("Test21.preprocess");
			super.iteration();
			System.out.println("Test21.postprocess");
		}
		
		@Override
		public void update(){
			super.update();
			System.out.println("Test21.update");
		}
	}
	

	class Test3 {
		public List<String> list = new ArrayList<String>();
		public Test3(){
			list.add("NOL");
			list.add("SATU");
			list.add("DUA");
			list.add("TIGA");
		}
	}
	
	@Test
	public void test2(){
		System.out.println(Math.pow(4, -(double)1/2));
		System.out.println(Math.pow(4.0, -0.5));
		System.out.println(Math.pow(4.0, -1.0/2));
	}
	
//	@Test
	public void testReference(){
		Test3 t = new Test3();
		String s = t.list.get(1);
		t.list.clear();
		t = null;
		Assert.assertNotNull(s);
		utils.log(s);
	}
	
//	@Test
	public void testJavaPointer(){
		Test21 t21 = new Test21();
		Test2 t2;
		
		t2 = t21;
		System.out.println(t2);
		System.out.println(t21);
		Assert.assertEquals(t2, t21);
	}
	
//	@Test
	public void TestOverride(){
//		Test2 test = new Test21();
//		test.testing();
		Test2 t2 = new Test21();
		t2.iteration();
	}
	
//	@Test
	public void TestInherit(){
//		Tes1 t1 = new Tes11();
//		Tes11 t2 = new Tes12();
//		FNLVQ f1 = new FNLVQ();
//		t2.show();
//		t2.testOverride();
		

	}
	
//	@Test
	public void TestObjectCreation(){
		WeightMatrix matrix = new WeightMatrix(2, 2);
		
		System.out.println("After create the matrix");
		for(int y=0;y<matrix.getRows();y++){
			for(int x=0;x<matrix.getCols();x++){
				System.out.println(matrix.get(y,x));
			}
		}
		
		System.out.println("Change only one cell content");
		FuzzyNode fn = (FuzzyNode) matrix.get(1, 1);
		fn.max = 100;
		//matrix.set(1, 1, fn);
		//matrix.get(1, 0).max = 50;		
		for(int y=0;y<matrix.getRows();y++){
			for(int x=0;x<matrix.getCols();x++){
				System.out.println(matrix.get(y,x));
			}
		}		
	}
	
//	@Test
	public void TestEncogMatrix(){
		Matrix matrix = new Matrix(3,3);
		matrix.clear();
		
		System.out.println("Change only one cell content");
		matrix.set(1, 1, 2);
		for(int y=0;y<matrix.getRows();y++){
			for(int x=0;x<matrix.getCols();x++){
				System.out.println(matrix.get(y,x));
			}
		}
		System.out.println();
		for(int x=0;x<matrix.getData()[1].length;x++){
			System.out.println(matrix.getData()[1][x]);
		}
		
	}
}
