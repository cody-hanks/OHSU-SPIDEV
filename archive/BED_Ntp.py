import ntplib



class serverntp:
	def __init__(self):
		self.client=ntplib.NTPClient()
	
	def response(self):
		self.response = self.client.request('us.pool.ntp.org',version =3)
		return self.response.tx_time
	
	
