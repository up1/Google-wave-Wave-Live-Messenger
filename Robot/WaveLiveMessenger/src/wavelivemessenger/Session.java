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

public class Session {
	/**
	 * Contains the wave id that the robot is situated within
	 */
	private String waveid;
	
	/**
	 * Contains the wavelet id that the robot is situated within
	 */
	private String waveletid;
	
	/**
	 * Contains the wave user name 
	 */
	private String waveUID;
	
	/**
	 * Contains the windows live messenger user name
	 */
	private String wlmUID;
	
	/**
	 * Contains the windows live messenger display name
	 */
	private String wlmDisplayName;
	
	/**
	 * true if the user is logged in
	 */
	private boolean loggedIn;
	
	/**
	 * true is the user is at the login conversation
	 */
	private boolean atLoginScreen;
	
	/**
	 * Contains the other participant in the currently open conversation
	 */
	private String openConversation;
	
	/**
	 * List of all contacts. wlm-email vs status/displayname
	 */
	private HashMap<String, WLMContact> contacts;
	
	/**
	 * List of all conversations. wlm-email vs conversation
	 */
	private HashMap<String, WLMConversation> conversations;
	
	public Session(String waveid, String waveletid, String waveUID) {
		this.waveid = waveid;
		this.waveletid = waveletid;
		this.waveUID = waveUID;
		
		loggedIn = false;
		atLoginScreen = true;
		
		this.contacts = new HashMap<String, WLMContact>();
		this.conversations = new HashMap<String, WLMConversation>();
	}
	
	
	
	public String getWaveid() {
		return waveid;
	}
	public void setWaveid(String waveid) {
		this.waveid = waveid;
	}
	public String getWaveletid() {
		return waveletid;
	}
	public void setWaveletid(String waveletid) {
		this.waveletid = waveletid;
	}
	public String getWaveUID() {
		return waveUID;
	}
	public void setWaveUID(String waveUID) {
		this.waveUID = waveUID;
	}
	public String getWlmUID() {
		return wlmUID;
	}
	public void setWlmUID(String wlmUID) {
		this.wlmUID = wlmUID;
	}
	public String getWlmDisplayName() {
		return wlmDisplayName;
	}
	public void setWlmDisplayName(String wlmDisplayName) {
		this.wlmDisplayName = wlmDisplayName;
	}
	public boolean isLoggedIn() {
		return loggedIn;
	}
	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}
	public boolean isAtLoginScreen() {
		return atLoginScreen;
	}
	public void setAtLoginScreen(boolean atLoginScreen) {
		this.atLoginScreen = atLoginScreen;
	}
	public String getOpenConversation() {
		return openConversation;
	}
	public void setOpenConversation(String openConversation) {
		this.openConversation = openConversation;
	}
	public HashMap<String, WLMContact> getContacts() {
		return contacts;
	}
	public void setContacts(HashMap<String, WLMContact> contacts) {
		this.contacts = contacts;
	}
	public HashMap<String, WLMConversation> getConversations() {
		return conversations;
	}
	public void setConversations(HashMap<String, WLMConversation> conversations) {
		this.conversations = conversations;
	}
	
}
