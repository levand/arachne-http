(ns arachne.http.config-test
  (:require [clojure.test :refer :all]
            [arachne.http.config :as http-cfg]
            [arachne.core :as core]
            [arachne.core.config :as cfg]))

(def cfg-init '(do
                 (require '[arachne.core.dsl :as core])
                 (require '[arachne.http.dsl :as http])

                 (core/runtime :test/rt [:test/server])

                 (core/component :test/handler-1 {} 'test/ctor)
                 (core/component :test/handler-2 {} 'test/ctor)
                 (core/component :test/handler-3 {} 'test/ctor)

                 (http/server :test/server 8080

                   (http/endpoint :get "/" :test/handler-1)
                   (http/endpoint :get "a/b/c/d" :test/handler-2)
                   (http/endpoint :get "a/b/x/y" :test/handler-3))))

(deftest find-server-dependencies
  (let [cfg (core/build-config "test" '[:org.arachne-framework/arachne-http]
              cfg-init)
        servers (@#'http-cfg/servers cfg)
        deps (@#'http-cfg/find-server-dependencies cfg (first servers))]
    (is (= 1 (count servers)))
    (is (= 3 (count deps)))))

(deftest server-dependencies-are-added
  (let [cfg (core/build-config "test" '[:org.arachne-framework/arachne-http]
              cfg-init)]
    (is (= 3 (cfg/q cfg '[:find (count ?dep) .
                          :where
                          [?server :arachne.http.server/port 8080]
                          [?server :arachne.component/dependencies ?dep]])))))