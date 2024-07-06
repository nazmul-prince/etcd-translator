# Etcd Translator Library

## Overview

The Etcd Translator Library provides a convenient way to manage and fetch translation resources from an etcd key-value store. This library includes configuration for setting up connection pools and handling asynchronous or synchronous retrieval of translation data.

## Features

- **Asynchronous and Synchronous Retrieval**: Choose between async and sync approaches for loading messages.
- **Configuration Flexibility**: Configure etcd server settings via application properties.
- **Thread Pool Management**: Customizable thread pools for handling etcd operations and long-running tasks.
- **Etcd Client and Watcher**: Easily fetch key-value pairs and watch for changes in the etcd store.


## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [License](#license)

## Installation
Add the following dependency to your Maven `pom.xml`:

```xml
<dependency>
    <groupId>com.bdris</groupId>
    <artifactId>etcd-translator</artifactId>
    <version>1.0.0</version>
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
import com.bdris.etcdtranslator.service.impl.EtcdMessageSource;
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
## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
This project is licensed under the [MIT License](LICENSE.md)
