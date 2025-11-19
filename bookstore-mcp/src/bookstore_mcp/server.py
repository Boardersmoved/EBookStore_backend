import os
import pymysql
from decimal import Decimal
from typing import Optional, List, Dict, Any
from fastmcp import FastMCP
from dotenv import load_dotenv

# 加载环境变量
load_dotenv()

# 创建MCP服务器实例
mcp = FastMCP("EBookStore")

# 数据库配置
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', 3306)),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', '1234'),
    'database': os.getenv('DB_NAME', 'ebookstore'),
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}


def get_db_connection():
    """创建并返回数据库连接"""
    try:
        connection = pymysql.connect(**DB_CONFIG)
        return connection
    except Exception as e:
        print(f"数据库连接失败: {e}")
        return None


def format_book(book: Dict[str, Any]) -> Dict[str, Any]:
    """格式化书籍数据，将Decimal转换为float"""
    if book is None:
        return None
    
    formatted = dict(book)
    if 'price' in formatted and isinstance(formatted['price'], Decimal):
        formatted['price'] = float(formatted['price'])
    
    # 移除封面图片的base64数据以减少传输量
    if 'cover_image_base64' in formatted:
        formatted['has_cover_image'] = formatted['cover_image_base64'] is not None
        del formatted['cover_image_base64']
    
    return formatted


@mcp.tool()
def get_all_books(limit: int = 50) -> List[Dict[str, Any]]:
    """
    获取所有书籍列表
    
    Args:
        limit: 返回的最大书籍数量，默认50本
        
    Returns:
        书籍列表
    """
    conn = get_db_connection()
    if not conn:
        return {"error": "数据库连接失败"}
    
    try:
        with conn.cursor() as cursor:
            sql = """
                SELECT id, title, author, isbn, price, description, 
                       sales, stock_quantity, is_available
                FROM books
                WHERE is_available = 1
                ORDER BY sales DESC
                LIMIT %s
            """
            cursor.execute(sql, (limit,))
            books = cursor.fetchall()
            return [format_book(book) for book in books]
    except Exception as e:
        return {"error": f"查询失败: {str(e)}"}
    finally:
        conn.close()


@mcp.tool()
def search_books_by_title(title_query: str, limit: int = 20) -> List[Dict[str, Any]]:
    """
    按书名搜索书籍（支持模糊查询）
    
    Args:
        title_query: 书名关键词
        limit: 返回的最大结果数量，默认20本
        
    Returns:
        匹配的书籍列表
    """
    conn = get_db_connection()
    if not conn:
        return {"error": "数据库连接失败"}
    
    try:
        with conn.cursor() as cursor:
            sql = """
                SELECT id, title, author, isbn, price, description, 
                       sales, stock_quantity, is_available
                FROM books
                WHERE title LIKE %s AND is_available = 1
                ORDER BY sales DESC
                LIMIT %s
            """
            cursor.execute(sql, (f"%{title_query}%", limit))
            books = cursor.fetchall()
            return [format_book(book) for book in books]
    except Exception as e:
        return {"error": f"查询失败: {str(e)}"}
    finally:
        conn.close()


@mcp.tool()
def search_books_by_author(author_query: str, limit: int = 20) -> List[Dict[str, Any]]:
    """
    按作者搜索书籍（支持模糊查询）
    
    Args:
        author_query: 作者名关键词
        limit: 返回的最大结果数量，默认20本
        
    Returns:
        匹配的书籍列表
    """
    conn = get_db_connection()
    if not conn:
        return {"error": "数据库连接失败"}
    
    try:
        with conn.cursor() as cursor:
            sql = """
                SELECT id, title, author, isbn, price, description, 
                       sales, stock_quantity, is_available
                FROM books
                WHERE author LIKE %s AND is_available = 1
                ORDER BY sales DESC
                LIMIT %s
            """
            cursor.execute(sql, (f"%{author_query}%", limit))
            books = cursor.fetchall()
            return [format_book(book) for book in books]
    except Exception as e:
        return {"error": f"查询失败: {str(e)}"}
    finally:
        conn.close()





@mcp.tool()
def get_top_selling_books(limit: int = 10) -> List[Dict[str, Any]]:
    """
    获取销量最高的书籍
    
    Args:
        limit: 返回的书籍数量，默认10本
        
    Returns:
        销量排行榜
    """
    conn = get_db_connection()
    if not conn:
        return {"error": "数据库连接失败"}
    
    try:
        with conn.cursor() as cursor:
            sql = """
                SELECT id, title, author, isbn, price, description, 
                       sales, stock_quantity, is_available
                FROM books
                WHERE is_available = 1
                ORDER BY sales DESC
                LIMIT %s
            """
            cursor.execute(sql, (limit,))
            books = cursor.fetchall()
            return [format_book(book) for book in books]
    except Exception as e:
        return {"error": f"查询失败: {str(e)}"}
    finally:
        conn.close()






