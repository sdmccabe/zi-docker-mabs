CC = musl-gcc
CFLAGS = 
TARGET = Zero\ Intelligence\ Traders-pthreads


all: $(TARGET)

$(TARGET): $(TARGET).c
	$(CC) $(CFLAGS) -o zi-traders $(TARGET).c -lpthread -lm -O2

debug:
	$(CC) $(CFLAGS) -g -o zi-traders $(TARGET).c -lpthread -lm -O2

clean: 
	$(RM) zi-traders
