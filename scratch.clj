(ns user
  (:require (eu.cassiel.arduino-osc [core :as c]
                                    [manifest :as m]
                                    [osc :as o])))

(ns user
  (:use :reload-all clodiuno.core)
  (:use :reload-all clodiuno.firmata))

(def board (arduino :firmata "/dev/tty.usbmodemfa141"))

(pin-mode board 13 OUTPUT)

(doseq [_ (range 5)]
  (digital-write board 13 HIGH)
  (Thread/sleep 1000)
  (digital-write board 13 LOW)
  (Thread/sleep 1000))

(pin-mode board 2 INPUT)
(enable-pin board :digital 2)

(digital-read board 2)

(close board)

(c/sos)

(vals m/IN-PINS)

(def doit (o/go :serial "/dev/tty.usbmodemfa141"
                :destinations ["localhost:4002"
                               "localhost:4001"]
                :msec 100))

(o/close doit)

(let [[_ host port] (re-find #"(\S+):(\d+)" "localhost:3000")]
  {:host host :port port})
