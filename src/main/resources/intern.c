#include "intern.h"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define INTERN_INITIAL_CAPACITY 64

typedef struct {
  const char **entries;
  uint32_t capacity;
  uint32_t size;
} InternTable;

static InternTable table = {NULL, 0, 0};

static uint32_t intern_hash(const char *s) {
  uint32_t h = 2166136261u;
  for (; *s; s++) {
    h ^= (uint8_t)*s;
    h *= 16777619u;
  }
  return h;
}

static void intern_grow(void) {
  uint32_t old_cap = table.capacity;
  const char **old = table.entries;
  uint32_t new_cap = old_cap == 0 ? INTERN_INITIAL_CAPACITY : old_cap * 2;
  table.entries = calloc(new_cap, sizeof(const char *));
  if (!table.entries) {
    fprintf(stderr, "out of memory\n");
    exit(1);
  }
  table.capacity = new_cap;
  table.size = 0;
  for (uint32_t i = 0; i < old_cap; i++) {
    if (old[i] != NULL) {
      uint32_t idx = intern_hash(old[i]) % new_cap;
      while (table.entries[idx] != NULL)
        idx = (idx + 1) % new_cap;
      table.entries[idx] = old[i];
      table.size++;
    }
  }
  free(old);
}

const char *intern_symbol(const char *name) {
  if (table.size * 2 >= table.capacity)
    intern_grow();
  uint32_t idx = intern_hash(name) % table.capacity;
  while (1) {
    if (table.entries[idx] == NULL) {
      table.entries[idx] = strdup(name);
      if (!table.entries[idx]) {
        fprintf(stderr, "out of memory\n");
        exit(1);
      }
      table.size++;
      return table.entries[idx];
    }
    if (strcmp(table.entries[idx], name) == 0)
      return table.entries[idx];
    idx = (idx + 1) % table.capacity;
  }
}
