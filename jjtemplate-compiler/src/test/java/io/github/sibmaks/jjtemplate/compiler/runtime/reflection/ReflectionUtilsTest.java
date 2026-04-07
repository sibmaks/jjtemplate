package io.github.sibmaks.jjtemplate.compiler.runtime.reflection;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sibmaks
 */
class ReflectionUtilsTest {

    @Test
    void getAllPropertiesOfMap() {
        var map = Map.of("a", 1, "b", 2);
        var props = ReflectionUtils.getAllProperties(map);
        assertEquals(2, props.size());
        assertEquals(1, props.get("a"));
        assertEquals(2, props.get("b"));
    }

    @Test
    void getAllPropertiesOfMapShouldIgnoreNonStringKeys() {
        var map = new LinkedHashMap<Object, Object>();
        map.put("a", 1);
        map.put(2, "ignored");

        var props = ReflectionUtils.getAllProperties(map);

        assertEquals(Map.of("a", 1), props);
    }

    @Test
    void getAllPropertiesOfList() {
        var list = List.of("x", "y");
        var props = ReflectionUtils.getAllProperties(list);
        assertEquals("x", props.get("0"));
        assertEquals("y", props.get("1"));
    }

    @Test
    void getAllPropertiesOfObject() {
        var person = new Person();
        var props = ReflectionUtils.getAllProperties(person);
        assertTrue(props.containsKey("age"));
        assertTrue(props.containsKey("active"));
        assertTrue(props.containsKey("name"));
    }

    @Test
    void getAllPropertiesShouldIgnoreFailingGetter() {
        var props = ReflectionUtils.getAllProperties(new BrokenProperty());
        assertFalse(props.containsKey("boom"));
    }

