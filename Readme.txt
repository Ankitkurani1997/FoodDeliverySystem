# Restaurant Service

Enter the respective directory of the Restaurant microservice

### Compile the project and generate the JAR file
./mvnw package

### Build the docker image
docker build -t restaurant-service .

### Run the docker image
docker run -p 8080:8080 --rm --name restaurant --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt restaurant-service


### Stop the container
docker stop restaurant

### Remove the docker image
docker image rm restaurant-service


# Delivery Service

Enter the respective directory of the Delivery microservice

### Compile the project and generate the JAR file
./mvnw package

### Build the docker image
docker build -t delivery-service .

### Build the docker image
docker run -p 8081:8080 --rm --name delivery --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt delivery-service

### Stop the container
docker stop delivery

### Remove the docker image
docker image rm delivery-service


# Wallet Service

Enter the respective directory of the Wallet microservice

### Compile the project and generate the JAR file
./mvnw package

### Build the docker image
docker build -t wallet-service .

### Build the docker image
docker run -p 8082:8080 --rm --name wallet --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt wallet-service

### Stop the container
docker stop wallet

### Remove the docker image
docker image rm wallet-service

