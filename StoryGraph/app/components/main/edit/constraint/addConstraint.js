(function (angular, $, undefined) {
    'use strict';

    var find = function(arr, predicate){
        if (arr === null) {
            throw new TypeError('Array.prototype.find called on null or undefined');
        }
        if (typeof predicate !== 'function') {
            throw new TypeError('predicate must be a function');
        }
        var list = Object(arr);
        /*jshint bitwise: false */
        var length = list.length >>> 0;
        /*jshint bitwise: true */
        var value;

        for (var i = 0; i < length; i++) {
            value = list[i];
            if (predicate.call(arr, value, i, list)) {
                return value;
            }
        }
        return undefined;
    };

    angular.module('sg.main')
        .controller('AddConstraintController', ['$modalInstance', 'constraint', 'profile',
            'TimeConstraint', 'CharacterConstraint', 'LocationConstraint', 'ThingConstraint',
            function ($modalInstance, constraint, profile,
                      TimeConstraint, CharacterConstraint, LocationConstraint, ThingConstraint) {
                var ctrl = this;
                var originalConstraint = constraint;
                var editMode = ctrl.editMode = !!constraint;

                ctrl.type = editMode ? constraint.type : null;
                ctrl.page = editMode ? 2 : 1;
                ctrl.next = function () {
                    ctrl.page++;
                    var AbsConstructor;
                    switch (ctrl.type) {
                        case 'where':
                            AbsConstructor = LocationConstraint;
                            break;
                        case 'when':
                            AbsConstructor = TimeConstraint;
                            break;
                        case 'who':
                            AbsConstructor = CharacterConstraint;
                            break;
                        case 'what':
                            AbsConstructor = ThingConstraint;

                            break;
                    }
                    ctrl.constraint = new AbsConstructor(profile);
                };
                ctrl.back = function(){
                    ctrl.page--;
                    ctrl.constraint = null;
                };
                ctrl.constraint = editMode ? constraint.clone() : null;
                ctrl.cancel = function () {
                    return $modalInstance.dismiss();
                };
                ctrl.ok = function () {
                    if (editMode) {
                        originalConstraint.mergeWith(ctrl.constraint);
                    } else {
                        originalConstraint = ctrl.constraint;
                    }
                    return $modalInstance.close(originalConstraint);
                };
                ctrl.valueExists = function(text){

                    var v=!!find(ctrl.constraint.value, function(e){return e.id===text;});
                    return v;
                };

                ctrl.constraintWho = {
                    groupFilter : function(option) {
                        var group=ctrl.constraint.group.id;
                        return (option.groups.indexOf(group)>-1);
                    },
                    selectAllInGroup : function(){
                        var group=ctrl.constraint.group.id;
                        angular.forEach(ctrl.constraint.options, function(option){
                            var idx=ctrl.constraint.value.indexOf(option);
                            if ((option.groups.indexOf(group)>-1) && (idx===-1)){
                                ctrl.constraint.value.push(option);
                            }
                        });
                    },
                    deselectAllInGroup : function(){
                        var group=ctrl.constraint.group.id;
                        angular.forEach(ctrl.constraint.options, function(option){
                            var idx=ctrl.constraint.value.indexOf(option);
                            if ((option.groups.indexOf(group)>-1) && (idx>-1)){
                                ctrl.constraint.value.splice(idx,1);
                            }
                        });
                    }
                };

            }])
        .factory('addEditConstraint', ['$modal', function ($modal) {
            return function (constraint, profile) {
                var modalInstance = $modal.open({
                    templateUrl: '/components/main/edit/constraint/addConstraint.tpl.html',
                    controller: 'AddConstraintController as ctrl',
                    backdrop: true,
                    size: 'sm',
                    resolve: {
                        constraint: function () {
                            return constraint;
                        },
                        profile: function(){
                            return profile;
                        }
                    }
                });
                return modalInstance;
            };
        }]);
})(angular, jQuery);
