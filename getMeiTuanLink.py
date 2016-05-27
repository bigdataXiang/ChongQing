# -*- coding:utf-8 -*-
import requests
from bs4 import BeautifulSoup
import json


def getpage(url,filename):
    res=requests.get(url)
    #print res.text
    soup=BeautifulSoup(res.text,from_encoding="utf-8")
    results=soup.find_all('div',class_="poi-tile__info")
    for result in results:
        try:
            abstract=result.find('a',class_="link f3").get_text()
            href=result.find('a',class_="link f3").get('href')

        except AttributeError:
            print "no info"

        item=dict({"abstract":abstract.encode('utf-8'),"href":href})
        with open("".join(filename.split())+'.json', 'a') as outfile:
	        json.dump(item, outfile,ensure_ascii=False)
        with open("".join(filename.split())+'.json', 'a') as outfile:
            outfile.write(",\n")

if __name__ == '__main__':
    base_url="http://cq.meituan.com/category/meishi/all/page2?mtt=1.index%2Fdefault%2Fpoi.0.0.iopn2kw0"
    filename=u"美团餐饮"
    getpage(base_url,filename)
