(ns com.inferstructure.tock.arity)

;; Code from https://stackoverflow.com/a/20312211

(defn- provided
  [cond fun x]
  (if cond
    (fun x)
    x))

(defn- append
  [xs x]
  (conj (vec xs) x))

(defn- arity-of-method
  [method]
  (->> method .getParameterTypes alength))

(defn arities
  [fun]
  (let [all-declared-methods (.getDeclaredMethods (class fun))
        methods-named (fn [name]
                        (filter #(= (.getName %) name) all-declared-methods))
        methods-named-invoke (methods-named "invoke")
        methods-named-do-invoke (methods-named "doInvoke")
        is-rest-fn (seq methods-named-do-invoke)]
    (->> methods-named-invoke
         (map arity-of-method)
         sort
         (provided is-rest-fn
                   (fn [v] (append v :rest))))))

;;
;; There is an edge in the above code I may investigate and change
;;  but it pretty much serves my purposes.
;;
;; With it in mind, this is the function that defines how arguments need to
;;  be applied to digit functions.
;;

(defn apply-last-max-arity
  "Like clojure.core/apply but only apply the 'lower order' args
  depending on the arities of f.
  TODO - better wording"
  [f args]
  (let [num-args (count args)
        arities (arities f)
        [ta da] (split-with #(and (number? %) (<= % num-args)) arities)
        num-to-apply (cond
                       (and (empty? ta)
                            (not= [:rest] da))
                       (throw (ex-info "Function does not have a supporting arity."
                                       {:fn      f
                                        :arities arities
                                        :args    args}))

                       (= [:rest] da)
                       num-args

                       :else
                       (last ta))]
    (apply f (->> args
                  (drop (- num-args num-to-apply))))))
