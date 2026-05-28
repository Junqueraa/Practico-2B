package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table; // 1. Importamos la anotación

@Table("profesores") // 2. Forzamos el nombre exacto de la tabla en SQLite
public class Profesor extends Model {
}