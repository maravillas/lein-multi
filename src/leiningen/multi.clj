(ns leiningen.multi
  (:use [leiningen.deps :only [deps]])
  (:require [leiningen.test]))

(def task-whitelist ["deps" "test" "run" "compile" "jar" "uberjar"])

;; http://thinkrelevance.com/blog/2009/08/12/rifle-oriented-programming-with-clojure-2.html
(defn- indexed
  [coll]
  (map vector (iterate inc 0) coll))

(defn- multi-library-path
  [project]
  ;; Should the path be relative to the project root or the cwd?
  ;; The defaults in leiningen.core/defproject choose the latter, so I will as
  ;; well, but it seems incorrect.
  ;; TODO: Verify
  (or (:multi-library-path project)
      (str (:root project) "/multi-lib")))

(defn- project-for-set
  [project index deps]
  (merge project {:library-path (str (multi-library-path project) "/set" index)
		  :dependencies deps}))

(defn- run-deps
  [project & args]
  (apply deps project args)
  (doseq [[index deps-set] (indexed (:multi-deps project))]
    (deps (project-for-set project index deps-set) true)))

(defn- run-test
  [project & args]
  (apply leiningen.test/test project args)
  (doseq [index (range (count (:multi-deps project)))]
    ))

(defn multi
  [project task & args]
  (when (= task "deps")
    (apply run-deps project args)))