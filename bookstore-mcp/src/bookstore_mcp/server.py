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
def get_book_by_id(book_id: int) -> Optional[Dict[str, Any]]:
    """
    根据ID获取特定书籍的详细信息
    
    Args:
        book_id: 书籍ID
        
    Returns:
        书籍详细信息，如果不存在返回None
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
                WHERE id = %s
            """
            cursor.execute(sql, (book_id,))
            book = cursor.fetchone()
            return format_book(book) if book else {"error": f"未找到ID为{book_id}的书籍"}
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
def get_books_in_stock(limit: int = 50) -> List[Dict[str, Any]]:
    """
    获取有库存的书籍列表
    
    Args:
        limit: 返回的最大书籍数量，默认50本
        
    Returns:
        有库存的书籍列表
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
                WHERE stock_quantity > 0 AND is_available = 1
                ORDER BY stock_quantity DESC
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
def check_availability(book_id: int, quantity: int = 1) -> Dict[str, Any]:
    """
    检查书籍库存可用性
    
    Args:
        book_id: 书籍ID
        quantity: 需要的数量，默认为1
        
    Returns:
        库存可用性信息
    """
    conn = get_db_connection()
    if not conn:
        return {"error": "数据库连接失败"}
    
    try:
        with conn.cursor() as cursor:
            sql = """
                SELECT id, title, stock_quantity, is_available
                FROM books
                WHERE id = %s
            """
            cursor.execute(sql, (book_id,))
            book = cursor.fetchone()
            
            if not book:
                return {"available": False, "error": f"未找到ID为{book_id}的书籍"}
            
            return {
                "book_id": book['id'],
                "title": book['title'],
                "available_count": book['stock_quantity'],
                "available": book['stock_quantity'] >= quantity and book['is_available'],
                "is_on_sale": book['is_available']
            }
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


@mcp.tool()
def search_books_by_price_range(min_price: float, max_price: float, limit: int = 20) -> List[Dict[str, Any]]:
    """
    按价格区间搜索书籍
    
    Args:
        min_price: 最低价格
        max_price: 最高价格
        limit: 返回的最大书籍数量，默认20本
        
    Returns:
        价格在指定区间内的书籍列表
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
                WHERE price BETWEEN %s AND %s AND is_available = 1
                ORDER BY price ASC
                LIMIT %s
            """
            cursor.execute(sql, (min_price, max_price, limit))
            books = cursor.fetchall()
            return [format_book(book) for book in books]
    except Exception as e:
        return {"error": f"查询失败: {str(e)}"}
    finally:
        conn.close()


@mcp.tool()
def get_book_statistics() -> Dict[str, Any]:
    """
    获取书店的统计信息
    
    Returns:
        包含书籍总数、在售书籍数、总库存等统计信息
    """
    conn = get_db_connection()
    if not conn:
        return {"error": "数据库连接失败"}
    
    try:
        with conn.cursor() as cursor:
            # 获取基本统计
            sql = """
                SELECT 
                    COUNT(*) as total_books,
                    SUM(CASE WHEN is_available = 1 THEN 1 ELSE 0 END) as available_books,
                    SUM(stock_quantity) as total_stock,
                    SUM(sales) as total_sales,
                    AVG(price) as avg_price,
                    MAX(price) as max_price,
                    MIN(price) as min_price
                FROM books
            """
            cursor.execute(sql)
            stats = cursor.fetchone()
            
            # 格式化Decimal类型
            if stats:
                stats['avg_price'] = float(stats['avg_price']) if stats['avg_price'] else 0
                stats['max_price'] = float(stats['max_price']) if stats['max_price'] else 0
                stats['min_price'] = float(stats['min_price']) if stats['min_price'] else 0
            
            return stats
    except Exception as e:
        return {"error": f"查询失败: {str(e)}"}
    finally:
        conn.close()
