<template>
  <div>
    <el-form-item label="消息事件">
      <el-select v-model="bpmnFormData.messageValue" class="m-2" placeholder="请选择消息事件" clearable @change="handleMessageChange" @clear="removeMessage">
        <el-option
            v-for="item in messageList"
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

<script setup name="MessageEventPanel">
import useModelerStore from '@/store/modules/modeler'
import {uuid, checkEventType} from "@/components/Process/common/bpmnUtils";

const modelerStore = useModelerStore()

const { proxy } = getCurrentInstance();
const messageList = ref([]);
const data = reactive({
    bpmnFormData: {
        messageValue: "",
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
    messageValue: "",
    eventDefinitions: []
  };
  getMassage();
  bpmnFormData.value.$type = modelerStore.getBpmnElement?.businessObject?.$type;
  // 处理事件
  if (checkEventType(bpmnFormData.value.$type)) {
    getFlowEvent(bpmnFormData.value, modelerStore.getBpmnElement.businessObject)
  }
}

// 处理事件
function getFlowEvent(bpmnFormData, businessObject) {
  if (businessObject.eventDefinitions && businessObject.eventDefinitions.length > 0) {
     if (businessObject.eventDefinitions[0].$type.indexOf("Message") !== -1) {
      if (businessObject.eventDefinitions[0].messageRef) {
        bpmnFormData.messageValue = businessObject.eventDefinitions[0].messageRef.id;
      }
    }
  }
}

// 更新消息事件
function handleMessageChange(value) {
  const message = messageList.value.find(element => element.$type === "bpmn:Message" && element.id === value)
  const messageEventDefinition = modelerStore.getModdle.create("bpmn:MessageEventDefinition", {
    id: `MessageEventDefinition_${uuid(8)}`,
    messageRef: message
  });
  modelerStore.getBpmnElement.businessObject.eventDefinitions = [messageEventDefinition];
}

// 删除消息
function removeMessage() {
  proxy.$confirm("确认移除消息吗？", "提示", {
    confirmButtonText: "确 认",
    cancelButtonText: "取 消",
    type: 'warning',
    center: true
  }).then(() => {
    const element = modelerStore.getBpmnElement;
    if (element.businessObject?.eventDefinitions) {
      element.businessObject.eventDefinitions =
          element.businessObject.eventDefinitions.filter(ex => ex.$type !== 'bpmn:MessageEventDefinition');
    }
  }).catch(() => console.info("操作取消"))
}

// 获取全局定义消息
function getMassage() {
  const rootElements = modelerStore.getModeler.getDefinitions().rootElements;
  messageList.value = [];
  rootElements.forEach(el => {
    if (el.$type === "bpmn:Message") {
      messageList.value.push({...el});
    }
  });
}

</script>
