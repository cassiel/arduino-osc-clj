(ns eu.cassiel.arduino-osc.main
  (:require (eu.cassiel.arduino-osc [core :as c]
                                    [manifest :as m]
                                    [osc :as o]))
  (:use [clojure.tools.cli :only [cli]])
  (:gen-class))

(defn -main
  [& args]
  (let [[{:keys [serial-port destination]} rest usage]
        (cli args
             ["-s" "--serial-port" "The Arduino serial port"]

             ["-d"
              "--destination"
              "One or more OSC destinations, host:port"
              :assoc-fn
              (fn [previous key val]
                (assoc previous key
                       (if-let [oldval (get previous key)]
                         (merge oldval val)
                         (hash-set val))))])]

    (o/go :serial serial-port
          :destinations destination
          :msec 100)))
