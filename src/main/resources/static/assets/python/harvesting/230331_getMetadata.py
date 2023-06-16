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

# This resolves the utf-8 problem https://stackoverflow.com/a/31137935
reload(sys)
sys.setdefaultencoding('utf-8')

#working dir
work_dir = sys.argv[1]

#check if directory for the responses of the API calls already exists. If it doesn't, create it
if os.path.isdir(work_dir + "apiResponses") != True:
    os.mkdir(work_dir + "apiResponses")
else:
   pass 

#initialize variable as an empty dictionary to store each harvested metadata record later
metadata = {}
#define default namespace of the MINiML schema GEO uses
ns = {'': 'http://www.ncbi.nlm.nih.gov/geo/info/MINiML'}

#open JSON file with Series IDs and store them in a variable
with open(work_dir + 'seriesIds.json') as f:
    seriesIds = json.load(f)

#iterate through all available Series IDs and harvest their related metadata
for series in seriesIds:
	#generate API query for GEO eSummary tool based on the Series ID, also including Sample metadata as an XML response
	r = requests.get("https://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=gse" + series + "&targ=all&view=brief&form=xml")
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
	#get identifier of the harvested GEO database for metadata provenance
	#return all child elements of the "Database" element
	#ns is used to provide the default namespace of the XML API response
	for dbID in root.findall(".Database/", ns):
		#check if element is named "Public-ID"
		#"Public-ID" has a minOccurs = 0, making it optional
		#"{" + ns[''] + "}" has to be added before the element name to provide the namespace
		if dbID.tag == "{" + ns[''] + "}" + "Public-ID":
			#return the value of the "Public-ID" element and add it to the metadata dictionary variable as "databaseID"
			metadata['databaseID'] = (dbID.text)
	#generate resourceLink to enable users to access the landing page of the original metadata record
	#return all child elements of the "Series" element
	for seriesID in root.findall(".Series/", ns):
		#check if element is named "Accession" 
		#"Accession" has a minOccurs = 0, maxOccurs = "unbounded" making it optional
		if seriesID.tag == "{" + ns[''] + "}" + "Accession":
			#concatenate the default URI path of GEO and the Series ID to generate the link to the landing page of a Series record and add it to the metadata dictionary variable as "resourceLink"
			metadata['resourceLink'] = "https://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=" + (seriesID.text)
	#return the "Series/Title" element
	for title in root.findall(".Series/Title", ns):
		#return the value of the "Title" element and add it to the metadata dictionary variable as "title"
		#"Title" has a no occurence information available
		metadata['title'] = (title.text)
	#get names of related creators/contributors of the Series
	#initialize list variable for storing all creator/contriubtor names
	peopleName = []
	#return all "Person" elements and their child elements recursively
	#"Person" has a no occurence information available
	for person in root.iter("{" + ns[''] + "}" + 'Person'):
		#search for "First" element as child element of the "Person" element
		for namePart in person.iter():
			if (namePart.tag) == "{" + ns[''] + "}" + "First":
				#add value of "First" element to a variable for storing and aggregating all parts of a name
				#"First" has a no occurence information available
				nameTemp = namePart.text
			#search for "Middle" element as child element of the "Person" element
			#"Middle" has a minOccurs = 0, making it optional
			if (namePart.tag) == "{" + ns[''] + "}" + "Middle":
				#concatenate value of "Middle" element to the value of the variable that stores the value of the sibling "First" element 
				nameTemp = nameTemp + ' ' + namePart.text
			#search for "Last" element as child element of the "Person" element
			#"Last" has a no occurence information available
			if (namePart.tag) == "{" + ns[''] + "}" + "Last":
				#concatenate value of "Last" element to the value of the variable that stores the values of the sibling "First" and "Middle" elements 
				nameTemp = nameTemp + ' ' + namePart.text
		#append the concatenated full name to the list variable to store all names related to the Series record
		peopleName.append(nameTemp)
	#add the list of all complete names to the metadata dictionary variable as "creators"
	metadata['creators'] = peopleName 

	#return the value of the "Series/Status/Release-Date" element
	for date in root.findall(".Series/Status/Release-Date", ns):
		#return the value of the "Series/Status/Release-Date" element and add it to the metadata dictionary variable as "releaseDate"
		metadata['releaseDate'] = date.text

	#return the value of the "Series/Summary" element
	for summary in root.findall(".Series/Summary", ns):
		#return the value of the "Series/Summary" element and add it to the metadata dictionary variable as "description" and remove unwanted characters with strip()
		metadata['description'] = summary.text.strip()

	#initialize dictionary variable to store all metadata on a single organism
	organismObject = {}
	#initialize list variable to aggregate all complete organisms
	organisms = []
	#return all "Series" elements and their child elements recursively
	for sample in root.iter("{" + ns[''] + "}" + 'Sample'):
		#iterate over all child elements of the "Sample" element
		for organism in sample.iter():
			#search for "Accession" element as child element of the "Sample" element
			if organism.tag == "{" + ns[''] + "}" + "Accession":
				#add value of "Accession" element to the organism dictionary variable as "organismId"
				organismObject['organismId'] = organism.text
			#search for "Organism" element as child element of the "Sample" element
			if organism.tag == "{" + ns[''] + "}" + "Organism":
				#add value of "Organism" element to the organism dictionary variable as "organismName"
				organismObject['organismName'] = organism.text
			#search for "Description" element as child element of the "Sample" element
			if organism.tag == "{" + ns[''] + "}" + "Description":
				#add value of "Description" element to the organism dictionary variable as "descriptionStudiedObject" and remove unwanted characters with strip()
				organismObject['descriptionStudiedObject'] = organism.text.strip()
			#search for "Molecule" element as child element of the "Sample" element
			if organism.tag == "{" + ns[''] + "}" + "Molecule":
				#add value of "Molecule" element to the organism dictionary variable as "measurementTarget"
				organismObject['measurementTarget'] = organism.text
			#search for "Treatment-Protocol" element as child element of the "Sample" element
			if organism.tag == "{" + ns[''] + "}" + "Treatment-Protocol":
				#add value of "Molecule" element to the organism dictionary variable as "samplePreparationOrProcessing" and remove unwanted characters with strip()
				organismObject['samplePreparationOrProcessing'] = organism.text.strip()
		#append copy of complete organism dictionary to the list variable storing all organisms
		#copy is needed to not overwrite preceding organism dictionaries
		organisms.append(organismObject.copy())	
	#add the list of all complete organism dictionaries to the metadata dictionary variable as "organisms"
	metadata['organisms'] = organisms
	
	#return the "Series/Overall-Design" element
	for methodDescription in root.findall(".Series/Overall-Design", ns):
		#return the value of the "Series/Overall-Design" element and add it to the metadata dictionary variable as "methodDescription"
		metadata['methodDescription'] = methodDescription.text.strip()
		
	#get methodUsed
	methodsUsed = []
	for methodUsed in root.findall(".Sample/Library-Selection", ns):
		methodsUsed.append(methodUsed)	
		metadata['methodUsed'] = methodUsed.text.strip()
		
	#write the metadata dictionary variable, containing a full harvested metadata record, into a JSON file, adding its Series ID to the name
	with open(os.path.join(work_dir + './apiResponses','metadata_record'+ series + '.json'), 'w') as json_file:
		json.dump(metadata, json_file, indent=2)
	#delay time of next query to not run into query limit
	time.sleep(45)
	
	#aggregate all harvested records into a single file
	
#check for every file in the subdirectory where the responses are stored
responseList = []

#check for every file in the subdirectory where the responses are stored
for file in os.listdir(work_dir + "./apiResponses/"):
	#open every response file and import it into python as json
	with open(os.path.join(work_dir + './apiResponses', file), "r") as read_file:
		responseData = json.load(read_file)
		#append the content of a file to the final aggregated list
		responseList.append(responseData)
		
		#write the list with all responses into a json file
		with open("all_response.json", "w", ) as json_file:
			json.dump(responseList, json_file, indent=2)
				
#write the final list into its own json file        
with open(work_dir + "all_response.json", "w", ) as json_file:
	json.dump(responseList, json_file, indent=2)

#clean up single response files
for file in os.listdir(work_dir + "./apiResponses/"):
	os.remove(os.path.join(work_dir + './apiResponses', file))
	
#cleanup of temporary files
for file in os.listdir("."):
	if file == work_dir + "gds_result.txt" or file == work_dir + "gdsIds.json" or file == work_dir + "seriesIds.json" or file == work_dir + "harvestGdsIds.json":
		os.remove(file)
	