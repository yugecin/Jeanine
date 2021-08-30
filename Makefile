SOURCES = $(wildcard src/net/basdon/jeanine/*.java)

jeanine.jar: $(SOURCES)
	@mkdir -p bin/src
	javac -g -d bin/src $(SOURCES)
	jar cfe jeanine.jar net.basdon.jeanine.Jeanine -C bin/src .
