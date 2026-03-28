#pragma once

typedef enum { INT, CONS, NIL } LispType;

typedef struct LispVal {
    LispType tag;
    union {
        int number;
        struct { struct LispVal* car; struct LispVal* cdr; } cons;
    };
} LispVal;

LispVal* make_int(int val);
LispVal* make_cons(LispVal* car, LispVal* cdr);
LispVal* make_nil();
LispVal* lisp_add(LispVal* left, LispVal* right);
LispVal* lisp_sub(LispVal* left, LispVal* right);
LispVal* lisp_mul(LispVal* left, LispVal* right);
void print_val(LispVal* val);
