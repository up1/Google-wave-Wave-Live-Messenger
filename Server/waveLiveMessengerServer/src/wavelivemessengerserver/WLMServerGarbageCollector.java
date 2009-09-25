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

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;

public class WLMServerGarbageCollector extends Thread{

    /**
     * The logging class. Can be easily modified to integrate with different services
     */
    private final Log LOG;

    /**
     * A reference to the WLMServer that called this thread
     */
    private final WLMServer WLMSERVER;

    /**
     * A copy of the hashmap containing user records
     */
    private HashMap<String, WLMSessionData> sessions;

    /**
     * If a session has been inactive for this amount of time it is killed
     */
    private final Long KILL_DELAY_MILLIS;

    /**
     * The thread sleeps for this amount of time
     */
    private final Long RESCAN_TIME_MILLIS;
    
    
    /**
     * 
     * @param wlmServer a reference to the class that started this thread
     */
    public WLMServerGarbageCollector(WLMServer wlmServer, Long killDelayMillis, Long rescanTimeMillis, Log LOG) {
        this.WLMSERVER = wlmServer;
        this.KILL_DELAY_MILLIS = killDelayMillis;
        this.RESCAN_TIME_MILLIS = rescanTimeMillis;
        this.LOG = LOG;
    }

    /**
     * Main thread of execution
     */
    @Override
    public void run() {
        LOG.log("{GARBAGE}Server garbage collector running every " + RESCAN_TIME_MILLIS
                + "ms, removing records more than " + KILL_DELAY_MILLIS + "ms old");
        while(true) {
            try{
                Thread.sleep(RESCAN_TIME_MILLIS);
                collectGarbage();
            }catch(InterruptedException e) {
                LOG.err("{GARBAGE}Unknown IO Error: " + e
                        + "Will attempt to collect garbage again in "
                        + RESCAN_TIME_MILLIS + "ms");
            }
        }
    }

    /**
     * Looks through the sessions variable for unused sessions
     */
    private void collectGarbage() {
        try{
            int removed = 0;
            //Retrieve an updated sessions
            sessions = WLMSERVER.getSessions();

            //Get all the keys
            Iterator<String> iter = sessions.keySet().iterator();

            //Check them all
            while(iter.hasNext()) {
                String key = iter.next();
                WLMSessionData tempSession = sessions.get(key);


                if(System.currentTimeMillis() - tempSession.lastAccessed() > KILL_DELAY_MILLIS) {
                    if(tempSession.getMessenger() != null) {
                        tempSession.getMessenger().logout();
                    }
                    sessions.remove(key);
                    removed++;
                }
            }
            LOG.log("{GARBAGE}" + removed + " records removed. Server using approx " + getMemUse() + "MB of memory");
        }catch(ConcurrentModificationException ex) {
            collectGarbage();//Try again
        }
    }

    /**
     * Returns an estiamte of the servers memory usage
     * @return int containing used memory
     */
    private Long getMemUse() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 100000;
    }
}
