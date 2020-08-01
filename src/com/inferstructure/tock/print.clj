(ns com.inferstructure.tock.print
  (:import [clojure.lang IPending]))

(defn seq->str
  [s]
  (if (and (instance? IPending s) (realized? s))
    (if (= 2 (count s))
      (str s)
      (str "(" (first s) " <+" (count s) " more>)"))
    (let [ft (take 2 s)
          r (drop 2 s)]
      (if (seq r)
        (str "(" (first ft) " <+unrealized>)")
        (str (apply list ft))))))
