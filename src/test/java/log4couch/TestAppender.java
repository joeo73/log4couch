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

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class TestAppender {
  CouchAppender ca;
  Logger log = Logger.getLogger(TestAppender.class);
  Logger log2 = Logger.getLogger("Another logger name");

  @Test
  public void testAppender()
  {
    log.debug("This is a log event");
    log.warn("This is a log event");
    log.trace("This is a log event");

    Exception ex = new Exception("Test Exception");

    log.error("Something bad happened", ex);

    log2.info("Another log message");
  }

}
