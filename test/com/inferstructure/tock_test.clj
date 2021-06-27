(ns com.inferstructure.tock-test
  (:require [clojure.test :refer :all]
            [com.inferstructure.tock :as tk]))

(def bit (tk/digit ::tk/digit-seq [0 1]))
(def two-bit (tk/counter [bit bit]))
(def cycle-bit (tk/digit ::tk/digit-seq (cycle [0 1])))
(def four-bit-linear (tk/counter [bit bit bit bit]))
(def four-bit-tree (tk/counter [two-bit two-bit]))

(deftest t2bit-finite-test
  (testing "two-bit finite counter"
    (is (= [[0 0] [0 1] [1 0] [1 1]]
           (-> two-bit
               (tk/start)
               (tk/value-seq))))))

(deftest t2bit-finite-start-test
  (testing "two-bit finite counter, start value"
    (is (= [[1 0] [1 1]]
           (-> (tk/counter [bit bit])
               (tk/start [1 0])
               (tk/value-seq))))))

(deftest t2bit-infinite-test
  (testing "two-bit infinite counter"
    (is (= [[0 0] [0 1] [1 0] [1 1] [0 0] [0 1]]
           (->> (-> (tk/counter [cycle-bit bit])
                    (tk/start)
                    (tk/value-seq))
                (take 6))))))

(deftest t2bit-force-infinite-test
  (testing "two-bit force infinite counter"
    (is (= [[0 0] [0 1] [1 0] [1 1] [0 0] [0 1] [1 0] [1 1] [0 0]]
           (->> (-> (tk/counter [bit bit])
                    (tk/start)
                    (tk/value-seq [:force]))
                (take 9))))))

(deftest t2bit-infinite-start-test
  (testing "two-bit infinite counter, start value"
    (is (= [[1 0] [1 1] [0 0] [0 1] [1 0] [1 1]]
           (->> (-> (tk/counter [cycle-bit bit])
                    (tk/start [1 0])
                    (tk/value-seq))
                (take 6))))))

(deftest t4bit-finite-linear-test
  (testing "four-bit finite counter, tree composition"
    (is (= [[0 0 0 0] [0 0 0 1] [0 0 1 0] [0 0 1 1]
            [0 1 0 0] [0 1 0 1] [0 1 1 0] [0 1 1 1]
            [1 0 0 0] [1 0 0 1] [1 0 1 0] [1 0 1 1]
            [1 1 0 0] [1 1 0 1] [1 1 1 0] [1 1 1 1]]
           (-> four-bit-linear
               (tk/start)
               (tk/value-seq))))))

(deftest t4bit-finite-liner-start-test
  (testing "four-bit finite counter, tree composition, start value"
    (is (= [[0 1 0 1] [0 1 1 0] [0 1 1 1]
            [1 0 0 0] [1 0 0 1] [1 0 1 0] [1 0 1 1]
            [1 1 0 0] [1 1 0 1] [1 1 1 0] [1 1 1 1]]
           (-> four-bit-linear
               (tk/start [0 1 0 1])
               (tk/value-seq))))))

(deftest t4bit-finite-tree-test
  (testing "four-bit finite counter, tree composition"
    (is (= [[0 0 0 0] [0 0 0 1] [0 0 1 0] [0 0 1 1]
            [0 1 0 0] [0 1 0 1] [0 1 1 0] [0 1 1 1]
            [1 0 0 0] [1 0 0 1] [1 0 1 0] [1 0 1 1]
            [1 1 0 0] [1 1 0 1] [1 1 1 0] [1 1 1 1]]
           (-> four-bit-tree
               (tk/start)
               (tk/value-seq))))))

(deftest t4bit-finite-tree-start-test
  (testing "four-bit finite counter, tree composition, start value"
    (is (= [[0 1 0 1] [0 1 1 0] [0 1 1 1]
            [1 0 0 0] [1 0 0 1] [1 0 1 0] [1 0 1 1]
            [1 1 0 0] [1 1 0 1] [1 1 1 0] [1 1 1 1]]
           (-> four-bit-tree
               (tk/start [0 1 0 1])
               (tk/value-seq))))))

