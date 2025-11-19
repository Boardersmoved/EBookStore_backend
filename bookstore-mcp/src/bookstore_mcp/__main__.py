"""
EBookStore MCP Server Entry Point
"""
from .server import mcp


def main():
    """启动MCP服务器"""
    mcp.run()


if __name__ == "__main__":
    main()
