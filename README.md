# spring-kafka
This project uses Java 8, Spring boot 2, spring-kafka, spring-date-mongodb-reactive, spring-social, twitter4j, lombok....
This project has 2 modules. Sender and Receiver. 
1) Sender subscribe to twitter stream based on filter and write message to Kafka
2) receiver is Kafka subscriber, once it receives message, parse message, convert to java object and save to Mongo DB
