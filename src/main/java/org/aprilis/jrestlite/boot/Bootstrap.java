/*
 * Copyright 2013 JRest Foundation and other contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.aprilis.jrestlite.boot;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.aprilis.jrestlite.compile.Compile;
import org.aprilis.jrestlite.constant.Constants;
import org.aprilis.jrestlite.constant.Exceptions;
import org.aprilis.jrestlite.execute.ExecutionEngine;
import org.aprilis.jrestlite.store.Store;

/**
 * The Bootstrap class initializes JREST. JREST is made up of several components that need to be
 * initialized when the webserver starts. The components include <br/>
 * <br/>
 * <b>Log4J logger -</b> JREST logging system that logs debug messages depending on the log
 * configurations <br/>
 * <br/>
 * <b>Definition store -</b> JREST loads and maintains all the configured JREST APIs in a definition
 * store. When a HTTP request with a JREST key is executed, the store is referred for the details of
 * the JREST key to execute the API <br/>
 * <br/>
 * <b>Compile -</b>
 * 
 * <br>
 * <br>
 * <b>Session store -</b>
 * 
 * <br>
 * <br>
 * <b>Execution engine - </b>
 * 
 * 
 */
public class Bootstrap implements ServletContextListener {
    /**
     * 
     */
    public Bootstrap() {
	String log4jConfigFile = System.getenv(Constants.gsLog4jPropertiesFile);

	if (log4jConfigFile != null && log4jConfigFile.length() > Constants.gshZero) {
	    PropertyConfigurator.configure(log4jConfigFile);
	} else {
	    PropertyConfigurator.configure(Constants.gsDefaultLog4jFileName);
	}// if (log4jConfigFile != null && log4jConfigFile.length() > Constants.gshZero)
    }/* public Bootstrap() */

    /**
     * 
     */
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
	if (moExecutionEngine != null) {
	    moExecutionEngine.freePool();
	}// if (moExecutionEngine != null)
    }/* public void contextDestroyed(ServletContextEvent arg0) */

    /**
     * 
     */
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
	initJrest();
    }/* public void contextInitialized(ServletContextEvent arg0) */

    /**
     * 
     */
    private void initJrest() {
	try {
	    moStore = Store.instance();
	    mLogger.debug(Exceptions.gsDefinitionStoreInitialized);

	    moCompile = Compile.instance();
	    mLogger.debug(Exceptions.gsCompilerInitialized);

	    moExecutionEngine = ExecutionEngine.instance();
	    mLogger.debug(Exceptions.gsExecutionEngineInitialized);

	    if (moStore != null && moExecutionEngine != null
		    && moCompile != null) {
		mthWorker = new Thread(moCompile);

		mLogger.debug(Exceptions.gsJrestInitialized);

		mthWorker.start();

		mLogger.debug(Exceptions.gsSweeperStarted);
	    } else {
		mLogger.debug(Exceptions.gsJrestInitFailed);
	    }// if (moSessionStore != null && moStore != null && ... )
	} catch (Exception e) {
	    e.printStackTrace();
	}// end of try ... catch block
    }/* private void initJrest() */

    /**
     * 
     */
    private Store moStore;

    /**
     * 
     */
    private Compile moCompile;

    /**
     * 
     */
    private ExecutionEngine moExecutionEngine;

    /**
     * 
     */
    private Thread mthWorker;

    /*
     * The logging handle for the system get the log files done.
     */
    private static Logger mLogger = Logger.getLogger(Bootstrap.class.getCanonicalName());

}/* public class Bootstrap implements ServletContextListener */
