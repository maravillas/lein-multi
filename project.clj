(defproject lein-multi "1.1.0-SNAPSHOT"
  :description "A Leiningen plugin for running tasks against multiple dependency sets."
  :url "http://github.com/maravillas/lein-multi"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :dev-dependencies [[org.clojure/clojure "1.2.0"]
                     [org.clojure/clojure-contrib "1.2.0"]

                     ;; A lein jar is necessary for the tests. Clojars is a bit
                     ;; oudated, though, so I'll just let you pull it in yourself.
                     ;; Sorry.

                     ;;[leiningen "1.5.0"]
                     ])