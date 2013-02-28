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
package org.aprilis.jrestlite.push;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.aprilis.jrestlite.compile.QueryBinder;
import org.aprilis.jrestlite.compile.Reflect;
import org.aprilis.jrestlite.constant.Constants;
import org.aprilis.jrestlite.constant.Exceptions;
import org.aprilis.jrestlite.constant.HttpCodes;
import org.aprilis.jrestlite.execute.ExecutionEngine;
import org.aprilis.jrestlite.execute.Executor;
import org.aprilis.jrestlite.store.Definition;
import org.aprilis.jrestlite.store.Store;

@Path("/push")
public class Push {
    /**
     * 
     */
    public Push() {
	moExecutor = null;
	msSqlQuery = null;

	moReflect = new Reflect();
	moQueryBinder = new QueryBinder();
	moStringWriter = new StringWriter();
	moPrintWriter = new PrintWriter(moStringWriter);

	moStore = Store.instance();
	moExecutionEngine = ExecutionEngine.instance();
    }/* public Push() */

    /**
     * 
     * @param sessionKey
     * @param jrestKey
     * @param jsonData
     * @return
     */
    @POST
    public Response executePush(
	    @HeaderParam(Constants.JREST_KEY) String jrestKey,
	    @DefaultValue(Constants.DEFAULT_JSON_DATA) @HeaderParam(Constants.JSON_DATA) String jsonData) {
	if (moStore.isSystemInReadyState() == false) {
	    mLogger.fatal(Exceptions.gsSystemInHaltState);

	    return Response.status(HttpCodes.SERVICE_UNAVAILABLE)
		    .entity(Exceptions.gsSystemInHaltState).build();
	}// if (moSessionStore.isSystemInReadyState() == false)

	try {
	    // 1. Check all the parameter except jsonData - Done
	    // 4. Build query - Done
	    // 5. Acquire executor - Done
	    // 6. Execute - Done
	    // 7. Release executor - Done
	    // 8. Form response base on execute state and return - Done
	    //
	    mLogger.debug(String.format(Exceptions.gsInfoJrestKey, jrestKey));

	    if (jrestKey != null) {
		Definition jrestDefinition = moStore.getDefinition(jrestKey, false);

		if (jrestDefinition != null) {
		    moReflect.setDefinition(jrestDefinition);
		    moReflect.setRestJsonData(jsonData);

		    if (jrestDefinition.getFqcnBefore() != null) {
			/*
			 * Execute Before method if it has been configured. If the before method is
			 * not successful, we expect it to throw an exception which we catch and
			 * halt processing of the request.
			 */
			try {
			    String sBeforeMethodResult = moReflect.executeBeforeMethod();

			    /*
			     * If before method is configured and if its results is to be consumed
			     * by JRest consume the result of beforeMethod and pass it to the
			     * QueryBinder class.
			     */
			    if (jrestDefinition.useResultFromBefore() == true) {
				if (sBeforeMethodResult != null) {
				    msSqlQuery = moQueryBinder.buildQueryForKey(jrestKey,
					    sBeforeMethodResult, false);
				} else {
				    mLogger.error(Exceptions.gsBeforeMethodOutputIsNull);

				    return Response.status(HttpCodes.PRECONDITION_FAILURE)
					    .entity(Exceptions.gsBeforeMethodOutputIsNull).build();
				}// if (sBeforeMethodResult != null)
			    } else {
				// Do not consume output of Before method
				msSqlQuery = moQueryBinder.buildQueryForKey(jrestKey, jsonData,
					false);
			    }// if (jrestDefinition.useResultFromBefore() == true)
			} catch (Exception e) {

			    mLogger.error(Exceptions.gsBeforeMethodFailed);

			    e.printStackTrace(moPrintWriter);
			    mLogger.error(moStringWriter.toString());

			    return Response.status(HttpCodes.PRECONDITION_FAILURE)
				    .entity(Exceptions.gsBeforeMethodFailed).build();
			}// end of try .. catch block
		    } else {
			/*
			 * If before method was not configured, the original Json data is passed to
			 * the QueryBinder.
			 */
			msSqlQuery = moQueryBinder.buildQueryForKey(jrestKey, jsonData, false);

		    }// if (jrestDefinition.getFqcnBefore() != null)

		    if (msSqlQuery == null) {
			mLogger.error(Exceptions.gsUnProcessableQuery);

			return Response.status(HttpCodes.UNPROCESSABLE_ENTITY)
				.entity(Exceptions.gsUnProcessableQuery).build();
		    }// if (sqlQuery == null)

		    mLogger.debug(String.format(Exceptions.gsFormedSqlQuery, msSqlQuery));

		    // Acquire executor handle from the pool engine
		    moExecutor = moExecutionEngine.acquireExecutorFromPool();

		    if (moExecutor != null) {
			// Trigger the query and check whether it was successful or not
			if (moExecutor.execute(msSqlQuery)) {
			    moExecutionEngine.releaseExecutorToPool(moExecutor);

			    /*
			     * Execute After method if it has been configured. If the after method
			     * is not successful, we expect it to throw an exception which we catch
			     * and return error.
			     */
			    if (jrestDefinition.getFqcnAfter() != null) {
				try {
				    String sAfterMethodResult = moReflect.executeAfterMethod(null);

				    return Response.status(HttpCodes.OK).entity(sAfterMethodResult)
					    .build();
				} catch (Exception e) {

				    mLogger.error(Exceptions.gsAfterMethodFailed);

				    e.printStackTrace(moPrintWriter);
				    mLogger.error(moStringWriter.toString());

				    return Response.status(HttpCodes.EXPECTATION_FAILED)
					    .entity(Exceptions.gsAfterMethodFailed).build();
				}// end of try .. catch block
			    } else {
				return Response.status(HttpCodes.OK).build();
			    }// if (jrestDefinition.getFqcnAfter() != null)

			} else {
			    mLogger.error(String.format(Exceptions.gsDmlResultedInVoid, msSqlQuery));

			    moExecutionEngine.releaseExecutorToPool(moExecutor);

			    return Response
				    .status(HttpCodes.UNPROCESSABLE_ENTITY)
				    .entity(String.format(Exceptions.gsDmlResultedInVoid,
					    msSqlQuery)).build();
			}// if (moExecutor.execute(sqlQuery))
		    } else {
			return Response.status(HttpCodes.SERVICE_UNAVAILABLE)
				.entity(Exceptions.gsNoFreeExecutorsAvailable).build();
		    }// if (moExecutor != null)

		}// if (jrestDefinition != null)

		mLogger.error(String.format(Exceptions.gsNoDefinitionFound, jrestKey));

		return Response.status(HttpCodes.NOT_FOUND)
			.entity(String.format(Exceptions.gsNoDefinitionFound, jrestKey)).build();

	    }// if (jrestKey != null)
	} catch (Exception e) {
	    e.printStackTrace();

	    if (moExecutor != null) {
		mLogger.error(Exceptions.gsExecutorLeakFixed);

		moExecutionEngine.releaseExecutorToPool(moExecutor);
	    }// if(moExecutor != null)

	    e.printStackTrace(moPrintWriter);

	    mLogger.error(moStringWriter.toString());
	}// end of try ... catch block

	return Response.status(HttpCodes.FORBIDDEN).build();
    }/* public Response executePush(...) */

    /**
     * 
     */
    private String msSqlQuery;

    /**
     * 
     */
    private Reflect moReflect;

    /**
     * 
     */
    private Store moStore;

    /**
     * 
     */
    private QueryBinder moQueryBinder;

    /**
     * 
     */
    private Executor moExecutor;

    /**
     * 
     */
    private ExecutionEngine moExecutionEngine;

    /*
     * The logging handle for the system get the log files done.
     */
    private static Logger mLogger = Logger.getLogger(Push.class.getCanonicalName());

    /**
     * 
     */
    private StringWriter moStringWriter;

    /**
     * 
     */
    private PrintWriter moPrintWriter;
}/* public class Push */
