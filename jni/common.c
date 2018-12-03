#include "common.h"

/*****************************************************************
    U A R T   C O N F I G E R
*****************************************************************/
static speed_t getBaudrate(int baudrate)
{
	switch(baudrate) {
	case 0: return B0;
	case 50: return B50;
	case 75: return B75;
	case 110: return B110;
	case 134: return B134;
	case 150: return B150;
	case 200: return B200;
	case 300: return B300;
	case 600: return B600;
	case 1200: return B1200;
	case 1800: return B1800;
	case 2400: return B2400;
	case 4800: return B4800;
	case 9600: return B9600;
	case 19200: return B19200;
	case 38400: return B38400;
	case 57600: return B57600;
	case 115200: return B115200;
	case 230400: return B230400;
	case 460800: return B460800;
	case 500000: return B500000;
	case 576000: return B576000;
	case 921600: return B921600;
	case 1000000: return B1000000;
	case 1152000: return B1152000;
	case 1500000: return B1500000;
	case 2000000: return B2000000;
	case 2500000: return B2500000;
	case 3000000: return B3000000;
	case 3500000: return B3500000;
	case 4000000: return B4000000;
	default: return B9600;
	}
}

int uartSetSpeed(int fd, int baudrate)
{
	int status;
	struct termios Opt;

	tcgetattr(fd, &Opt);

	//设置 串口的NL-CR 和CR-NL 的映射
	Opt.c_iflag &= ~ (INLCR | ICRNL | IGNCR);
	Opt.c_oflag &= ~(ONLCR | OCRNL);
	Opt.c_iflag &= ~ (IXON | IXOFF | IXANY);
	
	Opt.c_cflag |=  ( CREAD | HUPCL | CLOCAL);
	Opt.c_iflag = IGNBRK | IGNPAR;
	Opt.c_oflag = 0;
	Opt.c_lflag = 0;

	speed_t speed = getBaudrate(baudrate);

	tcflush(fd, TCIOFLUSH);

	cfsetispeed(&Opt, speed);
	cfsetospeed(&Opt, speed);

	status = tcsetattr(fd, TCSANOW, &Opt);

	return (status == 0);
}

int setUartConfig(int fd, int databits, int stopbits, int parity)
{
	struct termios options; 

	if (tcgetattr(fd,&options) !=  0) {
		LOGE("ERROR: tcgetattr \n");
		return(FALSE);
	}

	options.c_cflag &= ~CSIZE;
	options.c_lflag &= ~(ICANON | ECHO |ECHOE | ISIG);

	switch (databits) {
	case 7:
		options.c_cflag |= CS7;
		break;
	case 8:
		options.c_cflag |= CS8;
		break;
	default:
		LOGE("Unsupported data size\n");
		return (FALSE);
	}

	/*设置奇偶校验位*/
	switch (parity) {
	case 'n':
	case 'N':
		options.c_cflag &= ~PARENB;		/* Clear parity enable */
		options.c_iflag &= ~INPCK;		/* Enable parity checking */
		break;
	case 'o':
	case 'O':
		options.c_cflag |= (PARODD | PARENB);	/* 设置为奇效验*/
		options.c_iflag |= INPCK;		/* Disnable parity checking */
		break;
	case 'e':
	case 'E':
		options.c_cflag |= PARENB;		/* Enable parity */
		options.c_cflag &= ~PARODD;		/* 转换为偶效验*/
		options.c_iflag |= INPCK;		/* Disnable parity checking */
		break;
	case 'S':
	case 's':  /*as no parity*/
		options.c_cflag &= ~PARENB;
		options.c_cflag &= ~CSTOPB;
		break;
	default:
		LOGE("Unsupported parity\n");
		return (FALSE);
	}

	/* 设置停止位*/
	switch (stopbits) {
	case 1:
		options.c_cflag &= ~CSTOPB;
		break;  
	case 2:
		options.c_cflag |= CSTOPB;
		break;
	default:
		LOGE("Unsupported stop bits\n");
		return (FALSE);
	}

	/* Set input parity option */
	if ((parity != 'n') && (parity != 'N')) {
		//options.c_iflag |= INPCK;
	}

	tcflush(fd,TCIFLUSH);

	options.c_cflag |= CREAD;
	options.c_cc[VTIME] = 1;		/* 设置超时0 seconds*/
	options.c_cc[VMIN]  = 0;		/* Update the options and do it NOW */

	if (tcsetattr(fd, TCSANOW, &options) != 0) {
		LOGE("set tcsanow error\n");
		return (FALSE);
	}

	return (TRUE);
}

