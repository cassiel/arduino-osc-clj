(ns eu.cassiel.arduino-osc.osc
  (:require (eu.cassiel.arduino-osc [manifest :as m])
            (clodiuno [core :as c]
                      [firmata :as f]))
  (:import [java.net InetAddress]
           [net.loadbang.osc.data Message]
           [net.loadbang.osc.comms UDPTransmitter]))

(defprotocol CLOSEABLE
  (close [this] "stop the thread, close the connection."))

(defn- make-message [state]
  (-> (Message. "/state")
      (.addInteger state)))

(defn go [& {:keys [serial destinations msec]}]
  (let [board (c/arduino :firmata serial)
        make-tx (fn [dest]
                  (let
                      [[_ host port] (re-find #"(\S+):(\d+)" dest)]
                    (UDPTransmitter. (InetAddress/getByName host) (Integer/parseInt port))))
        txs (map make-tx destinations)]
    (doseq [p (vals m/OUT-PINS)]
      (c/pin-mode board p c/OUTPUT))

    (doseq [p (vals m/IN-PINS)]
      (c/pin-mode board p c/INPUT)
      (c/enable-pin board :digital p))

    (.start (Thread. (reify Runnable
                       (run [this]
                         (loop [state 0]
                           (let [state' (c/digital-read board (:button m/IN-PINS))]
                             (when (not= state state')
                               (c/digital-write board (:led m/OUT-PINS) state')
                               (doseq [tx txs]
                                 (.transmit tx (make-message state'))))
                             (Thread/sleep msec)
                             (recur state')))))))

    (reify CLOSEABLE
      (close [this]
        (doseq [tx txs] (.close tx))
        (c/close board)))))
