import BED_GlobalConstants
import BED_Input
import BED_Queue
from BED_GlobalConstants import l_mode
from BED_Log import log
import threading
import time
import io
import sys
import json

class Sampler():
	def __init__(self,Mode,ADC,Queue,ChannelList):
		self.mode = Mode
		self.ADC = ADC
		self.Queue = Queue
		self.ChanList = ChannelList
		self.sampleThread = threading.Thread(target=self.sample,name="Sampler")
		self.sampleThread.daemon = True
		self.cont = True		

	
	def sample(self):
		self.sampletime = time.time()*1000
		self.looptime = time.time()
		while self.cont:
			sample =[]
			for i in range(len(self.ChanList)):
				sample.append(self.ADC.continous_next())
			self.Queue.push(sample)
			if(((time.time())-self.looptime)>15):
				self.looptime = self.looptime +15
				self.Queue.push([time.localtime()])
			while (((time.time()*1000)-self.sampletime)<.12):
				pass
			self.sampletime = self.sampletime +.12
		
	def start(self):
		self.sampleThread = threading.Thread(target=self.sample,name="Sampler")
		self.ADC.continous_setup(self.ChanList)				
		self.cont = True
		self.sampleThread.start()
	
	def stop(self):
		self.cont =False
		
	def setchanlist(self,chanlist):
		self.ChanList =chanlist

	def getqueue(self):
		return self.Queue.length()		
	
	def writequeue(self):
		f=open('test.txt','a')
		vals=[]
		while (self.Queue.length()>0):
			vals = self.Queue.pop()
			f.write(json.dumps(vals))
			f.write("\n")		
		f.close()