// command frame:  [head] [len] [cmd] [data] [fcc]
static int parse_cmd(unsigned char* cmd, int len)
{
	unsigned int sum = 0;
	unsigned short fcc = 0, i = 0;

	if (cmd[0] != 0x02) {
		LOGE("cmd head[0x%02x] error!", cmd[0]);
		return 0;
	}
	
	for (i=0; i<(len-1); i++) {
		sum += cmd[i];
	}

	fcc = sum&0xff;

	LOGE("Data Sum:(0x%04x, 0x%02x, x%02x)", sum, fcc, cmd[len-1]);
	if (fcc != cmd[len-1]) {
		LOGE(" check sum error:(0x%02x, x%02x)", fcc, cmd[len-1]);
		return 0;
	}
	
	return 1;
}

int UartWork(int fd, unsigned char* buf)
{
	int tmplen = 0;
	int ret;
	int retval;
	fd_set rfds;
	struct timeval tv;
	unsigned char tmp_buf[8];
	int len=0;

	tv.tv_sec = 0;//1;		//set the rcv wait time
	tv.tv_usec = 300;		//1000us = 1ms

	FD_ZERO(&rfds);
	FD_SET(fd, &rfds);

	retval = select(fd + 1, &rfds, NULL, NULL, &tv);
	if (retval) {
		tmplen = 0;
		while(1) {
			ret = read(fd, tmp_buf, 1);
			if (ret > 0) {
				memcpy(buf+tmplen, tmp_buf, ret);
				tmplen += ret;
				if ((tmplen >= 2) && (tmplen >= (buf[1]+1))) {
					LOGD("size = 0x%02x", buf[1]);
					break;
				}
			} else {
				if (tmplen == 0) {
					LOGD(" null break! %d, %d", __LINE__, fd);
					break;
				}
			}
		}
	}

	if (tmplen == (buf[1]+1)) {
#if 1 //def DEBUG
		int i = 0;
		for (i=0; i<tmplen; i++) {
			LOGD("data[%d] =0x%02x, ", i, buf[i]);
		}
#endif
		if (parse_cmd(buf, tmplen)) {
			LOGD("			Data is OK!");
			return tmplen;
		} else {
			LOGD("			ERROR: data is not a cmd");
		}
	}
	return 0;
}


int UartWorkCard(int fd, unsigned char* buf)
{
	int i = 0;
	int tmplen = 0;
	int ret = -1;
	int retval;
	fd_set rfds;
	struct timeval tv;
	unsigned char tmp_buf[1024];
	int len=0;

	tv.tv_sec = 0;//1;		//set the rcv wait time
	tv.tv_usec = 300;		//1000us = 1ms

	FD_ZERO(&rfds);
	FD_SET(fd, &rfds);

	retval = select(fd + 1, &rfds, NULL, NULL, &tv);
	if(retval > 0) {
		bzero(tmp_buf, 1024);
		if((ret = read(fd, tmp_buf, 1024)) < 0) {
			LOGE("读取数据出错 %d", ret);
			return -1;
		} else if(ret > 0){
			memcpy(buf, tmp_buf, ret);
			for(i = 0; i < ret; i++){
				LOGI("size = 0x%02x", buf[i]);
			}
			return ret;
		}
	}
	return 0;
}
