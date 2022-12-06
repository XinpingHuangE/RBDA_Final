from pyhive import hive
import config
import sys

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
    print(args,len(args))
    if len(args) != 4:
        print("USAGE: python query.py <CITY> <START_DATE> <END_DATE>")
        print("Example: python query.py NYC 01/01/2018 01/01/2020")
        sys.exit()
    
    CITY = args[1]
    START_DATE = args[2]
    END_DATE = args[3]

    conn = HiveConnection()
    cursor = conn.cursor()
    cursor.execute(config.USE_CMD)

    # run query
    RANGE_QUERY = "SELECT location, bucket, sum(severit)/count(severity), avg(latitude) as avg_lat, avg(longitude) as avg_lon as crime_rate FROM {} WHERE city='{}' AND crime_date>='{}' AND crime_date<='{} GROUP BY location, bucket'".format(config.TABLE,CITY,START_DATE,END_DATE)
    print("range query: ",RANGE_QUERY)

    results = runQuery(cursor,RANGE_QUERY)
    for result in results:
        print(result)

if __name__ == "__main__":
    main()