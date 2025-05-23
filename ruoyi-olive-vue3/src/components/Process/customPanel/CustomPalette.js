/**
 * 自定义左侧工具栏
 * https://github.com/bpmn-io/bpmn-js/blob/80c1dc3231d274bc1d1a42afd21fc46a0cbe5b8f/lib/features/palette/PaletteProvider.js#L100
 */
import {
  assign
} from 'min-dash';

import { getDi } from 'bpmn-js/lib/util/ModelUtil';

/**
 * @typedef {import('diagram-js/lib/features/palette/Palette').default} Palette
 * @typedef {import('diagram-js/lib/features/create/Create').default} Create
 * @typedef {import('diagram-js/lib/core/ElementFactory').default} ElementFactory
 * @typedef {import('../space-tool/BpmnSpaceTool').default} SpaceTool
 * @typedef {import('diagram-js/lib/features/lasso-tool/LassoTool').default} LassoTool
 * @typedef {import('diagram-js/lib/features/hand-tool/HandTool').default} HandTool
 * @typedef {import('diagram-js/lib/features/global-connect/GlobalConnect').default} GlobalConnect
 * @typedef {import('diagram-js/lib/i18n/translate/translate').default} Translate
 *
 * @typedef {import('diagram-js/lib/features/palette/Palette').PaletteEntries} PaletteEntries
 */

/**
 * A palette provider for BPMN 2.0 elements.
 *
 * @param {Palette} palette
 * @param {Create} create
 * @param {ElementFactory} elementFactory
 * @param {SpaceTool} spaceTool
 * @param {LassoTool} lassoTool
 * @param {HandTool} handTool
 * @param {GlobalConnect} globalConnect
 * @param {Translate} translate
 */
export default function PaletteProvider(
    palette, create, elementFactory,
    spaceTool, lassoTool, handTool,
    globalConnect, translate) {

  this._palette = palette;
  this._create = create;
  this._elementFactory = elementFactory;
  this._spaceTool = spaceTool;
  this._lassoTool = lassoTool;
  this._handTool = handTool;
  this._globalConnect = globalConnect;
  this._translate = translate;

  palette.registerProvider(this);
}

PaletteProvider.$inject = [
  'palette',
  'create',
  'elementFactory',
  'spaceTool',
  'lassoTool',
  'handTool',
  'globalConnect',
  'translate'
];

/**
 * @return {PaletteEntries}
 */
PaletteProvider.prototype.getPaletteEntries = function() {

  var actions = {},
      create = this._create,
      elementFactory = this._elementFactory,
      spaceTool = this._spaceTool,
      lassoTool = this._lassoTool,
      handTool = this._handTool,
      globalConnect = this._globalConnect,
      translate = this._translate;

  function createAction(type, group, className, title, options) {

    function createListener(event) {
      var shape = elementFactory.createShape(assign({ type: type }, options));

      if (options) {
        var di = getDi(shape);
        di.isExpanded = options.isExpanded;
      }

      create.start(event, shape);
    }

    var shortType = type.replace(/^bpmn:/, '');

    return {
      group: group,
      className: className,
      title: title || translate('Create {type}', { type: shortType }),
      action: {
        dragstart: createListener,
        click: createListener
      }
    };
  }

  function createSubprocess(event) {
    var subProcess = elementFactory.createShape({
      type: 'bpmn:SubProcess',
      x: 0,
      y: 0,
      isExpanded: true
    });

    var startEvent = elementFactory.createShape({
      type: 'bpmn:StartEvent',
      x: 40,
      y: 82,
      parent: subProcess
    });

    create.start(event, [ subProcess, startEvent ], {
      hints: {
        autoSelect: [ subProcess ]
      }
    });
  }

  function createParticipant(event) {
    create.start(event, elementFactory.createParticipantShape());
  }

  assign(actions, {
    'hand-tool': {
      group: 'tools',
      className: 'bpmn-icon-hand-tool',
      title: translate('Activate the hand tool'),
      action: {
        click: function(event) {
          handTool.activateHand(event);
        }
      }
    },
    'lasso-tool': {
      group: 'tools',
      className: 'bpmn-icon-lasso-tool',
      title: translate('Activate the lasso tool'),
      action: {
        click: function(event) {
          lassoTool.activateSelection(event);
        }
      }
    },
    'space-tool': {
      group: 'tools',
      className: 'bpmn-icon-space-tool',
      title: translate('Activate the create/remove space tool'),
      action: {
        click: function(event) {
          spaceTool.activateSelection(event);
        }
      }
    },
    'global-connect-tool': {
      group: 'tools',
      className: 'bpmn-icon-connection-multi',
      title: translate('Activate the global connect tool'),
      action: {
        click: function(event) {
          globalConnect.start(event);
        }
      }
    },
    'tool-separator': {
      group: 'tools',
      separator: true
    },
    'create.start-event': createAction(
        'bpmn:StartEvent', 'event', 'bpmn-icon-start-event-none',
        translate('Create StartEvent')
    ),
    'create.intermediate-event': createAction(
        'bpmn:IntermediateThrowEvent', 'event', 'bpmn-icon-intermediate-event-none',
        translate('Create Intermediate/Boundary Event')
    ),
    'create.end-event': createAction(
        'bpmn:EndEvent', 'event', 'bpmn-icon-end-event-none',
        translate('Create EndEvent')
    ),
    'create.exclusive-gateway': createAction(
        'bpmn:ExclusiveGateway', 'gateway', 'bpmn-icon-gateway-none',
        translate('Create Gateway')
    ),
    'create.task': createAction(
        'bpmn:Task', 'activity', 'bpmn-icon-task',
        translate('Create Task')
    ),
    'create.user-task': createAction(
        'bpmn:UserTask', 'activity', 'bpmn-icon-user-task',
        translate('Create User Task')
    ),
    'create.data-object': createAction(
        'bpmn:DataObjectReference', 'data-object', 'bpmn-icon-data-object',
        translate('Create DataObjectReference')
    ),
    'create.data-store': createAction(
        'bpmn:DataStoreReference', 'data-store', 'bpmn-icon-data-store',
        translate('Create DataStoreReference')
    ),
    'create.subprocess-expanded': {
      group: 'activity',
      className: 'bpmn-icon-subprocess-expanded',
      title: translate('Create expanded SubProcess'),
      action: {
        dragstart: createSubprocess,
        click: createSubprocess
      }
    },
    'create.participant-expanded': {
      group: 'collaboration',
      className: 'bpmn-icon-participant',
      title: translate('Create Pool/Participant'),
      action: {
        dragstart: createParticipant,
        click: createParticipant
      }
    },
    'create.group': createAction(
        'bpmn:Group', 'artifact', 'bpmn-icon-group',
        translate('Create Group')
    ),
  });

  return actions;
};
