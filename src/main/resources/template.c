#include "runtime.h"
#include <stdio.h>

LispVal* lisp() {
{{BODY}}
}

int main() {
    LispVal* result = lisp();
    print_val(result);
    printf("\n");
    return 0;
}
