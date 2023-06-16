#sys for arguments
import sys
#package for handling json data
import json
#package for adding a delay between queries
import time
#package to create folders and write files into sub directories
import os, os.path
#package to store time of the query to make the formatting script only add new datasets since last query
import datetime

#initialize list variable to store the Geo Dataset IDs of a new harvesting run
currentGdsIds = []
#store time of harvesting process start
timestamp = str(datetime.datetime.now())
#full path to file
full_path = os.path.realpath(__file__)
#working dir
work_dir = sys.argv[1]

#get Geo Dataset IDs via https://www.ncbi.nlm.nih.gov/gds?term=gds%5BFilter%5D, selecting "Send to" "File", Format "Unqieu Identifier List", Sort by "Default order"
#open the Geo Dataset IDs at the current point in time, downloaded as a text file and store them as a list variable
with open(work_dir + 'gds_result.txt') as f:
    lines = f.readlines()
#clean up each ID to remove unwanted characters such as "\n"
#iterate through each Geo Dataset ID
for i in lines:
	#remove unwanted characters from each Geo Dataset ID
	i = i.strip()
	#store the clean GeoDataset IDs in the currentGdsIds list variable
	currentGdsIds.append(i)

#check if harvesting history file already exists. If not, create it
if (os.path.exists(work_dir + 'harvestingHistory.json') != True):
	#initialize harvesting history object for first harvesting run
	#the harvesting history contains the timestamp for the start of the harvesting process and a list of all IDs that have already been harvested
	harvestingHistoryOrigin = {"queryTimestamp": timestamp, "IDs":currentGdsIds}
	#create a JSON file to store harvesting history information
	with open(os.path.join(work_dir + 'harvestingHistory.json'), 'w') as json_file:
		#write the harvesting history object into the JSON file
		json.dump(harvestingHistoryOrigin, json_file, indent=2)
		#save the list of Geo Dataset IDs as a JSON file to make it available for following API calls 
		#since this is the first harvest run, all current Geo Dataset IDs are stored
		with open(os.path.join(work_dir + 'harvestGdsIds.json'), 'w') as json_file:
			json.dump(currentGdsIds, json_file, indent=2)
#if a previous harvesting run exists
else:
	#initialize list variable to store IDs that are new since the last harvesting run and therefore haven't been harvested before
	newHarvestIds = []
	#initialize list variable to store already harvested IDs
	oldHarvestIds = []
	#open file with information on already executed harvest to get all IDs that have already been harvested
	with open(work_dir + 'harvestingHistory.json', 'r+') as json_file:
		json_data = json.load(json_file)
		#iterate through all elements of the harvesting history file
		for key, value in json_data.items():
			#check if the element is the ID element
			if key == 'IDs':
				#iterate through all already harvested Geo Dataset IDs and add them to the oldHarvestIds list variable, which is used for identifying unharvested Geo Dataset IDs and updating the harvesting history file later
				oldHarvestIds.append(value)
		#remove the outer list of the oldHarvestIds variable to make further working with the list easier
		oldHarvestIds = oldHarvestIds[0]
		#iterate through all current Geo Dataset IDs downloaded from the GEO website
		for i in currentGdsIds:
			#check, if a current ID is part of the oldHarvestIds variable, meaning that it has already been harvested in an earlier harvesting run
			if i in oldHarvestIds:
				pass
			#if it is not part of the oldHarvestIds add it to the newHarvestIds list variable, which is used to provide the getMetadata script with the Geo Dataset IDs to harvest the metadata
			else:
				newHarvestIds.append(i)
				#add the new Geo Dataset IDs to the list of the already harvested Geo Dataset IDs to add them to the harvesting history file later
				oldHarvestIds.append(i)
	#save the list of new Geo Dataset IDs as a JSON file to make it available for following API calls 
	with open(os.path.join(work_dir + 'harvestGdsIds.json'), 'w') as json_file:
		json.dump(newHarvestIds, json_file, indent=2)
	
	#initialize new harvesting history object with the timestamp of the current harvesting run and the updated list of all now harvested Geo Datset IDs
	newHarvestingHistory = {"queryTimestamp": timestamp, "IDs":oldHarvestIds}
	#store the new harvesting history in the harvesting history JSON file
	with open(os.path.join(work_dir + 'harvestingHistory.json'), 'w') as json_file:
		json.dump(newHarvestingHistory, json_file, indent=2)