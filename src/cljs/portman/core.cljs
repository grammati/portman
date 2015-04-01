(ns portman.core
  (:require [cljsjs.react :as react]
            [clojure.string :as string]
            [rally.table :as table]
            [rally.events :as events]
            [cljs.core.async :as async :refer [<! >!]]
            [reagent.core :as reagent])
  (:require-macros [portman.utils :refer [for! cur-for]]
                   [cljs.core.async.macros :refer [go go-loop]]))


(enable-console-print!)

;;; The application state
(def state (reagent/atom nil))


;;; Components
(defn tab-bar [tabs]
  [:ul.nav.nav-tabs
   (for [{:keys [url title active?]} tabs]
     [:li [:a {:href  url
               :class (when active? "active")}
           title]])])

(defn table [config data]
  (let [selected (reagent/atom (first @data))]
    (events/subscribe :table/select
                      (fn [item]
                        (.log js/console "selected:" (:name item))
                        (reset! selected item)))
    (fn [config data]
      (let [config @config
            keyfn  (or (:keyfn config) :key)
            cols   (:cols config)]
        [:table.table.table-condensed
         {:tab-index 1
          :on-key-down (fn [evt]
                         (.log js/console (.-keyCode evt)))}
         [:thead
          [:tr
           (for [col cols]
             ^{:key (:header col)}
             [:th (:header col)])]]
         [:tbody
          (for! [item @data]
                ^{:key (keyfn item)}
                [:tr (merge
                      {:tab-index 1}
                      (if (identical? @selected item)
                        {:class "selected"}
                        {:on-click #(events/publish :table/select item)}))
                 (for! [col cols]
                       ^{:key (:header col)}
                       [:td ((:render col) item)])])]]))))


(def table-config
  (reagent/atom
   {:cols [{:header "Name"
            :render (fn [item] (:name item))}
           {:header "Value"
            :render :value}]}))

(def table-data
  (reagent/atom
   (vec (for [i (shuffle (range 1 8)) :let [v (str "Top-level-" i)]]
          {:name     v
           :value    i
           :size     (+ 2 i)
           :key      v
           :children (for [j (range 1 (- 5 i)) :let [v (str i "-" j)]]
                       {:name  v
                        :value v
                        :size  (+ 2 j)
                        :key   v})}))))


(defn deg->rad [deg]
  (/ (* Math/PI deg) 180))

(defn rad->deg [rad]
  (* 180 (/ rad Math/PI)))

(defn pt-on-circle
  "Return the point on the perimeter of the circle centered at [cx
  cy], with radius r, that is `angle` degrees clockwise from
  center-top."
  [[cx cy] r angle]
  (let [rads (deg->rad angle)]
    [(+ cx (* r (Math/sin rads)))
     (- cy (* r (Math/cos rads)))]))

(defn arc [[cx cy :as c] ri ro angle start-angle]
  (let [a0 start-angle
        a1 (+ start-angle angle)
        [x0 y0] (pt-on-circle c ro a0)
        [x1 y1] (pt-on-circle c ro a1)
        [x2 y2] (pt-on-circle c ri a1)
        [x3 y3] (pt-on-circle c ri a0)]
    [:path {:d     (string/join
                    " "
                    ["M" x0 y0
                     "A" ro ro 0 (if (> angle 180) 1 0) 1 x1 y1
                     "L" x2 y2
                     "A" ri ri 0 (if (> angle 180) 1 0) 0 x3 y3
                     "Z"] )
            :style {:stroke "#fff" :fill "#ccc" :stroke-width 1}}]))

(defn pie [config data]
  (let []
    (fn [config data]
      (let [total (reduce + (map :size @data))]
        [:svg {:width 600 :height 600}
         [:g
          (let [start-angle (atom 0)]
            (cur-for
             [item data
              :let [angle (-> @item :size (/ total) (* 360))]]
             (let [elt ^{:key (:key @item)} [arc [300 300] 50 250 angle @start-angle]]
               (swap! start-angle + angle)
               elt)))]]))))


(defonce pie-config
  {})

(defn app []
  [:div
   [:h1 "Portman"]
   ;[table table-config table-data]
   [pie pie-config table-data]
   ])

(defn mount-root []
  (reagent/render [app] (.getElementById js/document "portman")))

(defn init! []
  (enable-console-print!)
  (events/init!)
  (mount-root))
