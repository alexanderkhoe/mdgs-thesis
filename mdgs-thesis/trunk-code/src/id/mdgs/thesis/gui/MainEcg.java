package id.mdgs.thesis.gui;

import id.mdgs.dataset.DataStatistik;
import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.HitList;
import id.mdgs.dataset.KFoldedDataset;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fnlvq.Fpglvq;
import id.mdgs.fnlvq.TrainFpglvq;
import id.mdgs.glvq.Glvq;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.gui.CodebookMonitor;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.TrainLvq1;
import id.mdgs.lvq.TrainLvq21;
import id.mdgs.master.IClassify;
import id.mdgs.master.ITrain;
import id.mdgs.utils.utils;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JScrollBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JFormattedTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;

import javax.swing.JTextArea;
import javax.swing.JRadioButton;

import org.jfree.ui.RefineryUtilities;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

/**
 * 
 * @author I Made Agus Setiawan
 *
 */
public class MainEcg extends JFrame {

	private JPanel contentPane;
	private JScrollPane scrollPane;
	private JTextArea console;
	private JFormattedTextField txtAlpha;
	private JFormattedTextField txtWindowWidth;
	private JFormattedTextField txtBeta;
	private JFormattedTextField txtGamma;
	private JFormattedTextField txtDelta;
	private JFormattedTextField txtMaxEpoch;
	private JButton btnRun;
	private JButton btnBrowse;
	private JRadioButton btnRandomYes, btnRandomNo;
	private JLabel dsLabel;
	private JLabel txtAkurasi;
	private JComboBox cbK;
	private JComboBox cbPorsi;
	
	private File dsFile = null; 
	private JComboBox cbInit;
	private JComboBox cbAlgoritma;
	
	private KFoldedDataset<Dataset, Entry> kfolds = null;
	private FoldedDataset<Dataset, Entry> trainset = null;
	private FoldedDataset<Dataset, Entry> testset = null;
	private IClassify<?, Entry> net = null;
	private ITrain trainer = null;
	
	private CodebookMonitor cbm = null;
	
