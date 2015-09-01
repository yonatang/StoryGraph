(function (angular, undefined) {
    'use strict';

    var staticEnhencment = function (DepObject) {
        DepObject.operators = DepObject.prototype.operators;
    };
    angular.module('sg.model')
        .factory('Dependency', [function () {
            function Dependency(type, fromEventId, toEventId) {
                this.fromEventId = fromEventId;
                this.toEventId = toEventId;
                this.type = type;
                this.operator = null;
                this.value = null;
            }

            Dependency.prototype = Object.create({}, {
                operators: {value: {}},
                closedListValue: {value: false}
            });
            Dependency.prototype.constructor = Dependency;
            return Dependency;

        }])

        .factory('TimeDependency', ['Dependency',
            function (Dependency) {
                function TimeDependency(fromEventId, toEventId) {
                    Dependency.call(this, 'when', fromEventId, toEventId);
                }

                TimeDependency.prototype = Object.create(Dependency.prototype, {
                    operators: {
                        value: {
                            'increase': {id: 'increase', name: 'Increase'},
                            'decrease': {id: 'decrease', name: 'Decrease'},
                            'same': {id: 'same', name: 'Same'}
                        }
                    }
                });
                staticEnhencment(TimeDependency);
                TimeDependency.prototype.constructor = TimeDependency;
                return TimeDependency;

            }])

        .factory('LocationDependency', ['Dependency',
            function (Dependency) {
                function LocationDependency(fromEventId, toEventId) {
                    Dependency.call(this, 'where', fromEventId, toEventId);
                }

                LocationDependency.prototype = Object.create(LocationDependency.prototype, {
                    operators: {
                        value: {
                            'same': {id: 'same', name: 'Same'},
                            'different': {id: 'different', name: 'Different'}
                        }
                    }
                });
                staticEnhencment(LocationDependency);
                LocationDependency.prototype.constructor = LocationDependency;
                return LocationDependency;

            }])

        .factory('CharacterDependency', ['Dependency',
            function (Dependency) {
                function CharacterDependency(fromEventId, toEventId) {
                    Dependency.call(this, 'who', fromEventId, toEventId);
                }

                CharacterDependency.prototype = Object.create(CharacterDependency.prototype, {
                    operators: {
                        value: {
                            'include': {id: 'include', name: 'Include'},
                            'includeN': {id: 'includeN', name: 'Include at least N', extraN: true},
                            'exclude': {id: 'exclude', name: 'Exclude'}
                        }
                    },
                    valueNames: {
                        get: function () {
                            var arr = [];
                            if (!this.value) {
                                return arr;
                            }
                            this.value.forEach(function (elem) {
                                arr.push(elem.name);
                            });
                            return arr;
                        }
                    }
                });
                staticEnhencment(CharacterDependency);
                CharacterDependency.prototype.constructor = CharacterDependency;
                return CharacterDependency;

            }])

        .factory('ThingDependency', ['Dependency',
            function (Dependency) {
                function ThingDependency(fromEventId, toEventId) {
                    Dependency.call(this, 'what', fromEventId, toEventId);
                }

                ThingDependency.prototype = Object.create(ThingDependency.prototype, {
                    operators: {
                        value: {
                            'include': {id: 'include', name: 'Include'},
                            'exclude': {id: 'exclude', name: 'Exclude'}
                        }
                    }
                });
                staticEnhencment(ThingDependency);
                ThingDependency.prototype.constructor = ThingDependency;
                return ThingDependency;

            }]);


})(angular);
