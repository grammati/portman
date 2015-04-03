(ns portman.temp
  (:require [reagent.core :as reagent]))

;;; Experiments

(def config
  (reagent/atom
   {:cells [(fn [item] [:td (:a item)])
            (fn [item] [:td (:b item)])]
    :headers [(fn [] [:th "AVSD"])
              (fn [] [:th "BBB"])]}))

(def data
  (reagent/atom
   [{:a "a-1" :b "b-1"} {:a "a-2" :b "b-2"}]))

(defn table [config data]
  [:table
   [:thead
    (apply vector :tr
           (for [header (:headers @config)]
             (header)))]
   [:tbody
    (for [item @data]
      [:tr
       (for [cell (:cells @config)]
         [cell item])])]])

(defn app []
  [table config data])
