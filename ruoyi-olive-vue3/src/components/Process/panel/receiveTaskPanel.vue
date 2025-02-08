<template>
  <div style="margin-top: 16px">
    <el-form label-width="80px">
<!--      <el-form-item label="消息实例">-->
<!--          <el-select v-model="bindMessageId" @change="updateTaskMessage">-->
<!--            <el-option v-for="id in Object.keys(messageMap)" :value="id" :label="messageMap[id]" :key="id" />-->
<!--          </el-select>-->
<!--      </el-form-item>-->
      <el-empty description="接收任务无配置内容"/>
    </el-form>
  </div>
</template>

<script setup name="ReceiveTaskPanel">
import useModelerStore from '@/store/modules/modeler'
const modelerStore = useModelerStore()
const { proxy } = getCurrentInstance();

const bindMessageId = ref("");
const messageMap = ref({});
const bpmnMessageRefsMap = ref({});
const bpmnRootElements = ref([]);

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
        // proxy.$nextTick(() => getBindMessage());
      }
    },
    {immediate: true}
)
// initReceiveTask();
function initReceiveTask() {
  bpmnMessageRefsMap.value = Object.create(null);
  bpmnRootElements.value = modelerStore.modeler.getDefinitions().rootElements;
  bpmnRootElements.value
      .filter(el => el.$type === "bpmn:Message")
      .forEach(m => {
        bpmnMessageRefsMap.value[m.id] = m;
        Reflect.set(messageMap.value, m.id, m.name);
      });
  Reflect.set(messageMap.value, "-1", "无"); // 添加一个空对象，保证可以取消原消息绑定
}

function getBindMessage() {
  bindMessageId.value = modelerStore.getBpmnElement.businessObject?.messageRef?.id || "-1";
  deleteElementFormExtensions();
}

function updateTaskMessage(messageId) {
  if (messageId === "-1") {
    modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {
      messageRef: null
    });
  } else {
    modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {
      messageRef: bpmnMessageRefsMap.value[messageId]
    });
  }
}

// 新建节点时,默认是用户节点,会添加表单相关扩展属性,当前任务直接删除掉
function deleteElementFormExtensions() {
  const element = modelerStore.getBpmnElement;
  if (element.businessObject?.extensionElements?.values) {
    element.businessObject.extensionElements.values =
        element.businessObject.extensionElements.values.filter(ex => ex.$type !== 'flowable:FormItems');
  }
}
</script>
