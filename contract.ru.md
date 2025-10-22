# Формат входных данных

```json
{
  "definitions": [
  ],
  "template": ""
}
```

`definitions` - последовательность определений переменных, выполняемых до генерации шаблона, выполняется в указанном
порядке указания.
Если `definitions` содержит объект, то порядок указанных полей может не учитываться.

Для доступа к переменным используется префикс `.`, пример: `.varName`.

Можно делать цепочку вложенности, к примеру: `.parent.child.field`.
Если хотя бы одно из значений в цепочке `null`, то результат всего выражения `null`.

`template` - генерируемый шаблон, может следующего типа:

- статическое значение (без преобразований):
    - `"any string"` - строка
    - `42` - целое число
    - `3.14` - вещественное число
    - `true` / `false` - булевое значение
    - `{}` - пустой объект
    - `{"key": "value"}` - объект с содержимым
    - `[]` - пустой список
    - `[42, true, "text"]` - список с содержимым
- шаблонное значение

# Шаблонные значения

## Простая подстановка

Подстановка значения `as-is`, используется: `{{ <expression> }}`.
В случае если шаблонное выражение имеет вид: `"{{ <expression> }}"` - то есть занимает всю строку,
и является единственным шаблоном, тогда тип значения определяется результирующим типом выражения.

### Примеры

#### Статичные значения

##### Шаблон

```json
{
  "booleanTrue": "{{ true }}",
  "booleanFalse": "{{ false }}",
  "string": "{{ 'text' }}",
  "integer": "{{ 42 }}",
  "double": "{{ 3.1415 }}",
  "null": "{{ null }}",
  "array": [
    "{{ true }}",
    "{{ false }}",
    "{{ 'text' }}",
    "{{ 42 }}",
    "{{ 3.1415 }}",
    "{{ null }}"
  ],
  "key-{{ 'end' }}": "value"
}
```

##### Результат

```json
{
  "booleanTrue": true,
  "booleanFalse": false,
  "string": "text",
  "integer": 42,
  "double": 3.1415,
  "null": null,
  "array": [
    true,
    false,
    "text",
    42,
    3.1415,
    null
  ],
  "key-end": "value"
}
```

#### Значение из переменной

##### Шаблон

```json
{
  "booleanTrue": "{{ .booleanTrue }}",
  "booleanFalse": "{{ .booleanFalse }}",
  "string": "{{ .string }}",
  "integer": "{{ .integer }}",
  "double": "{{ .double }}",
  "null": "{{ .nullVar }}",
  "array": [
    "{{ .booleanTrue }}",
    "{{ .booleanFalse }}",
    "{{ .string }}",
    "{{ .integer }}",
    "{{ .double }}",
    "{{ .nullVar }}"
  ]
}
```

##### Контекст

```java
var context = Map.of(
        "booleanTrue", true,
        "booleanFalse", false,
        "string", "text",
        "integer", 42,
        "double", 3.1415,
        "nullVar", null
);
```

##### Результат

```json
{
  "booleanTrue": true,
  "booleanFalse": false,
  "string": "text",
  "integer": 42,
  "double": 3.1415,
  "null": null,
  "array": [
    true,
    false,
    "text",
    42,
    3.1415,
    null
  ]
}
```

## Условная подстановка в массив

Подстановка значения выражения в массив, если значение не `null`.

### Примеры

#### Статичные значения

##### Шаблон

```json
{
  "array": [
    "{{? true }}",
    "{{? false }}",
    "{{? 'text' }}",
    "{{? 42 }}",
    "{{? 3.1415 }}",
    "{{? null }}"
  ]
}
```

##### Результат

```json
{
  "array": [
    true,
    false,
    "text",
    42,
    3.1415
  ]
}
```

#### Значение из переменной

##### Шаблон

```json
{
  "array": [
    "{{? .booleanTrue }}",
    "{{? .booleanFalse }}",
    "{{? .string }}",
    "{{? .integer }}",
    "{{? .double }}",
    "{{? .nullVar }}"
  ]
}
```

##### Контекст

```java
var context = Map.of(
        "booleanTrue", true,
        "booleanFalse", false,
        "string", "text",
        "integer", 42,
        "double", 3.1415,
        "nullVar", null
);
```

##### Результат

```json
{
  "array": [
    true,
    false,
    "text",
    42,
    3.1415,
    null
  ]
}
```

## Разворачивание выражения в объекте или массиве

Содержимое выражение вставляется в родительский объект:

- для массива:
    - если значения выражения массив, то элементы массива добавляются в родительский массив
    - если значение выражения значение, то значение добавляется в родительский массив
- для объектов:
    - если значение выражения объект, то параметры из выражения добавляются,
      в том числе перезаписывая параметры родительского объекта.
    - иначе ошибка

