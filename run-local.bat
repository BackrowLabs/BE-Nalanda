@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

set DATABASE_URL=jdbc:postgresql://db.gdwegkmgqejgcunxrjih.supabase.co:5432/postgres
set DATABASE_USERNAME=postgres
set DATABASE_PASSWORD=Sagarpratheek@2025
set SUPABASE_URL=https://gdwegkmgqejgcunxrjih.supabase.co
set SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imdkd2Vna21ncWVqZ2N1bnhyamloIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3Njg1Njc0NiwiZXhwIjoyMDkyNDMyNzQ2fQ.h4QuAV8fFNaDrdimLlN7vH4ZLf1N5G4I4fpfgHFXQH0
mvn clean spring-boot:run -Dspring-boot.run.profiles=local




