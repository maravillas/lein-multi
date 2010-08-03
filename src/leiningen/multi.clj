(ns leiningen.multi
  (:use [leiningen.deps :only [deps]]
	[leiningen.core :only [resolve-task no-project-needed]])
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

(defn- print-base-message
  [task project]
  (println (str "Running \"lein " task "\" on base dependencies: " (:dependencies project))))

(defn- print-set-message
  [task n deps]
  (println (str "Running \"lein " task "\" on dependencies set " n ": " deps)))

(defn- run-deps
  [project & args]
  (print-base-message "deps" project)
  (apply deps project args)
  (run-multi-task #(deps % true)
		  project
		  (partial print-set-message "deps")))

(defn- run-task
  [task project & args]
  (print-base-message task project)
  (let [task-fn (resolve-task task)
	results (cons (apply task-fn project args)
		      (run-multi-task #(apply task-fn % args)
				      project
				      (partial print-set-message task)))
	;; Should we assume nils mean failure, if mixed with non-nils?
	valued? (some (complement nil?) results)
	success? (every? zero? results)]
    (when valued?
      (if success? 0 1))))

(defn multi
  "Run a task against multiple dependency sets as specified by :multi-deps in project.clj."
  [project task & args]
  (cond (@no-project-needed task) (do
				    (println (str "lein multi has no effect for task \"" task "\" - running task as normal"))
				    (apply (resolve-task task) args))
	(= task "deps") (apply run-deps project args)
	:else (apply run-task task project args)))