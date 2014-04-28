import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * A class that keeps listings of all sound clips related to some trash can.
 * Invoke the respond method to play the clips.
 * Some sound clips are to be played only at certain times of the day. The
 * class contains logic that checks whether clips of this type matches what the
 * time was when the appropriate method was invoked.
 * Invoke methods on objects of this class to play sound.
 * @author Børge Olav Haug
 */
public class TrashCan implements Constants {
	
	/**
	 * What type of clip to play is controlled by the mode passed as argument to the respond method.
	 * @author Børge Olav Haug
	 */
	public static enum Mode {
		DIRECT_RESPONSE, SPONTANEOUS, COMMENT
	}
	
	/* The expected paths under the path argument given to the constructor. */
	private final String GENERAL_PATH = "general\\";
	private final String GENERAL_TIME_PATH = "general_time\\";
	private final String SPONTANEOUS_GENERAL_PATH = "spontaneous_general\\";
	private final String SPONTANEOUS_TIME_PATH = "spontaneous_time\\";
	private final String COMMENTS_PATH = "comments\\";
	
	private Stack<Object> resources;
	
	/** List containing responses for when trash has been received. */
	private List<Clip> general;
	/** Map containing responses for when trash has been received. These clips will only played at certain times of the day. */
	private HashMap<String, List<Clip>> generalTime;
	/** List containing responses for when some other trash can received trash. */
	private List<Clip> comments;
	/** List containing responses for when there's been a while since the previous piece of trash has been received. */
	private List<Clip> spontaneousGeneral;
	/** Map containing responses for when there's been a while since the previous piece of trash has been received.
	 *  These clips will only played at certain times of the day. */
	private HashMap<String, List<Clip>> spontaneousTime;
	
	/** A generator for generating random numbers. */
	private Random generator;
	/** Time stamp in milliseconds for when the latest sound clip was played. */
	private long respondTimeStamp;
	/** Length of the clip currently being played. */
	private long currentClipLength;
	
	/** Name of the trash can */
	private String name;
	
	/**
	 * @param path Path to the outermost folder of a "trash can". 
	 */
	public TrashCan(String path) {
		this.general = new ArrayList<Clip>();
		this.generalTime = new HashMap<String, List<Clip>>();
		this.comments = new ArrayList<Clip>();
		this.spontaneousGeneral = new ArrayList<Clip>();
		this.spontaneousTime = new HashMap<String, List<Clip>>();
		this.generator = new Random(System.currentTimeMillis());
		this.respondTimeStamp = this.currentClipLength = 0;
		this.name = path.split("\\\\")[1];
		
		this.resources = new Stack<Object>();
		
		// Get all the files related to this trash can.
		File folder;
		
		folder = new File(path+GENERAL_PATH);
		getFiles(folder, general);
		
		folder = new File(path+GENERAL_TIME_PATH);
		getFiles(folder, generalTime);
		
		folder = new File(path+COMMENTS_PATH);
		getFiles(folder, comments);
		
		folder = new File(path+SPONTANEOUS_GENERAL_PATH);
		getFiles(folder, spontaneousGeneral);
		
		folder = new File(path+SPONTANEOUS_TIME_PATH);
		getFiles(folder, spontaneousTime);
	}
	
	/** Go through a folder and put all the sound clips into a list. */
	private void getFiles(File folder, List<Clip> list) {
		File[] filesInTheFolder = folder.listFiles();
		
		for(File f : filesInTheFolder) {
			if(f.isFile() && f.getName().split("\\.")[1].equals("wav")) {
				AudioInputStream audioIn;
				try {
					audioIn = AudioSystem.getAudioInputStream(f.getAbsoluteFile());
					Clip clip = AudioSystem.getClip();
					clip.open(audioIn);
					list.add(clip);
					resources.push(audioIn);
					resources.push(clip);
				} catch (UnsupportedAudioFileException e) {
					System.err.println("This program doesn't support the format of the given audio file.");
					e.printStackTrace();
				} catch (IOException e) {
					GUI.theGUI.displayText("IO error with " + f.getAbsolutePath());
					System.err.println("Encountered a problem with the given file.");
					e.printStackTrace();
				} catch (LineUnavailableException e) {
					System.err.println("Unavailable audio line.");
					e.printStackTrace();
				}
			}
		}
	}
	
