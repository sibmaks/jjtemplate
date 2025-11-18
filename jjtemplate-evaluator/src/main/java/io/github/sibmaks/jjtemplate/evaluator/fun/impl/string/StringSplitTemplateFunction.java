package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.4.0
 */
public class StringSplitTemplateFunction implements TemplateFunction<List<String>> {

    private List<String> split(Object value, String regex, int limit) {
        var string = String.valueOf(value);
        var values = string.split(regex, limit);
        var res = new ArrayList<String>(values.length);
        Collections.addAll(res, values);
        return res;
    }

    @Override
    public List<String> invoke(List<Object> args, Object pipeArg) {
        var limit = 0;
        var regex = "";

        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        if (pipeArg == null) {
            return null;
        }
        if (args.size() == 1) {
            regex = String.valueOf(args.get(0));
        } else if (args.size() == 2) {
            regex = String.valueOf(args.get(0));
            limit = Integer.parseInt(String.valueOf(args.get(1)));
        } else {
            throw fail("too much arguments passed");
        }

        return split(pipeArg, regex, limit);
    }

    @Override
    public List<String> invoke(List<Object> args) {
        var limit = 0;
        var value = "";
        var regex = "";

        if (args.size() < 2) {
            throw fail("at least 2 arguments required");
        }
        var arg0 = args.get(0);
        if (arg0 == null) {
            return null;
        }
        if (args.size() == 2) {
            value = String.valueOf(arg0);
            regex = String.valueOf(args.get(1));
        } else if (args.size() == 3) {
            value = String.valueOf(arg0);
            regex = String.valueOf(args.get(1));
            limit = Integer.parseInt(String.valueOf(args.get(2)));
        } else {
            throw fail("too much arguments passed");
        }

        return split(value, regex, limit);
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "split";
    }
}
