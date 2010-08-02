(ns leiningen.multi
  (:use [leiningen.deps :only [deps]]))

;; Hook clean
;; Put multi deps in /multi-lib

;; http://thinkrelevance.com/blog/2009/08/12/rifle-oriented-programming-with-clojure-2.html
(defn- indexed
  [coll]
  (map vector (iterate inc 0) coll))

(defn- run-deps
  [project]
  (doseq [[index deps-set] (indexed (:multi-deps project))]
    (deps (merge project {:library-path (format "multi-lib/set%d" index)
			  :dependencies deps-set}) true)))

(defn multi
  [project task & args]
  (when (= task "deps")
    (run-deps project)))