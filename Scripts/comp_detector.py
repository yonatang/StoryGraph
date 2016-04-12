import json
from pprint import pprint

with open('/Users/yonatan/Downloads/detections.json') as data_file:    
    detects = json.load(data_file)

manuals=[]

with open('/Users/yonatan/Dropbox/Studies/Story Albums/Sets/Riddle/Set1_full/annotatedSet.json') as data_file:
	data=json.load(data_file)
	manuals.extend(data['images'])
with open('/Users/yonatan/Dropbox/Studies/Story Albums/Sets/Riddle/Set5_full/annotatedSet.json') as data_file:
	data=json.load(data_file)
	manuals.extend(data['images'])
with open('/Users/yonatan/Dropbox/Studies/Story Albums/Sets/Riddle/Set2/annotatedSet.json') as data_file:
	data=json.load(data_file)
	manuals.extend(data['images'])
	

def remove_family(arr):
	def r(c):
		print 're',c
		while c in arr:
			print 'remove',arr,c
			arr.remove(c)
	r('girl')
	r('boy')
	r('father')
	r('mother')
	r('princessAriel-status')
	r('buzzLightYear-status')


def translate_manual(arr):
	def conv(fr,to):
		while fr in arr:
			arr.remove(fr)
			arr.append(to)
	conv('princessAurora','auaurora (sleeping beauty)')
	conv('princeAdamBeast','beast')
	conv('winnieThePooh','winnie the pooh')
	conv('donnaldDuck','donald duck')
	conv('mickeyMouse','mickey mouse')
	conv('princessSnowWhite','snow white')
	conv('peterPan','peter pan')


image_map={}
for manual in manuals:
	image_name = manual['imageFilename']
	characters=manual['characterIds']
	translate_manual(characters)
	characters.sort()
	remove_family(characters)
	image_map[image_name]={'manual':characters}

for detect in detects:
	image_name = detect['image_path'].split('/')[-1]
	if not image_name in image_map:
		continue
	characters=detect["character_names"]
	characters.sort()
	image_map[image_name]['auto']=characters


no_auto=0
both_empty=0
total=0
same=0
diff=0
for image_name in image_map:
	total += 1
	image=image_map[image_name]
	if ('auto' in image) and ('manual' in image):
		if len(image['auto'])==0 and len(image['manual'])==0:
			both_empty += 1
		elif image['auto']==image['manual']:
			same += 1
		else:
			diff += 1
			print image_name,image
	else:
		no_auto += 1
print 'Total',total
print 'No auto',no_auto
print 'Both empty',both_empty
print 'Same',same
print 'Diff',diff

#pprint(detects)

#pprint(manuals)