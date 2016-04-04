#!/usr/bin/python2
import sys, getopt
from urllib import FancyURLopener
from bs4 import BeautifulSoup,SoupStrainer

class MyOpener(FancyURLopener):
    version = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36'

def get_rel_articles(keywordPhrase):
	keyword = "+".join(keywordPhrase.split(" "))
	openurl = MyOpener().open("https://scholar.google.com/scholar?hl=en&q="+keyword)
	page = BeautifulSoup(openurl.read(), parse_only=SoupStrainer('div', id='gs_ccl'))
	for a in page.select('div.gs_r div.gs_ri h3.gs_rt a'): print "["+a.getText().encode("UTF-8")+","+a['href']+"]"

def main(argv):
	keyword_phrase = ''
	try:
		opts, args = getopt.getopt(argv,"hk:",["keywordphrase="])
	except getopt.GetoptError:
		print("scholar_bs4.py -k <keywordphrase>")
		sys.exit(2)
	for opt, arg in opts:
		if opt=='-h':
			print("scholar_bs4.py -k <keywordphrase>")
			sys.exit()
		elif opt in ("-k","--keywordphrase"):
			keyword_phrase = arg
	print("DEBUG: Keyword phrase is {keyword}".format(keyword=keyword_phrase))

	get_rel_articles(keyword_phrase)

# Calling main
if __name__ == "__main__":
	main(sys.argv[1:])



