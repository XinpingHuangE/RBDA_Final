from pyhive import hive
import config
import sys
import re
import csv
import os

class HiveConnection(object):
    _conn = None
    
    def __new__(cls,*args,**kwargs):
        if not cls._conn:
            cls._conn = hive.Connection(host=config.HIVE_HOST, port=config.PORT, username=config.USERNAME)
            return cls._conn

def writeResult(results,headers=None):
    f = open(config.RESULT_FILE_PATH,'w')
    csv_writer = csv.writer(f)
    csv_writer.writerow(headers)
    try:
        for result in results:
            result = list(result)
            csv_writer.writerow(result)
    except Exception as e:
        print("EXCEPTION RAISED IN WRITING RESULT TO FILE: ",e)
        sys.exit()
    finally:
        f.close()
    print("\n\n[*** RESULTS stored in {}/results.csv ***]".format(os.getcwd()))

def runQuery(cursor,query):
    cursor.execute(query)
    return cursor.fetchall()

def main():
    args = sys.argv

    # INPUT FROM USER
    CITY = input("ENTER CITY (NYC/LA/CHI): ")
    START_DATE = input("ENTER VALID START DATE (ex: 01/01/2012): ")
    END_DATE = input("ENTER VALID END DATE (ex: 01/01/2018): ")

    CITY = CITY.upper()

    # handle chicago case separately
    if CITY=='CHI':
        CITY = 'CHICAGO'

    start_date_match = re.match("^[0-9]{2}/[0-9]{2}/[0-9]{4}$", START_DATE)
    end_date_match = re.match("^[0-9]{2}/[0-9]{2}/[0-9]{4}$", END_DATE)

    if not bool(start_date_match) or not bool(end_date_match):
        print("Invalid date format")
        print("Example: python query.py NYC 01/01/2018 01/01/2020")
        sys.exit()

    conn = HiveConnection()
    cursor = conn.cursor()
    cursor.execute(config.USE_CMD)
    cursor.execute(config.SET_DATE_FORMAT_CMD)
    # run query
    # RANGE_QUERY = "SELECT location, bucket, sum(severity)/count(severity) as crime_rate, avg(latitude) as avg_lat, avg(longitude) as avg_lon FROM {} WHERE city='{}' AND crime_date>='{}' AND crime_date<='{}' GROUP BY location, bucket".format(config.TABLE,CITY,START_DATE,END_DATE)
    # select location,crime_date from crimes where from_unixtime(unix_timestamp(crime_date , 'MM/dd/yyyy')) >= from_unixtime(unix_timestamp('01/01/2012' ,'MM/dd/yyyy')) and from_unixtime(unix_timestamp(crime_date , 'MM/dd/yyyy')) <= from_unixtime(unix_timestamp('01/01/2014', 'MM/dd/yyyy'));
    RANGE_QUERY_PER_CRIME = "SELECT city, location, category, count(severity) FROM {} WHERE from_unixtime(unix_timestamp(crime_date , 'MM/dd/yyyy'))>=from_unixtime(unix_timestamp('{}' ,'MM/dd/yyyy')) AND from_unixtime(unix_timestamp(crime_date , 'MM/dd/yyyy'))<=from_unixtime(unix_timestamp('{}', 'MM/dd/yyyy')) GROUP BY city, location, category".format(config.TABLE,START_DATE,END_DATE)
    RANGE_QUERY = "SELECT city, location, sum(severity)/count(severity) as crime_index FROM {} WHERE city = '{}' AND from_unixtime(unix_timestamp(crime_date , 'MM/dd/yyyy'))>=from_unixtime(unix_timestamp('{}' ,'MM/dd/yyyy')) AND from_unixtime(unix_timestamp(crime_date , 'MM/dd/yyyy'))<=from_unixtime(unix_timestamp('{}', 'MM/dd/yyyy')) GROUP BY city, location".format(config.TABLE,CITY,START_DATE,END_DATE)
    CITY_WISE_PER_HOUR_CRIME_RATE_QUERY = "SELECT city, bucket, location, sum(CAST(severity AS BIGINT))/count(severity) as crime_rate FROM crimes WHERE city = 'NYC' AND from_unixtime(unix_timestamp(crime_date , 'MM/dd/yyyy'))>=from_unixtime(unix_timestamp('01/01/2019' ,'MM/dd/yyyy')) AND from_unixtime(unix_timestamp(crime_date , 'MM/dd/yyyy'))<=from_unixtime(unix_timestamp('12/31/2021', 'MM/dd/yyyy')) GROUP BY city, bucket, location"
    # RANGE_QUERY_PER_CRIME = "SELECT city, location, sum(severity)/count(severity) FROM {} WHERE from_unixtime(unix_timestamp(crime_date , 'MM/dd/yyyy'))>=from_unixtime(unix_timestamp('{}' ,'MM/dd/yyyy')) AND from_unixtime(unix_timestamp(crime_date , 'MM/dd/yyyy'))<=from_unixtime(unix_timestamp('{}', 'MM/dd/yyyy')) GROUP BY city, location, category".format(config.TABLE,START_DATE,END_DATE)
    print("range query: ",CITY_WISE_PER_HOUR_CRIME_RATE_QUERY)

    results = runQuery(cursor,CITY_WISE_PER_HOUR_CRIME_RATE_QUERY)
    writeResult(results,['city','location','crime_index'])

if __name__ == "__main__":
    main()
