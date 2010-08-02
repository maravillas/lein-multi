(ns leiningen.test.multi
  (:use [leiningen.multi] :reload-all)
  (:use [leiningen.core :only [read-project]]
	[clojure.test]
	[clojure.contrib.io :only [file]]))

(def test-project (read-project "test/sample/project.clj"))

(deftest test-multi-deps
  (let [project (merge lein-multi-test
		       ;; See comment in multi/run-deps re: path
		       {:multi-library-path "test/sample/multi-lib-test"
			:multi-deps [[['org.clojure/clojure "1.1.0"]]
				     [['org.clojure/clojure "1.2.0-beta1"]]]})
	lib-path (str (:root project) "/multi-lib-test")]
    (multi project "deps")
    (is (.exists (file lib-path "set0" "clojure-1.1.0.jar")))
    (is (.exists (file lib-path "set1" "clojure-1.2.0-beta1.jar")))))