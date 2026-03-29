#include "runtime.h"
#include <stdio.h>

static void print_tail(LispVal cdr_val);

void print_val(LispVal val) {
    switch (TAG(val)) {
    case TAG_NIL:
        printf("NIL");
        break;
    case TAG_BOOL_T:
        printf("#t");
        break;
    case TAG_BOOL_F:
        printf("#f");
        break;
    case TAG_INT:
        printf("%d", get_int(val));
        break;
    case TAG_SYMBOL:
        printf("%s", get_symbol(val));
        break;
    case TAG_CONS:
        printf("(");
        print_val(car(val));
        print_tail(cdr(val));
        break;
    case TAG_CLOSURE:
        printf("#<closure>");
        break;
    default:
        printf("#<unknown:%04x>", TAG(val));
        break;
    }
}

static void print_tail(LispVal cdr_val) {
    switch (TAG(cdr_val)) {
    case TAG_NIL:
        printf(")");
        break;
    case TAG_CONS:
        printf(" ");
        print_val(car(cdr_val));
        print_tail(cdr(cdr_val));
        break;
    default:
        printf(" . ");
        print_val(cdr_val);
        printf(")");
        break;
    }
}
