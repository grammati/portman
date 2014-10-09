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
         #js {:data     #js {:types              "portfolioitem/strategy"
                             :pagesize           200
                             :start              1
                             ;;:shallowFetch       "ObjectID,Name"
                             :fetch              true
                             :includePermissions true
                             }
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
           (for [t ["ID" "Name" "LeafStoryCount"]]
             [:th t])]]
         [:tbody
          (for [row data]
            [:tr
             [:td (row "FormattedID")]
             [:td (row "Name")]
             [:td (row "LeafStoryCount")]])]]))

(defn thingy [data]
  (let [d (->> data
               (map #(get % "LeafStoryCount"))
               (filter #(> % 0))
               sort
               reverse)]
   (pie d)))

(defn- zip [a b]
  (map vector a b))

(defn pie [data]
  (let [total      (float (reduce + data))
        ptots      (reductions + (cons 0 data))
        radius     150
        center     "200,200"
        top        "200,50"
        rand-color #(str "#" (rand-int 9) (rand-int 9) (rand-int 9))]
    [:svg {:width 400 :height 400}
     [:g {:stoke-width 2}
      (for [[val ptot] (zip data ptots)]
        (let [angle (* 2.0 Math/PI (/ val total))
              rot   (* 360.0 (/ ptot total))
              end-x (+ 200 (* (Math/sin angle) radius))
              end-y (- 200 (* (Math/cos angle) radius))
              _ (println total angle end-x end-y (/ val total))]
          [:path {:d (str "M " center " L " top " A 150 150 0 " (if (> rot 180) 0 1) " 1 " end-x " " end-y " z")
                  :transform (str "rotate(" rot "," center ") "
                                  "rotate(" (* 180 (/ val total)) "," center ")"
                                  "translate(0,0) "
                                  "rotate(" (* -180 (/ val total)) "," center ")"
                                  )
                  :style {:stroke "black" :stroke-width 0 :fill (rand-color)}}]))]]))

(defn app [data owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (load-data))
    om/IRender
    (render [_]
      (println "Rendering root")
      (html [:div
             (thingy (:data data))
             (pi-table (:data data))
             (thingy (:data data))]))))

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
