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
import java.net.ServerSocket;
import java.util.HashMap;


public class WLMServer {

    /**
     * Server version
     */
    private final String VERSION ="0.0.11";

    /**
     * The port that this server will listen on
     */
    private final int PORT = 8000;

    /**
     * If a session has been inactive for this amount of time it is killed
     */
    private final Long KILL_DELAY_MILLIS;

    /**
     * The garbage collector thread sleeps for this amount of time
     */
    private final Long RESCAN_TIME_MILLIS;

    /**
     * The logging class. Can be easily modified to integrate with different services
     */
    private final Log LOG;

    /**
     * Hashmap containing the variables for each session. Static so that all
     * child threads are able to access it.
     */
    private HashMap<String, WLMSessionData> sessions;

    /**
     * Path to log file
     */
    private final String FILE_PATH = "/tmp/log.txt";




    /**
     * Class entry point
     * @param args not utilized at present
     */
    public static void main(String[] args) {
        new WLMServer();
    }






    /**
     * Class constructor. Continues to listen for new requests and dispatches
     * them to appropriate threads
     */
    public WLMServer() {
        //Initialize a few variables
        LOG = new Log(FILE_PATH, true, true, false);
        KILL_DELAY_MILLIS = 30000L;//kill session after 30 secs of inactivity
        RESCAN_TIME_MILLIS = 30000L;//Rescan for sessions every 30 secs
        ServerSocket serverSocket = null;
        boolean listening = true;
        sessions = new HashMap<String, WLMSessionData>();



        new WLMServerGarbageCollector(this, KILL_DELAY_MILLIS, RESCAN_TIME_MILLIS, LOG).start();





        //Open up the server socket
        try{
            serverSocket = new ServerSocket(PORT);
            LOG.log("{DISPATCHER}Server version " + VERSION +" started on port " + PORT);
        } catch(IOException e) {
            LOG.err("{DISPATCHER}Could not listen on port: " + PORT, -1);
        }


        
        //Accept and assign new incoming connections
        while(listening) {
            try{
                Thread thread = new Thread(new WLMServerThread(serverSocket.accept(), LOG, sessions));
                thread.start();
            } catch(IOException e) {
                LOG.err("{DISPATCHER}Could not accept incoming connection: " +e);
            }
        }



        //Close the server down when execution finished
        try{
            serverSocket.close();
        } catch(IOException e) {
            LOG.err("{DISPATCHER}Could not close port: " + PORT, -1);
        }
        LOG.close();
    }

    /**
     * Allows a thread to aquire a copy of the hashmap
     * @return hashmap containing session data
     */
    public HashMap<String, WLMSessionData> getSessions() {
        return sessions;
    }

}
