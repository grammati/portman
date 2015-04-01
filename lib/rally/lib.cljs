(ns rally.lib)


(defn tab-bar [tabs]
  [:ul.nav.nav-tabs
   (for [{:keys [url title active?]} tabs]
     [:li [:a {:href  url
               :class (when active? "active")}
           title]])])
