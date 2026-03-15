# Custom MCP Server — Run Instructions

## 1. Install dependencies

```bash
cd homework-5/custom-mcp-server
uv venv .venv && source .venv/bin/activate   # create & activate venv
uv pip install -r requirements.txt            # installs fastmcp
```

## 2. Run the server

```bash
python server.py
```

The server starts on **stdio** transport by default — it communicates via stdin/stdout, which is how VS Code Copilot connects to it.

## 3. Connect via MCP configuration

Add an entry to your `mcp.json` (already included in `homework-5/mcp.json`):

```json
"custom-lorem": {
  "command": "homework-5/custom-mcp-server/.venv/bin/python",
  "args": ["homework-5/custom-mcp-server/server.py"]
}
```

VS Code will automatically detect and start the server.

## 4. Test the `read` tool

In Copilot Agent Mode, ask:

> *"Use the custom-lorem MCP read tool to get 10 words from the lorem ipsum file."*

Expected response: the first 10 words from `lorem-ipsum.md`.

## Resources vs Tools in MCP

- **Resources** are read-only data endpoints identified by a URI (e.g., `lorem://content/30`). The AI client can fetch them like reading a file or calling an API.
- **Tools** are callable actions that the AI can invoke to perform operations — reading data, running commands, or triggering side effects.

In this server, the **resource** (`lorem://content/{word_count}`) exposes the lorem ipsum text at a URI, while the **tool** (`read`) lets the AI actively request it with a parameter.
