/*
 * Copyright (C) 2012 Joe Osowski (joe.osowski@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package log4couch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * An appender for writing log4j logging events to a couch database
 *
 * http://couchdb.apache.org
 *
 * To use, simply place the log4couch-*.jar in your classpath next to the log4j jar.
 *
 * Append these values to your log4j.xml:
 *
 *
 * <appender name="couchdb" class="log4couch.CouchAppender">
 *   <param name="server" value="http://COUCHDB_HOST:5984/" />
 *   <param name="database" value="COUCHDB_LOG_DATABASE" />
 *   <param name="applicationName" value="APPLICATION_NAME" />
 * </appender>
 *
 * Or these values to your .properties file:
 *
 * log4j.appender.couch=log4couch.CouchAppender
 * log4j.appender.couch.server=http://COUCHDB_HOST:5984/
 * log4j.appender.couch.database=COUCHDB_LOG_DATABASE
 * log4j.appender.couch.applicationName=APPLICATION_NAME
 *
 */
public class CouchAppender extends AppenderSkeleton {
  private Gson gson = null;
  private String server;
  private String database;
  private String applicationName;

  public CouchAppender()
  {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(LoggingEvent.class, new LoggingEventAdapter());
    gson = gsonBuilder.create();
  }

  @Override
  protected void append(LoggingEvent loggingEvent) {
    String json = gson.toJson(loggingEvent);

    URL serverURLPath = null;
    HttpURLConnection connection = null;

    try {
      //In couchDB, we'll use the timestamp as the key, maybe use UUID if we are really concerned.
      serverURLPath = new URL(server + "/" + database + "/" + String.valueOf(loggingEvent.getTimeStamp()));
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }

    try {
      connection = (HttpURLConnection)serverURLPath.openConnection();
      connection.setRequestMethod("PUT");
      connection.setDoOutput(true);
      connection.addRequestProperty("Content-Type","application/json");
      connection.addRequestProperty("Content-Length", String.valueOf(json.getBytes().length));

      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(json);
      writer.flush();

      //Execute the method
      connection.getResponseCode();

    } catch (IOException ex) {
      //Die silently
      System.out.print(ex.getMessage());
      //throw new RuntimeException(, ex);
    } finally {
      // Release the connection.
      if(connection != null) {
        connection.disconnect();
      }
    }

  }

  private class LoggingEventAdapter extends TypeAdapter<LoggingEvent> {
    @Override
    public void write(JsonWriter writer, LoggingEvent event) throws IOException {
      writer.beginObject();

      writer.name("hostName").value(InetAddress.getLocalHost().getHostName());

      if(applicationName != null) {
        writer.name("applicationName").value(applicationName);
      }

      writer.name("message").value((String)event.getMessage());
      writer.name("timestamp").value(event.getTimeStamp());
      writer.name("level").value(event.getLevel().toString());
      writer.name("level_int").value(event.getLevel().toInt());
      writer.name("loggerName").value(event.getLoggerName());
      writer.name("threadName").value(event.getThreadName());
      writer.name("categoryName").value(event.getLoggerName());

      LocationInfo li = event.getLocationInformation();

      writer.name("locationInformation").beginObject();
      writer.name("className").value(li.getClassName());
      writer.name("fileName").value(li.getFileName());
      writer.name("lineNumber").value(li.getLineNumber());
      writer.name("methodName").value(li.getMethodName());

      writer.endObject();

      if(event.getThrowableInformation() != null) {
        ThrowableInformation throwable = event.getThrowableInformation();

        writer.name("exception").value(throwable.getThrowable().getMessage());
        writer.name("stack").beginArray();

        if (throwable.getThrowableStrRep() != null) {
          for (String line : throwable.getThrowableStrRep()) {
            writer.value(line);
          }
        }

        writer.endArray();
      }

      writer.endObject();
    }

    @Override
    public LoggingEvent read(JsonReader jsonReader) throws IOException {
      throw new RuntimeException("Not implemented");
    }
  }

  @Override
  public void close() {
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }


  public void setServer(String server) {
    this.server = server;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }
}