(deftest t4bit-finite-linear-equal-tree-test
  (testing "linear equals tree composition"
    (are [c1 c2 v]
      (= v
         (= (-> c1
                (tk/start)
                (tk/value-seq))
            (-> c2
                (tk/start)
                (tk/value-seq))))
      four-bit-linear four-bit-tree true
      four-bit-linear two-bit false)))

(deftest multi-value-digit-test
  (testing "multi-value-digit-test"
    (is (= [[[0 0] 0] [[0 0] 1] [[0 1] 0] [[0 1] 1]
            [[1 0] 0] [[1 0] 1] [[1 1] 0] [[1 1] 1]]
           (-> (tk/counter [(tk/digit ::tk/digit-seq [[0 0] [0 1] [1 0] [1 1]])
                            (tk/digit ::tk/digit-seq [0 1])])
               (tk/start)
               (tk/value-seq))))))

(deftest multi-value-digit-start-test
  (testing "multi-value-digit-test"
    (is (= [[[1 0] 1] [[1 1] 0] [[1 1] 1]]
           (-> (tk/counter [(tk/digit ::tk/digit-seq [[0 0] [0 1] [1 0] [1 1]])
                            (tk/digit ::tk/digit-seq [0 1])])
               (tk/start [[1 0] 1])
               (tk/value-seq))))))


(deftest stair-counter-test
  (testing "stair counter"
    (let [dx (tk/digit ::tk/digit-fn (fn [x] (range (inc (or x 0)))))]
      (is (= [[0 0 0]
              [1 0 0] [1 1 0] [1 1 1]
              [2 0 0] [2 1 0] [2 1 1] [2 2 0] [2 2 1] [2 2 2]]
             (-> (tk/counter [(tk/digit ::tk/digit-seq [0 1 2]) dx dx])
                 (tk/start)
                 (tk/value-seq)))))))

(deftest conditional-counter-test
  (testing "conditional counter"
    (is (= [[:ted :dust] [:ted :garbage]
            [:alice :dishes] [:alice :water]]
           (-> (tk/counter [(tk/digit ::tk/digit-seq [:ted :alice])
                            (tk/digit ::tk/digit-kvs {[:ted] [:dust :garbage]
                                                      [:alice] [:dishes :water]})])
               (tk/start)
               (tk/value-seq))))))

(defn leap?
  [y]
  (if (zero? (mod y 4))
    (if (zero? (mod y 100))
      (if (zero? (mod y 400))
        true
        false)
      true)
    false))

(def y-m-d (tk/counter [(tk/digit ::tk/digit-placeholder)
                        (tk/digit ::tk/digit-seq (range 1 13))
                        (tk/digit ::tk/digit-fn (fn [y m] (case m
                                                                (1 3 5 7 8 10 12) (range 1 32)
                                                                2 (range 1 (if (leap? y) 30 29))
                                                                (4 6 9 11) (range 1 31))))]))

(defn year-digit
  [sv]
  (tk/digit-fn ::tk/digit-seq (iterate inc sv)))

(deftest y-m-d-test
  (testing "y-m-d"
    (are [s d t v]
      (= v
         (->> (-> y-m-d
                  (tk/start s)
                  (tk/value-seq))
              (drop d)
              (take t)))
      [(year-digit 2020) 1 1] 30 2 [[2020 1 31] [2020 2 1]]
      [(year-digit 2020) 2 1] 28 2 [[2020 2 29] [2020 3 1]]
      [(year-digit 2020) 1 1] 366 1 [[2021 1 1]]
      [(year-digit 2100) 2 1] 27 2 [[2100 2 28] [2100 3 1]]
      [(year-digit 2100) 1 1] 365 1 [[2101 1 1]])))

(def y-m-d-dow (tk/counter [y-m-d (tk/digit ::tk/digit-placeholder)]))

(defn dow-digit
  [start-dow]
  (tk/digit-fn ::tk/digit-take-seq
               (drop-while
                 #(not= start-dow %)
                 (cycle [:sunday :monday :tuesday :wednesday :thursday :friday :saturday]))))

