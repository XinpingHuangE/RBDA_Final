import subprocess
import config

def copyResult():
    result = subprocess.run(['gcloud','compute','scp',config.SERVER_RESULT_FILE_PATH,config.LOCAL_RESULT_FILE_PATH])
    print(result)

if __name__=="__main__":
    copyResult()