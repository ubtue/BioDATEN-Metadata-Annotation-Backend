#sys for arguments
import sys
#package for handling json data
import json
#package to work with xml
import xml.etree.ElementTree as ET

# This resolves the utf-8 problem https://stackoverflow.com/a/31137935
reload(sys)
sys.setdefaultencoding('utf-8')

#initialize json object to store aggregation of all metadata elements
FDAT = {}
#initialize namespaces of the different sections of the annotation tool xml
ns = {}
ns['dc'] = 'http://datacite.org/schema/kernel-4'
ns['cmdp'] = 'http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1659015263825'

#initialize the access element
FDAT['access'] = {}

FDAT['access']["record"] = "public"
FDAT['access']["files"] = "public"

#initialize the file element
FDAT['files'] = {}
FDAT['files']["enabled"] = bool(1)

#initialize metadata root element
FDAT['metadata'] = {}

#read the annotation tool xml output and store it in a var
tree = ET.parse(sys.argv[1]) 

#store the root node of the xml in a var
root = tree.getroot() 

#create empty default json structure to ensure desired sequence
FDAT['metadata']['resource_type'] = {}
FDAT['metadata']['creators'] = []
FDAT['metadata']['title'] = ""
FDAT['metadata']['publication_date'] = ""
FDAT['metadata']['additional_titles'] = []
FDAT['metadata']['description'] = ""
FDAT['metadata']['additional_descriptions'] = []
FDAT['metadata']['rights'] = []
FDAT['metadata']['contributors'] = []
FDAT['metadata']['subjects'] = []
FDAT['metadata']['languages'] = []
FDAT['metadata']['dates'] = []
FDAT['metadata']['description'] = ""
#FDAT['metadata']['version'] = ""
FDAT['metadata']['publisher'] = ""
FDAT['metadata']['identifiers'] = []
FDAT['metadata']['related_identifiers'] = []
#FDAT['metadata']['sizes'] = []
#FDAT['metadata']['format'] = []
#FDAT['metadata']['locations'] = {}
#FDAT['metadata']['locations']['features'] = [] 
FDAT['metadata']['funding'] = []
#FDAT['metadata']['references'] = []

