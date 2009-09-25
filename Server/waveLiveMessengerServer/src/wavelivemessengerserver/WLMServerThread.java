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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import net.sf.jml.Email;
import net.sf.jml.MsnContact;
import net.sf.jml.MsnSwitchboard;
import net.sf.jml.event.MsnAdapter;
import net.sf.jml.MsnMessenger;
import net.sf.jml.MsnUserStatus;
import net.sf.jml.impl.MsnMessengerFactory;
import net.sf.jml.message.MsnInstantMessage;


public class WLMServerThread extends MsnAdapter implements Runnable {
    /**
     * The socket to be sent from the dispatcher
     */
    private Socket socket = null;

    /**
     * The logging class. Can be easily modified to integrate with different services
     */
    private final Log LOG;

    /**
     * String used to identify the beginning of the command
     */
    private final String COMMAND_SEQ = "<:>";

    /**
     * String used in splitting received messages
     */
    private final String SPLIT_L = "<";

    /**
     * String used in splitting received messages
     */
    private final String SPLIT_R = ">";

    /**
     * String used in splitting received messages
     */
    private final String RECORD_SPLIT = ":";
    
    /**
     * The input stream from the socket
     */
    private BufferedReader in;

    /**
     * A list of all seen users against a reference to their variable wrapper
     */
    private HashMap<String, WLMSessionData> sessions;

    /**
     * The id of the user that this thread is currently serving
     */
    private String waveUID;

    /**
     * The variable with the session data for this particular user
     */
    private WLMSessionData sessionData;

    /**
     * Encoding method used for data sent over the wire
     */
    private final String CHAR_ENCODING = "gb2312";









    


    /**
     * Constructor
     * @param socket is the socket that incoming message was received from
     * @param log is the logging class that allows printout of errors or logs
     * @param sessions contains all the sessions with references to the WLMSessionData data
     * containing their variables
     */
    public WLMServerThread(Socket socket, Log log, HashMap<String, WLMSessionData> sessions) {
        this.socket = socket;
        this.LOG = log;
        this.sessions = sessions;
    }

