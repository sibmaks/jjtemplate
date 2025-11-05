package io.github.sibmaks.jjtemplate.evaluator.reflection;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    void getPropertyFromStringByIndex() {
        var actual = ReflectionUtils.getProperty("hello", "1");
        assertEquals("e", actual);
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
        assertTrue(ex.getMessage().startsWith("List index out of range"));
    }

    @Test
    void getPropertyUnknownFieldThrows() {
        class Dummy {
        }
        var ex = assertThrows(TemplateEvalException.class, () ->
                ReflectionUtils.getProperty(new Dummy(), "unknown"));
        assertEquals("Unknown property 'unknown' of " + Dummy.class, ex.getMessage());
    }

    @Test
    void getPropertyNullReturnsNull() {
        assertNull(ReflectionUtils.getProperty(null, "any"));
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
        var ex = assertThrows(TemplateEvalException.class, () ->
                ReflectionUtils.invokeMethodReflective(p, "doesNotExist", List.of()));
        assertTrue(ex.getMessage().contains("No matching method"));
    }

    @Test
    void invokeOnNullTargetThrows() {
        var ex = assertThrows(TemplateEvalException.class, () ->
                ReflectionUtils.invokeMethodReflective(null, "x", List.of()));
        assertTrue(ex.getMessage().contains("Cannot call method on null target"));
    }

    @Test
    void invokeMethodIncompatibleArgsThrows() {
        var p = new Person();
        var ex = assertThrows(TemplateEvalException.class, () ->
                ReflectionUtils.invokeMethodReflective(p, "greet", List.of(123)));
        assertTrue(ex.getMessage().contains("No matching method"));
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

}
