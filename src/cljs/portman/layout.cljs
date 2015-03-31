(ns portman.layout)


(defn- bs-layout [class & rows]
  [:div {:class class} rows])

(defn row [& cols]
  [:div {:class "row"} cols])

(defn col [width & contents]
  [:div {:class (str "col-md-" width)}
   contents])

(defn container [& rows]
  (apply bs-layout "container" rows))

(defn container-fluid [& rows]
  (apply bs-layout "container-fluid" rows))
