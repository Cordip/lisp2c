#include "unity.h"
#include "runtime.h"
#include <gc.h>
#include <limits.h>

void setUp(void) {}
void tearDown(void) {}

void test_make_int(void) {
    LispVal n = make_int(42);
    TEST_ASSERT_EQUAL_UINT16(TAG_INT, TAG(n));
    TEST_ASSERT_EQUAL_INT32(42, get_int(n));
}

void test_int_zero(void) {
    TEST_ASSERT_EQUAL_INT32(0, get_int(make_int(0)));
}

void test_int_negative(void) {
    TEST_ASSERT_EQUAL_INT32(-1, get_int(make_int(-1)));
}

void test_int_min(void) {
    TEST_ASSERT_EQUAL_INT32(INT32_MIN, get_int(make_int(INT32_MIN)));
}

void test_int_max(void) {
    TEST_ASSERT_EQUAL_INT32(INT32_MAX, get_int(make_int(INT32_MAX)));
}

void test_nil_constant(void) {
    TEST_ASSERT_EQUAL_UINT16(TAG_NIL, TAG(LISP_NIL));
}

void test_bool_constants(void) {
    TEST_ASSERT_EQUAL_UINT16(TAG_BOOL_T, TAG(LISP_TRUE));
    TEST_ASSERT_EQUAL_UINT16(TAG_BOOL_F, TAG(LISP_FALSE));
}

void test_cons_car_cdr(void) {
    LispVal pair = make_cons(make_int(1), make_int(2));
    TEST_ASSERT_EQUAL_UINT16(TAG_CONS, TAG(pair));
    TEST_ASSERT_EQUAL_INT32(1, get_int(car(pair)));
    TEST_ASSERT_EQUAL_INT32(2, get_int(cdr(pair)));
}

void test_cons_list(void) {
    LispVal list = make_cons(make_int(1),
                   make_cons(make_int(2),
                   make_cons(make_int(3), LISP_NIL)));
    TEST_ASSERT_EQUAL_INT32(1, get_int(car(list)));
    TEST_ASSERT_EQUAL_INT32(2, get_int(car(cdr(list))));
    TEST_ASSERT_EQUAL_INT32(3, get_int(car(cdr(cdr(list)))));
    TEST_ASSERT_EQUAL_UINT16(TAG_NIL, TAG(cdr(cdr(cdr(list)))));
}

void test_is_truthy(void) {
    TEST_ASSERT_TRUE(is_truthy(make_int(1)));
    TEST_ASSERT_TRUE(is_truthy(make_int(0)));  /* Scheme: 0 is truthy */
    TEST_ASSERT_TRUE(is_truthy(LISP_TRUE));
    TEST_ASSERT_FALSE(is_truthy(LISP_NIL));
    TEST_ASSERT_FALSE(is_truthy(LISP_FALSE));
}

void test_arithmetic(void) {
    TEST_ASSERT_EQUAL_INT32(3, get_int(lisp_add(make_int(1), make_int(2))));
    TEST_ASSERT_EQUAL_INT32(2, get_int(lisp_sub(make_int(5), make_int(3))));
    TEST_ASSERT_EQUAL_INT32(6, get_int(lisp_mul(make_int(2), make_int(3))));
    TEST_ASSERT_EQUAL_INT32(-1, get_int(lisp_sub(make_int(1), make_int(2))));
}

void test_comparisons(void) {
    TEST_ASSERT_TRUE(is_truthy(lisp_lt(make_int(1), make_int(2))));
    TEST_ASSERT_FALSE(is_truthy(lisp_lt(make_int(2), make_int(1))));
    TEST_ASSERT_TRUE(is_truthy(lisp_gt(make_int(3), make_int(2))));
    TEST_ASSERT_FALSE(is_truthy(lisp_gt(make_int(1), make_int(2))));
    /* negative numbers — exercises sign extension in get_int */
    TEST_ASSERT_TRUE(is_truthy(lisp_lt(make_int(-1), make_int(0))));
    TEST_ASSERT_TRUE(is_truthy(lisp_gt(make_int(0), make_int(-1))));
}

