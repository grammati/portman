(ns portman.core
  (:require
   [cljs.core.async :as async :refer [<! >!]]
   [om.core :as om]
   [om-tools.core ]
   [om-tools.dom :as dom :include-macros true]
   [portman.data :as data]
   [portman.layout :as layout]
   [portman.table :as table]
   [portman.svg :as svg]
   [sablono.core :refer-macros [html]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))


(enable-console-print!)

(defonce app-data (atom {:message "Hello there"
                         :data []}))

(def drag-handle-col
  {:render   (fn [_] (html [:span.icon.icon-five-dots]))
   :td-class (fn [d] (str "row-handle bg-strategy"))})

(def checkbox-col
  {:render   (fn [d] [:input {:type "checkbox"}])
   :td-class "grid-controls"})

(def gear-menu-col
  {:render   (fn [d] [:span.icon.icon-gear])
   :td-class "grid-controls"})

(defn expandable [{:keys [render] :as col}]
  (assoc col
    :render (fn [d]
              (html
               [:span
                [:a {:on-click (fn [e] (println e)) :href "javascript:;"}
                 [:span.icon.icon-right-full {:style {:margin "0 10"}}]]
                (render d)]))
    :td-class "expand"))

(def formatted-id-col
  {:header "ID"
   :render (fn [d]
             (let []
               (html
                [:a.id {:href (d "_ref")}
                 [:span.icon.icon-portfolio.margin-right-half]
                 (d "FormattedID")])))})

(def portfolio-item-table
  (table/define-table
    {:cols [drag-handle-col
            checkbox-col
            gear-menu-col
            (expandable formatted-id-col)
            {:header "Name"
             :render (fn [d] (d "Name"))}
            {:header "Leaf Story Count"
             :render (fn [d] (d "LeafStoryCount"))}]}))

(defn pi-table [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/table
       {:class "table table-condensed"}
       (dom/thead
        (dom/tr
         (for [t ["ID" "Name" "LeafStoryCount"]]
           (dom/th t))))
       (dom/tbody
        (for [row data]
          (dom/tr
           (dom/td (row "FormattedID"))
           (dom/td (row "Name"))
           (dom/td (row "LeafStoryCount")))))))))


(defn app [data owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (go
        (let [pis (<! (data/load-pis))]
          (swap! app-data assoc-in [:data] pis))))
    om/IRender
    (render [_]
      (layout/container-fluid
       (layout/row
        (layout/col 9 (om/build portfolio-item-table (:data data)))
        (layout/col 3 (om/build svg/hot-pie (:data data))))
       ;;(thingy (:data data))
       ))))

(defn render-app []
  (om/root app
           app-data
           {:target (.getElementById js/document "portman-main")}))

(render-app)
