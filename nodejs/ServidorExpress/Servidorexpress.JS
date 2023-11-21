const express = require('express');
const mysql = require('mysql2');
const app = express();
const port = 3001;
const { PythonShell } = require("python-shell");
const cors = require('cors');


// Configuración de la base de datos
const db = mysql.createConnection({
  host: 'sql3.freesqldatabase.com',
  user: 'sql3657983',
  password: 'ZzRpEBZpe8',
  database: 'sql3657983',
});

// Middleware para procesar solicitudes con datos JSON
app.use(express.json());

// Conexión a la base de datos
db.connect((err) => {
  if (err) {
    console.error('Error al conectar a la base de datos:', err);
  } else {
    console.log('Conexión a la base de datos exitosa');
  }
});

// Ruta para recibir datos desde el programa Arduino
app.post('/arduino', (req, res) => {
  // Procesa los datos recibidos
  const humidity = req.body.h;
  const temperature = req.body.t;
  const humidityDHT = req.body.hd;
  console.log(typeof(humidity)); 
  // Inserta los datos en una tabla de la base de datos junto con la fecha actual en la zona horaria de México
 const sql = "INSERT INTO registrosArboles (humedad, temperaturaAmb, humedadAmb, fecha, hora) VALUES (?, ?, ?,CURDATE(), CONVERT_TZ(NOW(), '+00:00', '-06:00'))";
  db.query(sql, [humidity, temperature, humidityDHT], (err, result) => {
    if (err) {
      console.error('Error al insertar datos en la base de datos:', err);
      res.status(500).send('Error al insertar datos en la base de datos');
    } else {
      console.log('Datos insertados en la base de datos con éxito.');
      res.status(200).send('Datos recibidos con éxito.\n');
    }
  });

  // Aquí puedes realizar acciones con los datos, como almacenarlos en una base de datos o hacer un registro.
  console.log(`Datos recibidos - Humedad: ${humidity}%, Temperatura: ${temperature}°C, Humedad ambiente: ${humidityDHT}%`);
});

app.post('/escritorio', (req, res) => {
  // Procesa los datos recibidos
  const notificacion = req.body.noti;

  console.log(`"Si se recibio el JSON": ${notificacion }%`);
  res.send('Recibido mediante GET');
 
});   

app.get("/escritorio", function (req, res) {
  res.send("Hola DESDE SERVIDOR!");
}); 


/*PETICION REACT
app.get('/api/data', (req, res) => {
  const sqlQuery = 'SELECT * FROM registrosArboles'; 
  db.query(sqlQuery, (err, results) => {
    if (err) {
      console.error('Error al ejecutar la consulta:', err);
      return res.status(500).json({ error: 'Error en la base de datos' });
    }
    res.json(results);
  });
});*/

app.get('/react', (req, res) => {
  const datos = {
    mensaje: 'Hola desde Node.js',
    // Puedes agregar más datos según sea necesario
  };

  res.json(datos);
});

// Configurar middleware para permitir solicitudes CORS (si es necesario)
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
  next();
});
// Ruta para manejar las solicitudes no encontradas
app.use((req, res) => {
  res.status(404).send('Ruta no encontrada.\n');
});

// Inicia el servidor Express
app.listen(port, () => {
  console.log(`Servidor Express en ejecución en el puerto ${port}`);
});
