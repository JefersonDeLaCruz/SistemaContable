package com.ues.sic.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> { //los params de JpaRepository son <Modelo = clase, Tipo de pk>


    

}