### Примеры

#### Значение из переменной

##### Шаблон

```json
{
  "array": [
    "prefix",
    "{{. .varList}}"
  ],
  "object": {
    "key": "value",
    "{{. .varObject }}": true
  }
}
```

##### Контекст

```java
import java.util.Map;
import java.util.List;

var context = Map.of(
        "varList", List.of("text", true, 3.1415),
        "varObject", Map.of("key", "new-value")
);
```

##### Результат

```json
{
  "array": [
    "prefix",
    "text",
    true,
    3.1415
  ],
  "object": {
    "key": "new-value"
  }
}
```

# Поддерживаемые функции

Функция может быть вызвана в двух вариантах:

- прямой вызов: `<function> <args>`
  Вызов функции с передачей аргументов.

- вызов через pipe-оператор `|`: `<expression> | <function> <args>`
  Вызов функции с передачей левого выражения как последний аргумент в функцию.

Аргументы перечисляются через запятую.

С помощью оператора `|` можно последовательно вызывать функции: `<expression> | <expression> [ | <expression> ...]`.

## Преобразование в строку: `str`

Преобразование аргумента в строку. Если аргумент `null`, то результат `null`.

### Шаблон

```json
{
  "key": "{{ .value | str }}",
  "pipe1stArg": "{{ str .value }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", true);
```

### Ожидаемый вывод

```json
{
  "key": "true",
  "pipe1stArg": "true"
}
```

## Преобразование в целое число: `int`

Преобразование аргумента в целое числа.
Если аргумент `null`, то результат `null`.
Если аргумент не может быть преобразован, то генерируется ошибка.

### Шаблон

```json
{
  "key": "{{ .value | int }}",
  "pipe1stArg": "{{ int .value }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", "42");
```

### Ожидаемый вывод

```json
{
  "key": 42,
  "pipe1stArg": 42
}
```

## Преобразование в вещественное число: `double`

Преобразование аргумента в вещественное числа.
Если аргумент `null`, то результат `null`.
Если аргумент не может быть преобразован, то генерируется ошибка.

### Шаблон

```json
{
  "key": "{{ .value | double }}",
  "pipe1stArg": "{{ double .value }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", "3.1415");
```

### Ожидаемый вывод

```json
{
  "key": 3.1415,
  "pipe1stArg": 3.1415
}
```

## Преобразование строки в булевую: `boolean`

Преобразование аргумента в булевое значение.
Если аргумент `null`, то результат `null`.
Если аргумент не может быть преобразован, то генерируется ошибка.

### Шаблон

```json
{
  "key": "{{ .value | boolean }}",
  "pipe1stArg": "{{ boolean .value }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", "true");
```

### Ожидаемый вывод

```json
{
  "key": true,
  "pipe1stArg": true
}
```

## Длина строки: `len`

### Шаблон

```json
{
  "key": "{{ .value | len }}",
  "pipe1stArg": "{{ len .value }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", "1234");
```

### Ожидаемый вывод

```json
{
  "key": 4,
  "pipe1stArg": 4
}
```

## Проверка строки на пустоту: `empty`

### Шаблон

```json
{
  "key": "{{ .value | empty }}",
  "pipe1stArg": "{{ empty .value }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", "1234");
```

### Ожидаемый вывод

```json
{
  "key": false,
  "pipe1stArg": false
}
```

## Строка в верхнем регистре: `upper`

### Шаблон

```json
{
  "key": "{{ .value | upper }}",
  "pipe1stArg": "{{ upper .value }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", "hello");
```

### Ожидаемый вывод

```json
{
  "key": "HELLO",
  "pipe1stArg": "HELLO"
}
```

## Строка в нижнем регистре: `lower`

### Шаблон

```json
{
  "key": "{{ .value | lower }}",
  "pipe1stArg": "{{ lower .value }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", "HELLO");
```

### Ожидаемый вывод

```json
{
  "key": "hello",
  "pipe1stArg": "hello"
}
```

## Размер коллекции: `len`

### Шаблон

```json
{
  "key": "{{ .value | len }}",
  "pipe1stArg": "{{ len .value }}"
}
```

### Контекст

```java
import java.util.List;
import java.util.Map;

var context = Map.of("value", List.of("1", "2", "3", "4"));
```

### Ожидаемый вывод

```json
{
  "key": 4,
  "pipe1stArg": 4
}
```

## Проверка коллекции на пустоту: `empty`

### Шаблон

```json
{
  "key": "{{ .value | empty }}",
  "pipe1stArg": "{{ empty .value }}"
}
```

### Контекст

