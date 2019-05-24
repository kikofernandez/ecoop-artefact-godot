# Godot: All the Benefits of Implicit and Explicit Futures (Artefact)

This repository contains the up-to-date version of the ECOOP artefact for
the paper *Godot: All the Benefits of Implicit and Explicit Futures*.
The companion research artefact of the paper ([found here](http://filr.polacksbacken.uu.se/ssf/s/readFile/share/2267/-1137417964177284669/publicLink/GodotArtefact.zip))
is the official one, and it is immutable (receiving no updates).

This repository is a duplicate of the official one, excluding the Virtual Machine
(which is bigger than what Github allows me to host here).

(Note to myself: The internal scripts are located in [here](https://github.com/kikofernandez/scala-godot))

## Structure

Brief summary of the folder structure:

- `documentation` folder contains the documentation of the artefact.
- `code` folder contains the implementation data-flow futures in terms of
  control-flow futures.
- `LICENSE` the license of this work.
- A virtual machine ready to run can be downloaded from [here](http://filr.polacksbacken.uu.se/ssf/s/readFile/share/2267/-1137417964177284669/publicLink/GodotArtefact.zip)

## Artefact

This artefact contains an implementation of data-flow futures in terms of
control-flow futures, in the Scala language. In
the implementation, we show microbenchmarks that solve the three identified
problems in the paper:

1. [*The Type Proliferation Problem* (Section 2, Problems
Inherent in Explicit and Implicit Futures)](documentation/assets/submitted-version.pdf#page=4),
2. [*The Fulfilment Observation Problem* (Section 2, Problems
Inherent in Explicit and Implicit Futures)](documentation/assets/submitted-version.pdf#page=5), and
3. [*The Future Proliferation Problem* (Section 2, Problems
Inherent in Explicit and Implicit Futures)](documentation/assets/submitted-version.pdf#page=4)

This artifact can be seen as an extension to
[Section 5.2. Notes on Implementing Godot](documentation/assets/submitted-version.pdf#page=23).
However, it is out of the scope of the artifact to modify the Scala compiler to
perform implicit delegation
([Section 5.1 Avoiding Future Nesting through Implicit Delegation](documentation/assets/submitted-version.pdf#page22)),
which allows asynchronous tail-recursive calls to run in
constant space. This can be solved by either using an advanced macro system or
updating the Scala compiler ([Section 5.2. Notes on Implementing Godot](documentation/assets/submitted-version.pdf#page=23)).

This artifact shows an implementation of the formal semantics of the paper using
the well-established programming language Scala. The reader can:

- Run the tests by typing `sbt test` (in the `godot` folder), which tests type
  checking rules and runtime semantics described in the paper. This will run 18
  tests that exercise different features of the type system while also checking
  that well-typed programs work as expected.

- Run two microbenchmarks in the form of well-known algorithms
  (*factorial* and *fibonacci*) implemented using the future styles discussed in the
  paper, that highlight the difference between control-flow and data-flow
  futures.

- Run a simulation of a proxy service using control-flow futures parameterised by
  data-flow futures which allows inner data-flow computation to asynchronously
  delegate work to another worker, without mimicking the communication
  structure at the type level, mixing both styles of futures (control- and data-flow futures).

- Read the [Implementation details] section, which explains how data-flow futures
  are integrated in the Scala language, and is aimed at researchers who want to use our
  ideas in implementations of their own, or want to see a concrete example of
  the ideas in the paper integrated in a real programming language.

- Check the mapping of combinators from the formal semantics to the implementation, *Common API* section
  (from documentation). For example, the paper spawns a task (with a
  future) by calling `async expr`, and the implementation mimics the semantics
  by calling `Future { expr }`.

- Check the restrictions  of the current implementation (Section Restrictions in documentation).