void test_eq(void) {
    /* eq? — identity */
    TEST_ASSERT_TRUE(is_truthy(lisp_eq(LISP_TRUE, LISP_TRUE)));
    TEST_ASSERT_TRUE(is_truthy(lisp_eq(LISP_NIL, LISP_NIL)));
    TEST_ASSERT_FALSE(is_truthy(lisp_eq(LISP_TRUE, LISP_FALSE)));
    TEST_ASSERT_FALSE(is_truthy(lisp_eq(make_int(0), LISP_NIL)));
    /* same cons — same pointer */
    LispVal pair = make_cons(make_int(1), make_int(2));
    TEST_ASSERT_TRUE(is_truthy(lisp_eq(pair, pair)));
    /* different cons — different pointer */
    TEST_ASSERT_FALSE(is_truthy(lisp_eq(make_cons(make_int(1), make_int(2)), make_cons(make_int(1), make_int(2)))));
}

void test_eqv(void) {
    /* eqv? — value equality for numbers */
    TEST_ASSERT_TRUE(is_truthy(lisp_eqv(make_int(1), make_int(1))));
    TEST_ASSERT_FALSE(is_truthy(lisp_eqv(make_int(1), make_int(2))));
    TEST_ASSERT_TRUE(is_truthy(lisp_eqv(make_int(-1), make_int(-1))));
    TEST_ASSERT_TRUE(is_truthy(lisp_eqv(LISP_TRUE, LISP_TRUE)));
    TEST_ASSERT_FALSE(is_truthy(lisp_eqv(make_int(0), LISP_NIL)));
}

void test_equal(void) {
    /* equal? — structural */
    TEST_ASSERT_TRUE(is_truthy(lisp_equal(make_int(1), make_int(1))));
    TEST_ASSERT_FALSE(is_truthy(lisp_equal(make_int(1), make_int(2))));
    /* cons structural equality */
    TEST_ASSERT_TRUE(is_truthy(lisp_equal(
        make_cons(make_int(1), make_int(2)),
        make_cons(make_int(1), make_int(2)))));
    TEST_ASSERT_FALSE(is_truthy(lisp_equal(
        make_cons(make_int(1), make_int(2)),
        make_cons(make_int(1), make_int(3)))));
    /* symbol by name */
    TEST_ASSERT_TRUE(is_truthy(lisp_equal(make_symbol("foo"), make_symbol("foo"))));
    TEST_ASSERT_FALSE(is_truthy(lisp_equal(make_symbol("foo"), make_symbol("bar"))));
}

void test_get_tag(void) {
    TEST_ASSERT_EQUAL_UINT16(TAG_INT, get_tag(make_int(42)));
    TEST_ASSERT_EQUAL_UINT16(TAG_NIL, get_tag(LISP_NIL));
    TEST_ASSERT_EQUAL_UINT16(TAG_BOOL_T, get_tag(LISP_TRUE));
    TEST_ASSERT_EQUAL_UINT16(TAG_CONS, get_tag(make_cons(make_int(1), LISP_NIL)));
    TEST_ASSERT_EQUAL_UINT16(TAG_SYMBOL, get_tag(make_symbol("x")));
}

void test_symbol(void) {
    LispVal s = make_symbol("foo");
    TEST_ASSERT_EQUAL_UINT16(TAG_SYMBOL, TAG(s));
    TEST_ASSERT_EQUAL_STRING("foo", get_symbol(s));
}

void test_lisp_car_cdr(void) {
    LispVal pair = make_cons(make_int(10), make_int(20));
    TEST_ASSERT_EQUAL_INT32(10, get_int(lisp_car(pair)));
    TEST_ASSERT_EQUAL_INT32(20, get_int(lisp_cdr(pair)));
}

void test_gc_smoke(void) {
    // 100k allocations — GC should collect unreachable objects
    for (int i = 0; i < 100000; i++) {
        make_cons(make_int(i), LISP_NIL);
        make_symbol("test");
    }
    // if we got here without OOM, GC is working
    TEST_PASS();
}

int main(void) {
    GC_INIT();
    UNITY_BEGIN();
    RUN_TEST(test_make_int);
    RUN_TEST(test_int_zero);
    RUN_TEST(test_int_negative);
    RUN_TEST(test_int_min);
    RUN_TEST(test_int_max);
    RUN_TEST(test_nil_constant);
    RUN_TEST(test_bool_constants);
    RUN_TEST(test_cons_car_cdr);
    RUN_TEST(test_cons_list);
    RUN_TEST(test_is_truthy);
    RUN_TEST(test_arithmetic);
    RUN_TEST(test_comparisons);
    RUN_TEST(test_eq);
    RUN_TEST(test_eqv);
    RUN_TEST(test_equal);
    RUN_TEST(test_symbol);
    RUN_TEST(test_lisp_car_cdr);
    RUN_TEST(test_get_tag);
    RUN_TEST(test_gc_smoke);
    return UNITY_END();
}
