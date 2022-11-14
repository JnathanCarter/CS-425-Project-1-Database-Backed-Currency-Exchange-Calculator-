# CS-425-Project-1-Database-Backed-Currency-Exchange-Calculator-
cs425_p1_db_fa22.sql

Download cs425_p1_db_fa22.sql

cs425_p1_db_fa22.pdf

Download cs425_p1_db_fa22.pdf

For your first project, you will be implementing your own database-backed version of the Currency Exchange Calculator and API from Lab #4.  Recall that, in Lab #4, your Web-based client sent an AJAX request directly to the following Web API to retrieve the latest currency exchange data, which it proceeded to use in its calculations:

https://testbed.jaysnellen.com:8443/JSUExchangeRatesServer/rates

Links to an external site. (for a list of currency exchange rates)

https://testbed.jaysnellen.com:8443/JSUExchangeRatesServer/currencies

Links to an external site. (for a list of recognized currency codes)

In this version, you will implement your own API to service these requests instead, along with a Web-based client to provide an interface to the user.  Your API will act as a "work-alike" of the original API, but with additional features; the Web-based client will be a reuse of your completed code from Lab #4, modified to use your API instead of the original.

I recommend that you create your new project in NetBeans; select "New Project" from the NetBeans menu and choose "Java with Ant | Java Web | Web Application" from the list of categories and projects.

Here is how your completed application should function.  The user opens the Web application, enters the date and the original value (in US Dollars), and selects the target rate from the drop-down list, in the corresponding form fields.  When the user clicks "Convert," the client should send an AJAX request to your API.  Your API should first attempt to retrieve the exchange data for the specified date from its own database.  If the exchange rates are found in the database, they should be encoded as JSON data and returned to the client.  If the exchange rates are not found in the database, your server-side code should initiate an HTTP request to the original API (remember to specify the date as a query string parameter!).  Once the collection of rates has been retrieved as JSON data and decoded, the rates should then be added to your API's database, so that future requests for this data do not need to involve the original API again.  The data can then be retrieved from your database and sent to the client as usual, to service the original request.

