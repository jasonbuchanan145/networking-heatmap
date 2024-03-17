# Networking Heatmap

## Summary
A visualization tool chain for building a real time heatmap of network traffic

## System Requirements
* Tshark be installed on the host system and that sudo access is not required to run against a network interface (see [this thread ](https://osqa-ask.wireshark.org/questions/7976/wireshark-setup-linux-for-nonroot-user/) for an example)
* Docker cli
* Java 21
* A current version of maven

## Run
### Configuration
Set the device you want to sniff packets on in java-api/src/resources/application.properties. By default this property is set for Windows default wireless interface "Wi-Fi". 

On linux you will probably want to change this to eth0 or whatever the network interface you want to run against. 
### Start
Start the docker compose for the Prometheus and Grafana containers
```shell
docker-compose up
```
Go to the java-api package and start the spring boot app
```shell
mvn spring-boot:run
```
Navigate to localhost:9090 and view the output in the prometheus container

Navigate to localhost:3030 and view the output in grafana

## Work left to do
Heat map configurations are not stored in the grafana/ directory, only initialization settings in grafana/provisioning/datasources to link it automatically to prometheus since I am still changing visualizations. Once I settle on a reasonable default visualization I'll export the configuration
