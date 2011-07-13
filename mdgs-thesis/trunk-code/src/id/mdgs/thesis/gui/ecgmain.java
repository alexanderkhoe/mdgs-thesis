package id.mdgs.thesis.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import java.awt.Color;
import javax.swing.JTextPane;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTextField;

/**
 * 
 * @author I Made Agus Setiawan
 *
 */
public class ecgmain extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JPanel jPanel = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JScrollPane jScrollPane = null;
	private JEditorPane jEditorPane = null;
	private JLabel jLabel2 = null;
	private JComboBox jComboBox = null;
	private JLabel jLabel21 = null;
	private JLabel jLabel22 = null;
	private JLabel jLabel221 = null;
	private JButton jButton = null;
	private JTextField jTextField = null;
	private JLabel jLabel211 = null;
	private JLabel jLabel2111 = null;
	private JTextField jTextField1 = null;
	private JComboBox jComboBox1 = null;
	private JLabel jLabel23 = null;
	private JTextField jTextField2 = null;
	private JLabel jLabel24 = null;
	private JButton jButton1 = null;
	/**
	 * This is the default constructor
	 */
	public ecgmain() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(656, 434);
		this.setContentPane(getJContentPane());
		this.setTitle("ECG main window");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(331, 60, 304, 16));
			jLabel1.setFont(new Font("Consolas", Font.PLAIN, 12));
			jLabel1.setText("Output Console");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(7, 4, 630, 52));
			jLabel.setFont(new Font("Consolas", Font.BOLD, 24));
			jLabel.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel.setText("Simulasi Pengenalan Beat Arrhytmia");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJPanel(), null);
			jContentPane.add(jLabel, null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(getJScrollPane(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jLabel24 = new JLabel();
			jLabel24.setBounds(new Rectangle(11, 137, 138, 35));
			jLabel24.setText("K :");
			jLabel24.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel24.setFont(new Font("Consolas", Font.PLAIN, 12));
			jLabel23 = new JLabel();
			jLabel23.setBounds(new Rectangle(11, 171, 137, 28));
			jLabel23.setText("Proporsi Train :");
			jLabel23.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel23.setFont(new Font("Consolas", Font.PLAIN, 12));
			jLabel2111 = new JLabel();
			jLabel2111.setBounds(new Rectangle(20, 256, 99, 15));
			jLabel2111.setText("Window width :");
			jLabel2111.setVerticalAlignment(SwingConstants.CENTER);
			jLabel2111.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel2111.setFont(new Font("Consolas", Font.PLAIN, 12));
			jLabel211 = new JLabel();
			jLabel211.setBounds(new Rectangle(20, 235, 98, 16));
			jLabel211.setText("Alpha :");
			jLabel211.setVerticalAlignment(SwingConstants.CENTER);
			jLabel211.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel211.setFont(new Font("Consolas", Font.PLAIN, 12));
			jLabel221 = new JLabel();
			jLabel221.setBounds(new Rectangle(11, 90, 288, 48));
			jLabel221.setText("File : empty");
			jLabel221.setHorizontalTextPosition(SwingConstants.LEADING);
			jLabel221.setVerticalAlignment(SwingConstants.TOP);
			jLabel221.setFont(new Font("Consolas", Font.PLAIN, 12));
			jLabel22 = new JLabel();
			jLabel22.setBounds(new Rectangle(11, 62, 144, 22));
			jLabel22.setText("Dataset :");
			jLabel22.setFont(new Font("Consolas", Font.PLAIN, 12));
			jLabel21 = new JLabel();
			jLabel21.setBounds(new Rectangle(13, 212, 125, 18));
			jLabel21.setText("Parameter :");
			jLabel21.setVerticalAlignment(SwingConstants.TOP);
			jLabel21.setFont(new Font("Consolas", Font.BOLD, 12));
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(11, 27, 144, 35));
			jLabel2.setFont(new Font("Consolas", Font.PLAIN, 12));
			jLabel2.setText("Classifier :");
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setBounds(new Rectangle(4, 60, 324, 331));
			jPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			jPanel.add(jLabel2, null);
			jPanel.add(getJComboBox(), null);
			jPanel.add(jLabel21, null);
			jPanel.add(jLabel22, null);
			jPanel.add(jLabel221, null);
			jPanel.add(getJButton(), null);
			jPanel.add(getJTextField(), null);
			jPanel.add(jLabel211, null);
			jPanel.add(jLabel2111, null);
			jPanel.add(getJTextField1(), null);
			jPanel.add(getJComboBox1(), null);
			jPanel.add(jLabel23, null);
			jPanel.add(getJTextField2(), null);
			jPanel.add(jLabel24, null);
			jPanel.add(getJButton1(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBounds(new Rectangle(331, 77, 305, 313));
			jScrollPane.setViewportView(getJEditorPane());
			jScrollPane.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			jScrollPane.setAutoscrolls(true);
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jEditorPane	
	 * 	
	 * @return javax.swing.JEditorPane	
	 */
	private JEditorPane getJEditorPane() {
		if (jEditorPane == null) {
			jEditorPane = new JEditorPane();
			jEditorPane.setFont(new Font("Consolas", Font.PLAIN, 10));
		}
		
		return jEditorPane;
	}

	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox();
			jComboBox.setBounds(new Rectangle(155, 27, 144, 29));
			
			jComboBox.addItem("FN-GLVQ");
			jComboBox.addItem("GLVQ");
			jComboBox.addItem("LVQ21");
			jComboBox.addItem("LVQ1");
		}
		return jComboBox;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new Rectangle(155, 60, 144, 24));
			jButton.setText("Load");
		}
		return jButton;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setBounds(new Rectangle(120, 233, 85, 21));
		}
		return jTextField;
	}

	/**
	 * This method initializes jTextField1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new JTextField();
			jTextField1.setBounds(new Rectangle(120, 255, 85, 20));
		}
		return jTextField1;
	}

	/**
	 * This method initializes jComboBox1	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBox1() {
		if (jComboBox1 == null) {
			jComboBox1 = new JComboBox();
			jComboBox1.setBounds(new Rectangle(158, 171, 139, 28));
			
			for(double i=0.9;i > 0.1;i-=0.1){
				jComboBox1.addItem(String.format("%3.1f", i));	
			}
			
		}
		return jComboBox1;
	}

	/**
	 * This method initializes jTextField2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField2() {
		if (jTextField2 == null) {
			jTextField2 = new JTextField();
			jTextField2.setBounds(new Rectangle(157, 138, 141, 31));
			jTextField2.setText("10");
			jTextField2.setHorizontalAlignment(JTextField.CENTER);
		}
		return jTextField2;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setBounds(new Rectangle(181, 296, 116, 23));
			jButton1.setText("Code Monitor");
		}
		return jButton1;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
