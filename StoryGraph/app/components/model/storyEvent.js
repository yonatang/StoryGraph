(function (angular, $, undefined) {
    'use strict';
    angular.module('sg.model')
        .factory('StoryEvent', [function () {
            function StoryEvent(id, x, y, name) {
                this.id = id;
                this.x = x;
                this.y = y;
                this.selected = false;
                this.name = name || 'Event ' + id;
                this.constraints = [];
                this.text = '';
            }

            StoryEvent.prototype = Object.create({}, {
                toString: {
                    value: function () {
                        return '(' + this.id + ':' + this.name + ')';
                    }
                },
                clone: {
                    value: function () {
                        window.sgeOrg=this;
                        var cloned = new StoryEvent(this.id, this.x, this.y);
                        cloned.selected = this.selected;
                        cloned.name = this.name;
                        cloned.text = this.text;
                        angular.forEach(this.constraints, function (constraint) {
                            cloned.constraints.push(constraint.clone());
                        });
                        window.sgeClone=cloned;
                        return cloned;
                    }
                },
                mergeWith: {
                    value: function(other){
                        $.extend(this,other);
                    }
                }
            });

            StoryEvent.prototype.constructor = StoryEvent;
            return StoryEvent;
        }]);
})(angular, jQuery);
