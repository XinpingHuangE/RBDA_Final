import os

# Credentials
USERNAME = "ak8288_nyu_edu"
PORT = 10000
HIVE_HOST = "localhost"
RESULT_FILE_PATH = "results.csv"

# Database
TABLE = "crimes"
USE_CMD = "use "+USERNAME
SET_DATE_FORMAT_CMD = "SET DATEFORMAT mdy"

# Copy results.csv from dataproc to local machine /data folder
LOCAL_RESULT_FILE_PATH = os.path.join(*[os.getcwd(),"data"])
SERVER_RESULT_FILE_PATH = 'nyu-dataproc-m:~/final_project/pyHive/results.csv'
