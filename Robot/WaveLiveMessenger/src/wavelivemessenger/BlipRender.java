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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.wave.api.Blip;
import com.google.wave.api.ElementType;
import com.google.wave.api.FormElement;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;

public class BlipRender {
	
	
	/**
     * Reference to parent class for calling variables etc
     */
    private final WaveLiveMessengerServlet root;
    
    /**
     * ArrayList used to store feed items before flushing
     */
    private ArrayList<String> feed;
    
	
	
	/**
	 * Constructor
	 * @param root reference to parent calling class
	 */
	public BlipRender(WaveLiveMessengerServlet root) {
		this.root = root;
		this.feed = new ArrayList<String>();
	}
	
	/**
	 * Renders the login blip
	 * @param rootBlip the root blip of the wave
	 */
	public void loginBlip(Blip rootBlip) {
		clearWaveletBlips(rootBlip);
		TextView textView = rootBlip.getDocument();
		textView.append("Please enter your Windows Live Messenger login details below\n");
		
		textView.append("\nUsername: ");
		textView.appendElement(new FormElement(ElementType.INPUT, root.ID_INPUT_USERNAME));
		
		textView.append("\nPassword: ");
		textView.appendElement(new FormElement(ElementType.PASSWORD, root.ID_INPUT_PASSWORD));
		
		textView.appendElement(new FormElement(ElementType.BUTTON, root.ID_BUTT_LOGIN, "Connect"));
	}
	
	
	/**
	 * Renders the switchboard
	 * @param wavelet the current wavelet that the robot lives in
	 */
	public void renderSwitchboard(Wavelet wavelet, Session cSession) {
		//Extract a few variables that we will need later
		Blip rootBlip = wavelet.getRootBlip();
		TextView textView = rootBlip.getDocument();
		Set<String> keys;
		Iterator<String> iter;
		HashMap<String, WLMContact> contacts = cSession.getContacts();
		HashMap<String, WLMConversation> conversations = cSession.getConversations();
		
		//Clear out any notifications etc...
		clearWaveletBlips(rootBlip);
		
		//Draw the header of the new blip
		textView.append("Signed in to Windows Live Messenger as:\n" + cSession.getWlmDisplayName() + " (" + cSession.getWlmUID() + ")\n");
		textView.appendElement(new FormElement(ElementType.BUTTON, root.ID_BUTT_REFRESH, "Refresh"));
		textView.appendElement(new FormElement(ElementType.BUTTON, root.ID_BUTT_LOGOUT, "Logout"));
		
		//Draw the contact list
		keys = contacts.keySet();
		iter = keys.iterator();
		while(iter.hasNext()) {
			String key = iter.next();
			WLMContact contact = contacts.get(key);
			
			if(contact.getStatus().equals("OFFLINE") == false) {
				textView.append("\n[" + contact.getStatus() + "] ");
				textView.appendElement(new FormElement(ElementType.BUTTON, contact.getUID() + root.ID_BUTT_NEWCONV_PART, 
														contact.getDisplayName() + " (" + contact.getUID() + ")"));
			}
		}
		
		textView.append("\n\n");
		
		//Draw the running conversation tabs
		keys = conversations.keySet();
		iter = keys.iterator();
		while(iter.hasNext()) {
			String key = iter.next();
			int unreadCount = conversations.get(key).getUnreadCount();
			
			if(unreadCount == 0) {//No unread messages
				textView.appendElement(new FormElement(ElementType.BUTTON, key + root.ID_BUTT_OPENCONV_PART, 
						contacts.get(key).getDisplayName()));
			} else {//Unread messages present
				textView.appendElement(new FormElement(ElementType.BUTTON, key + root.ID_BUTT_OPENCONV_PART, 
						contacts.get(key).getDisplayName() + "(" + unreadCount + ")"));
			}
		}
	}
	
	
	/**
	 * Renders the open conversation in a blip
	 * @param wavelet the current wavelet that robot lives in
	 */
	public void renderOpenConversation(Wavelet wavelet, Session cSession) {
		String openConversation = cSession.getOpenConversation();
		//There is an open conversation. Lets render it
		if(openConversation != null) {
			//Get a few variables
			Blip convBlip = wavelet.getRootBlip().createChild();
			TextView textView = convBlip.getDocument();
			HashMap<String, WLMConversation> conversations = cSession.getConversations();
			
			textView.append("(Conversation between you and " + openConversation +")");
			
			//Grab the open conversation and draw it
			if(conversations.containsKey(openConversation)) {
				ArrayList<WLMPost> posts = conversations.get(openConversation).getPosts();
				
				for(int i = 0; i < posts.size(); i++) {
					WLMPost temp = posts.get(i);
					textView.append("\n" + temp.getAuthorDisplayName() + " says:     " + temp.getMessage());
				}
				textView.appendElement(new FormElement(ElementType.INPUT, root.ID_INPUT_IM));
				textView.appendElement(new FormElement(ElementType.BUTTON, root.ID_BUTT_SUBMITIM, "Submit"));
				textView.appendElement(new FormElement(ElementType.BUTTON, root.ID_BUTT_REFRESH, "Refresh"));
			}
		}
	}
	
	
	/**
	 * Informs the user that we are connecting to wlm severs
	 * @param rootBlip the root blip of the wave
	 */
	public void connectingBlip(Blip rootBlip) {
		clearWaveletBlips(rootBlip);
		TextView textView = rootBlip.getDocument();
		
		textView.appendElement(new FormElement(ElementType.BUTTON, root.ID_BUTT_REFRESH, "Refresh"));
		textView.append("connecting...");
	}
	
