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

import static org.junit.Assert.fail;

import javax.ws.rs.core.Response;

import org.aprilis.jrestlite.constant.HttpCodes;
import org.aprilis.jrestlite.db.ConnectionDetails;
import org.aprilis.jrestlite.execute.Executor;
import org.aprilis.jrestlite.pull.Pull;
import org.aprilis.jrestlite.store.Definition;
import org.aprilis.jrestlite.store.Store;
import org.junit.Test;

public class TestPull {

    public TestPull() {
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

	moPull = new Pull();
	moResponse = null;
	msJsonResponse = null;
	moPullDefinition = null;

	msJsonData = "{ \"1\" : \"One\", \"2\" : \"Two\"}";
	msJrestKey = "TEST_PULL";
	msQuery = "SELECT 'One' As ColumnOne FROM DUAL WHERE 'One' = ? AND 'Two' = ?;";
    }

    @Test
    public void testNonExistentKey() {
	
	msJrestKey = "NON_EXISTENT_KEY";
	moResponse = moPull.executePull(msJrestKey, msJsonData);

	if (moResponse.getStatus() != HttpCodes.NOT_FOUND) {
	    fail("testNonExistentKey failed. Expected return status " + HttpCodes.NOT_FOUND
		    + " Found " + moResponse.getStatus());
	}
    }


    @Test
    public void testQuerySyntaxError() {

	moPullDefinition = new Definition();
	moPullDefinition.setQuery("SELECT 'One' FAROM DUAL;");
	moDefinitionStore.addGetDefinition(msJrestKey, moPullDefinition);

	moResponse = moPull.executePull(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.UNPROCESSABLE_ENTITY) {
	    fail("testQuerySyntaxError failed. Expected " + HttpCodes.UNPROCESSABLE_ENTITY
		    + " Found " + moResponse.getStatus());
	}
    }

    @Test
    public void testQueryNoBindParam() {

	moPullDefinition = new Definition();
	moPullDefinition.setQuery("SELECT 'One' AS ColumnOne FROM DUAL;");
	moDefinitionStore.addGetDefinition(msJrestKey, moPullDefinition);

	msJsonResponse = "[{\"ColumnOne\":\"One\"}]";
	moResponse = moPull.executePull(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.OK) {
	    fail("testQueryNoBindParam failed. Expected " + HttpCodes.OK + " Found "
		    + moResponse.getStatus());
	} else if (moResponse.getEntity().toString().equals(msJsonResponse) == false) {
	    fail("testQueryNoBindParam failed. Expected " + msJsonResponse + " Found "
		    + moResponse.getEntity().toString());
	}
    }

    @Test
    public void testQueryWithBindParam() {

	moPullDefinition = new Definition();
	moPullDefinition.setQuery(msQuery);
	moDefinitionStore.addGetDefinition(msJrestKey, moPullDefinition);

	msJsonResponse = "[{\"ColumnOne\":\"One\"}]";
	moResponse = moPull.executePull(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.OK) {
	    fail("testQueryWithBindParam failed. Expected " + HttpCodes.OK + " Found "
		    + moResponse.getStatus());
	} else if (moResponse.getEntity().toString().equals(msJsonResponse) == false) {
	    fail("testQueryWithBindParam failed. Expected " + msJsonResponse + " Found "
		    + moResponse.getEntity().toString());
	}
    }

    @Test
    public void testQueryWithBindParamMismatch() {
	
	moPullDefinition = new Definition();
	moPullDefinition.setQuery(msQuery);
	moDefinitionStore.addGetDefinition(msJrestKey, moPullDefinition);

	msJsonData = "{ \"1\" : \"One\"}";
	moResponse = moPull.executePull(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.UNPROCESSABLE_ENTITY) {
	    fail("testQueryWithBindParamMismatch failed. Expected "
		    + HttpCodes.UNPROCESSABLE_ENTITY + " Found " + moResponse.getStatus());
	}
    }

    @Test
    public void testInsertQueryInPull() {
	Executor executor = new Executor((short) 0);
	executor.initialize();

	executor.execute("CREATE TABLE darwin.TEMPTABLE (COLUMN_ONE TEXT, COLUMN_TWO TEXT);");

	moPullDefinition = new Definition();
	moPullDefinition.setQuery("INSERT INTO TEMPTABLE VALUES('1', ?);");

	moDefinitionStore.addGetDefinition(msJrestKey, moPullDefinition);
	moResponse = moPull.executePull(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.UNPROCESSABLE_ENTITY) {
	    System.out.println(moResponse.getEntity().toString());
	    fail("testInsertQueryInPull failed. Expected " + HttpCodes.UNPROCESSABLE_ENTITY
		    + " Found " + moResponse.getStatus());
	}

	executor.execute("DROP TABLE TEMPTABLE;");
	executor.unInitialize();
    }

