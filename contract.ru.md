# Общие правила

Обращение к переменной всегда начинается со знака `.`.

Можно делать цепочку вложенности, к примеру: `.parent.child.field`.
Если хотя бы одно из значений в цепочке `null`, то результат всего выражения `null`.

pipe `|` всегда передаёт результат левого вычисления как 1-ый аргумент в функцию справа.

# Операции с ключами объекта

## Подстановка данных из контекста в ключ объекта

### Шаблон

```json
{
  "{{ .key }}": "value"
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("key", "placeholder");
```

### Ожидаемый вывод

```json
{
  "placeholder": "value"
}
```

# Операции со строками

Если строка состоит только из `"{{ <expression> }}"`, то значение определяется результатом выражения.

## Подстановка строки из контекста в строку

### Шаблон

```json
{
  "key": "{{ .value }}",
  "array": [
    "{{ .value }}"
  ]
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", "placeholder");
```

### Ожидаемый вывод

```json
{
  "key": "placeholder",
  "array": [
    "placeholder"
  ]
}
```

## Подстановка целого числа из контекста в строку

### Шаблон

```json
{
  "key": "{{ .value }}",
  "array": [
    "{{ .value }}"
  ]
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", 42);
```

### Ожидаемый вывод

```json
{
  "key": 42,
  "array": [
    42
  ]
}
```

## Подстановка вещественного числа из контекста в строку

### Шаблон

```json
{
  "key": "{{ .value }}",
  "array": [
    "{{ .value }}"
  ]
}
```

### Контекст

```java
import java.util.Map;

var context = Map.of("value", 3.1415);
```

### Ожидаемый вывод

```json
{
  "key": 3.1415,
  "array": [
    3.1415
  ]
}
```

## Подстановка булевого значения из контекста в строку

### Шаблон

```json
{
  "key": "{{ .value }}",
  "array": [
    "{{ .value }}"
  ]
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
  "key": true,
  "array": [
    true
  ]
}
```

# Поддерживаемые функции

С помощью оператора `|` можно последовательно вызывать функции, то что слева является 1-ым аргументом функции.

Можно строить цепочки из операций.

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
  "key": "{{ .value | empty }}"
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
  "key": "{{ not .value }}"
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
  "pipe1stArg": "{{ eq .value 'text' }}"
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
  "pipe1stArg": "{{ neq .value 'text' }}"
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
  "pipe1stArg": "{{ lt .value 0 }}"
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
  "pipe1stArg": "{{ le .value 0 }}"
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
  "pipe1stArg": "{{ gt .value 0 }}"
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
  "pipe1stArg": "{{ ge .value 0 }}"
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
  "pipe1stArg": "{{ and .v1 .v2 }}"
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
  "pipe1stArg": "{{ or .v1 .v2 }}"
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
  "key": "{{ list 'a' true .var1 }}",
  "pipe1stArg": "{{ 'a' | list true .var1 }}"
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
  "key": "{{ concat 'a' true .var1 }}",
  "pipe1stArg": "{{ 'a' | concat true .var1 }}"
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
  "key": "{{ concat .var1 'a' 123 }}",
  "pipe1stArg": "{{ .var1 | concat 123 'a' }}"
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

## Запись по условию: `optional`

Если первый аргумент истина, то выводится 2-ой аргумент, иначе:

- для массива не добавляется элемент
- для поля объекта не добавляется поле

### Шаблон

```json
{
  "key": "{{ optional .var1 'a' }}",
  "nKey": "{{ optional (.var1 | not) 'a' }}",
  "pipe1stArg": "{{ .var1 | optional 123 }}",
  "nPipe1stArg": "{{ (.var1 | not) | optional 'a' }}",
  "array": [
    "{{ optional .var1 'a' }}",
    "{{ optional (.var1 | not) 'a' }}",
    "{{ .var1 | optional 123 }}",
    "{{ (.var1 | not) | optional 'a' }}"
  ]
}
```

### Контекст

```java
import java.util.List;
import java.util.Map;

var context = Map.of("var1", true);
```

### Ожидаемый вывод

```json
{
  "key": "a",
  "pipe1stArg": 123,
  "array": [
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
  "key": "{{ default .var1 'a' }}",
  "nKey": "{{ default .var2 'a' }}",
  "pipe1stArg": "{{ .var1 | default 123 }}",
  "nPipe1stArg": "{{ .var2 | default 'a' }}",
  "array": [
    "{{ default .var1 'a' }}",
    "{{ default .var2 'a' }}",
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
