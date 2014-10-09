(ns portman.layout
  (:require
   [om-tools.dom :as dom :include-macros true]))


(defn- bs-layout [class & rows]
  (dom/div {:class class} rows))

(defn row [& cols]
  (dom/div {:class "row"} cols))

(defn col [width & contents]
  (dom/div {:class (str "col-md-" width)}
           contents))

(defn container [& rows]
  (apply bs-layout "container" rows))

(defn container-fluid [& rows]
  (apply bs-layout "container-fluid" rows))
