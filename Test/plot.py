import matplotlib.pyplot as plt
import csv

x = []
y = []

with open('NF_RC_10K_100n.csv','r') as csvfile:
    lines = csv.reader(csvfile, delimiter=',')
    for row in lines:
        x.append(float(row[0]))
        y.append(float(row[1]))



plt.plot(x, y, color = 'g',
        marker = 'o',)


plt.xscale('log')

#plt.xticks(rotation = 25)
plt.xlabel('Frekvencija (Hz)')
plt.ylabel('Amplituda (dB)')
plt.title('Pasivni LP filter', fontsize = 20)
#plt.grid()
plt.show()