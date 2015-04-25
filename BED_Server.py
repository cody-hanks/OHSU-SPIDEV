import socket 
import threading
import json
import copy
import sys
from BED_Log import log
import BED_Input



class server():
	def __init__(self):
		#constant replies 
		self.GENERAL_ERROR=["NAK",0]
		self.INVALID_REQUEST=["NAK",1]
		self.INVALID_PASSWORD=["NAK",2]

		#dictionary of handlers 
		self.HANDLERS={}
		self.Server = threading.Thread(target=self.bed_server_loop,name="Server")
		self.Server.daemon = True
		self.HANDLERS["GETSAMPLE"] = self.GETSAMPLE

	def setADC(self,ADC):
		self.ADC = ADC



	#---------------------------------------------------
	def bed_server_loop(self):
		#create a socket to listen 
		sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
		sock.bind(('',50000))#all ip's port 50,000
		sock.listen(5)#listen for up to 5 connections 
	
		#loop to listen for next connection
		while True:	
			clisock,(remhost,remport) = sock.accept()
			rcv_string = clisock.recv(10240)
			if rcv_string == "":
				#nothing recived ignore connection
				clisock.close()
				continue
	
			# try to decode message 
			try:
				rcv_list = json.loads(rcv_string)
				request = rcv_list[0]
			except:
				#return general 
				log(GENERAL_ERROR)
				log("RCV ="+ rcv_string)
				clisock.sendall(json.dumps(GENERAL_ERROR))
				clisock.close()
				continue
			#try to use handler to further handle the message 
			try:
				reply=self.HANDLERS[request](rcv_list)
			except:
				log("handler exception notfound "+request)
				reply=self.INVALID_REQUEST
			#
			clisock.sendall(json.dumps(reply))
			clisock.close()
		#end while 



	def start_serv(self):
		self.Server.start()

	#---------------------------------------------------------

	def GETSAMPLE(self,rcv):
		log(self.ADC.sample(rcv[1]))
		#log(rcv[1])
		#log(ADC.sample(rcv[1]))
		#return ADC.sample(rcv[1]) 
		return ["ACK"]
