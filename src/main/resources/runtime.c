#include "runtime.h"
#include <stdlib.h>
#include <stdio.h>

LispVal* make_int(int val) {
    LispVal* lispVal = malloc(sizeof(LispVal));
    lispVal->tag = INT;
    lispVal->number = val;
    return lispVal;
}

LispVal* make_cons(LispVal* car, LispVal* cdr) {
    LispVal* lispVal = malloc(sizeof(LispVal));
    lispVal->tag = CONS;
    lispVal->cons.car = car;
    lispVal->cons.cdr = cdr;
    return lispVal;
}

LispVal* make_nil() {
    LispVal* lispVal = malloc(sizeof(LispVal));
    lispVal->tag = NIL;
    return lispVal;
}

static void print_tail(LispVal* cdr);

void print_val(LispVal* val) {
    switch (val->tag)
    {
    case NIL:
        printf("NIL");
        break;

    case INT:
        printf("%d", val->number);
        break;

    case CONS:
        printf("(");
        print_val(val->cons.car);
        print_tail(val->cons.cdr);
        break;
    
    default:
        break;
    }
}

static void print_tail(LispVal* cdr) {
    switch (cdr->tag)
    {
    case NIL:
        printf(")");
        break;

    case CONS:
        printf(" ");
        print_val(cdr->cons.car);
        print_tail(cdr->cons.cdr);
        break;
    
    default:
        printf(" . ");
        print_val(cdr);
        printf(")");
        break;
    }
}
