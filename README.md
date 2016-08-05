<p align="center"><img width="100" src="https://cloud.githubusercontent.com/assets/4793869/16262957/d8c3ff5c-3869-11e6-913a-03e7972ac61a.png"></p>
<p align="center"><strong>Meet Volt</strong></p>

## Introduction

Volt is an asynchronous event-driven network package that allows you to quickly set up servers and clients. Core features include:

- Simple to install and use.
- Abstracts the user from most network concepts.
- Expressive and elegant syntax.
- Easily expandable.
- Routing and middleware.

Currently Volt is under heavy development, therefore it might undergo some changes during this process.

### "Hello Volt"

```java
public void main(String[] args) {
  Server server = Volt.server("udp", 30600);
  
  server.listen(":hello", (request) -> {
    System.out.println("Received message: " + request.message());
  });
  
  Client client = Volt.client("udp");
  
  client.every(3).send(":hello", "all:30600", "Hello Volt.")
        .after(10).stop();
}
```

## Contributing

If you happen to find an error, or you might be thinking about a general improvement to the project, please do create an Issue, or consider creating a Pull Request. All contributions are appreciated.
