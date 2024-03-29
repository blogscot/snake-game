(defproject clojure-snake "0.2.0"
  :description "The snake game implemented using Clojure and Swing."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.cli "0.4.2"]
                 [com.googlecode.soundlibs/mp3spi "1.9.5.4"]]
  :main ^:skip-aot clojure-snake.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
