package id.mdgs.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.fnlvq.Fnlvq;
import id.mdgs.fnlvq.Fpglvq;
import id.mdgs.glvq.Glvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.master.IClassify;
import id.mdgs.master.ITrain;


/**
 * Mekanisme
 * 1. Cek apa sudah ada bobot di folder path, jika belum, 
 *    training classifier, ini harus dilakukan manual 
 *    
 * @author I Made Agus Setiawan
 *
 */
public class McNemarTest {

	private String protoType = "prototype";
	private File directory;
	
	private Map<String, ITrain> trainers;
	private List<IClassify<?, ?>> classifiers;
	
	public IOMatrix ioMat;
	
	public McNemarTest(String path){
		initPath(path);
		
		trainers 	= new HashMap<String, ITrain>();
		classifiers = new ArrayList<IClassify<?, ?>>();
	}
	
	private void initPath(String path){
		directory = new File(path);
		if(!directory.exists()){
			if(!directory.mkdir()){
				if(!directory.mkdirs()){
					throw new RuntimeException("Can't make directory: " + path);
				}
			}
		}
	}
	
	public void reset(){
		trainers.clear();
		classifiers.clear();
	}
	
	//key 
	public void registerTrainer(String key, ITrain trainer){
		assert(trainer != null);
		
		trainers.put(key, trainer);
	}
	
	public void registerClassifier(String key, IClassify<?, ?> classifier){
		if(!trainers.containsKey(key)){
			throw new RuntimeException("Trainer is not registered yet.");
		}
			
		String protoName = key + "." + protoType;
		protoName = directory.getAbsoluteFile() + "/" + protoName;
		
		if(!checkAvailablePrototype(protoName)){
			trainClassifier(key, classifier);
			
			//save prototype
			classifier.saveCodebook(protoName);
		} else {
			//load prototype
			classifier.loadCodebook(protoName);
		}

		classifiers.add( classifier);
	}
	
	private void trainClassifier(String key, IClassify<?, ?> classifier){
		//get trainer
		ITrain train = trainers.get(key);
		train.setNetwork(classifier);
		
		do {
			train.iteration();
		}while(!train.shouldStop());
	}
	
	public boolean checkAvailablePrototype(String protoName){
		File proto = new File(protoName);
		if(proto.exists() && proto.isFile())
			return true;
		return false;
	}
	
	public String run (FoldedDataset<?, ?> testset){
		//check all 
		if(trainers.isEmpty() || classifiers.isEmpty()){
			throw new RuntimeException("trainer is empty or classifier is empty.");
		}
		
		ioMat = new IOMatrix(testset.size(), classifiers.size(), new File(directory.getAbsoluteFile() + "/report.log"));

		int row = 0;
		
		// TODO harusnya tidak perlu dicasting sample-nya
		for(Object sample: testset){
			int col = 0;
			for(IClassify<?, ?> net : classifiers){
				int win = -1, target;
				
				target = ((Entry) sample).label;
				if(net.getClass().getSimpleName().equals("Lvq")){
					win = ((Lvq) net).classify((Entry)sample);
				}else if(net.getClass().getSimpleName().equals("Glvq")){
					win = ((Glvq) net).classify((Entry)sample);
				} else if(net.getClass().getSimpleName().equals("Fpglvq")) {
					win = ((Fpglvq) net).classify((Entry)sample);
				} else if(net.getClass().getSimpleName().equals("Fnlvq")) {
					win = ((Fnlvq) net).classify((Entry)sample);
				}
				
				ioMat.feed(row, col, (win == target));
				col++;
			}
			
			row++;
		}
		
		//calculate ioMat
		ioMat.summarized();
		
		try {
			ioMat.logAll();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return this.toString();
	}
	
	public String toString(){
		if(ioMat == null) return "";
		
		return ioMat.toString();
	}
	
}
