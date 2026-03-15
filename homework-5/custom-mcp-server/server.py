from pathlib import Path
from fastmcp import FastMCP

mcp = FastMCP("lorem-ipsum-server")

LOREM_FILE = Path(__file__).parent / "lorem-ipsum.md"


def _read_words(word_count: int = 30) -> str:
    words = LOREM_FILE.read_text(encoding="utf-8").split()
    return " ".join(words[:word_count])


@mcp.resource("lorem://content/{word_count}")
def lorem_resource(word_count: int = 30) -> str:
    """Return the first `word_count` words from lorem-ipsum.md."""
    return _read_words(word_count)


@mcp.tool()
def read(word_count: int = 30) -> str:
    """Read the first `word_count` words from the lorem-ipsum resource."""
    return _read_words(word_count)


if __name__ == "__main__":
    mcp.run()
