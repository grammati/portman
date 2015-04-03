(ns rally.vis
  (:require [reagent.core :as reagent]
            [rally.data :as data]
            [cljs.core.async :as async :refer [<! >!]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))


(defn d3-sunburst [data]
  (let [did-mount
        (fn [this]
          (.log js/console "sunburst did-mounut")
          (let [svg    (-> js/d3
                           (.select (reagent/dom-node this))
                           (.append "svg")
                           (.attr "width" 600)
                           (.attr "height" 600)
                           (.append "g")
                           (.attr "transform" "translate(300,300)"))
                layout (-> js/d3
                           .-layout
                           .partition
                           (.size #js [(* 2 Math/PI), (* 300 300)])
                           (.value #(Math/max (.-LeafStoryCount %) 5)))
                arc    (-> js/d3
                           .-svg
                           .arc
                           (.startAngle #(.-x %))
                           (.endAngle #(+ (.-x %) (.-dx %)))
                           (.innerRadius (constantly 50) #_#(Math/sqrt (.-y %)))
                           (.outerRadius #(Math/sqrt (+ (.-y %) (.-dy %)))))
                color  (-> js/d3
                           .-scale
                           .category20c)]
            (-> svg
                (.datum #js {"children" (clj->js data)})
                (.selectAll "path")
                (.data (.-nodes layout))
                .enter
                (.append "path")
                (.attr "display" (fn [d] (if (zero? (.-depth d)) "none" nil)))
                (.attr "d" arc)
                (.style "stroke" "#fff")
                (.style "fill" #(let [name (.-Name %)]
                                  (.log js/console "fill" %)
                                  (color name))))))]
    (reagent/create-class
     {:display-name        "d3-sunburst"
      :reagent-render      (fn [data]
                             (.log js/console "Rendering sunburst div")
                             [:div])
      :component-did-mount did-mount})))

