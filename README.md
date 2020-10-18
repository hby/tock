# tock

A crazy, silly, little library for generalized counting.

## Goals

It is possible that others will get a benefit from using this library 

> I have spent lots of time on totally useless problems.
>
>  -- Claude Shannon

> So What or Kind of Blue were done in that era, the right hour, the right day. It's over; it's on the record.
>
>  -- Miles Davis


## Status

Sort of alpha, but quite usable by me.

Not at clojars.org yet.

## Usage

Better Readme and blog post coming. For now, a REPL session:
```clojure
% clj
user=> (require '[com.inferstructure.tock :as tk])
nil
user=> (def bit (tk/digit ::tk/digit-seq [0 1]))
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
user=> (def y-m-d (tk/counter [(tk/digit ::tk/digit-fn (fn [] (iterate inc 2020)))
                               (tk/digit ::tk/digit-fn (fn [] (range 1 13)))
                               (tk/digit ::tk/digit-fn (fn [y m] (case m
                                                                   (1 3 5 7 8 10 12) (range 1 32)
                                                                   2 (range 1 (if (leap? y) 30 29))
                                                                   (4 6 9 11) (range 1 31))))]))
#'user/y-m-d
user=> (->> (-> y-m-d
                (tk/start)
                (tk/value-seq))
            first)
(2020 1 1)
user=> (->> (-> y-m-d
                (tk/start)
                (tk/value-seq))
            (drop 365)
            (take 2))
((2020 12 31) (2021 1 1))
user=> (->> (-> y-m-d
                (tk/start [2020 11 3])
                (tk/value-seq))
            (take-while #(not= % [2021 1 1]))
            count)
59
user=>
```

## Installing and deploying

Build a deployable jar of this library:

    $ clojure -M:jar

Install it locally:

    $ clojure -M:install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment variables:

    $ clojure -M:deploy

## Running tests

    $ clojure -M:test:runner

## License

Copyright © 2020 Bret Young

Distributed under the MIT License.