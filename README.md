# Awesome Java 21 Concurrency Preview Project

This project showcases the upcoming concurrency features in Java 21 by implementing a set of APIs, starting with a fun Cat Facts and Pics API built with Spring Boot. 

## Disclaimer

This code is for demonstration purposes only and not meant for production use. The author makes no guarantees and assumes no responsibility for any issues or damages incurred from using this code or parts of it in a production environment.

## Getting Started

These instructions will help you get the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Java 21 (Ensure you have enabled preview features)
- Maven or Gradle

### Installing

Clone the repository to your local machine:

```bash
git clone https://github.com/julianmunozm45/jm45-blazt.git
cd jm45-blazt
```

Build the project and run tests:

```bash
./gradlew clean build
```

Run the application:

```bash
./gradlew bootRun
```

## APIs

### Cat Facts and Pics API

Fetch random cat facts or pictures. Each request may return either a cat fact or a cat picture.

#### Endpoints:

- `GET /cats/pic-or-fact` - Get a random cat fact or picture

More APIs showcasing Java 21 concurrency features will be added in the future.

## License
[MIT](LICENSE) (c) 2023 julianmunozm45
