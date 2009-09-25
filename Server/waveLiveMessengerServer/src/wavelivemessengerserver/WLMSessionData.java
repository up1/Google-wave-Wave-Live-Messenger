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


package wavelivemessengerserver;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import net.sf.jml.MsnMessenger;


public class WLMSessionData {

    /**
     * The logging class. Can be easily modified to integrate with different services
     */
    private final Log LOG;

    /**
     * Session id of this particular record
     */
    private String waveUID;

    /**
     * Instance of MSN messenger for this record
     */
    private MsnMessenger messenger;

    /**
     * ArrayList containing items ready to be sent back to client
     */
    private ArrayList<String> messagesOutQueue;

    /**
     * The last time the data was accessed
     */
    private Long lastAccess;

    /**
     * Set to true if we have just logged in. Used to block all logged out commands
     * set thereafter
     */
    private boolean newSession;

    /**
     * Encoding method used for data sent over the wire
     */
    private final String CHAR_ENCODING;







    
    /**
     * Constructor
     * @param id contains the id of this record
     */
    public WLMSessionData(String waveUID, Log log, String charEncoding) {
        this.waveUID = waveUID;
        this.LOG = log;
        this.CHAR_ENCODING = charEncoding;
        messagesOutQueue = new ArrayList<String>();
        newSession = false;
        accessed();
    }






    /**
     * Updates the last time the data record was accessed
     */
    private void accessed() {
        lastAccess = System.currentTimeMillis();
    }

    /**
     * returns the last time the data record was accessed
     * @return Long containing time in milliseconds
     */
    public Long lastAccessed() {
        return lastAccess;
    }

    /**
     * Adds a message to the out queue
     * @param msg
     */
    public void addMsgOut(String msg) {
        accessed();
        //Check to see if the message is logged in. If so clear all old messages
        // that might be from old sessions
        if(msg.equalsIgnoreCase("<loginComplete>")) {
            this.messagesOutQueue.clear();
            newSession = true;
        }
        //Check to see if the message is logged out. If it is and we are on a new
        // session then it is a redudant command and should be ignored
        if(msg.equalsIgnoreCase("<loggedOut>") && newSession) {
            return;
        }
        
        this.messagesOutQueue.add(msg);
    }

    /**
     * Flushes the messages out queue
     * @param socket the output socket that the response is to be sent to
     * @param stub sends ack if and only if true
     */
    public void flushMsgsOut(Socket socket, boolean stub) {
        accessed();
        try {
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), CHAR_ENCODING);
            String output = "";

            //If messages size is 0, return an end stub
            if(stub || messagesOutQueue.size() == 0) {
                output = "<ack>";
            }
            else {
                //Form messages out into single string
                for(int i = 0; i < messagesOutQueue.size(); i++) {
                    if(i == 0)
                        output += messagesOutQueue.get(i);
                    else
                        output += ":" + messagesOutQueue.get(i);
                }
                messagesOutQueue.clear();
                newSession = false;
            }

            out.write(output + "\n");
            out.flush();
            out.close();
        } catch (IOException e) {
            LOG.err("{" + waveUID + "}unknown IOException: " + e);
        }
    }

    /**
     * Adds the messenger instance for this user
     * @param messenger the messenger instance
     */
    public void setMessenger(MsnMessenger messenger) {
        accessed();
        this.messenger = messenger;
    }

    /**
     * Returns the instance of MsnMessenger
     * @return MsnMessenger
     */
    public MsnMessenger getMessenger() {
        accessed();
        return messenger;
    }

    /**
     * Returns the id associated with this session
     * @return int containing the id
     */
    public String getWaveUID() {
        accessed();
        return waveUID;
    }
}
