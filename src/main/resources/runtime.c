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

// --- Closure and Env ---

Env *make_env(int size) {
  Env *env = malloc(sizeof(Env));
  if (!env) {
    fprintf(stderr, "out of memory\n");
    exit(1);
  }
  env->size = size;
  env->vars = size > 0 ? malloc(sizeof(LispVal) * (size_t)size) : NULL;
  if (size > 0 && !env->vars) {
    fprintf(stderr, "out of memory\n");
    exit(1);
  }
  return env;
}

LispVal make_closure(LispVal (*fn)(Env *, int, LispVal *), Env *env) {
  ClosureData *data = malloc(sizeof(ClosureData));
  if (!data) {
    fprintf(stderr, "out of memory\n");
    exit(1);
  }
  data->fn = fn;
  data->env = env;
  return PACK(TAG_CLOSURE, (uint64_t)(uintptr_t)data);
}

LispVal apply_closure(LispVal closure, int argc, LispVal *argv) {
  if (TAG(closure) != TAG_CLOSURE) {
    fprintf(stderr, "apply_closure: expected CLOSURE, got tag %04x\n", TAG(closure));
    exit(1);
  }
  ClosureData *data = (ClosureData *)(uintptr_t)PAYLOAD(closure);
  return data->fn(data->env, argc, argv);
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
