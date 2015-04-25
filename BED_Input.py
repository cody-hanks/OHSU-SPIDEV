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
		upper = upper | (self.INx <<1)
		upper = upper | self.BW
		lower = 0 
		lower = lower | ( self.REF <<5)
		lower = lower | ( self.SEQ <<3)
		lower = lower | ( self.RB <<2)
		return [upper,lower]
		
	def sample(self,channel):
		log('getting sample ch %i' % channel)
		rcv =[]
		self.INx = channel
		self.CFG = 1
		config = self.cfg()
		log(config)
		rcv.append(config)
		rcv.append(self.spi.xfer2(config))
		self.CFG = 0 
		config = self.cfg()
		rcv.append(config)
		rcv.append(self.spi.xfer2(config))
		rcv.append(self.spi.xfer2(config))
		return rcv
