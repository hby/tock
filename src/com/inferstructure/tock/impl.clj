(ns com.inferstructure.tock.impl
  (:require [com.inferstructure.tock.arity :as ar]
            [com.inferstructure.tock.print :as p])
  (:import [java.io Writer]))


;; Legend:
;;  c, ctr  - counter
;;  d       - digit
;;  hoc     - high(er) order counter
;;  hovs    - high(er) order values
;;  sv      - start value
;;  df      - digit function
;;  dseq    - digit sequence
;;  ndig    - number of digits


(defprotocol ICounter
  "A Digit is an ICounter.
  A Counter is an ICounter composed of Digits and Counters."
  (value [this]
    "Returns a list of the digit value or all counter values")
  (start [this] [this sv] [this sv hovs]
    "Starts the counter with possible start value and possible higher order digits")
  (tick [this] [this hovs]
    "Returns an ICounter advanced one tick."))


(defrecord Digit [dfn dseq]
  ICounter
  (value [d]
    (take 1 (:dseq d)))

  (start [d] (start d nil []))
  (start [d sv] (start d sv []))
  (start [d sv hovs]
    (let [s (ar/apply-last-max-arity (:dfn d) hovs)]
      (if sv
        (do (assert (and (coll? sv) (= 1 (count sv))) "Wrong number of digits in start value")
            (assoc d :dseq (drop-while #(not= (first sv) %) s)))
        (assoc d :dseq s))))

  (tick [d] (tick d []))
  (tick [d hovs]
    (if (seq (:dseq d))
      (let [r (rest (:dseq d))]
        (if (seq r)
          (assoc d :dseq r)
          (if (empty? hovs)
            (assoc d :dseq r)
            (start d nil hovs))))
      (if (empty? hovs)
        d
        (start d nil hovs)))))

(defmethod print-method Digit
  [v ^Writer w]
  (.write w (str "Digit@" (p/seq->str (:dseq v)))))


(declare num-digits)

(defrecord Counter [ctr hoc]
  ICounter
  (value [c]
    (if (:hoc c)
      (concat (value (:hoc c)) (value (:ctr c)))
      (value (:ctr c))))

  (start [c] (start c nil []))
  (start [c sv] (start c sv []))
  (start [c sv hovs]
    (if (:hoc c)
      (let [ndig-hoc (num-digits (:hoc c))
            ndig-ctr (num-digits (:ctr c))
            _ (when sv
                (assert (= (count sv) (+ ndig-hoc ndig-ctr))
                        "Wrong number of digits in start value"))
            [ho-sv ctr-sv] (when sv (split-at ndig-hoc sv))
            new-hoc (start (:hoc c)
                           ho-sv
                           hovs)
            new-ctr (start (:ctr c)
                           ctr-sv
                           (concat hovs (value new-hoc)))]
        (assoc c :hoc new-hoc :ctr new-ctr))
      (assoc c :ctr (start (:ctr c)
                           sv
                           hovs))))

  (tick [c] (tick c []))
  (tick [c hovs]
    (if (empty? (value (:ctr c)))
      (if (empty? hovs)
        c
        (start c nil hovs))
      (let [ticked-ctr (tick (:ctr c))
            ticked-ctr-value (value ticked-ctr)]
        (if (empty? ticked-ctr-value)
          (if (:hoc c)
            (let [ticked-hoc (tick (:hoc c) hovs)
                  ticked-hoc-value (value ticked-hoc)]
              (if (empty? ticked-hoc-value)
                (assoc c :ctr ticked-ctr
                         :hoc ticked-hoc)
                (assoc c :ctr (start (:ctr c) nil (concat hovs ticked-hoc-value))
                         :hoc ticked-hoc)))
            (if (empty? hovs)
              (assoc c :ctr ticked-ctr)
              (start (:ctr c) nil hovs)))
          (assoc c :ctr ticked-ctr))))))

;; TODO - raw; needs work
(defmethod print-method Counter
  [v ^Writer w]
  (when (:hoc v)
    (print-method (:hoc v) w)
    (.write w ":"))
  (print-method (:ctr v) w))

(defmulti num-digits class)
(defmethod num-digits nil [_] 0)
(defmethod num-digits Digit [_] 1)
(defmethod num-digits Counter [c]
  (+ (num-digits (:ctr c))
     (num-digits (:hoc c))))

(comment

  ;;
  ;; future?
  ;;

  ; cycle in day of week along with y-m-d
  ;   -> cycling without carrying ??

  #_ [])
