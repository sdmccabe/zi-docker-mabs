CC = gcc
CFLAGS = 
TARGET = Zero\ Intelligence\ Traders-OpenMP


all: $(TARGET)

$(TARGET): $(TARGET).c
	$(CC) $(CFLAGS) -o zi-traders $(TARGET).c -lpthread -lm -fopenmp

debug:
	$(CC) $(CFLAGS) -g -o zi-traders $(TARGET).c -lpthread -lm -fopenmp

clean: 
	$(RM) zi-traders
