(ns cljs.portman.misc
  (:require [reagent.core :as reagent]))


(def CTG (reagent/adapt-react-class (aget js/React "addons" "CSSTransitionGroup")))

(defn animated [show]
  [:p {:style {:font-size "300%"}}
   "foo "
   [CTG {:transition-name "foo"}
    [:span "fixed "]
    (when @show
      ^{:key "kjadofid"} [:span "Animated"])]])
