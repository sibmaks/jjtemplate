package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.typebind;

import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileContext;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.VariableTemplateExpression;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author sibmaks
 * @since 0.9.0
 */
enum ResolutionStatus {
    RESOLVED,
    UNKNOWN,
    INVALID
}

/**
 * @author sibmaks
 * @since 0.9.0
 */
final class ChainResolution {
    private final ResolutionStatus status;
    private final CompileTypeSet nextTypes;
    private final VariableTemplateExpression.Chain chain;
    private final String message;

    ChainResolution(
            ResolutionStatus status,
            CompileTypeSet nextTypes,
            VariableTemplateExpression.Chain chain,
            String message
    ) {
        this.status = status;
        this.nextTypes = nextTypes;
        this.chain = chain;
        this.message = message;
    }

    ResolutionStatus status() {
        return status;
    }

    CompileTypeSet nextTypes() {
        return nextTypes;
    }

    VariableTemplateExpression.Chain chain() {
        return chain;
    }

    String message() {
        return message;
    }
}

/**
 * @author sibmaks
 * @since 0.9.0
 */
final class CompileTypeSet {
    private final boolean known;
    private final List<Class<?>> types;

    private CompileTypeSet(boolean known, List<Class<?>> types) {
        this.known = known;
        this.types = types;
    }

    static CompileTypeSet unknown() {
        return new CompileTypeSet(false, List.of());
    }

    static CompileTypeSet known(Class<?> type) {
        return new CompileTypeSet(true, List.of(type));
    }

    static CompileTypeSet known(Iterable<Class<?>> types) {
        var unique = new LinkedHashSet<Class<?>>();
        for (var type : types) {
            if (type != null) {
                unique.add(type);
            }
        }
        if (unique.isEmpty()) {
            return unknown();
        }
        return new CompileTypeSet(true, List.copyOf(unique));
    }

    boolean isKnown() {
        return known;
    }

    List<Class<?>> types() {
        return types;
    }
}

/**
 * @author sibmaks
 * @since 0.9.0
 */
final class CompileScope {
    private final CompileScope parent;
    private final Map<String, CompileTypeSet> types;
    private final TemplateCompileContext compileContext;

    CompileScope(
            CompileScope parent,
            Map<String, CompileTypeSet> types,
            TemplateCompileContext compileContext
    ) {
        this.parent = parent;
        this.types = types;
        this.compileContext = compileContext;
    }

    CompileScope child(Map<String, CompileTypeSet> local) {
        return new CompileScope(this, new LinkedHashMap<>(local), compileContext);
    }

    Optional<CompileTypeSet> lookup(String variableName) {
        if (types.containsKey(variableName)) {
            return Optional.of(types.get(variableName));
        }
        if (parent != null) {
            return parent.lookup(variableName);
        }
        var fromContext = compileContext.lookupTypes(variableName);
        return fromContext.map(CompileTypeSet::known);
    }
}
