#!/usr/bin/python3

import argparse
import os
import pprint
import json

def change_json(source):
	for root, dirnames, filenames in os.walk(source):
		for filename in filenames:
			if filename.endswith(('.json')):
				file_name = filename.split("/")[-1]
				print(file_name)
				with open(filename,"r") as inputfile:
					try:
						myjson = json.loads(inputfile.read())
						newjson = myjson[0]
						inputfile.close()
						with open(filename,"w") as opfile:
							json.dump(newjson,opfile)
							opfile.close()
					except KeyError:
						print("Something went bad for file {f}".format(f=file_name))

if __name__ == "__main__":
	argParser = argparse.ArgumentParser('Change JSON files')
	argParser.add_argument('--idir', required=True, help='path to directory with input files')
	args = argParser.parse_args()
	if args.idir:
		change_json(args.idir)

