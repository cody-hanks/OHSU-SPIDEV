import spidev
import time
from BED_Log import log
maxSpeed = 2000000
mode =0

#ADC7689 Const settings
BIPOL_VREF=0x00
BIPOL_COM =0x02
TEMP = 0x03
UNIPOL_PAIR = 0x04 
UNIPOL_COM = 0x06
UNIPOL_GND = 0x07

#ref buff 
INT_REF2 = 0x00
INT_REG4 = 0x01
EXT_TEMP = 0x02
EXT_INTBUFF = 0x03
EXT_NOTEMP = 0x06
EXT_INTBUFF_NOTEMP = 0x07


#SEQ
DISABLE=0x00
UPDATED=0x01
SCANTEM=0x02
SCANONL=0x04





class ADC():
	def __init__(self):
		log('in ADC setup')	
		self.spi = spidev.SpiDev()
		self.spi.open(1,0)
		self.ic =1
		self.spi.max_speed_hz = maxSpeed
		self.spi.mode = mode
		self.channellist=[]
		self.channellist_index=0
		#ADC 7689 config settings 
		self.CFG =0
		self.INCC = UNIPOL_GND
		self.INx = 0
		self.BW=1
		self.REF=INT_REG4
		self.SEQ=DISABLE
		self.RB = 1

	def cfg(self):
		upper = 0
		upper = upper | (self.CFG << 7)
		upper = upper | (self.INCC <<4)
		upper = upper | ((self.INx %8) <<1)
		upper = upper | self.BW
		lower = 0 
		lower = lower | ( self.REF <<5)
		lower = lower | ( self.SEQ <<3)
		lower = lower | ( self.RB <<2)
		return [upper,lower]
	
	def setic(self):
		pass
		
	def sample(self,channel):
		log('getting sample ch %i' % channel)
		rcv =[]
		if (channel >7):
			self.ic =2
		else:
			self.ic =1	
		self.setic()
		self.INx = channel
		self.CFG = 1 # write config word once
		config = self.cfg()
		log(config)
		rcv.append(config)
		rcv.append(self.spi.xfer2(config))
		self.CFG = 0 # dont write config word
		config = self.cfg()
		rcv.append(config)
		rcv.append(self.spi.xfer2(config))
		rcv.append(self.spi.xfer2(config))
		return rcv
	
	def continous_setup(self,channellist):
		self.channellist = channellist
		if (len(self.channellist) <2):
			return ["No Channellist"]
		self.setic()
		self.channellist_index =0
		self.INx = self.channellist[0]
		self.CFG = 1 # set to write config word
		log('continous setup configured with ')
		log(channellist)
		config = self.cfg()
		self.spi.xfer2(config)
		self.channellist_index += 1
		self.channellist_index = self.channellist_index %(len(self.channellist))
		self.INx = self.channellist[self.channellist_index]
		self.setic()
		self.CFG = 1 # set to write config word
		config = self.cfg()
		self.spi.xfer2(config)
		return [0]

	def continous_next(self):
		#log('continue next called')
		reply = []
		self.channellist_index += 1
		self.channellist_index = self.channellist_index %(len(self.channellist))
		self.INx = self.channellist[self.channellist_index]
		#log(self.INx)
		self.setic()
		self.CFG = 1 # set to write config word 
		config = self.cfg()
		#log(config)
		sampleresult= self.channellist_index +(len(self.channellist)-2)
		sampleresult= sampleresult % len(self.channellist)
		reply.append(self.channellist[sampleresult])
		#log(sampleresult)
		sample = self.spi.xfer2(config)
		#log(sample)
		reply.append(sample[0])
		reply.append(sample[1])
		return reply
	
		
		