	/** Go through a folder and put all the sound clips into a hash map. */
	private void getFiles(File folder, HashMap<String, List<Clip>> map) {
		File[] subFolders = folder.listFiles();
		for(File subFolder : subFolders) {
			if(subFolder.isDirectory()) {
				File[] filesInTheFolder = subFolder.listFiles();
				for(File f : filesInTheFolder) {
					if(f.isFile() && f.getName().split("\\.")[1].equals("wav")) {
						AudioInputStream audioIn;
						try {
							audioIn = AudioSystem.getAudioInputStream(f.getAbsoluteFile());
							Clip clip = AudioSystem.getClip();
							clip.open(audioIn);
							resources.push(audioIn);
							resources.push(clip);
							if(!map.containsKey(subFolder.getName()))
								map.put(subFolder.getName(), new ArrayList<Clip>());
							map.get(subFolder.getName()).add(clip);
						} catch (UnsupportedAudioFileException e) {
							System.err.println("This program doesn't support the format of the given audio file.");
							e.printStackTrace();
						} catch (IOException e) {
							System.err.println("Encountered a problem with the given file.");
							e.printStackTrace();
						} catch (LineUnavailableException e) {
							System.err.println("Unavailable audio line.");
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public boolean canBlock(long duration) {
		long now = System.currentTimeMillis();
		return now - respondTimeStamp >= currentClipLength;
	}
	
	public boolean block(long duration) {
		long now = System.currentTimeMillis();
		// Only block if the previous clip is done playing.
		if(now - respondTimeStamp >= currentClipLength) {
			respondTimeStamp = now;
			currentClipLength = duration;
			return true;
		}
		return false;
	}
	
	public void respond(Mode mode) {
		if(generator.nextDouble() <= Globals.getInstance().RESPOND_THRESHOLD) {
			List<TrashCan> other = GUI.theGUI.getTrashCans();
			long now = System.currentTimeMillis();
			// Only play new clips if the previous one is done playing.
			if(now - respondTimeStamp >= currentClipLength) {
				List<Clip> list;
				HashMap<String, List<Clip>> timeMap;
				List<Clip> timeList = null;
				switch(mode) {
				case DIRECT_RESPONSE:
					list = general;
					timeMap = generalTime;
					break;
				case SPONTANEOUS:
					list = spontaneousGeneral;
					timeMap = spontaneousTime;
					break;
				case COMMENT:
					list = comments;
					timeMap = null;
					break;
				default:
					list = timeList = null;
					timeMap = null;
					break;
				}
				Clip clip;
				double d = generator.nextDouble();
				double threshold = 1.0;
				// Two lists to choose from. Otherwise, we're dealing with the comments list.
				if(timeMap != null) {
					timeList = gatherClips(timeMap);
					// Avoid division by zero.
					if(list.size() + timeList.size() != 0)
						threshold = (double)list.size()/(double)(list.size()+timeList.size());
				}
				if(d < threshold) {
					if(list.isEmpty()) return;
					clip = list.get(generator.nextInt(list.size()));
				}
				else {
					if(timeList.isEmpty()) return;
					clip = timeList.get(generator.nextInt(timeList.size()));
				}
				// Reset clip and play it.
				currentClipLength = (long)(1.0 * clip.getMicrosecondLength() / 1000.0);
				for(TrashCan tc : other) {
					if(!tc.equals(this))
						tc.block(currentClipLength);
				}
				respondTimeStamp = now;
				clip.setMicrosecondPosition(0);
				clip.start();
			}
		}
	}
	
	public void release() {
		while(!resources.isEmpty()) {
			Object res = resources.pop();
			try {
				if(res instanceof AudioInputStream) {
					((AudioInputStream) res).close();
					
				} else if(res instanceof Clip) {
					((Clip) res).close();
				}
			} catch (IOException e) {
				// Ignore.
			}
		}
	}
	
	private List<Clip> gatherClips(HashMap<String, List<Clip>> map) {
		List<Clip> returnList = new ArrayList<Clip>();
		Calendar c = Calendar.getInstance();
		int hourNow = c.get(Calendar.HOUR_OF_DAY);
		int minuteNow = c.get(Calendar.MINUTE);
		for(String key : map.keySet()) {
			String[] s = key.split("_");
			String[] times = s[0].split("-");
			char mode = s[1].charAt(0);
			int hour1 = Integer.parseInt(times[0].split("\\.")[0]);
			int hour2 = Integer.parseInt(times[1].split("\\.")[0]);
			int min1 = Integer.parseInt(times[0].split("\\.")[1]);
			int min2 = Integer.parseInt(times[1].split("\\.")[1]);
			boolean add = false;
			switch(mode) {
			case 'A':
				if(hour1 <= hour2) {
					if(hourNow >= hour1 && hourNow <= hour2) {
						if(hourNow == hour1 && hourNow == hour2) {
							if(minuteNow >= min1 && minuteNow <= min2)
								add = true;
						}
						else if(hourNow == hour1) {
							if(minuteNow >= min1)
								add = true;
						}
						else if(hourNow == hour2) {
							if(minuteNow <= min2)
								add = true;
						}
						else {
							add = true;
						}
					}
				}
				else {
					if(hourNow >= hour1 || hourNow <= hour2) {
						if(hourNow == hour1 && hourNow == hour2) {
							if(minuteNow >= min1 && minuteNow <= min2)
								add = true;
						}
						else if(hourNow == hour1) {
							if(minuteNow >= min1)
								add = true;
						}
						else if(hourNow == hour2) {
							if(minuteNow <= min2)
								add = true;
						}
						else {
							add = true;
						}
					}
				}
				break;
			case 'B':
				if(hour1 <= hour2) {
					if((hourNow >= hour1 && hourNow <= hour2) &&
							(minuteNow >= min1 && minuteNow <= min2)) {
						add = true;
					}
				}
				else {
					if((hourNow >= hour1 || hourNow <= hour2) &&
							(minuteNow >= min1 && minuteNow <= min2)) {
						add = true;
					}
				}
				break;
			default:
				add = false;
				break;
			}
			if(add) {
				returnList.addAll(map.get(key));
			}
		}
		return returnList;
	}

	public String getName() {
		return name;
	}
}
