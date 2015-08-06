#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/spi/spidev.h>
#include <linux/types.h>
#include <getopt.h>


//constant hardware file for spi communication
static const char *spi_name = "/dev/spidev1.0";
/*
 * 
 * open file and return file descriptor
 * 
 */
int openspi() 
{
	return open(spi_name,O_RDWR);
}
/*
 * 
 * ioctl configure for mode 0 
 * 
 * 
 * 
 */
void setupspi(int fd)
{
	int mode =0;
	ioctl(fd, SPI_IOC_WR_MODE, &mode);
}
/*
 * close spi file 
 * recives the filedescriptor
 * 
 */
void closespi(int fd)
{
	close(fd);
}
/*
 * 
 * Xfer is the hardware ioctl command to send/recive SPI commands 
 * on transfer to spi also input is read
 * 
 */
int xfer(int fd,int cmd)
{
	int res=0;
	struct spi_ioc_transfer xfer;
	memset(&xfer,0,sizeof(xfer));
	char dataBuff[2] = {((cmd>>8)&0xff),(cmd&0xFF)};
	//char dataBuff[2] = {241,36};
	//printf("cmd value %ld\r\n",cmd);
	//printf("cmdh %i, cmdl %i\r\n",dataBuff[0],dataBuff[1]);
	char rxBuff[2];
	xfer.tx_buf = (unsigned long)dataBuff;
	xfer.rx_buf = (unsigned long)rxBuff;
	xfer.len = sizeof(dataBuff);
	xfer.speed_hz=2000000;
	xfer.cs_change=0;
	res = ioctl(fd,SPI_IOC_MESSAGE(1),&xfer);
	int val = rxBuff[0]<<8 |rxBuff[1];
	//printf("%i",(int)val);
	return (int)val;
}
/*
 * gpi pin 204 is odroid u3 pin for led /chip select
 * pin must be previously added to the rc.local and modded to allow user access
 * pin 204 is 
 */
int opengpio(int pin)
{
	return open("/sys/class/gpio/gpio204/value",O_WRONLY);
}
/*
 * 
 * 
 */
void setgpio(int fd,int val)
{
	char buf[10];
	lseek(fd,0,SEEK_SET);
	if(val ==1)
		write(fd,"1",1);
	else
		write(fd,"0",1);
}
/*
 * 
 * 
 * 
 */
void closegpio(int fd)
{
	close(fd);
}

/*
 * 
 * 
 */
char* test()
{
	return "Hello from c";
}
