/**
 * Created by yonatan on 5/5/2015.
 */
'use strict';
angular.module('annotatorApp')
  .service('AnnotatedSetService', function ($resource, profile) {
    var that = this;
    var charIdMap = {};
    angular.forEach(profile.characters, function(character){
      charIdMap[character.id]=character;
    });
    var story = 'Riddle';
    //var story = 'MP';
    //var story = 'Zoo';

    var AnnSet = $resource('/resources/'+story+'/:id/annotatedSet.json');
    var prefix = '/Users/yonatan/Dropbox/Studies/Story Albums/Sets';
    that.getAnnotatedSet = function (id) {
      return AnnSet.get({id: id}, function (annotatedSet) {
        //annotatedSet.baseDir = annotatedSet.baseDir.substr(prefix.length);
        //annotatedSet.images.sort(function(i1,i2){
        //  var fn1=i1.imageFilename.replace('.txt','').replace('.jpg','');
        //  var fn2=i2.imageFilename.replace('.txt','').replace('.jpg','');
        //  return (+fn1)-(+fn2);
        //});
        //remove non existing characters
        //angular.forEach(annotatedSet.images, function(image){
        //  var i=0;
        //  while (i<image.characterIds.length){
        //    var id=image.characterIds[i];
        //    if (charIdMap[id]){
        //      i++;
        //    } else {
        //      image.characterIds.splice(i,1);
        //    }
        //  }
        //});
        angular.forEach(annotatedSet.images, function(image){
          image.imageFilename=image.imageFilename.replace('.txt','.jpg');
        });
      });
    };

  });
