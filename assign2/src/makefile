all: GameServer GameCLient

GameServer: 
	javac User.java
	javac GameServer.java

GameCLient:
	javac GameClient.java

start-server:
	java GameServer 8000

start-client:
	java GameClient localhost 8000

clean: 
	rm -f *.class

start:
	python3 start_script.py