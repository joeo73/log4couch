An appender for writing log4j logging events to a couch database
  http://couchdb.apache.org

To use, simply place the log4couch-*.jar in your classpath next to the log4j jar.

Append these values to your log4j.xml:

    <appender name="couchdb" class="log4couch.CouchAppender">
      <param name="server" value="http://COUCHDB_HOST:5984/" />
      <param name="database" value="COUCHDB_LOG_DATABASE" />
      <param name="applicationName" value="APPLICATION_NAME" />
    </appender>
 
Or these values to your .properties file:
 
    log4j.appender.couch=log4couch.CouchAppender
    log4j.appender.couch.server=http://COUCHDB_HOST:5984/
    log4j.appender.couch.database=COUCHDB_LOG_DATABASE
    log4j.appender.couch.applicationName=APPLICATION_NAME
 