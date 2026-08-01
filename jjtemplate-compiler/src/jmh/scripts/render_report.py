#!/usr/bin/env python3
"""Render JMH JSON into a compact, comparable Markdown report."""

import argparse
import datetime
import json
import pathlib
import platform
import subprocess


def load_results(path):
    with pathlib.Path(path).open(encoding="utf-8") as source:
        return json.load(source)


def key(result):
    return result["benchmark"], tuple(sorted(result.get("params", {}).items()))


def short_name(result):
    return result["benchmark"].rsplit(".", 2)[-2] + "." + result["benchmark"].rsplit(".", 1)[-1]


def allocations(result):
    metric = result.get("secondaryMetrics", {}).get("gc.alloc.rate.norm")
    if not metric:
        return "-"
    return f'{metric["score"]:.1f} {metric["scoreUnit"]}'


def delta(current, baseline):
    if baseline is None or baseline == 0:
        return "-"
    value = (current - baseline) / baseline * 100
    return f"{value:+.1f}%"


def number(value):
    if isinstance(value, (int, float)):
        return f"{value:.3f}"
    return str(value)


def git_commit():
    try:
        return subprocess.check_output(
            ["git", "rev-parse", "HEAD"],
            text=True,
            stderr=subprocess.DEVNULL,
        ).strip()
    except (OSError, subprocess.CalledProcessError):
        return "unknown"


def render(results, baseline_results):
    baseline = {key(item): item for item in baseline_results or []}
    generated = datetime.datetime.now(datetime.timezone.utc).isoformat()
    lines = [
        "# JJTemplate JMH report",
        "",
        f"- Generated: `{generated}`",
        f"- Commit: `{git_commit()}`",
        f"- Host: `{platform.platform()}`",
        f"- Python: `{platform.python_version()}`",
        "",
        "Lower scores are better for `avgt` and `ss`; higher scores are better for `thrpt`.",
        "",
        "## Results",
        "",
        "| Benchmark | Parameters | Mode | Score | Error | Allocation | Baseline delta |",
        "|---|---|---:|---:|---:|---:|---:|",
    ]
    for item in sorted(results, key=key):
        metric = item["primaryMetric"]
        parameters = ", ".join(
            f"{name}={value}" for name, value in sorted(item.get("params", {}).items())
        ) or "-"
        previous = baseline.get(key(item))
        previous_score = previous["primaryMetric"]["score"] if previous else None
        lines.append(
            "| {name} | {params} | {mode} | {score:.3f} {unit} | ±{error} | {allocation} | {delta} |".format(
                name=short_name(item),
                params=parameters,
                mode=item["mode"],
                score=metric["score"],
                unit=metric["scoreUnit"],
                error=number(metric["scoreError"]),
                allocation=allocations(item),
                delta=delta(metric["score"], previous_score),
            )
        )

    comparisons = variant_comparisons(results)
    if comparisons:
        lines.extend([
            "",
            "## In-run variant comparisons",
            "",
            "Negative latency delta means the enabled variant is faster.",
            "",
            "| Benchmark | Fixed parameters | Variant | Disabled | Enabled | Delta |",
            "|---|---|---|---:|---:|---:|",
        ])
        lines.extend(comparisons)
    lines.append("")
    return "\n".join(lines)


def variant_comparisons(results):
    rows = []
    variants = (
        ("optimize", "false", "true"),
        ("binding", "DYNAMIC", "EXPLICIT_CONTEXT"),
        ("typed", "false", "true"),
    )
    for variant, disabled_name, enabled_name in variants:
        grouped = {}
        for item in results:
            params = item.get("params", {})
            if variant not in params:
                continue
            fixed = tuple(sorted((name, value) for name, value in params.items() if name != variant))
            group_key = item["benchmark"], fixed
            grouped.setdefault(group_key, {})[params[variant]] = item
        for (benchmark, fixed), values in sorted(grouped.items()):
            disabled = values.get(disabled_name)
            enabled = values.get(enabled_name)
            if not disabled or not enabled:
                continue
            disabled_metric = disabled["primaryMetric"]
            enabled_metric = enabled["primaryMetric"]
            params = ", ".join(f"{name}={value}" for name, value in fixed) or "-"
            rows.append(
                "| {name} | {params} | {variant} | {disabled:.3f} | {enabled:.3f} | {delta} |".format(
                    name=short_name(enabled),
                    params=params,
                    variant=variant,
                    disabled=disabled_metric["score"],
                    enabled=enabled_metric["score"],
                    delta=delta(enabled_metric["score"], disabled_metric["score"]),
                )
            )
    return rows


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("results", help="current JMH JSON results")
    parser.add_argument("--baseline", help="optional baseline JMH JSON")
    parser.add_argument("--output", required=True, help="Markdown output path")
    args = parser.parse_args()

    results = load_results(args.results)
    baseline = load_results(args.baseline) if args.baseline else None
    output = pathlib.Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(render(results, baseline), encoding="utf-8")


if __name__ == "__main__":
    main()
