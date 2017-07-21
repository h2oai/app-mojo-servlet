all:	train build run

train:
	Rscript script.R

build:
	./gradlew build

run:
	./gradlew jettyRunWar

clean:
	rm -f src/main/resources/*
	./gradlew clean

.PHONY: train build run clean
