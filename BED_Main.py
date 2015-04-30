import BED_Sampler
import BED_Input
import BED_Queue 
import BED_GlobalConstants
import BED_Filling
import time
import BED_Server
from BED_GlobalConstants import l_mode
from xml.dom.minidom import parse


#create the ADC
# NOTE name ADC global used in server 
ADC = BED_Input.ADC()


#Create the global queue
#
queue = BED_Queue.Queue(l_mode.debug)

sampler = BED_Sampler.Sampler(l_mode.debug,ADC,queue,[])


#create the server
print 'start server'
serv = BED_Server.server()
serv.setADC(ADC)
serv.setSampler(sampler)

serv.start_serv()


while True:
	time.sleep(1)
print 'end server'






#dom1 = parse('config.xml')
#print dom1.documentElement.tagName
#print dom1.documentElement.attributes.keys()
#print dom1.documentElement.attributes["mode"].value
#print dom1.documentElement.attributes["none"].value
#val = dom1.getElementsByTagName("sitename")[0].firstChild.nodeValue

#queue = BED_Queue.Queue(l_mode.debug)


#queue.push([1,2,3,4,5,6])
#print queue.pop()

#filer = BED_Filling.Filling(val.strip(),l_mode.debug)
#print filer.getcurrentfilename()
#filer.currentfilename()
