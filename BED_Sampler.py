import random
import BED_GlobalConstants
from BED_Log import log


class Sampler():
	
	def __init__(self,mode):
		self.mode = mode
		self._samples = 8
	
	def get_samples(self):
		#TODO update sample to get from IC's
		s=[]
		for i in range(self._samples):
			s.apend(random.randint(0,65336))
		if(mode >= l_mode.debug): 
			log('GOT 8 samples')	
		return s