```java
import java.util.List;
import java.util.Map;

var context = Map.of("value", List.of("1", "2", "3", "4"));
```

### Ожидаемый вывод

```json
{
  "key": false,
  "pipe1stArg": false
}
```

## Булевое отрицание: `not`

### Шаблон

```json
{
  "key": "{{ .value | not }}",
  "pipe1stArg": "{{ not .value }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", true);
```

### Ожидаемый вывод

```json
{
  "key": false,
  "pipe1stArg": false
}
```

## Булевая операция равенства: `eq`

### Шаблон

```json
{
  "key": "{{ .value | eq 'text' }}",
  "pipe1stArg": "{{ eq .value, 'text' }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", "text");
```

### Ожидаемый вывод

```json
{
  "key": true,
  "pipe1stArg": true
}
```

## Булевая операция неравенства: `neq`

### Шаблон

```json
{
  "key": "{{ .value | neq 'text' }}",
  "pipe1stArg": "{{ neq .value, 'text' }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", "text");
```

### Ожидаемый вывод

```json
{
  "key": false,
  "pipe1stArg": false
}
```

## Булевая операция меньше '<': `lt`

### Шаблон

```json
{
  "key": "{{ .value | lt 0 }}",
  "pipe1stArg": "{{ lt .value, 0 }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", 10);
```

### Ожидаемый вывод

```json
{
  "key": false,
  "pipe1stArg": false
}
```

## Булевая операция меньше или равно '<=': `le`

### Шаблон

```json
{
  "key": "{{ .value | le 0 }}",
  "pipe1stArg": "{{ le .value, 0 }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", 10);
```

### Ожидаемый вывод

```json
{
  "key": false,
  "pipe1stArg": false
}
```

## Булевая операция больше '>': `gt`

### Шаблон

```json
{
  "key": "{{ .value | gt 0 }}",
  "pipe1stArg": "{{ gt .value, 0 }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", 10);
```

### Ожидаемый вывод

```json
{
  "key": true,
  "pipe1stArg": true
}
```

## Булевая операция больше или равно '>=': `ge`

### Шаблон

```json
{
  "key": "{{ .value | ge 0 }}",
  "pipe1stArg": "{{ ge .value, 0 }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", 10);
```

### Ожидаемый вывод

```json
{
  "key": true,
  "pipe1stArg": true
}
```

## Булевая операция И '&': `and`

### Шаблон

```json
{
  "key": "{{ .v1 | and .v2 }}",
  "pipe1stArg": "{{ and .v1, .v2 }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("v1", true, "v2", false);
```

### Ожидаемый вывод

```json
{
  "key": false,
  "pipe1stArg": false
}
```

## Булевая операция И '|': `or`

### Шаблон

```json
{
  "key": "{{ .v1 | or .v2 }}",
  "pipe1stArg": "{{ or .v1, .v2 }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("v1", true, "v2", false);
```

### Ожидаемый вывод

```json
{
  "key": true,
  "pipe1stArg": true
}
```

## Создание списка: `list`

### Шаблон

```json
{
  "key": "{{ list 'a', true, .var1 }}",
  "pipe1stArg": "{{ 'a' | list true, .var1 }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("var1", 42);
```

### Ожидаемый вывод

```json
{
  "key": [
    "a",
    true,
    42
  ],
  "pipe1stArg": [
    "a",
    true,
    42
  ]
}
```

## Конкатенация объектов в строку: `concat`

Если первый аргумент не коллекция, то результат строка.

### Шаблон

```json
{
  "key": "{{ concat 'a', true, .var1 }}",
  "pipe1stArg": "{{ 'a' | concat true, .var1 }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("var1", 42);
```

### Ожидаемый вывод

```json
{
  "key": "atrue42",
  "pipe1stArg": "atrue42"
}
```

## Объединение списков: `concat`

Если первый аргумент коллекция, то результат коллекция.

### Шаблон

```json
{
  "key": "{{ concat .var1, 'a', 123 }}",
  "pipe1stArg": "{{ .var1 | concat 123, 'a' }}"
}
```

### Контекст

```java
import java.util.List;
import java.util.Map;

var context = Map.of("var1", List.of(true));
```

### Ожидаемый вывод

```json
{
  "key": [
    true,
    "a",
    123
  ],
  "pipe1stArg": [
    true,
    "a",
    123
  ]
}
```

## Значение по умолчанию: `default`

Если первый аргумент `null`, то выводится 2-ой аргумент, иначе 1-ый

### Шаблон

```json
{
  "key": "{{ default .var1, 'a' }}",
  "nKey": "{{ default .var2, 'a' }}",
  "pipe1stArg": "{{ .var1 | default 123 }}",
  "nPipe1stArg": "{{ .var2 | default 'a' }}",
  "array": [
    "{{ default .var1, 'a' }}",
    "{{ default .var2, 'a' }}",
    "{{ .var1 | default 123 }}",
    "{{ .var2 | default 'a' }}"
  ]
}
```

