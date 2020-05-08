/* pointer.c */

#include <stdio.h>

void func1(int k) {
  printf("func1(%1i)\n",k);
}

void func2(int k) {
  printf("func2(%1i)\n",k);
}

static void *pnt[2];

void callfunc(void (*func)(int), int k) {
    (*func)(k);
}

int main(int argc, char *argv[]) {
    pnt[0]=(*func1);
    pnt[1]=(*func2);
    callfunc(pnt[0],11);  
    callfunc(pnt[1],22);
}
