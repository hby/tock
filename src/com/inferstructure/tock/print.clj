(ns com.inferstructure.tock.print
  (:import [clojure.lang IPending]))

(def ^:dynamic *print-look-ahead-length* 2)

(defn seq->str
  [s]
  (let [[f' r] (split-at *print-look-ahead-length* s)
        f (apply list f')]
    (if (seq r)
      (str "(" (first f) " " (second f) " <+ more>)")
      (str f))))
