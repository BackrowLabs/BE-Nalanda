@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

set DATABASE_URL=
set DATABASE_USERNAME=
set DATABASE_PASSWORD=
set SUPABASE_URL
set SUPABASE_SERVICE_ROLE_KEY=
mvn clean spring-boot:run -Dspring-boot.run.profiles=local




