#pragma once

#include <stdint.h>

// Immediate types (0x00xx) — value in payload, no malloc
#define TAG_NIL 0x0000
#define TAG_BOOL_F 0x0001
#define TAG_BOOL_T 0x0002
#define TAG_INT 0x0003

// Heap types (0x01xx) — payload = 48-bit pointer
#define TAG_CONS 0x0100
#define TAG_SYMBOL 0x0101
#define TAG_CLOSURE 0x0102
#define TAG_STRING 0x0103
#define TAG_VECTOR 0x0104
#define TAG_ENV 0x0105
#define TAG_PORT 0x0106

// Special (0x02xx)
#define TAG_ERROR 0x0200

// Helpers
#define IS_IMMEDIATE(tag) (((tag) & 0xFF00) == 0x0000)
#define IS_HEAP(tag) (((tag) & 0xFF00) == 0x0100)
