import pyvisa
import socket
from time import sleep

HOST = "0.0.0.0"  # Standard loopback interface address (localhost)
PORT = 5025  # Port to listen on (non-privileged ports are > 1023)

class Instument():
    def __init__(self):
        self.instrument_str = ''
        self.instrument = None
        self.rm = pyvisa.ResourceManager()

    def parse_request(self, data):
        data = data.decode().strip()
        print(data)
        if(data == 'list instruments'):
            return str(self.rm.list_resources())
        elif(data.find('USB0')!=-1):
            self.instrument_str = data
#            print(data)
            try:
                self.instrument = self.rm.open_resource(data)
                #self.instrument = self.rm.open_resource('USB0::62701::60986::SDG10GAX3R0381::0::INSTR')
                return 'USB instrument connected\n'
            except:
                return 'Instrument connection error\n'
        elif(data.find('?')!=-1 and self.instrument):
            try:
                #sleep(0.4)
                recv = self.instrument.query(data)
            except:
                recv = 'VISA timeout\n'
            return recv
        elif(self.instrument):
            #sleep(0.2)
            self.instrument.write(data)
        else:
            return 'Instrument is not connected\n'

    

instrument = Instument()

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((HOST, PORT))
    s.listen()
    while True:
        conn, addr = s.accept()
        with conn:
            while True:
                try:
                    recv = conn.recv(1024)
                    
                except:
                    print('Client disconnected')
                    break
                    

                if recv.decode() == '':
                    print('Client disconnected')
                    break
                    
                print('Request: '+ recv.decode())
                if recv.decode():
                    response = instrument.parse_request(recv)
                    if(response != None and response != ''):
                        print('Response: ' + response)
                        conn.sendall(response.encode())


