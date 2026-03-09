---
name: research-quality-measurement
description: >
  Measures and labels the quality of bug-research documents by verifying
  file:line references, code-snippet fidelity, completeness, and logical
  soundness. Use when a Research Verifier agent needs to score a
  codebase-research report and produce a verified-research summary.
---

# Research Quality Measurement

Use this skill to **evaluate and label** the quality of a codebase research
document (e.g. `codebase-research.md`) produced by a Bug Researcher agent.

---

## Quality Levels

| Level | Score | When to assign |
|-------|-------|----------------|
| **EXCELLENT** | 90 – 100 | All references verified, code snippets match source exactly, no discrepancies. |
| **GOOD** | 70 – 89 | Most references verified, minor discrepancies that don't affect conclusions. |
| **ACCEPTABLE** | 50 – 69 | Key references verified, some missing or outdated references but core analysis is sound. |
| **POOR** | 25 – 49 | Multiple broken references, code snippets don't match, unreliable conclusions. |
| **FAILING** | 0 – 24 | Majority of references invalid, analysis cannot be trusted. |

---

## What to Measure

### 1. File:Line Reference Accuracy

Check every `file:line` reference in the research document.

- [ ] Referenced file exists in the project
- [ ] Referenced line number is within file bounds
- [ ] Content at that line matches the claim made in the research

**Scoring**: `(verified_references / total_references) × 100`

### 2. Code Snippet Fidelity

Compare each quoted code snippet against the actual source.

- [ ] Snippet text matches the source file exactly (whitespace-tolerant)
- [ ] Snippet is attributed to the correct file and line range
- [ ] No truncation that changes meaning

**Scoring**: `(matching_snippets / total_snippets) × 100`

### 3. Completeness

Assess whether the research covers all files and modules relevant to the bug.

- [ ] All files mentioned in the bug context are analyzed
- [ ] Related files (imports, shared utilities) are considered
- [ ] No critical code paths are skipped

**Scoring**: `(covered_relevant_files / total_relevant_files) × 100`

### 4. Logical Soundness

Evaluate whether the conclusions follow from the evidence presented.

- [ ] Each conclusion cites specific code evidence
- [ ] Root-cause hypothesis is consistent with the referenced code
- [ ] No contradictions between claims

**Scoring**: subjective 0 – 100 based on the criteria above.

---

## How to Calculate the Overall Score

```
overall = (reference_accuracy × 0.35)
        + (snippet_fidelity   × 0.30)
        + (completeness        × 0.20)
        + (logical_soundness   × 0.15)
```

Map the resulting number to the **Quality Levels** table above.

---

## Output Format

When writing `verified-research.md`, include these sections:

### Verification Summary

```
Research Quality : <LEVEL> (<score>/100)
Verified Claims  : <n> / <total>
Discrepancies    : <count>
```

### Verified Claims

List every claim that was confirmed, with the checked reference.

### Discrepancies Found

For each discrepancy:

| # | Claim | Expected | Actual | Severity |
|---|-------|----------|--------|----------|
| 1 | … | … | … | high / medium / low |

### Research Quality Assessment

- **Level**: `<LEVEL>`
- **Score**: `<score>/100`
- **Breakdown**: reference accuracy / snippet fidelity / completeness / logical soundness
- **Reasoning**: 1-3 sentences explaining why this level was assigned.

### References

List every file and line that was checked during verification.
