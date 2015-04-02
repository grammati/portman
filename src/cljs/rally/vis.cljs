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
     {:reagent-render          (fn [data]
                                 (.log js/console "Rendering sunburst div")
                                 [:div])
      :component-did-mount     did-mount})))



(defn arc-path
  "Calculate an SVG arc path."
  [[cx cy :as center] radius total start value]
  (let [angle (* 2.0 Math/PI (/ value total))
        rot   (* 360.0 (/ ptot total))
        end-x (+ 200 (* (Math/sin angle) radius))
        end-y (- 200 (* (Math/cos angle) radius))
        path  (str "M " center " L " top " A 150 150 0 " (if (> angle 180) 1 0) " 1 " end-x " " end-y " z")]
    [:path {:d         path
            :transform (str "rotate(" rot "," center ") "
                            "rotate(" (* 180 (/ val total)) "," center ")"
                            "translate(0,-0) "
                            "rotate(" (* -180 (/ val total)) "," center ")"
                            )
            :style     {:stroke "white" :fill (rand-color)}}]))


(defn foo []
  (let [center       [300 300]
        inner-radius 100
        outer-radius 280
        path         (fn [] {:d []})
        goto         (fn [path [x y]]
                       (update-in path [:d] conj {:M [x y]}))
        line         (fn towards
                       ([path [x y]]
                        (update-in path [:d] conj {:L [x y]}))
                       ([path dir target amount]
                        (:???)))
        from         (fn [[x y] dir amount]
                       (case dir
                         :up [x (+ y amount)]))
        arc          (fn [path radius angle]
                       (let [dx (* radius (Math/sin angle))
                             dy (* radius (Math/cos angle))]
                         (update-in path [:d] conj {:A [radius radius 0 (if (> angle 180) 1 0) 1 dx dy]})))
        towards      (fn [[x y :as center] amount])]
   (-> (path)
       (goto (from center :up inner-radius))
       (line (from center :up outer-radius))
       (arc  outer-radius angle)
       (line :towards center (- outer-radius inner-radius))
       (arc  inner-radius (- angle))))

  )

(defn svg-sunburst [data]
  (reagent/create-class
   {:reagent-render
    (fn [data]
      (let [data       (->> data
                            (map #(get % "LeafStoryCount"))
                            (filter #(> % 0))
                            sort
                            reverse)
            total      (float (reduce + data))
            ptots      (reductions + (cons 0 data))
            radius     150
            center     "200,200"
            top        "200,50"
            rand-color #(str "#" (rand-int 9) (rand-int 9) (rand-int 9))]
        [:svg {:width 600 :height 600}
         [:g
          (for [[val ptot] (map vector data ptots)]
            (let [angle (* 2.0 Math/PI (/ val total))
                  rot   (* 360.0 (/ ptot total))
                  end-x (+ 200 (* (Math/sin angle) radius))
                  end-y (- 200 (* (Math/cos angle) radius))]
              [:path {:d (str "M " center " L " top " A 150 150 0 " (if (> angle 180) 1 0) " 1 " end-x " " end-y " z")
                      :transform (str "rotate(" rot "," center ") "
                                      "rotate(" (* 180 (/ val total)) "," center ")"
                                      "translate(0,-0) "
                                      "rotate(" (* -180 (/ val total)) "," center ")"
                                      )
                      :style {:stroke "white" :fill (rand-color)}}]))]]))}))


(defn app [state]
  (reagent/create-class
   {:component-did-mount
    (fn [this]
      (.log js/console "MOUNTED")
      (go
        (let [strategies (<! (data/load-pis))
              strategies (vec (sort-by #(- (get % "LeafStoryCount" 0)) strategies))]
          (.log js/console "Loaded data")
          (.log js/console (clj->js strategies))
          (swap! state assoc-in [:data] strategies))))

    :reagent-render
    (fn [state]
      (.log js/console "Rendering" (count (:data @state)))
      (if (pos? (count (:data @state)))
        [:div
         [:h1 "sunburst"]
         [svg-sunburst (:data @state)]]
        [:div "loading..."]))}))
