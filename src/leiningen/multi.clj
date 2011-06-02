(ns leiningen.multi
  (:use [leiningen.deps :only [deps]]
        [leiningen.core :only [resolve-task arglists]]
        [clojure.string :only [replace-first]]
        [clojure.contrib.logging :only [spy]])
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
  (if (= name "base")
    project
    (merge project {:library-path (str (multi-library-path project) "/" name)
                    :dependencies deps})))

(defn- print-set-message
  [task name deps]
  (println (str "\nRunning \"lein " task "\" on dependencies set " name ": " deps)))

(defn- run-task-on-set
  [task task-fn args [name project]]
  (print-set-message task name (:dependencies project))
  (apply task-fn project args))

(defn- create-projects
  [project dep-sets]
  (into {} (map (fn [[name deps]] {name (project-for-set project name deps)})
                dep-sets)))

(defn- combine-results
  [results]
  (if (every? number? results)
    (if (every? zero? results) 0 1)
    results))

(defn- run-task
  [task project dep-sets args]
  (let [projects (create-projects project dep-sets)
        task-fn (resolve-task task)
        results (doall (map #(run-task-on-set task task-fn args %) projects))]
    (combine-results results)))

;; Handle the deps task individually, as we want to pass the "skip-dev" param
;; to the base call, but pass true for the multi calls.
(defn- run-deps-task
  [project dep-sets & args]
  (let [projects (create-projects project dep-sets)
        task-fn (resolve-task "deps")
        arg-fn (fn [args [name _]] (if (= name "base") args [true]))
        results (doall (map #(run-task-on-set "deps" task-fn (arg-fn args %) %) projects))]
    (combine-results results)))

(defn- project-needed?
  [task]
  (some #(= 'project (first %)) (arglists task)))

(defn- option-map
  [args]
  (->> args
       (partition 2)
       (take-while #(.startsWith (first %) "--"))
       (map (fn [[flag value]] [(keyword (replace-first flag "--" "")) value]))
       (into {})))

(defn- without-options
  [args]
  (->> args
       (partition-all 2)
       (drop-while #(.startsWith (first %) "--"))
       (flatten)))

(defn- collect-sets
  [project options]
  (if (contains? options :with)
    (when-let [set ((:multi-deps project) (:with options))]
      {(:with options) set})
    (apply array-map
           "base" (:dependencies project)
           (mapcat identity (:multi-deps project)))))

(defn multi
  "Run a task against multiple dependency sets as specified by :multi-deps in
project.clj."
  [project task & args]
  (when (not (:multi-deps project))
    (println "Warning: No :multi-deps found in project.clj."))
  (let [options (option-map args)
        args (without-options args)
        dep-set (collect-sets project options)
        set-missing? (and (contains? options :with)
                          (nil? dep-set))]
    (cond
     set-missing? (do
                    (println (str "Error: No dependency set named \"" (:with options) "\" found in project.clj."))
                    1)
     (not (project-needed? task)) (do
                                    (println (str "lein multi has no effect for task \""
                                                  task "\" - running task as normal"))
                                    (apply (resolve-task task) args))
     (= task "deps") (apply run-deps-task project dep-set args)
     :else (run-task task project dep-set args))))

