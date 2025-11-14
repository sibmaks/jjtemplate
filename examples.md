# Examples

This document demonstrates basic template conversions using **JJTemplate**.

All inputs and outputs are valid JSON.

Templates may contain:

* optional `definitions`
* main `template` object

---

## Simple substitution

**Template:**

```json
{
  "template": {
    "greeting": "{{ 'Hello, World!' }}",
    "answer": "{{ 42 }}"
  }
}
```

**Output:**

```json
{
  "greeting": "Hello, World!",
  "answer": 42
}
```

---

## Variable substitution

**Template:**

```json
{
  "definitions": [
    {
      "userName": "Alice",
      "userAge": 30
    }
  ],
  "template": {
    "name": "{{ .userName }}",
    "age": "{{ .userAge }}"
  }
}
```

**Output:**

```json
{
  "name": "Alice",
  "age": 30
}
```

---

## Conditional insertion (`{{? expr}}`)

**Template:**

```json
{
  "template": {
    "values": [
      "{{? 'text' }}",
      "{{? null }}"
    ]
  }
}
```

**Output:**

```json
{
  "values": [
    "text"
  ]
}
```

---

## Spread operator (`{{. expr}}`)

**Template:**

```json
{
  "definitions": [
    {
      "extra": {
        "key2": "value2"
      }
    }
  ],
  "template": {
    "key1": "value1",
    "{{. .extra}}": true
  }
}
```

**Output:**

```json
{
  "key1": "value1",
  "key2": "value2"
}
```

---

## Range definition

**Template:**

```json
{
  "definitions": [
    {
      "listVar": [
        1,
        2,
        3
      ],
      "objects range item,index of .listVar": {
        "key-{{ .index }}": "{{ .item }}"
      }
    }
  ],
  "template": "{{ .objects }}"
}
```

**Output:**

```json
[
  {
    "key-0": 1
  },
  {
    "key-1": 2
  },
  {
    "key-2": 3
  }
]
```

---

## Switch definition

**Template:**

```json
{
  "definitions": [
    {
      "message switch .status": {
        "'ok'": "All good",
        "'fail'": "Something went wrong",
        "else": "Unknown"
      }
    }
  ],
  "template": "{{ .message }}"
}
```

**Context:**

```java
import java.util.Map;

var context = Map.of("status", "fail");
```

**Output:**

```json
{
  "message": "Something went wrong"
}
```

---

## Nested switch

**Template:**

```json
{
  "definitions": [
    {
      "booleanVar": false,
      "anotherBooleanVar": true
    },
    {
      "defResult switch .booleanVar": {
        "true": "fail-out",
        "false switch .anotherBooleanVar": {
          "true": "ok",
          "false": "fail-in"
        }
      }
    }
  ],
  "template": "{{ .defResult }}"
}
```

**Output:**

```json
"ok"
```

---

## Function + Pipe example

**Template:**

```json
{
  "definitions": [
    {
      "name": "world"
    }
  ],
  "template": {
    "message": "{{ 'Hello, ' | string:concat .name | string:upper }}"
  }
}
```

**Output:**

```json
{
  "message": "HELLO, WORLD"
}
```

---

## Date and Time Functions

Demonstrating `date:format`, `date:parse`, and `datetime:parse`.

---

### `date:format`

Formats date or time values using a given pattern.
Supports:

- `java.time.LocalDate`
- `java.time.LocalDateTime`
- `java.time.ZonedDateTime`
- `java.util.Date`

**Template:**

```json
{
  "definitions": [
    {
      "localDate": "{{ date:parse 'dd.MM.yyyy', '28.10.2024' }}"
    }
  ],
  "template": {
    "formatted": "{{ date:format 'dd/MM/yyyy', .localDate }}",
    "pipeFormatted": "{{ .localDate | date:format 'yyyy-MM-dd' }}"
  }
}
```

**Output:**

```json
{
  "formatted": "28/10/2024",
  "pipeFormatted": "2024-10-28"
}
```

---

### `date:parse`

Parses a **string** into a `java.time.LocalDate` using the given format.
Commonly used before formatting or as a definition variable.

**Template:**

```json
{
  "template": {
    "parsed": "{{ date:parse 'dd.MM.yyyy', '05.12.2023' | cast:str }}",
    "pipeParsed": "{{ '05.12.2023' | date:parse 'dd.MM.yyyy' | cast:str }}"
  }
}
```

