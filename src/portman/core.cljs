(ns portman.core
  (:require
    [figwheel.client :as fw]
    [clojure.string :as string]
    [om.core :as om]
    [sablono.core :refer-macros [html]]
    [cljs.core.async :as async :refer [<! >! go]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defn by-id [id]
  (.getElementById js/document id))

;; define your app data so that it doesn't get over-written on reload
(defonce app-data (atom {:message "Hello there"}))

(defn app [data owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (go (loop [i 0]
            (when (< i 10)
              (println "loop " i)
              (om/transact! data :message (fn [m] (str m " " i)))
              (<! (async/timeout 1000))
              (recur (inc i))))))
    om/IRender
    (render [_]
      (println "Rendering root")
      (html [:div (:message data)]))))

(om/root app
         app-data
         {:target (by-id "portman-main")})



(println "Edits to this text should show up in your developer console.")

(fw/watch-and-reload
 :websocket-url (str "ws://" js/location.host "/portman/figwheel-ws")
 :url-rewriter  (fn [u]
                  (when u
                    (.replace u js/location.host (str js/location.host "/portman"))))
 :jsload-callback (fn []
                    (println "reloaded JS")
                    ))
