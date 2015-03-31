(ns portman.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljsjs.react :as react]
              [portman.lib :as lib]
              [portman.app :as app])
    (:import goog.History))


;;; The application state
(def state (atom {:current-page nil
                  :tabs [{:url "#/" :title "Home" :active? true}
                         {:url "#/about" :title "About"}
                         {:url "#/cool" :title "Cool"}]}))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to portman, dude!"]
   [:div [:a {:href "#/about"} "go to about page!!"]]])

(defn about-page []
  [:div [:h2 "About portman"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn cool-page []
  [:div [app/app state]])

(defn current-page []
  (.log js/console "calling current-page")
  (let [page (:current-page @state)]
    (.log js/console "page is" page)
    [:div (when page [page])]))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (swap! state assoc :current-page #'home-page))

(secretary/defroute "/about" []
  (swap! state assoc :current-page #'about-page))

(secretary/defroute "/cool" []
  (swap! state assoc :current-page #'cool-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(defn app []
  [:div [lib/tab-bar (:tabs @state)]
   [current-page]])

(defn mount-root []
  (reagent/render [app] (.getElementById js/document "portman"))
  ;;(reagent/render [portman.app/app] (.getElementById js/document "portman"))
  )

(defn init! []
  (hook-browser-navigation!)
  (mount-root))

