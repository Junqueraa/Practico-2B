CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- Clave primaria 
    name TEXT NOT NULL UNIQUE,          -- Usamos el tipo "TEXT" para cadenas ya que es el recomendado para SQLite
    password TEXT NOT NULL          
);

CREATE TABLE teacher (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    dni TEXT UNIQUE NOT NULL, --nos aseguramos de que sea ÚNICO (por ende evitamos repeticiones)
    name TEXT NOT NULL,
    lastName TEXT NOT NULL,
    address TEXT, 
    phone INTEGER
);