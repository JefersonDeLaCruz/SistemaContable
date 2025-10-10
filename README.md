Avla

1.Clonar el repositorio
2. en terminal: docker build -f dockerfile.node -t node-img-test .
3.docker compose up --build
4.Solo queda ejecutar el proyecto de java, desde la clase SicApplication.java

si queres tener un apoyo visual de la base podes conectarte a esta desde un gestor, yo use DBeaver.

**Para acceder con el usuario por defecto**
Si no tienen un usuario por defecto solo corran la aplicación de java y en la terminal les va a dar el usuario/gmail y el password, ahí solo lo ingresan y ya se tiene digamos la funciuonalidad que tendría un admin normal, esto es solo para esta fase del proyecto puesto que estamos probando en localhost, de igual manera en git no se publica nada referente a la seguridad y si una persona quisiera acceder digamos lo haría de manera local sin comprometer el proyecto

**en caso de que ustedes quieran acceder o configurar su propio usuario solo cambien lo que viene en la aplicattion propierities**

el proceso para todo el proyecto dockerizado 
es el siguiente 

mvn clean package -DskipTests
docker compose build
docker compose up

pero esto hasta el final.