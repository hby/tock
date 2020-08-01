(ns com.inferstructure.tock
  (:require [com.inferstructure.tock.impl :as im]))

;;
;; Create Digits
;;

(defmulti digit
          "Returns a Digit with a proper digit function given
          some digit spec data."
          (fn [type _digit-spec] type))

;;
;; Some builtin digit constructors
;;

(defmethod digit :tock-builtin/fn
  [_ f]
  (im/->Digit f nil))

(defmethod digit :tock-builtin/seq
  [_ s]
  (im/->Digit (constantly s) nil))

(defn- suffix
  [v sv]
  (if (<= (count sv) (count v))
    (reduce #(and % %2) (map = (reverse v) (reverse sv)))
    false))

(defmethod digit :tock-builtin/kvs
  [_ kvs]
  (im/->Digit (fn [& vs]
                (let [[_ v] (first (filter #(suffix vs (first %)) kvs))]
                  (if v v ())))
              nil))

;;
;; Create Counters
;;

(defn counter [ctrs]
  (reduce (fn [cs c]
            (im/->Counter c cs))
          nil
          ctrs))

;;
;; Sequences
;;

(defn counter-seq
  [ctr]
  (lazy-seq
    (cons ctr (counter-seq (im/tick ctr)))))

(defn value-seq
  [ctr]
  (->> ctr
       counter-seq
       (map im/value)
       (take-while #(not-empty %))))

;;
;; Digit and Counter API
;;

(defn start
  ([ic] (im/start ic))
  ([ic sv] (im/start ic sv))
  ([ic sv hovs] (im/start ic sv hovs)))

(defn tick
  ([ic] (im/tick ic))
  ([ic hovs] (im/tick ic hovs)))

(defn value
  [ic]
  (im/value ic))
