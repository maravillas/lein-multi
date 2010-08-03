(ns sample
  (:use [clojure.test]))

(deftest passing-test
  (is true))

(deftest failing-test-1.1.0
  (is (not= *clojure-version*
	    {:major 1 :minor 1 :incremental 0 :qualifier ""})))