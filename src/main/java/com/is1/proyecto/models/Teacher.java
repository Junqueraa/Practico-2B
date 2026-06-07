package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;


@Table("teacher") // Esta anotación asocia explícitamente el modelo 'Teacher' con la tabla 'teacher' en la DB.
public class Teacher extends Model {

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getLastName() {
        return getString("lastName");
    }

    public void setLastName(String lastName) {
        set("lastName", lastName);
    }

    public Long getDni() {
        return getLong("dni");
    }

    public void setDni(Long dni) {
        set("dni", dni);
    }

    public Long getPhone() {
        return getLong("phone");
    }

    public void setPhone(Long phone) {
        set("phone", phone);
    }

    public String getAddress() {
        return getString("address");
    }

    public void setAddress(String address) {
        set("address", address);
    }
}