	public enum Algoritma {
		FN_GLVQ, GLVQ, LVQ21, LVQ1 
	};
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					
					MainEcg frame = new MainEcg();
					//frame.setAlwaysOnTop(true);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainEcg() {
		setTitle("Main Window");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 564, 476);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);
		
		JMenu mnOption = new JMenu("Option");
		menuBar.add(mnOption);
		
		JMenuItem mntmViewCodeMonitor = new JMenuItem("View Code Monitor");
		mntmViewCodeMonitor.setEnabled(false);
		mntmViewCodeMonitor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(cbm != null)
					cbm.setVisible(true);
			}
		});
		mnOption.add(mntmViewCodeMonitor);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		
		scrollPane = new JScrollPane();
		
		JLabel lblNewLabel = new JLabel("Classification Interface");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Consolas", Font.BOLD, 20));
		
		dsLabel = new JLabel("label");
		dsLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
		setDatasetlabel("empty");
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
				.addComponent(panel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
				.addComponent(scrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
				.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
					.addComponent(dsLabel, GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
					.addGap(2)
					.addComponent(dsLabel)
					.addGap(2)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		console = new JTextArea();
		console.setToolTipText("Console");
		console.setEditable(false);
		console.setFont(new Font("Consolas", Font.PLAIN, 10));
		scrollPane.setViewportView(console);
		panel.setLayout(null);
		
		JLabel lblAlgoritma = new JLabel("Algoritma");
		lblAlgoritma.setBounds(10, 11, 46, 14);
		panel.add(lblAlgoritma);
		
		JLabel lblDataset = new JLabel("Dataset");
		lblDataset.setBounds(10, 36, 46, 14);
		panel.add(lblDataset);
		
		JLabel lblPorsiDataset = new JLabel("Porsi dataset");
		lblPorsiDataset.setToolTipText("");
		lblPorsiDataset.setBounds(10, 61, 82, 14);
		panel.add(lblPorsiDataset);
		
		cbAlgoritma = new JComboBox();
		lblAlgoritma.setLabelFor(cbAlgoritma);
		cbAlgoritma.setModel(new DefaultComboBoxModel(Algoritma.values()));
		cbAlgoritma.setBounds(102, 8, 93, 20);
		panel.add(cbAlgoritma);
		
		btnBrowse = new JButton("Load...");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();
				if(dsFile != null){
					fc.setCurrentDirectory(dsFile);
				} else {
					fc.setCurrentDirectory(new File(utils.getDefaultPath() + "/resources/ecgdata/with-header"));	
				}
				
				int returnVal = fc.showOpenDialog(MainEcg.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
		            dsFile = fc.getSelectedFile();
		            log("Using Dataset : " + dsFile.getName());
		            setDatasetlabel(dsFile.getName());
		        } else {
//		            log("Open command cancelled by user");
		        }
				
			}
		});
		btnBrowse.setBounds(102, 32, 93, 23);
		panel.add(btnBrowse);
		
		JLabel lblAlpha = new JLabel("Alpha");
		lblAlpha.setBounds(217, 11, 46, 14);
		panel.add(lblAlpha);
		
		JLabel lblWindowWidth = new JLabel("Window Width");
		lblWindowWidth.setBounds(217, 36, 82, 14);
		panel.add(lblWindowWidth);
		
		JLabel lblBeta = new JLabel("Beta");
		lblBeta.setBounds(400, 11, 46, 14);
		panel.add(lblBeta);
		
		JLabel lblGamma = new JLabel("Gamma");
		lblGamma.setBounds(400, 36, 46, 14);
		panel.add(lblGamma);
		
		JLabel lblDelta = new JLabel("Delta");
		lblDelta.setBounds(400, 61, 46, 14);
		panel.add(lblDelta);
		
		txtAlpha = new JFormattedTextField();
		txtAlpha.setText("0.05");
		txtAlpha.setHorizontalAlignment(SwingConstants.CENTER);
		txtAlpha.setBounds(300, 8, 69, 20);
		panel.add(txtAlpha);
		
		txtWindowWidth = new JFormattedTextField();
		txtWindowWidth.setText("0.005");
		txtWindowWidth.setHorizontalAlignment(SwingConstants.CENTER);
		txtWindowWidth.setBounds(300, 33, 69, 20);
		panel.add(txtWindowWidth);
		
		txtBeta = new JFormattedTextField();
		txtBeta.setText("0.00005");
		txtBeta.setHorizontalAlignment(SwingConstants.CENTER);
		txtBeta.setBounds(456, 8, 69, 20);
		panel.add(txtBeta);
		
		txtGamma = new JFormattedTextField();
		txtGamma.setText("0.00005");
		txtGamma.setHorizontalAlignment(SwingConstants.CENTER);
		txtGamma.setBounds(456, 33, 69, 20);
		panel.add(txtGamma);
		
		txtDelta = new JFormattedTextField();
		txtDelta.setText("0.1");
		txtDelta.setHorizontalAlignment(SwingConstants.CENTER);
		txtDelta.setBounds(456, 58, 69, 20);
		panel.add(txtDelta);
		
		btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setEnable(false);
				
				Thread worker = new Thread() {
					public void run(){
						
						try{
//							log("start");
							if(prepareResource()){
								train(cbm);
								test();
							}
//							Thread.sleep(2000);
						} catch (Exception ex) {
							
						}
						
						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								log("done");
								setEnable(true);
							}
						});
					}
				};
				
				worker.start();
			}
		});
		btnRun.setBounds(436, 116, 89, 23);
		panel.add(btnRun);
		
		JLabel lblMaxIteration = new JLabel("Max Epoch");
		lblMaxIteration.setBounds(217, 61, 82, 14);
		panel.add(lblMaxIteration);
		
		txtMaxEpoch = new JFormattedTextField();
		txtMaxEpoch.setText("150");
		txtMaxEpoch.setHorizontalAlignment(SwingConstants.CENTER);
		txtMaxEpoch.setBounds(300, 58, 69, 20);
		panel.add(txtMaxEpoch);
		
		cbInit = new JComboBox();
		cbInit.setModel(new DefaultComboBoxModel(new String[] {"1-5nn", "random 1-dataset", "random-eksternal", "random 0.5d-dataset", "random 1d-dataset"}));
		cbInit.setBounds(102, 106, 115, 20);
		panel.add(cbInit);
		
		JLabel lblInitBobot = new JLabel("Init bobot");
		lblInitBobot.setBounds(10, 109, 82, 14);
		panel.add(lblInitBobot);
		
		JLabel lblRandomDset = new JLabel("Random dset");
		lblRandomDset.setBounds(10, 84, 93, 14);
		panel.add(lblRandomDset);
		
		btnRandomYes = new JRadioButton("Yes");
		btnRandomYes.setBounds(102, 80, 46, 23);
		panel.add(btnRandomYes);
		
		btnRandomNo = new JRadioButton("No");
		btnRandomNo.setSelected(true);
		btnRandomNo.setBounds(154, 80, 39, 23);
		panel.add(btnRandomNo);
		contentPane.setLayout(gl_contentPane);
		
		ButtonGroup  randomGrp = new ButtonGroup();  
		randomGrp.add(btnRandomYes);
		randomGrp.add(btnRandomNo);
		
		JLabel lblK = new JLabel("K");
		lblK.setBounds(85, 61, 17, 14);
		panel.add(lblK);
		
		cbK = new JComboBox();
		cbK.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				arrangePorsi();
			}
		});
		cbK.setModel(new DefaultComboBoxModel(new String[] {"2", "5", "10"}));
		cbK.setBounds(102, 58, 37, 20);
		panel.add(cbK);
		
		cbPorsi = new JComboBox();
		cbPorsi.setToolTipText("Training Portion");
		cbPorsi.setBounds(141, 58, 52, 20);
		panel.add(cbPorsi);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panel_1.setBounds(287, 106, 82, 33);
		panel.add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		txtAkurasi = new JLabel("");
		panel_1.add(txtAkurasi, BorderLayout.CENTER);
		txtAkurasi.setHorizontalAlignment(SwingConstants.CENTER);
		txtAkurasi.setFont(new Font("Consolas", Font.BOLD, 16));
		
		cbm = createCBM();
		arrangePorsi();
	}
	
	private void arrangePorsi(){
		int K = Integer.parseInt((String) cbK.getSelectedItem());
		switch(K){
		case 2: 
			String[] aModel1 = {"0.5"};
			cbPorsi.setModel(new DefaultComboBoxModel(aModel1));
			cbPorsi.setSelectedIndex(0);
			break;
		case 5: 
			String[] aModel2 = {"0.2", "0.4", "0.6", "0.8"};
			cbPorsi.setModel(new DefaultComboBoxModel(aModel2));
			cbPorsi.setSelectedIndex(2);
			break;
		case 10: 
			String[] aModel3 = {"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9"};
			cbPorsi.setModel(new DefaultComboBoxModel(aModel3));
			cbPorsi.setSelectedIndex(4);
			break;
		}
		
		
	}
	
	private CodebookMonitor createCBM(){
		CodebookMonitor cbm = null;
		
		cbm = new CodebookMonitor(" Codebook Monitor");
		//cbm.setNetTrain(net, trainer);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(false);
		
		return cbm;
	}
	
	private void log(String msg){
		this.console.append(msg + "\n");
		
		JScrollBar sb = scrollPane.getVerticalScrollBar();
		sb.setValue( sb.getMaximum());
	}

	private void setEnable(boolean b){
		btnRun.setEnabled(b);
		btnBrowse.setEnabled(b);
		cbAlgoritma.setEnabled(b);
		cbPorsi.setEnabled(b);
		cbK.setEnabled(b);
		txtAlpha.setEnabled(b);
		txtWindowWidth.setEnabled(b);
		cbInit.setEnabled(b);
		txtBeta.setEnabled(b);
		txtGamma.setEnabled(b);
		txtDelta.setEnabled(b);
		txtMaxEpoch.setEnabled(b);
	}
	
	private void train(CodebookMonitor cbm){
		this.trainer.reset();
		this.trainer.setNetwork(this.net);
		
		cbm.setNetTrain(net, trainer);
		cbm.setTitle(net.getClass().getSimpleName() + " Codebook Monitor");
		this.setAlwaysOnTop(true);
		
		log("Training...");
		log(this.trainer.information());
		
		do{
			this.trainer.iteration();
			log("Epoch: " + this.trainer.getCurrEpoch() + " -> error: " + this.trainer.getError());
			
			if(cbm != null)
				cbm.update(this.trainer.getCurrEpoch(), this.trainer.getError());
			
		}while(!this.trainer.shouldStop());
		
		this.setAlwaysOnTop(false);
	}
	
	private void test(){
		log("Testing...");
		
		//cek jumlah kelas
		HitList hl = new HitList();
		if(this.net.getCodebook().getClass().getSimpleName().equals("Dataset"))
			hl.run((Dataset)this.net.getCodebook());
		else 
			hl.run((FCodeBook)this.net.getCodebook());
		
		ConfusionMatrix cm = new ConfusionMatrix(hl.size());
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
		}
		
		txtAkurasi.setText(String.format("%5.2f%%", cm.getAccuracy()*100));
		
		log(cm.toString());
