# tock

A crazy, silly, little library for generalized counting.

TODO - quote here

## Status

Sort of alpha, but quite usable by me.

Not at clojars.org yet.

## Usage

Better Readme and blog post coming. For now, a REPL session:
```clojure
% clj
user=> (require '[com.inferstructure.tock :as tk])
nil
user=> (def bit (tk/digit :tock-builtin/seq [0 1]))
#'user/bit
user=> (def two-bit-counter (tk/counter [bit bit]))
#'user/two-bit-counter
user=> (-> two-bit-counter
           (tk/start)
           (tk/value-seq))
((0 0) (0 1) (1 0) (1 1))
user=> (def four-bit-counter (tk/counter [two-bit-counter two-bit-counter]))
#'user/four-bit-counter
user=> (-> four-bit-counter
           (tk/start)
           (tk/value-seq))
((0 0 0 0) (0 0 0 1) (0 0 1 0) (0 0 1 1) (0 1 0 0) (0 1 0 1) (0 1 1 0) (0 1 1 1) (1 0 0 0) (1 0 0 1) (1 0 1 0) (1 0 1 1) (1 1 0 0) (1 1 0 1) (1 1 1 0) (1 1 1 1))
user=> (defn leap?
         [y]
         (if (zero? (mod y 4))
           (if (zero? (mod y 100))
             (if (zero? (mod y 400))
               true
               false)
             true)
           false))
#'user/leap?
user=> (def ymd (tk/counter [(tk/digit :tock-builtin/fn (fn [] (iterate inc 2020)))
                             (tk/digit :tock-builtin/fn (fn [] (range 1 13)))
                             (tk/digit :tock-builtin/fn (fn [y m]
                                                           (case m
                                                             (1 3 5 7 8 10 12) (range 1 32)
                                                             2 (range 1 (if (leap? y) 30 29))
                                                             (4 6 9 11) (range 1 31))))]))
#'user/ymd
user=> (->> (-> ymd
                (tk/start)
                (tk/value-seq))
            first)
(2020 1 1)
user=> (->> (-> ymd
                (tk/start)
                (tk/value-seq))
            (drop 365)
            (take 2))
((2020 12 31) (2021 1 1))
user=> (->> (-> ymd
                (tk/start [2020 11 3])
                (tk/value-seq))
            (take-while #(not= % [2021 1 1]))
            count)
59
user=>
```

## Installing and deploying

Build a deployable jar of this library:

    $ clojure -A:jar

Install it locally:

    $ clojure -A:install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment variables:

    $ clojure -A:deploy

## Running tests

    $ clojure -A:test:runner

## License

Copyright © 2020 Bret Young

Distributed under the MIT License.