**Output:**

```json
{
  "parsed": "2023-12-05",
  "pipeParsed": "2023-12-05"
}
```

> The `| cast:str` converts `LocalDate` into a JSON string for display.

---

### `datetime:parse`

Parses a **string with date and time** into a `java.time.LocalDateTime`.

**Template:**

```json
{
  "template": {
    "parsed": "{{ datetime:parse 'dd.MM.yyyy HH:mm', '05.12.2023 14:30' | cast:str }}",
    "pipeParsed": "{{ '05.12.2023 14:30' | datetime:parse 'dd.MM.yyyy HH:mm' | cast:str }}"
  }
}
```

**Output:**

```json
{
  "parsed": "2023-12-05T14:30:00",
  "pipeParsed": "2023-12-05T14:30:00"
}
```

---

## Map and Collapse

**Template:**

```json
{
  "definitions": [
    {
      "listVar": [
        1,
        2,
        3
      ],
      "objects range item,index of .listVar": {
        "key-{{ .index }}": "{{ .item }}"
      }
    }
  ],
  "template": "{{ .objects | map:collapse }}"
}
```

**Output:**

```json
{
  "key-0": 1,
  "key-1": 2,
  "key-2": 3
}
```

---

## Locale Functions

The `locale:new` function creates Java `Locale` instances.

### Syntax

```text
locale:new(language)
locale:new(language, country)
locale:new(language, country, variant)
```

or via pipe:

```text
'EN' | locale:new 'US'
'EN' | locale:new 'US', 'WIN'
```

---

### Example

**Template:**

```json
{
  "definitions": [
    {
      "simpleLocale": "{{ locale:new 'en' }}",
      "fullLocale": "{{ locale:new 'en', 'US' }}",
      "variantLocale": "{{ locale:new 'en', 'US', 'WIN' }}",
      "pipeLocale": "{{ 'fr' | locale:new 'FR' }}"
    }
  ],
  "template": {
    "simple": "{{ .simpleLocale | cast:str }}",
    "full": "{{ .fullLocale | cast:str }}",
    "variant": "{{ .variantLocale | cast:str }}",
    "pipe": "{{ .pipeLocale | cast:str }}"
  }
}
```

**Output:**

```json
{
  "simple": "en",
  "full": "en_US",
  "variant": "en_US_WIN",
  "pipe": "fr_FR"
}
```

---

### Used with `date:format`

**Template:**

```json
{
  "definitions": [
    {
      "localDate": "{{ date:parse 'dd.MM.yyyy', '05.12.2023' }}",
      "frLocale": "{{ locale:new 'fr', 'FR' }}"
    }
  ],
  "template": {
    "formattedDefault": "{{ date:format 'dd MMM yyyy', .localDate }}",
    "formattedFR": "{{ date:format .frLocale, 'dd MMM yyyy', .localDate }}"
  }
}
```

**Output:**

```json
{
  "formattedDefault": "05 Dec 2023",
  "formattedFR": "05 déc. 2023"
}
```

---

## Ternary Operator Examples

**Template:**

```json
{
  "definitions": [
    {
      "age": 20
    }
  ],
  "template": {
    "category": "{{ gt .age 18 ? 'adult' : 'minor' }}",
    "inline": "{{ .age | lt 10 ? 'child' : gt .age 60 ? 'senior' : 'adult' }}"
  }
}
```

**Output:**

```json
{
  "category": "adult",
  "inline": "adult"
}
```

> You can nest ternaries and use them inside pipes or namespace calls.

---

## List and Map Creation

**Template:**

```json
{
  "template": {
    "listExample": "{{ list:new 1, 2, 3 | list:concat [4,5] }}",
    "mapExample": "{{ map:new 'a', 1, 'b', 2 }}"
  }
}
```

**Output:**

```json
{
  "listExample": [
    1,
    2,
    3,
    4,
    5
  ],
  "mapExample": {
    "a": 1,
    "b": 2
  }
}
```

---

## Default Fallback

**Template:**

```json
{
  "definitions": [
    {
      "name": null
    }
  ],
  "template": {
    "safeName": "{{ default .name 'Anonymous' }}"
  }
}
```

**Output:**

```json
{
  "safeName": "Anonymous"
}
```