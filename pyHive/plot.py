import os
import pandas as pd
import config

class Plot:
    
    def __init__(self):
        self.data = None

    def readData(self):
        self.data = pd.read_csv(os.path.join(config.LOCAL_RESULT_FILE_PATH,'results.csv'),sep=',')

    def printData(self):
        print(self.data)

if __name__ == "__main__":
    plot = Plot()
    plot.readData() 
    plot.printData()