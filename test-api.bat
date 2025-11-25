@echo off
echo ======================================
echo Employee Chatbot API Test Script
echo ======================================
echo.

echo 1. Creating test user...
curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d "{\"username\":\"testuser\",\"email\":\"test@company.com\",\"password\":\"test123\",\"fullName\":\"Test User\",\"department\":\"IT\"}"
echo.
echo.

timeout /t 2 /nobreak > nul

echo 2. Sending first message...
curl -X POST http://localhost:8080/api/chat/message -H "Content-Type: application/json" -d "{\"userId\":1,\"message\":\"Hello! Tell me about Spring Boot.\"}"
echo.
echo.

timeout /t 2 /nobreak > nul

echo 3. Getting user stats...
curl http://localhost:8080/api/gamification/users/1/stats
echo.
echo.

timeout /t 2 /nobreak > nul

echo 4. Getting achievements...
curl http://localhost:8080/api/gamification/users/1/achievements
echo.
echo.

echo Test completed!
pause
