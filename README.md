# tock

A crazy, silly, little library for generalized counting.

## Huh?

What the heck is "generalized counting"?

> __count__
> <br>
> _verb_
> 
> to say the names of numbers one after the other in order
> <br>
> -- Cambridge Dictionary

Let's break this down.

### to say the names of numbers
In our case, to 'say' a number is to produce the current value of a counter.
A counter is an ordered collection of digits.
In Clojure, this is an actual _sequence_ of digits.

So, in our world of generlized counting, each number is a single (finite) sequence of digits.

### one after the other
Counting takes a current number and produces another number according to
some prescribed formula.
Without much thought, we count 0, 1, 2, 3, ..., by applying the formula 'increment' 
to produce one number from the next. 

When we count (2020 2 28), (2020 2 29), (2020 3 1), we are applying a more complex
formuala that understands how many days are in each month, leap years. etc.

### in order
Since a counter knows how to produce a number after its current number, 
it can be thought of as representing an ordered collection of numbers.

Putting it all together, a counter produces an ordered collection of numbers.
In Clojure, this is an actual _sequence_ of numbers.

## I need more details

I know.

We count all the time. So much so that what is actually happening goes by unnoticed.

Even if the code in this library does not prove to be very useful, I hope it can
serve as an example of relentlessly pulling on a conceptual thread until the
garment is reduced to an undistiguished pile of raw material.

So, let's start pulling.

Let's say we are going to count based on two digits. Now, right there you
are probably making an assumption (even if justifiably so) of what a digit
is and how the two digits are related. We need to bring those assumptions
to light.

A two-digit number normally means:
- Each digit runs through a sequence of numbers from 0 to 9.
- When a digit reaches the end of its sequence it starts over at 0, 
  and the next higher digit advances.

We are so used to this kind of uniformity when we think of counting.
Even numbers in abase othert than 10 has the same uniformity where
each digit position runs through the sequence of numbers dictated by
the base causing higher digit positions to advance as lower positions
cycle back around to 0.

To help see how counting can be made more general consider a calendar.
Let's say our 3 "digit" calendar numer is (year month day). As days
(the lowest order digit) advance, they will cycle back around to 1
when you have exhausted all the days _for the current year and month_.
The number of days in month depends on the month as well as whether
it is a leap year.

And it is here we get a glimpse behind the curtain and see more of how
simple numbers actually work. When a base 10 digit advances from 9, the
new sequence of numbers it will count through is determined by the
higher order digits. But, as it happens, for natural numbers in our world
that sequence is a constant 0 through 9.

Generalizing, we get this.
```
For an n-digit number, (dn-1 dn-2 ... d1 d0),
the sequence of numbers that di counts through is 
fdi(dn-1 dn-2 ... di+1)
```

It just so happens that in most of the counting we are used to
`fdi == (0 1 ...9) for all i`. But the world is full of much more
comples examples of counting. We just need to look for them.

## Purpose

The purpose of this library is to provide these things:
- a way to define counters with arbitrarily complex digit sequences
- a way to easily specify common cases of digit sequences
- a way to compose counters from other counters

## Goals

My main goal was to take a simple idea and play with it until I was satified
that I exhausted all it had to offer me.

Now, since a little library resulted from the exploriation, it is possible 
that others  might get a benefit from using this library.
And, as I stated earlier, maybe someone will get a benefit from seeing how one
person took a simple idea and just beat it relentlessly until it had no more
to give.

_TODO maybe - explain why I think this is enough to stop, for me._

That said, Mr. Shannon and Mr. Davis sum up my feelings quite nicely.

> I have spent lots of time on totally useless problems.
>
>  -- Claude Shannon

> So What or Kind of Blue were done in that era, the right hour, the right day. It's over; it's on the record.
>
>  -- Miles Davis


## Status

- I find it quite usable, but I'm not sure yet if there will be an incompatable
change so, it's best to call it alpha for now.

- Not at clojars.org yet.

## Usage

This REPL session demontrates a number of ways you can use tock.

_TODO add api docs and redo/extend this_
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