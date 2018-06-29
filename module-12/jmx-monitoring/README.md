# Instructions

This exercise re-uses the sample of `module-9`. It augments it by adding monitoring to the picture.

1. Make sure Kafka is running. From withing the `streams` folder run:

    ```
    $ docker-compose up -d
    ```

2. From within the **project** folder build the application:

    ```
    $ docker container run --rm \
        -v "$PWD":/home/gradle/project \
        -v "$HOME"/.gradle:/home/gradle/.gradle \
        -w /home/gradle/project \
        gradle:4.8.0-jdk8-alpine gradle build
    ```

3. From within the **project** folder run the application:

    ```
    $ docker container run -p 4444:4444 --rm \
        --net streams_streams-net \
        -v "$PWD"/build/libs/map-sample-0.1.0.jar:/app.jar \
        openjdk:8-jre-alpine java \
            -Dcom.sun.management.jmxremote \
            -Dcom.sun.management.jmxremote.authenticate=false \
            -Dcom.sun.management.jmxremote.ssl=false \
            -Djava.rmi.server.hostname=127.0.0.1 \
            -Dcom.sun.management.jmxremote.rmi.port=4444 \
            -Dcom.sun.management.jmxremote.port=4444 \
            -jar /app.jar
    ```

4. Start `JConsole` and make a **remote connection** to `localhost:4444`.

5. From within the `streams` folder create some input data:

    ```
    docker-compose exec kafka sh -c 'cat << EOF | kafka-console-producer \
        --broker-list kafka:9092 \
        --property "parse.key=true" \
        --property "key.separator=:" \
        --key-serializer=org.apache.kafka.common.serialization.StringSerializer \
        --value-serializer=org.apache.kafka.common.serialization.StringSerializer \
        --topic lines-topic
    1:"Kafka powers the Confluent Streaming Platform"
    2:"Events are stored in Kafka"
    3:"Confluent contributes to Kafka"
    EOF'
    ```

6. From within the `streams` folder double check that output was generated by your application:

    ```
    $ docker-compose exec kafka kafka-console-consumer \
        --bootstrap-server kafka:9092 \
        --key-deserializer=org.apache.kafka.common.serialization.StringDeserializer \
        --value-deserializer=org.apache.kafka.common.serialization.StringDeserializer \
        --from-beginning \
        --topic lines-lower-topic
    ```