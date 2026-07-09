.PHONY: build test run docker-build docker-run clean

build:
	mvn package -DskipTests

test:
	mvn test

run:
	mvn spring-boot:run

docker-build:
	docker build -t ai-gateway:latest .

docker-run:
	docker run -p 8080:8080 --env-file .env ai-gateway:latest

clean:
	mvn clean
