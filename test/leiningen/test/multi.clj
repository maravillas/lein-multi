(ns leiningen.test.multi
  (:use [leiningen.multi] :reload-all)
  (:use [leiningen.core]
	[clojure.test]
	[clojure.contrib.io :only [file]]))

(defproject lein-multi-test "0.1.0"
  :dependencies [[org.clojure/clojure "1.2.0-RC1"]])

(deftest test-multi-deps
  (let [project (merge lein-multi-test
		       {:multi-library-path "multi-lib-test"
			:multi-deps [[['org.clojure/clojure "1.1.0"]]
				     [['org.clojure/clojure "1.2.0-beta1"]]]})]
    (multi project "deps")
    (println (file (:root project) "multi-lib-test" "set0" "clojure-1.1.0.jar"))
    (is (.exists (file (:root project) "multi-lib-test" "set0" "clojure-1.1.0.jar")))
    (is (.exists (file (:root project) "multi-lib-test" "set1" "clojure-1.2.0-beta1.jar")))))