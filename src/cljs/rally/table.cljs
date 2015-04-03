(ns rally.table
  (:require [reagent.core :as reagent]
            [rally.events :as events])
  (:require-macros [rally.reagent-utils :refer [for! cur-for]]))


(defn loading-row [config]
  (let [colspan (count (:cols config))]
    [:tr.bd-grid-child
     [:td.text-center {:col-span colspan}
      "Loading..."]]))

(defn define-table
  "Returns a table component with the columns configured in `config`,
  which must be a map containing:
   - :cols - vector of column configs, each of which is a map containing:
     - :render - function that returns the rendered contents of a cell
     - :td-class - optional, string or fn, returns the class to apply to
                   td elements in this column
  "
  [config]
  (let [cell-components
        (for [{:keys [render td-class]} (:cols config)
              :let [render-fn (if (fn? render) render (constantly render))
                    td-class-fn (if (fn? td-class) td-class (constantly td-class))]]
          (fn [row-data]
            [:td {:class (td-class-fn row-data)}
             (render-fn row-data)]))

        row-component
        (fn [row-data]
          [:tr
           (for [comp cell-components]
             (comp row-data))])

        build-rows
        (fn build-rows [row-data depth]
          (cons
           [row-component (assoc row-data :_depth depth)]
           (if (:loading-children? row-data)
             [(loading-row config)]
             (mapcat #(build-rows % (inc depth)) (:children row-data)))))
        
        ;; Component for a group of rows, including a top-level row
        ;; and, optionally, all its descendant rows (children,
        ;; grandchildren, etc.)
        top-level-row-component
        (fn [row-data]
          [:tbody
           (build-rows row-data 0)])]
    
    (fn [data]
      [:table.table.table-condensed
       [:thead
        [:tr
         (for [col (:cols config)]
           [:th (:header col)])]]
       (for [item data]
         [top-level-row-component item])])))



(defn- indexed [col]
  (map-indexed vector col))

(defn table [config data]
  (let [selected (reagent/atom (first @data))]
    (events/subscribe :table/select
                      (fn [item]
                        (reset! selected item)))
    (fn [config data]
      (let [config @config
            keyfn  (or (:keyfn config) :key)
            cols   (->> config
                        :cols
                        (map-indexed vector)
                        (map (fn [[i item]] (assoc item :index i)))
                        vec)]
        [:table.table.table-condensed
         {:tab-index 1
          :on-key-down (fn [evt]
                         (.log js/console (.-keyCode evt)))}
         [:thead
          [:tr
           (for [col cols]
             ^{:key (:index col)}
             [:th (:header col)])]]
         [:tbody
          (cur-for [$item data]
            ^{:key (keyfn @$item)}
            [:tr (merge
                  {:tab-index 1}
                  (if (identical? @selected @$item)
                    {:class "selected"}
                    {:on-click #(events/publish :table/select @$item)}))
             (for! [col cols]
               ^{:key (:index col)}
               [:td ((:render col) $item)])])]]))))

