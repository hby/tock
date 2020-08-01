(ns com.inferstructure.tock-test
  (:require [clojure.test :refer :all]
            [com.inferstructure.tock :refer :all]))

(def bit (digit :tock-builtin/seq [0 1]))
(def two-bit (counter [bit bit]))
(def cycle-bit (digit :tock-builtin/seq (cycle [0 1])))
(def four-bit-linear (counter [bit bit bit bit]))
(def four-bit-tree (counter [two-bit two-bit]))

(deftest t2bit-finite-test
  (testing "two-bit finite counter"
    (is (= [[0 0] [0 1] [1 0] [1 1]]
           (-> two-bit
               (start)
               (value-seq))))))

(deftest t2bit-finite-start-test
  (testing "two-bit finite counter, start value"
    (is (= [[1 0] [1 1]]
           (-> (counter [bit bit])
               (start [1 0])
               (value-seq))))))

(deftest t2bit-infinite-test
  (testing "two-bit infinite counter"
    (is (= [[0 0] [0 1] [1 0] [1 1] [0 0] [0 1]]
           (->> (-> (counter [cycle-bit bit])
                    (start)
                    (value-seq))
                (take 6))))))

(deftest t2bit-infinite-start-test
  (testing "two-bit infinite counter, start value"
    (is (= [[1 0] [1 1] [0 0] [0 1] [1 0] [1 1]]
           (->> (-> (counter [cycle-bit bit])
                    (start [1 0])
                    (value-seq))
                (take 6))))))

(deftest t4bit-finite-linear-test
  (testing "four-bit finite counter, tree composition"
    (is (= [[0 0 0 0] [0 0 0 1] [0 0 1 0] [0 0 1 1]
            [0 1 0 0] [0 1 0 1] [0 1 1 0] [0 1 1 1]
            [1 0 0 0] [1 0 0 1] [1 0 1 0] [1 0 1 1]
            [1 1 0 0] [1 1 0 1] [1 1 1 0] [1 1 1 1]]
           (-> four-bit-linear
               (start)
               (value-seq))))))

(deftest t4bit-finite-liner-start-test
  (testing "four-bit finite counter, tree composition, start value"
    (is (= [[0 1 0 1] [0 1 1 0] [0 1 1 1]
            [1 0 0 0] [1 0 0 1] [1 0 1 0] [1 0 1 1]
            [1 1 0 0] [1 1 0 1] [1 1 1 0] [1 1 1 1]]
           (-> four-bit-linear
               (start [0 1 0 1])
               (value-seq))))))

(deftest t4bit-finite-tree-test
  (testing "four-bit finite counter, tree composition"
    (is (= [[0 0 0 0] [0 0 0 1] [0 0 1 0] [0 0 1 1]
            [0 1 0 0] [0 1 0 1] [0 1 1 0] [0 1 1 1]
            [1 0 0 0] [1 0 0 1] [1 0 1 0] [1 0 1 1]
            [1 1 0 0] [1 1 0 1] [1 1 1 0] [1 1 1 1]]
           (-> four-bit-tree
               (start)
               (value-seq))))))

(deftest t4bit-finite-tree-start-test
  (testing "four-bit finite counter, tree composition, start value"
    (is (= [[0 1 0 1] [0 1 1 0] [0 1 1 1]
            [1 0 0 0] [1 0 0 1] [1 0 1 0] [1 0 1 1]
            [1 1 0 0] [1 1 0 1] [1 1 1 0] [1 1 1 1]]
           (-> four-bit-tree
               (start [0 1 0 1])
               (value-seq))))))

(deftest t4bit-finite-linear-equal-tree-test
  (testing "linear equals tree composition"
    (are [c1 c2 v]
      (= v
         (= (-> c1
                (start)
                (value-seq))
            (-> c2
                (start)
                (value-seq))))
      four-bit-linear four-bit-tree true
      four-bit-linear two-bit false)))

(deftest stair-counter-test
  (testing "stair counter"
    (let [dx (digit :tock-builtin/fn (fn [x] (range (inc (or x 0)))))]
      (is (= [[0 0 0]
              [1 0 0] [1 1 0] [1 1 1]
              [2 0 0] [2 1 0] [2 1 1] [2 2 0] [2 2 1] [2 2 2]]
             (-> (counter [(digit :tock-builtin/seq [0 1 2]) dx dx])
                 (start)
                 (value-seq)))))))

(deftest conditional-counter-test
  (testing "conditional counter"
    (is (= [[:ted :dust] [:ted :garbage]
            [:alice :dishes] [:alice :water]]
           (-> (counter [(digit :tock-builtin/seq [:ted :alice])
                         (digit :tock-builtin/kvs {[:ted] [:dust :garbage]
                                                   [:alice] [:dishes :water]})])
               (start)
               (value-seq))))))

(defn leap?
  [y]
  (if (zero? (mod y 4))
    (if (zero? (mod y 100))
      (if (zero? (mod y 400))
        true
        false)
      true)
    false))

(def ymd (counter [(digit :tock-builtin/fn (fn [] (iterate inc 2020)))
                   (digit :tock-builtin/fn (fn [] (range 1 13)))
                   (digit :tock-builtin/fn (fn [y m]
                                             (case m
                                               (1 3 5 7 8 10 12) (range 1 32)
                                               2 (range 1 (if (leap? y) 30 29))
                                               (4 6 9 11) (range 1 31))))]))

(deftest ymd-test
  (testing "ymd"
    (are [s d t v]
      (= v
         (->> (-> ymd
                  (start s)
                  (value-seq))
              (drop d)
              (take t)))
      [2020 1 1] 30 2 [[2020 1 31] [2020 2 1]]
      [2020 2 1] 28 2 [[2020 2 29] [2020 3 1]]
      [2020 1 1] 366 1 [[2021 1 1]]
      [2100 2 1] 27 2 [[2100 2 28] [2100 3 1]]
      [2100 1 1] 365 1 [[2101 1 1]])))
