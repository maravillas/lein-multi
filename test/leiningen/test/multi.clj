(ns leiningen.test.multi
  (:use [leiningen.multi] :reload-all)
  (:use [leiningen.core :only [defproject read-project]]
	[clojure.test]
	[clojure.contrib.io :only [file delete-file-recursively]]))

(def test-project (read-project "test/sample/project.clj"))

(defn list-files
  [path]
  (set (map #(.getName %) (.listFiles (file path)))))

(deftest test-multi-deps
  (println (:root test-project))
  (delete-file-recursively (file (:root test-project) "lib") true)
  (delete-file-recursively (file (:root test-project) "multi-lib-test") true)
  (let [project (merge test-project
		       ;; See comment in multi/run-deps re: path
		       {:multi-library-path "test/sample/multi-lib-test"
			:multi-deps [[['org.clojure/clojure "1.1.0"]]
				     [['org.clojure/clojure "1.2.0-beta1"]]]})
	lib-path (str (:root project) "/multi-lib-test")]
    (multi project "deps")
    (is (= #{"clojure-1.2.0-RC1.jar"} (list-files (str (:root project) "/lib"))))
    (is (= #{"clojure-1.1.0.jar"} (list-files (str lib-path "/set0"))))
    (is (= #{"clojure-1.2.0-beta1.jar"} (list-files (str lib-path "/set1"))))))