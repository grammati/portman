(ns portman.data
  (:require
   [cljs.core.async :as async :refer [<! >!]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))


(defonce wsapi-cache (atom nil))

(defn ajax [url opts]
  (let [ch (async/chan)]
    (.ajax js/jQuery
           url
           (clj->js
            {:data     (clj->js (:params opts))
             :dataType "text"
             :success  (fn [response-text]
                         (let [body (js->clj (.parse js/JSON response-text))]
                           (async/put! ch
                                       (if (body "Errors")
                                         (js/Error. body)
                                         (get-in body ["QueryResult" "Results"])))))
             :error    (fn [e]
                         (async/put! ch e))}))
    ch))

(defn load-pis []
  (ajax "/slm/webservice/v2.0/artifact"
        {:params {:types              "portfolioitem/strategy"
                  :pagesize           50
                  :start              1
                  ;;:shallowFetch       "ObjectID,Name"
                  :fetch              true
                  :includePermissions true
                  }}))

(defn get-children [thing]
  (or (get thing "Children")
      (get thing "UserStories")))

(defn load-children! [d]
  (swap! d assoc :loading-children? true)
  (go
    (let [children (<! (ajax (get (get-children @d) "_ref") {}))
          children (vec (sort-by #(- (get % "LeafStoryCount" 0)) children))]
      (swap! d assoc :children children)
      (swap! d assoc :loading-children? nil))))
