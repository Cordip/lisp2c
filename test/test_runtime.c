#include "unity.h"
#include "runtime.h"
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

void test_make_nil(void) {
    LispVal n = make_nil();
    TEST_ASSERT_EQUAL_UINT16(TAG_NIL, TAG(n));
}

void test_make_bool(void) {
    TEST_ASSERT_EQUAL_UINT16(TAG_BOOL_T, TAG(make_bool(1)));
    TEST_ASSERT_EQUAL_UINT16(TAG_BOOL_F, TAG(make_bool(0)));
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
                   make_cons(make_int(3), make_nil())));
    TEST_ASSERT_EQUAL_INT32(1, get_int(car(list)));
    TEST_ASSERT_EQUAL_INT32(2, get_int(car(cdr(list))));
    TEST_ASSERT_EQUAL_INT32(3, get_int(car(cdr(cdr(list)))));
    TEST_ASSERT_EQUAL_UINT16(TAG_NIL, TAG(cdr(cdr(cdr(list)))));
}

void test_is_truthy(void) {
    TEST_ASSERT_TRUE(is_truthy(make_int(1)));
    TEST_ASSERT_TRUE(is_truthy(make_int(0)));  /* Scheme: 0 is truthy */
    TEST_ASSERT_TRUE(is_truthy(make_bool(1)));
    TEST_ASSERT_FALSE(is_truthy(make_nil()));
    TEST_ASSERT_FALSE(is_truthy(make_bool(0)));
}

void test_arithmetic(void) {
    TEST_ASSERT_EQUAL_INT32(3, get_int(lisp_add(make_int(1), make_int(2))));
    TEST_ASSERT_EQUAL_INT32(2, get_int(lisp_sub(make_int(5), make_int(3))));
    TEST_ASSERT_EQUAL_INT32(6, get_int(lisp_mul(make_int(2), make_int(3))));
    TEST_ASSERT_EQUAL_INT32(-1, get_int(lisp_sub(make_int(1), make_int(2))));
}

void test_comparisons(void) {
    TEST_ASSERT_TRUE(is_truthy(lisp_eq(make_int(1), make_int(1))));
    TEST_ASSERT_FALSE(is_truthy(lisp_eq(make_int(1), make_int(2))));
    TEST_ASSERT_TRUE(is_truthy(lisp_lt(make_int(1), make_int(2))));
    TEST_ASSERT_FALSE(is_truthy(lisp_lt(make_int(2), make_int(1))));
    TEST_ASSERT_TRUE(is_truthy(lisp_gt(make_int(3), make_int(2))));
    TEST_ASSERT_FALSE(is_truthy(lisp_gt(make_int(1), make_int(2))));
    /* negative numbers — exercises sign extension in get_int */
    TEST_ASSERT_TRUE(is_truthy(lisp_lt(make_int(-1), make_int(0))));
    TEST_ASSERT_TRUE(is_truthy(lisp_gt(make_int(0), make_int(-1))));
    TEST_ASSERT_TRUE(is_truthy(lisp_eq(make_int(-1), make_int(-1))));
}

void test_get_tag(void) {
    TEST_ASSERT_EQUAL_UINT16(TAG_INT, get_tag(make_int(42)));
    TEST_ASSERT_EQUAL_UINT16(TAG_NIL, get_tag(make_nil()));
    TEST_ASSERT_EQUAL_UINT16(TAG_BOOL_T, get_tag(make_bool(1)));
    TEST_ASSERT_EQUAL_UINT16(TAG_CONS, get_tag(make_cons(make_int(1), make_nil())));
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

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_make_int);
    RUN_TEST(test_int_zero);
    RUN_TEST(test_int_negative);
    RUN_TEST(test_int_min);
    RUN_TEST(test_int_max);
    RUN_TEST(test_make_nil);
    RUN_TEST(test_make_bool);
    RUN_TEST(test_cons_car_cdr);
    RUN_TEST(test_cons_list);
    RUN_TEST(test_is_truthy);
    RUN_TEST(test_arithmetic);
    RUN_TEST(test_comparisons);
    RUN_TEST(test_symbol);
    RUN_TEST(test_lisp_car_cdr);
    RUN_TEST(test_get_tag);
    return UNITY_END();
}
