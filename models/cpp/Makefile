CC = g++
CFLAGS = -std=c++11 -O2
TARGET = Zero\ Intelligence\ Traders


all: $(TARGET)

$(TARGET): $(TARGET).c++
	$(CC) $(CFLAGS) -o zi-traders $(TARGET).c++ Data.c++ -lpthread -lgflags

debug:
	$(CC) $(CFLAGS) -g -o zi-traders $(TARGET).c++ Data.c++ -lpthread -lgflags

clean: 
	$(RM) zi-traders
