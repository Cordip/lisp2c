#include "runtime.h"
#include <gc.h>
#include <stdio.h>

LispVal lisp(void) {
{{BODY}}
}

int main(void) {
  GC_INIT();
  LispVal result = lisp();
  print_val(result);
  printf("\n");
  return 0;
}