//		log("Test Result :");
//		log(String.format("True : %d", cm.getTruePrediction()));
//		log(String.format("Total: %d", cm.getTotal()));
//		log(String.format("Accuracy = %.4f",cm.getAccuracy()));
	}
	
	
	private boolean prepareResource(){
		//create folding data
		log("loading dataset ..." + dsFile.getAbsolutePath());
		Dataset dset = new Dataset(dsFile.getAbsolutePath());
		dset.load();
		
		int K = Integer.parseInt((String)cbK.getSelectedItem());
		double porsi = Double.parseDouble((String)cbPorsi.getSelectedItem());
		
		log("Folding data...");
		this.kfolds 	= new KFoldedDataset<Dataset, Dataset.Entry>(dset, K, porsi, btnRandomYes.isSelected());
		this.trainset 	= kfolds.getKFoldedForTrain(0);
		this.testset	= kfolds.getKFoldedForTest(0);
		
		DataStatistik ds = new DataStatistik();
		log("Statistik trainset");
		ds.setData(trainset);
		log(ds.toString());
		log("Statistik testset");
		ds.setData(testset);
		log(ds.toString());
		
		log("Create Network...");
		//create network
		this.net = this.createNetwork((Algoritma)cbAlgoritma.getSelectedItem());
		
		log("Create Trainer...");
		this.trainer = this.createTrainer((Algoritma)cbAlgoritma.getSelectedItem());
		
		txtAkurasi.setText("");
		
		//show confirmation
		if(JOptionPane.showConfirmDialog(null, "Continue running?", "", JOptionPane.OK_CANCEL_OPTION) == 0)
			return true;
		else
			return false;
	}
	
	private void setDatasetlabel(String msg){
		dsLabel.setText(String.format("Dataset: %s", msg));
	}
	
	private IClassify<?, Entry> createNetwork(Algoritma algoritma){
		IClassify<?, Entry> net = null;
		
		String init = (String) cbInit.getSelectedItem();
		
		if(algoritma == Algoritma.GLVQ || 
			algoritma == Algoritma.LVQ1 || 
			algoritma == Algoritma.LVQ21 ){
			
			if(algoritma == Algoritma.GLVQ){
				net = new Glvq();

			} else {
				net = new Lvq();
			}

			if(init.equals("1-5nn")){
				((Lvq)net).initStaticCodes(trainset, 1, 5);
			} else if(init.equals("random-eksternal")) {
				((Lvq)net).initCodes(trainset, 0d, 1d, 1);
			}
			else {
				((Lvq)net).initCodes(trainset, 1);
			}
			
		} else if(algoritma == Algoritma.FN_GLVQ){
			net = new Fpglvq();

			if(init.equals("random 1d-dataset")){
				((Fpglvq)net).initCodes(trainset, 1d, false);
			} else if(init.equals("random-eksternal")) {
				((Fpglvq)net).initCodes(trainset, 0d, 1d);
			} else {
				((Fpglvq)net).initCodes(trainset, 0.5d, true);
			}
			
		}
		
		return net;
	}
	
	private ITrain createTrainer(Algoritma algoritma){
		ITrain trainer = null;
		
		double alpha = Double.parseDouble(txtAlpha.getText());
		double window= Double.parseDouble(txtWindowWidth.getText());
		double beta	 = Double.parseDouble(txtBeta.getText());
		double gamma = Double.parseDouble(txtGamma.getText());
		double delta = Double.parseDouble(txtDelta.getText());
		int maxEpoch = Integer.parseInt(txtMaxEpoch.getText());
		
		switch(algoritma){
		case LVQ1:
			trainer = new TrainLvq1(trainset, alpha);
			break;
		case LVQ21: 
			trainer = new TrainLvq21(trainset, alpha, window);
			break;
		case GLVQ: 
			trainer = new TrainGlvq(trainset, alpha);
			break;
		case FN_GLVQ: 
			trainer = new TrainFpglvq(trainset, alpha, beta, gamma, delta);
			break;
		}
		
		trainer.setMaxEpoch(maxEpoch);
		
		return trainer;
	}
}
