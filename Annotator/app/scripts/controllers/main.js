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

    ctrl.handleKeydown = function(event){

    }
    ctrl.handleKeyup = function(event, model){
      if (event.keyCode===27){
        ctrl.characterId='';
      } else
      if (!model){
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
      //$('#locationInput').focus(); //oh lord, forgive me.

    };

    $('#img').on('load', function () {
      $scope.$apply(function () {
        var elem = $('#img')[0];
        ctrl.imageData = {
          nHeight: elem.naturalHeight,
          nWidth: elem.naturalWidth,
          cHeight: elem.clientHeight,
          cWidth: elem.clientWidth
        };
      });
    });

    ctrl.nextImage = function(){
      if (ctrl.idx<annotatedSet.images.length-1){
        ctrl.idx++;
        ctrl.tagData.showTagger = false;
        ctrl.thisImage=annotatedSet.images[ctrl.idx];
        updateAutsuggest();

      }
    };
    ctrl.prevImage = function(){
      if (ctrl.idx>0){
        ctrl.idx--;
        ctrl.tagData.showTagger = false;
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
      var imageSrc = '/resources' + annotatedSet.baseDir + '/' + filename;
      return imageSrc;
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
      var charId=ctrl.thisImage.characterIds[idx];
      ctrl.thisImage.characterIds.splice(idx, 1);
      delete ctrl.thisImage.charQualities[charId];
    };
    ctrl.removeItem = function(idx){
      ctrl.thisImage.itemIds.splice(idx, 1);
    };

    ctrl.isCharHaveTag = function (idx) {
      var charId = ctrl.thisImage.characterIds[idx];
      var img = ctrl.thisImage;
      if (!img.charQualities || !img.charQualities[charId]) {
        return false;
      }
      var charQuality = img.charQualities[charId];
      return (charQuality.length > 0);
    };

    ctrl.tagData = {};
    ctrl.showBorder = function (idx, show) {
      var charId = ctrl.thisImage.characterIds[idx];
      var charQuality = ctrl.thisImage.charQualities[charId][0].box;
      ctrl.tagData.showTagger = show;

      ctrl.tagData.charId = charId;
      var data = ctrl.imageData,
        ratioW = data.cWidth / data.nWidth,
        ratioH = data.cHeight / data.nHeight;

      var x = charQuality.x * ratioW,
        y = charQuality.y * ratioH,
        height = charQuality.height * ratioH,
        width = charQuality.width * ratioW;

      ctrl.tagData.borderStyle = {
        left: x + 'px',
        top: y + 'px',
        width: width + 'px',
        height: height + 'px'
      };
    };

    ctrl.tagCharachter = function (idx) {
      var charId = ctrl.thisImage.characterIds[idx];
      ctrl.tagData.inProgress = true;
      ctrl.tagData.charId = charId;
      ctrl.tagData.firstClick = true;
      ctrl.tagData.charId = charId;
    };
    ctrl.imageMouseMove = function ($event, inTagger) {
      if (!ctrl.tagData.inProgress || ctrl.tagData.firstClick) {
        return;
      }

      var x = $event.offsetX,
        y = $event.offsetY;

      if (!inTagger){
        x -= ctrl.tagData.x;
        y -= ctrl.tagData.y;
      }
      ctrl.tagData.borderStyle.width = x +'px';
      ctrl.tagData.borderStyle.height = y + 'px';
    };
    ctrl.imageClick = function ($event) {
      if (!ctrl.tagData.inProgress){
        return;
      }
      var x = $event.offsetX,
        y = $event.offsetY,
        data = ctrl.imageData;

      var realX = x * data.nWidth / data.cWidth,
        realY = y * data.nHeight / data.cHeight;

      if (ctrl.tagData.firstClick) {
        ctrl.tagData.x = x;
        ctrl.tagData.y = y;
        ctrl.tagData.realX = realX;
        ctrl.tagData.realY = realY;
        ctrl.tagData.firstClick = false;
        ctrl.tagData.showTagger = true;
        ctrl.tagData.borderStyle = {
          left: x + 'px',
          top: y + 'px',
          width: 1 + 'px',
          height: 1 + 'px'
        };
      } else {
        var charQuality = {
          box : {
            x: parseInt(ctrl.tagData.realX),
            y: parseInt(ctrl.tagData.realY),
            width: parseInt(realX - ctrl.tagData.realX),
            height: parseInt(realY - ctrl.tagData.realY)
          }
        };
        if (!ctrl.thisImage.charQualities) {
          ctrl.thisImage.charQualities={};
        }
        ctrl.thisImage.charQualities[ctrl.tagData.charId] = [charQuality];
        ctrl.tagData.inProgress=false;
        ctrl.tagData.showTagger = false;
      }
    };
  });
