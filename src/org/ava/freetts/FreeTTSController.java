package org.ava.freetts;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

/**
 * This class controls any activity of the FreeTTS plugin.
 * <p>
 * This includes:
 * 		-- synthesizing a given message and generating the audio output.
 * 		-- continuing and interrupting the audio output
 *
 * @author conwitti
 * @since 2016-05-25
 * @version 1
 *
 */
public class FreeTTSController {

	private final static Logger log = LogManager.getLogger();

	/** Lists containing the current audio player (aka voice) and the thread executing
	 *  that audio player. These lists will contain only one audio player or thread
	 *  and are ugly workarounds for accessing the audio player from both the application
	 *  thread and the thread executing the audio player.*/
	private List<Voice> vl;
	private List<Thread> threadList;

	/** Voice that will be loaded at plugin initialization. */
	private final String VOICE_NAME = "kevin16";

	/** When user requests audio ouput, but an existing audio player is active,
	 *  existing player will be stopped and after given time (in ms) the new
	 *  audio player will start playing.
	 */
	private final int PAUSE_WHEN_ABORTING_CURRENT_OUTPUT = 100;

	/**
	 * Initializes the FreeTTS engine.
	 */
	public FreeTTSController() {
		vl = new ArrayList<Voice>();
		threadList = new ArrayList<Thread>();
		log.debug("FreeTTS instance triggered [voice = " + VOICE_NAME + "].");
	}

	/**
	 * Synthesize the given text and play the audio.
	 * <p>
	 * Audio playback will happen in a separate thread, to avoid blocking Ava
	 * when playing long texts.
	 *
	 * @param msg The text to synthesize and play.
	 */
	public void speak(final String msg) {
		// we use the audio player list and thread list to enable
		// communication with the audio thread (i.e. stopping the audio player)
		// for some reason it did not work otherwise, so we will stick to this ugly
		// workaround for now

		Runnable speaker = new Runnable() {
            public void run() {
            	if(!vl.isEmpty()) {
            		vl.get(0).getAudioPlayer().pause();
            		log.debug("Aborted audio output [voice: " + vl.get(0) + "]");
            		try {
						Thread.sleep(PAUSE_WHEN_ABORTING_CURRENT_OUTPUT);
					} catch (InterruptedException e) {
						// ignore; InterruptedException will always be thrown due to the thread
						// actively playing audio
					}
            		vl.remove(0);
            		threadList.remove(0);
            	}
            	VoiceManager vm = VoiceManager.getInstance();
        		Voice voice = vm.getVoice(VOICE_NAME);
        		vl.add(voice);
        		voice.allocate();
                voice.speak(msg);
                log.debug("Started audio output [voice: " + voice + "]");
                voice.deallocate();
            }
        };

        Thread t = new Thread(speaker);
        threadList.add(t);
        t.start();
	}

	/**
	 * Stop the plugin and take care of any open resources.
	 * Stop any playing audio as well as any running threads.
	 */
	public void destruct() {
		log.debug("FreeTTS destruct triggered.");
		for(Voice v : vl) {
			log.debug("Remove voice " + v.getName() + " [" + v + "] from voice list.");
			v.getAudioPlayer().cancel();
			v.getAudioPlayer().close();
			v.deallocate();
		}
		vl.clear();

		for(Thread t : threadList) {
			log.debug("Remove thread " + t.getName() + " [" + t + "] from thread list.");
			try {
				t.interrupt();
			} catch (Exception ex) {
				if( ex instanceof InterruptedException ) {
					// ignore; InterruptedException will always be thrown when
					// interrupting this thread (due to it actively playing audio)
				}
			}
		}
		threadList.clear();
	}

	/**
	 * Continue paused audio output.
	 */
	public void continueSpeaking() {
		vl.get(0).getAudioPlayer().resume();
		log.info("Resumed FreeTTS engine.");
	}

	/**
	 * Interrupt current audio output.
	 */
	public void interruptSpeaking() {
		vl.get(0).getAudioPlayer().pause();
		log.info("Paused FreeTTS engine.");
	}

}