### Контекст

```java
import java.util.List;
import java.util.Map;

var context = Map.of("var1", null, "var2", "text");
```

### Ожидаемый вывод

```json
{
  "key": "a",
  "nKey": "text",
  "pipe1stArg": 123,
  "nPipe1stArg": "text",
  "array": [
    "a",
    "text",
    123,
    "text"
  ]
}
```

## Схлопывание списка объектов: `collapse`

### Шаблон

```json
{
  "result": "{{ collapse .varList }}",
  "resultPipe": "{{ .varList | collapse }}"
}
```

### Контекст

```java
import java.util.List;
import java.util.Map;

var context = Map.of("varList", List.of(
        Map.of("key", "value"),
        Map.of("key", "value-2", "key2", "value-3")
));
```

### Ожидаемый вывод

```json
{
  "result": {
    "key": "value-2",
    "key2": "value-3"
  }
}
```

## Пример цепочки операций

### Шаблон

```json
{
  "key": "{{ .var1 | concat 'rue' | boolean }}"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("var1", 't');
```

### Ожидаемый вывод

```json
{
  "key": true
}
```

# Определение переменных

Блок `definitions` - это список объектов, может быть пустым или отсутствовать.

Элемент списка (JSON объект) - `definition` содержит определения переменных.

Каждый ключ в `definition` определяет переменную одним из следующих вариантов:

- явное определение переменной
- условное определение переменной
- определение переменной массива

## Явное определение переменной

Если ключ `definition` начинается с символа `a-zA-Z` и далее содержит только знаки: `a-zA-Z0-9`,
то это является явным определением переменной. Значение может быть как статичным JSON-ом, так и шаблоном.

### Пример статичных значений

```json
{
  "definitions": [
    {
      "textVar": "textValue",
      "intVar": 42,
      "doubleVar": 3.1415,
      "booleanVar": true,
      "arrayVar": [
        "text",
        42,
        3.1415,
        true,
        {
          "key": "value"
        }
      ],
      "objectVar": {
        "key": "value"
      }
    }
  ]
}
```

### Пример шаблонных значений

```json
{
  "definitions": [
    {
      "textVar": "{{ 'textValue' }}",
      "intVar": "{{ 42 }}",
      "doubleVar": "{{ 3.1415 }}",
      "booleanVar": "{{ true }}",
      "arrayVar": [
        "{{ 'text' }}",
        "{{ 42 }}",
        "{{ 3.1415 }}",
        "{{ true }}",
        {
          "key": "{{ 'value' }}"
        }
      ],
      "objectVar": {
        "key": "{{ 'value' }}"
      }
    }
  ]
}
```

## Условное определение переменной

Если ключ `definition` начинается с явного определения переменной за которым следует конструкция: `case <expression>`,
то данное определение является условным.
В данном случае вычисляется результат `<expression>` и переменной присваивается значение в зависимости от описанных
условий.
Если ни одно из условий не выполняется, то переменная не создаётся.

Поддерживаемые типы условий:

- констант:
    - строки: `'text'`
    - числа: `42`, `3.1415`
    - булевые: `true`, `false`
- выражения
    - значение переменной `.someVar`
    - значение функции или `pipe`, пример: `upper .someVar`
- `then` - строгое равенство `<expression>` значению `true`
- `else` - выполняется если ни одно из условий ранее не было выполнено

### Пример

```json
{
  "definitions": [
    {
      "varResult case .testVariable": {
        "42": "text",
        "true": "text-2",
        "'text'": "text-3",
        ".varString": "text-4",
        "else": "text-5"
      }
    }
  ]
}
```

## Определение переменной массива

Если ключ `definition` начинается с явного определения переменной за которым следует конструкция:
`range <item variable>,<index variable> <expression>`,
то данное определение является определением массива, где:

- `<item variable>` - определение имени переменной значения из коллекции в контексте создаваемого массива.
- `<index variable>` - определение имени переменной порядково номера значения из коллекции в контексте создаваемого
  массива.
- `<expression>` - выражение, результат которого должен быть итерируемая коллекция.

Если коллекция пустая, то создаётся пустой массив.
Если коллекция равна `null`, то создаётся переменная со значением `null`.

### Пример

```json
{
  "definitions": [
    {
      "varObject2 range item,index of .listOfNodes": {
        "key-{{ .index }}": "{{ .item }}"
      },
      "varObject3 range item,index of .listOfNodes": "{{ .item | str }}"
    }
  ]
}
```
