# Research Notes — context7 Queries

context7 queries made during code generation to look up up-to-date library and framework documentation. Each entry records what was searched, the library ID context7 returned, and the key insight or pattern applied.

---

## Query 1: Decimal / monetary arithmetic (Python)

- Search: "Python decimal module ROUND_HALF_UP monetary arithmetic"
- context7 library ID: /python/cpython
- Source: Python stdlib `decimal` docs (context7 result: /python/cpython)
- Applied: Use `decimal.Decimal` for all monetary values; never use binary `float`. For display and settlement rounding use `quantize()` with `ROUND_HALF_UP` to implement banking-style rounding to two decimal places. Parse incoming amounts into `Decimal` immediately and keep calculations in Decimal until final output.

Code pattern applied:

```
from decimal import Decimal, ROUND_HALF_UP, localcontext

TWOPLACES = Decimal('0.01')

def round_cents(amount: str) -> Decimal:
	d = Decimal(amount)
	return d.quantize(TWOPLACES, rounding=ROUND_HALF_UP)

with localcontext() as ctx:
	ctx.prec = 28
	total = sum(Decimal(x) for x in ['10.00', '0.105'])
	total_rounded = total.quantize(TWOPLACES, rounding=ROUND_HALF_UP)

```

Notes:
- `quantize()` can also validate input precision when used with traps (e.g., `Inexact`).
- Use `localcontext()` when changing precision/rounding to avoid global side effects.

---

## Query 2: FastMCP — Python server, tool & resource decorators

- Search: "FastMCP Python server tool resource decorator"
- context7 library ID: /prefecthq/fastmcp
- Source: FastMCP server docs (context7 result: /prefecthq/fastmcp)
- Applied: Register MCP tools and resources using decorator patterns. Use `@mcp.tool` or `@tool` to expose callable tools (type hints drive input schema generation). Use `@mcp.resource` or `@resource(uri=...)` to expose textual or structured resources like `pipeline://summary`. For grouping, implement a component class (subclassing `MCPMixin` or similar) and call `component.register_all(mcp_server, prefix=...)`.

Example patterns applied in `mcp/server.py`:

```
from fastmcp import FastMCP

mcp = FastMCP(name="pipeline-status")

@mcp.tool
def get_transaction_status(transaction_id: str) -> dict:
	# read shared/results/ and return JSON for transaction_id
	...

@mcp.tool
def list_pipeline_results() -> list:
	# summarize processed transactions
	...

@mcp.resource("pipeline://summary")
def pipeline_summary() -> str:
	# return latest pipeline run summary text
	...

if __name__ == '__main__':
	mcp.serve()
```

Notes:
- In FastMCP v3 the decorators return the original function (not a FunctionTool object) unless `FASTMCP_DECORATOR_MODE=object` is set. Tools still get registered when the server instance observes the decorated functions or when `register_all` is used.
- Metadata (name, description, annotations) can be passed to decorators to improve generated schemas and tool docs.

---

These context7 findings guided the implementation choices for monetary arithmetic and the FastMCP server tools/resources in this project.
