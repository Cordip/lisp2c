#pragma once

#include "tags.h"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef uint64_t LispVal;

// --- Pack / Unpack ---
// Layout: [48-bit payload][16-bit tag]
// Tag in lower bits — shift right strips the tag and extracts payload.
// For ints, (int32_t) cast recovers the sign after shift.

#define TAG(v) ((uint16_t)((v) & 0xFFFF))
#define PAYLOAD(v) ((v) >> 16)
#define PACK(tag, payload) (((uint64_t)(payload) << 16) | (uint16_t)(tag))

// --- Constants ---
#define LISP_NIL PACK(TAG_NIL, 0)
#define LISP_TRUE PACK(TAG_BOOL_T, 0)
#define LISP_FALSE PACK(TAG_BOOL_F, 0)

// --- Constructors (inline — no malloc) ---

static inline LispVal make_int(int32_t n) { return PACK(TAG_INT, (uint64_t)(uint32_t)n); }

// --- Constructors (heap — malloc) ---

static inline LispVal make_cons(LispVal head, LispVal tail) {
  LispVal *cell = malloc(2 * sizeof(LispVal));
  if (!cell) {
    fprintf(stderr, "out of memory\n");
    exit(1);
  }
  cell[0] = head;
  cell[1] = tail;
  return PACK(TAG_CONS, (uint64_t)(uintptr_t)cell);
}

static inline LispVal make_symbol(const char *name) {
  char *copy = strdup(name);
  if (!copy) {
    fprintf(stderr, "out of memory\n");
    exit(1);
  }
  return PACK(TAG_SYMBOL, (uint64_t)(uintptr_t)copy);
}

// --- Accessors ---

static inline uint16_t get_tag(LispVal v) { return TAG(v); }
static inline int32_t get_int(LispVal v) { return (int32_t)((int64_t)v >> 16); }
static inline LispVal car(LispVal v) { return ((LispVal *)(uintptr_t)PAYLOAD(v))[0]; }
static inline LispVal cdr(LispVal v) { return ((LispVal *)(uintptr_t)PAYLOAD(v))[1]; }
static inline const char *get_symbol(LispVal v) { return (const char *)(uintptr_t)PAYLOAD(v); }

static inline int is_truthy(LispVal v) { return TAG(v) != TAG_NIL && TAG(v) != TAG_BOOL_F; }

// --- Arithmetic ---

static inline void expect_int(LispVal v, const char *op) {
  if (TAG(v) != TAG_INT) {
    fprintf(stderr, "%s: expected INT, got tag %04x\n", op, TAG(v));
    exit(1);
  }
}

static inline LispVal lisp_add(LispVal a, LispVal b) {
  expect_int(a, "+");
  expect_int(b, "+");
  return make_int(get_int(a) + get_int(b));
}

static inline LispVal lisp_sub(LispVal a, LispVal b) {
  expect_int(a, "-");
  expect_int(b, "-");
  return make_int(get_int(a) - get_int(b));
}

static inline LispVal lisp_mul(LispVal a, LispVal b) {
  expect_int(a, "*");
  expect_int(b, "*");
  return make_int(get_int(a) * get_int(b));
}

// eq? — pointer/identity equality
static inline LispVal lisp_eq(LispVal a, LispVal b) { return a == b ? LISP_TRUE : LISP_FALSE; }

// eqv? — eq? + value equality for numbers
static inline LispVal lisp_eqv(LispVal a, LispVal b) {
  if (TAG(a) != TAG(b))
    return LISP_FALSE;
  if (TAG(a) == TAG_INT)
    return get_int(a) == get_int(b) ? LISP_TRUE : LISP_FALSE;
  return a == b ? LISP_TRUE : LISP_FALSE;
}

// equal? — structural (recursive, defined in runtime.c)
LispVal lisp_equal(LispVal a, LispVal b);

static inline LispVal lisp_lt(LispVal a, LispVal b) {
  expect_int(a, "<");
  expect_int(b, "<");
  return get_int(a) < get_int(b) ? LISP_TRUE : LISP_FALSE;
}

static inline LispVal lisp_gt(LispVal a, LispVal b) {
  expect_int(a, ">");
  expect_int(b, ">");
  return get_int(a) > get_int(b) ? LISP_TRUE : LISP_FALSE;
}
static inline LispVal lisp_car(LispVal v) { return car(v); }
static inline LispVal lisp_cdr(LispVal v) { return cdr(v); }

// --- print_val (implemented in runtime.c) ---

void print_val(LispVal val);
