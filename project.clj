(defproject portman "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3126" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 
                 [reagent "0.5.1-SNAPSHOT" :exclusions [cljsjs/react]]
                 [cljsjs/react-with-addons "0.13.1-0"]
                 
                 [ring-server "0.4.0"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.4"]
                 [prone "0.8.1"]
                 [compojure "1.3.2"]
                 [selmer "0.8.2"]
                 [environ "1.0.0"]

                 ]

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-environ "1.0.0"]
            [lein-ring "0.9.1"]
            [lein-asset-minifier "0.2.2"]]

  :ring {:handler portman.handler/app
         :uberwar-name "portman.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "portman.jar"

  :main portman.server

  :clean-targets ^{:protect false} ["resources/public/js"]

  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.2"]
                                  [leiningen "2.5.1"]
                                  [figwheel "0.2.5"]
                                  [pjstadig/humane-test-output "0.7.0"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.2.5"]
                             [com.cemerick/clojurescript.test "0.3.2"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port      3449
                              :css-dirs         ["resources/public/css"]
                              :ring-handler     portman.handler/app
                              :nrepl-port       8912
                              }

                   :env {:dev? true}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler     {:main       "portman.dev"
                                                             :source-map true}}
                                        :test {:source-paths ["src/cljs"  "test/cljs"]
                                               :compiler     {:output-to     "target/test.js"
                                                              :optimizations :whitespace
                                                              :pretty-print  true
                                                              :preamble      ["react/react.js"]}}}
                               :test-commands {"unit" ["phantomjs" :runner
                                                       "test/vendor/es5-shim.js"
                                                       "test/vendor/es5-sham.js"
                                                       "test/vendor/console-polyfill.js"
                                                       "target/test.js"]}}}

             :uberjar {:hooks [leiningen.cljsbuild minify-assets.plugin/hooks]
                       :env {:production true}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
