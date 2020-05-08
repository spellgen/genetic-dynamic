OBJ	= gasim.o gasdev.o ran1.o
INCDIR	= $(HOME)/include
LIBDIR	= $(HOME)/lib
CFLAGS	= -DDOUBLE -I$(INCDIR) -g

default: libgasim.a

test: test.o libgasim.a
	gcc -o run-$@ -g $@.o -L. -lgasim -L$(LIBDIR) -ltfd -lm

libgasim.a: $(OBJ)
	ar cr $@ $(OBJ)
	ranlib $@

install: libgasim.a
	cp libgasim.a $(HOME)/lib

clean:
	rm -f *.o *.a runsim
