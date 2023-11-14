import mysql.connector
import json
import requests

# https://www.w3schools.com/python/python_mysql_select.asp
conn = mysql.connector.connect(host='sql3.freesqldatabase.com', password='ZzRpEBZpe8', user='sql3657983', database='sql3657983')
mycursor = conn.cursor()

# PETICION POST AL SERVIDOR
url = 'https://chubby-crabs-march.loca.lt/escritorio'

#Confirmar la conexion con mysql
if conn.is_connected():
    print("Connection established..")

def consulta(sentencia):
    mycursor.execute(sentencia)
    myresult = mycursor.fetchall()
    return myresult
def update():
    # Llamar al metodo get
    respuesta = requests.get(url)

    # Decodificar la respuesta recibida a formato UTF8
    mensajes_js = respuesta.content.decode("utf8")

    # Convertir el string de JSON a un diccionario de Python
    mensajes_diccionario = json.loads(mensajes_js)

    # Devolver este diccionario
    return respuesta

#Guardar el resultado en un TXT
'''with open("file.txt", "w") as output:
    output.write(str(myresult))'''
#Obtener el ultimo registro no clasificados para clasificado.
#ORDER BY id_registro DESC LIMIT 1
registrosArboles = consulta("SELECT * FROM registrosArboles WHERE buenaCondicion IS NULL ORDER BY id_registro DESC LIMIT 1")
#id_registro, id_arbol,humedad,temperaturaAmb,humedadAmb,fecha,hora,buenaCondicion
# 0             1           2         3          4        5     6        7

idTipoArbol = 1;
# registrosArboles[0][0] obtener el id del arbol
datosArboles = consulta("SELECT * FROM datosArboles WHERE id_arbol ="+str(idTipoArbol))
#id_arbol, nombre,nombreCientifico,humMinima,humMaxima,tempAmbientalMinima,humAmbientalMinima
# 0          1          2               3        4              5                   6
humMinima = datosArboles[0][3]
humMaxima = datosArboles[0][4]

def clasificar():
    id = arbol[0]
    humedad = arbol[2]
    temperaturaAmb = arbol[3]
    humedadAmb = arbol[4]
    if humedad < humMinima or humedad > humMaxima:
        print("El registro: " + str(arbol[0]) + " MALA CONDICION")
        estado = "Mal estado"
        condicion = 0
    else:
        print("El registro: " + str(arbol[0]) + " BUENA CONDICION")
        estado = "Buen estado"
        condicion = 1
    # Establecer condicion en BD
    mycursor.execute("UPDATE registrosArboles SET buenaCondicion=" + str(condicion) + " WHERE id_registro = " + str(id))
    conn.commit()
    print(mycursor.rowcount, "record(s) affected")

    myobj = {'noti': 'Los datos censados actuales son: humedad:'+str(humedad)+'temperatura ambiental:'+str(temperaturaAmb)
             +'humedad ambiental:'+str(humedadAmb)+' el arbol se encuentra en: '+estado}
    requests.post(url, json=myobj)
    print(requests.get(url))

#CLASIFICACION DE ACUERDO A HUMBRAL DE HUMEDAD
for arbol in registrosArboles:
    clasificar()


#Recibir peticiones del servidor
'''
import socket

host = '127.0.0.1'
port = 80
buffer_size = 20  

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((host, port))
s.listen(1)

conn, addr = s.accept()
print 'Connection address:', addr
while 1:
    data = conn.recv(buffer_size)
if not data: break
print("received data:", data)
conn.send(data)  # echo
conn.close()
'''