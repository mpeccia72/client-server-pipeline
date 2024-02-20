@echo off
javac Client.java                      
javac Server.java
java Server 10 | java Client
pause

