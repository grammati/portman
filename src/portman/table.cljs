(ns portman.table
  (:require
   [om.core :as om]
   [om-tools.dom :as dom :include-macros true]
   [sablono.core :refer-macros [html]]))


(defn loading-row [config]
  (let [colspan (count (:cols config))]
    (html [:tr.bd-grid-child
           [:td.text-center {:col-span colspan}
            "Loading..."]])))

(defn define-table [config]
  (let [cell-components
        (for [{:keys [render td-class]} (:cols config)
              :let [render-fn (if (fn? render) render (constantly render))
                    td-class-fn (if (fn? td-class) td-class (constantly td-class))]]
          (fn [row-data owner]
            (reify
              om/IRender
              (render [_]
                (html
                 [:td {:class (td-class-fn row-data)}
                  (render-fn row-data)])))))

        row-component
        (fn [row-data owner]
          (reify
            om/IRender
            (render [_]
              (html
               [:tr
                (for [comp cell-components]
                  (om/build comp row-data))]))))

        build-rows
        (fn build-rows [row-data depth]
          (cons
           (om/build row-component (assoc row-data :_depth depth))
           (if (:loading-children? row-data)
             [(loading-row config)]
             (mapcat #(build-rows % (inc depth)) (:children row-data)))))
        
        ;; Component for a group of rows, including a top-level row
        ;; and, optionally, all its descendant rows (children,
        ;; grandchildren, etc.)
        top-level-row-component
        (fn [row-data owner]
          (reify
            om/IRender
            (render [_]
              (let []
                (html
                 [:tbody
                  (build-rows row-data 0)])))))]
    
    (fn [data owner]
      (reify
        om/IRender
        (render [_]
          (html
           [:table.table.table-condensed
            [:thead
             [:tr
              (for [col (:cols config)]
                [:th (:header col)])]]
            (om/build-all top-level-row-component data {:key "ObjectID"})]))))))


