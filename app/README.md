Fractal Explorer v1.0
=====================

This is an extension of a script I wrote in JavaScript for
drawing the Mandelbrot Set. The result of this script was performance
limited by my implementation in JavaScript, and limited to a grayscale
colouring scheme. Within my application, the drawing method (calling
a point to be rendered) was ineficient. Writing to a byte array and
then drawing this would have worked better.

The purpose of this application is to extend on the script's
functionality while improving performance, extending the colour scheme,
and allow for exportation of high-quality renders.



### Old Description ###
Mandelbrot set navigator - by Phillip Halloran(2016)
Inspired by [How to Fold a Julia Fractal - Steven Wittens](http://acko.net/blog/how-to-fold-a-julia-fractal/)

The purpose of this script (Originally written in JavaScript) is to provide and interactive experience of the Mandelbrot set.
	The Mandelbrot set is a set of numbers, existing on the complex plane. Numbers
	are in this set if and only if they do not "blow up to infinity" (-thanks to Mark
	for using those words).

My idea of colouring these points is be based on how fast they blow up to a point of obvious divergence.
	This was mentioned on Wikipedia. There is an unseen set of calculations that are not shown,
	which are the numerous iterations of Z to the point of a hard limit or to a point of divergence.

So, to start of with I'm just going to jump into some reading on how to I can program the controls as
	they are what will provide the interactivity.
	I have a few ideas:
	- sliders
	- mouse coordinates
	- numerical input areas
	-(set resolution of input with another input for higher accuracy (linear, polynomial, logarithmic))

What things can these inputs change?
	- resolution (Greatly affects performance of program)
	- centre coordinate (a part of VIEW PARAMETERS)
	- the way the set is coloured (do the colours change in a way unaffected by zoom?)

VIEW PARAMETERS
	Default range values for view are set to -2, 2 for both real and imaginary components with origin at (0,0)
	Range can be set by zoom factor and centre point input or directly

creates a set of values stored in a 2D array, the first element being a coordinate on the complex Cartesian plane
	across the domain Re([-2, 2]), Im([-2, 2]) and the second being their calculated escape time.
	One draw loop takes place, drawing all the escape values across the range