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
    "Returns a list of all of the digit values.")
  (start [this] [this sv] [this sv hovs]
    "Starts the counter with possible start value and possible higher order digits.
    'Starting' is what establishes a sequence for each Digit based on starting values
    and higher order digits.")
  (tick [this] [this hovs]
    "Returns an ICounter advanced one tick. This can cause a call to 'start' when a
    digit sequence runs out of values."))

(defn extract-start-value
  "Return [digit-fn start-value]"
  [sv]
  (let [raw-sv (first sv)]
    (cond
      (fn? raw-sv)
      [raw-sv nil]

      (and (vector? raw-sv) (fn? (first raw-sv)))
      raw-sv

      :else
      [nil raw-sv])))

(defn placeholder?
  [f]
  (get (meta f) ::digit-placeholder false))

(defrecord Digit [dfn dseq]
  ICounter
  (value [_]
    (take 1 dseq))

  (start [d] (start d nil []))
  (start [d sv] (start d sv []))
  (start [d sv hovs]
    (if (and sv (not= sv [:com.inferstructure.tock/sv*]))
      (do
        (assert (and (coll? sv) (= 1 (count sv))) "Wrong number of digits in start value")
        (let [[sv-fn sv-v] (extract-start-value sv)
              use-d (if sv-fn (assoc d :dfn sv-fn) d)
              _ (when (placeholder? (:dfn use-d)) (throw (IllegalStateException. "Unfilled placeholder")))]
          (if sv-v
            (assoc use-d :dseq (drop-while #(not= sv-v %)
                                           (ar/apply-last-max-arity (:dfn use-d) hovs)))
            (assoc use-d :dseq (ar/apply-last-max-arity (:dfn use-d) hovs)))))
      (do
        (when (placeholder? dfn) (throw (IllegalStateException. "Unfilled placeholder")))
        (assoc d :dseq (ar/apply-last-max-arity dfn hovs)))))

  (tick [d] (tick d []))
  (tick [d hovs]
    (if (seq dseq)
      ;; we are not already run out
      (let [r (rest dseq)]
        (if (seq r)
          ;; this tick does not run out of values,
          ;; just set new seq
          (assoc d :dseq r)
          ;; this tick runs out of values
          (if (empty? hovs)
            ;; without hovs, just set new (empty) seq
            (assoc d :dseq r)
            ;; with hovs, start the digit again
            (start d nil hovs))))
      ;; we are already run out
      (if (empty? hovs)
        ;; Without any hovs, this Digit has run out of values.
        d
        ;; Will be given hovs when there are higher order digits and they have
        ;; been asked to tick. Or, called directly with hovs in a REPL or test.
        (start d nil hovs)))))

(defmethod print-method Digit
  [^Digit d ^Writer w]
  (.write w (str "Digit@" (p/seq->str (.dseq d)))))


(declare num-digits)

(defrecord Counter [ctr hoc]
  ICounter
  (value [_]
    (if hoc
      (concat (value hoc) (value ctr))
      (value ctr)))

  (start [c] (start c nil []))
  (start [c sv] (start c sv []))
  (start [c sv hovs]
    (if hoc
      ;; with a higher order counter,
      ;;   need to start it
      ;;   and use its value a our hovs.
      (let [ndig-hoc (num-digits hoc)
            ndig-ctr (num-digits ctr)
            _ (when sv
                (assert (= (count sv) (+ ndig-hoc ndig-ctr))
                        "Wrong number of digits in start value"))
            ;; split digits to pass to high order counter vs. this counter
            [ho-sv ctr-sv] (when sv (split-at ndig-hoc sv))
            ;; start  high order counter
            new-hoc (start hoc
                           ho-sv
                           hovs)
            ;; use high order counter value to start this counter
            new-ctr (start ctr
                           ctr-sv
                           (concat hovs (value new-hoc)))]
        ;; put the pieces together
        (assoc c :hoc new-hoc :ctr new-ctr))
      ;; no higher order counter, just start our ctr
      (assoc c :ctr (start ctr
                           sv
                           hovs))))

  (tick [c] (tick c []))
  (tick [c hovs]
    (if (empty? (value ctr))
      ;; we have already run out of values
      (if hoc
        ;; with a high order counter,
        ;;   tick the high order counter and use its value to start this counter
        (let [ticked-hoc (tick hoc hovs)
              ticked-hoc-value (value ticked-hoc)]
          (if (empty? ticked-hoc-value)
            ;; if the high order counter has run out, assemble the pieces
            ;;   tick this counter (even though empty) so that correct behavior
            ;;   happens if we have hovs
            (assoc c :hoc ticked-hoc
                     :ctr (tick ctr hovs))
            ;; if the high order counter has a value,
            ;;   start this counter using the value of the high order counter
            ;;   and assemble the pieces
            (assoc c :ctr (start ctr nil (concat hovs ticked-hoc-value))
                     :hoc ticked-hoc)))
        ;; we have no high order counter
        (if (empty? hovs)
          ;; without hovs, we are done
          c
          ;; with hovs, we start again
          (start c nil hovs)))
      ;; we have not already run out of values,
      ;; look at this counter that has been ticked
      (let [ticked-ctr (tick ctr)
            ticked-ctr-value (value ticked-ctr)]
        (if (empty? ticked-ctr-value)
          ;; this ticked counter has run out
          (if hoc
            ;; we have a high order counter
            ;; tick the high order counter and use value to start this counter
            (let [ticked-hoc (tick hoc hovs)
                  ticked-hoc-value (value ticked-hoc)]
              (if (empty? ticked-hoc-value)
                ;; if the high order counter has run out, assemble the pieces
                (assoc c :ctr ticked-ctr
                         :hoc ticked-hoc)
                ;; if the high order counter has a value,
                ;;   start this counter using the value of the high order counter
                ;;   and assemble the pieces
                (assoc c :ctr (start ctr nil (concat hovs ticked-hoc-value))
                         :hoc ticked-hoc)))
            ;; we do not have a high order counter
            (if (empty? hovs)
              ;; without hovs, set the run out counter
              (assoc c :ctr ticked-ctr)
              ;; with hovs, start this run out counter
              (start ctr nil hovs)))
          ;; this ticked counter has not run out
          ;; just set new ticked counter
          (assoc c :ctr ticked-ctr))))))

(defn print-counter
  [^Writer w ctrs]
  (.write w "Counter[")
  (loop [[fc & rcs] ctrs]
    (when fc (print-method fc w))
    (when (seq rcs)
      (.write w ":")
      (recur rcs)))
  (.write w "]"))

(defmethod print-method Counter
  [c w]
  (letfn [(gather [c']
            (loop [cc c' res ()]
              (if (nil? cc)
                res
                (recur (.hoc cc) (conj res (.ctr cc))))))]
    (print-counter w (gather c))))

(defmulti num-digits class)
(defmethod num-digits nil [_] 0)
(defmethod num-digits Digit [_] 1)
(defmethod num-digits Counter [^Counter c]
  (+ (num-digits (.ctr c))
     (num-digits (.hoc c))))
