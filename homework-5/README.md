# 🔌 Homework 5: Configure MCP Servers (GitHub, Filesystem, Jira, Custom)

> **Student Name**: Serhii Musiienko
> **Date Submitted**: 10.03.2026
> **AI Tools Used**: VS Code, GitHub Copilot Agent mode

---

## 📋 Project Overview

Configured three external MCP servers (GitHub, Filesystem, Jira) and built a custom MCP server with FastMCP. All servers are registered in `mcp.json` and verified with live interactions.

---

## ✅ Tasks Completed

### Task 1: GitHub MCP
Installed and configured the official `@modelcontextprotocol/server-github` server with a personal access token. Used it to list the last 3 pull requests in the `AI-Coding-Partner-Homework` repository — titles, numbers, and statuses were returned correctly.

### Task 2: Filesystem MCP
Configured `@modelcontextprotocol/server-filesystem` pointing at the local project directory. Listed all files inside `homework-5/` folder to confirm the server reads the local filesystem correctly.

### Task 3: Jira MCP
Configured `mcp-atlassian` with Jira credentials via environment variables. Queried board for the last 5 bug-type tickets (`issuetype in (Bug, Defect)`) and received valid ticket numbers.

### Task 4: Custom MCP Server with FastMCP
Built a Python FastMCP server in `custom-mcp-server/` that exposes a `lorem://content/{word_count}` resource and a `read` tool. The tool reads `lorem-ipsum.md` and returns exactly the requested number of words (default: 30). Verified with a live MCP call returning 15 words.

---

## 📁 Project Structure

```
homework-5/
├── README.md
├── TASKS.md
├── mcp.json                      # MCP server configuration
└── custom-mcp-server/
    ├── server.py                 # FastMCP server implementation
    ├── lorem-ipsum.md            # Source text for the resource
    ├── requirements.txt          # fastmcp dependency
    └── HOWTORUN.md               # Setup and usage instructions
```

---

## 📸 Screenshots

Evidence of all MCP interactions is in [`docs/screenshots/`](docs/screenshots/).

---

<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

</div>
