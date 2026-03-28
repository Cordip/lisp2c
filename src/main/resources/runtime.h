#pragma once

typedef enum { INT, CONS, NIL, BOOL } LispType;

typedef struct LispVal {
    LispType tag;
    union {
        int number;
        struct { struct LispVal* car; struct LispVal* cdr; } cons;
        int boolean;
    };
} LispVal;

LispVal* make_int(int val);
LispVal* make_cons(LispVal* car, LispVal* cdr);
LispVal* make_nil();
LispVal* make_bool(int val);
LispVal* lisp_add(LispVal* left, LispVal* right);
LispVal* lisp_sub(LispVal* left, LispVal* right);
LispVal* lisp_mul(LispVal* left, LispVal* right);
int is_truthy(LispVal* val);
void print_val(LispVal* val);
