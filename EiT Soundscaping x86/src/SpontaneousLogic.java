import java.util.List;
import java.util.Random;

/**
 * This class takes care of playing sound from trash cans ({@link TrashCan})
 * after some time has gone.
 * @author Børge Olav Haug
 */
public class SpontaneousLogic implements Runnable, Constants {
	
	/** The list of trash cans that this object will invoke methods on. */
	private List<TrashCan> trashCans;
	/** Time stamp in milliseconds for when the latest sound clip was played. */
	private long prevPlayTimeStamp;
	/** A generator for generating random numbers. */
	private Random generator;
	
	private SpecialClipContainer foo;
	
	/**
	 * 
	 * @param trashCans
	 */
	public SpontaneousLogic(List<TrashCan> trashCans) {
		this.trashCans = trashCans;
		this.prevPlayTimeStamp = System.currentTimeMillis();
		this.generator = new Random(System.currentTimeMillis());
	}
	
	public SpontaneousLogic(List<TrashCan> trashCans, SpecialClipContainer foo) {
		this.trashCans = trashCans;
		this.foo = foo;
		this.prevPlayTimeStamp = System.currentTimeMillis();
		this.generator = new Random(System.currentTimeMillis());
	}

	@Override
	public void run() {
		while(true) {
			// Wait the minimum time period before playing any sound from the trash cans.
			// Whether a sound clip should be played is controlled by a random element
			// which depends on the time passed since the previous clip was played.
			// The more time has passed, the more likely it is that a clip will be played.
			long now = System.currentTimeMillis();
			double minutesGone = ((double)(now - prevPlayTimeStamp))/(1000.0*60.0);
			int ceil = (int)Math.ceil(minutesGone);
			Globals g = Globals.getInstance();
			if(ceil > 0 && ceil % (g.LOOP_SLEEP/1000) == 0) GUI.theGUI.displayText(ceil + " minutes have passed without receiving any trash.");
			if(g.MIN_TIME_SPONTANEOUS_WAIT < minutesGone &&
					generator.nextDouble()* g.MAX_TIME_SPONTANEOUS_WAIT < minutesGone) {
				double d = generator.nextDouble();
				if(d < g.SPECIAL_CLIPS_THRESHOLD) {
					foo.play(this);
				}
				else {
					TrashCan tc = trashCans.get(generator.nextInt(trashCans.size()));
					GUI.theGUI.displayText(tc.getName() + " responds spontaneously!");
					tc.respond(TrashCan.Mode.SPONTANEOUS);
					prevPlayTimeStamp = now;
				}
			}
			else {
				try {
					// It may be slightly CPU consuming to do the above check constantly.
					// Sleep a short while to avoid this.
					Thread.sleep(g.LOOP_SLEEP);
				} catch (InterruptedException e2) {
					System.err.println("Thread interrupted!");
					e2.printStackTrace();
				}
			}
		}
	}

	/**
	 * Set the previous play time stamp to the current time.
	 */
	public void reset() {
		prevPlayTimeStamp = System.currentTimeMillis();
	}
}
