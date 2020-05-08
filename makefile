all:
	javac *.java
	(cd csupport; make)

install: all
	(cd csupport; make install)

distrib:
	(cd csupport; make clean)
	tar cf ga2-dist.tar *.java csupport

bu:
	(cd csupport; make clean)
	bu *.java csupport

clean:
	rm -f *.class
	(cd csupport; make clean)
