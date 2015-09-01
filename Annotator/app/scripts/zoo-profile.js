/**
 * Created by yonatan on 5/5/2015.
 */
var zooProfile = {
  'id' : 'zoo',
  'name' : 'Zoo',
  'locations' : [ ],
  'characters' : [
    {'id': 'boy', 'name': 'Boy', 'gender': 'male', 'groups': ['family']},
    {'id': 'girl', 'name': 'Girl', 'gender': 'female', 'groups': ['family']},
    {'id': 'mother', 'name': 'Mother', 'gender': 'female', 'groups': ['family']},
    {'id': 'father', 'name': 'Father', 'gender': 'male', 'groups': ['family']},

    {'id':'rhino','name':'Rhino','gender':'','groups':['animals','with-horns']},
    {'id':'giraffe','name':'Giraffe','gender':'','groups':['animals','fabulous']},
    {'id':'lutra','name':'Lutra','gender':'','groups':['animals','eat-salmon']},
    {'id':'tiger','name':'Tiger','gender':'','groups':['animals','fabulous','with-teeth']},
    {'id':'seahorse','name':'Seahorse','gender':'','groups':['animals']},
    {'id':'peacock','name':'Peacock','gender':'','groups':['animals','fabulous']},
    {'id':'tukan','name':'Tukan','gender':'','groups':['animals','fabulous']},
    {'id':'wild-cat','name':'Wild Cat','gender':'','groups':['animals','with-teeth']},
    {'id':'lion','name':'Lion','gender':'','groups':['animals','with-teeth']},
    {'id':'bird','name':'Bird','gender':'','groups':['animals']},
    {'id':'bat','name':'Bat','gender':'','groups':['animals']},
    {'id':'dear','name':'Dear','gender':'','groups':['animals']},
    {'id':'lizard','name':'Lizard','gender':'','groups':['animals']},
    {'id':'fish','name':'Fish','gender':'','groups':['animals']},
    {'id':'duck','name':'Duck','gender':'','groups':['animals']},
    {'id':'spider','name':'Spider','gender':'','groups':['animals']},
    {'id':'squirrel','name':'Squirrel','gender':'','groups':['animals','with-teeth']},
    {'id':'hyena','name':'Hyena','gender':'','groups':['animals','with-teeth']},
    {'id':'butterfly','name':'Butterfly','gender':'','groups':['animals','fabulous']},
    {'id':'goat','name':'Goat','gender':'','groups':['animals']},
    {'id':'monkey','name':'Monkey','gender':'','groups':['animals','monkeys']},
    {'id': 'meerkat', 'name': 'Meerkat', 'gender': '', 'groups':['animals']},
    {'id': 'flamingo', 'name': 'Flamingo', 'gender': '', 'groups':['animals', 'eat-salmon', 'fabulous']},
    {'id': 'wolf', 'name': 'Wolf', 'gender': '', 'groups':['with-teeth', 'animals']},
    {'id': 'centipede', 'name': 'Centipede', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'shark', 'name': 'Shark', 'gender': '', 'groups':['with-teeth', 'animals', 'fabulous']},
    {'id': 'lemur', 'name': 'Lemur', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'sheep', 'name': 'Sheep', 'gender': '', 'groups':['with-horns', 'animals']},
    {'id': 'turtle', 'name': 'Turtle', 'gender': '', 'groups':['animals']},
    {'id': 'dog', 'name': 'Dog', 'gender': '', 'groups':['with-teeth', 'animals']},
    {'id': 'cat', 'name': 'Cat', 'gender': '', 'groups':['with-teeth', 'animals']},
    {'id': 'sea-lion', 'name': 'Sea Lion', 'gender': '', 'groups':['animals', 'eat-salmon']},
    {'id': 'chameleon', 'name': 'Chameleon', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'hippopotamus', 'name': 'Hippopotamus', 'gender': '', 'groups':['animals', 'eat-salmon']},
    {'id': 'sea-slug', 'name': 'SeaSlug', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'toad', 'name': 'Toad', 'gender': '', 'groups':['animals']},
    {'id': 'elephant', 'name': 'Elephant', 'gender': '', 'groups':['with-teeth', 'animals']},
    {'id': 'beetle', 'name': 'Beetle', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'camel', 'name': 'Camel', 'gender': '', 'groups':['animals']},
    {'id': 'mongoose', 'name': 'Mongoose', 'gender': '', 'groups':['with-teeth', 'animals']},
    {'id': 'water-ox', 'name': 'WaterOx', 'gender': '', 'groups':['with-horns', 'animals']},
    {'id': 'chimpanzee', 'name': 'Chimpanzee', 'gender': '', 'groups':['with-teeth', 'monkeys', 'animals']},
    {'id': 'snake', 'name': 'Snake', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'ice-bear', 'name': 'IceBear', 'gender': '', 'groups':['with-teeth', 'animals', 'eat-salmon']},
    {'id': 'gazelle', 'name': 'Gazelle', 'gender': '', 'groups':['with-horns', 'animals']},
    {'id': 'baboon', 'name': 'Baboon', 'gender': '', 'groups':['with-teeth', 'monkeys', 'animals']},
    {'id': 'globefish', 'name': 'Globefish', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'jaguar', 'name': 'Jaguar', 'gender': '', 'groups':['with-teeth', 'animals', 'fabulous']},
    {'id': 'rabbit', 'name': 'Rabbit', 'gender': '', 'groups':['animals']},
    {'id': 'tigris', 'name': 'Tigris', 'gender': '', 'groups':['with-teeth', 'animals', 'fabulous']},
    {'id': 'cheetah', 'name': 'Cheetah', 'gender': '', 'groups':['with-teeth', 'animals', 'fabulous']},
    {'id': 'sea-star', 'name': 'SeaStar', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'crawdaddy', 'name': 'Crawdaddy', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'orangutang', 'name': 'Orangutang', 'gender': '', 'groups':['with-teeth', 'monkeys', 'animals']},
    {'id': 'bittern', 'name': 'Bittern', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'panda', 'name': 'Panda', 'gender': '', 'groups':['with-teeth', 'animals', 'fabulous']},
    {'id': 'macaque', 'name': 'Macaque', 'gender': '', 'groups':['with-teeth', 'monkeys', 'animals']},
    {'id': 'indri', 'name': 'Indri', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'goldfish', 'name': 'Goldfish', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'wild-boar', 'name': 'WildBoar', 'gender': '', 'groups':['with-teeth', 'animals']},
    {'id': 'fox', 'name': 'Fox', 'gender': '', 'groups':['with-teeth', 'animals', 'fabulous']},
    {'id': 'anemone-fish', 'name': 'AnemoneFish', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'snow-leopard', 'name': 'SnowLeopard', 'gender': '', 'groups':['with-teeth', 'animals', 'fabulous']},
    {'id': 'warthog', 'name': 'Warthog', 'gender': '', 'groups':['with-teeth', 'animals']},
    {'id': 'polar-bear', 'name': 'PolarBear', 'gender': '', 'groups':['with-teeth', 'animals', 'eat-salmon']},
    {'id': 'siamang', 'name': 'Siamang', 'gender': '', 'groups':['monkeys', 'animals']},
    {'id': 'iguana', 'name': 'Iguana', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'llama', 'name': 'Llama', 'gender': '', 'groups':['animals']},
    {'id': 'gorilla', 'name': 'Gorilla', 'gender': '', 'groups':['with-teeth', 'monkeys', 'animals']},
    {'id': 'orangutan', 'name': 'Orangutan', 'gender': '', 'groups':['monkeys', 'animals']},
    {'id': 'wombat', 'name': 'Wombat', 'gender': '', 'groups':['animals']},
    {'id': 'crocodile', 'name': 'Crocodile', 'gender': '', 'groups':['with-teeth', 'animals']},
    {'id': 'impala', 'name': 'Impala', 'gender': '', 'groups':['with-horns', 'animals']},
    {'id': 'king-penguin', 'name': 'KingPenguin', 'gender': '', 'groups':['animals', 'eat-salmon', 'fabulous']},
    {'id': 'flatworm', 'name': 'Flatworm', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'macaw', 'name': 'Macaw', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'horse', 'name': 'Horse', 'gender': '', 'groups':['animals', 'eat-salmon']},
    {'id': 'starfish', 'name': 'Starfish', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'lionfish', 'name': 'Lionfish', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'buffalo', 'name': 'Buffalo', 'gender': '', 'groups':['with-horns', 'animals']},
    {'id': 'terrapin', 'name': 'Terrapin', 'gender': '', 'groups':['animals']},
    {'id': 'kangaroo', 'name': 'Kangaroo', 'gender': '', 'groups':['animals']},
    {'id': 'marmoset', 'name': 'Marmoset', 'gender': '', 'groups':['monkeys', 'animals', 'fabulous']},
    {'id': 'stork', 'name': 'Stork', 'gender': '', 'groups':['animals', 'eat-salmon', 'fabulous']},
    {'id': 'koala', 'name': 'Koala', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'american-black-bear', 'name': 'AmericanBlackBear', 'gender': '', 'groups':['animals', 'eat-salmon']},
    {'id': 'blue-jack', 'name': 'BlueJack', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'dromedary', 'name': 'Dromedary', 'gender': '', 'groups':['animals']},
    {'id': 'spider-monkey', 'name': 'Spider Monkey', 'gender': '', 'groups':['monkeys', 'animals', 'fabulous']},
    {'id': 'bison', 'name': 'Bison', 'gender': '', 'groups':['with-horns', 'animals']},
    {'id': 'swan', 'name': 'Swan', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'jacamar', 'name': 'Jacamar', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'ibex', 'name': 'Ibex', 'gender': '', 'groups':['with-horns', 'animals']},
    {'id': 'pelican', 'name': 'Pelican', 'gender': '', 'groups':['animals', 'eat-salmon']},
    {'id': 'marmot', 'name': 'Marmot', 'gender': '', 'groups':['animals']},
    {'id': 'brown-bear', 'name': 'BrownBear', 'gender': '', 'groups':['with-teeth', 'animals', 'eat-salmon']},
    {'id': 'dingo', 'name': 'Dingo', 'gender': '', 'groups':['with-teeth', 'animals']},
    {'id': 'zebra', 'name': 'Zebra', 'gender': '', 'groups':['animals', 'fabulous']},
    {'id': 'hartebeest', 'name': 'Hartebeest', 'gender': '', 'groups':['with-horns', 'animals']},
    {'id': 'platypus', 'name': 'Platypus', 'gender': '', 'groups':['animals']}
  ],
  'groups' : [
    {'id' : 'family','name' : 'Family'},
    {'id' : 'monkeys','name' : 'Monkeys'},
    {'id' : 'animals','name' : 'Animals'},
    {'id' : 'with-teeth','name' : 'With Sharp Teeth'},
    {'id' : 'fabulous','name' : 'Fabulous'},
    {'id' : 'eat-salmon','name' : 'Eats Salmon'},
    {'id' : 'with-horns','name' : 'With Horns'}
  ]
};

function Profile(obj) {
  $.extend(true, this, obj);
  this.timesById = arrayToObject(this.times);
  this.locationsById = arrayToObject(this.locations);
  this.charactersById = arrayToObject(this.characters);
  this.groupsById = arrayToObject(this.groups);
}

function arrayToObject(array) {
  var obj = {};
  for (var i in array) {
    var elem = array[i];
    obj[elem.id] = elem;
  }
  return obj;
}

angular.module('annotatorApp').constant('profile',
  new Profile(zooProfile)
);