    /**
     * Main thread of execution. Waits for incoming data
     */
    public void run() {
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), CHAR_ENCODING));
            String inputCommand;

            //Loop through the head stuff and get the line with the command char in front
            while((inputCommand = in.readLine()) != null) {
                if(inputCommand.startsWith(COMMAND_SEQ)) {
                    inputCommand = inputCommand.replaceFirst(COMMAND_SEQ, "");
                    break;
                }
            }

            //Get the user ID so we can see if we have seen this user before
            if(getWaveUID(inputCommand)) {
                if(sessions.containsKey(waveUID)) {
                    //We have seen this user before, get their variables
                    sessionData = sessions.get(waveUID);
                    LOG.log("{" + waveUID +"}Fetches commands");
                }
                else {
                    //We have not seen this user before
                    sessionData = new WLMSessionData(waveUID, LOG, CHAR_ENCODING);
                    sessions.put(waveUID, sessionData);
                    LOG.log("{" + waveUID +"}Creates session");
                }

                //Now we have the user we can start to process their new commands
                String[] commands = inputCommand.split(RECORD_SPLIT);
                for(int i = 1; i < commands.length; i++) {//We start at 1 because 0 is the userId
                    processCommand(commands[i]);
                }
            }
            //Tidy up this thread so it dies gracefully
            socket.close();
            in.close();
        }catch(IOException e) {
            LOG.err("{" + waveUID + "}unknown IOException: " + e);
        }
    }


    /**
     * Gets the userID from the command string
     * @param inputCommand contains the raw input
     */
    private boolean getWaveUID(String inputCommand) {
        try{
            String tempWaveUID = inputCommand.split(RECORD_SPLIT)[0];
            tempWaveUID = tempWaveUID.split(SPLIT_R)[0].split(SPLIT_L)[1];
            this.waveUID = tempWaveUID;
        } catch(NullPointerException ex) {
            LOG.err("{?unknown?}Malformed command: " + ex);
            return false;
        }
        return true;
    }


    /**
     * Processes incoming commands, assigning them to the correct method
     * @param inputLine the line of data that was received
     * @param command the command that has been taken from the line of data
     */
    private void processCommand(String rawCommand) {
        try{
            //Split the raw command up so that each field is in an array index
            String[] commandList = rawCommand.split(SPLIT_R);
            for(int i = 0; i < commandList.length; i++) {
                commandList[i] = commandList[i].split(SPLIT_L)[1];
            }

            //Run the appropriate method for the command
            String command = commandList[0];
            if(command.equalsIgnoreCase("login")) {
                loginUser(commandList);
                sessionData.flushMsgsOut(socket, true);
            } else if (command.equalsIgnoreCase("get")) {
                sessionData.flushMsgsOut(socket, false);//This one is the only one that actually sends the data
            } else if(command.equalsIgnoreCase("im")) {
                submitIM(commandList);
                sessionData.flushMsgsOut(socket, true);
            } else if(command.equalsIgnoreCase("logout")) {
                logoutUser();
                sessionData.flushMsgsOut(socket, true);
            }
        }catch(ArrayIndexOutOfBoundsException ex) {
            LOG.err("{"+ waveUID + "}Malformed command");
        }
    }


    /**
     * Logs the user into MSN
     * @param commandList the partly processed command
     */
    private void loginUser(String[] commandList) {
        try {
            MsnMessenger messenger = MsnMessengerFactory.createMsnMessenger(commandList[1], commandList[2]);
            messenger.getOwner().setInitStatus(MsnUserStatus.ONLINE);
            messenger.addListener(this);
            messenger.login();
            sessionData.setMessenger(messenger);
            LOG.log("{"+ waveUID + "}Requests login");
        } catch(IndexOutOfBoundsException e) {
            LOG.err("{"+ waveUID + "}Malformed login command");
        } catch(IllegalArgumentException e) {
            LOG.err("{"+ waveUID + "}Malformed email");
        }
    }


    /**
     * Submits a new instant message to the MSN servers
     * @param commandList the partly processed command
     */
    private void submitIM(String[] commandList) {
        try {
            //Send message over the line
            sessionData.getMessenger().sendText(Email.parseStr(commandList[1]), commandList[2]);
            LOG.log("{" + waveUID + "}IM's a contact");
        }catch(IndexOutOfBoundsException e) {
            LOG.err("{" + waveUID + "}Malformed instant message command");
        }
    }


    /**
     * Logs the user out of the MSN session
     */
    private void logoutUser() {
        LOG.log("{" + waveUID + "}Requests logout");
        sessionData.getMessenger().logout();
    }

    /**
     * Strips out banned characters
     * This method assumes that all command characters are 1 char long
     * @param text the message to be checked
     * @return the text with the banned characters replaced
     */
    private String stripBanned(String text) {
            text = text.replace(RECORD_SPLIT.charAt(0), '?');
            text = text.replace(SPLIT_L.charAt(0), '?');
            text = text.replace(SPLIT_R.charAt(0), '?');
            return text;
    }


    /**
     * Triggers when the users status changes
     * @param messenger
     */
    @Override
    public void ownerStatusChanged(MsnMessenger messenger) {
        LOG.log("{" + waveUID + "}status changed");
        sessionData.addMsgOut("<Owner><" + messenger.getOwner().getDisplayName() + ">");
    }


    /**
     * Triggers when the contact list has been fully updated
     * @param messenger
     */
    @Override
    public void contactListInitCompleted(MsnMessenger messenger) {
        LOG.log("{" + waveUID + "}Contact list initalized");
        //Get the messenger contacts
        MsnContact[] contacts = messenger.getContactList().getContacts();
        String outputMessage = "<contactListInit>";

        //Place the details in a string
        for(int i = 0; i < contacts.length; i++) {
            MsnContact temp = contacts[i];
            outputMessage += "<" + temp.getEmail().getEmailAddress() + "><" +
                                    stripBanned(temp.getDisplayName()) + "><" +
                                    temp.getStatus() +">";
        }

        //Place the message in the output queue for transmission
        sessionData.addMsgOut(outputMessage);
    }


    /**
     * Triggers when a contacts status has changed
     * @param messenger
     * @param contact
     */
    @Override
    public void contactStatusChanged(MsnMessenger messenger, MsnContact contact) {
        //Construct the message and place in queue
        final String outputMessage = "<statusChange><" + contact.getEmail().getEmailAddress() +
                                "><" + contact.getOldStatus() + "><" +
                                contact.getStatus() + ">";
        sessionData.addMsgOut(outputMessage);
        
    }


    /**
     * Triggers when an instant message has been received
     * @param switchboard
     * @param message
     * @param contact
     */
    @Override
    public void instantMessageReceived(MsnSwitchboard switchboard, MsnInstantMessage message, MsnContact contact) {
        LOG.log("{" + waveUID + "}IM received");
        //Construct the message and place in a queue
        final String outputMessage = "<im><" + contact.getEmail().getEmailAddress() + "><" +
                                stripBanned(message.getContent()) + ">";
        sessionData.addMsgOut(outputMessage);
    }


    /**
     * Triggers when the logging in process has been completed
     * @param messenger
     */
    @Override
    public void loginCompleted(MsnMessenger messenger) {
        LOG.log("{" + waveUID + "}Login completed");
        sessionData.addMsgOut("<loginComplete>");
    }


    /**
     * Triggers when the logging out process has been completed
     * @param messenger
     */
    @Override
    public void logout(MsnMessenger messenger) {
        LOG.log("{" + waveUID + "}Logout completed");
        sessionData.addMsgOut("<loggedOut>");
    }

    /**
     * Triggers when an exception is caused. We will only process the password
     * incorrect exception
     * @param messenger
     * @param exception
     */
    @Override
    public void exceptionCaught(MsnMessenger messenger, Throwable exception) {
        if(exception.getMessage().equalsIgnoreCase("incorrect password")) {
            LOG.log("{" + waveUID + "}Username/password fail");
            sessionData.addMsgOut("<loginFail>");
        }
    }
}
