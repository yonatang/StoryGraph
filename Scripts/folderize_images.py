import json
import os
from shutil import copyfile

filename = '/Users/yonatan/Dropbox/Studies/Story Albums/Sets/Zoo/72157603658654812/annotatedSet.json'
base = '/Users/yonatan/Dropbox/Studies/Story Albums/Sets'
outputPath = '/Users/yonatan/out/'


def create_and_copy(outFolder, outFilename, srcFullPath):
    output_path_char = outputPath + outFolder
    if not os.path.exists(output_path_char): os.makedirs(output_path_char)
    copyfile(srcFullPath, output_path_char + '/' + outFilename)


with open(filename) as data_file:
    album = json.load(data_file)
    relativeBase = album['baseDir']
    images = album['images']
    for image in images:
        imageFilename = image['imageFilename']
        imageFullPath = base + relativeBase + '/' + image['imageFilename']
        characters = image['characterIds']
        if len(characters) == 0:
            create_and_copy('none', imageFilename, imageFullPath)
            # outputPathChar = outputPath+'none'
            # if not os.path.exists(outputPathChar): os.makedirs(outputPathChar)
            # copyfile(imageFullPath, outputPathChar+'/'+imageFilename)

        for character in characters:
            create_and_copy(character, imageFilename, imageFullPath)
            # outputPathChar = outputPath+character
            # if not os.path.exists(outputPathChar): os.makedirs(outputPathChar)
            # copyfile(imageFullPath, outputPathChar+'/'+imageFilename)
