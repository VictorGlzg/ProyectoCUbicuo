import mysql.connector
import requests
import pandas as pd
# https://www.w3schools.com/python/python_mysql_select.asp
conn = mysql.connector.connect(host='sql3.freesqldatabase.com', password='ZzRpEBZpe8', user='sql3657983', database='sql3657983')

#url = 'http://localhost:3000/auth'
#r = requests.get(url)
#print(r.text)

#Confirmar la conexion con mysql
if conn.is_connected():
    print("Connection established..")

def imprimir(sentencia):
    mycursor = conn.cursor()
    mycursor.execute(sentencia)
    myresult = mycursor.fetchall()
    #print(type(myresult))
    return myresult

#Guardar el resultado en un TXT
'''with open("file.txt", "w") as output:
    output.write(str(myresult))'''

datosArboles = imprimir("SELECT * FROM datosArboles")

dADataFrame = pd.DataFrame(datosArboles, columns=["id_arbol","nombre","nombreCientifico",
                                                  "humMinima","humMaxima","tempAmbientalMinima","humAmbientalMinima"])

registrosArboles = imprimir("SELECT * FROM registrosArboles")

rADataFrame = pd.DataFrame(registrosArboles, columns=["id_registro","nombre","nombreCientifico","humedad",
                                                      "temperaturaAmb","humedadAmb","fecha","buenaCondicion"])

print(dADataFrame)
print(rADataFrame)

'''
def update():
    # Llamar al metodo getUpdates del bot haciendo una peticion HTTPS (se obtiene una respuesta codificada)
    respuesta = requests.get(URL + "getUpdates")

    # Decodificar la respuesta recibida a formato UTF8 (se obtiene un string JSON)
    mensajes_js = respuesta.content.decode("utf8")

    # Convertir el string de JSON a un diccionario de Python
    mensajes_diccionario = json.loads(mensajes_js)

    # Devolver este diccionario
    return mensajes_diccionario
'''