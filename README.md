# genetic-dynamic

Genetic algorithm simulation of stochastic dynamical systems

This code was writting against JDK 1.0 - it was the first object oriented language I had access to and this project pretty much required OO.
The java piece maintains a collection of (initially random) systems of stochastic differential equations that are evaluated for
matching a target function defined over time.

Each generation is scored with respect to how well they match the target function and the fittets survive and get modified by random "mutations".
As generations come and go the hope is that the best survivors will match the target function better and better.
This is indeed what happens when the target function is a sine wave and the appropriate harmonic oscillator is found.

The java side organizes the genetic algorithm, dynamic system manipulations, and the advancement of generations. It also generates C-code for
calculating the time-evolution of said system of equations.
