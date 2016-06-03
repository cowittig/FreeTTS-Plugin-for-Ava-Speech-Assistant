package org.ava.freetts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ava.pluginengine.TTSPlugin;

/**
 * This class implements the base plugin class to be
 * compatible to Ava's plugin engine.
 * <p>
 * It maninly provides lifecycle functionality, like
 * starting and stopping the plugin.
 *
 * @author Constantin
 * @since 2016-05-25
 * @version 1
 */
public class FreeTTSPlugin implements TTSPlugin {

	private final static Logger log = LogManager.getLogger();

	/** The plugin controller manages the plugin activity. */
	private FreeTTSController pluginController;

	/**
	 * Default constructor.
	 */
	public FreeTTSPlugin() {

	}

	/**
	 * Start the plugin.
	 */
	@Override
	public void start() {
		this.pluginController = new FreeTTSController();
		log.info("FreeTTS plugin started.");
	}

	/**
	 * Stop the plugin.
	 */
	@Override
	public void stop() {
		if( !hasPluginBeenStarted() ) {
			return;
		}

		this.pluginController.destruct();
		log.info("FreeTTS plugin stopped.");
	}


	/**
	 * Continue the plugin.
	 * <p>
	 * This continues the paused speech output.
	 */
	@Override
	public void continueExecution() {
		if( !hasPluginBeenStarted() ) {
			return;
		}

		log.info("FreeTTS plugin resume triggered.");
		this.pluginController.continueSpeaking();
	}

	/**
	 * Interrupt the plugin.
	 * <p>
	 * This pauses any current speech output.
	 */
	@Override
	public void interruptExecution() {
		if( !hasPluginBeenStarted() ) {
			return;
		}

		log.info("FreeTTS plugin interrupt triggered.");
		this.pluginController.interruptSpeaking();
	}

	/**
	 * Synthesize the given message and play the generated audio.
	 *
	 * @param msg The text to synthesize.
	 */
	@Override
	public void sayText(String msg) {
		if( !hasPluginBeenStarted() ) {
			return;
		}

		this.pluginController.speak(msg);
	}

	/**
	 * Checks if the plugin has been started yet.
	 *
	 * @return boolean True if the plugin has been started yet, false if not.
	 */
	private boolean hasPluginBeenStarted() {
		if( this.pluginController == null ) {
			log.error("FreeTTS plugin not initialized. Start plugin first.");
			return false;
		} else {
			return true;
		}
	}

}
