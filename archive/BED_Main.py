import BED_Sampler
import BED_Input
import BED_Queue 
import BED_GlobalConstants
import BED_Filling
import BED_Ntp
import time
import datetime
import BED_Server
import multiprocessing
import BED_Consumer
from BED_GlobalConstants import l_mode
from xml.dom.minidom import parse

@profile
def app():
	#create the ADC
	# NOTE name ADC global used in server 
	ADC = BED_Input.ADC()


	#Create the global queue
	#
	queue = multiprocessing.JoinableQueue()



	#filer = BED_Filling.Filling("01",l_mode.verbose,queue) 
	#filer.cfgver =1
	#filer.subnet =0


	sampler = BED_Sampler.Sampler(l_mode.debug,ADC,queue,[])


	#ntp = BED_Ntp.serverntp()
	#dt=datetime.datetime.fromtimestamp(ntp.response())
	#print dt.strftime('%Y-%m-%d %H:%M:%S')
	#print time.ctime(ntp.response())


	#create the server
	print 'start server'
	serv = BED_Server.server()
	serv.setADC(ADC)
	serv.setSampler(sampler)
	serv.setQueue(queue)
	# start server 
	serv.start_serv()

	# testing 
	sampler.setchanlist([0,3,1])
	sampler.setfreq(1)
	sampler.start()
		
	consumer = BED_Consumer.Consumer(queue)
	consumer.start()
	
	time.sleep(60*10)
		
	sampler.stop() 
	queue.put(None)


	#while True:
	#	time.sleep(1)
	
	print 'end server'



app() 


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
