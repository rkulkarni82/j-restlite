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
package org.aprilis.jrestlite;

import static org.junit.Assert.*;

import javax.ws.rs.core.Response;

import org.aprilis.jrestlite.constant.HttpCodes;
import org.aprilis.jrestlite.db.ConnectionDetails;
import org.aprilis.jrestlite.execute.Executor;
import org.aprilis.jrestlite.push.Push;
import org.aprilis.jrestlite.store.Definition;
import org.aprilis.jrestlite.store.Store;
import org.junit.Test;

public class TestPush {

    public TestPush() {
	moDefinitionStore = Store.instance();
	moDefinitionStore.clearAllDefinitions();
	
	ConnectionDetails dbConnDetails = new ConnectionDetails();
	dbConnDetails.setDatabaseType("MySql");
	dbConnDetails.setHostName("localhost");
	dbConnDetails.setPortNumber("3306");
	dbConnDetails.setDatabaseName("darwin");
	dbConnDetails.setUserName("root");
	dbConnDetails.setPassWord("xmc4vhcf");

	moDefinitionStore.setJdbcConnectionDetails(dbConnDetails);

	moDefinitionStore.setSystemToReadyState();

	moPush = new Push();
	moResponse = null;

	msJsonData = "{ \"1\" : \"One\", \"2\" : \"Two\"}";
	msJrestKey = "TEST_PUSH";
	moPushDefinition = null;
    }

    private void createTempTable() {

	Executor executor = new Executor((short) 0);
	executor.initialize();

	executor.execute("CREATE TABLE darwin.TEMPTABLE (COLUMN_ONE TEXT, COLUMN_TWO TEXT);");
	executor.unInitialize();
    }

    private void dropTempTable() {

	Executor executor = new Executor((short) 0);
	executor.initialize();

	executor.execute("DROP TABLE TEMPTABLE;");
	executor.unInitialize();
    }

    @Test
    public void testNonExistentKey() {
	msJrestKey = "NON_EXISTENT_KEY";
	moResponse = moPush.executePush(msJrestKey, msJsonData);

	if (moResponse.getStatus() != HttpCodes.NOT_FOUND) {
	    fail("testNonExistentKey failed. Expected " + HttpCodes.NOT_FOUND + " Found "
		    + moResponse.getStatus());
	}
    }


    @Test
    public void testQuerySyntaxError() {
	createTempTable();

	moPushDefinition = new Definition();
	moPushDefinition.setQuery("INSERT INTO TEMPTABLE VALUES('1, 'One'");
	moDefinitionStore.addSetDefinition(msJrestKey, moPushDefinition);

	moResponse = moPush.executePush(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.UNPROCESSABLE_ENTITY) {
	    fail("testQuerySyntaxError failed. Expected " + HttpCodes.UNPROCESSABLE_ENTITY
		    + " Found " + moResponse.getStatus());
	}
    }

    @Test
    public void testQueryNoBindParam() {
	moPushDefinition = new Definition();
	moPushDefinition.setQuery("INSERT INTO TEMPTABLE VALUES('1', 'One');");
	moDefinitionStore.addSetDefinition(msJrestKey, moPushDefinition);

	moResponse = moPush.executePush(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.OK) {
	    fail("testQueryNoBindParam failed. Expected " + HttpCodes.OK + " Found "
		    + moResponse.getStatus());
	}
    }

    @Test
    public void testQueryWithBindParam() {
	moPushDefinition = new Definition();
	moPushDefinition.setQuery("INSERT INTO TEMPTABLE VALUES('1', ?);");
	moDefinitionStore.addSetDefinition(msJrestKey, moPushDefinition);

	moResponse = moPush.executePush(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.OK) {
	    fail("testQueryWithBindParam failed. Expected " + HttpCodes.OK + " Found "
		    + moResponse.getStatus());
	}

    }

    @Test
    public void testQueryWithBindParamMismatch() {

	moPushDefinition = new Definition();
	moPushDefinition.setQuery("INSERT INTO TEMPTABLE VALUES('1', ?);");
	moDefinitionStore.addSetDefinition(msJrestKey, moPushDefinition);

	msJsonData = "{ \"2\" : \"Hello\"}";
	moResponse = moPush.executePush(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.UNPROCESSABLE_ENTITY) {
	    fail("testQueryWithBindParamMismatch failed. Expected "
		    + HttpCodes.UNPROCESSABLE_ENTITY + " Found " + moResponse.getStatus());
	}
    }

    @Test
    public void testBeforeOnly() {
	moPushDefinition = new Definition();
	moPushDefinition.setQuery("INSERT INTO TEMPTABLE VALUES('1', ?);");
	moPushDefinition.setFqcnBefore("org.aprilis.sample.TestBeforeAfter");
	moPushDefinition.setBeforeMethod("sayHelloBefore");

	moDefinitionStore.addSetDefinition(msJrestKey, moPushDefinition);
	moResponse = moPush.executePush(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.OK) {
	    fail("testBeforeOnly failed. Expected " + HttpCodes.OK + " Found "
		    + moResponse.getStatus());
	}
    }

