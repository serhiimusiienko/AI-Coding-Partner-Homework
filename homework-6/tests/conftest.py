"""Shared test configuration for homework-6.

Loads mcp/server.py under the module alias 'pipeline_server' so tests can
import it without shadowing the installed 'mcp' PyPI package that fastmcp
depends on internally. Coverage.py tracks by file path, so lines in
mcp/server.py are still measured correctly.
"""
from __future__ import annotations

import importlib.util
import sys
from pathlib import Path


def pytest_configure(config: object) -> None:
    """Pre-load mcp/server.py as 'pipeline_server' before any test is collected."""
    server_path = Path(__file__).resolve().parent.parent / "mcp" / "server.py"
    spec = importlib.util.spec_from_file_location("pipeline_server", str(server_path))
    module = importlib.util.module_from_spec(spec)  # type: ignore[arg-type]
    sys.modules["pipeline_server"] = module
    spec.loader.exec_module(module)  # type: ignore[union-attr]
