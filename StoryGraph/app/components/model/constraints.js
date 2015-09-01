(function (angular, $, undefined) {
    'use strict';
    var singleValueOperator = {
        'oneOf': {id: 'oneOf', name: 'Must be one of'},
        'notOneOf': {id: 'notOneOf', name: 'Cannot be one of'}
    };
    //multiplier - if more than 1 instance of that item is required.
    var multiValueOperator = {
        'includeAll': {id: 'includeAll', name: 'Must include all of', multiplier:true},
        'includeN': {id: 'includeN', name: 'Must include N of', extraN: true, multiplier:true},
        'excludeAll': {id: 'excludeAll', name: 'Cannot include all of'}
    };
    angular.module('sg.model')
        .factory('Constraint', function () {
            function Constraint(type, profile) {
                this.type = type;
                this.operator = null;
                this.value = [];
                this.soft = false;
                Object.defineProperty(this, 'profile', {
                    value: profile || undefined,
                    writable: true,
                    enumerable: false
                });
            }

            Constraint.prototype = Object.create({}, {
                operators: {value: {}},
                valueNames: {
                    get: function () {
                        var arr = [];
                        this.value.forEach(function (elem) {
                            arr.push(elem.name);
                        });
                        return arr;
                    }
                },
                clone: {
                    value: function () {
                        window.constOrg = this;
                        var cloned = new this.constructor(this.profile);
                        $.extend(cloned, this);
                        cloned.value = [].concat(this.value);
                        window.constClone = cloned;
                        return cloned;
                    }
                },
                mergeWith: {
                    value: function (other) {
                        $.extend(this, other);
                    }
                },
                valid: {
                    get: function () {
                        return this.operator && this.value && this.value.length > 0;
                    }
                }
            });

            Constraint.prototype.constructor = Constraint;

            return Constraint;
        })

        .factory('TimeConstraint', ['Constraint', function (Constraint) {
            function TimeConstraint(profile) {
                Constraint.call(this, 'when', profile);
            }

            TimeConstraint.prototype = Object.create(Constraint.prototype, {
                operators: {value: singleValueOperator},
                options: {
                    value: [
                        {id: 'early-morning', name: 'Early Morning'},
                        {id: 'morning', name: 'Morning'},
                        {id: 'afternoon', name: 'Afternoon'},
                        {id: 'evening', name: 'Evening'},
                        {id: 'night', name: 'Night'},
                        {id: 'late-night', name: 'Late Night'}
                    ]
                }
            });

            TimeConstraint.prototype.constructor = TimeConstraint;

            return TimeConstraint;
        }])

        .factory('CharacterConstraint', ['Constraint', function (Constraint) {
            function CharacterConstraint(profile) {
                Constraint.call(this, 'who', profile);
                this.group = null;
            }

            CharacterConstraint.prototype = Object.create(Constraint.prototype, {
                operators: {value: multiValueOperator},
                options: {
                    get: function () {
                        return this.profile.characters;
                    }
                }
            });

            CharacterConstraint.prototype.constructor = CharacterConstraint;

            return CharacterConstraint;
        }])


        .factory('LocationConstraint', ['Constraint', function (Constraint) {
            function LocationConstraint(profile) {
                Constraint.call(this, 'where', profile);
            }

            LocationConstraint.prototype = Object.create(Constraint.prototype, {
                operators: {value: singleValueOperator},
                options: {
                    get: function () {
                        return this.profile.locations;
                    }
                }
            });

            LocationConstraint.prototype.constructor = LocationConstraint;

            return LocationConstraint;
        }])


        .factory('ThingConstraint', ['Constraint', function (Constraint) {
            function ThingConstraint() {
                Constraint.call(this, 'what');
            }

            ThingConstraint.prototype = Object.create(Constraint.prototype, {
                operators: {value: multiValueOperator}
            });

            ThingConstraint.prototype.constructor = ThingConstraint;

            return ThingConstraint;
        }])

        .factory('ConstraintFactory', ['TimeConstraint', 'CharacterConstraint', 'LocationConstraint', 'ThingConstraint',
            function (TimeConstraint, CharacterConstraint, LocationConstraint, ThingConstraint) {
                return {
                    get: function (type) {
                        switch (type) {
                            case 'when':
                                return TimeConstraint;
                            case 'where':
                                return LocationConstraint;
                            case 'who':
                                return CharacterConstraint;
                            case 'what':
                                return ThingConstraint;
                        }
                        return null;
                    }
                };

            }]);

})(angular, jQuery);
