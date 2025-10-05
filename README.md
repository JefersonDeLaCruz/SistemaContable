Avla

1.Clonar el repositorio
2. en terminal: docker build -f dockerfile.node -t node-img-test .
3.docker compose up --build
4.Solo queda ejecutar el proyecto de java, desde la clase SicApplication.java

si queres tener un apoyo visual de la base podes conectarte a esta desde un gestor, yo use DBeaver.


el proceso para todo el proyecto dockerizado 
es el siguiente 

mvn clean package -DskipTests
docker compose build
docker compose up

pero esto hasta el final.