(To make it easier for you to reuse your client code from Lab #4, your JSON data should be formatted to exactly match the format used by the original API.)


Part 1: Server-Side Components (Version 1)

To begin, create the MySQL database by importing the attached SQL backup file.  This database contains two tables: a "currency" table containing a list of valid currency symbols and their descriptions, and a "rate" table where you will be adding your exchange data.  The "rate" table uses foreign keys linked to the "currency" table for validation purposes, so only recognized currency codes can be used.  Also, because the "rate" table uses a composite primary key (consisting of a currency code paired with a date), it should not be possible to add duplicate entries for the same currency code on the same date.  To give you a starting set of data, the database has been "pre-loaded" with exchange rate data for October 23rd, 2022.

Import the database, and grant the necessary permissions to a new MySQL user account, by entering the following four commands one at a time at the MySQL prompt:

source C:\Users\JSU\Desktop\cs425_p1_db_fa22.sql
create user 'p1_user'@'localhost' identified by 'CS425!p1user';
grant select, insert, update, delete on cs425_p1_db.* to 'p1_user'@'localhost';
flush privileges;

(Of course, you should make any necessary changes to the path to the SQL file.  Also, feel free to use a different username and password for the MySQL account; these are provided only as examples.)

As you work on the server-side components, refer to the "Database Programming" and "JSON" lecture notes.  The "JSON" notes provide examples of encoding and decoding JSON data (using the JSON.simple libraries) and using HTTP requests in Java, while the "Database Programming" notes provide useful examples for working with JDBC, connection pooling in Tomcat, and creating and using DAOs.  (In particular, you should refer to the examples in the "Database Programming" notes for committing database updates in batches.  The processes of iterating through your new exchange data, and adding this data to your database, can be done in a single loop, very similar to the one shown in the examples.)

All database-related operations in your server-side code must be concentrated in separate DAO classes, separate from your servlet classes.  In addition, your application must use prepared statements and parametrized queries, and it must acquire a connection to the database from a connection pool, as discussed in the lecture notes and as illustrated in the Example Web Applications provided on Canvas.  I recommend using diagnostic prints to the server console to indicate when new exchange data is being retrieved from the API.  This may make it easier for you to determine whether your server-side code is correctly populating and using its database.  Remember, your application should need to retrieve the exchange data from the API for a given date only once!

HINT: Although you should use DAO classes and a DAO factory, as we have done in our earlier assignments, note that the data from the database (the list of rates, and the list of currencies) will only be returned to the servlets and sent directly to the client as JSON data.  So, for this application, there is no need to create separate model classes for the rates and currencies; the DAO methods can return JSON data directly.

When the time comes to test your API, remember that you can initiate HTTP requests from the command line using tools such as cURL and HTTPie.  As we have seen in our earlier work, this is a useful way to view and log the data returned by your servlets in response to all types of HTTP requests, without having to construct the entire Web application first.  Just be sure that you have built and deployed your application, and that the Tomcat server is running on your workstation, before attempting to send these requests to your servlet URL(s).  You should not proceed to working on the client-side components (in Part 3) until you have verified that the requests are being received and processed correctly.

 

Part 2: Server-Side Components (Version 2)

Once you have the basic API working, the next step is to add the ability to use RESTful service URLs, to allow the user to identify resources using paths.  For example, instead of requesting a specific currency rate on a given date with query string parameters, like this ...

http://localhost:8180/CS425_Project1/rates?date=2022-10-23?currency=GBP

... the user should be able to specify the resource with a path, like this ...

http://localhost:8180/CS425_Project1/rates/2022-10-23/GBP

Notice that the path follows the pattern "rates/date/currency", so that by adding steps to the path, the user can "drill down" to smaller subsets of the data.  If the user omits the "date" and "currency", the API should return all rates for today's date; if the user specifies the "date" but not the "currency", it should return all rates for the specified date.  In your servlet code, you can retrieve the request path and split it as follows:

String uri = request.getRequestURI();
String[] path = uri.split("/");

Next, add the requirement for the user to specify an access key using a query string parameter, like this:

http://localhost:8180/CS425_Project1/rates?key=1881efa4ba22c6c16f8bdb5944794535

Commercial Web services often require access keys to limit access to users with paid subscriptions, and/or to limit the number of requests per day for free subscriptions.

To add this requirement to your API, use the "user" and "user_activity" tables in the sample database.  First, using a MySQL client (such as the MySQL Workbench or Query Browser), manually add at least one record to the "user" table to create a fictitious user account; you will need to specify an e-mail address and a 32-character key in the form of an MD5 hash.  MySQL can compute MD5 hashes for you with the MD5() function, like so:

INSERT INTO `user` (email, `key`) VALUES ('jsnellen@jsu.edu', MD5('CS425!p1user'));

(Note that the table and field names "user" and "key" must be enclosed in backticks in your queries because both are MySQL reserved keywords.)

When the user submits a request which includes a key, look up the user's numeric ID from the "user" table, then add a corresponding record in the "user_activity" table to count the number of accesses by this user on the current date.  If a record already exists for this user on this date, update the record to increment the count by one.  (Note: this record should keep track of the dates of the API requests, not the rate date(s) specified in the requests.)  For this assignment, if the user exceeds ten requests per day, your API should return a JSON error object like the following:

{
   "message": "You have exceeded the maximum number of requests per day.",
   "success": false
}

Once you have tested your changes to ensure that requests sent with keys are being processed correctly, make one last change to deny any requests that are sent without keys.  In both cases, remember to set the HTTP status codes appropriately!  HTTP Error 403 (Forbidden) is a good choice for key-related errors.  (You'll find examples of how to set the HTTP status code of a response in the "Introduction to REST" lecture notes, starting on Page 13, along with a list of the most commonly-used codes.)

If a request is sent without a key, or if the key that was sent is invalid (that is, not found in the database), a JSON error object like the following should be returned:

{
   "message": "This API requires a valid access key.",
   "success": false
}


Part 3: Client-Side Components

When you are ready to turn your attention to developing the client-side components, you may find it convenient to adapt your completed work from Lab #4 (and also the code provided in the Example Web Applications as needed), so refer to these earlier assignments for elements that you can reuse.  From Lab #4, you should be able to reuse most of your HTML and client-side JavaScript code, with only a few modifications.  (For instance, add an extra field to the form to allow the user to provide the access key that you added in the previous steps.)  You might find it helpful to use the Example Web Applications for reference when setting up the data source for your database pool.  (Just remember that any assets you reuse from these earlier assignments will almost certainly need to be revised for your new application!)


Due Date and Submission Information

This assignment is worth 100 points toward the "Project" portion of your grade, and is due by the end of the day on Sunday, November 6th.  I recommend creating a Git repository to track your changes to the application.  When you are finished, perform a "Clean" on your application to remove any compiled classes and other build files, then push your changes to a GitHub repository, and submit the repository URL to Canvas.
