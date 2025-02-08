<template>
  <div>
    <el-form-item label="信号事件">
      <el-select v-model="bpmnFormData.signalValue" class="m-2" placeholder="请选择信号事件" clearable @change="handleSignalChange" @clear="removeSignal">
        <el-option
            v-for="item in signalList"
            :key="item.id"
            :label="item.name"
            :value="item.id">
          <span style="float: left">{{ item.name }}</span>
          <span style="float: right; color: #8492a6; font-size: 13px">{{ item.id }}</span>
        </el-option>
      </el-select>
    </el-form-item>
  </div>
</template>

<script setup name="SignalEventPanel">
import useModelerStore from '@/store/modules/modeler'
import {uuid, checkEventType} from "@/components/Process/common/bpmnUtils";
const modelerStore = useModelerStore()

const { proxy } = getCurrentInstance();
const signalList = ref([]);
const data = reactive({
    bpmnFormData: {
        signalValue: "",
        eventDefinitions: []
    },
});

const { bpmnFormData} = toRefs(data);

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
    signalValue: "",
    eventDefinitions: []
  };
  getSignal();
  bpmnFormData.value.$type = modelerStore.getBpmnElement?.businessObject?.$type;
  // 处理事件
  if (checkEventType(bpmnFormData.value.$type)) {
    getFlowEvent(bpmnFormData.value, modelerStore.getBpmnElement.businessObject)
  }
}

// 处理事件
function getFlowEvent(bpmnFormData, businessObject) {
  if (businessObject.eventDefinitions && businessObject.eventDefinitions.length > 0) {
     if (businessObject.eventDefinitions[0].$type.indexOf("Signal") !== -1) {
      if (businessObject.eventDefinitions[0].signalRef) {
        bpmnFormData.signalValue = businessObject.eventDefinitions[0].signalRef.id;
      }
    }
  }
}

// 更新信号事件
function handleSignalChange(value) {
  const signal = signalList.value.find(element => element.$type === "bpmn:Signal" && element.id === value)
  const signalEventDefinition = modelerStore.getModdle.create("bpmn:SignalEventDefinition", {
    id: `SignalEventDefinition_${uuid(8)}`,
    signalRef: signal
  });
  modelerStore.getBpmnElement.businessObject.eventDefinitions = [signalEventDefinition];
}

// 删除信号
function removeSignal() {
  proxy.$confirm("确认移除信号吗？", "提示", {
    confirmButtonText: "确 认",
    cancelButtonText: "取 消",
    type: 'warning',
    center: true
  }).then(() => {
    const element = modelerStore.getBpmnElement;
    console.log(element.businessObject?.eventDefinitions,"element.businessObject?.eventDefinitions")
    if (element.businessObject?.eventDefinitions) {
      element.businessObject.eventDefinitions =
          element.businessObject.eventDefinitions.filter(ex => ex.$type !== 'bpmn:SignalEventDefinition');
    }
  }).catch(() => console.info("操作取消"))
}

// 获取全局定义信号
function getSignal() {
  const rootElements = modelerStore.getModeler.getDefinitions().rootElements;
  signalList.value = [];
  rootElements.forEach(el => {
    if (el.$type === "bpmn:Signal") {
      signalList.value.push({...el});
    }
  });
}

</script>
