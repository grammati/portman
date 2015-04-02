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




(defn deg->rad [deg]
  (/ (* Math/PI deg) 180))

(defn rad->deg [rad]
  (* 180 (/ rad Math/PI)))

(defn pt-on-circle
  "Return the point on the perimeter of the circle centered
  at [cx,cy], with radius r, that is `angle` degrees clockwise from
  center-top."
  [[cx cy] r angle]
  (let [rads (deg->rad angle)]
    [(+ cx (* r (Math/sin rads)))
     (- cy (* r (Math/cos rads)))]))

(defn arc-path
  "Calculate and return the path of a circular arc."
  [[cx cy :as c] ri ro angle start-angle]
  (let [a0 start-angle
        a1 (+ start-angle angle)
        [x0 y0] (pt-on-circle c ro a0)
        [x1 y1] (pt-on-circle c ro a1)
        [x2 y2] (pt-on-circle c ri a1)
        [x3 y3] (pt-on-circle c ri a0)]
    (string/join
     " "
     ["M" x0 y0
      "A" ro ro 0 (if (> angle 180) 1 0) 1 x1 y1
      "L" x2 y2
      "A" ri ri 0 (if (> angle 180) 1 0) 0 x3 y3
      "Z"] )))

(defn pie [config data]
  [:g
   (let [[X Y]       (:center config)
         inner-r     (:inner-r config 0)
         ring-widths (let [w (:ring-widths config 100)]
                       (if (number? w) (repeat w) (concat [nil] w (repeat (last w)))))
         depth       (:depth config 1)
         outer-r     (+ inner-r (nth ring-widths depth))
         full-angle  (:full-angle config 360)
         start-angle (atom (:start-angle config 0))
         total       (reduce + (map #(:size % 1) @data))
         palette     (-> js/d3 .-scale .category20b)]
     (doall
      (for [{:keys [size key] :as item} @data]
        (let [angle (-> size (/ total) (* full-angle))
              path  (arc-path [X Y] inner-r outer-r angle @start-angle)
              children (when-let [ch (:children item)]
                         [pie (merge config
                                     {:full-angle  angle
                                      :inner-r     outer-r
                                      :depth       (inc depth)
                                      :start-angle @start-angle})
                          (atom ch)])]
          (swap! start-angle + angle)
          (palette (str "junk" key))    ; skip some colors
          ^{:key key}
          [:g [:path {:d     path
                      :style {:stroke       "#fff"
                              :fill         (palette key)
                              :stroke-width 1}}]
           children]))))])


(def pie-config
  {:center      [600 10]
   :inner-r     50
   :ring-widths [150 125 100 80 50 30 20]
   :full-angle  180
   :start-angle 90})

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
   (gen-test-data 3 5 0.95)))


(def CTG (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))

(defn animated []
  [CTG {:transition-name "foo"}
   [:h1 "Animated"]])

(defn app []
  [:div
   [animated]
   ;[table table-config test-data]
   [:svg {:width 1200 :height 800}
    [pie pie-config test-data]] 
   ])

(defn mount-root []
  (reagent/render [app] (.getElementById js/document "portman")))

(defn init! []
  (enable-console-print!)
  (events/init!)
  (mount-root))