(deftest y-m-d-dow-test
  (testing "y-m-d-dow"
    (are [s d t v]
      (= v
         (->> (-> y-m-d-dow
                  (tk/start s)
                  (tk/value-seq))
              (drop d)
              (take t)))
      [(year-digit 2020) 1 1 (dow-digit :wednesday)] 30 2 [[2020 1 31 :friday] [2020 2 1 :saturday]]
      [(year-digit 2020) 2 1 (dow-digit :saturday)] 28 2 [[2020 2 29 :saturday] [2020 3 1 :sunday]]
      [(year-digit 2020) 1 1 (dow-digit :wednesday)] 366 1 [[2021 1 1 :friday]]
      [(year-digit 2100) 2 1 (dow-digit :monday)] 27 2 [[2100 2 28 :sunday] [2100 3 1 :monday]]
      [(year-digit 2100) 1 1 (dow-digit :friday)] 365 1 [[2101 1 1 :saturday]])))

(def fizz-buzz (tk/counter [(tk/digit ::tk/digit-seq (iterate inc 1))
                            (tk/digit ::tk/digit-take-seq (cycle ["" "" "Fizz"]))
                            (tk/digit ::tk/digit-take-seq (cycle ["" "" "" "" "Buzz"]))
                            (tk/digit ::tk/digit-fn (fn [n f b] (let [fb (str f b)
                                                                      v (if (empty? fb) n fb)]
                                                                  [v])))]))

(deftest fizz-buzz-test
  (testing "fizzbuzz"
    (is (= [1 2 "Fizz" 4 "Buzz" "Fizz" 7 8 "Fizz" "Buzz" 11 "Fizz" 13 14 "FizzBuzz"]
           (->> (-> fizz-buzz
                    (tk/start)
                    (tk/value-seq))
                (map (fn [[_ _ _ fb]] fb))
                (take 15))))))

(def with-placeholder (tk/counter [(tk/digit ::tk/digit-placeholder)]))
(def without-placeholder (tk/counter [(tk/digit ::tk/digit-seq [1 3 5])]))

(deftest unfilled-placeholder-test
  (testing "unfilled-counter-placeholder"
    (is (thrown-with-msg? Exception #"Unfilled placeholder"
                          (-> with-placeholder
                              (tk/start)))))
  (testing "unfilled-start-placeholder"
    (is (thrown-with-msg? Exception #"Unfilled placeholder"
                          (-> without-placeholder
                              (tk/start [(tk/digit-fn ::tk/digit-placeholder)]))))))

(deftest middle-placeholder-start-test
  (testing "middle-placeholder-start-test"
    (is (= [[1 999 :a] [1 999 :b]
            [2 999 :a] [2 999 :b]]
           (-> (tk/counter [(tk/digit ::tk/digit-seq [1 2])
                            (tk/digit ::tk/digit-placeholder)
                            (tk/digit ::tk/digit-seq [:a :b])])
               (tk/start [::tk/sv*
                          (tk/digit-fn ::tk/digit-seq [999])
                          ::tk/sv*])
               (tk/value-seq))))))

(deftest no-placeholder-sub-digit-fn-on-start-test
  (testing "no-placeholder-sub-digit-fn-on-start-test"
    (is (= [[:z 100 :a] [:z 100 :b] [:z 200 :a] [:z 200 :b]]
           (-> (tk/counter [(tk/digit ::tk/digit-seq [:x :y])
                            (tk/digit ::tk/digit-seq [1 2])
                            (tk/digit ::tk/digit-seq [:a :b])])
               (tk/start [(tk/digit-fn ::tk/digit-seq [:z])
                          (tk/digit-fn ::tk/digit-seq [100 200])
                          ::tk/sv*])
               (tk/value-seq))))))

(deftest empty-digit-counter-test
  (testing "empty-digit-counter"
    (is (= [[1] [2]]
           (->> (-> (tk/counter [(tk/digit ::tk/digit-seq [1 2])
                                 (tk/digit ::tk/digit-seq [])])
                    (tk/start)
                    (tk/value-seq))
                (take 3))))))

(deftest take-2-counter-test
  (testing "empty-digit-counter"
    (is (= [[1 :a] [1 :b] [2 :c] [2 :d] [3] [4]]
           (->> (-> (tk/counter [(tk/digit ::tk/digit-seq [1 2 3 4])
                                 (tk/digit ::tk/digit-take-seq [:a :b :c :d] 2)])
                    (tk/start)
                    (tk/value-seq))
                (take 6))))))
