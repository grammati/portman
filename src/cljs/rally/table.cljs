(ns rally.table
  (:require [reagent.core :as reagent]
            [rally.events :as events])
  (:require-macros [rally.reagent-utils :refer [for! cur-for]]))


(defn loading-row [config]
  (let [colspan (count (:cols config))]
    [:tr.bd-grid-child
     [:td.text-center {:col-span colspan}
      "Loading..."]]))

(defn table
  "Table component with the columns configured in `config`, which must be 
   a map containing:
   - :keyfn - default `:key`
   - :cells - vector of components. Must return a :td.
   - :headers - vector of strings
  "
  [config data]
  (let [keyfn (:keyfn @config :key)

        row-component
        (fn [-item]
          ^{:key (keyfn item)}
          [apply vector :tr
           (for [cell-component (:cells @config)]
             (cell-component -item))])

        build-rows
        (fn build-rows [-item depth]
          (swap! -item assoc :_depth depth)
          (let [row        [row-component -item]
                child-rows (cond
                             (:loading-children? @-item)
                             (list [loading-row @config])

                             (seq (:children @-item))
                             (mapcat #(build-rows % (inc depth))
                                     (cur-for [child (reagent/cursor -item [:children])]
                                              child)))]
            (cons row child-rows)))
        
        ;; Component for a group of rows, including a top-level row
        ;; and, optionally, all its descendant rows (children,
        ;; grandchildren, etc.)
        top-level-row-component
        (fn [-item]
          [:tbody
           (build-rows -item 0)])]
    
    [:table.table.table-condensed
     [:thead
      [:tr
       (for [header (:headers @config)]
         [:th header])]]
     (cur-for [-item data]
              [top-level-row-component -item])]))



