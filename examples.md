# Examples

This document demonstrates basic template conversions using **JJTemplate**.

All inputs and outputs are valid JSON.  
Templates may contain `definitions` (optional) and the main `template` object.

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
````

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

## Inner switch

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
    "message": "{{ concat 'Hello, ', .name | upper }}"
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

This section demonstrates how to use `formatDate`, `parseDate`, and `parseDateTime` functions.

---

### `formatDate`

Formats date or time objects into a string according to a provided pattern.  
Supports:

- `java.time.LocalDate`
- `java.time.LocalDateTime`
- `java.time.ZonedDateTime`
- `java.util.Date`

#### Template

```json
{
  "definitions": [
    {
      "localDate": "{{ parseDate 'dd.MM.yyyy', '28.10.2024' }}"
    }
  ],
  "template": {
    "formatted": "{{ formatDate 'dd/MM/yyyy', .localDate }}",
    "pipeFormatted": "{{ .localDate | formatDate 'yyyy-MM-dd' }}"
  }
}
````

#### Output

```json
{
  "formatted": "28/10/2024",
  "pipeFormatted": "2024-10-28"
}
```

---

### `parseDate`

Parses a **string** into a `java.time.LocalDate` using the given format.
Commonly used before formatting or as a definition variable.

#### Template

```json
{
  "template": {
    "parsed": "{{ parseDate 'dd.MM.yyyy', '05.12.2023' | str }}",
    "pipeParsed": "{{ '05.12.2023' | parseDate 'dd.MM.yyyy' | str }}"
  }
}
```

#### Output

```json
{
  "parsed": "2023-12-05",
  "pipeParsed": "2023-12-05"
}
```

> The `| str` is used here to convert `LocalDate` into a JSON string for readability.

---

### `parseDateTime`

Parses a **string with date and time** into a `java.time.LocalDateTime`.

#### Template

```json
{
  "template": {
    "parsed": "{{ parseDateTime 'dd.MM.yyyy HH:mm', '05.12.2023 14:30' | str }}",
    "pipeParsed": "{{ '05.12.2023 14:30' | parseDateTime 'dd.MM.yyyy HH:mm' | str }}"
  }
}
```

#### Output

```json
{
  "parsed": "2023-12-05T14:30:00",
  "pipeParsed": "2023-12-05T14:30:00"
}
```

---

### 💡 Notes

* `formatDate` accepts both Java `Date` and `TemporalAccessor` types (`LocalDate`, `LocalDateTime`, etc.).
* `parseDate` and `parseDateTime` always return **Java time objects**, not strings.
* Combine them with `| str` to safely render as plain JSON text.

---