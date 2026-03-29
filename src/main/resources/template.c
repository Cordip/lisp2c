#include "runtime.h"
#include <stdio.h>

LispVal lisp(void) {
{{BODY}}
}

int main(void) {
  LispVal result = lisp();
  print_val(result);
  printf("\n");
  return 0;
}
