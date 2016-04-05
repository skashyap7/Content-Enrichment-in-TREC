#!/usr/bin/python2
import urllib2,sys
import json

cat_list = ["distance","volume","weight","energy","time","temperature","unit"]
sub_cat_list = ["metre","litre","gram","second","feet","inch","yard","mile","other",
"gallon","pint","quart","cup","spoon","cubic","ounce","pound","ton","calorie","joule"]

list_val = {}

def get_solr_reponse(keyword):
	response = urllib2.urlopen("http://localhost:8983/solr/measure/select?q=measurements%3A%22"+keyword+"%22&fl=numFound&wt=json")
	data = json.load(response)  
	value= data["response"]["numFound"]+5 if data["response"]["numFound"]!=0 else 5
	list_val[keyword.lower()]= value

def main(argv):
	for keyword in cat_list:	
		get_solr_reponse(keyword)
	for keyword in sub_cat_list:	
		get_solr_reponse(keyword)
	with open('measurements.json','r+') as inputfile:
		measure_json = json.load(inputfile)
		for child in measure_json["children"]:
			try:
				for baby in child["children"]:
					if(list_val.has_key(baby["name"].lower())):
						baby["size"]=list_val[baby["name"].lower()]
			except KeyError:
				if(list_val.has_key(child["name"].lower())):
						child["size"]=list_val[child["name"].lower()] #get temperature and unit nodes
		inputfile.seek(0)
		inputfile.truncate()
		json.dump(measure_json,inputfile, indent=4, sort_keys=True)
		inputfile.close()

# Calling main
if __name__ == "__main__":
	main(sys.argv[1:])



