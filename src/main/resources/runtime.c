#include "runtime.h"
#include <stdio.h>

// equal? — structural recursive equality
LispVal lisp_equal(LispVal a, LispVal b) {
  if (TAG(a) != TAG(b))
    return LISP_FALSE;
  if (TAG(a) == TAG_INT)
    return get_int(a) == get_int(b) ? LISP_TRUE : LISP_FALSE;
  if (TAG(a) == TAG_CONS)
    return is_truthy(lisp_equal(car(a), car(b))) && is_truthy(lisp_equal(cdr(a), cdr(b))) ? LISP_TRUE : LISP_FALSE;
  if (TAG(a) == TAG_SYMBOL)
    return strcmp(get_symbol(a), get_symbol(b)) == 0 ? LISP_TRUE : LISP_FALSE;
  return a == b ? LISP_TRUE : LISP_FALSE;
}

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
