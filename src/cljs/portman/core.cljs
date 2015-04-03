(ns portman.core
  (:require [cljs.core.async :as async :refer [<! >!]]
            [cljsjs.react :as react]
            [clojure.string :as string]
            [portman.pi-table :as pi-table]
            [rally.animate :as animate]
            [rally.events :as events]
            [rally.layout :as layout]
            [rally.svg :as svg]
            [rally.table :as table]
            [reagent.core :as reagent])
  (:require-macros [rally.animate :as animate-clj]
                   [rally.reagent-utils :refer [for! cur-for]]
                   [cljs.core.async.macros :refer [go go-loop]]))


(enable-console-print!)

;;; The application state
(def state (reagent/atom nil))



(def table-config
  (reagent/atom
   {:cols [{:header "Name"
            :render (fn [item] (:name item))}
           {:header "Value"
            :render :value}]}))

(def pie-config
  (reagent/atom
   {:center      [0 350]
    :inner-r     30
    :ring-widths [120 80 50 30 20]
    :full-angle  180
    :start-angle 0
    :size-fn     #(get % "LeafStoryCount")
    :key-fn      #(get % "FormattedID")
    :children-fn #(get % "Children")
    }))

(def child-pie-config
  (reagent/atom
   {:center      [350 350]
    :inner-r     30
    :ring-widths [120 80 50 30 20]
    :full-angle  360
    }))

(defn gen-test-data [num depth prob]
  (when (pos? depth)
    (vec
     (for [i (range num)]
       {:key  i
        :size (Math/random)
        :children (when (< (Math/random) prob)
                    (gen-test-data num (dec depth) prob))}))))


(def test-data
  (reagent/atom
   (gen-test-data 5 4 0.8)))



(defn app []
  [layout/container-fluid
   [layout/row
    [layout/col 4
     #_[:svg {:width 350 :height 700} [svg/pie pie-config (reagent/cursor state [:data])]]]
    [layout/col 8
     [:h4 "Portfolio Items"]
     [pi-table/app state]]]
   ])

(defn mount-root []
  (reagent/render [app] (.getElementById js/document "portman")))

(defn init! []
  (enable-console-print!)
  ;(animate/init!)
  (events/init!)
  (mount-root))