	/**
	 * Provides a niceish way to output unknown errors to the user
	 * @param rootBlip the root blip of the wave
	 * @param message additional information about the error
	 * @param cSession the currently running session
	 */
	public void awwSnap(Blip rootBlip, String message) {
		clearWaveletBlips(rootBlip);
		rootBlip.getDocument().append("Awww Snap! Something went wrong. Try reloading the robot\n\n" + message);
	}
	
	/**
	 * Removes all the child blips
	 */
	private void clearWaveletBlips(Blip rootBlip) {
		List<Blip> children = rootBlip.getChildren();
		
		//Iterate through the children removing them
		for(int i = 0; i < children.size(); i++) {
			children.get(i).delete();
		}
		
		//Clear the root blip
		rootBlip.getDocument().delete();
	}
	
	/**
	 * Provides a niceish way to output unknown errors to the user from a cron event
	 * @param rootBlip the root blip of the wave
	 * @param message additional information about the error
	 * @param cSession the currently running session
	 */
	public void awwSnapCron(Wavelet wavelet, String message) {
		wavelet.appendBlip().getDocument().append("Awww Snap! Something went wrong. Try reloading the robot\n\n" + message);
	}
	
	/**
	 * Provides a way to output a network error
	 * @param rootBlip the root blip of the wave
	 * @param message additional information about the error
	 * @param cSession the currently running session
	 */
	public void networkError(Wavelet wavelet, String message, Session cSession) {
		wavelet.appendBlip().getDocument().append("Awww Snap! Something went wrong. Looks like the server may be down...\n\n(Further information) " + message);
		cSession.setLoggedIn(false);
		cSession.setAtLoginScreen(true);
	}
	
	/**
	 * Appends a feed item to an array
	 * @param message the message to be posted
	 */
	public void updateFeed(String message) {
		feed.add(message);
	}
	
	/**
	 * Flushes the feed into a single blip
	 * @param wavelet the current wavelet that the robot lives in
	 */
	public void flushFeed(Wavelet wavelet) {
		if(feed.size() > 0) {
			TextView textView = wavelet.appendBlip().getDocument();
			for(int i = 0; i < feed.size(); i++) {
				textView.append("\n-" + feed.get(i));
			}
			
			feed.clear();
		}
	}
}