    @Test
    void getAllPropertiesOfNull() {
        var result = ReflectionUtils.getAllProperties(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPropertyFromMap() {
        var map = Map.of("x", 42);
        var actual = ReflectionUtils.getProperty(map, "x");
        assertEquals(42, actual);
    }

    @Test
    void getPropertyFromArrayByIndex() {
        var array = new String[]{"a", "b"};
        var actual = ReflectionUtils.getProperty(array, "0");
        assertEquals("a", actual);
    }

    @Test
    void getPropertyFromListByIndex() {
        var list = List.of("apple", "banana");
        var actual = ReflectionUtils.getProperty(list, "1");
        assertEquals("banana", actual);
    }

    @Test
    void getPropertyFromArrayOutOfRangeThrows() {
        var array = new String[]{"a"};
        var ex = assertThrows(TemplateEvalException.class, () -> ReflectionUtils.getProperty(array, "10"));
        assertEquals("Array index out of range: 10", ex.getMessage());
    }

    @Test
    void getPropertyFromStringByIndex() {
        var actual = ReflectionUtils.getProperty("hello", "1");
        assertEquals("e", actual);
    }

    @Test
    void getPropertyFromStringOutOfRangeThrows() {
        var ex = assertThrows(TemplateEvalException.class, () -> ReflectionUtils.getProperty("a", "10"));
        assertEquals("String index out of range: 10", ex.getMessage());
    }

    @Test
    void getPropertyFromObjectField() {
        var person = new Person();
        var actual = ReflectionUtils.getProperty(person, "name");
        assertEquals("John", actual);
    }

    @Test
    void getPropertyOutOfRangeThrows() {
        var list = List.of("a");
        var ex = assertThrows(TemplateEvalException.class, () -> ReflectionUtils.getProperty(list, "10"));
        assertEquals("List index out of range: 10", ex.getMessage());
    }

    @Test
    void getPropertyUnknownFieldThrows() {
        class Dummy {
        }
        var dummy = new Dummy();
        var ex = assertThrows(TemplateEvalException.class, () -> ReflectionUtils.getProperty(dummy, "unknown"));
        assertEquals("Unknown property 'unknown' of " + Dummy.class, ex.getMessage());
    }

    @Test
    void getPropertyNullReturnsNull() {
        assertNull(ReflectionUtils.getProperty(null, "any"));
    }

    @Test
    void getPropertyUsesFieldResolver() {
        var value = ReflectionUtils.getProperty(new CustomFieldResolver(), "name");
        assertEquals("field:name", value);
    }

    @Test
    void getPropertyUsesFieldFallbackResolverWhenDefaultLookupFails() {
        var value = ReflectionUtils.getProperty(new CustomFieldFallbackResolver(), "missing");
        assertEquals("fallback:missing", value);
    }

    @Test
    void getPropertyUsesFieldFallbackResolverForMapWhenKeyMissing() {
        var map = new FallbackMap();
        map.put("present", "value");

        var value = ReflectionUtils.getProperty(map, "missing");

        assertEquals("map-fallback:missing", value);
    }

    @Test
    void getPropertyReturnsNullForMapWhenKeyMissingAndNoFieldFallbackResolver() {
        var map = new HashMap<String, Object>();
        map.put("present", "value");

        var value = ReflectionUtils.getProperty(map, "missing");

        assertNull(value);
    }

    @Test
    void getPropertyWithResolvedPropertiesShouldFallbackWhenOwnerTypeDoesNotMatch() {
        var resolved = ReflectionUtils.resolveProperty(Person.class, "name").orElseThrow();

        var actual = ReflectionUtils.getProperty(Map.of("name", "Bob"), "name", List.of(resolved));

        assertEquals("Bob", actual);
    }

    @Test
    void getPropertyWithResolvedPropertiesShouldWrapAccessorFailure() {
        var resolved = ReflectionUtils.resolveProperty(BrokenProperty.class, "boom").orElseThrow();

        var ex = assertThrows(
                TemplateEvalException.class,
                () -> ReflectionUtils.getProperty(new BrokenProperty(), "boom", List.of(resolved))
        );

        assertEquals(
                "Failed to access property 'boom' of class io.github.sibmaks.jjtemplate.compiler.runtime.reflection.ReflectionUtilsTest$BrokenProperty",
                ex.getMessage()
        );
        assertNotNull(ex.getCause());
    }

    @Test
    void invokeSimpleMethod() {
        var p = new Person();
        var result = ReflectionUtils.invokeMethodReflective(p, "greet", List.of("Hi"));
        assertEquals("Hi John", result);
    }

    @Test
    void invokeNumericConversion() {
        var p = new Person();
        var result = ReflectionUtils.invokeMethodReflective(p, "addNumbers", List.of(5L, 2));
        assertEquals(7.0, result);
    }

    @Test
    void invokePrimitiveNumericConversion() {
        var p = new Person();
        var result = ReflectionUtils.invokeMethodReflective(p, "addNumbers", List.of(5, 2.0));
        assertEquals(7.0, result);
    }

    @Test
    void invokeVarargsMethod() {
        var p = new Person();
        var actual = ReflectionUtils.invokeMethodReflective(p, "collectVarargs", List.of("sum", 1, 2, 3));
        assertEquals(1 + 2 + 3, actual);
    }

    @Test
    void invokeEnumMethodWithString() {
        var p = new Person();
        ReflectionUtils.invokeMethodReflective(p, "setMode", List.of("ON"));
        assertEquals(Mode.ON, p.mode);
    }

    @Test
    void invokeEnumMethodWithUnknownEnumConstantThrows() {
        var p = new Person();
        var ex = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtils.invokeMethodReflective(p, "setMode", List.of("UNKNOWN"))
        );
        assertTrue(ex.getMessage().contains("No enum constant"));
    }

    @Test
    void invokeEnumMethodWithNull() {
        var p = new Person();
        var args = new ArrayList<>();
        args.add(null);
        ReflectionUtils.invokeMethodReflective(p, "setMode", args);
        assertNull(p.mode);
    }

    @Test
    void invokeMethodWithAssignable() {
        var p = new Person();
        var value = UUID.randomUUID().toString();
        var actual = ReflectionUtils.invokeMethodReflective(p, "callSupplier", List.of(new StubSupplier(value)));
        assertEquals(value, actual);
    }

    @Test
    void invokeMethodWithOptionalConversion() {
        class WithOptional {
            Optional<String> last;

            public void setLast(Optional<String> s) {
                this.last = s;
            }
        }
        var obj = new WithOptional();
        ReflectionUtils.invokeMethodReflective(obj, "setLast", List.of("value"));
        assertEquals(Optional.of("value"), obj.last);
    }

    @Test
    void invokeMethodNoSuchMethodThrows() {
        var p = new Person();
        var args = List.of();
        var ex = assertThrows(TemplateEvalException.class, () ->
                ReflectionUtils.invokeMethodReflective(p, "doesNotExist", args));
        assertTrue(ex.getMessage().contains("No matching method"));
    }

    @Test
    void invokeOnNullTargetThrows() {
        var args = List.of();
        var ex = assertThrows(TemplateEvalException.class, () ->
                ReflectionUtils.invokeMethodReflective(null, "x", args));
        assertTrue(ex.getMessage().contains("Cannot call method on null target"));
    }

    @Test
    void invokeMethodIncompatibleArgsThrows() {
        var p = new Person();
        var args = List.<Object>of(123);
        var ex = assertThrows(
                TemplateEvalException.class,
                () -> ReflectionUtils.invokeMethodReflective(p, "greet", args)
        );
        assertTrue(ex.getMessage().contains("No matching method"));
    }

    @Test
    void invokeMethodUsesMethodResolver() {
        var result = ReflectionUtils.invokeMethodReflective(new CustomMethodResolver(), "sum", List.of(1, 2, 3));
        assertEquals("sum:[1, 2, 3]", result);
    }

    @Test
    void invokeMethodUsesMethodFallbackResolverWhenDefaultLookupFails() {
        var result = ReflectionUtils.invokeMethodReflective(
                new CustomMethodFallbackResolver(),
                "missing",
                List.of("a", "b")
        );
        assertEquals("fallback:missing:[a, b]", result);
    }

    @Test
    void invokeMethodShouldWrapReflectiveInvocationFailure() {
        var ex = assertThrows(
                TemplateEvalException.class,
                () -> ReflectionUtils.invokeMethodReflective(new BrokenMethod(), "explode", List.of())
        );

        assertTrue(ex.getMessage().contains("Error invoking method explode"));
        assertNotNull(ex.getCause());
    }

    @Test
    void invokeMethodWithResolvedMethodsShouldFallbackWhenOwnerTypeDoesNotMatch() {
        var resolved = ReflectionUtils.resolveMethods(Person.class, "greet", List.of(String.class));

        var actual = ReflectionUtils.invokeMethodReflective("text", "substring", List.of(1), resolved);

        assertEquals("ext", actual);
    }

    @Test
    void invokeMethodWithResolvedMethodsShouldWrapInvocationFailure() {
        var resolved = ReflectionUtils.resolveMethods(BrokenMethod.class, "explode", List.of());

        var ex = assertThrows(
                TemplateEvalException.class,
                () -> ReflectionUtils.invokeMethodReflective(new BrokenMethod(), "explode", List.of(), resolved)
        );

        assertTrue(ex.getMessage().contains("Error invoking method explode"));
        assertNotNull(ex.getCause());
    }

    @Test
    void invokeMethodWithResolvedMethodsNullTargetThrows() {
        var resolved = ReflectionUtils.resolveMethods(Person.class, "greet", List.of(String.class));

        var ex = assertThrows(
                TemplateEvalException.class,
                () -> ReflectionUtils.invokeMethodReflective(null, "greet", List.of("Hi"), resolved)
        );

        assertEquals("Cannot call method on null target", ex.getMessage());
    }

    @Test
    void resolvePropertyReturnsEmptyForUnsupportedTypes() {
        assertTrue(ReflectionUtils.resolveProperty(null, "x").isEmpty());
        assertTrue(ReflectionUtils.resolveProperty(String[].class, "0").isEmpty());
        assertTrue(ReflectionUtils.resolveProperty(Map.class, "x").isEmpty());
        assertTrue(ReflectionUtils.resolveProperty(List.class, "0").isEmpty());
    }

    @Test
    void isSoftlyExtensibleShouldHandleFinalAndSpecialTypes() {
        assertTrue(ReflectionUtils.isSoftlyExtensible(Person.class));
        assertFalse(ReflectionUtils.isSoftlyExtensible(String.class));
        assertFalse(ReflectionUtils.isSoftlyExtensible(int.class));
        assertFalse(ReflectionUtils.isSoftlyExtensible(String[].class));
        assertFalse(ReflectionUtils.isSoftlyExtensible(Mode.class));
    }

    @Test
    void conversionSupportShouldHandleWrapDefaultAndFailures() throws NoSuchMethodException {
        assertEquals(Boolean.class, ReflectionConversionSupport.wrap(boolean.class));
        assertEquals(Character.class, ReflectionConversionSupport.wrap(char.class));
        assertEquals(void.class, ReflectionConversionSupport.wrap(void.class));
        assertNull(ReflectionConversionSupport.tryConvertArgs(new Class[]{String.class}, List.of(1)));

        var method = Person.class.getMethod("greet", String.class);
        var ex = assertThrows(
                TemplateEvalException.class,
                () -> ReflectionMethodSupport.invokeResolvedMethod(method, new Person(), List.of(1))
        );
        assertEquals("No matching method greet found for args [1]", ex.getMessage());
    }

    @Test
    void reflectionUtilsShouldResolveEnumConstantWrapper() throws Exception {
        var method = ReflectionUtils.class.getDeclaredMethod("resolveEnumConstant", Class.class, String.class);
        method.setAccessible(true);

        var result = method.invoke(null, Mode.class, "ON");

        assertEquals(Mode.ON, result);
    }

    @Test
    void methodSupportShouldRejectIncompatibleVarArgsDuringResolvedInvocation() throws NoSuchMethodException {
        var method = Person.class.getMethod("collectVarargs", String.class, Integer[].class);
        var ex = assertThrows(
                TemplateEvalException.class,
                () -> ReflectionMethodSupport.invokeResolvedMethod(method, new Person(), List.of(1))
        );
        assertEquals("No matching method collectVarargs found for args [1]", ex.getMessage());
    }

    @Test
    void methodSupportShouldInvokeResolvedVarArgsMethod() throws ReflectiveOperationException {
        var method = Person.class.getMethod("collectVarargs", String.class, Integer[].class);

        var result = ReflectionMethodSupport.invokeResolvedMethod(method, new Person(), List.of("sum", 1, 2, 3));

        assertEquals(6, result);
    }

    @Test
    void methodSupportShouldRejectResolvedVarArgsWithIncompatibleElementType() throws NoSuchMethodException {
        var method = Person.class.getMethod("collectVarargs", String.class, Integer[].class);
        var ex = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionMethodSupport.invokeResolvedMethod(method, new Person(), List.of("sum", "bad"))
        );
        assertNotNull(ex.getMessage());
    }

