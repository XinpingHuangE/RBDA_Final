import os
import re


class Parser:
    def __init__(self):
        self.__unique_crimes = set()
        self.__lines = []               # each line in file
        self.__lineOffenceCount = []    # the count of each line
        self.__ignoreChars = ['"',',','$','-',':','(',')','\'','&','.','/','\n']
        self.__replaceChars = ['=','&','/']
        self.__map = {}
        self.__crimeMap = {
            "SEXUAL OFFENCES"           :   ['indecentexposure','prostitution','penetration','rape','copulation','sexual','sex','incest','porn'],
            "THEFT"                     :   ['theft','pickpocket','bunco','snatching','stolen','larceny','burglar'],
            "ASSAULT"                   :   ['battery','assault','jostling','jostle','felony'],
            "MAJOR THEFT"               :   ['grandtheft','trespas','embezzlement','prowler','robbery','vandalism'],
            "FRAUD"                     :   ['forgery','fraud','counterfeit','bribe','deceptive','deception'],
            "CRIME AGAINST CHILDREN"    :   ['childstealing','childabandon','children','childabuse'],
            "DRUGS"                     :   ['drugs','gambling','narcotic'],
            "LYNCHING"                  :   ['lynch','riot','arson'],
            "SHOOTING"                  :   ['weaponschool','firearms','deadlyweapon','weapon','shots'],
            "HOMICIDE"                  :   ['homicide','manslaughter','murder'],
            "TERRORISM"                 :   ['bomb'],
            "HUMAN TRAFFICKING"         :   ['humantrafficking'],
            "TRAFFIC"                   :   ['intoxicated','impaired','reckless','drunk'],
            "KIDNAPPING"                :   ['kidnap','extortion'],
            "OTHER"                     :   [] 
        
        }
        self.__crimeCategoryCount = {x:0 for x in self.__crimeMap.keys()}
        

    def parse(self,file_path):
        with open(file_path) as file:
            self.__lines = file.readlines()
        self.processLines()

    def processLines(self):
        for line in self.__lines:
            for c in self.__replaceChars:
                line = line.replace(c," ")

            offenceCount = int(line.split("\t")[-1].split(" ")[-1])
            self.__lineOffenceCount.append(offenceCount)
            # print("line: ",line,", Count: ",line.split("\t")[-1])
            _line = re.sub("([\(\[]).*?([\)\]])", "\g<1>\g<2>", line)
            preProcessedLine = ''.join([i for i in _line if i not in self.__ignoreChars and not i.isdigit()])
            preProcessedLine = preProcessedLine.strip()
            self.__unique_crimes.add(preProcessedLine)

    def getUniqueCrimes(self):
        return self.__unique_crimes
    
    def mapDescToCategory(self):
        for index,line in enumerate(self.__lines):
            _line = re.sub("([\(\[]).*?([\)\]])", "\g<1>\g<2>", line)
            preProcessedLine = ''.join([i for i in _line if i not in self.__ignoreChars and not i.isdigit()])
            candidateStr = ''.join([c for c in preProcessedLine if c != ' '])
            category = self.findCategory(candidateStr)
            self.__crimeCategoryCount[category] += self.__lineOffenceCount[index]
            self.__map[preProcessedLine] = category

    def findCategory(self,string):
        for key in self.__crimeMap:
            for keyword in self.__crimeMap[key]:
                if keyword.lower() in string.lower():
                    return key
        return "OTHER"
    
    def printCategoryMap(self):
        for key in self.__map:
            print(key,"\t:\t",self.__map[key])

    def printOffenceCountMap(self):
        for key in self.__crimeCategoryCount:
            print(key,"\t:\t",self.__crimeCategoryCount[key])

def findUniqueKeyWords(crimeSet):
    uniqueWords = set([x for crime in crimeSet for x in crime.split()])
    return uniqueWords
    
def printUniqueWords(words):
    for word in words:
        print(word)

def main():
    file_path = "uniqueCrimes.txt"
    parser = Parser()
    parser.parse(file_path)
    uniqueCrimes = parser.getUniqueCrimes()
    parser.mapDescToCategory()
    parser.printCategoryMap()
    parser.printOffenceCountMap()


if __name__ == "__main__":
    main()
    # ASSLT

