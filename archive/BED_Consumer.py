import multiprocessing
import time
from BED_Log import log

class Consumer(multiprocessing.Process):
	def __init__(self,task_queue):
		multiprocessing.Process.__init__(self)
		self.task_queue = task_queue
		self.f = open('/media/sdd/files/test.txt','a')
		
	def run(self):
		proc_name = self.name
		while True:
			looptime= time.time()
			next_task = self.task_queue.get() 
			if (next_task is None):
				log('exit')
				self.task_queue.task_done()
				break
			self.f.write(next_task())
			self.task_queue.task_done()
			log('loop time %f'%(time.time()-looptime))
		self.f.close()
		return 
	
	
