import mysql.connector
import requests

conn = mysql.connector.connect(host='sql3.freesqldatabase.com', password='ZzRpEBZpe8', user='sql3657983', database='sql3657983')

url = 'http://localhost:3000/auth'
r = requests.get(url)
print(r.text)

#Confirmar la conexion con mysql
if conn.is_connected():
    print("Connection established..")

mycursor = conn.cursor()

mycursor.execute("SELECT * FROM datosArboles")
myresult = mycursor.fetchall()
#print(type(myresult))

for x in myresult:
  print(x)

#Guardar el resultado en un TXT
with open("file.txt", "w") as output:
    output.write(str(myresult))

