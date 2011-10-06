/**
 * 
 */
package id.mdgs.dump;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.encog.engine.data.EngineData;
import org.encog.engine.data.EngineDataSet;
import org.encog.engine.util.EngineArray;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataPair;
import org.encog.neural.data.folded.FoldedDataSet;
import org.encog.neural.networks.training.TrainingError;

/**
 * @author I Made Agus Setiawan
 *
 */
public class FoldedDataSet implements NeuralDataSet {

	public static final String METHOD_NOT_SUPPORTED = "Method is not supported.";
	private final NeuralDataSet owner;
	private int foldSize;
	private int numFolds;
	private int currentFold;
	private Map<Integer, List<Integer>> foldedMap;
	private Map<Integer, List<Integer>> classTable;
	
	/**
	 * 
	 */
	public FoldedDataSet(final NeuralDataSet owner) {
		this.owner = owner;
		this.classTable = this.getClassTable(owner);
	}

	private int decodeTarget(NeuralData ideal){
		if(ideal.getData().length == 1){
			return (int) ideal.getData()[0];
		} else {
			return EngineArray.maxIndex(ideal.getData());
		}
	}	
	
	private Map<Integer,List<Integer>> getClassTable(final NeuralDataSet set){
		Map<Integer,List<Integer>> output = new HashMap<Integer,List<Integer>>();
		
		for(int i=0; i< set.getRecordCount(); i++){
			NeuralDataPair pair = BasicNeuralDataPair.createPair(
					set.getInputSize(), set.getIdealSize());
			set.getRecord(i, pair);
			int cat = this.decodeTarget(pair.getIdeal());
			List<Integer> list;
			
			if(!output.containsKey(cat)){
				output.put(cat, new ArrayList<Integer>());
			}
			list = output.get(cat);
			list.add(i);
			pair = null;
		}
		
		return output;
	}
	
	public void fold(final int numFolds){
		//find max number of data in each class
		int maxCount = Integer.MIN_VALUE;
		for(Integer cat: this.classTable.keySet()){
			if(maxCount < this.classTable.get(cat).size()){
				maxCount = this.classTable.get(cat).size();
			}
		}
		
		this.numFolds = (int) Math.min(numFolds, maxCount);
		
		this.foldSize = (int) (this.underlying.getRecordCount() / this.numFolds);
		this.lastFoldSize = (int) (this.underlying.getRecordCount() - (this.foldSize * this.numFolds));
		setCurrentFold(0);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<NeuralDataPair> iterator() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.encog.engine.data.EngineDataSet#getRecord(long, org.encog.engine.data.EngineData)
	 */
	@Override
	public void getRecord(long index, EngineData pair) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.encog.engine.data.EngineDataSet#getRecordCount()
	 */
	@Override
	public long getRecordCount() {
		
	}

	/* (non-Javadoc)
	 * @see org.encog.engine.data.EngineDataSet#openAdditional()
	 */
	@Override
	public EngineDataSet openAdditional() {
		throw new TrainingError(FoldedDataSet.METHOD_NOT_SUPPORTED);
	}

	/* (non-Javadoc)
	 * @see org.encog.neural.data.NeuralDataSet#add(org.encog.neural.data.NeuralData)
	 */
	@Override
	public void add(NeuralData arg0) {
		throw new TrainingError(FoldedDataSet.METHOD_NOT_SUPPORTED);

	}

	/* (non-Javadoc)
	 * @see org.encog.neural.data.NeuralDataSet#add(org.encog.neural.data.NeuralDataPair)
	 */
	@Override
	public void add(NeuralDataPair arg0) {
		throw new TrainingError(FoldedDataSet.METHOD_NOT_SUPPORTED);

	}

	/* (non-Javadoc)
	 * @see org.encog.neural.data.NeuralDataSet#add(org.encog.neural.data.NeuralData, org.encog.neural.data.NeuralData)
	 */
	@Override
	public void add(NeuralData arg0, NeuralData arg1) {
		throw new TrainingError(FoldedDataSet.METHOD_NOT_SUPPORTED);

	}

	/* (non-Javadoc)
	 * @see org.encog.neural.data.NeuralDataSet#close()
	 */
	@Override
	public void close() {
		this.owner.close();
	}

	/* (non-Javadoc)
	 * @see org.encog.neural.data.NeuralDataSet#getIdealSize()
	 */
	@Override
	public int getIdealSize() {
		return this.owner.getIdealSize();
	}

	/* (non-Javadoc)
	 * @see org.encog.neural.data.NeuralDataSet#getInputSize()
	 */
	@Override
	public int getInputSize() {
		this.getInputSize();
	}

	/* (non-Javadoc)
	 * @see org.encog.neural.data.NeuralDataSet#isSupervised()
	 */
	@Override
	public boolean isSupervised() {
		this.owner.isSupervised();
	}
}
