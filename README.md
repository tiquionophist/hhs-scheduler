# hhs-scheduler

A standalone companion to the adult game [HHS+](https://www.henthighschool.com/hhsplus/) (NSFW)
which generates class schedules based on teacher assignments and the number of times each subject
should be taught per week. This makes it easy to specify the way you want your school to be run and
let this program figure out how to satisfy those requirements.

This project is in early development and is not ready for general use yet.

Nerd note: the general solution to this kind of scheduling problem is known to be
[NP-complete](https://en.wikipedia.org/wiki/NP-completeness)!
([For example](https://math.stackexchange.com/q/2285015), as a reduction to k-coloring.)
This means that there is no known (and likely no possible) efficient algorithm to deal with large
instances of the problem (in this case, when you have many classes and many teachers). For simple
cases (scheduling 2 or 3 classes), an exhaustive search is possible but for even medium-size
problems (6-8 classes) the search space is too large to be brute forced. I've implemented a few
alternative algorithms; in my experience a randomized search which regularly starts from scratch to
avoid local maximums will solve any reasonable schedule in HHS+ in well under a second.
