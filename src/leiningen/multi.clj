(ns leiningen.multi
  (:use [leiningen.deps :only [deps]]
	[leiningen.core :only [resolve-task arglists]])
  (:require [leiningen.test]))

(defn- multi-library-path
  [project]
  ;; Should the non-default path be relative to the project root or the cwd?
  ;; The defaults in leiningen.core/defproject choose the latter, so I will as
  ;; well. Doing so seems incorrect, edge case though it may be.
  ;; TODO: Verify
  (or (:multi-library-path project)
      (str (:root project) "/multi-lib")))

(defn- project-for-set
  [project name deps]
  (merge project {:library-path (str (multi-library-path project) "/" name)
		  :dependencies deps}))

(defn- run-multi-task
  ([task-fn project]
     (run-multi-task task-fn project nil))
  ([task-fn project delimiter-fn]
     (doall
      (map (fn [[k v]]
             (when delimiter-fn (delimiter-fn k v))
             (task-fn (project-for-set project k v)))
           (:multi-deps project)))))

(defn- print-base-message
  [task project]
  (println (str "Running \"lein " task "\" on base dependencies: "
                (:dependencies project))))

(defn- print-set-message
  [task name deps]
  (println (str "\nRunning \"lein " task "\" on dependencies set " name ": " deps)))

;; Handle the deps task individually, as we want to pass the "skip-dev" param
;; to the base call, but pass true for the multi calls.
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
        valued? (every? number? results)
        success? (every? #(and (number? %) (zero? %)) results)]
    (if valued?
      (if (every? zero? results) 0 1)
      results)))

(defn- project-needed?
  [task]
  (some #(= 'project (first %)) (arglists task)))

(defn multi
  "Run a task against multiple dependency sets as specified by :multi-deps in
  project.clj."
  [project task & args]
  (cond
   (not (project-needed? task)) (do
                                  (println (str "lein multi has no effect for task \""
                                                task "\" - running task as normal"))
                                  (apply (resolve-task task) args))
   (= task "deps") (apply run-deps project args)
   :else (apply run-task task project args)))