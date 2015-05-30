import time

class Task(object):
	def __init__(self,sample):
		self.ch = sample[0]
		self.value = (sample[1]<<8)|sample[2]
	
	def __call__(self):
		return ',%s,%i'%(self.ch,self.value)
	
	def __str__(self):
		return '%s'%self.value

class Taskln(object):
	def __init__(self):
		pass	
	def __call__(self):
		return "\n"

class TaskTime(object):
	def __init__(self):
		pass
	def __call__(self):
		return time.strftime("%H:%M:%S") 

