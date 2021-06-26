# tock

A crazy, silly, little library for generalized counting.

## Huh?

What the heck is __generalized counting__?

> __count__
> <br>
> _verb_
> 
> to say the names of numbers one after the other in order
> <br>
> -- Cambridge Dictionary

Let's break this down.

_to say the names of numbers_
<br>
In our case, to 'say' a number is to produce the current value of a counter.
A counter is an ordered collection of digits.
In Clojure, this is an actual _sequence_ of digits.

So, in our world of generlized counting, each number is a single (finite) sequence of digits.

_one after the other_
<br>
Counting takes a current number and produces another number according to
some prescribed formula.
Without much thought, we count `0, 1, 2, 3, ...`, by applying the formula `increment` 
to produce one number from the next. 

When we count `(2020 2 28), (2020 2 29), (2020 3 1)`, we are applying a more complex
formuala that understands how many days are in each month, leap years. etc.

_in order_
<br>
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

We can easily generalize this by not assuming we are in base 10
and change this description by replacing 9 with base-1.

But there is another generality that is nearly unseen in this
example because we are so used to a certain kind of uniformity
inherent in this particular counting process.

To help see this other generality we consider a calendar.
Let's say our 3 "digit" calendar number is `(year month day)`. 
Here, counting means advancing the day digit.
As the day advances, it will cycle back around to 1
when you have exhausted all the days _for the current year and month_.
The number of days in month depends on the month as well as whether
it is a leap year.

And it is here we get a glimpse behind the curtain and see how
simple numbers actually work. When a base 10 digit advances from 9, the
new sequence of numbers it will count through is determined by the
higher order digits. But, as it happens, for natural numbers in our world,
that sequence is a constant `(0 ... 9)`.

Generalizing, we get this.
```
For an n-digit number, (dn-1 dn-2 ... d1 d0),
the sequence of numbers that di counts through is a function
of the value of all of the higher order digits, (fdi di+1 ... dn-1).
```

It just so happens that for the counting we are mostly used to,
the value of calling `fdi` is the sequence `(0 1 ... 9) for any i`.

But the world has a variety of examples of counting.
We just need to look for them.

## Counting, simply stated

The tock library provides a general way to count where:
- digits are grouped and ordered into a counter
- a digit is a sequence that is defined in cooperation with 
  the higher order digits of its containing counter
- the mechanism of sequencing digits and handling the process 
  of determining digit sequences based on higher order values 
  is handled for you

## Purpose

The purpose of this library is to provide these things:
- a way to define counters with arbitrarily complex digit sequences
- an open way to easily specify common cases of digit sequences
- a way to compose counters from other counters
- a built-in mechanism to generate lazy sequnces of both counters
  and counter values
- to ruin that old sweater of mine

## Goals

My main goal was to take a simple idea and play with it until I was satified
that I exhausted all it had to offer me.

Now, since a little library resulted from the exploriation, it is possible 
that others might get a benefit from using it.

And, as I stated earlier, maybe someone will get a benefit from seeing how one
person took a simple idea and just beat it relentlessly until it had no more
life to give.

If this ends up no more than a personal, intellctual exercise,
I'll be happy with that.

> I have spent lots of time on totally useless problems.
>
>  -- Claude Shannon

In any case, this as a statement in time.
Much like jazz groups that recorded their musical ideas 
for others to build on, this is a record of some code ideas
that could influence further ideas in others. That is something
I would like to see more of.

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