    @Test
    public void testBeforeThrowsException() {
	moPushDefinition = new Definition();
	moPushDefinition.setQuery("INSERT INTO TEMPTABLE VALUES('1', ?);");
	moPushDefinition.setFqcnBefore("org.aprilis.sample.TestBeforeAfter");
	moPushDefinition.setBeforeMethod("beforeThrowsException");

	moDefinitionStore.addSetDefinition(msJrestKey, moPushDefinition);
	moResponse = moPush.executePush(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.PRECONDITION_FAILURE) {
	    fail("testBeforeThrowsException failed. Expected return status "
		    + HttpCodes.PRECONDITION_FAILURE + " Found " + moResponse.getStatus());
	}
    }

    @Test
    public void testAfterThrowsException() {
	moPushDefinition = new Definition();
	moPushDefinition.setQuery("INSERT INTO TEMPTABLE VALUES('1', ?);");
	moPushDefinition.setFqcnAfter("org.aprilis.sample.TestBeforeAfter");
	moPushDefinition.setAfterMethod("afterThrowsException");

	moDefinitionStore.addSetDefinition(msJrestKey, moPushDefinition);
	moResponse = moPush.executePush(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.EXPECTATION_FAILED) {
	    fail("testAfterThrowsException failed. Expected return status "
		    + HttpCodes.EXPECTATION_FAILED + " Found " + moResponse.getStatus());
	}
    }

    @Test
    public void testAfterOnly() {
	moPushDefinition = new Definition();
	moPushDefinition.setQuery("INSERT INTO TEMPTABLE VALUES('1', ?);");
	moPushDefinition.setFqcnAfter("org.aprilis.sample.TestBeforeAfter");
	moPushDefinition.setAfterMethod("sayHelloAfter");

	moDefinitionStore.addSetDefinition(msJrestKey, moPushDefinition);
	moResponse = moPush.executePush(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.OK) {
	    fail("testAfterOnly failed. Expected " + HttpCodes.OK + " Found "
		    + moResponse.getStatus());
	}
    }

    @Test
    public void testBeforeAndAfter() {
	moPushDefinition = new Definition();
	moPushDefinition.setQuery("INSERT INTO TEMPTABLE VALUES(?, ?);");
	moPushDefinition.setFqcnBefore("org.aprilis.sample.TestBeforeAfter");
	moPushDefinition.setBeforeMethod("sayHelloBefore");
	moPushDefinition.setFqcnAfter("org.aprilis.sample.TestBeforeAfter");
	moPushDefinition.setAfterMethod("sayHelloAfter");

	moDefinitionStore.addSetDefinition(msJrestKey, moPushDefinition);
	moResponse = moPush.executePush(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.OK) {
	    fail("testBeforeAndAfter failed. Expected " + HttpCodes.OK + " Found "
		    + moResponse.getStatus());
	}
    }

    @Test
    public void testGarbageJsonData() {
	moPushDefinition = new Definition();
	moPushDefinition.setQuery("INSERT INTO TEMPTABLE VALUES('1', ?);");
	moDefinitionStore.addSetDefinition(msJrestKey, moPushDefinition);

	msJsonData = "{ \"Hi\" : \"Hello\"}";
	moResponse = moPush.executePush(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.UNPROCESSABLE_ENTITY) {
	    fail("testQueryWithBindParamMismatch failed. Expected "
		    + HttpCodes.UNPROCESSABLE_ENTITY + " Found " + moResponse.getStatus());
	}
    }

    @Test
    public void testUpdateZeroRows() {
	moPushDefinition = new Definition();
	moPushDefinition.setQuery("UPDATE TEMPTABLE SET COLUMN_TWO = ? WHERE COLUMN_ONE = ?;");
	moDefinitionStore.addSetDefinition(msJrestKey, moPushDefinition);

	moResponse = moPush.executePush(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.UNPROCESSABLE_ENTITY) {
	    fail("testQueryWithBindParamMismatch failed. Expected "
		    + HttpCodes.UNPROCESSABLE_ENTITY + " Found " + moResponse.getStatus());
	} else if (moResponse.getEntity() != null) {
	    System.out
		    .println("testUpdateZeroRows response = " + moResponse.getEntity().toString());
	}

	/*
	 * NOTE: To be added in the last test case to drop the temp table
	 */
	dropTempTable();
    }

    private Push moPush;
    private Definition moPushDefinition;
    private String msJrestKey;
    private Store moDefinitionStore;
    private Response moResponse;
    private String msJsonData;
}
