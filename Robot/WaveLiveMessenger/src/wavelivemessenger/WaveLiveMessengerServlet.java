// Copyright 2009, Acknack Ltd. All rights reserved.
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.


package wavelivemessenger;

import java.util.HashMap;

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.Wavelet;

public class WaveLiveMessengerServlet extends AbstractRobotServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Called to process cron event
	 */
	private final CronEvent cronEvent;
	
	/**
	 * Called to process user interaction event
	 */
	private final InteractionEvent interactionEvent;
	
	/**
	 * Called to render a blip
	 */
	private final BlipRender render;
	
	/**
	 * Communicates with the server
	 */
	private final ServerCommunication sComms;
	
	/**
	 * ID used for user name input field
	 */
	public final String ID_INPUT_USERNAME = "usernameF";
	
	/**
	 * ID used for password input field
	 */
	public final String ID_INPUT_PASSWORD = "passwordF";
	
	/**
	 * ID used for login button
	 */
	public final String ID_BUTT_LOGIN = "loginB";
	
	/**
	 * ID used for refresh button
	 */
	public final String ID_BUTT_REFRESH = "refreshB";
	
	/**
	 * ID used for logout button
	 */
	public final String ID_BUTT_LOGOUT = "logoutB";
	
	/**
	 * ID for open conversation button
	 */
	public final String ID_BUTT_OPENCONV_PART = "OpenConv";
	
	/**
	 * ID for new conversation button
	 */
	public final String ID_BUTT_NEWCONV_PART = "NewConv";
	
	/**
	 * ID for submit button
	 */
	public final String ID_BUTT_SUBMITIM = "submitImB";
	
	/**
	 * ID for new im field
	 */
	public final String ID_INPUT_IM = "submitImF";
	
	/**
     * String used in splitting received messages
     */
	public final String COMMAND_SPLIT = ":";
    
    /**
	 * The location of the 'in-between' server
	 */
	public final String HOST = "http://robot.wave.to/";
	
	/**
	 * The string that dictates that the message is a command
	 */
	public final String COMMAND_IDENT = "<:>";
	
	/**
     * String used in splitting received messages
     */
	public final String SPLIT_L = "<";

    /**
     * String used in splitting received messages
     */
	public final String SPLIT_R = ">";
    
    /**
     * The robots address
     */
	public final String ROBOT_ADDRESS = "sanoodiwave@appspot.com";
    
    /**
     * Contains all details about particular session
     */
	public HashMap<String, Session> sessions;
	
	/**
     * Encoding method used for data sent over the wire
     */
    public final String CHAR_ENCODING = "gb2312";
    
	
	
    
    
    
    
    /**
     * Constructor. Set up child classes
     */
	public WaveLiveMessengerServlet() {
		this.sComms = new ServerCommunication(this);
		this.render = new BlipRender(this);
		this.cronEvent = new CronEvent(this, render, sComms);
		this.interactionEvent = new InteractionEvent(this, render, sComms);
		sessions = new HashMap<String, Session>();
	}
	
	
	
	
	/**
	 * Receives and handles Wave events
	 * @param bundle contains all incoming wave events
	 */
	@Override
	public void processEvents(RobotMessageBundle bundle) {
		Wavelet wavelet = bundle.getWavelet();
		
		//Added to new wave. Prepare for cron events etc
		if(bundle.wasSelfAdded()) {
			wavelet.appendBlip().getDocument().append("18th Sept 09 - At present there is a bug with Google Wave which means this robot does not function" +
					" entirely as you would expect. At present you will NOT receive update blips, however if you press refresh the updates will be loaded" +
					" and you can continue to use this robot as usual. Visit http://wave.to for more information.");
			addedToWave(wavelet);
			//Render the login screen
			render.loginBlip(wavelet.getRootBlip());
		}
		
		//Called from a cron event
		if(bundle.getEvents().size() == 0) {
			cronEvent.newCronEvent(bundle);
		}
		//Called from a user interaction event
		else {
			interactionEvent.processEvents(bundle);
		}

	}
	
	/**
	 * Needs to be run when the robot is first added to the wave. Sets variables up for when called by cron
	 * @param wavelet contains the wavelet that the robot lives in
	 */
	private void addedToWave(Wavelet wavelet) {
		String waveUID = wavelet.getCreator();
		sessions.put(waveUID, new Session(	wavelet.getWaveId(),
											wavelet.getWaveletId(),
											waveUID));
	}
}






















