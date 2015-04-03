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


;;; Table Cell definitions

(defn drag-handle [-item]
  [:td {:class (str "row-handle " (bg-class @-item))}
   [:span.icon.icon-five-dots]])

(defn checkbox [_]
  [:td.grid-controls [:input {:type "checkbox"}]])

(defn gear-menu [_]
  [:td.grid-contorls [:span.icon.icon-gear]])

(defn formatted-id [-item]
  [:a.id {:href (get @-item "_ref")}
   [:span.icon.icon-portfolio.margin-right-half]
   (get @-item "FormattedID")])

(defn expandable-formatted-id [-item]
  (let [item @-item]
    [:td.expand
     [:span {:style {:margin-left (* 10 (:_depth item 0))}}
      [:a {:on-click (fn [e]
                       (data/load-children! -item))
           :href     "javascript:;"}
       [:span.icon {:style {:margin "0 10"}
                    :class (if (pos? (get (data/get-children item) "Count"))
                             (if (or (:children item) (:loading-children? item))
                               "icon-down-full"
                               "icon-right-full"))}]]
      [formatted-id -item]]]))


(def pi-table-config
  (reagent/atom
   {:keyfn #(get % "FormattedID")
    :cells [drag-handle
            checkbox
            gear-menu
            expandable-formatted-id
            (fn [-item] [:td (get @-item "Name")])
            ;; {:header "Child Count"
            ;;  :render (fn [$item] (get (data/get-children @$item) "Count"))}
            ;; {:header "Leaf Story Count"
            ;;  :render (fn [$item] (get @$item "LeafStoryCount"))}
            ]
    :headers [nil nil nil "ID" "Name"]}))


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
      ;;[:h1 "foo"]
      [table/table pi-table-config (reagent/cursor state [:data])]
      )}))

