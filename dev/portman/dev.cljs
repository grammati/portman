(ns portman.dev
  (:require [figwheel.client :as fw]
            [portman.core]))

;;(enable-console-print!)

(fw/watch-and-reload
 :websocket-url (str "wss://" js/location.host "/portman/figwheel-ws")
 :url-rewriter  (fn [u]
                  (when u
                    (.replace u js/location.host (str js/location.host "/portman"))))
 :jsload-callback (fn []
                    (println "reloaded JS")
                    (portman.core/render-app)
                    ))

