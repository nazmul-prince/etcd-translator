# Etcd Translator Library For Spring Boot

## Overview

The Etcd Translator Library for spring boot provides a convenient way to manage and fetch translation resources from an
etcd key-value store. This library includes configuration for setting up connection pools and handling asynchronous or
synchronous retrieval of translation data.

## Features

- **Asynchronous and Synchronous Retrieval**: Choose between async and sync approaches for loading messages.
- **Configuration Flexibility**: Configure etcd server settings via application properties.
- **Thread Pool Management**: Customizable thread pools for handling etcd operations and long-running tasks.
- **Etcd Client and Watcher**: Easily fetch key-value pairs and watch for changes in the etcd store.

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Dependencies](#dependencies)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [Requirements](#requirements)
- [License](#license)

## Installation

Add the following dependency to your Maven `pom.xml`:

```xml

<dependency>
    <groupId>io.etcd</groupId>
    <artifactId>springi18n</artifactId>
    <version>0.0.2</version>
</dependency>
```

## Configuration

### Application Properties

Configure the etcd server connection and other settings in your `application.properties` file:

```properties
# Example configuration
etcd.server.hosts=http://localhost
etcd.server.port=2379
etcd.server.baseDir=/messages
etcd.server.baseDirTowatch=/messages
etcd.server.localesKey=/locales
etcd.server.connection.corePoolSize=2500
etcd.server.connection.maxPoolSize=3000
etcd.server.connection.queueCapacity=1000
etcd.server.connection.threadNamePrefix=etcd-conn-
etcd.server.connection.blockingCorePoolSize=10
etcd.server.connection.blockingMaxPoolSize=20
etcd.server.connection.blockingQueueCapacity=20
etcd.server.connection.blockingThreadNamePrefix=etcd-blocking-
```

### Java Configuration

You dont have to do anything here.

## Usage

### EtcdMessageSource

```
import impl.service.io.etcd.springi18n.EtcdMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    private final EtcdMessageSource etcdMessageSource;

    @Autowired
    public TranslationService(EtcdMessageSource etcdMessageSource) {
        this.etcdMessageSource = etcdMessageSource;
    }

    public String getTranslation(String key, Locale locale) {
        return etcdMessageSource.getMessage(key, locale);
    }
}
```

## Dependencies

### Excluding `protobuf-java`

This project excludes the `protobuf-java` dependency by default. If you need to include it or manage its version, you
can add it explicitly in your `build.gradle` or `pom.xml`:

#### Maven Example

```
<dependencies>
    <dependency>
        <groupId>your.dependency.group</groupId>
        <artifactId>your-dependency-artifact</artifactId>
        <version>version</version>
        <exclusions>
            <exclusion>
                <groupId>com.google.protobuf</groupId>
                <artifactId>protobuf-java</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
</dependencies>
```

Or you need to add the following dependency:

```
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>greater-then-3.18</version>
</dependency>
```

## Requirements

- greater or equeal Java 11
- Spring Boot 3.*
- etcd version 3 or greater (here 3.4 have been used)

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the [MIT License](LICENSE.md)
