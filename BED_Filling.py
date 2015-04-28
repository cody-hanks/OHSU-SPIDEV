# Cody Hanks
# 4/18/2015 
# Filling this section handles file rotation and file write settings 
import GlobalConstants
import datetime
import time

class Filling:
	
	# setup filing object,  get current name (note will be updated later) 
	def __init__(self,site_name,mode):
		self.site_name = site_name
		self.currentfilename = self.getnewfilename()
		self.getfolder()		

	#get the current file name 
	def getcurrentfilename(self):
		return self.currentfilename
	
	# generate new file name (note not unique file may already exist) 
	def getnewfilename(self):
		now = datetime.datetime.now()
		y = now.year
		m = now.month
		d = now.day
		h = now.hour
		if (h >= 12) :
			pm = 1
		else: 
			pm = 0
		return ("%i%i%i%i_%s.txt" % (y,m,d,pm,self.site_name))
		#print ("%i %i %i %i" % (y,m,d,h))
		#print now
	
	def getfolder(self): 
		if not os.path.exists(logfilebasedir):
			os.mkdirs(logfilebasedir)
			os.chmod(logfilebasedir,0777)
		if not os.path.exists(datafilebasedir):
			os.mkdirs(datafilebasedir)
			os.chmod(datafilebasedir,0777)
		
	
