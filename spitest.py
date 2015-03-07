#!/usr/bin/python

# Lots of Pots Board Analog to Digital Converter test
# Modern Device
# www.moderndevice.com

import spidev    # import the spidev module
import time      # import time for the sleep function
 
spi = spidev.SpiDev()   # create a new spidev object
spi.open(1, 0)          # open bus 0, chip enable 0
spi.max_speed_hz = 200

 
def readadc(channel):
    if channel > 7 or channel < 0:
        return -1

    # spi.xfer2 sends three bytes and returns three bytes:
    # byte 1: the start bit (always 0x01)
    # byte 2: configure bits, see MCP3008 datasheet table 5-2
    # byte 3: don't care
    r = spi.xfer2([1, 8 + channel << 4, 0])

    # Three bytes are returned; the data (0-1023) is in the 
    # lower 3 bits of byte 2, and byte 3 (datasheet figure 6-1)    
    v = ((r[1] & 3) << 8) + r[2]

    return v;


while True:
	print 'out:'
	spi.writebytes([0xFF,0xAA,0xAA,0xFF])
	time.sleep(3)	
