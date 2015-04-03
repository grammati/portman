(ns rally.layout
  "Bootsrap layout components.")


(defn map-keys [col]
  (map-indexed #(vary-meta %2 assoc :key %1) col))

(defn- bs-layout [class & rows]
  (apply vector :div {:class class} rows))

(defn row [& cols]
  (apply vector :div {:class "row"} cols))

(defn col [width & contents]
  (apply vector :div {:class (str "col-md-" width)} contents))

(defn container [& rows]
  (apply bs-layout "container" rows))

(defn container-fluid [& rows]
  (apply bs-layout "container-fluid" rows))

(defn tab-bar [tabs]
  [:ul.nav.nav-tabs
   (for [{:keys [url title active?]} tabs]
     [:li [:a {:href  url
               :class (when active? "active")}
           title]])])

