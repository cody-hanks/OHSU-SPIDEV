# Cody Hanks
# 4/18/2015 
# Filling this section handles file rotation and file write settings 
import BED_GlobalConstants
import datetime
import time
import os
import BED_Ntp
import BED_Queue
import threading
import gc
import objgraph
from BED_Log import log
from BED_GlobalConstants import l_mode

class Filling:
	
	# setup filing object,  get current name (note will be updated later) 
	def __init__(self,site_id,mode,queue):
		self.ntp = BED_Ntp.serverntp()		
		#self.getfolder()	
		self.mode = mode
		self.queue = queue	
		self.siteid = site_id
		self.cfgver ="x"
		self.subnet ="x"
		self.timer = threading.Thread(target=self.dequeue,name="writer")
		self._continue = True
		self.currentfilename = self.getnewfilename()
		self.string=""

	def gettime(self):
		dt = datetime.datetime.fromtimestamp(self.ntp.response())
		return dt
	
	#get the current file name 
	def getcurrentfilename(self):
		return self.currentfilename
	
	# generate new file name (note not unique file may already exist) 
	def getnewfilename(self):
		try:
			now = self.gettime()
			log('ntp time')	
		except:
			now = datetime.datetime.now() 
			log('local time')
	
		name = now.strftime("%y%m%d_%H%M%S_")
		name = name + self.siteid + "_" + self.cfgver
		name = name + "_" + self.subnet
		return name
	
	def output(self,sample):
		self.string += '%s\n'%sample
		if(len(self.string) >4000000):
			if(self.mode >= l_mode.debug):
				log('str length 4M or > ')
			self.timer = threading.Thread(target=self.writefile,name="wf")
			self.timer.start()
		if(self.mode >= l_mode.verbose):
			log('str length %i'%len(self.string))
		gc.collect() 
	
	def writefile(self):
		thistime = time.time()
		f = open('/media/sdd/files/'+self.currentfilename,'a')
		f.write(self.string)
		f.close()
		taken = time.time() - thistime 
		if( self.mode >= l_mode.debug):
			log('%s byte to file %s in %f seconds'%(len(self.string),self.currentfilename,taken)) 
		self.string=""
	
	def flush(self):
		f = open('/media/sdd/files/'+self.currentfilename,'a')
		f.write(self.string)
		f.close()
		self.string =""	
		
	def dequeue(self):
		if(self.queue.length() >1):
			#log('queue length %i'%self.queue.length())
			f = open('/media/sdd/files/'+self.currentfilename,'a')
			var =[]
			cnt = 0 
			strval =""
			tick = time.time()
			while self.queue.length() >0:
				tick = time.time()
				var = self.queue.pop2()
				for i in range(len(var)):
					if(len(var[i])==3):
						strval += '%s,%i,'%(var[i][0],var[i][1]<<8|var[i][2])
					else:
						strval += '%s,'%var[i]
				del var[0:]
				strval+= "\n"
				if len(strval) >4000000:
					log('time to read %f'%(time.time()-tick))
					tick=time.time()
					f.write(strval)
					log('time to write %f'%(time.time()-tick))
					strval =""
					gc.collect()
					objgraph.show_most_common_types()
			
			f.write(strval)	
			f.close()
			log('run finished')
			if self._continue:
				time.sleep(3)
				self.timer = threading.Thread(target=self.dequeue,name="writer")
				self.timer.start()	

	def start(self):
		self.timer.start()
	
	def stop(self):
		self._continue = False

	#def getfolder(self): 
	#	if not os.path.exists(BED_GlobalConstants.logfilebasedir):
	#		os.mkdirs(BED_GlobalConstants.logfilebasedir)
	#		os.chmod(BED_GlobalConstants.logfilebasedir,0777)
	#	if not os.path.exists(datafilebasedir):
	#		os.mkdirs(datafilebasedir)
	#		os.chmod(datafilebasedir,0777)
		
	
