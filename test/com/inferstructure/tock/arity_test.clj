(ns com.inferstructure.tock.arity-test
  (:require [clojure.test :refer :all]
            [com.inferstructure.tock.arity :refer :all])
  (:import [clojure.lang ArityException ExceptionInfo]))

(defn arity2-fn
  ([a b] [:2 a b]))

(deftest arity2-fn-test
  (testing "arity2-fn"
    (is (thrown-with-msg? ExceptionInfo
                          #"Function does not have a supporting arit."
                          (apply-last-max-arity arity2-fn [1])))
    (is (= (apply-last-max-arity arity2-fn [1 2])
           [:2 1 2]))
    (is (= (apply-last-max-arity arity2-fn [1 2 3])
           [:2 2 3]))))

(defn arity2+4-fn
  ([] [:0])
  ([a b] [:2 a b])
  ([a b c d] [:4 a b c d]))

(deftest arity2+4-fn-test
  (testing "arity2+4-fn"
    (are [a t]
      (= (apply-last-max-arity arity2+4-fn a) t)
      []            [:0]
      [1]           [:0]
      [1 2]         [:2 1 2]
      [1 2 3]       [:2 2 3]
      [1 2 3 4]     [:4 1 2 3 4]
      [1 2 3 4 5]   [:4 2 3 4 5]
      [1 2 3 4 5 6] [:4 3 4 5 6])))

(defn arity2+4+v-fn
  ([] [:0])
  ([a b] [:2 a b])
  ([a b c d] [:4 a b c d])
  ([a b c d & r] (into [:v a b c d] r)))

(deftest arity2+4+v-fn-test
  (testing "arity2+4+v-fn"
    (are [a t]
      (= (apply-last-max-arity arity2+4+v-fn a) t)
      []            [:0]
      [1]           [:0]
      [1 2]         [:2 1 2]
      [1 2 3]       [:2 2 3]
      [1 2 3 4]     [:4 1 2 3 4]
      [1 2 3 4 5]   [:v 1 2 3 4 5]
      [1 2 3 4 5 6] [:v 1 2 3 4 5 6])))

(defn arityv-fn
  ([& r] (into [:v] r)))

(deftest arityv-fn-test
  (testing "arityv-fn"
    (are [a t]
      (= (apply-last-max-arity arityv-fn a) t)
      []      [:v]
      [1]     [:v 1]
      [1 2]   [:v 1 2]
      [1 2 3] [:v 1 2 3])))

(defn arity1+gap+v-fn
  ([a] [:1 a])
  ([a b c & r] (into [:v a b c] r)))

(deftest arity1+gap+v-fn-test
  (testing "arity1+gap+v-fn"
    (are [a t]
      (= (apply-last-max-arity arity1+gap+v-fn a) t)
      [1]       [:1 1]
      [1 2 3]   [:v 1 2 3]
      [1 2 3 4] [:v 1 2 3 4])))

(deftest arity1+gap+v-fn-ex-test
  (testing "arity1+gap+v-fn-ex"
    (is (thrown-with-msg? ArityException
                          #"Wrong number of args \(2\)"
                          (apply-last-max-arity arity1+gap+v-fn [1 2])))))
