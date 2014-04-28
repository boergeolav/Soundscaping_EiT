import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


public class TestGui extends JFrame implements Constants {
	
	private static final long serialVersionUID = 1L;

	private Random generator;
	
	private SpontaneousLogic ss;
	
	private TrashCan bottulf;
	private TrashCan botteline;
	private TrashCan bottbert;
	
	private JPanel main;
	
	private JLabel bottulfLabel;
	private JLabel bottelineLabel;
	private JLabel bottbertLabel;
	
	private JButton bottulfButton;
	private JButton bottelineButton;
	private JButton bottbertButton;
	
	public TestGui(TrashCan[] trashCans, SpontaneousLogic ss) {
		generator = new Random(System.currentTimeMillis());
		this.ss = ss;
		
		main = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		main.setLayout(new GridBagLayout());
		
		// Buttons and labels
		bottulfLabel = new JLabel("Bøttulf:   ");
		bottelineLabel = new JLabel("Bøtteline:   ");
		bottbertLabel = new JLabel("Bøttbert:   ");
		
		c.gridx = c.gridy = 0;
		main.add(bottulfLabel, c);
		c.gridy = 1;
		main.add(bottelineLabel, c);
		c.gridy = 2;
		main.add(bottbertLabel, c);
		
		bottulf = trashCans[0];
		botteline = trashCans[1];
		bottbert = trashCans[2];
		
		bottulfButton = new JButton("Throw trash!");
		bottulfButton.addActionListener(new ButtonListener(bottulf));
		bottelineButton = new JButton("Throw trash!");
		bottelineButton.addActionListener(new ButtonListener(botteline));
		bottbertButton = new JButton("Throw trash!");
		bottbertButton.addActionListener(new ButtonListener(bottbert));
		
		c.gridy = 0;
		c.gridx = 1;
		main.add(bottulfButton, c);
		c.gridy = 1;
		main.add(bottelineButton, c);
		c.gridy = 2;
		main.add(bottbertButton, c);
		
		getContentPane().add(main);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});
		
		setTitle("Trash can test");
		setSize(200, 150);
		setLocation(200, 200);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setVisible(true);
	}
	
	private class ButtonListener implements ActionListener {

		private TrashCan trashCan;
		private TrashCan[] otherTrashCans;
		
		public ButtonListener(TrashCan trashCan) {
			this.trashCan = trashCan;
			this.otherTrashCans = new TrashCan[2];
			if(this.trashCan.equals(bottulf)) {
				otherTrashCans[0] = botteline;
				otherTrashCans[1] = bottbert;
			}
			else if(this.trashCan.equals(botteline)) {
				otherTrashCans[0] = bottulf;
				otherTrashCans[1] = bottbert;
			}
			else if(this.trashCan.equals(bottbert)) {
				otherTrashCans[0] = bottulf;
				otherTrashCans[1] = botteline;
			}
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			ss.reset();
			if(generator.nextDouble() < Globals.getInstance().COMMENT_THRESHOLD) {
				int index = generator.nextInt(otherTrashCans.length);
				GUI.theGUI.displayText(otherTrashCans[index].getName() + " comments because " + trashCan.getName() + " received trash.");
				otherTrashCans[index].respond(TrashCan.Mode.COMMENT);
			}
			else {
				GUI.theGUI.displayText(trashCan.getName() + " responds because he/she received trash.");
				trashCan.respond(TrashCan.Mode.DIRECT_RESPONSE);
			}
		}
		
	}

}
