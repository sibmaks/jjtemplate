package io.github.sibmaks.jjtemplate.compiler.benchmark;

import io.github.sibmaks.jjtemplate.compiler.api.MapTemplateCompileContext;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;

import java.util.List;
import java.util.Map;

/**
 * Creates DTO and Map fixtures that exercise distinct type-binding paths.
 */
public final class TypeBindingFixtures {
    private TypeBindingFixtures() {
        // Prevents instantiation of this utility class.
    }

    /**
     * Builds a type-binding scenario.
     *
     * @param scenario scenario name
     * @return type-binding case
     */
    public static TypeBindingCase create(TypeBindingScenario scenario) {
        switch (scenario) {
            case DTO_PROPERTY:
                return dtoProperty();
            case DTO_METHOD:
                return dtoMethod();
            case POLYMORPHIC_PROPERTY:
                return polymorphicProperty();
            case MAP_FALLBACK:
                return mapFallback();
            default:
                throw new IllegalArgumentException("Unknown type-binding scenario: " + scenario);
        }
    }

    private static TypeBindingCase dtoProperty() {
        var person = new Person("Alice", new Address("Moscow"));
        return new TypeBindingCase(
                script("{{ .person.address.city }}"),
                Map.of("person", person),
                new MapTemplateCompileContext(Map.of("person", List.of(Person.class))),
                "Moscow",
                true
        );
    }

    private static TypeBindingCase dtoMethod() {
        var person = new Person("Alice", new Address("Moscow"));
        return new TypeBindingCase(
                script("{{ .person.displayName() }}"),
                Map.of("person", person),
                new MapTemplateCompileContext(Map.of("person", List.of(Person.class))),
                "Alice",
                true
        );
    }

    private static TypeBindingCase polymorphicProperty() {
        var person = new VipPerson("Alice", new Address("Moscow"));
        return new TypeBindingCase(
                script("{{ .person.address.city }}"),
                Map.of("person", person),
                new MapTemplateCompileContext(Map.of(
                        "person",
                        List.of(Person.class, VipPerson.class)
                )),
                "Moscow",
                true
        );
    }

    private static TypeBindingCase mapFallback() {
        var person = Map.<String, Object>of(
                "address",
                Map.<String, Object>of("city", "Moscow")
        );
        return new TypeBindingCase(
                script("{{ .person.address.city }}"),
                Map.of("person", person),
                new MapTemplateCompileContext(Map.of("person", List.of(Map.class))),
                "Moscow",
                false
        );
    }

    private static TemplateScript script(String template) {
        return TemplateScript.builder().template(template).build();
    }

    /**
     * Address DTO used to create a bound property chain.
     */
    public static final class Address {
        private final String city;

        /**
         * Creates an address.
         *
         * @param city city name
         */
        public Address(String city) {
            this.city = city;
        }

        /**
         * Returns the city.
         *
         * @return city name
         */
        public String getCity() {
            return city;
        }
    }

    /**
     * Person DTO used for bound properties and methods.
     */
    public static class Person {
        private final String name;
        private final Address address;

        /**
         * Creates a person.
         *
         * @param name display name
         * @param address address value
         */
        public Person(String name, Address address) {
            this.name = name;
            this.address = address;
        }

        /**
         * Returns the address.
         *
         * @return address value
         */
        public Address getAddress() {
            return address;
        }

        /**
         * Returns the display name through a method call.
         *
         * @return display name
         */
        public String displayName() {
            return name;
        }
    }

    /**
     * Second receiver type used to measure polymorphic type binding.
     */
    public static final class VipPerson extends Person {
        /**
         * Creates a VIP person.
         *
         * @param name display name
         * @param address address value
         */
        public VipPerson(String name, Address address) {
            super(name, address);
        }
    }
}
