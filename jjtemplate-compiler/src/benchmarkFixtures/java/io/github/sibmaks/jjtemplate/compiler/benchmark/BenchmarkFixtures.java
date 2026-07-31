package io.github.sibmaks.jjtemplate.compiler.benchmark;

import io.github.sibmaks.jjtemplate.compiler.api.Definition;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds deterministic benchmark inputs outside measured JMH operations.
 */
public final class BenchmarkFixtures {
    private static final int DEFAULT_COLLECTION_SIZE = 16;

    private BenchmarkFixtures() {
    }

    /**
     * Builds a representative scenario using its default data size.
     *
     * @param scenario scenario name
     * @return benchmark case
     */
    public static BenchmarkCase create(BenchmarkScenario scenario) {
        switch (scenario) {
            case STATIC_LITERAL:
                return staticLiteral();
            case SCALAR_SUBSTITUTION:
                return scalarSubstitution();
            case NESTED_MAP:
                return nestedMap();
            case FUNCTION_PIPELINE:
                return functionPipeline();
            case CONDITIONALS:
                return conditionals();
            case COLLECTION:
                return collection(DEFAULT_COLLECTION_SIZE);
            case REALISTIC_DOCUMENT:
                return realisticDocument(DEFAULT_COLLECTION_SIZE);
            default:
                throw new IllegalArgumentException("Unknown benchmark scenario: " + scenario);
        }
    }

    /**
     * Builds a collection range scenario of the requested size.
     *
     * @param size collection size
     * @return benchmark case
     */
    public static BenchmarkCase collection(int size) {
        return collection(size, CollectionDataLocation.EXTERNAL);
    }

    /**
     * Builds a range scenario with either runtime or inline collection data.
     *
     * @param size collection size
     * @param dataLocation location of the range data
     * @return benchmark case
     */
    public static BenchmarkCase collection(
            int size,
            CollectionDataLocation dataLocation
    ) {
        var items = new ArrayList<String>(size);
        var expected = new ArrayList<Map<String, Object>>(size);
        for (int i = 0; i < size; i++) {
            var value = "value-" + i;
            items.add(value);
            expected.add(orderedMap(
                    "index", i,
                    "value", value,
                    "label", "item-" + value
            ));
        }

        var range = new Definition();
        range.put(
                "{{ rows range item,index of .items }}",
                orderedMap(
                        "index", "{{ .index }}",
                        "value", "{{ .item }}",
                        "label", "item-{{ .item }}"
                )
        );
        var definitions = new ArrayList<Definition>();
        var context = Map.<String, Object>of("items", items);
        if (dataLocation == CollectionDataLocation.INLINE) {
            var values = new Definition();
            values.put("items", items);
            definitions.add(values);
            context = Map.of();
        }
        definitions.add(range);
        var script = TemplateScript.builder()
                .definitions(definitions)
                .template("{{ .rows }}")
                .build();
        return new BenchmarkCase(script, context, expected);
    }

    private static BenchmarkCase staticLiteral() {
        var template = orderedMap(
                "service", "jjtemplate",
                "status", "ok",
                "features", List.of("compile", "render")
        );
        return new BenchmarkCase(
                TemplateScript.builder().template(template).build(),
                Map.of(),
                template
        );
    }

    private static BenchmarkCase scalarSubstitution() {
        var template = orderedMap(
                "message", "Hello, {{ .name }}!",
                "active", "{{ .active }}",
                "count", "{{ .count }}"
        );
        var context = orderedMap(
                "name", "Alice",
                "active", true,
                "count", 42
        );
        var expected = orderedMap(
                "message", "Hello, Alice!",
                "active", true,
                "count", 42
        );
        return new BenchmarkCase(
                TemplateScript.builder().template(template).build(),
                context,
                expected
        );
    }

    private static BenchmarkCase nestedMap() {
        var address = Map.<String, Object>of("city", "Moscow");
        var person = Map.<String, Object>of("address", address);
        return new BenchmarkCase(
                TemplateScript.builder().template("{{ .person.address.city }}").build(),
                Map.of("person", person),
                "Moscow"
        );
    }

    private static BenchmarkCase functionPipeline() {
        return new BenchmarkCase(
                TemplateScript.builder()
                        .template("{{ .name | string:trim | string:upper }}")
                        .build(),
                Map.of("name", "  Alice  "),
                "ALICE"
        );
    }

    private static BenchmarkCase conditionals() {
        var template = orderedMap(
                "status", "{{ .enabled ? 'enabled' : 'disabled' }}",
                "fallback", "{{ default .missing, 'unknown' }}",
                "visible", "{{ and .enabled, .allowed }}"
        );
        var context = orderedMap(
                "enabled", true,
                "allowed", true,
                "missing", null
        );
        var expected = orderedMap(
                "status", "enabled",
                "fallback", "unknown",
                "visible", true
        );
        return new BenchmarkCase(
                TemplateScript.builder().template(template).build(),
                context,
                expected
        );
    }

    private static BenchmarkCase realisticDocument(int size) {
        var items = new ArrayList<Map<String, Object>>(size);
        var expectedLines = new ArrayList<Map<String, Object>>(size);
        for (int i = 0; i < size; i++) {
            var sku = "SKU-" + i;
            var quantity = i + 1;
            items.add(orderedMap("sku", sku, "quantity", quantity));
            expectedLines.add(orderedMap(
                    "position", i,
                    "sku", sku,
                    "quantity", quantity
            ));
        }

        var range = new Definition();
        range.put(
                "{{ lines range item,index of .items }}",
                orderedMap(
                        "position", "{{ .index }}",
                        "sku", "{{ .item.sku }}",
                        "quantity", "{{ .item.quantity }}"
                )
        );
        var template = orderedMap(
                "customer", "{{ .customer.name }}",
                "city", "{{ .customer.address.city }}",
                "note", "{{ .note | string:trim }}",
                "lines", "{{ .lines }}"
        );
        var context = orderedMap(
                "customer", orderedMap(
                        "name", "Alice",
                        "address", Map.of("city", "Moscow")
                ),
                "note", "  priority  ",
                "items", items
        );
        var expected = orderedMap(
                "customer", "Alice",
                "city", "Moscow",
                "note", "priority",
                "lines", expectedLines
        );
        return new BenchmarkCase(
                TemplateScript.builder()
                        .definitions(List.of(range))
                        .template(template)
                        .build(),
                context,
                expected
        );
    }

    private static Map<String, Object> orderedMap(Object... entries) {
        var result = new LinkedHashMap<String, Object>(entries.length / 2);
        for (int i = 0; i < entries.length; i += 2) {
            result.put((String) entries[i], entries[i + 1]);
        }
        return result;
    }
}
