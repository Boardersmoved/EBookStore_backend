# EBookStore 微服务启动脚本 (使用 Maven Wrapper)
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   EBookStore 微服务启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 获取项目根目录
$rootDir = $PSScriptRoot

# 检查 Docker
Write-Host "[1/6] 检查 Docker 服务..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "✓ Docker 运行正常" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker 未运行，请先启动 Docker Desktop" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

# 启动 Redis
Write-Host "[2/6] 启动 Redis 容器..." -ForegroundColor Yellow
docker start ebook
Start-Sleep -Seconds 3
Write-Host "✓ Redis 已启动" -ForegroundColor Green

# 启动 Eureka Server
Write-Host "[3/6] 启动 Eureka Server (端口 8761)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\eureka-server'; ..\mvnw.cmd spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 15
Write-Host "✓ Eureka Server 启动中..." -ForegroundColor Green

# 启动 Author Service
Write-Host "[4/6] 启动 Author Service (端口 8081)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\author-service'; ..\mvnw.cmd spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 10
Write-Host "✓ Author Service 启动中..." -ForegroundColor Green

# 启动主应用
Write-Host "[5/6] 启动 EBookStore Backend (端口 8082)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir'; .\mvnw.cmd spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 10
Write-Host "✓ EBookStore Backend 启动中..." -ForegroundColor Green

# 启动 API Gateway
Write-Host "[6/6] 启动 API Gateway (端口 8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\api-gateway'; ..\mvnw.cmd spring-boot:run" -WindowStyle Normal
Write-Host "✓ API Gateway 启动中..." -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ 所有服务正在启动中..." -ForegroundColor Green
Write-Host ""
Write-Host "等待约 30-60 秒后，访问：" -ForegroundColor Yellow
Write-Host "  - Eureka: http://localhost:8761" -ForegroundColor White
Write-Host "  - Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "  - 主应用: http://localhost:8082" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "提示：每个服务会在单独的窗口中启动" -ForegroundColor Gray
Write-Host "关闭窗口即可停止对应服务" -ForegroundColor Gray
Write-Host ""
Read-Host "按回车键关闭此窗口"