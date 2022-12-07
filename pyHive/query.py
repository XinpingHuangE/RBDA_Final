from pyhive import hive
import config
import sys
import re

class HiveConnection(object):
    _conn = None
    
    def __new__(cls,*args,**kwargs):
        if not cls._conn:
            cls._conn = hive.Connection(host=config.HIVE_HOST, port=config.PORT, username=config.USERNAME)
            return cls._conn

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
    RANGE_QUERY = "SELECT location, sum(severity)/count(severity) as crime_rate, avg(latitude) as avg_lat, avg(longitude) as avg_lon FROM {} WHERE city='{}' AND from_unixtime(unix_timestamp(crime_date , 'MM/dd/yyyy'))>=from_unixtime(unix_timestamp('{}' ,'MM/dd/yyyy')) AND from_unixtime(unix_timestamp(crime_date , 'MM/dd/yyyy'))<=from_unixtime(unix_timestamp('{}', 'MM/dd/yyyy')) GROUP BY location".format(config.TABLE,CITY,START_DATE,END_DATE)
    print("range query: ",RANGE_QUERY)

    results = runQuery(cursor,RANGE_QUERY)
    for result in results:
        print(result)

if __name__ == "__main__":
    main()
