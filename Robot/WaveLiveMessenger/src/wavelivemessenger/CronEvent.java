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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.Wavelet;

public class CronEvent {
	/**
	 * Called to render a blip
	 */
	private final BlipRender render;
	
	/**
	 * Used for communicating with the wlm server
	 */
	private final ServerCommunication sComms;
    
    /**
     * Reference to parent class for calling variables etc
     */
    private final WaveLiveMessengerServlet root;
	
	
	
	
	
	
	/**
	 * Used for setting up the class. We can't set these variables in the constructor as
	 * we don't know them then
	 * @param root reference to parent class for calling variables etc
	 * @param sComms reference to the server communication class
	 * @param render class reference that allows you to render blips
	 */
	public CronEvent(WaveLiveMessengerServlet root, BlipRender render, ServerCommunication sComms) {
		this.root = root;
		this.render = render;
		this.sComms = sComms;
	}
	
	/**
	 * Called when cron calls the robot. Sends a get command to server and processes the response
	 * @param waveid the id of the wave that the robot was originally added to
	 * @param waveletid the id of the wavelet that the robot was originally added to
	 * @param bundle the bundle containing event information
	 * @param rootBlipId the id of the root blip in the wavelet
	 */
	public void newCronEvent(RobotMessageBundle bundle) {	
		//Get the keys from all the session
		HashMap<String, Session> sessions = root.sessions;
		
		Set<String> sessionKeys = sessions.keySet();
		Iterator<String> iter = sessionKeys.iterator();
		
		while(iter.hasNext()) {
			String key = iter.next();
			Session cSession = sessions.get(key);
			
			//Process each session
			if(cSession.isAtLoginScreen() == false) {
				//Poll the server to see if there are any updates
				try {
					Wavelet wavelet = bundle.getWavelet(cSession.getWaveid(), cSession.getWaveletid());
					processIncomingMessage(sComms.get(cSession.getWaveUID()), wavelet, cSession);
				} catch (MalformedURLException e) {
					render.awwSnapCron(bundle.getWavelet(cSession.getWaveid(), cSession.getWaveletid()), e.getMessage());
				} catch (IOException e) {
					render.networkError(bundle.getWavelet(cSession.getWaveid(), cSession.getWaveletid()), e.getMessage(), cSession);
				}
			}
		}
	}
	
	
	/**
	 * Receives the incoming message, decompiles it and performs the associated actions
	 * @param rawMessage the input string from the server
	 * @param wavelet containing the current wave so messages can be appended to it
	 * @param cSession the current session
	 */
	private void processIncomingMessage(String rawMessage, Wavelet wavelet, Session cSession) {
		String[] commandList = rawMessage.split(root.COMMAND_SPLIT);
		
		//Process each command individually
		for(int i = 0; i < commandList.length; i++) {
			//Split the command up so that each field is in an array index
			String[] deconstructedCommand = commandList[i].split(root.SPLIT_R);
			for(int j = 0; j < deconstructedCommand.length; j++) {
				deconstructedCommand[j] = deconstructedCommand[j].split(root.SPLIT_L)[1];
			}
			
			//Run the appropriate method for the command
			String command = deconstructedCommand[0];
			if(command.equalsIgnoreCase("statusChange")) {
				statusChangeEvent(deconstructedCommand, wavelet, cSession);
			} else if(command.equalsIgnoreCase("ack")) {
				ackEvent(deconstructedCommand, wavelet);
			} else if(command.equalsIgnoreCase("im")) {
				imEvent(deconstructedCommand, wavelet, cSession);
			} else if(command.equalsIgnoreCase("loginComplete")) {
				loginCompleteEvent(deconstructedCommand, wavelet, cSession);
			} else if(command.equalsIgnoreCase("loggedOut")) {
				loggedOutEvent(deconstructedCommand, wavelet, cSession);
			} else if(command.equalsIgnoreCase("contactListInit")) {
				contactListInitEvent(deconstructedCommand, wavelet, cSession);
			} else if(command.equalsIgnoreCase("owner")) {
				ownerStatusChange(deconstructedCommand, cSession);
			} else if(command.equalsIgnoreCase("loginFail")) {
				loginFailed(wavelet, cSession);
			}
		}
		render.flushFeed(wavelet);
	}
	
