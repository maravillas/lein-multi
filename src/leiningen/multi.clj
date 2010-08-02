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
  ;; Should the path be relative to the project root or the cwd?
  ;; The defaults in leiningen.core/defproject choose the latter, so I will as
  ;; well, but it seems incorrect.
  ;; TODO: Verify
  (let [multi-library-path (or (:multi-library-path project)
			       (str (:root project) "/multi-lib"))]
    (deps project true)
    (doseq [[index deps-set] (indexed (:multi-deps project))]
      (deps (merge project {:library-path (str multi-library-path "/set" index)
			    :dependencies deps-set}) true))))

(defn multi
  [project task & args]
  (when (= task "deps")
    (run-deps project)))