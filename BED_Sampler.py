import BED_GlobalConstants
import BED_Input
import BED_Queue
from BED_GlobalConstants import l_mode
from BED_Log import log
import threading
import time

class Sampler():
	def __init__(self,Mode,ADC,Queue,ChannelList):
		self.mode = mode
		self.ADC = ADC
		self.Queue = Queue
		self.ChanList = ChannelList
		self.sampleThread = threading.Thread(target=self.sample,name="Sampler")
		self.sampleThread.daemon = True
		self.cont = True		

	
	def sample(self):
		self.sampletime = time.time()*1000
		while self.cont:
			sample =[]
			for i in range(len(self.ChanList)):
				sample.append(self.ADC.continous_next())
			self.Queue.push(sample)
			if(((time.time())-self.looptime)>15):
				self.looptime = self.looptime +15
				self.Queue.push([time.localtime()])
			while (((time.time()*1000)-self.sampletime)<100):
				pass
			self.sampletime = self.sampletime +100
		
	def start(self):
		self.ADC.continous_setup(self.ChanList)				
		self.cont = True
		self.sampleThread.start()
	
	def stop(self):
		self.cont =False
		
	def setchanlist(self,chanlist):
		self.ChanList =chanlist
		
