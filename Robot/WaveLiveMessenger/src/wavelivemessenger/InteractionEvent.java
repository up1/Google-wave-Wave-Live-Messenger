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

import com.google.wave.api.EventType;
import com.google.wave.api.FormView;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.Wavelet;

public class InteractionEvent {
	/**
	 * Reference to the calling class
	 */
	private final WaveLiveMessengerServlet root;
	
	/**
	 * Called to render a blip
	 */
	private final BlipRender render;
	
	/**
	 * Used for communicating with the wlm server
	 */
	private final ServerCommunication sComms;

    
    
    
	
	
	
	
	/**
	 * Used for setting up the class. We can't set these variables in the constructor as
	 * we don't know them then
	 * @param root reference to calling class
	 * @param sComms reference to the server communication class
	 * @param render class reference that allows you to render blips
	 */
	public InteractionEvent(WaveLiveMessengerServlet root, BlipRender render, ServerCommunication sComms) {
		this.root = root;
		this.sComms = sComms;
		this.render = render;
	}
	
	/**
	 * Receives and handles wave events when they have been created by user interaction(not cron)
	 * @param bundle contains all incoming wave events
	 */
	public void processEvents(RobotMessageBundle bundle) {
		//Get some variables from various sources
		Wavelet wavelet = bundle.getWavelet();
		Session cSession = root.sessions.get(wavelet.getCreator());
		
		
		for(com.google.wave.api.Event ev : bundle.getEvents()) {
			if(ev.getType() == EventType.FORM_BUTTON_CLICKED && ev.getModifiedBy().equals(cSession.getWaveUID())) {
				//Process each button input accordingly
				if(ev.getButtonName().equalsIgnoreCase(root.ID_BUTT_REFRESH)) {
					refreshClicked(ev, cSession);
				} else if(ev.getButtonName().equalsIgnoreCase(root.ID_BUTT_LOGIN)) {
					loginClicked(ev, cSession);
				} else if(ev.getButtonName().contains(root.ID_BUTT_OPENCONV_PART)) {
					openConversationClicked(ev, cSession);
				} else if(ev.getButtonName().contains(root.ID_BUTT_NEWCONV_PART)) {
					newConversationClicked(ev, cSession);
				} else if(ev.getButtonName().equalsIgnoreCase(root.ID_BUTT_SUBMITIM)) {
					submitIMClicked(ev, cSession);
				} else if(ev.getButtonName().equalsIgnoreCase(root.ID_BUTT_LOGOUT)) {
					logoutClicked(ev, cSession, wavelet);
				}
			}
		}
	}
	
	/**
	 * Refreshes the screen loading the data from the data structures
	 * @param ev the event data containing information about the wave
	 * @param cSession the current session variables containing all data about this session
	 */
	private void refreshClicked(com.google.wave.api.Event ev, Session cSession) {
		//If we are logged in render the switchboard, otherwise render the login blip
		if(cSession.isLoggedIn()) {
			render.renderSwitchboard(ev.getWavelet(), cSession);
			render.renderOpenConversation(ev.getWavelet(), cSession);
		}else{
			render.loginBlip(ev.getWavelet().getRootBlip());
		}
	}
	
	
	/**
	 * Attempts to log the user in after fetching the data from the blip
	 * @param ev the event data containing information about the event
	 */
	private void loginClicked(com.google.wave.api.Event ev, Session cSession) {
		
		//Get the values from the input fields & validate
		FormView formView = ev.getBlip().getDocument().getFormView();
		String uid = formView.getFormElement(root.ID_INPUT_USERNAME).getValue();
		String password = formView.getFormElement(root.ID_INPUT_PASSWORD).getValue();
		
		//Simple validation-- check length. This may stop crashes later on
		if(uid.length() == 0 || password.length() == 0) {
			ev.getBlip().getDocument().append("You must enter a valid username and password");
			return;
		}
		
		//Validation passed, continue
		cSession.setWlmUID(uid);
		cSession.setAtLoginScreen(false);

		//Update the blip
		render.connectingBlip(ev.getWavelet().getRootBlip());
		
		//Connect to the server
		try {
			sComms.login(ev.getWavelet().getCreator(), cSession.getWlmUID(), password);//Will return <ack>. This can be ignored
		} catch (MalformedURLException e) {
			render.awwSnap(ev.getWavelet().getRootBlip(), e.getMessage());
		} catch (IOException e) {
			render.awwSnap(ev.getWavelet().getRootBlip(), e.getMessage());
		}
	}
	
	
	/**
	 * Refreshes the screen loading the data from the data structures
	 * @param ev the event data containing information about the wave
	 */
	private void openConversationClicked(com.google.wave.api.Event ev, Session cSession) {
		//Extract user name from button
		cSession.setOpenConversation(ev.getButtonName().replace(root.ID_BUTT_OPENCONV_PART, ""));
		cSession.getConversations().get(cSession.getOpenConversation()).setAllRead();//Nasty
		refreshClicked(ev, cSession);
	}
	
	
	/**
	 * Starts a new conversation with the user
	 * @param ev the event data containing information about the wave
	 */
	private void newConversationClicked(com.google.wave.api.Event ev, Session cSession) {
		String conversationWith = ev.getButtonName().replace(root.ID_BUTT_NEWCONV_PART, "");
		//Set up the new conversation
		cSession.getConversations().put(conversationWith, new WLMConversation(conversationWith));
		cSession.setOpenConversation(conversationWith);
		refreshClicked(ev, cSession);
	}
	
	
	/**
	 * Refreshes the screen loading the data from the data structures
	 * @param ev the event data containing information about the wave
	 */
	private void submitIMClicked(com.google.wave.api.Event ev, Session cSession) {
		//Extract the data from the form
		FormView formView = ev.getBlip().getDocument().getFormView();
		String newIm = formView.getFormElement(root.ID_INPUT_IM).getValue();
		String openConversation = cSession.getOpenConversation();
		
		
		if(cSession.getConversations().containsKey(openConversation)) {
			//Add newIM to local data structure
			cSession.getConversations().get(openConversation).addPost(new WLMPost(cSession.getWlmUID(), cSession.getWlmDisplayName(), newIm, true));  
			
			//Send newIM over the wire
			try {
				sComms.newIm(cSession.getWaveUID(), openConversation, newIm);
			} catch (MalformedURLException e) {
				render.awwSnap(ev.getWavelet().getRootBlip(), e.getMessage());
			} catch (IOException e) {
				render.awwSnap(ev.getWavelet().getRootBlip(), e.getMessage());
			}
			refreshClicked(ev, cSession);
		}
	}
	
	
	/**
	 * Logs the user out of the WLM service
	 * @param ev the event data containing information about the wave
	 */
	private void logoutClicked(com.google.wave.api.Event ev, Session cSession, Wavelet wavelet) {
		try {
			sComms.logout(cSession.getWaveUID());
			cSession.setLoggedIn(false);
			refreshClicked(ev, cSession);
			root.sessions.put(cSession.getWaveUID(), new Session(	wavelet.getWaveId(),
														wavelet.getWaveletId(),
														wavelet.getCreator()));
		} catch (MalformedURLException e) {
			render.awwSnap(ev.getWavelet().getRootBlip(), e.getMessage());
		} catch (IOException e) {
			render.awwSnap(ev.getWavelet().getRootBlip(), e.getMessage());
		}
	}
	
}