# JJTemplate

[English](README.md) | [Русский](README.ru.md)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sibmaks_jjtemplate&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sibmaks_jjtemplate)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=sibmaks_jjtemplate&metric=bugs)](https://sonarcloud.io/summary/new_code?id=sibmaks_jjtemplate)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=sibmaks_jjtemplate&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=sibmaks_jjtemplate)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sibmaks_jjtemplate&metric=coverage)](https://sonarcloud.io/summary/new_code?id=sibmaks_jjtemplate)

**JJTemplate** - это легковесный шаблонизатор, ориентированный на **минимальное время рендеринга** и **JSON-совместимый
вход/выход**. Он компилирует шаблоны в оптимизированные абстрактные синтаксические деревья (AST) для быстрого
выполнения,
гарантируя валидный JSON в результате.

Плагин IDEA: [здесь](https://github.com/sibmaks/jjtemplate-plugin).

## Использование

### Maven

```xml

<dependency>
    <groupId>io.github.sibmaks.jjtemplate</groupId>
    <artifactId>jjtemplate</artifactId>
    <version>0.5.0</version>
    <type>pom</type>
</dependency>
```

### Gradle

```kotlin
implementation("io.github.sibmaks.jjtemplate:jjtemplate:0.5.0")
```

## Пример использования

1. **Скомпилировать шаблон**
   ```java
   var compiler = TemplateCompiler.getInstance();
   var script = new TemplateScript(...);
   var compiled = compiler.compile(script);
   ```
2. Отрендерить с данными
    ```java
    var result = compiled.render(Map.of("name", "Alice"));
    ```

## Формат шаблона

Шаблоны пишутся в чистом JSON со встроенными выражениями в двойных фигурных скобках:

```json
{
  "definitions": [
    {
      "greeting": "{{ string:concat 'Hello, ', .name }}"
    }
  ],
  "template": {
    "message": "{{ .greeting }}"
  }
}
```

### Типы выражений

- `.varName` - доступ к значениям переменных
- `{{ expression }}` - прямая подстановка выражения
- `{{? expression }}` - условная вставка (пропускается, если `null`)
- `{{. expression }}` - разворачивание значений в массивы или объекты

Поддерживаются выражения, pipe-вызовы (`|`), тернарный оператор (`?`, `:`), разворот аргументов функций (`...`).

## Основные концепции

### Переменные и доступ

- `.varName` - доступ к значениям переменных из контекста
- Поддерживается вложенный доступ к объектам (например, `.user.profile.name`)

Определения переменных: статические, условные (`switch`) и диапазонные (`range`)

## Встроенные функции

Функции организованы по **пространствам имен** в зависимости от типа или назначения.
Синтаксис вызова использует двоеточие (`:`), например `{{ cast:str .value }}` или `{{ .text | string:upper }}`.

---

### `cast` - Преобразование типов

* `cast:str(value)` - преобразовать в строку
* `cast:int(value)` - преобразовать в целое (`BigInteger`)
* `cast:float(value)` - преобразовать в десятичное число (`BigDecimal`)
* `cast:boolean(value)` - преобразовать в логический тип

---

### `string` - Строковые операции

* `string:concat(base, ...values)` - конкатенация строк
* `string:join(glue, ...values)` - склеивание строк с разделителем
* `string:joinNotEmpty(glue, ...values)` - склеивание строк с разделителем, пропуская `null` и пустые значения
* `string:len(string)` - длина строки
* `string:empty(string)` - проверка на пустоту или `null`
* `string:contains(string, ...substrings)` - проверка, что все подстроки присутствуют в строке
* `string:format([locale], pattern, ...args)` - форматирование строки (как `String.format`)
* `string:lower([locale], value)` - в нижний регистр
* `string:upper([locale], value)` - в верхний регистр
* `string:trim(value)` - удалить пробелы в начале и конце
* `string:split(value, regex, [limit])` - разделить строку по регулярному выражению.
* `string:indexOf(value, str)` - вернуть индекс первого вхождения указанной подстроки.
* `string:lastIndexOf(value, str)` - вернуть индекс последнего вхождения указанной подстроки.
* `string:substr(value, beginIndex, [endIndex])` - вернуть подстроку. Поддерживаются отрицательные индексы.
* `string:replace(value, target, replacement)` - заменить каждое вхождение литеральной подстроки на другую.
* `string:replaceAll(value, regex, replacement)` - заменить каждое вхождение по регулярному выражению.
  Подстрока начинается с `beginIndex` и продолжается до символа с индексом `endIndex - 1`.

---

### `list` - Операции со списками/массивами

* `list:new(...items)` - создать список
* `list:concat(...lists)` - объединить несколько списков или массивов
* `list:len(list)` - получить размер
* `list:empty(list)` - проверить на пустоту
* `list:contains(list, ...values)` - проверить, что список содержит все значения
* `list:head(list)` - получить первый элемент списка или `null`
* `list:tail(list)` - получить хвост списка или пустой список
* `list:join(glue, ...lists)` - объединить все списки в одну строку

---

### `map` - Операции с map/объектами

* `map:new(key, value, ...)` - создать map
* `map:len(map)` - получить количество записей
* `map:empty(map)` - проверить map на пустоту
* `map:contains(map, ...keys)` - проверить, что все ключи существуют
* `map:collapse(object|array|collection)` - объединить свойства объекта в одну map

---

### `date` - Утилиты для дат

* `date:format([locale], pattern, date)` - форматировать дату (`Date`, `LocalDate`, `LocalDateTime`,
  `ZonedLocalDateTime`)
* `date:parse(pattern, string)` - распарсить строку в `LocalDate`
* `date:now()` - получить текущий `LocalDate`

---

### `datetime` - Утилиты для даты и времени

* `datetime:parse(pattern, string)` - распарсить строку в `LocalDateTime`
* `datetime:now()` - получить текущий `LocalDateTime`

---

### `locale` - Утилиты локали

* `locale:new(language[, country[, variant]])` - создать экземпляр `Locale`

---

### `numberFormat` - Утилиты форматирования чисел

* `numberFormat:new(locale[, settings])` - создать экземпляр `NumberFormat` для указанной `Locale`
  и необязательной `Map` настроек.
  Поддерживаемые ключи `settings`:

- `style` (`number|integer|currency|percent`),
- `groupingUsed`,
- `parseIntegerOnly`,
- `maximumIntegerDigits`,
- `minimumIntegerDigits`,
- `maximumFractionDigits`,
- `minimumFractionDigits`,
- `currency`,
- `roundingMode`.

---

### `math` - Математические операции

* `math:neg(value)` - сменить знак числового значения
* `math:sum(left, right)` - сумма двух чисел
* `math:sub(left, right)` - разность двух чисел
* `math:mul(left, right)` - произведение двух чисел
* `math:div(left, right, [mode])` - деление двух чисел со scale, указанным через `mode`.
* `math:scale(value, amount, mode)` - вернуть `float` с заданным scale; значение рассчитывается умножением или делением
  не масштабированного значения на степень десяти.

---

### `default`

* `default(value, fallback)` - вернуть `fallback`, если значение равно `null`

---

### Логические операторы и операторы сравнения

(Остаются **глобальными**, без пространства имен.)

* `not(value)` - инверсия булевого значения
* `eq(a, b)`, `neq(a, b)` - проверка равенства
* `lt(a, b)`, `le(a, b)`, `gt(a, b)`, `ge(a, b)` - сравнения
* `and(a, b)`, `or(a, b)`, `xor(a, b)` - логические операции

---

### Примечания

* Все функции можно использовать **в pipe-форме**, например:

  ```json
  { "upperName": "{{ .name | string:upper }}" }
  ```
* Разделение по пространствам имен предотвращает конфликты имен и улучшает читаемость.

---

### `?` Условные выражения (тернарный оператор)

JJTemplate поддерживает встроенные условные выражения с тернарным оператором:

```text
condition ? valueIfTrue : valueIfFalse
```

Оператор вычисляет условие и возвращает одно из двух значений:

* Если условие **истинно**, возвращается выражение перед двоеточием (`:`).
* Если условие **ложно** или `null`, возвращается выражение после двоеточия.

#### Пример

```json
{
  "status": "{{ eq .ge 18 ? 'adult' : 'minor' }}"
}
```

Если `.age >= 18`, результат будет:

```json
{
  "status": "adult"
}
```

Иначе:

```json
{
  "status": "minor"
}
```

---

#### Выражения внутри тернарного оператора

И `condition`, и результаты (`valueIfTrue` / `valueIfFalse`)
могут содержать **любые выражения**, включая вызовы функций и pipe:

```json
{
  "greeting": "{{ .isMorning ? string:upper 'good morning' : string:upper 'good evening' }}"
}
```

или с pipe-синтаксисом:

```json
{
  "formatted": "{{ .amount | gt 1000 ? 'large' : 'small' }}"
}
```

---

#### Вложенность

Тернарные выражения можно **вкладывать** для компактной логики:

```json
{
  "label": "{{ eq .type 'a' ? 'Alpha' : eq .type 'b' ? 'Beta' : 'Other' }}"
}
```

---

Больше примеров [здесь](examples.md).

## Архитектура

JJTemplate построен на модульной архитектуре:

- **Lexer** - токенизирует строки шаблона
- **Parser** - строит AST из токенов
- **Compiler** - генерирует исполняемые деревья узлов
    - **Optimizer** - применяет оптимизации производительности
    - **Runtime** - выполняет шаблоны и формирует результат

## Цели

- **Минимальное время рендеринга** за счет оптимизации AST
- **Четкое разделение** парсинга, компиляции и выполнения
- **Предсказуемый результат** с гарантиями JSON-совместимости
- **Оптимизированная производительность** на каждом этапе обработки
