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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class ServerCommunication {
	
	/**
	 * Reference to the calling class
	 */
	private final WaveLiveMessengerServlet root;
	
	
    
    /**
     * Constructor
     * @param wlmServlet reference to calling class
     */
	public ServerCommunication(WaveLiveMessengerServlet root) {
		this.root = root;
	}
	
	
	/**
	 * Logs into the wlm service
	 * @param waveUsername the users google wave username
	 * @param wlmUsername the users username used to login to wlm
	 * @param wlmPassword the users password used to login to wlm
	 * @return string containing the response. Most likely 'ack'
	 * @throws MalformedURLException
	 * @throws {@link IOException}
	 */
	public String login(String waveUsername, String wlmUsername, String wlmPassword) throws MalformedURLException, IOException {
		wlmUsername = stripBanned(wlmUsername);
		wlmPassword = stripBanned(wlmPassword);
		final String msg = root.COMMAND_IDENT + "<" + waveUsername + ">" + root.COMMAND_SPLIT + "<login><" + wlmUsername + "><" + wlmPassword + ">";
		return communicateWithServer(msg);
	}
	
	/**
	 * Logs the user out of the WLM server
	 * @param waveUsername the users google wave user name
	 * @return string containing servers response
	 */
	public String logout(String waveUsername) throws MalformedURLException, IOException {
		final String msg = root.COMMAND_IDENT + "<" + waveUsername + ">" + root.COMMAND_SPLIT + "<logout>";
		return communicateWithServer(msg);
	}
	
	/**
	 * Retrieves any new data from the server
	 * @return string containing the response
	 * @param waveUsername google wave user name
	 * @throws MalformedURLException
	 * @throws {@link IOException}
	 */
	public String get(String waveUsername) throws MalformedURLException, IOException {
		final String msg = root.COMMAND_IDENT + "<" + waveUsername + ">" + root.COMMAND_SPLIT + "<get>";
		return communicateWithServer(msg);
	}
	
	/**
	 * Sends a new instant message to a participant
	 * @param waveUsername google wave user name
	 * @param email the email address that the message is going to
	 * @param post the message to be delivered
	 * @throws MalformedURLException
	 * @throws {@link IOException}
	 */
	public String newIm(String waveUsername, String email, String post) throws MalformedURLException, IOException {
		email = stripBanned(email);
		post = stripBanned(post);
		final String msg = root.COMMAND_IDENT + "<" + waveUsername + ">" + root.COMMAND_SPLIT + "<im><" + email + "><" + post + ">";
		return communicateWithServer(msg);
	}
	
	
	/**
	 * Sets the majority of things up to communicate with the wlm server and receive a response
	 * @param outMsg the message to be sent to the server
	 * @return string containing the servers response
	 * @throws MalformedURLException
	 * @throws {@link IOException}
	 */
	private String communicateWithServer(String outMsg) throws MalformedURLException, IOException {
		//Set the connection up
		URL serverAddress = new URL(root.HOST);
		HttpURLConnection connection = (HttpURLConnection) serverAddress.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setReadTimeout(10000);
		connection.connect();
		
		//Send the message to the server
		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), root.CHAR_ENCODING);
		out.write(outMsg + "\n");
		out.flush();
		
		//Read the returned message
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), root.CHAR_ENCODING));
		StringBuilder builder = new StringBuilder();
		String tempLine;
		while((tempLine = in.readLine()) != null) {
			builder.append(tempLine);
		}
		
		//Tidy up
		connection.disconnect();
		out.close();
		in.close();
		
		String result = builder.toString();
		if(result.contains("503 Service Temporarily Unavailable")) {
			throw new SocketTimeoutException("Service Temporarily Unavailable. Please try again later.");
		} else {
			return builder.toString();
		}
	}
	
	/**
	 * Strips out banned characters
	 * This method assumes that all command characters are 1 char long
	 * @param text the message to be checked
	 * @return the text with the banned characters replaced
	 */
	private String stripBanned(String text) {
		text = text.replace(root.COMMAND_SPLIT.charAt(0), '?');
		text = text.replace(root.SPLIT_L.charAt(0), '?');
		text = text.replace(root.SPLIT_R.charAt(0), '?');
		return text;
	}
	

}
