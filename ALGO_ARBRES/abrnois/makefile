CC = gcc
CFLAGS = -Wall -Wextra -O2

TARGET = abrnois
SRC = abrnois.c

.PHONY: all clean run

all: $(TARGET)

$(TARGET): $(SRC)
	$(CC) $(CFLAGS) -o $@ $^

# Exemple d'exécution :
# make run ARGS="frequents.txt corpus2.txt"
# make run ARGS="frequents.txt -g -n 3 corpus2.txt"
run: $(TARGET)
	./$(TARGET) $(ARGS)

clean:
	-del /Q $(TARGET).exe *.o *.dot *.pdf 2>nul || rm -f $(TARGET) *.o *.dot *.pdf

