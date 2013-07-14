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

(defn go [& {:keys [serial host port msec]}]
  (let [board (c/arduino :firmata serial)
        tx (UDPTransmitter. (InetAddress/getByName host) port)]
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
                               (.transmit tx (make-message state')))
                             (Thread/sleep msec)
                             (recur state')))))))

    (reify CLOSEABLE
      (close [this]
        (.close tx)
        (c/close board)))))
