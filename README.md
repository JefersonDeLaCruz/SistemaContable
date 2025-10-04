Avla

Despues de haber clonado el repo
lavantar el compose
y listo, solo queda ejecutar el proyecto de java, desde la clase SicApplication.java

si queres tener un apoyo visual de la base podes conectarte a esta desde un gestor, yo use DBeaver.







el proceso para todo el proyecto dockerizado 
es el siguiente 

mvn clean package -DskipTests
docker compose build
docker compose up

pero esto hasta el final.