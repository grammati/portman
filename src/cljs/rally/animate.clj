(ns rally.animate
  (:require [clojure.core.async :as async :refer [go go-loop <! >!]]))


(defmacro animate [frames & body]
  `(let [ch# (async/take ~frames (async/tap ~'rally.animate/raf-mult))]
     (go-loop [i# 0]
       (when-let [t# (<! ch#)]
         (let [~'&t t# ~'&n i#]
           ~@body)
         (recur (inc i#))))))

