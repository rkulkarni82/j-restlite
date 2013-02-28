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
package org.aprilis.jrestlite.store;


public class Definition {
    /**
     * 
     */
    public Definition() {
	msQuery = null;
	
	mbUseResultFromBefore = false;
	msBeforeMethod = null;
	msAfterMethod = null;
    }/* public Definition() */

    /**
     * Returns the SQL query string which was already set
     * 
     * @return null if nothing is set otherwise a valid SQL is returned
     */
    public String getQuery() {
	return msQuery;
    }/* public String getQuery() */

    public void setQuery(String sQuery) {
	msQuery = sQuery;
    }/* public void setQuery(String sQuery) */

    public void setFqcnAfter(String sAfter) {
	if (sAfter != null) {
	    msFqcnAfter = sAfter;
	}
    }/* public void setFqcnAfter(String sAfter) */

    public String getFqcnAfter() {
	return msFqcnAfter;
    }/* public String getFqcnAfter() */

    public void setFqcnBefore(String sBefore) {
	if (sBefore != null) {
	    msFqcnBefore = sBefore;
	}
    }/* public void setFqcnBefore(String sBefore) */
    
    public void setBeforeUsagePattern(boolean bConsumeOutptOfBefore) {
	mbUseResultFromBefore = bConsumeOutptOfBefore;
    }/* public void setBeforeUsagePattern(boolean bConsumeOutptOfBefore) */

    public boolean useResultFromBefore() {
	return mbUseResultFromBefore;
    }/* public boolean useResultFromBefore () */
    
    public String getFqcnBefore() {
	return msFqcnBefore;
    }/* public String getFqcnBefore() */

    public void setBeforeMethod(String sMethod) {
	msBeforeMethod = sMethod;
    }/* public void setBeforeMethod(String sMethod)  */
    
    public String getBeforeMethod() {
	return msBeforeMethod;
    }/* public String getBeforeMethod() */
    
    public void setAfterMethod(String sMethod) {
	msAfterMethod = sMethod;
    }/* public void setAfterMethod(String sMethod) */
    
    public String getAfterMethod() {
	return msAfterMethod;
    }/* public String getAfterMethod() */
    
    
    /**
     * String to which SQL query would be stored
     */
    private String msQuery;

    /**
     * String which would hold the Java API that must be called before we actually make a call to
     * the desired REST definition
     */
    private String msFqcnBefore;

    /**
     * String which would hold the Java API that must be called after we actually make a call to
     * the desired REST definition
     */
    private String msFqcnAfter;
    
    /**
     * 
     */
    private boolean mbUseResultFromBefore;
    
    /**
     * 
     */
    private String msBeforeMethod;
    
    /**
     * 
     */
    private String msAfterMethod;
}/* public class Definition */
