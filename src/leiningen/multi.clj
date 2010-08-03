(ns leiningen.multi
  (:use [leiningen.deps :only [deps]])
  (:require [leiningen.test]))

(def task-whitelist ["deps" "test" "run" "compile" "jar" "uberjar"])

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

(defn- run-multi-task
  ([task-fn project]
     (run-multi-task task-fn project nil))
  ([task-fn project delimiter-fn]
     (doall
      (map-indexed (fn [i v]
		     (when delimiter-fn (delimiter-fn i v))
		     (task-fn (project-for-set project i v)))
		   (:multi-deps project)))))

(defn- run-deps
  [project & args]
  (println "Fetching base dependencies:" (:dependencies project))
  (apply deps project args)
  (run-multi-task #(deps % true)
		  project
		  #(println (str "Fetching dependencies set " %1 ": " %2))))

(defn- run-test
  [project & args]
  (println "Testing against base dependencies:" (:dependencies project))
  (let [result (cons (apply leiningen.test/test project args)
		     (run-multi-task leiningen.test/test
				     project
				     #(println (str "Testing against dependencies set " %1 ": " %2))))
	success? (every? zero? result)]
    ;; TODO: Summarize all runs
    (if success? 0 1)))

(defn multi
  [project task & args]
  (cond (= task "deps") (apply run-deps project args)
	(= task "test") (apply run-test project args)))