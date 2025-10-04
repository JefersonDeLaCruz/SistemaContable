fetch("http://localhost:8080/api/users")
  .then(response => response.json())
  .then(data => {
    const usersList = document.getElementById("users");
    data.forEach(user => {
      const li = document.createElement("li");
      li.textContent = `${user.id} - ${user.name} (${user.email})`;
      usersList.appendChild(li);
    });
  })
  .catch(error => console.error("Error en fetch:", error));
