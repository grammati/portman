(ns rally.svg
  (:require [clojure.string :as string]
            [reagent.core :as reagent])
  (:require-macros [rally.reagent-utils :refer [for! cur-for]]))


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


(defn pie [config-cur data]
  [:g
   (let [config      @config-cur
         [X Y]       (:center config)
         inner-r     (:inner-r config 0)
         ring-widths (let [w (:ring-widths config 100)]
                       (if (number? w) (repeat w) (concat [nil] w (repeat (last w)))))
         depth       (:depth config 1)
         outer-r     (+ inner-r (nth ring-widths depth))
         full-angle  (:full-angle config 360)
         start-angle (atom (:start-angle config 0))
         size-fn     (or (:size-fn config) :size)
         key-fn      (or (:key-fn config) :key)
         children-fn (or (:children-fn config) :children)
         total       (reduce + (map size-fn @data))
         palette     (-> js/d3 .-scale .category20b)]
     (cur-for [item data]
       (let [key   (key-fn @item)
             size  (max (size-fn @item) 1)
             angle (-> size (/ total) (* full-angle))
             path  (arc-path [X Y] inner-r outer-r angle @start-angle)
             children (when-let [ch (children-fn @item)]
                        [pie (atom
                              (merge config
                                     {:full-angle  angle
                                      :inner-r     outer-r
                                      :depth       (inc depth)
                                      :start-angle @start-angle}))
                         (reagent/atom ch)])]
         (swap! start-angle + angle)
         (palette (str "junk" key))    ; skip some colors
         ^{:key key}
         [:g [:path {:d     path
                     :style {:stroke       "#fff"
                             :fill         (palette key)
                             :stroke-width 1
                             :cursor       "hand"}}]
          children])))])

