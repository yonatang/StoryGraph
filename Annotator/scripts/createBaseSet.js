var setDir='/Users/yonatan/Dropbox/Studies/Story Albums/sets/MP/72157602995838247/images';
var fs=require('fs');

var files = fs.readdirSync(setDir);

files.forEach(function(file) {
  fullFile = setDir + '/' + file;
  if (file.indexOf('.jpg')>1) {
    var stat = fs.statSync(fullFile);
    console.log(file, stat.isFile());
  }
});
