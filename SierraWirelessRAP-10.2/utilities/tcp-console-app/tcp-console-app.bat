@echo off
set TCP_PORT=5570
set SERVER=localhost
set JAVA_HOME=C:\Program Files\ArcGIS\Server\framework\runtime\jre

"%JAVA_HOME%\bin\java" TcpPrinter %TCP_PORT% %SERVER%