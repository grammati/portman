(ns rally.app
  (:require
   [cljs.core.async :as async :refer [<! >!]]
   [rally.data :as data]
   [rally.layout :as layout]
   [rally.table :as table]
   [rally.svg :as svg]
   [reagent.core :as reagent :refer [atom]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))


(defn- bg-class [d]
  (if-let [type (d "_type")]
    (str "bg-" (-> type (.split "/") second (or "") .toLowerCase))))

(def drag-handle-col
  {:render   (fn [_] [:span.icon.icon-five-dots])
   :td-class (fn [d] (str "row-handle " (bg-class d)))})

(def checkbox-col
  {:render   (fn [d] [:input {:type "checkbox"}])
   :td-class "grid-controls"})

(def gear-menu-col
  {:render   (fn [d] [:span.icon.icon-gear])
   :td-class "grid-controls"})

(defn expandable [{:keys [render] :as col}]
  (assoc col
    :render (fn [d]
              (fn []
                (let [local-state (atom {:expanded? false})]
                  [:span {:style {:margin-left (* 10 (:_depth d 0))}}
                   [:a {:on-click (fn [e]
                                    (data/load-children! d))
                        :href     "javascript:;"}
                    [:span.icon {:style {:margin "0 10"}
                                 :class (if (pos? (get (data/get-children d) "Count"))
                                          (if (or (:children d) (:loading-children? d))
                                            "icon-down-full"
                                            "icon-right-full"))}]]
                   (render d)])))
    :td-class (fn [d]
                (str ((:td-class col (constantly "")) d) " expand"))))

(def formatted-id-col
  {:header   "ID"
   :render   (fn [d]
               [:a.id {:href (d "_ref")}
                [:span.icon.icon-portfolio.margin-right-half]
                (d "FormattedID")])})

(def portfolio-item-table
  (table/define-table
    {:cols [drag-handle-col
            checkbox-col
            gear-menu-col
            (expandable formatted-id-col)
            {:header "Name"
             :render (fn [d] (d "Name"))}
            {:header "Child Count"
             :render (fn [d] (get (data/get-children d) "Count"))}
            {:header "Leaf Story Count"
             :render (fn [d] (d "LeafStoryCount"))}]}))


(defn app [state]
  (go
    (let [strategies (<! (data/load-pis))
          strategies (vec (sort-by #(- (get % "LeafStoryCount" 0)) strategies))]
      (swap! state assoc-in [:data] strategies)))
  (fn []
    (layout/container-fluid
     (layout/row
      (layout/col 8 [portfolio-item-table (:data @state)])
      (layout/col 4 [svg/hot-pie (:data @state)])))))

(defn render-app []
  (reagent/render-component [app]
                            {:target (.getElementById js/document "rally")}))
