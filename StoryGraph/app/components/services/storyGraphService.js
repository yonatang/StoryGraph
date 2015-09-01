(function (angular, window, undefined) {
    'use strict';

    function StoryEventDependency(event1Id, event2Id, type, data) {
        this.event1Id = event1Id;
        this.event2Id = event2Id;
        this.type = type;
        this.data = data;
    }

    StoryEventDependency.prototype.toString = function () {
        return '(' + this.type + ': ' + this.event1Id + ' -> ' + this.event2Id + ')';
    };

    //https://github.com/cpettitt/graphlib
    angular.module('sg.services').service('storyGraphService', [
        'sg.profiles', 'StoryEvent', 'ConstraintFactory',
        function (profiles, StoryEvent, ConstraintFactory) {
            var that = this;
            if (!window.graphlib) {
                throw new Error('Missing graphlib');
            }
            var _graph = new window.graphlib.Graph({multigraph: true}),
                _id = 0,
                _state = {selectedEvents: [], selectedDependencies: []},
                _nodes = [],
                _edges = [],
                _eventEdges = {};

            var clearState = function () {
                _state.selectedEvents.splice(0);
                _state.selectedEvents.splice(0);
            };
            clearState();

            Object.defineProperty(_state, 'numSelected', {
                get: function () {
                    return this.selectedEvents.length + this.selectedDependencies.length;
                }
            });

            this.addNewEvent = function (x, y) {
                _id++;
                _graph.setNode(_id, new StoryEvent(_id, x, y));
                refreshDataStructure();
            };

            this.addDependency = function (dependencyObj) {
                var v = dependencyObj.fromEventId,
                    w = dependencyObj.toEventId,
                    type = dependencyObj.type;

                _graph.setEdge(v, w, dependencyObj, type);
                refreshDataStructure();
            };

            this.removeEvent = function (events) {
                if (!events) {
                    console.error('trying to remove null event');
                    return;
                }
                if (!angular.isArray(events)) {
                    events = [events];
                } else {
                    events = [].concat(events);
                }
                angular.forEach(events, function (event) {
                    _graph.removeNode(event.id);
                    var idx = _state.selectedEvents.indexOf(event);
                    if (idx > -1) {
                        _state.selectedEvents.splice(idx, 1);
                    }
                });
                refreshDataStructure();
            };

            var deselectAll = this.deselectAll = function () {
                angular.forEach(_nodes, function (node) {
                    node.selected = false;
                });
                angular.forEach(_edges, function (edge) {
                    edge.selected = false;
                });
                _state.selectedEvents = [];
                _state.selectedDependencies = [];
            };

            this.getEventById = function (eventId) {
                return _graph.node(eventId);
            };

            this.selectEvent = function (event, append) {
                if (!append) {
                    deselectAll();
                }
                if (append && event.selected) {
                    event.selected = false;
                    var idx = _state.selectedEvents.indexOf(event);
                    if (idx > -1) {
                        _state.selectedEvents.splice(idx, 1);
                    }
                } else {
                    event.selected = true;
                    _state.selectedEvents.push(event);
                }
            };

            this.selectDependency = function (dependency, append) {
                if (!append) {
                    deselectAll();
                }
                if (append && dependency.selected) {
                    dependency.selected = false;
                    var idx = _state.selectedDependencies.indexOf(dependency);
                    if (idx > -1) {
                        _state.selectedDependencies.splice(idx, 1);
                    }
                } else {
                    dependency.selected = true;
                    _state.selectedDependencies.push(dependency);
                }
            };

            this.removeDependency = function (dependencies) {
                if (!dependencies) {
                    console.error('trying to remove null dependency');
                    return;
                }
                if (!angular.isArray(dependencies)) {
                    dependencies = [dependencies];
                } else {
                    dependencies = [].concat(dependencies);
                }
                angular.forEach(dependencies, function (dependency) {
                    _graph.removeEdge(dependency.fromEventId, dependency.toEventId, dependency.type);
                    var idx = _state.selectedDependencies.indexOf(dependency);
                    if (idx > -1) {
                        _state.selectedDependencies.splice(idx, 1);
                    }
                });
                refreshDataStructure();
            };

            var refreshDataStructure = function () {
                _nodes.splice(0);
                _edges.splice(0);
                Object.keys(_eventEdges).forEach(function (key) {
                    delete _eventEdges[key];
                });
                var init = function (node) {
                    if (!_eventEdges[node]) {
                        _eventEdges[node] = {inDeps: [], outDeps: []};
                    }
                };

                angular.forEach(_graph.nodes(), function (nodeName) {
                    init(nodeName);
                    _nodes.push(_graph.node(nodeName));
                });
                angular.forEach(_graph.edges(), function (edge) {
                    _edges.push(_graph.edge(edge));
                    var inNode = edge.v,
                        outNode = edge.w,
                        edgeData = _graph.edge(inNode, outNode, edge.name);
                    _eventEdges[inNode].outDeps.push(edgeData);
                    _eventEdges[outNode].inDeps.push(edgeData);
                });
            };

            this.state = _state;
            this.events = _nodes;
            this.eventDeps = _eventEdges;
            this.dependencies = _edges;
            this.profile = profiles[0];

            this.export = function () {
                var graph = angular.copy({
                    graph: window.graphlib.json.write(_graph),
                    profile: that.profile
                });
                // clear selected nodes
                angular.forEach(graph.graph.nodes, function (node) {
                    delete node.value.selected;
                });
                return graph;
            };
            var reinstantiateGraph = function (nodes) {
                //reinstantiate nodes
                angular.forEach(nodes, function (nodeName) {
                    var node = _graph.node(nodeName);
                    var newNode = new StoryEvent();
                    newNode.mergeWith(node);
                    if (node.id>_id){
                        _id=node.id;
                    }
                    //reinstantiate constraints of the node
                    var newConstraints = [];
                    var constraints = [].concat(newNode.constraints);
                    angular.forEach(constraints, function (constraint) {
                        var Constraint = ConstraintFactory.get(constraint.type);
                        var newConstraint = new Constraint();
                        newConstraint.mergeWith(constraint);
                        newConstraints.push(newConstraint);
                    });
                    newNode.constraints = newConstraints;
                    _graph.setNode(newNode.id, newNode);
                    _nodes.push(_graph.node(nodeName));
                });
            };
            this.import = function (data) {
                _graph = window.graphlib.json.read(data.graph);
                reinstantiateGraph(_graph.nodes());
                clearState();
                refreshDataStructure();
                that.profile = data.profile;
            };

        }]);

})(angular, window);
