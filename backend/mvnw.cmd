@ECHO OFF
SETLOCAL
SET BASEDIR=%~dp0
SET WRAPPER_DIR=%BASEDIR%\.mvn\wrapper
SET WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
SET WRAPPER_PROPS=%WRAPPER_DIR%\maven-wrapper.properties

IF NOT EXIST "%WRAPPER_JAR%" (
  FOR /F "tokens=2 delims==" %%A IN ('findstr /B "wrapperUrl=" "%WRAPPER_PROPS%"') DO SET WRAPPER_URL=%%A
  powershell -NoProfile -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
)

java -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
ENDLOCAL
