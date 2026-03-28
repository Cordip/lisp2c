#include "runtime.h"
#include <stdlib.h>
#include <stdio.h>

LispVal* make_int(int val) {
    LispVal* lispVal = malloc(sizeof(LispVal));
    lispVal->tag = INT;
    lispVal->number = val;
    return lispVal;
}

LispVal* make_bool(int val) {
    LispVal* lispVal = malloc(sizeof(LispVal));
    lispVal->tag = BOOL;
    lispVal->boolean = val;
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

static int get_int_value(LispVal* val) {
    if (val->tag != INT) {
        fprintf(stderr, "Expected INT value in arithmetic\n");
        exit(1);
    }
    return val->number;
}

LispVal* lisp_add(LispVal* left, LispVal* right) {
    return make_int(get_int_value(left) + get_int_value(right));
}

LispVal* lisp_sub(LispVal* left, LispVal* right) {
    return make_int(get_int_value(left) - get_int_value(right));
}

LispVal* lisp_mul(LispVal* left, LispVal* right) {
    return make_int(get_int_value(left) * get_int_value(right));
}

int is_truthy(LispVal* val) {
    if (val->tag == NIL) return 0;
    if (val->tag == BOOL && val->boolean == 0) return 0;
    return 1;
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

    case BOOL:
        printf(val->boolean ? "#t" : "#f");
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
