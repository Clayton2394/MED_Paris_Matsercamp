@echo off
title MED Paris - Lancement
color 0A
echo ===================================================
echo     Lancement du Projet MED Paris - Itineraire
echo ===================================================
echo.

echo [1/2] Demarrage du serveur Backend (Java Spring Boot)...
cd backend-api
start "Backend (Spring Boot)" cmd /k "mvnw.cmd spring-boot:run"
cd ..

:: Petite temporisation pour laisser le backend demarrer avant le frontend
timeout /t 5 /nobreak >nul

echo [2/2] Demarrage du serveur Frontend (Vue.js)...
cd app-itineraire
start "Frontend (Vue.js)" cmd /k "npm install && npm run dev"
cd ..

echo.
echo Les serveurs sont en cours de lancement dans de nouvelles fenetres.
echo - Le backend sera disponible sur http://localhost:8080
echo - Le frontend sera disponible sur http://localhost:5173 (ou le port indique)
echo.
pause
