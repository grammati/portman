(ns rally.animate
  (:require [cljs.core.async :as async]))

;;; Expose requestAnimationFrame as a channel
(def raf-chan (async/chan (async/sliding-buffer 1)))
(def raf-mult (async/mult raf-chan))

(defn push-animation-event []
  (.requestAnimationFrame js/window
                          (fn [t]
                            (async/put! raf-chan t)
                            (push-animation-event))))

(defn init! []
  (push-animation-event))

