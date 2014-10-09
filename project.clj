(defproject portman "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 ;;[com.facebook/react "0.11.1"]
                 [com.rallydev/figwheel "0.1.4-SNAPSHOT"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                 [om "0.7.1"]
                 [sablono "0.2.2"]
                 [prismatic/om-tools "0.3.2"]
                 [net.drib/strokes "0.5.1"]]
  
  :plugins [[lein-cljsbuild "1.0.3"]
            [com.rallydev/lein-figwheel "0.1.4-SNAPSHOT"]]

  :source-paths ["src"]

  :profiles
  {:dev {:source-paths ["dev"]}}
  
  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src" "dev"]
              :compiler {:output-to "resources/public/js/compiled/portman.js"
                         :output-dir "resources/public/js/compiled/out"
                         :optimizations :none
                         :source-map true}}
             {:id "min"
              :source-paths ["src"]
              :compiler {:output-to "www/portman.min.js"
                         :optimizations :advanced
                         :pretty-print false
                         :preamble ["react/react.min.js"]
                         :externs ["react/externs/react.js"]}}]}
  :figwheel {
             :http-server-root "public" ;; default and assumes "resources" 
             :server-port 3449 ;; default
             :css-dirs ["public/resources/css"] ;; watch and update CSS
             ;; :ring-handler portman.server/handler
             })
