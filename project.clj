(defproject simple "0.9.0"
  :dependencies [[org.clojure/clojure        "1.8.0"]
                 [org.clojure/clojurescript  "1.9.227"]
                 [reagent  "0.6.0-rc" :exclusions [cljsjs/react
                                                   cljsjs/react-dom]]
                 [re-frame "0.9.4"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-figwheel  "0.5.4-7"]]

  :hooks [leiningen.cljsbuild]

  :profiles {:dev {:cljsbuild
                   {:builds {:client {:figwheel     {:on-jsload "simple.core/run"}
                                      :compiler     {:main "simple.core"
                                                     :asset-path "js"
                                                     :optimizations :none
                                                     :foreign-libs
                                                       [{:file "public/js/bundle.js"
                                                         :provides ["cljsjs.react" "cljsjs.react.dom" "webpack.bundle"]}]
                                                     :source-map true
                                                     :source-map-timestamp true}}}}}

             :prod {:cljsbuild
                    {:builds {:client {:compiler    {:optimizations :simple
                                                     :closure-defines {goog.DEBUG false}
                                                     :foreign-libs
                                                       [{:file "public/js/bundle.js"
                                                         :provides ["cljsjs.react" "cljsjs.react.dom" "webpack.bundle"]}]
                                                     :elide-asserts true
                                                     :pretty-print false}}}}}}

  :figwheel {:repl false}

  :clean-targets ^{:protect false} ["resources/public/js"]

  :cljsbuild {:builds {:client {:source-paths ["src"]
                                :compiler     {:output-dir "resources/public/js"
                                               :output-to  "resources/public/js/client.js"}}}})
