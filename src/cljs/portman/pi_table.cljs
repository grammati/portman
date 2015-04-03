(ns portman.pi-table
  (:require
   [cljs.core.async :as async :refer [<! >!]]
   [rally.data :as data]
   [rally.layout :as layout]
   [rally.table :as table]
   [rally.svg :as svg]
   [reagent.core :as reagent :refer [atom]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))


(defn- bg-class [item]
  (if-let [type (get item "_type")]
    (str "bg-" (-> type (.split "/") second (or "") .toLowerCase))))

(def drag-handle-col
  {:render   (fn [_] [:span.icon.icon-five-dots])
   :td-class (fn [$item] (str "row-handle " (bg-class @$item)))})

(def checkbox-col
  {:render   (fn [$item] [:input {:type "checkbox"}])
   :td-class "grid-controls"})

(def gear-menu-col
  {:render   (fn [$item] [:span.icon.icon-gear])
   :td-class "grid-controls"})

(defn expandable [{:keys [render] :as col}]
  (assoc col
    :render (fn [$item]
              (let [local-state (atom {:expanded? false})]
                [:span {:style {:margin-left (* 10 (:_depth @$item 0))}}
                 [:a {:on-click (fn [e]
                                  (data/load-children! $item))
                      :href     "javascript:;"}
                  [:span.icon {:style {:margin "0 10"}
                               :class (if (pos? (get (data/get-children @$item) "Count"))
                                        (if (or (:children @$item) (:loading-children? @$item))
                                          "icon-down-full"
                                          "icon-right-full"))}]]
                 [render $item]]))
    :td-class (fn [$item]
                (str ((:td-class col (constantly "")) $item) " expand"))))

(def formatted-id-col
  {:header "ID"
   :render (fn [$item]
             [:a.id {:href (get @$item "_ref")}
              [:span.icon.icon-portfolio.margin-right-half]
              (get @$item "FormattedID")])})


(def pi-table-config
  (reagent/atom
   {:keyfn #(get % "FormattedID")
    :cols  [
            drag-handle-col
            checkbox-col
            gear-menu-col
            (expandable formatted-id-col)
            {:header "Name"
             :render (fn [$item] (get @$item "Name"))}
            ;; {:header "Child Count"
            ;;  :render (fn [$item] (get (data/get-children @$item) "Count"))}
            ;; {:header "Leaf Story Count"
            ;;  :render (fn [$item] (get @$item "LeafStoryCount"))}
            ]}))


(defn app [state]
  (reagent/create-class
   {:display-name "pi-table-app"
    :component-did-mount
    (fn [this]
      (go
        (let [strategies (<! (data/load-pis))
              strategies (vec (sort-by #(- (get % "LeafStoryCount" 0)) strategies))]
          (.log js/console "Loaded strategies:" (clj->js strategies))
          (swap! state assoc-in [:data] strategies))))
    :reagent-render
    (fn [state]
      [:h1 "foo"]
      [table/table pi-table-config (reagent/cursor state [:data])]
      #_[table/table
       (reagent/atom {:cols [{:header "One"
                              :render (fn [_] [:span "foo"])}]})
       (reagent/atom [{} {} {}])]
      )}))

