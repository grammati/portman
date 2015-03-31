(ns portman.svg)


(defn hot-pie [data]
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
    [:svg {:width 400 :height 400}
     [:g
      (for [[val ptot] (map vector data ptots)]
        (let [angle (* 2.0 Math/PI (/ val total))
              rot   (* 360.0 (/ ptot total))
              end-x (+ 200 (* (Math/sin angle) radius))
              end-y (- 200 (* (Math/cos angle) radius))]
          [:path {:d (str "M " center " L " top " A 150 150 0 " (if (> rot 180) 0 1) " 1 " end-x " " end-y " z")
                  :transform (str "rotate(" rot "," center ") "
                                  "rotate(" (* 180 (/ val total)) "," center ")"
                                  "translate(0,-0) "
                                  "rotate(" (* -180 (/ val total)) "," center ")"
                                  )
                  :style {:stroke "black" :stroke-width 0 :fill (rand-color)}}]))]]))



(comment
  {:component-did-mount
   (fn [this]
     (-> d3
         (.select (reagent/dom-node this))
         (.append "defs")
         (.append "clipPath")
         (.attr "id" "foobar")
         (.append "circle")
         (.attr "cx" 200)
         (.attr "cy" 200)
         (.attr "r" 150))
     (-> d3
         (.select (.get (js/jQuery "g" (reagent/dom-node this)) 0))
         (.attr "clip-path" "url(#foobar)")))})
