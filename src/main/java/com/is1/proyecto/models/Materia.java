package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("materias")
public class Materia extends Model {
    
    public int getCodigo() {
        return getInteger("codigo_mat");
    }

    public void setCodigo(int codigo) {
        set("codigo_mat", codigo);
    }

    public String getNombre() {
        return getString("nombre_mat");
    }

    public void setNombre(String nombre) {
        set("nombre_mat", nombre);
    }
}