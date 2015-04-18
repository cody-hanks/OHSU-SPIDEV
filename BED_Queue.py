import threading
import BED_GlobalConstants
from BED_GlobalConstants import l_mode
from BED_Log import log

class Queue:
	
	def __init__(self,mode):
		self.in_stack =[]
		self.out_stack=[]
		self.lock = threading.RLock()
		self.mode = mode


	#append object to in queue 	
	def push(self,obj):
		#Gain write lock 
		with self.lock:
			self.in_stack.append(obj)
	
	#pop of the FIFO list 
	def pop(self):
		with self.lock:
			if not self.out_stack:
				self.in_stack.reverse()
				self.out_stack = self.in_stack
				self.in_stack =[]
				if self.mode >= l_mode.debug:
					log('Transfered %i records from input to output' % len(self.out_stack))
			return self.out_stack.pop()
	
	# get total length of all 
	def length(self):
		with self.lock:
			return (len(self.out_stack) + len(self.in_stack))