    @Test
    void methodSupportShouldResolveCompatibilityBranches() {
        assertFalse(ReflectionMethodSupport.resolveMethods(Person.class, "greet", Collections.singletonList(null)).isEmpty());
        assertFalse(ReflectionMethodSupport.resolveMethods(Person.class, "addNumbers", List.of(Long.class, Integer.class)).isEmpty());
        assertFalse(ReflectionMethodSupport.resolveMethods(Person.class, "collectVarargs", List.of(String.class, Integer.class)).isEmpty());
        assertTrue(ReflectionMethodSupport.resolveMethods(Person.class, "collectVarargs", List.of(String.class, String.class)).isEmpty());
        assertTrue(ReflectionMethodSupport.resolveMethods(Person.class, "collectVarargs", List.of()).isEmpty());
        assertTrue(ReflectionMethodSupport.resolveMethods(Person.class, "collectVarargs", List.of(Integer.class)).isEmpty());
    }

    @Test
    void resolveEnumConstantThrowsWhenTypeIsNotEnum() throws Exception {
        var method = ReflectionUtils.class.getDeclaredMethod("resolveEnumConstant", Class.class, String.class);
        method.setAccessible(true);

        var ex = assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(null, String.class, "ANY")
        );
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertEquals("class java.lang.String is not an enum type", ex.getCause().getMessage());
    }

    @ParameterizedTest
    @MethodSource("numericConversionCases")
    void invokeMethodCoversAllConvertNumberBranches(
            String methodName,
            Number input,
            Number expectedValue,
            Class<?> expectedType
    ) {
        var target = new NumericConversionTarget();
        var result = ReflectionUtils.invokeMethodReflective(target, methodName, List.of(input));

        assertEquals(expectedValue, result);
        assertEquals(expectedType, result.getClass());
    }

    enum Mode {
        OFF, ON
    }

    public static class Person {
        private final int age = 30;
        private final boolean active = true;
        public String name = "John";
        public List<Integer> scores = List.of(10, 20);
        private Mode mode = Mode.OFF;

        public int getAge() {
            return age;
        }

        public boolean isActive() {
            return active;
        }

        public String greet(String prefix) {
            return prefix + " " + name;
        }

        public double addNumbers(int a, double b) {
            return a + b;
        }

        public String callSupplier(Supplier<String> supplier) {
            return supplier.get();
        }

        public int collectVarargs(String label, Integer... nums) {
            if ("sum".equals(label)) {
                var sum = 0;
                for (Integer num : nums) {
                    sum += num;
                }
                return sum;
            }
            throw new IllegalArgumentException("Unknown label: " + label);
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }
    }


    static class StubSupplier implements Supplier<String> {
        private final String value;

        public StubSupplier(String value) {
            this.value = value;
        }

        @Override
        public String get() {
            return value;
        }
    }

    static class CustomFieldResolver implements FieldResolver {
        @Override
        public Object resolve(String fieldName) {
            return "field:" + fieldName;
        }
    }

    static class CustomFieldFallbackResolver implements FieldFallbackResolver {
        @Override
        public Object resolve(String fieldName) {
            return "fallback:" + fieldName;
        }
    }

    static class CustomMethodResolver implements MethodResolver {
        @Override
        public Object resolve(String methodName, Object[] args) {
            return methodName + ":" + Arrays.toString(args);
        }
    }

    static class CustomMethodFallbackResolver implements MethodFallbackResolver {
        @Override
        public Object resolve(String methodName, Object[] args) {
            return "fallback:" + methodName + ":" + Arrays.toString(args);
        }
    }

    public static class BrokenProperty {
        public String getBoom() {
            throw new IllegalStateException("boom");
        }
    }

    public static class BrokenMethod {
        public String explode() {
            throw new IllegalStateException("boom");
        }
    }

    static class FallbackMap extends HashMap<String, Object> implements FieldFallbackResolver {
        @Override
        public Object resolve(String fieldName) {
            return "map-fallback:" + fieldName;
        }
    }

    static Stream<Arguments> numericConversionCases() {
        return Stream.of(
                Arguments.of("asInt", 7L, 7, Integer.class),
                Arguments.of("asPrimitiveInt", 7L, 7, Integer.class),
                Arguments.of("asWrapperInt", 7L, 7, Integer.class),
                Arguments.of("asLong", 7, 7L, Long.class),
                Arguments.of("asPrimitiveLong", 7, 7L, Long.class),
                Arguments.of("asWrapperLong", 7, 7L, Long.class),
                Arguments.of("asDouble", 7, 7.0d, Double.class),
                Arguments.of("asPrimitiveDouble", 7, 7.0d, Double.class),
                Arguments.of("asWrapperDouble", 7, 7.0d, Double.class),
                Arguments.of("asFloat", 7, 7.0f, Float.class),
                Arguments.of("asPrimitiveFloat", 7, 7.0f, Float.class),
                Arguments.of("asWrapperFloat", 7, 7.0f, Float.class),
                Arguments.of("asShort", 7, (short) 7, Short.class),
                Arguments.of("asPrimitiveShort", 7, (short) 7, Short.class),
                Arguments.of("asWrapperShort", 7, (short) 7, Short.class),
                Arguments.of("asByte", 7, (byte) 7, Byte.class),
                Arguments.of("asPrimitiveByte", 7, (byte) 7, Byte.class),
                Arguments.of("asWrapperByte", 7, (byte) 7, Byte.class),
                Arguments.of("asNumber", 7, 7, Integer.class)
        );
    }

    static class NumericConversionTarget {
        public Integer asInt(int value) {
            return value;
        }

        public int asPrimitiveInt(int value) {
            return value;
        }

        public Integer asWrapperInt(Integer value) {
            return value;
        }

        public Long asLong(long value) {
            return value;
        }

        public long asPrimitiveLong(long value) {
            return value;
        }

        public Long asWrapperLong(Long value) {
            return value;
        }

        public Double asDouble(double value) {
            return value;
        }

        public double asPrimitiveDouble(double value) {
            return value;
        }

        public Double asWrapperDouble(Double value) {
            return value;
        }

        public Float asFloat(float value) {
            return value;
        }

        public float asPrimitiveFloat(float value) {
            return value;
        }

        public Float asWrapperFloat(Float value) {
            return value;
        }

        public Short asShort(short value) {
            return value;
        }

        public short asPrimitiveShort(short value) {
            return value;
        }

        public Short asWrapperShort(Short value) {
            return value;
        }

        public Byte asByte(byte value) {
            return value;
        }

        public byte asPrimitiveByte(byte value) {
            return value;
        }

        public Byte asWrapperByte(Byte value) {
            return value;
        }

        public Number asNumber(Number value) {
            return value;
        }
    }
}
