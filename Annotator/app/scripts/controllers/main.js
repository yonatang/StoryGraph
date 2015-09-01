'use strict';

/**
 * @ngdoc function
 * @name annotatorApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the annotatorApp
 */
angular.module('annotatorApp')
  .controller('MainCtrl', function ($scope, AnnotatedSetService, profile,hotkeys) {
    var ctrl = this;
    ctrl.thisImage = {};
    ctrl.characterId = null;
    ctrl.itemID = null;
    ctrl.albumId = 'Set5_full';
    var annotatedSet;
    //var annotatedSet = ctrl.annotatedSet = AnnotatedSetService.getAnnotatedSet(ctrl.albumId);
    ctrl.idx = 0;
    ctrl.loadAlbum = function(albumId){
      annotatedSet = ctrl.annotatedSet = AnnotatedSetService.getAnnotatedSet(albumId);
      ctrl.albumId=albumId;
      $scope.albumId='';
      annotatedSet.$promise.then(function () {
        ctrl.thisImage = annotatedSet.images[0];
        ctrl.idx=0;
      });
    };
    ctrl.loadAlbum(ctrl.albumId);

    //annotatedSet.$promise.then(function () {
    //  ctrl.thisImage = annotatedSet.images[ctrl.idx];
    //});
    console.log(profile);
    ctrl.characters = profile.characters;
    ctrl.items = profile.items;
    ctrl.locations = profile.locations;
    console.log(ctrl.items);

    hotkeys.add({
      combo: 'right',
      description: 'Next Image',
      callback: function() {
        ctrl.nextImage();
      }
    });
    hotkeys.add({
      combo: 'left',
      description: 'Previous Image',
      callback: function() {
        ctrl.prevImage();
      }
    });

    ctrl.handleKeyup = function(event){
      if (event.keyCode===27){
        ctrl.characterId='';
      } else
      if (!ctrl.characterId){
        console.log(ctrl.characterId);
        if (event.keyCode===39){
          ctrl.nextImage();
        }
        if (event.keyCode===37){
          ctrl.prevImage();
        }

      }

      //console.log('1',event);
    };

    var updateAutsuggest = function(){
      var allIds={};
      var allCIds={};
      angular.forEach(annotatedSet.images, function(image){
        if (image.locationId) {
          allIds[image.locationId]=true;
        }
        angular.forEach(image.characterIds, function(charId){
          allCIds[charId]=true;
        });
      });
      var allExistingIds={};
      var allCExistingIds={};
      angular.forEach(ctrl.locations, function(location){
        allExistingIds[location.id]=true;
      });
      angular.forEach(ctrl.characters, function(character){
        allCExistingIds[character.id]=true;
      });
      var id;
      for (id in allIds){
        if (!allExistingIds[id]){
          ctrl.locations.push({id:id,name:id});
          console.log('added location',id);
        }
      }
      for (id in allCIds){
        if (!allCExistingIds[id]){
          ctrl.characters.push({id:id,name:id,gender:'',groups:[]});
          console.log('added char',id);
        }
      }
      $('#locationInput').focus(); //oh lord, forgive me.

    };

    ctrl.nextImage = function(){
      if (ctrl.idx<annotatedSet.images.length-1){
        ctrl.idx++;
        ctrl.thisImage=annotatedSet.images[ctrl.idx];
        updateAutsuggest();

      }
    };
    ctrl.prevImage = function(){
      if (ctrl.idx>0){
        ctrl.idx--;
        ctrl.thisImage=annotatedSet.images[ctrl.idx];
        updateAutsuggest();
      }
    };

    ctrl.getImageSrc = function () {
      var filename = ctrl.thisImage.imageFilename;
      if (!filename) {
        return null;
      }
      filename = filename.replace('.txt', '.jpg');
      return '/resources' + annotatedSet.baseDir + '/' + filename;
    };
    ctrl.addCharacter = function () {
      if (ctrl.characterId) {
        ctrl.thisImage.characterIds.push(ctrl.characterId);
        ctrl.characterId = null;
      }
    };
    ctrl.addItem = function () {
      if (ctrl.itemId) {
        ctrl.thisImage.itemIds.push(ctrl.itemId);
        ctrl.itemId = null;
      }
    };
    ctrl.removeCharachter = function (idx) {
      ctrl.thisImage.characterIds.splice(idx, 1);
    };
    ctrl.removeItem = function(idx){
      ctrl.thisImage.itemIds.splice(idx, 1);
    };
  });
