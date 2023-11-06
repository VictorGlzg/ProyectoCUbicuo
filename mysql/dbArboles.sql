create database sql3657983;
-- drop database sql3657983;
use sql3657983;

create table datosArboles
(
	id_arbol int auto_increment primary key,
    nombre varchar(50),
    nombreCientifico varchar(50),
    humMinima float, -- Rango de humedad
    humMaxima float,
    tempAmbientalMinima float, -- Temperatura del ambiente 0 = no data.
    humAmbientalMinima float, -- Humedad del ambiente minima
    unique (id_arbol,nombreCientifico)
);

insert into datosArboles (nombre,nombreCientifico,humMinima,humMaxima,tempAmbientalMinima,humAmbientalMinima) values
("Árbol de mangos","Mangifera indica",60,80,25,50);

select * from datosArboles;

create table registrosArboles
(
	id_registro int auto_increment primary key,
    -- id_arduino int, -- ID del arduino conectado
    nombre varchar(50),
    nombreCientifico varchar(50),
    humedad float,
    temperaturaAmb float,
    humedadAmb float,
    fecha date,
    buenaCondicion bool
);

-- drop table registrosArboles;

insert into registrosArboles (nombre,nombreCientifico,humedad,temperaturaAmb,humedadAmb,fecha,buenaCondicion) values
("Árbol de mangos","Mangifera indica",65,24,77,"2023-10-29",true);

select * from registrosArboles;