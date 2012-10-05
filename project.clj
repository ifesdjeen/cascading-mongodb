(defproject com.clojurewerkz/cascading-mongodb "0.0.1-SNAPSHOT"
  :description "Cascading MongoDB Tap"
  :url "http://github.com/ifesdjeen/cascading-mongodb"
  :min-lein-version "2.0.0"
  :dependencies [[org.apache.hadoop/hadoop-core "0.20.2-cdh3u3"]
                 [org.mongodb/mongo-hadoop-streaming "1.0.0-SNAPSHOT"]
                 [org.mongodb/mongo-hadoop-core_cdh3u3 "1.0.0-rc0"]
                 [cascading/cascading-core "2.0.2"]
                 [cascading/cascading-hadoop "2.0.2"]
                 [log4j/log4j "1.2.17"]]
  :java-source-paths ["src/main/java"]
  :test-paths        ["src/main/test"]
  :profiles {:dev {:resource-paths     ["src/resources"]
                   :dependencies [[org.clojure/clojure "1.4.0"]
		   		  [com.novemberain/monger "1.2.0"]
                                  [cascalog "1.10.0"]
                                  [midje "1.3.0" :exclude [org.clojure/clojure]]
                                  [midje-cascalog "0.4.0" :exclude [org.clojure/clojure]]]}}
  :test-selectors {:all     (constantly true)
                   :focus   :focus
                   :default (constantly true)}
  :javac-options     ["-target" "1.7" "-source" "1.7"]
  :repositories {"conjars" "http://conjars.org/repo/"
                 "local" ~(str (.toURI (java.io.File. "repo")))
                 "cloudera" {:url "https://repository.cloudera.com/artifactory/cloudera-repos"}})
