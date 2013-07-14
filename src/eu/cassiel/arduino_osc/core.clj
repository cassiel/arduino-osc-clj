(ns eu.cassiel.arduino-osc.core
  "Test code."
  (:require (clodiuno [core :as c]
                      [firmata :as f])))

(def short-pulse 250)
(def long-pulse 500)
(def letter-delay 1000)

(def letter-s [0 0 0])
(def letter-o [1 1 1])

(defn blink [board time]
  (c/digital-write board 13 c/HIGH)
  (Thread/sleep time)
  (c/digital-write board 13 c/LOW)
  (Thread/sleep time))

(defn blink-letter [board letter]
  (doseq [i letter]
    (if (= i 0)
      (blink board short-pulse)
      (blink board long-pulse)))
  (Thread/sleep letter-delay))

(defn sos []
  (let [board (c/arduino :firmata "/dev/tty.usbmodemfa1451")]
    ;;allow arduino to boot
    (Thread/sleep 5000)
    (c/pin-mode board 13 c/OUTPUT)

    (doseq [_ (range 3)]
      (blink-letter board letter-s)
      (blink-letter board letter-o)
      (blink-letter board letter-s))

    (c/close board)))
