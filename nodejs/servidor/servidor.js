const http = require('http');
const url = require('url');
const mysql = require('mysql2');

// Configuración del servidor
const hostname = '192.168.0.122'; // Escucha en la dirección IP local
const port = 3000; // Puerto en el que escuchará el servidor

// Configuración de la base de datos
const db = mysql.createConnection({
  host: 'sql3.freesqldatabase.com',
  user: 'sql3657983',
  password: 'ZzRpEBZpe8',
  database: 'sql3657983',
});

// Crear un servidor HTTP
const server = http.createServer((req, res) => {
  const parsedUrl = url.parse(req.url, true);

  // Conexion base de datos
  db.connect((err) => {
    if (err) {
      console.error('Error al conectar a la base de datos:', err);
    } else {
      console.log('Conexión a la base de datos exitosa');
    }
  });

  // Ruta para recibir datos desde el programa Arduino
  if (parsedUrl.pathname === '/data' && req.method === 'GET') {
    // Procesa los datos recibidos
    const queryData = parsedUrl.query;
    const humidity = queryData.h;
    const temperature = queryData.t;
    const humidityDHT = queryData.hd;

    // Inserta los datos en una tabla de la base de datos junto con la fecha actual en la zona horaria de México
    const sql = "INSERT INTO registrosArboles (humedad, temperaturaAmb, humedadAmb, fecha) VALUES (?, ?, ?, CONVERT_TZ(NOW(), '+00:00', '-05:00'))";
    db.query(sql, [humidity, temperature, humidityDHT], (err, result) => {
      if (err) {
        console.error('Error al insertar datos en la base de datos:', err);
      } else {
        console.log('Datos insertados en la base de datos con éxito.');
      }
    });

    // Aquí puedes realizar acciones con los datos, como almacenarlos en una base de datos o hacer un registro.
    console.log(`Datos recibidos - Humedad: ${humidity}%, Temperatura: ${temperature}°C, Humedad ambiente: ${humidityDHT}%`);

    // Responde al Arduino para confirmar la recepción
    res.statusCode = 200;
    res.setHeader('Content-Type', 'text/plain');
    res.end('Datos recibidos con éxito.\n');
  } else {
    res.statusCode = 404;
    res.setHeader('Content-Type', 'text/plain');
    res.end('Ruta no encontrada.\n');
  }
});

// Cierra la conexión a la base de datos cuando el servidor se cierra
server.on('close', () => {
  db.end((err) => {
    if (err) {
      console.error('Error al cerrar la conexión a la base de datos:', err);
    } else {
      console.log('Conexión a la base de datos cerrada.');
    }
  });
});

// Inicia el servidor y escucha en el puerto y la dirección IP especificados
server.listen(port, hostname, () => {
  console.log(`Servidor Node.js en ejecución en http://${hostname}:${port}/`);
});
