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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

    private final boolean logging;
    private final PrintStream out;
    private final PrintStream err;
    private final DateFormat dateFormat;
    private Date date;

    private BufferedWriter fileOut;
    private boolean screenLogging;
    private boolean fileLogging;


    /**
     * Constructor
     * @param logging set to true for logging to be enabled
     */
    public Log(String FILE_PATH, boolean logging, boolean screenLogging, boolean fileLogging) {
        this.logging = logging;
        this.out = System.out;
        this.err = System.err;
        this.dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        this.screenLogging = screenLogging;
        this.fileLogging = fileLogging;
        
        //Setup file writing
        try {
            fileOut = new BufferedWriter(new FileWriter(FILE_PATH));
        } catch (IOException ex) {
            err.println("UNABLE TO WRITE TO FILE. Writing to screen..." + ex);
        }
    }


    /**
     * An unrecoverable error
     * @param message to be displayed at console
     * @param exitCode
     */
    public void err(String message, int exitCode) {
        date = new Date();
        
        //Write to file
        if(fileLogging) {
            try {
                fileOut.write("[" + dateFormat.format(date) + "]" + message + "\n");
                fileOut.flush();
            } catch (IOException ex) {
                err.println("UNABLE TO WRITE TO FILE. Writing to screen..." + ex);
                screenLogging = true;
            }
        }

        //Write to screen
        if(screenLogging)
            err.println("[" + dateFormat.format(date) + "]" + message);

        //Exit
        System.exit(exitCode);
    }


    /**
     * A recoverable error
     * @param message to be displayed at console
     */
    public void err(String message) {
        date = new Date();
        
        //Write to file
        if(fileLogging) {
            try {
                fileOut.write("[" + dateFormat.format(date) + "]" + message + "\n");
                fileOut.flush();
            } catch (IOException ex) {
                err.println("UNABLE TO WRITE TO FILE. Writing to screen... " + ex);
                screenLogging = true;
            }
        }

        //Write to screen
        if(screenLogging)
            err.println("[" + dateFormat.format(date) + "]" + message);
    }


    /**
     * Log entry
     * @param message to be displayed at console
     */
    public void log(String message) {
        if(logging) {
            date = new Date();

            if(fileLogging) {
                //Write to file
                try {
                    fileOut.write("[" + dateFormat.format(date) + "]" + message + "\n");
                    fileOut.flush();
                } catch (IOException ex) {
                    err.println("UNABLE TO WRITE TO FILE. Writing to screen... " + ex);
                    screenLogging = true;
                }
            }

            //Write to screen
            if(screenLogging)
                out.println("[" + dateFormat.format(date) + "]" + message);
        }
    }

    
    /**
     * Close down writer
     */
    public void close() {
        out.close();
        err.close();
        try {
            fileOut.close();
        } catch (IOException ex) {
            err.println("UNABLE TO CLOSE FILE: " + ex);
        }
    }
}
