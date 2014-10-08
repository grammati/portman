(ns portman.core
  (:require
    [figwheel.client :as fw]
    [clojure.string :as string]
    [om.core :as om]
    [sablono.core :refer-macros [html]]
    [cljs.core.async :as async :refer [<! >!]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defn by-id [id]
  (.getElementById js/document id))

;; define your app data so that it doesn't get over-written on reload
(defonce app-data (atom {:message "Hello there"
                         :data []}))

(defn load-data []
  (.ajax js/jQuery
         "/slm/webservice/v2.0/artifact"
         #js {:data     #js {:types "portfolioitem/feature"
                             :pagesize 100
                             :start 1
                             :shallowFetch "ObjectID,Name"
                             :includePermissions true}
              :dataType "text"
              :success  (fn [x]
                          (let [result (js->clj (.parse js/JSON x))]
                            (swap! app-data assoc-in [:data] (get-in result ["QueryResult" "Results"]))))
              :error    (fn [x]
                          (println "ERROR" x))}))

(defn pi-table [data]
  (html [:table {:class "table table-condensed"}
         [:thead
          [:tr
           (for [t ["ID" "Name"]]
             [:th t])]]
         [:tbody
          (for [row data]
            [:tr
             [:td (row "ObjectID")]
             [:td (row "Name")]])]]))

(defn app [data owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (load-data))
    om/IRender
    (render [_]
      (println "Rendering root")
      (html [:div

             (pi-table (:data data))
             
             [:svg {:width 800 :height 350}
              [:rect {:width 400 :height 300 :x 50 :y 25 :ry 10 :fill "#822" :style {:border "4px solid black"}}]
              [:circle {:cx 250 :cy 175 :r 125 :fill "#228"}]
              [:text {:x 250 :y 200 :text-anchor "middle" :font-size 60 :font-family "helvetica" :fill "white"}
               "React!"]]

             [:span (str (js/Date.))]]))))

(defn render-app []
  (om/root app
           app-data
           {:target (by-id "portman-main")}))



(println "Edits to this text should show up in your developer console.")

(fw/watch-and-reload
 :websocket-url (str "wss://" js/location.host "/portman/figwheel-ws")
 :url-rewriter  (fn [u]
                  (when u
                    (.replace u js/location.host (str js/location.host "/portman"))))
 :jsload-callback (fn []
                    (println "reloaded JS")
                    ))


(.setTimeout js/window (fn [] (render-app)) 10)
