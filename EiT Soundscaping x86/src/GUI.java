import gnu.io.CommPortIdentifier;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

public class GUI extends JFrame implements Constants {
	
	public static GUI theGUI;
	
	private static final long serialVersionUID = 1L;

	private JPanel north;
	
	private Receiver reciever;
	private TrashCan[] trashCans;
	
	public List<TrashCan> getTrashCans() {
		return Arrays.asList(trashCans);
	}

	private SpecialClipContainer foo;
	
	private HashMap<String, CommPortIdentifier> portMap;
	private JLabel selectedPortLabel;
	private JComboBox<String> commPortBox;
	private JButton refreshButton;
	private JButton connectButton;
	
	private JTextArea textArea;
	
	public GUI() {
		theGUI = this;
		north = new JPanel();
		north.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		portMap = new HashMap<String, CommPortIdentifier>();
		
		selectedPortLabel = new JLabel("The selected port:");
		c.gridx = c.gridy = 0;
		north.add(selectedPortLabel);
		c.gridx = 1;
		north.add(Box.createRigidArea(new Dimension(5,0)), c);
		c.gridy = 1; c.gridx = 0;
		commPortBox = new JComboBox<String>();
		commPortBox.setPrototypeDisplayValue("COM PORT XXXX");
		north.add(commPortBox, c);
		
		c.gridx = 1;
		north.add(Box.createRigidArea(new Dimension(5,0)), c);
		
		refreshButton = new JButton("Search for ports");
		refreshButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				searchForPorts();
			}
		});
		
		c.gridx = 2;
		north.add(refreshButton, c);
		
		connectButton = new JButton("Connect");
		JPanel textPanel = new JPanel();
		textArea = new JTextArea(10, 30);
		textArea.setEditable(false);
		textArea.setBackground(Color.LIGHT_GRAY);
		JScrollPane areaScrollPane = new JScrollPane(textArea);
		areaScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		textPanel.add(areaScrollPane);
		
		connectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				commPortBox.setEnabled(false);
				refreshButton.setEnabled(false);
				connectButton.setEnabled(false);
				TrashCan bottulf = new TrashCan(BOTTULF_PATH);
				TrashCan botteline = new TrashCan(BOTTELINE_PATH);
				TrashCan bottbert = new TrashCan(BOTTBERT_PATH);
				trashCans = new TrashCan[3];
				trashCans[0] = bottulf; trashCans[1] = botteline; trashCans[2] = bottbert;
				HashMap<String, TrashCan> map = new HashMap<String, TrashCan>();
				map.put(bottulf.getName(), bottulf);
				map.put(botteline.getName(), botteline);
				map.put(bottbert.getName(), bottbert);
				foo = new SpecialClipContainer(SPECIAL_CLIPS_PATH, map);
				
				SpontaneousLogic ss = new SpontaneousLogic(Arrays.asList(trashCans), foo);
				Thread t = new Thread(ss);
				t.start();
				
//				TestGui testGui = new TestGui(trashCans, ss);
				
				Receiver r = new Receiver(portMap.get(commPortBox.getSelectedItem()), trashCans, ss);
				r.toString();
			}
		});
		
		searchForPorts();
		
		getContentPane().setLayout(new GridBagLayout());
		c.gridx = c.gridy = 0;
		getContentPane().add(north, c);
		c.gridy = 1;
		getContentPane().add(Box.createRigidArea(new Dimension(0,5)), c);
		c.gridy = 2;
		getContentPane().add(connectButton, c);
		c.gridy = 3;
		getContentPane().add(Box.createRigidArea(new Dimension(0,5)), c);
		c.gridy = 4;
		getContentPane().add(areaScrollPane, c);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(reciever != null) reciever.disconnect();
				if(trashCans != null) {
					for(TrashCan tc : trashCans)
					tc.release();
				}
				if(foo != null) foo.release();
				System.exit(0);
			}
		});
		
		setTitle("Select Comm Port");
		setSize(380, 300);
		setLocation(200, 200);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setVisible(true);
		Globals.getInstance();
	}
	
	public void displayText(String s) {
//		textArea.append(s+"\n");
		textArea.setText(textArea.getText()+s+"\n");
	}
	
	private void searchForPorts() {
		portMap.clear();
		commPortBox.removeAllItems();
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portIdentifiers = (Enumeration<CommPortIdentifier>)CommPortIdentifier.getPortIdentifiers();
		if(!portIdentifiers.hasMoreElements()) {
			connectButton.setEnabled(false);
			textArea.append("Found no ports!\n");
		}
		else
			connectButton.setEnabled(true);
		while(portIdentifiers.hasMoreElements()) {
			CommPortIdentifier pid = portIdentifiers.nextElement();
			if(pid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portMap.put(pid.getName(), pid);
				commPortBox.addItem(pid.getName());
			}
		}
	}
	
	public static void main(String[] args) {
		GUI gui = new GUI();
		gui.toString();
	}

}
