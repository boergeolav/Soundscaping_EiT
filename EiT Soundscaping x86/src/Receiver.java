
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;

/**
 * 
 * @author Børge Olav Haug
 *
 */
public class Receiver implements SerialPortEventListener, Constants {
	
	private SerialPort serialPort;
	private BufferedReader in;
	
	private TrashCan[] trashCans;
	private SpontaneousLogic ss;
	
	private Random generator;
	
	public Receiver(CommPortIdentifier portId, TrashCan[] trashCans, SpontaneousLogic ss) {
		this.trashCans = trashCans;
		this.ss = ss;
		this.generator = new Random(System.currentTimeMillis());
		
		if(portId == null) {
			GUI.theGUI.displayText("Port was null");
			Timer t = new Timer();
			t.schedule(new TimerTask() {
				
				@Override
				public void run() {
					System.exit(1);
				}
			}, 5000);
		}
		serialPort = null;
		try {
			String s = "Preparing port " + portId.getName();
			GUI.theGUI.displayText(s);
			System.out.println(s);
			serialPort = (SerialPort) portId.open(
					"EiT",
					10000
					);
		} catch (PortInUseException e) {
			GUI.theGUI.displayText("Port already in use...");
			System.err.println("Port already in use: " + e);
		    System.exit(1);
		}
		try {
			serialPort.setSerialPortParams(
					9600,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE
					);
			serialPort.notifyOnDataAvailable(true);
			serialPort.notifyOnOutputEmpty(false);
			serialPort.notifyOnBreakInterrupt(false);
			serialPort.notifyOnCarrierDetect(false);
			serialPort.notifyOnCTS(false);
			serialPort.notifyOnDSR(false);
			serialPort.notifyOnFramingError(false);
			serialPort.notifyOnOverrunError(false);
			serialPort.notifyOnParityError(false);
			serialPort.notifyOnRingIndicator(false);

		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		}
		try {
			in = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
		} catch (IOException e) {
			System.err.println("IO error!");
			e.printStackTrace();
		}
		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			System.err.println("The serial port already had a listener attached to it.");
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		if(serialPort != null)
			serialPort.close();
		if(in != null)
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	@Override
	public void serialEvent(SerialPortEvent e) {
		TrashCan trashCan;
		List<TrashCan> otherTrashCans = new ArrayList<TrashCan>();
			
		if(e.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			ss.reset();
			
			// Read input and figure out which trash can should respond.
			char[] input = new char[1];
			try {
				in.read(input);
				int i = (int) input[0];
				if(i >= trashCans.length || i < 0) {
					GUI.theGUI.displayText("The number" + i + " received at the port is out of bounds.");
					System.err.println("The number" + i + " received at the port is out of bounds.");
					return;
				}
				if(DEBUG) System.out.println("Received " + i + " as input.");
				GUI.theGUI.displayText("Received " + i + " as input at the port.");
				trashCan = trashCans[i];
				Writer w = null;
				try {
					w = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(trashCan.getName()+"_statistics.txt", true), "utf-8"));
					Calendar c = Calendar.getInstance();
					w.append("Received trash " + 
							c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+ " on " +
							c.get(Calendar.DAY_OF_MONTH) +"."+c.get(Calendar.MONTH)+"."+c.get(Calendar.YEAR) +
							"\r\n"
							);
					w.flush();
					w.close();
				} catch (IOException ioe) {
					// TODO: handle exception
				}
				for (int j = 0; j < trashCans.length; j++) {
					if(j!=i) {
						otherTrashCans.add(trashCans[j]);
					}
				}
				if(generator.nextDouble() < Globals.getInstance().COMMENT_THRESHOLD) {
					int index = generator.nextInt(otherTrashCans.size());
					TrashCan tc1 = otherTrashCans.get(index);
					GUI.theGUI.displayText(tc1.getName() + " comments because " + trashCan.getName() + " received trash.");
					tc1.respond(TrashCan.Mode.COMMENT);
				}
				else {
					GUI.theGUI.displayText(trashCan.getName() + " responds because he/she received trash.");
					trashCan.respond(TrashCan.Mode.DIRECT_RESPONSE);
				}
			} catch (IOException e1) {
				System.err.println("IO error!");
				e1.printStackTrace();
			}
		}
	}
	
}
