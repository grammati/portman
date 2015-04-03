(ns rally.events
  (:require [cljs.core.async :as async :refer [<! >!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))


(defonce event-bus (async/chan))

(defn publish [event-id payload]
  (.log js/console "pub")
  (async/put! event-bus [event-id payload])
  nil)

(defonce subscriptions
  (atom nil))

(defn subscribe [event-id handler]
  (swap! subscriptions update-in [event-id]
         (fn [handlers]
           (if handlers
             (conj handlers handler)
             [handler]))))

(defn init! []
  (go-loop []
    (when-let [[event-id payload] (<! event-bus)]
      (.log js/console "event!")
      (doseq [handler (get @subscriptions event-id)]
        (try
          (handler payload)
          (catch :default e
            (.log js/console "Exception in handler:" handler))))
      (recur))))

