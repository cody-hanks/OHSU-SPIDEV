import socket 
import threading
import json
import copy
import sys
from BED_Log import log
import BED_Input
import BED_Filling
import BED_Consumer

class server():
	def __init__(self):
		#constant replies 
		self.GENERAL_ERROR=["NAK",0]
		self.INVALID_REQUEST=["NAK",1]
		self.INVALID_PASSWORD=["NAK",2]
		self.Queue = None
		#dictionary of handlers 
		self.HANDLERS={}
		self.Server = threading.Thread(target=self.bed_server_loop,name="Server")
		self.Server.daemon = True
		self.write = threading.Thread()
		# setup handlers 
		# ["functionName"] = self.FunctionName
		self.HANDLERS["GETSAMPLE"] = self.GETSAMPLE
		self.HANDLERS["GETROUND"] = self.GETROUND
		self.HANDLERS["START"]=self.START
		self.HANDLERS["STOP"]=self.STOP




	# configure ADC for reading 1ADC for 2 IC's 
	def setADC(self,ADC):
		self.ADC = ADC
	
	def setSampler(self,Sampler):
		self.Sampler = Sampler
	
	def setFiller(self,filer):
		self.Filler = filer
	
	def setQueue(self,queue):
		self.Queue = queue

	#---------------------------------------------------
	def bed_server_loop(self):
		#create a socket to listen 
		sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
		sock.bind(('',50000))#all ip's port 50,000
		sock.listen(5)#listen for up to 5 connections 
	
		#loop to listen for next connection
		while True:	
			# sockets accept 
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
				log(self.GENERAL_ERROR)
				log("RCV ="+ rcv_string)
				clisock.sendall(json.dumps(self.GENERAL_ERROR))
				clisock.close()
				continue
			#try to use handler to further handle the message 
			try:
				reply=self.HANDLERS[request](rcv_list)
			except:
				log("handler exception notfound "+ rcv_string)
				reply=self.INVALID_REQUEST
			#
			clisock.sendall(json.dumps(reply))
			clisock.close()
		#end while 

	def start_serv(self):
		self.Server.start()

	#---------------------------------------------------------




	# get sample rcv[1] = <channel> 
	def GETSAMPLE(self,rcv):
		reading = self.ADC.sample(rcv[1])
		log(reading)
		reply = ["ACK"]
		reply.append(reading[4])
		# reply [<ACK>,[<upper>,<lower>]]
		return reply
	

	# get a full list of samples from RCV[1] 
	#rcv[1] = [ch,ch...]
	def GETROUND(self,rcv):
		reply =["ACK"]
		self.ADC.continous_setup(rcv[1])
		for i in range(len(rcv[1])):
			reply.append(self.ADC.continous_next())
		return reply
		# reply [<ACK>,[[ch,<upper>,<lower>],[......]]
	

	#Start sampling the input to the queue 
	def START(self,rcv):
		reply =["ACK"]
		self.Sampler.setchanlist(rcv[2])
		self.Sampler.setfreq(rcv[1])
		self.Sampler.start()
		#self.Filler.getcurrentfilename()
		#self.Filler.start()
		
		consumer = BED_Consumer.Consumer(self.Queue)
		consumer.start()
		return reply
	
	def STOP(self,rcv):
		reply =["ACK"]
		self.Sampler.stop()
		#reply.append(self.Sampler.getqueue())
		#self.write = threading.Thread(target=self.Sampler.writequeue(),name="w")
		#self.write.start()
		#self.Filler.stop()
		#self.Filler.flush()
		self.Queue.put(None)
		return reply
	



