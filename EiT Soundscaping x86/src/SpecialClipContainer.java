import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
 * Some sound clips involve more than one trash can. This class keeps track of this, and when
 * playing a clip, it blocks the trash cans involved for the duration of the clip. Trash cans
 * not involved in the clip may still play its own sounds.
 * @author Børge Olav Haug
 */
public class SpecialClipContainer {
	
	private HashMap<FClip, List<TrashCan>> specials;
	private Random generator;
	
	private List<TrashCan> trashCans;
	
	private Stack<Object> resources;
	
	public SpecialClipContainer(String path, HashMap<String, TrashCan> trashCans) {
		this.generator = new Random(System.currentTimeMillis());
		this.specials = new HashMap<FClip, List<TrashCan>>();
		this.resources = new Stack<Object>();
		this.trashCans = new ArrayList<TrashCan>();
		this.trashCans.addAll(this.trashCans);
		File folder = new File(path);
		File[] subfolders = folder.listFiles();
		for(File subFolder : subfolders) {
			if(subFolder.isDirectory()) {
				FClip fclip = new FClip();
				
				fclip.clips = new ArrayList<List<Clip>>();
				int n = Integer.parseInt(subFolder.getName().split("-")[2]);
				for(int i = 0; i < n; i++) {
					fclip.clips.add(new ArrayList<Clip>());
				}
				File[] files = subFolder.listFiles();
				for(File f : files) {
					if(f.getName().split("\\.")[1].equals("wav")) {
						int index = Integer.parseInt(f.getName().split("\\.")[0].split("_")[1]);
						try {
							AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.getAbsoluteFile());
							Clip clip = AudioSystem.getClip();
							clip.open(audioIn);
							resources.push(audioIn);
							resources.push(clip);
							fclip.clips.get(index).add(clip);
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
				String[] cans = subFolder.getName().split("-")[1].split("_");
				List<TrashCan> l = new ArrayList<TrashCan>();
				for(String name : cans) {
					l.add(trashCans.get(name));
				}
				specials.put(fclip, l);
			}
		}
	}
	
	public void play(SpontaneousLogic ss) {
		FClip fclip = (FClip) specials.keySet().toArray()[generator.nextInt(specials.keySet().size())];
		long duration = 0;
		for(List<Clip> list : fclip.clips) {
			long d = 0;
			for(Clip clip : list) {
				long f = (long)(1.0 * clip.getMicrosecondLength() / 1000.0);
				if(f > d) d = f;
			}
			duration += d;
		}
		boolean ok = false;
		for(TrashCan trashCan : specials.get(fclip)) {
			ok = trashCan.canBlock(duration);
		}
		if(ok) {
			String s = "";
			for(TrashCan trashCan : trashCans /*specials.get(fclip)*/) {
				s += trashCan.getName() + " and";
				trashCan.block(duration);
			}
			s = s.substring(0, s.length()-4);
			GUI.theGUI.displayText(s + " are responding spontaneously!");
			for(List<Clip> list : fclip.clips) {
				Clip clip = list.get(generator.nextInt(list.size()));
				clip.setMicrosecondPosition(0);
				clip.start();
				try {
					Thread.sleep((long)(1.0 * clip.getMicrosecondLength() / 1000.0));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			ss.reset();
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
	
	private class FClip {
		List<List<Clip>> clips;
	}

}
