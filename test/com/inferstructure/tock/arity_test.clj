(ns com.inferstructure.tock.arity-test
  (:require [clojure.test :refer :all]
            [com.inferstructure.tock.arity :refer :all])
  (:import [clojure.lang ArityException ExceptionInfo]))

(defn df2
  ([a b] [:2 a b]))

(deftest tf2-test
  (testing "df2"
    (is (thrown-with-msg? ExceptionInfo
                          #"Function does not have a supporting arit."
                          (apply-last-max-arity df2 [1])))
    (is (= (apply-last-max-arity df2 [1 2])
           [:2 1 2]))
    (is (= (apply-last-max-arity df2 [1 2 3])
           [:2 2 3]))))

(defn df2+4
  ([] [:0])
  ([a b] [:2 a b])
  ([a b c d] [:4 a b c d]))

(deftest tf2+4-test
  (testing "df2+4"
    (are [a t]
      (= (apply-last-max-arity df2+4 a) t)
      []            [:0]
      [1]           [:0]
      [1 2]         [:2 1 2]
      [1 2 3]       [:2 2 3]
      [1 2 3 4]     [:4 1 2 3 4]
      [1 2 3 4 5]   [:4 2 3 4 5]
      [1 2 3 4 5 6] [:4 3 4 5 6])))

(defn df2+4+v
  ([] [:0])
  ([a b] [:2 a b])
  ([a b c d] [:4 a b c d])
  ([a b c d & r] (into [:v a b c d] r)))

(deftest tf2+4+v-test
  (testing "df2+4+v"
    (are [a t]
      (= (apply-last-max-arity df2+4+v a) t)
      []            [:0]
      [1]           [:0]
      [1 2]         [:2 1 2]
      [1 2 3]       [:2 2 3]
      [1 2 3 4]     [:4 1 2 3 4]
      [1 2 3 4 5]   [:v 1 2 3 4 5]
      [1 2 3 4 5 6] [:v 1 2 3 4 5 6])))

(defn dfv
  ([& r] (into [:v] r)))

(deftest tfv-test
  (testing "dfv"
    (are [a t]
      (= (apply-last-max-arity dfv a) t)
      []      [:v]
      [1]     [:v 1]
      [1 2]   [:v 1 2]
      [1 2 3] [:v 1 2 3])))

(defn dfgapv
  ([a] [:1 a])
  ([a b c & r] (into [:v a b c] r)))

(deftest tfgapv-test
  (testing "dfgapv"
    (are [a t]
      (= (apply-last-max-arity dfgapv a) t)
      [1]       [:1 1]
      [1 2 3]   [:v 1 2 3]
      [1 2 3 4] [:v 1 2 3 4])))

(deftest tfgapv-ex-test
  (testing "dfgapv"
    (is (thrown-with-msg? ArityException
                          #"Wrong number of args \(2\)"
                          (apply-last-max-arity dfgapv [1 2])))))
