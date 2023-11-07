import telebot

#Conexión con el Bot
TOKEN = '6953803560:AAHBI_I4fH_O1dyvA8_UZFdNRLVa1pYUuCo'
bot = telebot.TeleBot(TOKEN)

@bot.message_handler(commands = ['start'])
def send_Welcome(message):
    bot.reply_to(message, 'Comando start \n Puedes usar el comando /help')

@bot.message_handler(commands = ['help'])
def send_help(message):
    bot.reply_to(message, 'Comando help')

@bot.message_handler(func=lambda n: True)
def echo_all(message):
    bot.reply_to(message, message.text)

if __name__ == "__main__":
    bot.polling(none_stop=True)