import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class Globals {
	
	private static Globals INSTANCE;
	
	private Globals() {
		File f = new File("globals.txt");
		if(f.exists()) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(f));
				String line = br.readLine();
				while(line != null) {
					String[] el = line.split(" = ");
					double value = Double.parseDouble(el[1]);
					if(  		el[0].equals("RESPOND_THRESHOLD")) {
						RESPOND_THRESHOLD = value;
					} else if(  el[0].equals("SPECIAL_CLIPS_THRESHOLD")) {
						SPECIAL_CLIPS_THRESHOLD = value;
					} else if(  el[0].equals("COMMENT_THRESHOLD")) {
						COMMENT_THRESHOLD = value;
					} else if(	el[0].equals("MIN_TIME_SPONTANEOUS_WAIT")) {
						MIN_TIME_SPONTANEOUS_WAIT = value;
					} else if(  el[0].equals("MAX_TIME_SPONTANEOUS_WAIT")) {
						MAX_TIME_SPONTANEOUS_WAIT = value;
					} else if(  el[0].equals("LOOP_SLEEP")) {
						LOOP_SLEEP = (long)value;
					}
					line = br.readLine();
				}
				br.close();
			} catch (IOException ioe) {
				// TODO: handle exception
				ioe.printStackTrace();
			}
		}
		System.err.println(
				"RESPOND_THRESHOLD = " + RESPOND_THRESHOLD +
				"\nSPECIAL_CLIPS_THRESHOLD = " + SPECIAL_CLIPS_THRESHOLD +
				"\nCOMMENT_THRESHOLD = " + COMMENT_THRESHOLD +
				"\nMIN_TIME_SPONTANEOUS_WAIT = " + MIN_TIME_SPONTANEOUS_WAIT +
				"\nMAX_TIME_SPONTANEOUS_WAIT = " + MAX_TIME_SPONTANEOUS_WAIT +
				"\nLOOP_SLEEP = " + LOOP_SLEEP
		);
	}
	
	public static Globals getInstance() {
		if(INSTANCE == null)
			INSTANCE = new Globals();
		return INSTANCE;
	}
	
	/** Probability of responding with sound */
	public double RESPOND_THRESHOLD = 1.0;
	
	/** Probability of having two or more trash cans respond together */
	public double SPECIAL_CLIPS_THRESHOLD = 0.1;
	
	/** Probability of having another trash can respond than the one that received trash */
	public double COMMENT_THRESHOLD = 0.05;
	
	/** How long to sleep before returning to the top of the loop. */
	public long LOOP_SLEEP = 5000;
	
	/** Objects of this class will wait a minimum of number of minutes before playing sounds. */
	public double MIN_TIME_SPONTANEOUS_WAIT = 3.0;
	
	/** Objects of this class will play sounds if the maximum number of minutes has passed. */
	public double MAX_TIME_SPONTANEOUS_WAIT = 20.0;

}
