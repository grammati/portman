(ns rally.animate
  (:require [cljs.core.async :as async :refer [<! >!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

;;; Expose requestAnimationFrame as a channel
(def raf-chan (async/chan (async/sliding-buffer 1)))
(def raf-mult (async/mult raf-chan))

(defn animation-loop []
  (.requestAnimationFrame js/window
                          (fn [t]
                            (async/put! raf-chan t)
                            (animation-loop))))



