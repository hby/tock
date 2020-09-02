(ns com.inferstructure.tock.impl-test
  (:require [clojure.test :refer :all]
            [com.inferstructure.tock.impl :refer :all]))

(deftest single-seq-digit-test
  (testing "a single digit as seq"
    (let [d10 (->Digit (constantly (range 10)) nil)]
      (is (= [0]
             (-> d10 start value)))
      (is (= [8]
             (-> d10 (start [8]) value)))
      (is (= [9]
             (-> d10 (start [8]) tick value)))
      (is (= []
             (-> d10 (start [8]) tick tick value))))))

(deftest single-cycle-digit-test
  (testing "a single digit as seq"
    (let [d10 (->Digit (constantly (cycle (range 10))) nil)]
      (is (= [0]
             (-> d10 start value)))
      (is (= [8]
             (-> d10 (start [8]) value)))
      (is (= [9]
             (-> d10 (start [8]) tick value)))
      (is (= [0]
             (-> d10 (start [8]) tick tick value))))))

(deftest double-seq-digit-counter-test
  (testing "a single digit as seq"
    (let [d10 (->Digit (constantly (range 10)) nil)
          c2d10 (->Counter d10 (->Counter d10 nil))]
      (is (= [0 0]
             (-> c2d10 start value)))
      (is (= [5 8]
             (-> c2d10 (start [5 8]) value)))
      (is (= [5 9]
             (-> c2d10 (start [5 8]) tick value)))
      (is (= [6 0]
             (-> c2d10 (start [5 8]) tick tick value)))
      (is (= [0 1]
             (-> c2d10 (start [5 8]) tick tick start tick value))))))

