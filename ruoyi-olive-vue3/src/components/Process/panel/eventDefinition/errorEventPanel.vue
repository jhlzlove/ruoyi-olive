<template>
  <div>
    <el-form-item label="错误事件">
      <el-select v-model="bpmnFormData.errorValue" class="m-2" placeholder="请选择错误事件" clearable @change="handleErrorChange" @clear="removeError">
        <el-option
            v-for="item in errorList"
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

<script setup name="ErrorEventPanel">
import useModelerStore from '@/store/modules/modeler'
import {uuid, checkEventType} from "@/components/Process/common/bpmnUtils";

const modelerStore = useModelerStore()

const { proxy } = getCurrentInstance();
const errorList = ref([]);
const data = reactive({
    bpmnFormData: {
        errorValue: "",
        eventDefinitions: []
    },
});

const { rules, bpmnFormData} = toRefs(data);

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
    errorValue: "",
    eventDefinitions: []
  };
  getError();
  bpmnFormData.value.$type = modelerStore.getBpmnElement?.businessObject?.$type;
  // 处理事件
  if (checkEventType(bpmnFormData.value.$type)) {
    getFlowEvent(bpmnFormData.value, modelerStore.getBpmnElement.businessObject)
  }
}

// 处理事件
function getFlowEvent(bpmnFormData, businessObject) {
  if (businessObject.eventDefinitions && businessObject.eventDefinitions.length > 0) {
      if (businessObject.eventDefinitions[0].$type.indexOf("Error") !== -1) {
      if (businessObject.eventDefinitions[0].signalRef) {
        bpmnFormData.errorValue = businessObject.eventDefinitions[0].signalRef.id;
      }
    }
  }
}

// 更新错误事件
function handleErrorChange(value) {
  const error = errorList.value.find(element => element.$type === "bpmn:Error" && element.id === value)
  const errorEventDefinition = modelerStore.getModdle.create("bpmn:ErrorEventDefinition", {
    id: `ErrorEventDefinition${uuid(8)}`,
    errorRef: error
  });
  modelerStore.getBpmnElement.businessObject.eventDefinitions = [errorEventDefinition];
}

// 删除错误
function removeError() {
  proxy.$confirm("确认移除错误吗？", "提示", {
    confirmButtonText: "确 认",
    cancelButtonText: "取 消",
    type: 'warning',
    center: true
  }).then(() => {
    const element = modelerStore.getBpmnElement;
    if (element.businessObject?.eventDefinitions) {
      element.businessObject.eventDefinitions =
          element.businessObject.eventDefinitions.filter(ex => ex.$type !== 'bpmn:ErrorEventDefinition');
    }
  }).catch(() => console.info("操作取消"))
}

// 获取全局定义错误事件
function getError() {
  const rootElements = modelerStore.getModeler.getDefinitions().rootElements;
  errorList.value = [];
  rootElements.forEach(el => {
    if (el.$type === "bpmn:Error") {
      errorList.value.push({...el});
    }
  });
}

</script>
