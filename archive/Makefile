all: exe

exe: main.cpp
	g++ -Wall -g -c main.cpp
	g++ -g -pthread main.o -o main
	#g++ main.cpp -pthread -Wall -std=c++11 -o main

clean:
	rm main main.o
