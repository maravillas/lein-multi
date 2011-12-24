(ns leiningen.test.multi
  (:use [leiningen.multi] :reload-all)
  (:use [leiningen.core :only [defproject read-project]]
        [clojure.test]
        [clojure.java.io :only [file]])
  (:import [org.apache.commons.io FileUtils]))

(def test-project (merge (read-project "test-project/project.clj")
                         ;; See comment in multi/run-deps re: path
                         {:multi-library-path "test-project/multi-lib-test"}))

(defn add-clojure-deps
  [project & versions]
  (merge-with merge project {:multi-deps
                             (apply hash-map
                                    (mapcat (fn [v] [v [['org.clojure/clojure v]]])
                                            versions))}))

(defn add-common
  [project & common]
  (merge-with merge project {:multi-deps
                             {:all common}}))

(defn list-files
  [path]
  (set (map #(.getName %) (.listFiles (file path)))))

(defn delete-dirs
  [& dirs]
  (try
    (doseq [dir dirs]
      (FileUtils/deleteDirectory dir))
    (catch Exception e
      (println e))))

(deftest test-multi-deps
  (delete-dirs (file (:root test-project) "lib")
               (file (:root test-project) "multi-lib-test"))
  (let [test-project (add-clojure-deps test-project "1.1.0" "1.2.0")
        lib-path (str (:root test-project) "/multi-lib-test")]
    (multi test-project "deps")
    (is (= #{"clojure-1.2.0-RC1.jar"} (list-files (str (:root test-project) "/lib"))))
    (is (= #{"clojure-1.1.0.jar"} (list-files (str lib-path "/1.1.0"))))
    (is (= #{"clojure-1.2.0.jar"} (list-files (str lib-path "/1.2.0"))))))

(deftest test-failing-multi-tests
  (delete-dirs (file (:root test-project) "multi-lib-test"))
  (println "*** Begin embedded tests - ignore results below ***")
  (let [test-project (add-clojure-deps test-project "1.1.0" "1.2.0")
        result (multi test-project "test")]
    (println "*** End embedded tests - ignore results above ***")
    (is (= result 1))))

(deftest test-passing-multi-tests
  (delete-dirs (file (:root test-project) "multi-lib-test"))
  (println "*** Begin embedded tests - ignore results below ***")
  (let [test-project (add-clojure-deps test-project "1.2.0")
        result (multi test-project "test")]
    (println "*** End embedded tests - ignore results above ***")
    (is (= result 0))))

(deftest test-multi-tests-with-namespaces
  (delete-dirs (file (:root test-project) "multi-lib-test"))
  (println "*** Begin embedded tests - ignore results below ***")
  (let [test-project (add-clojure-deps test-project "1.2.0")
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
  (delete-dirs (file "multi-test-new-project")))

(deftest test-with-dep
  (delete-dirs (file (:root test-project) "multi-lib-test"))
  (let [test-project (add-clojure-deps test-project "1.1.0" "1.2.0")
        lib-path (str (:root test-project) "/multi-lib-test")
        result (multi test-project "deps" "--with" "1.2.0")]
    (is (= #{"clojure-1.2.0.jar"} (list-files (str lib-path "/1.2.0"))))
    (is (not= #{"clojure-1.1.0.jar"} (list-files (str lib-path "/1.1.0"))))))

(deftest test-missing-with-set
  (let [test-project (add-clojure-deps test-project "1.1.0" "1.2.0")
        result (multi test-project "classpath" "--with" "1.3.0")]
    (is (= result 1))))

(deftest test-common-deps-set
  (delete-dirs (file (:root test-project) "lib")
               (file (:root test-project) "multi-lib-test"))
  (let [test-project (add-clojure-deps test-project "1.1.0" "1.2.0")
        test-project (add-common test-project ['org.clojure/core.logic "0.6.7"])
        lib-path (str (:root test-project) "/multi-lib-test")]
    (multi test-project "deps")
    (is (some #{"core.logic-0.6.7.jar"} (list-files (str (:root test-project) "/lib"))))
    (is (some #{"core.logic-0.6.7.jar"} (list-files (str lib-path "/1.1.0"))))
    (is (some #{"core.logic-0.6.7.jar"} (list-files (str lib-path "/1.2.0"))))))

(deftest test-common-deps-set-and-with
  (delete-dirs (file (:root test-project) "lib")
               (file (:root test-project) "multi-lib-test"))
  (let [test-project (add-clojure-deps test-project "1.1.0" "1.2.0")
        test-project (add-common test-project ['org.clojure/core.logic "0.6.7"])
        lib-path (str (:root test-project) "/multi-lib-test")]
    (multi test-project "deps" "--with" "1.2.0")
    (is (not-any? #{"core.logic-0.6.7.jar"} (list-files (str (:root test-project) "/lib"))))
    (is (not-any? #{"core.logic-0.6.7.jar"} (list-files (str lib-path "/1.1.0"))))
    (is (some #{"core.logic-0.6.7.jar"} (list-files (str lib-path "/1.2.0"))))))

(deftest test-common-deps-set-and-exclusions
  (delete-dirs (file (:root test-project) "lib")
               (file (:root test-project) "multi-lib-test"))
  (let [test-project (merge test-project
                            {:dependencies [['org.clojure/clojure "1.3.0"]]
                             :multi-deps {:all [['koan-engine "0.1.1"
                                                 :exclusions ['fresh]]]
                                          "1.2.0" [['org.clojure/clojure "1.2.0"]]
                                          "1.2.1" [['org.clojure/clojure "1.2.1"]]}})
        lib-path (str (:root test-project) "/multi-lib-test")]
    (multi test-project "deps")
    (let [files-base (list-files (str (:root test-project) "/lib"))
          files-120 (list-files (str lib-path "/1.2.0"))
          files-121 (list-files (str lib-path "/1.2.1"))]
      (is (some #{"koan-engine-0.1.1.jar"} files-base))
      (is (some #{"koan-engine-0.1.1.jar"} files-120))
      (is (some #{"koan-engine-0.1.1.jar"} files-121))
      (is (not-any? #(re-matches #"fresh.*" %) files-base))
      (is (not-any? #(re-matches #"fresh.*" %) files-120))
      (is (not-any? #(re-matches #"fresh.*" %) files-121)))))