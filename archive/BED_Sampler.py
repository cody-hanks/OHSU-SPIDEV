import BED_GlobalConstants
import BED_Input
import BED_Queue
import BED_Task
from BED_GlobalConstants import l_mode
from BED_Log import log
import threading
import time
import io
import sys
import json
import gc

class Sampler():
	def __init__(self,Mode,ADC,Queue,ChannelList):
		self.mode = Mode
		self.ADC = ADC
		#self.File = File
		self.Queue = Queue
		self.ChanList = ChannelList
		self.sampleThread = threading.Thread(target=self.freqsample,name="Sampler")
		self.cont = True		
		self.freq =100.0
		self.wait = (1.0/(8.0*self.freq))*10.0*1000.0
		
	
	#@profile	
	def freqsample(self):
		self.sampletime= time.time()*10.0 *1000.0
		looptime = time.time()
		sample =[]
		cnt =0
		samplestr=""
		while self.cont: 
			self.Queue.put(BED_Task.Task(self.ADC.continous_next()))
			cnt=cnt +1
			if(cnt == len(self.ChanList)): 
				self.Queue.put(BED_Task.Taskln())
				cnt =0
			#var = self.ADC.continous_next()
			#sample.append(self.ADC.continous_next())
				#self.Queue.push(sample) 
			#if((time.time())-self.looptime)>15):
			#	self.looptime = self.looptime +15 
			#	self.Queue.put(BED_Task.TaskTime())
			if(self.mode >= l_mode.verbose):
				print (time.time()-thistime)
			while (((time.time()*10.0*1000.0)-self.sampletime)<self.wait):
				pass
			self.sampletime = self.sampletime + self.wait

	
	def start(self):
		self.sampleThread = threading.Thread(target=self.freqsample,name="Sampler")
		self.ADC.continous_setup(self.ChanList)	
					
		self.cont = True
		self.sampleThread.start()
	
	def stop(self):
		self.cont =False
		
	def setfreq(self,freq):
		if (self.mode >= l_mode.debug):
			log("freq %f" % freq)
		self.freq = freq
		if (self.mode >= l_mode.debug):
			log("Chan list %i" % len(self.ChanList))
		self.wait = (1.0/(len(self.ChanList)* self.freq))*10.0*1000.0
		if (self.mode >= l_mode.debug):
			log("Wait %f" % self.wait)

	def setchanlist(self,chanlist):
		self.ChanList =chanlist

	#def getqueue(self):
	#	return self.Queue.length()		
	
	#def writequeue(self):
	#	f=open('test.txt','a')
	#	vals=[]
	#	while (self.Queue.length()>0):
	#		vals = self.Queue.pop()
	#		f.write(json.dumps(vals))
	#		f.write("\n")		
	#	f.close()
