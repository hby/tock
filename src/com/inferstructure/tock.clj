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
  ;; a literal digit function
  [_ f]
  (im/->Digit f nil))

(defmethod digit :tock-builtin/seq
  ;; digit function is constructed that always returns the given sequence
  [_ s]
  (im/->Digit (constantly s) nil))

(defn- suffix
  "Returns true if sv is a suffix of v."
  [v sv]
  (if (<= (count sv) (count v))
    (reduce #(and % %2) (map = (reverse v) (reverse sv)))
    false))

(defmethod digit :tock-builtin/kvs
  ;;
  [_ kvs]
  (im/->Digit (fn [& vs]
                (let [[_ v] (first (filter #(suffix vs (first %)) kvs))]
                  (if v v ())))
              nil))

;;
;; Create Counters
;;

(defn counter
  "Create and return a Counter from a sequence of Counters or Digits."
  [ctrs]
  (reduce (fn [cs c]
            (im/->Counter c cs))
          nil
          ctrs))

;;
;; Digit and Counter API
;;

(defn start
  "Returns a started counter or digit.
  Optionally takes a starting value and sequence of higher order values."
  ([ic] (im/start ic))
  ([ic sv] (im/start ic sv))
  ([ic sv hovs] (im/start ic sv hovs)))

(defn tick
  "Returns a given counter advanced one 'count'.
  Optionally takes a sequence of higher order values."
  ([ic] (im/tick ic))
  ([ic hovs] (im/tick ic hovs)))

(defn value
  "Returns the value of the given counter."
  [ic]
  (im/value ic))

;;
;; Sequences
;;

(defn counter-seq
  "Return a lazy sequence of Counters ctr, (tick ctr), (tick (tick ctr)), ...
  This will always be an infinite sequence since once (value ctr) is empty
  then (tick ctr) will continue to produce an empty valued counter."
  [ctr]
  (lazy-seq
    (cons ctr (counter-seq (im/tick ctr)))))

(defn value-seq
  "Return a lazy sequence of counters values
   (value ctr), (value (tick ctr)), (value (tick (tick ctr))), ...
   The sequence ends when the underlying counter sequence produces
   an empty value (that is, the counter runs out)"
  [ctr]
  (->> ctr
       counter-seq
       (map im/value)
       (take-while #(not-empty %))))
