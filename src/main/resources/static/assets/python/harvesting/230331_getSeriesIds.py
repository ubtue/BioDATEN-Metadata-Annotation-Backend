#sys for arguments
import sys
#package for working with apis
import requests
#package for handling json data
import json
#package for adding a delay between queries
import time
#package to create folders and write files into sub directories
import os, os.path
#package to store time of the query to make the formatting script only add new datasets since last query
import datetime
#package to work with xml
import xml.etree.ElementTree as ET

#working dir
work_dir = sys.argv[1]

#initialize list variable to store series IDs
seriesIds = []

#open JSON file with Geo Dataset IDs and store them in a variable
with open(work_dir + 'harvestGdsIds.json') as f:
    gdsIds = json.load(f)

#iterate through all Geo Dataset IDs and use them to retrieve related Series IDs for metadata harvesting
for gd in gdsIds:
	print(gd)
	#generate API query for GEO eSummary tool based on the Geo Dataset ID
	r = requests.get("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=gds&id=" + gd)
	#decode the API response as UTF-8 to remove unwanted characters
	rContent = r.content.decode('utf-8')
	#transform the decoded API response to a string
	rContent = str(rContent)
	#join the response into a single string
	content = "".join(rContent)
	#remove unwanted characters from response
	content = content.replace("\n", " ")
	#parse the response as an XML tree
	root=ET.fromstring(content)
	#traverse the XML tree and search for all Series IDs
	for series in root.findall("./DocSum/Item/[@Name='GSE']"):
		#add each Series ID to the list variable
		seriesIds.append(series.text)
	#delay time of next query to not run into query limit
	time.sleep(45)

#write the list of all collected Series IDs into a JSON file
with open(os.path.join(work_dir + 'seriesIds.json'), 'w') as json_file:	
	json.dump(seriesIds, json_file, indent=2)