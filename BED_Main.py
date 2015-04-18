import BED_Sampler
import BED_Queue 
import BED_GlobalConstants
from BED_GlobalConstants import l_mode

queue = BED_Queue.Queue(l_mode.debug)


queue.push([1,2,3,4,5,6])
print queue.pop()
