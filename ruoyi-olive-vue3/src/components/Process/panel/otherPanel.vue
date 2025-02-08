<template>
  <el-form label-width="80px" status-icon @submit.native.prevent>
    <el-form-item label="跳过表达式">
      <el-input v-model="bpmnFormData.skipExpression" @change="updateElementTask('skipExpression')"/>
    </el-form-item>
    <el-form-item label="是否为补偿">
      <el-input v-model="bpmnFormData.isForCompensation" @change="updateElementTask('isForCompensation')"/>
    </el-form-item>
    <el-form-item label="服务任务可触发">
      <el-input v-model="bpmnFormData.triggerable" @change="updateElementTask('triggerable')"/>
    </el-form-item>
  </el-form>
</template>

<script setup name="OtherPanel">
import useModelerStore from '@/store/modules/modeler'
import {StrUtil} from "@/utils/StrUtil";
const modelerStore = useModelerStore()
const { proxy } = getCurrentInstance();

/** 组件传值  */
const props = defineProps({
  id: {
    type: String,
    required: true
  },
});

const data = reactive({
  bpmnFormData: {}
});

const { bpmnFormData } = toRefs(data);

/** 传值监听 */
watch(() => props.id, newVal => {
      if (newVal) {
        resetFlowForm();
      }
    },
    {immediate: true}
)

// 方法区
function resetFlowForm() {
  bpmnFormData.value = JSON.parse(JSON.stringify(modelerStore.getBpmnElement.businessObject));
}

function updateElementTask(key) {
  const taskAttr = Object.create(null);
  taskAttr[key] = bpmnFormData.value[key] || null;
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, taskAttr);
}

</script>
