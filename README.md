# Cascading Tap for Mongodb

This is a Mongodb Tap that can be used as a sink and source. Works
with the latest version of Mongodb and Cascading (2.0), is tested,
well-maintained. It's working fine for us, but use it at your own
risk.

Project was in part inspired by MongoDB Tap from @brugidou.

# Usage

To use it as both source and sink, simply create a Schema:


```java
import com.clojurewerkz.cascading.mongodb.MongoDBScheme;
import com.clojurewerkz.cascading.mongodb.MongoDBTap;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap

// List of columns to be fetched from Mongo
List<String> columns = new ArrayList<String>();
columns.add("first-column-name");
columns.add("second-column-name");
columns.add("third-column-name");

// When writing back to mongodb, you may have Cascading output tuple item names
// a bit different from your Mongodb ColumnFamily definition. Otherwise, you can
// simply specify both key and value same.
Map<String, String> mappings = new HashMap<String, String>();
mappings.put("first-column-name", "cascading-output-tuple-first-item-name");
mappings.put("second-column-name", "cascading-output-tuple-second-item-name");
mappings.put("third-column-name", "cascading-output-tuple-third-item-name");

MongoDBScheme scheme = new MongoDBScheme("localhost"
                                         27017
                                        "keyspace-name"
                                        "column-family-name"
                                        "key-column-name"
                                        columns
                                        mappings);

MongoDBTap tap = new MongoDBTap(scheme);
```

That's pretty much it. To do same thing in Clojure (with Cascalog),
you can use following code:

```clojure
(defn create-tap
  []
  (let [scheme (MongoDBScheme. "localhost"                 ;; host
                               (java.lang.Integer. 27017)  ;; port
                               "cascading_mongodb"         ;; database
                               "libraries"                 ;; collection
                               ["name" "language" "votes"] ;; fields
                               {"_id" "?value1"            ;; field mappings
                                "name" "?value1"
                                "language" "?value2"
                                "votes" "?value3"})
        tap    (MongoDBTap. scheme)]
    tap))
```

# Development

## Deploying required/missing artifacts:

Get the latest `mongo-hadoop` from `git://github.com/mongodb/mongo-hadoop.git`,

Within mongo-hadoop, run:

```shell
./sbt mongo-hadoop-streaming/assembly
```

And put resulting assembly to your local maven repo:

```shell
mvn deploy:deploy-file -Dfile=./mongo-hadoop-streaming-assembly-1.1.0-SNAPSHOT.jar \
                       -DartifactId=mongo-hadoop-streaming
                       -Dversion=1.0.0-SNAPSHOT
                       -DgroupId=org.mongodb
                       -Dpackaging=jar
                       -DgeneratePom=true
                       -Durl=file:repo
```

## Run tests:

```
lein do javac, test
```

# Dependency

Jar is hosted on Clojars: https://clojars.org/com.clojurewerkz/cascading-mongodb

## Leiningen

```clojure
[com.clojurewerkz/cascading-mongodb "0.0.4"]
```

## Maven

```xml
<dependency>
  <groupId>com.clojurewerkz</groupId>
  <artifactId>cascading-mongodb</artifactId>
  <version>0.0.4</version>
</dependency>
```

# License

Copyright (C) 2011-2012 Alex Petrov

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# Credits

Alex Petrov: alexp at coffeenco dot de
