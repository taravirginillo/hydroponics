import serial
import time

SERIAL_NUMBER = 9600

# Set up the serial line
ser = serial.Serial('COM4', SERIAL_NUMBER)
time.sleep(2)

data =[]                       # empty list to store the data
for i in range(50):
    b = ser.readline()         # read a byte string
    string_n = b.decode()  # decode byte string into Unicode
string = string_n.rstrip() # remove \n and \r
flt = float(string)        # convert string to float
print(flt)
data.append(flt)           # add to the end of data list
time.sleep(0.1)            # wait (sleep) 0.1 seconds

ser.close()

for line in data:
    print(line)
