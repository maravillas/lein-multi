(ns leiningen.test.multi
  (:use [leiningen.multi] :reload-all)
  (:use [leiningen.core :only [defproject read-project]]
	[clojure.test]
	[clojure.contrib.io :only [file delete-file-recursively]]))

(def test-project (merge (read-project "test-project/project.clj")
		       ;; See comment in multi/run-deps re: path
		       {:multi-library-path "test-project/multi-lib-test"}))

(defn add-clojure-deps
  [project & versions]
  (merge project {:multi-deps
                  (apply hash-map
                         (mapcat (fn [v] [v [['org.clojure/clojure v]]])
                                 versions))}))

(defn list-files
  [path]
  (set (map #(.getName %) (.listFiles (file path)))))

(deftest test-multi-deps
  (delete-file-recursively (file (:root test-project) "lib") true)
  (delete-file-recursively (file (:root test-project) "multi-lib-test") true)
  (let [test-project (add-clojure-deps test-project "1.1.0" "1.2.0")
	lib-path (str (:root test-project) "/multi-lib-test")]
    (multi test-project "deps")
    (is (= #{"clojure-1.2.0-RC1.jar"} (list-files (str (:root test-project) "/lib"))))
    (is (= #{"clojure-1.1.0.jar"} (list-files (str lib-path "/1.1.0"))))
    (is (= #{"clojure-1.2.0.jar"} (list-files (str lib-path "/1.2.0"))))))

(deftest test-failing-multi-tests
  (delete-file-recursively (file (:root test-project) "multi-lib-test") true)
  (println "*** Begin embedded tests - ignore results below ***")
  (let [test-project (add-clojure-deps test-project "1.1.0" "1.2.0")
	result (multi test-project "test")]
    (println "*** End embedded tests - ignore results above ***")
    (is (= result 1))))

(deftest test-passing-multi-tests
  (delete-file-recursively (file (:root test-project) "multi-lib-test") true)
  (println "*** Begin embedded tests - ignore results below ***")
  (let [test-project (add-clojure-deps test-project "1.2.0")
	result (multi test-project "test")]
    (println "*** End embedded tests - ignore results above ***")
    (is (= result 0))))

(deftest test-multi-tests-with-namespaces
  (delete-file-recursively (file (:root test-project) "multi-lib-test") true)
  (println "*** Begin embedded tests - ignore results below ***")
  (let [test-project (add-clojure-deps test-project "1.1.0")
	result (multi test-project "test" "sample2")]
    (println "*** End embedded tests - ignore results above ***")
    ;; If the sample2 namespace argument is ignored, sample/failing-test-1.1.0
    ;; will result in a return value of 1, as in test-failing-multi-tests
    (is (= result 0))))

(deftest test-multi-no-sets
  (println "*** Begin embedded tests - ignore results below ***")
  (let [test-project (merge test-project {:multi-deps nil})
	result (multi test-project "test")]
    (println "*** End embedded tests - ignore results above ***")
    (is (= result 0))))

(deftest test-multi-new
  (multi test-project "new" "multi-test-new-project")
  (is (.exists (file "multi-test-new-project")))
  (delete-file-recursively (file "multi-test-new-project") true))