	/**
	 * Process WLM Event: statusChange
	 * @param command string[] containing the command and variables
	 * @param wavelet containing the current wave to messages can be appended to it
	 * @param cSession the current session
	 */
	private void statusChangeEvent(String[] command, Wavelet wavelet, Session cSession) {
		//Command: <statusChange><email><oldStatus><newStatus>
		try {
			//Update data structure
			if(cSession.getContacts().containsKey(command[1])) {
				WLMContact tempContact = cSession.getContacts().get(command[1]);
				tempContact.setStatus(command[3]);
				
				//Update feed
				render.updateFeed(tempContact.getDisplayName() + " has changed his/her status from " + command[2] + " to " + command[3]);
			}
			
		}catch(ArrayIndexOutOfBoundsException ex) {
			render.awwSnapCron(wavelet, ex.getMessage());
		}
	}
	
	/**
	 * Process WLM Event: ack
	 * @param command string[] containing the command and variables
	 * @param wavelet containing the current wave to messages can be appended to it
	 * @param cSession the current session
	 */
	private void ackEvent(String[] command, Wavelet wavelet) {
		//Command: <ack>
		
		// TODO. We could do something with this? For the moment though the execution path just dies
	}
	
	/**
	 * Process WLM Event: im
	 * @param command string[] containing the command and variables
	 * @param wavelet containing the current wave to messages can be appended to it
	 * @param cSession the current session
	 */
	private void imEvent(String[] command, Wavelet wavelet, Session cSession) {
		//Command: <im><email><msg>
		try {
			HashMap<String, WLMConversation> conversations = cSession.getConversations();
			
			//Check if it is a new conversation-->if it is set the conversation up
			if(conversations.containsKey(command[1]) == false) {
				conversations.put(command[1], new WLMConversation(command[1]));
			}
			
			String otherParticipantDisplayName = cSession.getContacts().get(command[1]).getDisplayName();
			//Update data structure
			conversations.get(command[1]).addPost(new WLMPost(command[1], otherParticipantDisplayName, command[2], false));
			
			//Update feed
			render.updateFeed(otherParticipantDisplayName + " has just sent you a message");
			
		}catch(ArrayIndexOutOfBoundsException ex) {
			render.awwSnapCron(wavelet, ex.getMessage());
		}
	}
	
	/**
	 * Process WLM Event: loginComplete
	 * @param command string[] containing the command and variables
	 * @param wavelet containing the current wave to messages can be appended to it
	 * @param cSession the current session
	 */
	private void loginCompleteEvent(String[] command, Wavelet wavelet, Session cSession) {
		//Command: <loginComplete>
		cSession.setLoggedIn(true);
		
		render.updateFeed("You have been signed in successfully");
	}
	
	/**
	 * Process WLM Event: loggedOut
	 * @param command string[] containing the command and variables
	 * @param wavelet containing the current wave to messages can be appended to it
	 * @param cSession the current session
	 */
	private void loggedOutEvent(String[] command, Wavelet wavelet, Session cSession) {
		//Command: <loggedOut>
		cSession.setLoggedIn(false);
		cSession.setAtLoginScreen(true);
		
		render.updateFeed("You have been signed out");
		root.sessions.put(cSession.getWaveUID(), new Session(	cSession.getWaveid(),
																cSession.getWaveletid(),
																cSession.getWaveUID()));
	}
	
	/**
	 * Process WLM Event: contact list init
	 * @param command string[] containing the command and variables
	 * @param wavelet containing the current wave to messages can be appended to it
	 * @param cSession the current session
	 */
	private void contactListInitEvent(String[] command, Wavelet wavelet, Session cSession) {
		try {
			/*
			 * Command constructed as....
			 * <contactListInit><email><displayname><status><email......
			 */
			for(int i = 1; i < command.length; i += 3) {
				cSession.getContacts().put(command[i], new WLMContact(command[i], command[i+1], command[i+2]));
			}
			
			//Update feed
			render.updateFeed("Your contact list has been downloaded");
			
		}catch(ArrayIndexOutOfBoundsException ex) {
			render.awwSnapCron(wavelet, ex.getMessage());
		}
	}
	
	/**
	 * Process WLM Event: ownerStatusChanged
	 * @param command string[] containing the command and variables
	 * @param cSession the current session
	 */
	private void ownerStatusChange(String[] command, Session cSession) {
		cSession.setWlmDisplayName(command[1]);
	}
	
	/**
	 * Process WLM Event: Login Failed
	 * @param wavelet containing the current wave to messages can be appended to it
	 * @param cSession the current session
	 */
	private void loginFailed(Wavelet wavelet, Session cSession) {
		cSession.setLoggedIn(false);
		cSession.setAtLoginScreen(true);
		render.updateFeed("Your username/password was incorrect. Press refresh to retry.");
	}
}




















