<b>JRest</b>

  JRest is a Meta Programming Language that builds REST services on a webserver automatically, using JSON as definition language. 
JRest is built using Java, Jersey and JSON Simple, and is deploy-able on any web servers.

We have put a Google group in place for all of you to interact with us on JRest. 
Feel free to let us know about your thoughts, opinions, corrections, requests etc. over the group forum https://groups.google.com/forum/#!forum/jrest

JRestlite is a lighter version of JRest. It decouples the authentication and session, great choice if you have your own mechanism of authentication and session management.  <br><br>
<b>5 Minutes Guide</b>

<para>
1. Download the source code <br>
2. Stop your web server now, if you are running one! we will start it back in 5 minutes <br>
3. To compile the source, you must have Maven already installed along with Java <br>
4. To generate the war file from source code, go to the j-restlite folder and execute mvn install. This should generate the war file which you need to place it under the webapps folder of your webserver. <br>
5. Set an environment variable by the name <b>JREST_DEFINITION_PATH</b> to any of your favorite path. Depending on your platform you may need to reopen/restart the shell/command prompt. <br>
6. To work with Oracle and Sql Server you need to have their jdbc drivers installed and accessible on <b>CLASSPATH</b>. <br>
7. Make sure you have/create a table called User on your database, with username, name and password columns present in them. This we shal use to demonstrate JRestlite. <br>
8. Now move into JREST_DEFINITION_PATH, and open a new file jrest.json in edit mode, and fill in the details given below (replace the values accordingly). <br>

    {
        
        "JDBC":{
                "Host":"<hostname>",
                "Port":"<database port>",
                "User":"<username>",
                "Pass":"<password>",
                "Db"  :"<database/schema name>",
                "Type":"MySql/PostgreSql/SQLServer/Oracle"
        }
    }

<para>
9. Now open another file users.json in edit mode in JREST_DEFINITION_PATH and put the contents given below <br>

    {
  
          "Users" : {
                  "Query" : "Select username, name, password From User;",
                  "Type" : "GET"
  
          }
    }

    
<para> 
10. Now start your web server or execute mvn jetty:run on the shell prompt (you must be inside the j-restlite directory where you have uncompressed the JRestlite source) <br>
11. Observe the output of web server; your definition files must have loaded successfully. <br>
12. Make sure you have Postman plugin for Google Chrome or REST Client extension for Firefox; this is needed to test the REST service. <br>
13. Create a HTTP POST request with the URL http://localhost:8080/jrestlite/pull  <br>
14. Pass the below header parameters  <br>

    JREST_KEY : Users

<para>
16. There you go! You have successfully interacted with your webserver using j-rest.  <br>
17. On the same lines, execute other definitions. The information needed in case of definitions is that the HTTP request should contain the following information in the Header params <br>

      JREST_KEY : Definition key
      JSON_DATA : Optional data to supply actual values to the query corresponding to the JREST_KEY as shown in the above example.  

