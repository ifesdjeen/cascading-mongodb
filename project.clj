(defproject com.clojurewerkz/cascading-mongodb "0.0.4"
  :description "Cascading MongoDB Tap"
  :url "http://github.com/ifesdjeen/cascading-mongodb"
  :min-lein-version "2.0.0"
  :dependencies [[org.mongodb/mongo-hadoop-streaming "1.1.0-SNAPSHOT"]
                 ;; [mongo-hadoop-streaming_cdh3u3  "1.0.0-rc0"]
                 [org.mongodb/mongo-hadoop-core_cdh3u3 "1.0.0"]
                 [log4j/log4j "1.2.17"]]
  :java-source-paths ["src/main/java"]
  :test-paths        ["src/main/test"]
  :profiles {:dev {:resource-paths     ["src/resources"]
                   :dependencies [[org.clojure/clojure "1.4.0"]
                                  [com.novemberain/monger "1.2.0"]
                                  [cascalog "1.10.0"]
                                  [midje "1.3.0" :exclude [org.clojure/clojure]]
                                  [midje-cascalog "0.4.0" :exclude [org.clojure/clojure]]]}
             :provided {:dependencies [[org.apache.hadoop/hadoop-core "0.20.2-cdh3u3"]
                                       [cascading/cascading-core "2.0.2"]
                                       [cascading/cascading-hadoop "2.0.2"]]}}
  :test-selectors {:all     (constantly true)
                   :focus   :focus
                   :default (constantly true)}
  :javac-options   ["-target" "1.6" "-source" "1.6"]
  :repositories {"conjars" "http://conjars.org/repo/"
                 "cloudera" {:url "https://repository.cloudera.com/artifactory/cloudera-repos"}})
