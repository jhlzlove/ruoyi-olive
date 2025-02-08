<template>
  <div>
    <el-form-item label="条件事件">
       <el-input v-model="bpmnFormData.conditionValue" placeholder="请输入条件" @change="updateElementEvent"/>
    </el-form-item>
    <el-form-item label="变量名称">
       <el-input v-model="bpmnFormData.variableName" placeholder="请输入变量名称" @change="updateElementEvent"/>
    </el-form-item>
    <el-form-item label="操作类型">
      <el-select v-model="bpmnFormData.variableEvent" @change="updateElementEvent" multiple placeholder="请选择操作类型" style="width: 280px">
        <el-option
            v-for="item in variableEventType"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </el-select>
    </el-form-item>
  </div>
</template>

<script setup name="ConditionEventPanel">
import useModelerStore from '@/store/modules/modeler'
import {uuid, checkEventType} from "@/components/Process/common/bpmnUtils";
const modelerStore = useModelerStore()

const {proxy} = getCurrentInstance();
const data = reactive({
  bpmnFormData: {
    conditionValue: '',
    variableName: '',
    variableEvent: [],
    eventDefinitions: []
  },
  variableEventType: [
    {value: 'create',label: 'create',},
    {value: 'update',label: 'update',},
    {value: 'delete',label: 'delete',},
  ]
});

const { bpmnFormData,variableEventType } = toRefs(data);

/** 组件传值  */
const props = defineProps({
    id: {
        type: String,
        required: true
    },
});

/** 传值监听 */
watch(() => props.id, newVal => {
      if (newVal) {
        resetTaskForm();
      }
    },
    {immediate: true}
)

function resetTaskForm() {
  bpmnFormData.value = {
    conditionValue: '',
    variableName: '',
    variableEvent: [],
    eventDefinitions: []
  };
  bpmnFormData.value.$type = modelerStore.getBpmnElement?.businessObject?.$type;
  // 处理事件
  if (checkEventType(bpmnFormData.value.$type)) {
    getFlowEvent(bpmnFormData.value, modelerStore.getBpmnElement.businessObject)
  }
}

// 处理事件
function getFlowEvent(bpmnFormData, businessObject) {
  if (businessObject.eventDefinitions && businessObject.eventDefinitions.length > 0) {
    if (businessObject.eventDefinitions[0].$type.indexOf("Condition") !== -1) {
      bpmnFormData.conditionValue = businessObject.eventDefinitions[0]['condition'].body;
      bpmnFormData.variableName = businessObject.eventDefinitions[0].variableName;
      const variableEvent = businessObject.eventDefinitions[0].variableEvent;
      bpmnFormData.variableEvent = variableEvent ? variableEvent.split(',') : [];
    }
  }
}

// 更新条件事件
function updateElementEvent() {
  const conditionValue = bpmnFormData.value.conditionValue || null;
  if (conditionValue) {
    let taskAttr = Object.create(null);
    taskAttr.variableName = bpmnFormData.value.variableName || null;
    taskAttr.variableEvent = bpmnFormData.value.variableEvent?.join(',') || null;

    const conditionExpression = modelerStore.getModdle.create("bpmn:FormalExpression", {
      body: conditionValue
    });

    const conditionEventAttrs = {
      id: `ConditionalEventDefinition_${uuid(8)}`,
      condition: conditionExpression,
    };

    if (taskAttr.variableName) {
      conditionEventAttrs.variableName = taskAttr.variableName;
    }
    if (taskAttr.variableEvent) {
      conditionEventAttrs.variableEvent = taskAttr.variableEvent;
    }

    const conditionEventDefinition = modelerStore.getModdle.create("bpmn:ConditionalEventDefinition", conditionEventAttrs);
    modelerStore.getBpmnElement.businessObject.eventDefinitions = [conditionEventDefinition];
  } else {
    bpmnFormData.value.variableName = null;
    bpmnFormData.value.variableEvent = [];
    modelerStore.getBpmnElement.businessObject.eventDefinitions =
        modelerStore.getBpmnElement.businessObject.eventDefinitions.filter(ex => ex.$type !== 'bpmn:ConditionalEventDefinition');
  }
}
</script>
