(ns rally.animate
  (:require [cljs.core.async.macros :refer [go go-loop]]))


(defmacro animate [frames & body]
  `(let [ch1# (cljs.core.async/chan ~frames)
         _#   (cljs.core.async/tap ~'rally.animate/raf-mult ch1#)
         ch#  (cljs.core.async/take ~frames ch1#)]
     (go-loop [i# 0]
       (try
         (when-let [t# (cljs.core.async/<! ch#)]
           (let [~'&t t# ~'&n i#]
             ~@body
             (recur (inc i#))))
         (finally
           (cljs.core.async/untap ~'rally.animate/raf-mult ch1#))))))

