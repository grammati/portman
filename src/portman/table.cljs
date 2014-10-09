(ns portman.table
  (:require
   [om.core :as om]
   [om-tools.dom :as dom :include-macros true]
   [sablono.core :refer-macros [html]]))


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
                  (om/build comp row-data))]))))]
    
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
            [:tbody
             (om/build-all row-component data)]]))))))


