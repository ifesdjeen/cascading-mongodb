(ns com.clojurewerkz.cascading.mongodb.core-test
  (:use cascalog.api
        clojure.test
        [midje sweet cascalog])
  (:require [cascalog.io :as io]
            [cascalog.ops :as c]
            [monger.core :as monger]
            [monger.collection :as mc])
  (:import [com.clojurewerkz.cascading.mongodb MongoDBScheme MongoDBTap]))

(monger/connect!)
(monger/set-db! (monger/get-db "cascading_mongodb"))

(defn create-tap
  []
  (let [scheme (MongoDBScheme. "localhost"
                               (java.lang.Integer. 27017)
                               "cascading_mongodb"
                               "libraries"
                               ["name" "language" "votes"]
                               {"_id" "?value1"
                                "name" "?value1"
                                "language" "?value2"
                                "votes" "?value3"})
        tap    (MongoDBTap. scheme)]
    tap))

(deftest t-mongodb-tap-as-source
  (mc/remove "libraries")
  (dotimes [counter 100]
    (mc/insert "libraries" {:name (str "Cassaforte" counter) :language (str "Clojure" counter) :votes counter}))

  (fact "Handles simple calculations"
        (<-
         [?count ?sum]
         ((create-tap) ?value1 ?value2 ?value3)
         (c/count ?count)
         (c/sum ?value3 :> ?sum))
        => (produces [[100 4950]])))

(deftest t-mongodb-tap-as-source-2
  (mc/remove "libraries")
  (mc/insert "libraries" {:name "Riak" :language "Erlang" :votes 5})
  (mc/insert "libraries" {:name "Cassaforte" :language "Clojure" :votes 3})

  (fact "Retrieves data"
        (<-
         [?value1 ?value3]
         ((create-tap) ?value1 ?value2 ?value3))
        => (produces [["Cassaforte" 3] ["Riak" 5]])))

(deftest t-mongo-tap-as-sink
  (mc/remove "libraries")
  (let [test-data [["Riak" "Erlang" 5]
                   ["Cassaforte" "Clojure" 3]]]

    (?<- (create-tap)
         [?value1 ?value2 ?value3]
         (test-data ?value1 ?value2 ?value3))

    (let [res (mc/find-maps "libraries")]
      (is (= {:name "Riak" :language "Erlang" :votes 5}
             (dissoc (first res) :_id)))
      (is (= {:name "Cassaforte" :language "Clojure" :votes 3}
             (dissoc (second res) :_id))))))