#iterate over the xml root and all its children
for child in root.iter():
	#resourceType (1)
	#search for a 'resourceType' element
	#addition of namespace to the element tag comparison via the namespace variable 'ns'
	if child.tag == "{" + ns['dc'] + "}" + "resourceType":
		#store the value of the 'resourceTypeGeneral' attribute in the final json object
		FDAT['metadata']['resource_type']['id'] = child.get('resourceTypeGeneral').lower()
	
	#creators (1-n)
	#search for 'creator' elements
	if child.tag == "{" + ns['dc'] + "}" + "creator":
		#intialize an object to aggregate all information on a single creator
		creator = {}
		#intialize objects to temporarily store information to create nested structure
		person_or_org = {}
		person_or_org_identifiers = []
		person_or_org_identifier = {}
		affiliations = []
		affiliation = {}
		#iterate over all child elements of each 'creator' element
		for schild in child.iter():
			#search for 'creatorName' element
			if schild.tag == "{" + ns['dc'] + "}" + "creatorName":
				#store the value of the 'creatorName' element in the json object in the according layer
				person_or_org['name'] = schild.text
				#store the value of the 'nameType' attribute in the json object in the according layer
				person_or_org['type'] = schild.get('nameType').lower()
			#search for 'givenName' element
			if schild.tag == "{" + ns['dc'] + "}" + "givenName":
				#store the value of the 'given_name' element in the json object in the according layer
				person_or_org['given_name'] = schild.text
			#search for 'familyName' element
			if schild.tag == "{" + ns['dc'] + "}" + "familyName":
				#store the value of the 'family_name' element in the json object in the according layer
				person_or_org['family_name'] = schild.text
			#search for 'nameIdentifier' element
			if schild.tag == "{" + ns['dc'] + "}" + "nameIdentifier":
				#store the value of the 'nameIdentifierScheme' attribute in the json object in the according layer
				person_or_org_identifier['scheme'] = schild.get('nameIdentifierScheme')
				#store the value of the 'nameIdentifier' element in the json object in the according layer
				person_or_org_identifier['identifier'] = schild.text
				#append a copy of the layer object to the super-layer object
				person_or_org_identifiers.append(person_or_org_identifier.copy())
				#add the layer object to its super-layer
				person_or_org['identifiers'] = person_or_org_identifiers
			#search for 'affiliation' element	
			if schild.tag == "{" + ns['dc'] + "}" + "affiliation":
				#store the value of the 'affiliation' element in the json object in the according layer
				affiliation['id'] = schild.text
				#store the value of the 'affiliationIdentifier' attribute in the json object in the according layer
				affiliation['name'] = schild.get('affiliationIdentifier')
				#append a copy of the layer object to the super-layer object
				affiliations.append(affiliation.copy())
		#add the layer object to its super-layer		
		creator['person_or_org'] = person_or_org
		#add the layer object to its super-layer		
		creator['affiliations'] = affiliations
		#append the creator object to the final json object
		FDAT['metadata']['creators'].append(creator)		
	
	#title (1)
	#search for 'title' element	that has not 'titleType' attribute
	if child.tag == "{" + ns['dc'] + "}" + "title" and child.get('titleType') == None:
		#store the value of the 'title' element in the final json object
		FDAT['metadata']['title'] = child.text
		
	#publicationDate (1)
	#automatically generated when published in invenio?
	
	#additionalTitles (0-n)
    #search for 'title' element	that has any 'titleType' attribute
	if child.tag == "{" + ns['dc'] + "}" + "title" and child.get('titleType') != None:
        #intialize an object to aggregate all information on a single additional title
		additional_title = {}
		#intialize objects to temporarily store information to create nested structure
		additional_title_type = {}
		additional_title_type_title = {}
		#store the value of the 'title' element in the json object in the according layer
		additional_title['title'] = child.text
		#store the value of the 'titleType' attribute in the json object in the according layer
		additional_title_type['id'] = child.get('titleType')
        #store the value of the 'title' element in the json object in the according layer
		additional_title_type_title['en'] = child.text
        #add the layer object to its super-layer
		additional_title_type['title'] = additional_title_type_title
        #add the layer object to its super-layer
		additional_title['type'] = additional_title_type
        #append the additional title object to the final json object
		FDAT['metadata']['additional_titles'].append(additional_title)
	
	#description (0-1)
	#search for 'description' element that has a 'descriptionType' attribute with the value of 'Abstract'
	if child.tag == "{" + ns['dc'] + "}" + "description" and child.get('descriptionType') == 'Abstract':
		#store the value of the 'description' element in the final json object
		FDAT['metadata']['description'] = child.text
	
	#additional descriptions (0-n)
	#search for 'description' element that has a 'descriptionType' attribute with any value that is not 'Abstract'
	if child.tag == "{" + ns['dc'] + "}" + "description" and child.get('descriptionType') != 'Abstract' or "{" + ns['cmdp'] + "}" + "Method":
		#intialize an object to aggregate all information on a single additional description
		additional_description = {}
		#intialize objects to temporarily store information to create nested structure
		#define value to ensure correct order inside the layer object
		additional_description['description'] = ""
		additional_description_type = {}
		additional_description_type_title = {}
		#search for 'description' element that has a 'descriptionType' attribute with any value that is not 'Abstract'
		if child.tag == "{" + ns['dc'] + "}" + "description" and child.get('descriptionType') != 'Abstract':
			#store the value of the 'description' element in the json object in the according layer
			additional_description['description'] = child.text
			#store the value of the 'descriptionType' attribute in the json object in the according layer
			additional_description_type['id'] = child.get('descriptionType').lower()
			#store the value of the 'descriptionType' attribute in the json object in the according layer
			additional_description_type_title['en'] = child.get('descriptionType')
			#add the layer object to its super-layer
			additional_description_type['title'] = additional_description_type_title
			#add the layer object to its super-layer
			additional_description['type'] = additional_description_type
			#append the additional title object to the final json object
			FDAT['metadata']['additional_descriptions'].append(additional_description)
		#search for 'Method' element
		if child.tag == "{" + ns['cmdp'] + "}" + "Method":
			#iterate over all child elements of each 'Method' element and count their position in the loop
			for count,schild in enumerate(child.iter()):
				#search for 'typeOfMethod' element
				if schild.tag == "{" + ns['cmdp'] + "}" + "typeOfMethod":
					#store the loop position of the 'typeOfMethod' element
					typeOfMethodPos = count
					#store the value of the 'typeOfMethod' element in a var for later concatination
					typeOfMethodTemp = schild.text
					#check if the 'typeOfMethod' element has a value of 'Other'
					if schild.text == "Other":
						#iterate over all child elements of each 'Method' element and count their position in the loop
						for count,schild in enumerate(child.iter()):
							#search for the element that comes after the 'typeOfMethod' element in the loop 
							if count == typeOfMethodPos + 1:
								#store the value of the element after the 'typeOfMethod' element in the json object in the according layer
								additional_description['description'] = schild.text
					#continue, if 'typeOfMethod' element has a value that is not 'Other'
					else:
						#iterate over all child elements of each 'Method' element and count their position in the loop
						for count,schild in enumerate(child.iter()):
							#search for the element that comes after 'typeOfMethod' in the loop 
							if count == typeOfMethodPos + 1:
								#check if the 'typeOfMethod' element after the 'typeOfMethod' element has a value, that is not 'Other'
								if schild.text != "Other":
									#concatinate the value stored in the typeOfMethodTemp var with the value of the element after the 'typeOfMethod' element and store it in the json object in the according layer
									additional_description['description'] = typeOfMethodTemp + ": " + schild.text
								#continue, if the element after the 'typeOfMethod' element has a value of 'Other'
								else:
									#search for the element that comes after the element that comes after the 'typeOfMethod' element in the loop 
									for count,schild in enumerate(child.iter()):
										if count == typeOfMethodPos + 2:
											#concatinate the value stored in the typeOfMethodTemp var with the value of the element after the element after the 'typeOfMethod' element and store it in the json object in the according layer
											additional_description['description'] = typeOfMethodTemp + ": " + schild.text
				#store the 'Method' in the json object in the according layer
				additional_description_type['id'] = 'methods'
				#store the 'Method' in the json object in the according layer
				additional_description_type_title['en'] = 'Method'
				#add the layer object to its super-layer
				additional_description_type['title'] = additional_description_type_title
				#add the layer object to its super-layer
				additional_description['type'] = additional_description_type
			#append the additional descriptions object to the final json object
			FDAT['metadata']['additional_descriptions'].append(additional_description)							
	
	#rights (0-n)
	#search for 'rights' elemens
	if child.tag == "{" + ns['dc'] + "}" + "rights":
		#intialize an object to aggregate all information on a single rights statement
		right= {}
		#intialize objects to temporarily store information to create nested structure
		right_title = {}
		#add right description information to annotation tool later and implement here
		#right_description = {}
		#right_description['en'] = 
		#add right id information to annotation tool later and implement here
		#add right id
		#store the value of the 'rights' element in the json object in the according layer
		right_title['en'] = child.text		
		#add right link information to annotation tool later and implement here
		#right['link'] =
		#add the layer object to its super-layer
		right['title'] = right_title
		#append the rights object to the final json object
		FDAT['metadata']['rights'].append(right)
	
	#contributors (0-n)
	#search for 'contributor' elements
	if child.tag == "{" + ns['dc'] + "}" + "contributor":
		#intialize an object to aggregate all information on a single contributor
		contributor = {}
		#intialize objects to temporarily store information to create nested structure
		person_or_org = {}
		person_or_org_identifiers = []
		person_or_org_identifier = {}
		affiliations = []
		affiliation = {}
		#iterate over all child elements of each 'contributor' element
		for schild in child.iter():
			#search for 'contributor' element
			if schild.tag == "{" + ns['dc'] + "}" + "contributor":
				#intialize role object to temporarily store role information
				role = {}
				#store the value of the 'contributorType' attribute in the json object in the according layer
				role['id'] = schild.get('contributorType').lower()
				#add the layer object to its super-layer
				contributor['role'] = role
			#search for 'contributorName' element
			if schild.tag == "{" + ns['dc'] + "}" + "contributorName":
				#store the value of the 'contributorName' element in the json object in the according layer
				person_or_org['name'] = schild.text
				#store the value of the 'nameType' attribute in the json object in the according layer
				person_or_org['type'] = schild.get('nameType').lower()
			#search for 'givenName' element
			if schild.tag == "{" + ns['dc'] + "}" + "givenName":
				#store the value of the 'given_name' element in the json object in the according layer
				person_or_org['given_name'] = schild.text
			#search for 'familyName' element
			if schild.tag == "{" + ns['dc'] + "}" + "familyName":
				#store the value of the 'family_name' element in the json object in the according layer
				person_or_org['family_name'] = schild.text
			#search for 'nameIdentifier' element
			if schild.tag == "{" + ns['dc'] + "}" + "nameIdentifier":
				#store the value of the 'nameIdentifierScheme' attribute in the json object in the according layer
				person_or_org_identifier['scheme'] = schild.get('nameIdentifierScheme')
				#store the value of the 'nameIdentifier' element in the json object in the according layer
				person_or_org_identifier['identifier'] = schild.text
				#append a copy of the layer object to the super-layer object
				person_or_org_identifiers.append(person_or_org_identifier.copy())
				#add the layer object to its super-layer
				person_or_org['identifiers'] = person_or_org_identifiers			
				#add the layer object to its super-layer
				contributor['person_or_org'] = person_or_org
			#search for 'affiliation' element
			if schild.tag == "{" + ns['dc'] + "}" + "affiliation":
				#store the value of the 'affiliation' element in the json object in the according layer
				affiliation['id'] = schild.text
				#store the value of the 'affiliationIdentifier' attribute in the json object in the according layer
				affiliation['name'] = schild.get('affiliationIdentifier')
				#append a copy of the layer object to the super-layer object
				affiliations.append(affiliation.copy())
				#add the layer object to its super-layer
				contributor['affiliations'] = affiliations
		#add the layer object to its super-layer		
		contributor['person_or_org'] = person_or_org
		#append the contributor object to the final json object
		FDAT['metadata']['contributors'].append(contributor)
	
	#subjects (0-n)
    #search for 'subject', 'organism' or 'measurementTarget' elements
	if child.tag == "{" + ns['dc'] + "}" + "subject" or "{" + ns['cmdp'] + "}" + "organism" or "{" + ns['cmdp'] + "}" + "measurementTarget":
		#initialize object to temporary store information on a single subject
		subject = {}
		#search for 'subject' elements
		if child.tag == "{" + ns['dc'] + "}" + "subject":
			#add keyword id/link once available in the annotation tool and implement here
			#subject['id'] = 
			#store the value of the 'subject' element in the json object in the according layer
			subject['subject'] = child.text
			#append the subject object to the final json object
			FDAT['metadata']['subjects'].append(subject)
		#search for 'organism' elements
		if child.tag == "{" + ns['cmdp'] + "}" + "organism":
			#store the value of the 'organism' element in the json object in the according layer
			subject['subject'] = child.text
			#store the value of the 'organism' element in the json object in the according layer
			#subject['id'] = child.text
			#append the subject object to the final json object
			FDAT['metadata']['subjects'].append(subject)
		#search for 'measurementTarget' elements
		if child.tag == "{" + ns['cmdp'] + "}" + "measurementTarget":
			#store the value of the 'measurementTarget' element in the json object in the according layer
			subject['subject'] = child.text
			#subject['id'] = child.text
			#append the subject object to the final json object
			FDAT['metadata']['subjects'].append(subject)
    
	#languages (0-n)
	#search for 'language' element
	if child.tag == "{" + ns['dc'] + "}" + "language":
		#initialize object to temporary store information on a single language
		language = {}
		#store the value of the 'language' element in the json object in the according layer
		language['id'] = child.text
		#append the language object to the final json object
		FDAT['metadata']['languages'].append(language)

	#dates (0-n)
	#search for 'date' element
	if child.tag == "{" + ns['dc'] + "}" + "date":
		#initialize object to temporary store information on a single date
		date = {}
		#intialize objects to temporarily store information to create nested structure
		date_type = {}
		date_type_title = {}
		#store the value of the 'language' element in the json object in the according layer
		date['date'] = child.text
		#store the value of the 'dateInformation' attribute in the json object in the according layer
		date_type['id'] = child.get('dateInformation')
		#store the value of the 'dateType' attribute in the json object in the according layer
		date_type_title['en'] = child.get('dateType')
		#add the layer object to its super-layer
		date_type['title'] = date_type_title
		#add the layer object to its super-layer
		date['type'] = date_type
		#store the value of the 'dateInformation' attribute in the json object in the according layer
		date['description'] = child.get('dateInformation')
		#append the date object to the final json object
		FDAT['metadata']['dates'].append(date)
	
	#version (0-1)
	#not needed
	
	#publisher (0-1)
	#search for 'publisher' element
	if child.tag == "{" + ns['dc'] + "}" + "publisher":
		#store the value of the publisher element in the final json object
		FDAT['metadata']['publisher'] = child.text
	
	#alternate identifiers (0-n)
	#search for 'alternateIdentifier' element
	if child.tag == "{" + ns['dc'] + "}" + "alternateIdentifier":
		#initialize object to temporary store information on a alternateIdentifier
		identifier = {}
		#store the value of the 'identifier' element in the json object in the according layer
		identifier['identifier'] = child.text
		#store the value of the 'alternateIdentifierType' attribute in the json object in the according layer
		identifier['scheme'] = child.get('alternateIdentifierType')
		#append the alternate identifier object to the final json object
		FDAT['metadata']['identifiers'].append(identifier)
		
	
	#related identifiers (0-n)
	#search for 'relatedIdentifier' element
	if child.tag == "{" + ns['dc'] + "}" + "relatedIdentifier":
		#initialize object to temporary store information on a single relatedIdentifier
		related_identifier = {}
		#intialize objects to temporarily store information to create nested structure
		relation_type = {}
		relation_type_title = {}
		#store the value of the 'relatedIdentifier' element in the json object in the according layer
		related_identifier['identifier'] = child.text
		#store the value of the 'relatedIdentifierType' attribute in the json object in the according layer
		related_identifier['scheme'] = child.get('relatedIdentifierType')
		#store the value of the 'relationType' attribute in the json object in the according layer
		relation_type['id'] = child.get('relationType').lower()
		#store the value of the 'relationType' attribute in the json object in the according layer
		relation_type_title['en'] = child.get('relationType')
		#add the layer object to its super-layer
		relation_type['title'] = relation_type_title
		#add the layer object to its super-layer
		related_identifier['relationType'] = relation_type
		#append the related identifier object to the final json object
		FDAT['metadata']['related_identifiers'].append(related_identifier)
    
	#sizes (0-n)
	#not needed, as file upload will be handled via FDAT
	
	#formats (0-n)
	#not needed, as file upload will be handled via FDAT
	
	#locations (0-n)
	#not needed
	
	#funding references (0-n)
	#search for 'fundingReference' element
	if child.tag == "{" + ns['dc'] + "}" + "fundingReference":
		#initialize object to temporary store information on a single funding
		funding = {}
		#intialize objects to temporarily store information to create nested structure
		funding_funder = {}
		#define value to ensure correct order inside the layer object
		funding_funder['id'] = ""
		#define value to ensure correct order inside the layer object
		#define value to ensure correct order inside the layer object
		funding_funder['name'] = ""
		funding_award = {}
		#define value to ensure correct order inside the layer object
		funding_award['title'] = ""
		funding_award_title = {}
		funding_award_identifiers = []
		funding_award_identifier = {}
		#iterate over all child elements of each 'fundingReference' element
		for schild in child.iter():
			#search for 'funderIdentifier' element
			if schild.tag == "{" + ns['dc'] + "}" + "funderIdentifier":
				#store the value of the 'funderIdentifier' element in the json object in the according layer
				funding_funder['id'] = schild.text
			#search for 'funderName' element
			if schild.tag == "{" + ns['dc'] + "}" + "funderName":
				#store the value of the 'funderName' element in the json object in the according layer
				funding_funder['name'] = schild.text
			#search for 'awardTitle' element
			if schild.tag == "{" + ns['dc'] + "}" + "awardTitle":	
				#store the value of the 'funderName' element in the json object in the according layer
				funding_award_title['en'] = schild.text
				#add the layer object to its super-layer
				funding_award['title'] = funding_award_title 
			#search for 'awardNumber' element
			if schild.tag == "{" + ns['dc'] + "}" + "awardNumber":
				#store the value of the 'awardNumber' element in the json object in the according layer
				funding_award['number'] = schild.text
				#store the 'url' in the json object in the according layer
				funding_award_identifier['scheme'] = 'url'
				#store the value of the 'awardURI' attribute in the json object in the according layer
				funding_award_identifier['identifier'] = schild.get('awardURI')
				#add the layer object to its super-layer
				funding_award_identifiers.append(funding_award_identifier)
		#add the layer object to its super-layer
		funding['funder'] = funding_funder
		#add the layer object to its super-layer
		funding['award'] = funding_award
		#add the layer object to its super-layer
		funding['award']['identifiers'] = funding_award_identifiers
		#append the funding object to the final json object
		FDAT['metadata']['funding'].append(funding)
		
	#references (0-n)
	#not needed?

#create a json file
with open(sys.argv[2], 'w') as json_file:
	#write the final metadata json object into the json file
	json.dump(FDAT, json_file, indent=2, ensure_ascii=False)