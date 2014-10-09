(ns portman.svg
  (:require
   [om.core :as om]
   [om-tools.dom :as dom :include-macros true]
   [strokes :refer [d3]]))


(defn hot-pie [data owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (-> d3
          (.select (.getDOMNode owner))
          (.append "defs")
          (.append "clipPath")
          (.attr "id" "foobar")
          (.append "circle")
          (.attr "cx" 200)
          (.attr "cy" 200)
          (.attr "r" 150))
      (-> d3
          (.select (.get (js/jQuery "g" (.getDOMNode owner)) 0))
          (.attr "clip-path" "url(#foobar)")))
    om/IRender
    (render [_]
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
        (dom/svg {:width 400 :height 400}
                 (dom/g
                  (for [[val ptot] (map vector data ptots)]
                    (let [angle (* 2.0 Math/PI (/ val total))
                          rot   (* 360.0 (/ ptot total))
                          end-x (+ 200 (* (Math/sin angle) radius))
                          end-y (- 200 (* (Math/cos angle) radius))]
                      (dom/path {:d (str "M " center " L " top " A 150 150 0 " (if (> rot 180) 0 1) " 1 " end-x " " end-y " z")
                                 :transform (str "rotate(" rot "," center ") "
                                                 "rotate(" (* 180 (/ val total)) "," center ")"
                                                 "translate(0,-0) "
                                                 "rotate(" (* -180 (/ val total)) "," center ")"
                                                 )
                                 :style {:stroke "black" :stroke-width 0 :fill (rand-color)}})))))))))

