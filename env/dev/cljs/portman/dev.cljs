(ns ^:figwheel-no-load portman.dev
  (:require [portman.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [weasel.repl :as weasel]
            [reagent.core :as r]))

(enable-console-print!)

(figwheel/watch-and-reload
 :websocket-url   (str "wss://" js/location.host "/figwheel-ws")
 :jsload-callback core/mount-root)

(weasel/connect (str "wss://" js/location.host "/weasel/") :verbose true)

(core/init!)
