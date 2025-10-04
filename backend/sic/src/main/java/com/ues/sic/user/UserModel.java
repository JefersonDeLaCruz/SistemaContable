package com.ues.sic.user;


import jakarta.persistence.*;



@Entity
@Table(name = "users")
public class UserModel {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) //equivale a autoincremental pk
    private Long id;


    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;


    //getter y setters

    public Long getId(){
        return this.id;
    }
    public void setId(Long id){
        this.id = id;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getEmail(){
        return this.email;
    }

    public void setEmail(String email){
        this.email = email;
    }

}