    @Test
    public void testBeforeOnly() {
	moPullDefinition = new Definition();
	moPullDefinition.setQuery(msQuery);
	moPullDefinition.setFqcnBefore("org.aprilis.sample.TestBeforeAfter");
	moPullDefinition.setBeforeMethod("sayHelloBefore");

	msJsonResponse = "[{\"ColumnOne\":\"One\"}]";
	moDefinitionStore.addGetDefinition(msJrestKey, moPullDefinition);
	moResponse = moPull.executePull(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.OK) {
	    System.out.println(moResponse.getEntity().toString());

	    fail("testBeforeOnly failed. Expected " + HttpCodes.OK + " Found "
		    + moResponse.getStatus());
	} else if (moResponse.getEntity().toString().equals(msJsonResponse) == false) {
	    fail("testBeforeOnly failed. Expected " + msJsonResponse + " Found "
		    + moResponse.getEntity().toString());
	}
    }

    @Test
    public void testAfterOnly() {
	moPullDefinition = new Definition();
	moPullDefinition.setQuery(msQuery);
	moPullDefinition.setFqcnAfter("org.aprilis.sample.TestBeforeAfter");
	moPullDefinition.setAfterMethod("sayHelloAfter");

	msJsonResponse = "[{\"ColumnOne\":\"One\"}]";
	moDefinitionStore.addGetDefinition(msJrestKey, moPullDefinition);
	moResponse = moPull.executePull(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.OK) {
	    System.out.println(moResponse.getEntity().toString());

	    fail("testAfterOnly failed. Expected " + HttpCodes.OK + " Found "
		    + moResponse.getStatus());
	} else if (moResponse.getEntity().toString().equals(msJsonResponse) == false) {
	    fail("testAfterOnly failed. Expected " + msJsonResponse + " Found "
		    + moResponse.getEntity().toString());
	}
    }

    @Test
    public void testBeforeAndAfter() {
	moPullDefinition = new Definition();
	moPullDefinition.setQuery(msQuery);
	moPullDefinition.setFqcnBefore("org.aprilis.sample.TestBeforeAfter");
	moPullDefinition.setBeforeMethod("sayHelloBefore");
	moPullDefinition.setFqcnAfter("org.aprilis.sample.TestBeforeAfter");
	moPullDefinition.setAfterMethod("sayHelloAfter");

	msJsonResponse = "[{\"ColumnOne\":\"One\"}]";
	moDefinitionStore.addGetDefinition(msJrestKey, moPullDefinition);
	moResponse = moPull.executePull(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.OK) {
	    fail("testBeforeAndAfter failed. Expected " + HttpCodes.OK + " Found "
		    + moResponse.getStatus());
	} else if (moResponse.getEntity().toString().equals(msJsonResponse) == false) {
	    fail("testBeforeAndAfter failed. Expected " + msJsonResponse + " Found "
		    + moResponse.getEntity().toString());
	}
    }

    @Test
    public void testBeforeThrowsException() {
	moPullDefinition = new Definition();
	moPullDefinition.setQuery(msQuery);
	moPullDefinition.setFqcnBefore("org.aprilis.sample.TestBeforeAfter");
	moPullDefinition.setBeforeMethod("beforeThrowsException");

	moDefinitionStore.addGetDefinition(msJrestKey, moPullDefinition);
	moResponse = moPull.executePull(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.PRECONDITION_FAILURE) {
	    fail("testBeforeThrowsException failed. Expected return status "
		    + HttpCodes.PRECONDITION_FAILURE + " Found " + moResponse.getStatus());
	}
    }

    @Test
    public void testAfterThrowsException() {
	moPullDefinition = new Definition();
	moPullDefinition.setQuery(msQuery);
	moPullDefinition.setFqcnAfter("org.aprilis.sample.TestBeforeAfter");
	moPullDefinition.setAfterMethod("afterThrowsException");

	moDefinitionStore.addGetDefinition(msJrestKey, moPullDefinition);
	moResponse = moPull.executePull(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.EXPECTATION_FAILED) {
	    System.out.println(moResponse.getEntity().toString());
	    fail("testAfterThrowsException failed. Expected return status "
		    + HttpCodes.EXPECTATION_FAILED + " Found " + moResponse.getStatus());
	}
    }
    
    @Test
    public void testGarbageJsonData() {

	moPullDefinition = new Definition();
	moPullDefinition.setQuery(msQuery);
	moDefinitionStore.addGetDefinition(msJrestKey, moPullDefinition);

	msJsonData = "{ \"Hi\" : \"One\", \"Bye\" : \"Two\"}";
	moResponse = moPull.executePull(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.UNPROCESSABLE_ENTITY) {
	    fail("testQueryWithBindParamMismatch failed. Expected "
		    + HttpCodes.UNPROCESSABLE_ENTITY + " Found " + moResponse.getStatus());
	}
    }

    @Test
    public void testGetZeroRows() {

	moPullDefinition = new Definition();
	moPullDefinition.setQuery(msQuery);
	moDefinitionStore.addGetDefinition(msJrestKey, moPullDefinition);

	msJsonData = "{ \"1\" : \"Won\", \"2\" : \"Too\" }";
	moResponse = moPull.executePull(msJrestKey, msJsonData);
	if (moResponse.getStatus() != HttpCodes.NO_CONTENT) {
	    fail("testQueryWithBindParamMismatch failed. Expected "
		    + HttpCodes.NO_CONTENT + " Found " + moResponse.getStatus());
	}
    }

    private Pull moPull;
    private Definition moPullDefinition;
    private String msJrestKey;
    private Store moDefinitionStore;
    private String msQuery;
    private Response moResponse;
    private String msJsonResponse;
    private String msJsonData;
}
