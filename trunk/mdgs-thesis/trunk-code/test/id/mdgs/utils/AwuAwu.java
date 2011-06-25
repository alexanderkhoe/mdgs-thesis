package id.mdgs.utils;


import java.io.File;
import java.io.IOException;
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
	public void test7() throws IOException{
		String name = utils.getDefaultPath() + "/resources/evaluation/khold.log";
		File f = new File(name);
		
		System.out.println(String.format("%-15s : %s","getAbsolutePath", f.getAbsolutePath()));
		System.out.println(String.format("%-15s : %s","getCanonicalPath", f.getCanonicalPath()));
		System.out.println(String.format("%-15s : %s","getName", f.getName()));
		System.out.println(String.format("%-15s : %s","getParent", f.getParent()));
		System.out.println(String.format("%-15s : %s","getPath", f.getPath()));
		System.out.println(String.format("%-15s : %s","isDirectory", f.isDirectory()));
		System.out.println(String.format("%-15s : %s","isFile", f.isFile()));
		
	}
	
//	@Test
	public void test6(){
		Test2 tmp = new Test2();
		Test2 tmp2 = new Test2();
		if(tmp instanceof Test2)
			System.out.print("Instance Test2");
		else if(tmp instanceof Test21)
			System.out.print("Instance Test21");
		
		if(tmp.getClass().getSimpleName().equals("Test2"))
			System.out.println("Instance Test2");
		else if (tmp.getClass().getSimpleName().equals("Test21"))
			System.out.println("Instance Test21");
		
		System.out.println(tmp.hashCode());
		System.out.println(tmp2.hashCode());
	}
	
	
//	@Test
	public void test5(){
		CombinationGenerator gen = new CombinationGenerator(4, 2);
		while(gen.hasMore()){
			int[] idx = gen.getNext();
			
			for(int i=0;i<idx.length;i++){
				System.out.print(idx[i] + ",");
			}
			System.out.println();
		}
//		int mask = 1;
//		mask <<= (4-1);
//		System.out.println(mask);
		
//		int N00 = 0, bitmask00 = 0,
//		N01 = 0, bitmask01 = 0,
//		N10 = 0, bitmask10 = 0,
//		N11 = 0, bitmask11 = 0, mask = 0;
//		int bitmask = 1;
//		
//		double X2 = 0;
//		int nAlgo = 4, idAlgo1 = 0, idAlgo2 = 3;
//		
//		//idAlgo1
//		for(int i=nAlgo-1; i >= 0; i--){
//			if(idAlgo1 == i ){
//				//bitmask10
//				bitmask10 |= bitmask;
//				
//				mask |= bitmask;
//			}
//	
//			if(idAlgo2 == i){
//				//bitmask01
//				bitmask01 |= bitmask;
//				
//				mask |= bitmask;
//			}
//			
//			bitmask <<= 1;
//		}
		
//		for(int i=0; i < Math.pow(2, nAlgo); i++){
//			if((mask & i) == 0)
//				System.out.print(i + ",");
//		}
//		System.out.println();
//		for(int i=0; i < Math.pow(2, nAlgo); i++){
//			if((mask & i) == bitmask01)
//				System.out.print(i + ",");
//		}
//		System.out.println();
//		for(int i=0; i < Math.pow(2, nAlgo); i++){
//			if((mask & i) == bitmask10)
//				System.out.print(i + ",");
//		}
//		System.out.println();
//		for(int i=0; i < Math.pow(2, nAlgo); i++){
//			if((mask & i) == mask)
//				System.out.print(i + ",");
//		}
//		System.out.println();
//		
//		System.out.println(bitmask00);
//		System.out.println(bitmask01);
//		System.out.println(bitmask10);
//		System.out.println(bitmask11);
	}
	
//	@Test
	public void test4(){
//		Test2 tmp = new Test2();
//		System.out.print(tmp.getClass().getSimpleName());
		
		int pos = 0;
		int bitmask = 1;
		
		int[] mat = { 0, 1, 0, 1};
		
		for(int c=mat.length - 1; c >= 0; c--){
			if(mat[c] == 1){
				pos |= bitmask;
			}
			
			bitmask <<= 1;
		}
		
		System.out.print(pos);
	}
	
	@Test
	public void test3(){
//		double num = 1.4;
//		System.out.println(Math.round(1.4));
		
//		ArrayList<Integer> test = new ArrayList<Integer>();
//		test.add(1);
//		test.add(2);
//		
//		ArrayList<Integer> ctest = (ArrayList<Integer>) test.clone();
//		Assert.assertEquals(1, (int)ctest.get(0));
//		
//		test.set(0, 3);
//		Assert.assertEquals(1, (int)ctest.get(0));
//		Assert.assertEquals(3, (int)test.get(0));
//		
//		test.clear();
//		Assert.assertEquals(0, test.size());
//		Assert.assertEquals(2, ctest.size());
	}
	
//	@Test
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
